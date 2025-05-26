package car;

import java.awt.Color;
import java.awt.Graphics;
import obstacle.Obstacle;
import track.Track;

public class AICar extends Car {
    private int targetIndex = 1;

    public AICar(double x, double y) {
        super(x, y);
    }

    public void update(Track track) {
        int[][] points = trackPoints(track);
        if (targetIndex >= points.length) targetIndex = 1;
        double tx = points[targetIndex][0];
        double ty = points[targetIndex][1];
        double angleToTarget = Math.toDegrees(Math.atan2(tx - x, -(ty - y)));
        double angleDiff = angleToTarget - direction;
        angleDiff = (angleDiff + 540) % 360 - 180; // Normalize to [-180,180]
        if (angleDiff > 5) direction += 2;
        else if (angleDiff < -5) direction -= 2;
        speed += 0.1;
        if (speed > 4.0) speed = 4.0;
        x += speed * Math.sin(Math.toRadians(direction));
        y -= speed * Math.cos(Math.toRadians(direction));
        if (Math.hypot(x - tx, y - ty) < 30) targetIndex++;
    }

    private int[][] trackPoints(Track track) {
        try {
            java.lang.reflect.Field f = track.getClass().getDeclaredField("points");
            f.setAccessible(true);
            return (int[][]) f.get(track);
        } catch (Exception e) {
            return new int[][]{{400,100}};
        }
    }

    public void draw(Graphics g, int cameraX, int cameraY) {
        g.setColor(Color.RED);
        g.fillRect((int)x - cameraX - 15, (int)y - cameraY - 30, 30, 60);
    }

    public boolean collidesWith(Car other) {
        return Math.abs(this.x - other.x) < 30 && Math.abs(this.y - other.y) < 60;
    }

    public boolean collidesWith(Obstacle obs) {
        return Math.abs(this.x - obs.getX()) < 30 && Math.abs(this.y - obs.getY()) < 30;
    }
} 