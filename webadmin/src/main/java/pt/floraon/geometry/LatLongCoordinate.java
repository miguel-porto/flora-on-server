package pt.floraon.geometry;

public class LatLongCoordinate {
	private float latitude, longitude;
	
	public LatLongCoordinate(float lat,float lon) {
		this.latitude = lat;
		this.longitude = lon;
	}

	public float getLatitude() {return latitude;}
	public float getLongitude() {return longitude;}
}
