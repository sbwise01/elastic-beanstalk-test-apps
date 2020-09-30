package com.foghornconsulting.bwisetest;

import com.amazonaws.DefaultRequest;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.http.HttpMethodName;
import com.google.common.collect.LinkedHashMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.Charsets;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class AwsIamLogin {

    private final String region;
    private final AWSCredentials credentials;
    private final String endpoint;
    private final String requestBody;

    public AwsIamLogin() {
        this.region = "us-east-1";
        this.credentials = new DefaultAWSCredentialsProviderChain().getCredentials();
        this.endpoint = String.format("https://sts.%s.amazonaws.com", region);
        this.requestBody = "Action=GetCallerIdentity&Version=2011-06-15";
    }

    /**
     * Based on https://github.com/BetterCloud/vault-java-driver/issues/118#issuecomment-427731009
     * and https://gist.github.com/kalpit/5670ff0729277e764981008dec67864b
     */
    public String getBase64EncodedRequestHeaders(String vaultDomain) throws URISyntaxException {

        final LinkedHashMap<String, String> headers = new LinkedHashMap<>();
        headers.put("X-Vault-AWS-IAM-Server-ID", vaultDomain);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

        final DefaultRequest defaultRequest = new DefaultRequest("sts");
        defaultRequest.setContent(new ByteArrayInputStream(requestBody.getBytes(Charsets.UTF_8)));
        defaultRequest.setHeaders(headers);
        defaultRequest.setHttpMethod(HttpMethodName.POST);
        defaultRequest.setEndpoint(new URI(endpoint));

        final AWS4Signer aws4Signer = new AWS4Signer();
        aws4Signer.setServiceName(defaultRequest.getServiceName());
        aws4Signer.setRegionName(region);
        aws4Signer.sign(defaultRequest, credentials);

        final LinkedHashMultimap<String, String> signedHeaders = LinkedHashMultimap.create();
        final Map<String, String> defaultRequestHeaders = defaultRequest.getHeaders();
        defaultRequestHeaders.entrySet().forEach(entry -> signedHeaders.put(entry.getKey(), entry.getValue()));

        final JsonObject jsonObject = new JsonObject();
        signedHeaders.asMap().forEach((k, v) -> {
            final JsonArray array = new JsonArray();
            v.forEach(array::add);
            jsonObject.add(k, array);
        });

        final String signedHeaderString = jsonObject.toString();
        return Base64.getEncoder().encodeToString(signedHeaderString.getBytes(Charsets.UTF_8));
    }

    public String getBase64EncodedRequestBody() {
        return Base64.getEncoder().encodeToString(requestBody.getBytes(Charsets.UTF_8));
    }

    public String getBase64EncodedRequestUrl() {
        return Base64.getEncoder().encodeToString(endpoint.getBytes(Charsets.UTF_8));
    }
}
