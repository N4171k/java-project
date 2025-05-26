package graphics;

import java.awt.Graphics;
import graphics.Sprite;

public class Renderer {
    public static void drawSprite(Graphics g, Sprite sprite, int x, int y) {
        g.drawImage(sprite.getImage(), x, y, null);
    }
} 