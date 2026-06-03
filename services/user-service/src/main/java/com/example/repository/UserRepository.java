package com.example.repository;

import com.example.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.*;

public interface UserRepository extends Neo4jRepository<User, String> {
     List<User> findByUsername(String username);
     List<User> findByEmail(String email);
     List<User> findByUsernameContainingIgnoreCase(String username);

     @Query("MATCH (u1:User {id: $currentUserId}), (u2:User {id: $targetUserId}) MERGE (u1)-[:FOLLOWS]->(u2)")
     void followUser(String currentUserId, String targetUserId);

     @Query("MATCH (u1:User {id: $currentUserId})-[r:FOLLOWS]->(u2:User {id: $targetUserId}) DELETE r")
     void unfollowUser(String currentUserId, String targetUserId);

     @Query("MATCH (u:User {id: $userId})-[:FOLLOWS]->(f:User) RETURN f.id")
     List<String> findFollowingIds(String userId);
}