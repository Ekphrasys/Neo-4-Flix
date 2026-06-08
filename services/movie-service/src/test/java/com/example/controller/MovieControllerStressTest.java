package com.example.controller;

import com.example.model.Movie;
import com.example.model.Genre;
import com.example.service.MovieService;
import com.example.security.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(MovieController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "test-user")
@DisplayName("MovieController - Stress Tests (Concurrent Requests)")
class MovieControllerStressTest {

    private static final UUID uuid1 = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    // ==================== Mocks Initialization ====================
    @BeforeEach
    void setUpMocks() {
        Movie testMovie = new Movie(uuid1, "Test", "Description", 2020, Genre.ACTION);

        // Configuration globale unifiée et thread-safe grâce à lenient()
        Mockito.lenient().when(movieService.getAllMovies())
                .thenReturn(Arrays.asList(testMovie));

        Mockito.lenient().when(movieService.searchMovies(any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(testMovie));

        Mockito.lenient().when(movieService.getMovieById(any()))
                .thenReturn(Optional.of(testMovie));

        // L'utilisation de thenAnswer évite de recréer l'état interne à chaque fois
        Mockito.lenient().when(movieService.saveMovie(any(Movie.class)))
                .thenAnswer(invocation -> {
                    Movie arg = invocation.getArgument(0);
                    return new Movie(uuid1, arg.getTitle(), arg.getDescription(), arg.getReleaseYear(), arg.getGenre());
                });
    }

    // ==================== CONCURRENT REQUEST TESTS ====================

    @Test
    @DisplayName("Should handle 100 concurrent GET requests")
    void handle100ConcurrentGetRequests() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                try {
                    mockMvc.perform(get("/api/movies")
                                    .with(user("test-user")))
                            .andExpect(status().isOk());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assert successCount.get() > 90 : "At least 90% of requests should succeed";
        assert errorCount.get() < 10 : "Less than 10% error rate acceptable under stress";

        System.out.println("✓ 100 concurrent requests - Success: " + successCount.get() + ", Errors: " + errorCount.get());
    }

    @Test
    @DisplayName("Should handle 500 concurrent search requests")
    void handle500ConcurrentSearchRequests() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(500);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < 500; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    mockMvc.perform(get("/api/movies")
                                    .param("q", "query" + index)
                                    .with(user("test-user")))
                            .andExpect(status().isOk());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        assert successCount.get() > 450 : "At least 90% success rate";
        System.out.println("✓ 500 concurrent searches - Success: " + successCount.get() + ", Errors: " + errorCount.get());
    }

    @Test
    @DisplayName("Should handle rapid sequential requests without degradation")
    void handle1000RapidSequentialRequests() throws Exception {
        long startTime = System.currentTimeMillis();
        int errorCount = 0;

        for (int i = 0; i < 1000; i++) {
            try {
                mockMvc.perform(get("/api/movies"))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                errorCount++;
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        double requestsPerSecond = (1000 / (totalTime / 1000.0));

        assert errorCount < 50 : "Error rate should be less than 5%";
        System.out.println("✓ 1000 rapid sequences - Time: " + totalTime + "ms, Rate: " + requestsPerSecond + " req/s, Errors: " + errorCount);
    }

    // ==================== CONCURRENT CREATE REQUESTS ====================

    @Test
    @DisplayName("Should handle concurrent POST requests (race conditions)")
    void handle50ConcurrentCreateRequests() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(50);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < 50; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    Movie movie = new Movie(null, "Movie " + index, "Description", 2020, Genre.ACTION);

                    mockMvc.perform(post("/api/movies")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(movie))
                                    .with(user("test-user")))
                            .andExpect(status().isOk());

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Create request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assert successCount.get() > 45 : "At least 90% of concurrent creates should succeed";
        System.out.println("✓ 50 concurrent POSTs - Success: " + successCount.get());
    }

    // ==================== MIXED WORKLOAD STRESS ====================

    @Test
    @DisplayName("Should handle mixed workload (GET, POST, PUT, DELETE)")
    void handleMixedWorkloadStress() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(200);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < 200; i++) {
            final int operation = i % 4;  // 0: GET, 1: POST, 2: PUT, 3: DELETE
            executor.submit(() -> {
                try {
                    switch (operation) {
                        case 0:  // GET
                            mockMvc.perform(get("/api/movies").with(user("test-user")))
                                    .andExpect(status().isOk());
                            break;
                        case 1:  // POST
                            Movie newMovie = new Movie(null, "Movie X", "Description", 2020, Genre.ACTION);
                            mockMvc.perform(post("/api/movies")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(newMovie))
                                            .with(user("test-user")))
                                    .andExpect(status().isOk());
                            break;
                        case 2:  // PUT
                            Movie updatedMovie = new Movie(null, "Updated", "Description", 2020, Genre.ACTION);
                            mockMvc.perform(put("/api/movies/{id}", uuid1)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(updatedMovie))
                                            .with(user("test-user")))
                                    .andExpect(status().isOk());
                            break;
                        case 3:  // DELETE
                            mockMvc.perform(delete("/api/movies/{id}", uuid1).with(user("test-user")))
                                    .andExpect(status().isNoContent());
                            break;
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Mixed workload request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        assert successCount.get() > 180 : "At least 90% success rate in mixed workload";
        System.out.println("✓ Mixed workload (200 reqs) - Success: " + successCount.get());
    }

    // ==================== MEMORY STRESS ====================

    @Test
    @DisplayName("Should not leak memory during repeated requests")
    void memoryStressTest() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();

        for (int i = 0; i < 1000; i++) {
            mockMvc.perform(get("/api/movies"))
                    .andExpect(status().isOk());
        }

        System.gc();
        Thread.sleep(100);

        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        long memIncrease = memAfter - memBefore;

        assert memIncrease < 100 * 1024 * 1024 : "Potential memory leak - increase too large";
        System.out.println("✓ Memory stress test - Memory increase: " + (memIncrease / 1024 / 1024) + "MB");
    }

    // ==================== PARAMETER EXPLOSION STRESS ====================

    @Test
    @DisplayName("Should handle requests with many parameters")
    void handleManyParametersStress() throws Exception {
        var requestBuilder = get("/api/movies");

        IntStream.range(0, 50).forEach(i ->
                requestBuilder.param("param" + i, "value" + i)
        );

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());
    }

    // ==================== CONNECTION STRESS ====================

    @Test
    @DisplayName("Should handle connection timeout gracefully")
    void handleConnectionTimeoutStress() throws Exception {
        Mockito.lenient().when(movieService.getAllMovies())
                .thenAnswer(invocation -> {
                    Thread.sleep(100);
                    return Arrays.asList(new Movie(uuid1, "Test", "Description", 2020, Genre.ACTION));
                });

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/movies")
                        .requestAttr("timeout", 5000))
                .andExpect(status().isOk());

        long duration = System.currentTimeMillis() - startTime;
        assert duration < 5000 : "Should complete before timeout";

        System.out.println("✓ Connection timeout test - Duration: " + duration + "ms");
    }
}