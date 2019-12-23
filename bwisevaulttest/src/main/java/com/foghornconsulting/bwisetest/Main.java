/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.foghornconsulting.bwisetest;

import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 *
 * @author bwise
 */
@Path("")
public class Main {
    
    
    
    @GET
    @Produces("text/html")
    public String getHtml() {
        ArrayList<People> people = People.getPeople();
        String peopleSection = "";
        for (People person : people) {
            peopleSection = peopleSection + "<tr>" +
                                            "<td>" + person.getFirstName() + "</td>" +
                                            "<td>" + person.getLastName() + "</td>" +
                                            "</tr>";
        }
        String returnString = "<html lang=\"en\">" +
                                "<head>" +
                                "<style>" +
                                "table, th, td {" +
                                "    border: 1px solid black;" +
                                "}" +
                                "</style>" +
                                "</head>" +
                                "<body>" +
                                "<h1>Hello Tomcat Netbeans World v 1.10-SNAPSHOT!!</h1>" +
                                "<br></br>" +
                                "<h2>People Table</h2>" +
                                "<table style=\"width:40%\">" +
                                "<tr>" +
                                "<th>First Name</th>" +
                                "<th>Last Name</th>" +
                                "</tr>" +
                                peopleSection +
                                "</table>" +
                                "</body>" +
                                "</html>";
        return returnString;
    }
    
}
