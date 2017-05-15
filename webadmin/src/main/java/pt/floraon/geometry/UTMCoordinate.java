package pt.floraon.geometry;

public class UTMCoordinate {
	private int XZone;
	private char YZone;
	private long X, Y;
	private Precision precision;
	
	public UTMCoordinate(int XZone, char YZone, long X, long Y) {
		this.XZone = XZone;
		this.YZone = YZone;
		this.X = X;
		this.Y = Y;
	}

	public UTMCoordinate(int XZone, char YZone, long X, long Y, Precision precision) {
		this(XZone, YZone, X, Y);
		this.precision = precision;
	}

	public int getXZone() {
		return XZone;
	}

	public char getYZone() {
		return YZone;
	}

	public long getX() {
		return X;
	}

	public long getY() {
		return Y;
	}

	public Precision getPrecision() {
		return precision;
	}

	public void setPrecision(Precision precision) {
		this.precision = precision;
	}

	@Override
	public String toString() {
		return String.format("%d%c %d %d", XZone, YZone, X, Y);
	}
}
