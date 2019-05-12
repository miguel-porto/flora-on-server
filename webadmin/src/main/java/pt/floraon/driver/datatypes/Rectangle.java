package pt.floraon.driver.datatypes;

import pt.floraon.driver.FloraOnException;

public class Rectangle {
    private long top, bottom, left, right;

    public Rectangle(long left, long right, long top, long bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    public Rectangle(String bounds) throws FloraOnException {
        String[] tmpbounds = bounds.split(" ");
        if(tmpbounds.length != 4) throw new FloraOnException("Invalid bounding box");

        this.left = Long.parseLong(tmpbounds[0]);
        this.right = Long.parseLong(tmpbounds[1]);
        this.top = Long.parseLong(tmpbounds[2]);
        this.bottom = Long.parseLong(tmpbounds[3]);
    }

    public long getTop() {
        return top;
    }

    public void setTop(long top) {
        this.top = top;
    }

    public long getBottom() {
        return bottom;
    }

    public void setBottom(long bottom) {
        this.bottom = bottom;
    }

    public long getLeft() {
        return left;
    }

    public void setLeft(long left) {
        this.left = left;
    }

    public long getRight() {
        return right;
    }

    public void setRight(long right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return this.getLeft() + " " + this.getBottom() + " " + (this.getRight() - this.getLeft())
                + " " + (this.getTop() - this.getBottom());
    }

    public String toString(int divisor) {
        return Math.round(this.getLeft() / divisor) + " " + Math.round(this.getBottom() / divisor) + " "
                + Math.round((this.getRight() - this.getLeft()) / divisor)
                + " " + Math.round((this.getTop() - this.getBottom()) / divisor);
    }

}
