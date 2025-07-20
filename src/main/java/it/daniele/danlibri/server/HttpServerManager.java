package it.daniele.danlibri.server;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Component
public class HttpServerManager {
    private static final String PID_FILE = "/home/daniele/lanciaLibri.pid";

    public void startServer() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "sh", "/home/daniele/lanciaLibri.sh");
            processBuilder.start();
            System.out.println("Script started with sudo.");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public void stopServer() {
        try {
            // Leggi il PID dal file
            BufferedReader reader = new BufferedReader(new FileReader(PID_FILE));
            String line = reader.readLine();
            if (line != null) {
                int pid = Integer.parseInt(line);
                // Termina il processo
                Process killProcess = Runtime.getRuntime().exec("kill " + pid);
                killProcess.waitFor();
                System.out.println("Server stopped.");
            } else {
                System.out.println("PID file is empty or not found.");
            }
            reader.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace(System.out);
        }
    }

}
