package com.example.offerteamazon.server;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HttpServerManager {
    private Process serverProcess;

    public void startServer() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "http-server /home/daniele/libri -p 81");
            serverProcess = processBuilder.start();
            System.out.println("Server started.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (serverProcess != null) {
            serverProcess.destroy();
            System.out.println("Server stopped.");
        } else {
            System.out.println("Server is not running.");
        }
    }

}
