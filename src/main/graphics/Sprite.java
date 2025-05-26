package graphics;

import java.awt.Image;

public class Sprite {
    private Image image;

    public Sprite(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }
} 