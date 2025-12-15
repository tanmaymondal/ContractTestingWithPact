package com.example.pact.consumer;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(PactConsumerTestExt.class)
public class UserServiceConsumerTest {

    @Pact(consumer = "UserConsumer", provider = "UserProvider")
    public V4Pact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder
                .given("user with id 1 exists")
                .uponReceiving("a request to get user by id")
                    .path("/users/1")
                    .method("GET")
                .willRespondWith()
                    .status(200)
                    .headers(headers)
                    .body("{\n" +
                          "  \"id\": 1,\n" +
                          "  \"name\": \"John Doe\",\n" +
                          "  \"email\": \"john.doe@example.com\"\n" +
                          "}")
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "createPact", pactVersion = PactSpecVersion.V4, port = "8080")
    void testGetUserById() throws IOException {
        UserServiceClient client = new UserServiceClient("http://localhost:8080");
        User user = client.getUserById(1);

        assertNotNull(user);
        assertEquals(1, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john.doe@example.com", user.getEmail());
    }
}
