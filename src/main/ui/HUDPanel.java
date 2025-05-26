package ui;

import javax.swing.*;
import java.awt.*;

public class HUDPanel extends JPanel {
    private int score = 0;
    private int lap = 1;
    private int totalLaps = 3;

    public HUDPanel() {
        setOpaque(false);
    }

    public void setScore(int score) { this.score = score; }
    public void setLap(int lap) { this.lap = lap; }
    public void setTotalLaps(int totalLaps) { this.totalLaps = totalLaps; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Lap: " + lap + "/" + totalLaps, 10, 40);
    }
} 