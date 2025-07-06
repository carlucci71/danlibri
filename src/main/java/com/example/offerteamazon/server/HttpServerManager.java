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
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "sh", "/home/daniele/lanciaLibri.sh");
            serverProcess = processBuilder.start();
            System.out.println("Script started with sudo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (serverProcess != null) {
            serverProcess.destroy();
            try {
                if (!serverProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    serverProcess.destroyForcibly();
                }
                System.out.println("Script stopped.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Script is not running.");
        }
    }

}
