package com.example.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    private final RestTemplate restTemplate;

    public WebhookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Value("${app.name}")
    private String name;

    @Value("${app.regNo}")
    private String regNo;

    @Value("${app.email}")
    private String email;

    public void executeFlow() {
        try {
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            Map<String, Object> body = new HashMap<>();
            body.put("name", name);
            body.put("regNo", regNo);
            body.put("email", email);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, request, Map.class);

            if (response.getBody() == null) {
                System.out.println("No response from generateWebhook API.");
                return;
            }

            Map<String, Object> resp = response.getBody();
            String webhookUrl = (String) (resp.get("webhookUrl") != null ? resp.get("webhookUrl") : resp.get("webhook"));
            String accessToken = (String) resp.get("accessToken");

            if (webhookUrl == null || accessToken == null) {
                System.out.println("Missing webhookUrl/webhook or accessToken in response: " + resp);
                return;
            }

            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Access Token (JWT): " + accessToken);

            String finalQuery =
                "SELECT p.AMOUNT AS SALARY, " +
                "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                "d.DEPARTMENT_NAME " +
                "FROM PAYMENTS p " +
                "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
                "ORDER BY p.AMOUNT DESC LIMIT 1;";

            Map<String, Object> answer = new HashMap<>();
            answer.put("finalQuery", finalQuery);

            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setContentType(MediaType.APPLICATION_JSON);

            if (accessToken.startsWith("Bearer ")) {
                authHeaders.set("Authorization", accessToken);
            } else {
                authHeaders.setBearerAuth(accessToken);
            }

            HttpEntity<Map<String, Object>> answerReq = new HttpEntity<>(answer, authHeaders);

            ResponseEntity<String> submitResp = restTemplate.postForEntity(webhookUrl, answerReq, String.class);
            System.out.println("Submit response status: " + submitResp.getStatusCodeValue());
            System.out.println("Submit response body: " + submitResp.getBody());

        } catch (Exception e) {
            System.out.println("Error during flow: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
