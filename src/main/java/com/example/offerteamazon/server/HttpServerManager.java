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
            //processBuilder.redirectErrorStream(true);
            serverProcess = processBuilder.start();
            /*
            BufferedReader reader = new BufferedReader(new InputStreamReader(serverProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            */
            System.out.println("Script started with sudo.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        System.out.println("VOGLIO STOPPARE");
        if (serverProcess != null) {
            serverProcess.destroy();
            try {
                if (!serverProcess.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    System.out.println("FORZATO");
                    serverProcess.destroyForcibly();
                }
                System.out.println("Script stopped.");
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Script is not running.");
        }
    }

}
