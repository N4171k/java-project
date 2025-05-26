package ui;

import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {
    public MenuPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("2D Car Racing Game");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(50));
        add(title);
        add(Box.createVerticalStrut(30));
        JButton startBtn = new JButton("Start Game");
        JButton optionsBtn = new JButton("Options");
        JButton exitBtn = new JButton("Exit");
        startBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        optionsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(startBtn);
        add(Box.createVerticalStrut(10));
        add(optionsBtn);
        add(Box.createVerticalStrut(10));
        add(exitBtn);
    }
} 