package com.bradandmarsha.bwisevaultawsauthtest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Main {
    public static void main(String[] args) throws URISyntaxException, UnsupportedEncodingException, IOException {
        String vaultDomain = "vault.aws.bradandmarsha.com";
        AwsIamLogin awsIamLogin = new AwsIamLogin();
        JsonObject requestData = new JsonObject();
        requestData.addProperty("iam_http_request_method", "POST");
        requestData.addProperty("iam_request_url", awsIamLogin.getBase64EncodedRequestUrl());
        requestData.addProperty("iam_request_body", awsIamLogin.getBase64EncodedRequestBody());
        requestData.addProperty("iam_request_headers", awsIamLogin.getBase64EncodedRequestHeaders(vaultDomain));

        HttpPost request = new HttpPost(String.format("https://%s/v1/auth/aws/login", vaultDomain));
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        request.setEntity(new StringEntity(requestData.toString()));
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String responseString = EntityUtils.toString(entity);
                System.out.println("Response from vault is:  " + responseString);
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = parser.parse(responseString).getAsJsonObject();
                JsonObject authObject = (JsonObject) jsonObject.get("auth");
                System.out.println("Vault token is: " + authObject.get("client_token").getAsString());
            }
        }
    }
}
