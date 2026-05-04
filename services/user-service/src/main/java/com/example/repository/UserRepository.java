package com.example.repository;

import com.example.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

public interface UserRepository extends Neo4jRepository<User, Long> {
     List<User> findByUsername(String username);
     List<User> findByEmail(String email);
}