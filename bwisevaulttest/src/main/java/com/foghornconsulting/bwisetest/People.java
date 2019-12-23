/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.foghornconsulting.bwisetest;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author bwise
 */
public class People implements Serializable {
    
    private final static Logger LOG = Logger.getLogger(People.class);
    
    private final String firstName;
    private final String lastName;
    
    public People(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public static ArrayList<People> getPeople() {
        ArrayList<People> people = new ArrayList<>();
        
        Connection db = null;
        ResultSet results = null;
        PreparedStatement stmt;
        String sql = "";
        try {
            String vaultToken = System.getProperty("VAULT_TOKEN");
            HttpGet request = new HttpGet("https://vault.aws.bradandmarsha.com/v1/database/creds/my-role");
            request.addHeader("X-Vault-Token", vaultToken);
            final CloseableHttpClient httpClient = HttpClients.createDefault();
            String userName = "";
            String password = "";
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String responseString = EntityUtils.toString(entity);
                    JSONObject jsonObject = (JSONObject) JSONValue.parse(responseString);
                    JSONObject responseData = (JSONObject) jsonObject.get("data");
                    userName = (String) responseData.get("username");
                    password = (String) responseData.get("password");
                }
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
            String dbName = System.getProperty("RDS_DB_NAME");
            String hostname = System.getProperty("RDS_HOSTNAME");
            String port = System.getProperty("RDS_PORT");
            String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
            LOG.info("Getting remote connection with connection string from environment variables.");
            db = DriverManager.getConnection(jdbcUrl);
            db.setAutoCommit(false);
            
            sql = "select firstname,lastname from people";
            stmt = db.prepareStatement(sql);
            LOG.info("sql:  "+stmt);
            results = stmt.executeQuery();
            while (results.next()) {
                try {
                    String firstName = results.getString("firstname");
                    String lastName = results.getString("lastname");
                    people.add(new People(firstName, lastName));
                } catch (Exception e) {
                    LOG.error("Error processing sql row results: ", e);
                }
            }
            stmt.close();

        } catch (Exception e) {
            LOG.error("SQL Exception executing or parsing results from SQL: " + sql, e);
        } finally {
            try {
                if (results != null) {
                    results.close();
                }
                if (db != null) {
                    db.close();
                }
            } catch (Exception e) {
                LOG.error("Error closing Result Set or Statement", e);
            }
        }
        
        return people;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

}
