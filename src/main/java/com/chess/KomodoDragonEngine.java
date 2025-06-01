package com.chess;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.nio.file.attribute.PosixFilePermissions;

public class KomodoDragonEngine {
    private Process dragonProcess;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static final String DRAGON_RESOURCE_PATH = "/komodo/";
    private static final int DEFAULT_MOVE_TIME = 1000; // 1 second

    public KomodoDragonEngine() {
        try {
            System.out.println("Initializing Komodo Dragon engine...");
            initializeEngine();
            System.out.println("Komodo Dragon engine initialized successfully");
        } catch (IOException e) {
            System.err.println("Failed to initialize Komodo Dragon engine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeEngine() throws IOException {
        System.out.println("Extracting Komodo Dragon binary...");
        extractDragonBinary();
        System.out.println("Binary extracted successfully");

        reader = new BufferedReader(new InputStreamReader(dragonProcess.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(dragonProcess.getOutputStream()));

        // Initialize UCI protocol
        System.out.println("Initializing UCI protocol...");
        sendCommand("uci");
        
        // Wait for engine to finish sending its configuration
        String line;
        boolean configComplete = false;
        while ((line = reader.readLine()) != null) {
            System.out.println("Engine response: " + line);
            if (line.startsWith("uciok")) {
                configComplete = true;
                break;
            }
        }
        
        if (!configComplete) {
            throw new IOException("Engine did not complete UCI configuration");
        }
        
        // Now send isready and wait for readyok
        System.out.println("Waiting for engine ready signal...");
        sendCommand("isready");
        
        int timeout = 0;
        while ((line = reader.readLine()) != null) {
            System.out.println("Engine response: " + line);
            if (line.equals("readyok")) {
                System.out.println("Engine is ready");
                return;
            }
            timeout++;
            if (timeout > 10) {
                throw new IOException("Engine did not respond with readyok after 10 attempts");
            }
        }
        throw new IOException("Engine process terminated while waiting for readyok");
    }

    private void extractDragonBinary() throws IOException {
        String binaryName = getBinaryName();
        System.out.println("Using binary: " + binaryName);
        Path tempDir = Files.createTempDirectory("komodo");
        Path binaryPath = tempDir.resolve(binaryName);
        System.out.println("Temporary directory: " + tempDir);
        
        // First try to load from classpath resources
        try (InputStream is = getClass().getResourceAsStream("/komodo/" + binaryName)) {
            if (is != null) {
                System.out.println("Found binary in classpath resources");
                Files.copy(is, binaryPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // If not found in classpath, try to load from file system
                Path fileSystemPath = Paths.get("src", "main", "resources", "komodo", binaryName);
                if (Files.exists(fileSystemPath)) {
                    System.out.println("Found binary in file system: " + fileSystemPath);
                    Files.copy(fileSystemPath, binaryPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    throw new IOException("Komodo Dragon binary not found in resources: " + binaryName);
                }
            }
        }
        
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            Files.setPosixFilePermissions(binaryPath, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
        
        System.out.println("Starting Komodo Dragon process...");
        dragonProcess = new ProcessBuilder(binaryPath.toString()).start();
        System.out.println("Process started with PID: " + dragonProcess.pid());
    }

    private String getBinaryName() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        System.out.println("OS: " + os + ", Architecture: " + arch);
        
        if (os.contains("windows")) {
            return "dragon-64bit-avx2.exe";
        } else if (os.contains("linux")) {
            return "komodo-dragon-linux-x86-64";
        } else if (os.contains("mac")) {
            return "komodo-dragon-mac-x86-64";
        } else {
            throw new RuntimeException("Unsupported operating system: " + os);
        }
    }

    public void sendCommand(String command) throws IOException {
        System.out.println("Sending command to engine: " + command);
        writer.write(command + "\n");
        writer.flush();
    }

    public String getBestMove(String fen, int moveTime) throws IOException {
        System.out.println("Getting best move for FEN: " + fen);
        sendCommand("position fen " + fen);
        sendCommand("go movetime " + moveTime);

        String line;
        String bestMove = null;
        int timeout = 0;
        while ((line = reader.readLine()) != null) {
            System.out.println("Engine response: " + line);
            if (line.startsWith("bestmove")) {
                bestMove = line.split(" ")[1];
                System.out.println("Best move found: " + bestMove);
                break;
            }
            timeout++;
            if (timeout > 100) { // Increased timeout for move calculation
                throw new IOException("Engine did not respond with bestmove after 100 attempts");
            }
        }
        
        if (bestMove == null) {
            throw new IOException("Engine process terminated while waiting for bestmove");
        }

        return bestMove;
    }

    public void stop() {
        if (dragonProcess != null) {
            try {
                System.out.println("Stopping Komodo Dragon engine...");
                sendCommand("quit");
                dragonProcess.waitFor(1, TimeUnit.SECONDS);
                System.out.println("Engine stopped successfully");
            } catch (Exception e) {
                System.err.println("Error stopping engine: " + e.getMessage());
                dragonProcess.destroyForcibly();
            }
        }
    }
} 