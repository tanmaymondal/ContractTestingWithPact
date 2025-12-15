package com.example.pact.consumer;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class UserServiceClient {
    private final String baseUrl;
    private final Gson gson = new Gson();

    public UserServiceClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public User getUserById(int userId) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(baseUrl + "/users/" + userId);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String json = EntityUtils.toString(response.getEntity());
                return gson.fromJson(json, User.class);
            }
        }
    }
}
