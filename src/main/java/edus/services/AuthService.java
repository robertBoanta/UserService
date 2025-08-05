package edus.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

    @Value("${auth0.client-id}")
    private String clientId;

    @Value("${auth0.client-secret}")
    private String clientSecret;

    @Value("${auth0.domain}")
    private String domain;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> getUserData(Jwt jwt) {
        Map<String, Object> userData = new HashMap<>();
        String namespace = "https://dev-78xqbotqc6dcympy.us.auth0.com/";

        // Standard claims
        userData.put("userId", jwt.getSubject());
        userData.put("email", jwt.getClaim("email"));
        userData.put("name", jwt.getClaim("name"));
        userData.put("emailVerified", jwt.getClaim("email_verified"));

        // Educational institution claims
        userData.put("roles", jwt.getClaim(namespace + "roles"));
        userData.put("institutieInvatamant", jwt.getClaim(namespace + "institutie_invatamant"));
        userData.put("numePrenume", jwt.getClaim(namespace + "nume_prenume"));
        userData.put("adreseLivrare", jwt.getClaim(namespace + "adrese_livrare"));
        userData.put("adresaFacturare", jwt.getClaim(namespace + "adresa_facturare"));

        return userData;
    }

    public void updateUserAdresses (String userId, String institutieInvatamant,
                                    String numePrenume, List<Object> adreseLivrare,
                                    Object adresaFacturare) {

        String accesToken = getManagementApiToken();

        Map<String, Object> appMetadata = new HashMap<>();
        appMetadata.put("institutie_invatamant", institutieInvatamant);
        appMetadata.put("nume_prenume", numePrenume);
        appMetadata.put("adrese_livrare", adreseLivrare);
        appMetadata.put("adresa_facturare", adresaFacturare);

        Map<String, Object> update = new HashMap<>();
        update.put("app_metadata", appMetadata);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accesToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(update, headers);

        String url = "https://" + domain + "/api/v2/users/" + userId;
        restTemplate.exchange(url, HttpMethod.PATCH, request, String.class);
    }

    private String getManagementApiToken() {
        Map<String, String> tokenRequest = new HashMap<>();
        tokenRequest.put("client_id", clientId);
        tokenRequest.put("client_secret", clientSecret);
        tokenRequest.put("audience", "https://" + domain + "/api/v2/");
        tokenRequest.put("grant_type", "client_credentials");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(tokenRequest, headers);

        String url = "https://" + domain + "/oauth/token";
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        return (String) response.getBody().get("access_token");
    }
}
