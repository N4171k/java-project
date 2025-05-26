import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Terrain {
    private List<Point> points;
    private Path2D terrainPath;
    private int width;
    private int height;
    private Random random;
    private List<Cloud> clouds;
    private List<Mountain> mountains;
    
    // Terrain generation parameters
    private static final int MIN_HILL_DISTANCE = 150;
    private static final int MAX_HILL_DISTANCE = 300;
    private static final int MAX_HEIGHT_CHANGE = 100;
    private static final int MIN_HEIGHT = 100;
    private static final int CLOUD_COUNT = 5;
    private static final int MOUNTAIN_COUNT = 3;
    
    // Colors
    private static final Color SKY_COLOR = new Color(135, 206, 235);
    private static final Color GRASS_COLOR = new Color(34, 139, 34);
    private static final Color DIRT_COLOR = new Color(139, 69, 19);
    private static final Color MOUNTAIN_COLOR = new Color(169, 169, 169);
    
    public Terrain(int width, int height) {
        this.width = width;
        this.height = height;
        this.points = new ArrayList<>();
        this.random = new Random();
        this.clouds = new ArrayList<>();
        this.mountains = new ArrayList<>();
        
        generateTerrain();
        generateClouds();
        generateMountains();
    }
    
    private void generateTerrain() {
        // Start with a flat surface
        points.add(new Point(0, height - MIN_HEIGHT));
        
        // Generate hills using Perlin-like noise
        int x = 0;
        int lastY = height - MIN_HEIGHT;
        double noise = 0;
        
        while (x < width) {
            // Calculate next point with smoother height changes
            x += MIN_HILL_DISTANCE + (int)(Math.random() * (MAX_HILL_DISTANCE - MIN_HILL_DISTANCE));
            
            // Generate smooth noise
            noise += (random.nextDouble() - 0.5) * 2;
            noise *= 0.8; // Dampen the noise
            
            // Calculate new height with noise
            int heightChange = (int)(noise * MAX_HEIGHT_CHANGE);
            int newY = lastY + heightChange;
            
            // Keep height within reasonable bounds
            newY = Math.max(height - MIN_HEIGHT - MAX_HEIGHT_CHANGE, 
                   Math.min(height - MIN_HEIGHT + MAX_HEIGHT_CHANGE, newY));
            
            points.add(new Point(x, newY));
            lastY = newY;
        }
        
        // Create the terrain path
        terrainPath = new Path2D.Double();
        terrainPath.moveTo(points.get(0).x, points.get(0).y);
        
        // Use quadratic curves for smoother terrain
        for (int i = 1; i < points.size(); i++) {
            Point p1 = points.get(i-1);
            Point p2 = points.get(i);
            
            // Calculate control point for smooth curve
            int controlX = (p1.x + p2.x) / 2;
            int controlY = (p1.y + p2.y) / 2;
            
            terrainPath.quadTo(controlX, controlY, p2.x, p2.y);
        }
    }
    
    private void generateClouds() {
        for (int i = 0; i < CLOUD_COUNT; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height / 3);
            int size = 50 + random.nextInt(50);
            clouds.add(new Cloud(x, y, size));
        }
    }
    
    private void generateMountains() {
        for (int i = 0; i < MOUNTAIN_COUNT; i++) {
            int x = random.nextInt(width);
            int height = 100 + random.nextInt(150);
            mountains.add(new Mountain(x, height));
        }
    }
    
    public void draw(Graphics2D g2d, int cameraX) {
        // Draw sky
        g2d.setColor(SKY_COLOR);
        g2d.fillRect(0, 0, width, height);
        
        // Draw mountains
        g2d.translate(-cameraX, 0);
        for (Mountain mountain : mountains) {
            mountain.draw(g2d);
        }
        
        // Draw clouds
        for (Cloud cloud : clouds) {
            cloud.draw(g2d);
        }
        
        // Draw terrain with dirt and grass
        // First draw the dirt base
        g2d.setColor(DIRT_COLOR);
        g2d.fill(terrainPath);
        
        // Then draw the grass on top
        g2d.setColor(GRASS_COLOR);
        Path2D grassPath = new Path2D.Double(terrainPath);
        g2d.setStroke(new BasicStroke(10));
        g2d.draw(grassPath);
        
        g2d.translate(cameraX, 0);
    }
    
    public double getHeightAt(double x) {
        // Find the two points that surround the given x coordinate
        for (int i = 0; i < points.size() - 1; i++) {
            if (x >= points.get(i).x && x <= points.get(i + 1).x) {
                // Quadratic interpolation between points
                double x1 = points.get(i).x;
                double y1 = points.get(i).y;
                double x2 = points.get(i + 1).x;
                double y2 = points.get(i + 1).y;
                
                // Calculate control point
                double controlX = (x1 + x2) / 2;
                double controlY = (y1 + y2) / 2;
                
                // Quadratic interpolation
                double t = (x - x1) / (x2 - x1);
                return (1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * controlY + t * t * y2;
            }
        }
        return height - MIN_HEIGHT; // Default height if x is out of bounds
    }
    
    public double getAngleAt(double x) {
        // Find the two points that surround the given x coordinate
        for (int i = 0; i < points.size() - 1; i++) {
            if (x >= points.get(i).x && x <= points.get(i + 1).x) {
                // Calculate angle using quadratic curve tangent
                double x1 = points.get(i).x;
                double y1 = points.get(i).y;
                double x2 = points.get(i + 1).x;
                double y2 = points.get(i + 1).y;
                
                // Calculate control point
                double controlX = (x1 + x2) / 2;
                double controlY = (y1 + y2) / 2;
                
                // Calculate tangent at point x
                double t = (x - x1) / (x2 - x1);
                double tangentX = 2 * (1 - t) * (controlX - x1) + 2 * t * (x2 - controlX);
                double tangentY = 2 * (1 - t) * (controlY - y1) + 2 * t * (y2 - controlY);
                
                return Math.toDegrees(Math.atan2(tangentY, tangentX));
            }
        }
        return 0; // Default angle if x is out of bounds
    }
    
    public boolean isOutOfBounds(double x) {
        return x < 0 || x > width;
    }
    
    // Cloud class for background decoration
    private class Cloud {
        private int x, y, size;
        
        public Cloud(int x, int y, int size) {
            this.x = x;
            this.y = y;
            this.size = size;
        }
        
        public void draw(Graphics2D g2d) {
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillOval(x, y, size, size/2);
            g2d.fillOval(x + size/4, y - size/4, size/2, size/2);
            g2d.fillOval(x + size/2, y, size/2, size/3);
        }
    }
    
    // Mountain class for background decoration
    private class Mountain {
        private int x, height;
        
        public Mountain(int x, int height) {
            this.x = x;
            this.height = height;
        }
        
        public void draw(Graphics2D g2d) {
            int[] xPoints = {x, x + 100, x + 200};
            int[] yPoints = {height, 0, height};
            g2d.setColor(MOUNTAIN_COLOR);
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
    }
} 