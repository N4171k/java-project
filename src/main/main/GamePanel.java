package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import input.InputHandler;
import car.PlayerCar;
import car.AICar;
import car.Car;
import track.Track;
import track.TrackLoader;
import obstacle.Obstacle;
import audio.SoundManager;

public class GamePanel extends JPanel implements Runnable {
    private Thread gameThread;
    private boolean running = false;
    private GameState gameState = GameState.MENU;

    private InputHandler inputHandler;
    private PlayerCar playerCar;
    private ArrayList<AICar> aiCars;
    private ArrayList<Obstacle> obstacles;
    private Track track;
    private SoundManager soundManager;
    private int score = 0;
    private int lap = 1;
    private int totalLaps = 3;
    private int difficultyLevel = 1;
    private long lastDifficultyIncrease = 0;
    private static final long DIFFICULTY_INCREASE_INTERVAL = 10000; // 10 seconds
    private int[] finishLine;
    private boolean crossedFinish = false;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocus();
        inputHandler = new InputHandler();
        addKeyListener(inputHandler);
        soundManager = new SoundManager();
        initGame();
        startGame();
    }

    private void initGame() {
        track = TrackLoader.loadTrack("default");
        finishLine = track.getFinishLine();
        playerCar = new PlayerCar(finishLine[0], finishLine[1]);
        aiCars = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            aiCars.add(new AICar(400 + i * 30, 150));
        }
        obstacles = new ArrayList<>();
        score = 0;
        lap = 1;
        crossedFinish = false;
        lastDifficultyIncrease = System.currentTimeMillis();
    }

    public void startGame() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        final int FPS = 60;
        final long targetTime = 1000 / FPS;

        while (running) {
            long startTime = System.nanoTime();

            updateGame();
            repaint();

            long elapsed = (System.nanoTime() - startTime) / 1000000;
            long wait = targetTime - elapsed;
            if (wait < 0) wait = 5;

            try {
                Thread.sleep(wait);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateGame() {
        switch (gameState) {
            case PLAYING:
                playerCar.update(inputHandler);
                for (AICar ai : aiCars) ai.update(track);
                // Check if player is on track
                if (!track.isOnTrack(playerCar.getX(), playerCar.getY())) {
                    soundManager.playSound("crash");
                    gameState = GameState.GAME_OVER;
                }
                // Collisions
                for (AICar ai : aiCars) {
                    if (playerCar.collidesWith(ai)) {
                        soundManager.playSound("crash");
                        gameState = GameState.GAME_OVER;
                    }
                }
                for (Obstacle obs : obstacles) {
                    if (playerCar.collidesWith(obs)) {
                        soundManager.playSound("crash");
                        gameState = GameState.GAME_OVER;
                    }
                }
                // Lap/finish line logic
                if (Math.abs(playerCar.getX() - finishLine[0]) < 40 && Math.abs(playerCar.getY() - finishLine[1]) < 40) {
                    if (!crossedFinish) {
                        lap++;
                        crossedFinish = true;
                        if (lap > totalLaps) gameState = GameState.GAME_OVER;
                    }
                } else {
                    crossedFinish = false;
                }
                // Difficulty increase
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastDifficultyIncrease > DIFFICULTY_INCREASE_INTERVAL) {
                    difficultyLevel++;
                    lastDifficultyIncrease = currentTime;
                    aiCars.add(new AICar(400 + (aiCars.size() * 30), 150));
                }
                score++;
                break;
            case MENU:
                if (inputHandler.isEnter()) {
                    gameState = GameState.PLAYING;
                    initGame();
                }
                break;
            case GAME_OVER:
                if (inputHandler.isEnter()) {
                    gameState = GameState.MENU;
                }
                break;
            case PAUSED:
                // No update
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cameraX = (int)playerCar.getX() - getWidth() / 2;
        int cameraY = (int)playerCar.getY() - getHeight() / 2;
        switch (gameState) {
            case PLAYING:
                track.draw(g, cameraX, cameraY);
                for (Obstacle obs : obstacles) obs.draw(g, cameraX, cameraY);
                for (AICar ai : aiCars) ai.draw(g, cameraX, cameraY);
                playerCar.drawAt(g, getWidth() / 2, getHeight() / 2);
                drawHUD(g);
                break;
            case MENU:
                drawMenu(g);
                break;
            case PAUSED:
                drawPause(g);
                break;
            case GAME_OVER:
                drawGameOver(g);
                break;
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Lap: " + lap + "/" + totalLaps, 10, 40);
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("2D Car Racing Game", 200, 200);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press ENTER to Start", 250, 300);
    }

    private void drawPause(Graphics g) {
        g.setColor(new Color(0,0,0,150));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Paused", 350, 300);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Game Over", 300, 250);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.setColor(Color.WHITE);
        g.drawString("Final Score: " + score, 320, 300);
        g.drawString("Press ENTER for Menu", 270, 350);
    }
} 