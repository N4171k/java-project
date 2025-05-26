import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int DELAY = 16; // ~60 FPS
    
    private Vehicle vehicle;
    private Terrain terrain;
    private Timer timer;
    private boolean isAccelerating;
    private boolean isBraking;
    private int score;
    private boolean gameOver;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
        
        initGame();
        
        timer = new Timer(DELAY, this);
        timer.start();
    }
    
    private void initGame() {
        terrain = new Terrain(5000, HEIGHT);
        vehicle = new Vehicle(100, (int)terrain.getHeightAt(100) - 50);
        score = 0;
        gameOver = false;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw terrain
        terrain.draw(g2d, (int)vehicle.getX() - WIDTH/3);
        
        // Draw vehicle
        vehicle.draw(g2d);
        
        // Draw HUD
        drawHUD(g2d);
        
        if (gameOver) {
            drawGameOver(g2d);
        }
    }
    
    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Draw score
        g2d.drawString("Score: " + score, 20, 30);
        
        // Draw fuel bar
        g2d.drawString("Fuel: " + (int)vehicle.getFuel() + "%", 20, 60);
        g2d.setColor(Color.GREEN);
        g2d.fillRect(100, 45, (int)(vehicle.getFuel() * 2), 20);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(100, 45, 200, 20);
    }
    
    private void drawGameOver(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString("Game Over", WIDTH/2 - 100, HEIGHT/2);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Final Score: " + score, WIDTH/2 - 60, HEIGHT/2 + 40);
        g2d.drawString("Press SPACE to restart", WIDTH/2 - 100, HEIGHT/2 + 80);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            updateGame();
            repaint();
        }
    }
    
    private void updateGame() {
        // Update vehicle
        vehicle.update(isAccelerating, isBraking, terrain.getAngleAt(vehicle.getX()));
        
        // Handle terrain collision
        double terrainHeight = terrain.getHeightAt(vehicle.getX());
        if (vehicle.getY() > terrainHeight - 50) { // 50 is the vehicle's height offset
            vehicle.setPosition(vehicle.getX(), terrainHeight - 50);
        }
        
        // Update score
        score = (int)(vehicle.getX() / 10);
        
        // Check game over conditions
        if (vehicle.isFlipped() || vehicle.getFuel() <= 0 || terrain.isOutOfBounds(vehicle.getX())) {
            gameOver = true;
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                isAccelerating = true;
                break;
            case KeyEvent.VK_LEFT:
                isBraking = true;
                break;
            case KeyEvent.VK_SPACE:
                if (gameOver) {
                    initGame();
                }
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                isAccelerating = false;
                break;
            case KeyEvent.VK_LEFT:
                isBraking = false;
                break;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
} 