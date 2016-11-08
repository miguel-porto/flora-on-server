package pt.floraon.utmlatlong;

public class UTMCoordinate {
	private int XZone;
	private char YZone;
	private long X, Y;
	
	public UTMCoordinate(int XZone, char YZone, long X, long Y) {
		this.XZone = XZone;
		this.YZone = YZone;
		this.X = X;
		this.Y = Y;
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
}
