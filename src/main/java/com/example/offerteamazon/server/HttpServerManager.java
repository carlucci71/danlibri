package com.example.offerteamazon.server;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Component
public class HttpServerManager {
    private static final String PID_FILE = "/home/daniele/lanciaLibri.pid";
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

/*    public void stopServer() {
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
*/
public void stopScript() {
    try {
        // Leggi il PID dal file
        BufferedReader reader = new BufferedReader(new FileReader(PID_FILE));
        String line = reader.readLine();
        if (line != null) {
            int pid = Integer.parseInt(line);
            // Termina il processo
            Process killProcess = Runtime.getRuntime().exec("kill " + pid);
            killProcess.waitFor();
            System.out.println("Script stopped.");
        } else {
            System.out.println("PID file is empty or not found.");
        }
        reader.close();
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
}

}
