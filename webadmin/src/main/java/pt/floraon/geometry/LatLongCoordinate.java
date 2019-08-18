package pt.floraon.geometry;

import java.util.Objects;

public class LatLongCoordinate {
	private float latitude, longitude;
	
	public LatLongCoordinate(float lat,float lon) {
		this.latitude = lat;
		this.longitude = lon;
	}

	public float getLatitude() {return latitude;}
	public float getLongitude() {return longitude;}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LatLongCoordinate that = (LatLongCoordinate) o;
		return String.format("%.6f %.6f", latitude, longitude).equals(
			String.format("%.6f %.6f", that.latitude, that.longitude));
	}

	@Override
	public int hashCode() {
		return Objects.hash(String.format("%.6f %.6f", latitude, longitude));
	}
}
