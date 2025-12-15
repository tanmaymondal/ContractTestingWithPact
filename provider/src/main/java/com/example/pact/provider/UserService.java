package com.example.pact.provider;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final Map<Integer, User> users = new HashMap<>();

    public UserService() {
        // Initialize with some test data
        users.put(1, new User(1, "John Doe", "john.doe@example.com"));
        users.put(2, new User(2, "Jane Smith", "jane.smith@example.com"));
    }

    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }
}
