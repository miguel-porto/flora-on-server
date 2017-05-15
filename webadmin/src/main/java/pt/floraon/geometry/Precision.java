package pt.floraon.geometry;

import pt.floraon.driver.FloraOnException;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miguel on 12-05-2017.
 */
public class Precision implements Serializable {
    private transient final static Pattern p = Pattern.compile("^ *(?<radius>[0-9.]+)(?: *(x|X) *(?<side>[0-9]+))? *(?<unit>[a-zA-Z]+)? *$");
    private Float radius;
    private Integer square;

    public Precision() { }

    public Precision(String precisionString) throws FloraOnException {
        Matcher m = p.matcher(precisionString);
        if(m.find()) {
            Float rad = Float.parseFloat(m.group("radius"));
            Integer side = null, mult;
            String s = m.group("side");
            String u = m.group("unit");
            if(s != null) side = Integer.parseInt(s);
            if(u == null) u = "m";
            if(u.toLowerCase().equals("m") || u.toLowerCase().equals("metros"))
                mult = 1;
            else if(u.toLowerCase().equals("km"))
                mult = 1000;
            else
                throw new FloraOnException(String.format("Unit '%s' not understood", u));

            if(side != null) {
                if(rad.intValue() != side.intValue())
                    throw new FloraOnException(String.format("Precision must be either a number or the side of a square (not rectangle)"));
                this.setSquare(side * mult);
            } else
                this.setRadius(rad * mult);
        } else
            throw new FloraOnException(String.format("Precision string '%s' not understood", precisionString));
    }

    public Float getRadius() {
        return radius;
    }

    public void setRadius(Float radius) {
        this.radius = radius;
    }

    public Integer getSquare() {
        return square;
    }

    public void setSquare(Integer square) {
        this.square = square;
    }

    @Override
    public String toString() {
        if(radius != null)
            return radius.intValue() + " m";
        if(square != null) {
            if(square < 1000)
                return square + "x" + square + " m";
            else
                return (square/1000) + "x" + (square/1000) + " km";
        }
        return "";
    }

}
