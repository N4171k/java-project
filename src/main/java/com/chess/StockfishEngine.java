package com.chess;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.nio.file.attribute.PosixFilePermissions;

public class StockfishEngine {
    private Process stockfishProcess;
    private BufferedReader reader;
    private BufferedWriter writer;
    private static final String STOCKFISH_RESOURCE_PATH = "/stockfish/";
    private static final int DEFAULT_MOVE_TIME = 1000; // 1 second

    public StockfishEngine() {
        try {
            initializeEngine();
        } catch (IOException e) {
            System.err.println("Failed to initialize Stockfish engine: " + e.getMessage());
        }
    }

    private void initializeEngine() throws IOException {
        extractStockfishBinary();

        reader = new BufferedReader(new InputStreamReader(stockfishProcess.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(stockfishProcess.getOutputStream()));

        // Initialize UCI protocol
        sendCommand("uci");
        waitForReady();
    }

    private void extractStockfishBinary() throws IOException {
        String binaryName = getBinaryName();
        Path tempDir = Files.createTempDirectory("stockfish");
        Path binaryPath = tempDir.resolve(binaryName);
        
        // First try to load from classpath resources
        try (InputStream is = getClass().getResourceAsStream("/stockfish/" + binaryName)) {
            if (is != null) {
                Files.copy(is, binaryPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                // If not found in classpath, try to load from file system
                Path fileSystemPath = Paths.get("src", "main", "resources", "stockfish", binaryName);
                if (Files.exists(fileSystemPath)) {
                    Files.copy(fileSystemPath, binaryPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    throw new IOException("Stockfish binary not found in resources: " + binaryName);
                }
            }
        }
        
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            Files.setPosixFilePermissions(binaryPath, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
        
        stockfishProcess = new ProcessBuilder(binaryPath.toString()).start();
    }

    private String getBinaryName() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        
        if (os.contains("windows")) {
            return "stockfish-windows-x86-64.exe";
        } else if (os.contains("linux")) {
            return "stockfish-linux-x86-64";
        } else if (os.contains("mac")) {
            return "stockfish-mac-x86-64";
        } else {
            throw new RuntimeException("Unsupported operating system: " + os);
        }
    }

    public void sendCommand(String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
    }

    private void waitForReady() throws IOException {
        sendCommand("isready");
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("readyok")) {
                break;
            }
        }
    }

    public String getBestMove(String fen, int moveTime) throws IOException {
        sendCommand("position fen " + fen);
        sendCommand("go movetime " + moveTime);

        String line;
        String bestMove = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("bestmove")) {
                bestMove = line.split(" ")[1];
                break;
            }
        }

        return bestMove;
    }

    public void stop() {
        if (stockfishProcess != null) {
            try {
                sendCommand("quit");
                stockfishProcess.waitFor(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                stockfishProcess.destroyForcibly();
            }
        }
    }
} 