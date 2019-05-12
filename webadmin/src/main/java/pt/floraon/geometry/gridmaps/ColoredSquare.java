package pt.floraon.geometry.gridmaps;

import pt.floraon.geometry.Point2D;

public class ColoredSquare extends Square implements ISquare {
    private String color;

    public ColoredSquare(Point2D coordinate, long sizeOfSquare) {
        super(coordinate, sizeOfSquare);
        this.color = "#ffffff";
    }

    public ColoredSquare(Point2D coordinate, long sizeOfSquare, String color) {
        super(coordinate, sizeOfSquare);
        this.color = color;
    }

    @Override
    public boolean hasColor() {
        return true;
    }

    @Override
    public String getColor() {
        return this.color;
    }

    @Override
    public void setColor(String color) {
        this.color = color;
    }
}
