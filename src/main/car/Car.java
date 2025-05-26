package car;

public class Car {
    protected double x, y, speed, direction;

    public Car(double x, double y) {
        this.x = x;
        this.y = y;
        this.speed = 0;
        this.direction = 0;
    }

    public void update() {
        // Update car logic
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getSpeed() { return speed; }
    public double getDirection() { return direction; }
} 