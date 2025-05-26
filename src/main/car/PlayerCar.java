package car;

import java.awt.Color;
import java.awt.Graphics;
import input.InputHandler;
import obstacle.Obstacle;

public class PlayerCar extends Car {
    public PlayerCar(double x, double y) {
        super(x, y);
    }

    public void update(InputHandler input) {
        double accel = 0.2;
        double maxSpeed = 6.0;
        double turnSpeed = 4.0;
        if (input.isUp()) speed += accel;
        if (input.isDown()) speed -= accel;
        if (input.isLeft()) direction -= turnSpeed;
        if (input.isRight()) direction += turnSpeed;
        if (speed > maxSpeed) speed = maxSpeed;
        if (speed < -maxSpeed/2) speed = -maxSpeed/2;
        // Friction
        speed *= 0.98;
        // Move
        x += speed * Math.sin(Math.toRadians(direction));
        y -= speed * Math.cos(Math.toRadians(direction));
    }

    public void drawAt(Graphics g, int x, int y) {
        g.setColor(Color.BLUE);
        g.fillRect(x - 15, y - 30, 30, 60);
    }

    public boolean collidesWith(Car other) {
        return Math.abs(this.x - other.x) < 30 && Math.abs(this.y - other.y) < 60;
    }

    public boolean collidesWith(Obstacle obs) {
        return Math.abs(this.x - obs.getX()) < 30 && Math.abs(this.y - obs.getY()) < 30;
    }
} 