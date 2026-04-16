package com.consumer.service;

import com.consumer.entity.EwbApiReqRespMtb;
import com.consumer.entity.EwbApiUrlMtb;
import com.consumer.repository.EwbApiReqRespRepository;
import com.consumer.repository.EwbApiUrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class LdgApiService {

    private final EwbApiUrlRepository apiUrlRepo;
    private final EwbApiReqRespRepository reqRespRepo;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ldg.api.token}")
    private String token;

    public LdgApiService(EwbApiUrlRepository apiUrlRepo,
                         EwbApiReqRespRepository reqRespRepo) {
        this.apiUrlRepo = apiUrlRepo;
        this.reqRespRepo = reqRespRepo;
    }

    public void callLdgApi(String plainPayload) {

        // 1️⃣ Convert Plain → Base64
        String base64Payload = Base64.getEncoder()
                .encodeToString(plainPayload.getBytes(StandardCharsets.UTF_8));

        // 2️⃣ Fetch API config
        EwbApiUrlMtb api = apiUrlRepo.findByApiId("10000");
        String apiId=api.getApiId();
        if (api == null || !Boolean.TRUE.equals(api.getIsActive())) {
            saveError(apiId, plainPayload, "API config not found or inactive");
            return;
        }

        String url = api.getUrl();

        // 3️⃣ Build Request Body
        String requestBody = "{ \"rawPayload\": \"" + base64Payload + "\" }";

        // 4️⃣ Headers (FIXED AUTH)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ✔ Correct Bearer handling
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String response;

        try {

            ResponseEntity<String> apiResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            response = apiResponse.getBody();

        } catch (HttpStatusCodeException ex) {

            response = "{ \"status\": " + ex.getStatusCode().value() +
                    ", \"error\": \"" + ex.getStatusText() +
                    "\", \"body\": " + ex.getResponseBodyAsString() + " }";

        } catch (ResourceAccessException ex) {

            response = "{ \"status\": \"TIMEOUT\", \"error\": \"" +
                    ex.getMessage() + "\" }";

        } catch (Exception ex) {

            response = "{ \"status\": \"ERROR\", \"error\": \"" +
                    ex.getMessage() + "\" }";
        }

        // 5️⃣ Save Req/Resp
        EwbApiReqRespMtb rr = new EwbApiReqRespMtb();
        rr.setApiId(apiId);
        rr.setRequest(requestBody);
        rr.setResponse(response);
        rr.setCreatedBy("EVENT_HUB");
        rr.setCreatedOn(LocalDateTime.now());

        reqRespRepo.save(rr);
    }

    private void saveError(String apiId, String request, String errorMsg) {

        EwbApiReqRespMtb rr = new EwbApiReqRespMtb();
        rr.setApiId(apiId);
        rr.setRequest(request);
        rr.setResponse("{ \"status\": \"ERROR\", \"message\": \"" + errorMsg + "\" }");
        rr.setCreatedBy("EVENT_HUB");
        rr.setCreatedOn(LocalDateTime.now());

        reqRespRepo.save(rr);
    }
}