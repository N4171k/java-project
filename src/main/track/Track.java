package track;

import java.awt.*;

public class Track {
    // Centerline points for a hilly, bumpy terrain
    private final int[][] points = {
        {400, 500}, {450, 450}, {500, 400}, {550, 350}, {600, 300}, {650, 350}, {700, 400}, {750, 450}, {800, 500}, {850, 450}, {900, 400}, {950, 350}, {1000, 300}, {1050, 350}, {1100, 400}, {1150, 450}, {1200, 500}
    };
    private final int roadWidth = 80;

    public void draw(Graphics g, int cameraX, int cameraY) {
        // Draw gradient background (sky to ground)
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(new GradientPaint(-cameraX, -cameraY, new Color(135, 206, 235), -cameraX, -cameraY + 600, new Color(34, 139, 34)));
        g2.fillRect(-cameraX - 400, -cameraY - 400, 1600, 1200);

        // Draw road
        g2.setStroke(new BasicStroke(roadWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.DARK_GRAY);
        for (int i = 0; i < points.length - 1; i++) {
            g2.drawLine(points[i][0] - cameraX, points[i][1] - cameraY, points[i+1][0] - cameraX, points[i+1][1] - cameraY);
        }
        // Draw centerline
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.WHITE);
        for (int i = 0; i < points.length - 1; i++) {
            g2.drawLine(points[i][0] - cameraX, points[i][1] - cameraY, points[i+1][0] - cameraX, points[i+1][1] - cameraY);
        }
        // Draw finish line
        g2.setColor(Color.YELLOW);
        g2.setStroke(new BasicStroke(roadWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        g2.drawLine(points[0][0] - cameraX, points[0][1] - cameraY, points[0][0] - cameraX, points[0][1] - cameraY + 10);
    }

    // Check if a point is on the road (within roadWidth/2 of any segment)
    public boolean isOnTrack(double px, double py) {
        for (int i = 0; i < points.length - 1; i++) {
            double dist = pointToSegmentDist(px, py, points[i][0], points[i][1], points[i+1][0], points[i+1][1]);
            if (dist < roadWidth / 2.0) return true;
        }
        return false;
    }

    // Helper: distance from point to segment
    private double pointToSegmentDist(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        if (dx == 0 && dy == 0) return Math.hypot(px - x1, py - y1);
        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * dx, projY = y1 + t * dy;
        return Math.hypot(px - projX, py - projY);
    }

    public int[] getFinishLine() {
        return points[0];
    }
} 