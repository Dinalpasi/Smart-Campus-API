package org.example;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        URI baseUri = URI.create("http://0.0.0.0:8080/api/v1/");

        ResourceConfig config = new ResourceConfig()
                .register(DiscoveryResource.class)
                .register(RoomResource.class)
                .register(SensorResource.class)
                .register(LoggingFilter.class)
                .register(RoomNotEmptyExceptionMapper.class)
                .register(LinkedResourceNotFoundExceptionMapper.class);

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);

        System.out.println("Smart Campus API running at " + baseUri);
        System.out.println("Try: GET http://localhost:8080/api/v1/");
        System.out.println("Press ENTER to stop.");
        System.in.read();

        server.shutdownNow();
    }
}

