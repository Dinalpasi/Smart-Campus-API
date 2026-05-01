package org.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiInfo() {
        String jsonResponse = "{\"version\": \"v1\", \"rooms\": \"/api/v1/rooms\", \"sensors\": \"/api/v1/sensors\"}";
        return Response.ok(jsonResponse).build();
    }
}

