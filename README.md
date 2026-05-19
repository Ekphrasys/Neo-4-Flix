# Neo-4-Flix

Neo-4-Flix is a modern movie streaming, reference, and recommendation platform built on a robust microservices architecture. It demonstrates modern full-stack development practices, utilizing a graph database for intelligent content suggestion and scalable backend services.

## Exhaustive Feature List

### 1. Advanced Authentication & User Security
Security is a core focus of the system, primarily managed by the **User-Service** and protected through various backend checks and Angular routing guards on the frontend.
- **JWT-Based Authorization**: A fully stateless login and registration flow utilizing JSON Web Tokens. Secure endpoints require Bearer tokens to be accessed.
- **Strong Password Policy Validation**: When creating an account or updating credentials, users must provide a password of at least 8 characters, including uppercase letters, lowercase letters, numbers, and special symbols. This is strictly validated synchronously on the backend.
- **Two-Factor Authentication (2FA)**: Time-based One-Time Password (TOTP) integration via Google Authenticator. Users have the option to enable a two-step verification process to further secure their accounts. A dedicated Angular component handles the QR code display and setup flow.
- **Role-Based Access Control**: Route guards restrict unauthorized users from accessing specific functional areas or administrative features of the frontend application.

### 2. Movie Catalog & Microservices Integration
The **Movie-Service** acts as the central hub for exposing film data and metadata to the application interface.
- **Movie CRUD Operations**: Robust backend functionality for creating, reading, updating, and deleting movie records within the application ecosystem.
- **Seamless Frontend Presentation**: The Angular application includes a dedicated list component styled with a premium "Netflix-inspired" dynamic layout and aesthetically pleasing CSS displays for browsing catalogs.
- **Resilient API Communication**: Dedicated client architecture that prevents parsing errors and ensures reliable HTTP JSON communication between the Angular UI and backend Spring Boot microservices.

### 3. Intelligent Graph-Based Recommendation Engine
Instead of traditional relational databases, Neo-4-Flix uses **Neo4j** (Cypher DB) to map the extensive relationships between viewers and the media they consume.
- **Graph Data Modeling**: Users and movies act as nodes, while user ratings and previous interactions act as the edges linking the nodes together.
- **Personalized Suggestions**: The recommendation engine queries the graph structure directly to suggest new movies based on complex paths and collaborative filtering (e.g., matching a user's viewing history with those of users demonstrating similar tastes).

### 4. Independent Rating Ecosystem
The **Rating-Service** operates independently to capture and process user feedback without blocking the core application logic.
- **Submit and Store Ratings**: Users can seamlessly submit ratings for physical movie assets directly from the frontend interface. 
- **Data Hydration**: Ratings are passed securely to the graph database to continuously influence the recommendation engine's accuracy, acting as the primary input loop for refining future personalized suggestions.

### 5. Code Quality Assurance & CI/CD Pipelines
The project enforces strict software engineering standards through automated pipelines, testing boundaries, and quality gates.
- **Unit Testing Lifecycle**: Intensive integration of unit tests (JUnit and TS specs) across Angular logic, Spring Boot controllers, and secure configurations, complete with comprehensive dependency mocking.
- **Test Coverage Metrics (JaCoCo)**: Automatic coverage reports are generated during the Maven build lifecycle to ensure critical logic pathways remain rigorously tested during development.
- **Continuous Integration (Jenkins)**: A standalone `Jenkinsfile` fully controls automated build, test, and reporting environments directly configured into the project's root space.
- **Static Code Analysis (SonarQube)**: Configured via `sonar-project.properties`, the application undergoes automated pipeline checks to eliminate cognitive complexity bottlenecks, highlight code smells, and solve security vulnerabilities instantly (like enforcing Subresource Integrity).

## Prerequisites

To run this project, make sure you have the following installed on your machine:
- Docker and Docker Compose (e.g., via Docker Desktop for Windows or Mac users).

## How to Run the Project

For the initial startup, or whenever you make significant structural changes or updates to the code, use the following command to build the Docker images and start the services:

```bash
docker compose up --build
```

*(Note for Windows users: Ensure Docker Desktop is completely launched and running before executing these commands).*

For all subsequent starts without rebuilding the images:

```bash
docker compose up
```

## How to Stop the Project

To gracefully stop and remove the running Docker containers, use:

```bash
docker compose down
```

## Exposed Services and Ports

The application communicates over several mapped ports. Below is a detailed mapping of the services when running locally:

| Service | Port | Description |
| :--- | :--- | :--- |
| Frontend | 4200 | Client-side Angular web application (User Interface) |
| Neo4j (HTTP) | 7474 | Cypher DB web administration interface |
| Neo4j (Bolt) | 7687 | Internal application communication port for the graph database |
| Jenkins | 8080 | Continuous Integration server |
| User-service | 8081 | Spring Boot API for user management and authentication |
| Movie-service | 8082 | Spring Boot API for the movie catalog and recommendation engine |
| Rating-service | 8083 | Spring Boot API for handling and processing user movie ratings |

## Frontend Interface & Application Flow

The frontend client is built with Angular and provides a smooth, Netflix-inspired user experience. Below is a breakdown of the core pages and how they manage user interactions:

### 1. Registration Page (`/register`)
- **Purpose**: Allows new users to create an account.
- **How it works**: The user inputs their desired credentials. The component synchronously checks if the password meets the strong password policy (verifying length, capitalization, and special characters). Upon successful creation, the user can be immediately prompted to configure their Two-Factor Authentication (2FA).

### 2. Login Page (`/login`)
- **Purpose**: Authenticates returning users securely.
- **How it works**: The user enters their username and password. The frontend sends a request to the backend `user-service`. If the credentials are valid, a JWT (JSON Web Token) is returned. If the user has 2FA enabled, they are automatically prompted with a secondary verification step to enter their Google Authenticator code before the token is granted. 

### 3. Two-Factor Setup Page (`/two-factor-setup`)
- **Purpose**: Provides the configuration interface for optional 2FA.
- **How it works**: When a user opts into 2FA, this page communicates with the backend to generate a unique TOTP secret. It displays a dynamically generated QR Code that the user can scan with the Google Authenticator app. The user must then enter their first generated pin to verify and finalize the setup.

### 4. Movie Catalog Page (`/movie-list`)
- **Purpose**: Serves as the primary browsing interface.
- **How it works**: Styled using dark-mode aesthetic CSS elements, this component fetches a broad catalog of movies from the `movie-service`. It handles API JSON parsing and displays the films in an easily navigable grid. The UI ensures graceful error handling if backend services are temporarily unreachable.

### 5. Movie Details & Interactions (`/movie-detail`)
- **Purpose**: Displays comprehensive information regarding a specific movie.
- **How it works**: Users can click on a movie card to navigate to this page. Here, the system fetches metadata, descriptions, and current ratings. From this interface, users can submit their own ratings (sending data to the `rating-service`) or interact with the content, actively feeding the graph database's recommendation algorithms.

### 6. Personal Watchlist (`/watchlist`)
- **Purpose**: Allows users to save movies for later viewing.
- **How it works**: Users can curate a private list of content. The frontend maintains this state and synchronizes it with the backend, making the user's saved preferences persistent and accessible across different devices upon logging in.
