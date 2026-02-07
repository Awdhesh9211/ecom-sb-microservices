package com.ecommerce.userms.service.adminservice;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class KeycloakAdminService {

    @Value("${keycloak.admin.username}")
    private String adminUsername;
    @Value("${keycloak.admin.password}")
    private String adminPassword;
    @Value("${keycloak.admin.serveruri}")
    private String keycloakServerUrl;
    @Value("${keycloak.admin.realm}")
    private String realm;
    @Value("${keycloak.admin.clientId}")
    private String clientId;

    private final RestTemplate restTemplate= new RestTemplate();


    // ============== GET ADMIN ACCESS TOKEN ==============
    public String getAdminAccessToken() {

        String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("username", adminUsername);
        body.add("password", adminPassword);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }


    // ============== CREATE USER IN KEYCLOAK ==============
    public String createUser(String username, String email, String firstName, String lastName, String password) {

        String accessToken = getAdminAccessToken();

        String createUserUrl =
                keycloakServerUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("enabled", true);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", password);
        credentials.put("temporary", false);

        user.put("credentials", List.of(credentials));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(user, headers);

        ResponseEntity<Void> response =
                restTemplate.postForEntity(createUserUrl, request, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("User created successfully in Keycloak");
        } else {
            System.out.println("Failed to create user: " + response.getStatusCode());
            throw new RuntimeException("Failed to create user in keycloak "+response.getBody());
        }

        URI location=response.getHeaders().getLocation();
        if(location == null){
            throw new RuntimeException("Keycloak not gives location !");
        }
        String path= location.getPath();
        return path.substring(path.lastIndexOf("/")+1);
    }

    private Map<String, Object> getRealmRoleRepresentation(String token, String roleName) {

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + roleName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        Map.class
                );

        return response.getBody();
    }

    public void assignRealmRoleToUser(String username, String roleName, String userId) {

        String adminToken = getAdminAccessToken();

        // 1) Get role representation from Keycloak
        Map<String, Object> roleRepresentation =
                getRealmRoleRepresentation(adminToken, roleName);

        // Keycloak expects a LIST of roles (even for single role)
        List<Map<String, Object>> roles = List.of(roleRepresentation);

        String url = keycloakServerUrl +
                "/admin/realms/" + realm +
                "/users/" + userId +
                "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        HttpEntity<List<Map<String, Object>>> request =
                new HttpEntity<>(roles, headers);

        ResponseEntity<Void> response =
                restTemplate.exchange(url, HttpMethod.POST, request, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Role [" + roleName + "] assigned to user [" + username + "]");
        } else {
            throw new RuntimeException(
                    "Failed to assign role: " + response.getStatusCode()
            );
        }
    }


    // ============== DELETE USER IN KEYCLOAK ==============
    public void deleteUser(String userId) {

        String adminToken = getAdminAccessToken();

        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response =
                restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("User deleted from Keycloak: " + userId);
        } else {
            throw new RuntimeException("Failed to delete user from Keycloak: " + response.getStatusCode());
        }
    }


}
