package obstacle;

import java.awt.Color;
import java.awt.Graphics;

public class Obstacle {
    private double x, y;

    public Obstacle(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void draw(Graphics g, int cameraX, int cameraY) {
        g.setColor(Color.YELLOW);
        g.fillOval((int)x - cameraX - 15, (int)y - cameraY - 15, 30, 30);
    }
} 