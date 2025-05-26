import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class Vehicle {
    private double x, y;           // Position
    private double velocityX, velocityY;  // Velocity
    private double angle;          // Rotation angle
    private double wheelAngle;     // Wheel rotation
    private double fuel = 100;     // Fuel level
    private boolean isFlipped;     // Track if vehicle is flipped
    
    // Vehicle dimensions
    private static final int BODY_WIDTH = 60;
    private static final int BODY_HEIGHT = 30;
    private static final int WHEEL_RADIUS = 15;
    
    // Physics constants
    private static final double GRAVITY = 0.5;
    private static final double FRICTION = 0.98;
    private static final double MAX_VELOCITY = 15;
    private static final double ACCELERATION = 0.2;
    private static final double BRAKE_FACTOR = 0.95;
    
    public Vehicle(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.angle = 0;
        this.wheelAngle = 0;
        this.velocityX = 0;
        this.velocityY = 0;
    }
    
    public void update(boolean isAccelerating, boolean isBraking, double terrainAngle) {
        // Apply gravity
        velocityY += GRAVITY;
        
        // Handle acceleration and braking
        if (isAccelerating && fuel > 0) {
            velocityX = Math.min(MAX_VELOCITY, velocityX + ACCELERATION);
            fuel -= 0.1;
            wheelAngle += 5;
        }
        if (isBraking) {
            velocityX *= BRAKE_FACTOR;
            wheelAngle -= 2;
        }
        
        // Apply terrain angle to vehicle
        angle = terrainAngle;
        
        // Update position with terrain following
        x += velocityX;
        y += velocityY;
        
        // Check if flipped
        isFlipped = Math.abs(angle) > 90;
        
        // Apply friction
        velocityX *= FRICTION;
        
        // Prevent excessive velocity
        velocityX = Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, velocityX));
        velocityY = Math.max(-MAX_VELOCITY, Math.min(MAX_VELOCITY, velocityY));
    }
    
    public void draw(Graphics2D g2d) {
        AffineTransform oldTransform = g2d.getTransform();
        
        // Save current transform
        g2d.translate(x, y);
        g2d.rotate(Math.toRadians(angle));
        
        // Draw body
        g2d.setColor(Color.RED);
        g2d.fillRect(-BODY_WIDTH/2, -BODY_HEIGHT/2, BODY_WIDTH, BODY_HEIGHT);
        
        // Draw wheels
        g2d.setColor(Color.BLACK);
        // Front wheel
        g2d.translate(BODY_WIDTH/2, BODY_HEIGHT/2);
        g2d.rotate(Math.toRadians(wheelAngle));
        g2d.fillOval(-WHEEL_RADIUS, -WHEEL_RADIUS, WHEEL_RADIUS*2, WHEEL_RADIUS*2);
        g2d.rotate(-Math.toRadians(wheelAngle));
        g2d.translate(-BODY_WIDTH/2, -BODY_HEIGHT/2);
        
        // Back wheel
        g2d.translate(-BODY_WIDTH/2, BODY_HEIGHT/2);
        g2d.rotate(Math.toRadians(wheelAngle));
        g2d.fillOval(-WHEEL_RADIUS, -WHEEL_RADIUS, WHEEL_RADIUS*2, WHEEL_RADIUS*2);
        
        // Restore transform
        g2d.setTransform(oldTransform);
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getFuel() { return fuel; }
    public boolean isFlipped() { return isFlipped; }
    public void addFuel(double amount) { fuel = Math.min(100, fuel + amount); }
    
    // Set position (used for terrain collision)
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }
} 