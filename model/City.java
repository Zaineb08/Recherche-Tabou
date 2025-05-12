package model;

public class City {
    public int id;
    public double x;
    public double y;

    public City(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public double distanceTo(City other) {
        return Math.hypot(this.x - other.x, this.y - other.y);
    }
}