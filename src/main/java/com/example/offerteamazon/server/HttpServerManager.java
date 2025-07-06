package com.example.offerteamazon.server;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Component
public class HttpServerManager {
    private Process serverProcess;

    public void startServer() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "sh", "http-server /home/daniele/libri -p 81");
            processBuilder.redirectErrorStream(true);
            serverProcess = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            System.out.println("Script started with sudo.");
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
