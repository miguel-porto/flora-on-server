package pt.floraon.geometry;


import pt.floraon.geometry.mgrs.MGRSCoordConverter;
import pt.floraon.geometry.mgrs.UTMCoord;

import java.util.HashMap;
import java.util.Map;

/**
 * This code was adapted from the great C++ code taken originally from
 * http://www.koders.com/cpp/fid56D52408FAC344874E65BF9A1C54F3731C96A39B.aspx
 * http://www.mlab.cz/WebSVN/filedetails.php?repname=MLAB&path=%2FDesigns%2Fskrysohledac2%2FSW%2Futm.c
 *
 * TODO: methods for converting arrays (as many computations need to be made only once)
 *
 * Original headers in the C++ code
 * ********************************
 * File: Utm.cpp
 * RCS: $Header: /cvsroot/stelvio/stelvio/NavStar/Utm.cpp,v 1.1 2001/03/18 20:07:03 steve_l Exp $
 * Author: Steve Loughran
 * Created: 2001
 * Language: C++
 * Package:
 * Status: Experimental
 * This is code to do UTM conversion.
 * I took this code from Jason Bevins' GPS thing which blagged the VB algorithms
 * from the Mapping Datum Transformation Software (MADTRAN) program,
 * written in PowerBasic.  To get the source code for MADTRAN, go to:
 * http://164.214.2.59/publications/guides/MADTRAN/index.html
 * this version retains the core algorithms as static functions
 */
public final class CoordinateConversion {
	private static char[] cArray={'C','D','E','F','G','H','J','K','L','M','N','P','Q','R','S','T','U','V','W','X'};
	private static double PI=3.141592635;
	//private static double FOURTHPI=PI/4;
	private static double deg2rad=PI/180;
	private static double rad2deg=180/PI;
	private static double fe=500000;
	private static double ok=0.9996;
	private static Map<String, String> defaultUTMZones = new HashMap<>();

	static {	// TODO: default zones should be user-defined
		defaultUTMZones.put("MJ", "29T");
		defaultUTMZones.put("MH", "29T");
		defaultUTMZones.put("MG", "29T");
		defaultUTMZones.put("MF", "29T");
		defaultUTMZones.put("ME", "29T");
		defaultUTMZones.put("NJ", "29T");
		defaultUTMZones.put("NH", "29T");
		defaultUTMZones.put("NG", "29T");
		defaultUTMZones.put("NF", "29T");
		defaultUTMZones.put("NE", "29T");
		defaultUTMZones.put("PJ", "29T");
		defaultUTMZones.put("PH", "29T");
		defaultUTMZones.put("PG", "29T");
		defaultUTMZones.put("PF", "29T");
		defaultUTMZones.put("PE", "29T");
		defaultUTMZones.put("QJ", "29T");
		defaultUTMZones.put("QH", "29T");
		defaultUTMZones.put("QG", "29T");
		defaultUTMZones.put("QF", "29T");
		defaultUTMZones.put("QE", "29T");

		defaultUTMZones.put("MD", "29S");
		defaultUTMZones.put("MC", "29S");
		defaultUTMZones.put("MB", "29S");
		defaultUTMZones.put("MA", "29S");
		defaultUTMZones.put("ND", "29S");
		defaultUTMZones.put("NC", "29S");
		defaultUTMZones.put("NB", "29S");
		defaultUTMZones.put("NA", "29S");
		defaultUTMZones.put("PD", "29S");
		defaultUTMZones.put("PC", "29S");
		defaultUTMZones.put("PB", "29S");
		defaultUTMZones.put("PA", "29S");
		defaultUTMZones.put("QD", "29S");
		defaultUTMZones.put("QC", "29S");
		defaultUTMZones.put("QB", "29S");
		defaultUTMZones.put("QA", "29S");
	}

	private static double CalculateESquared(double a,double b) {
		return(((a * a) - (b * b)) / (a * a));
	}

	private static double CalculateE2Squared(double a,double b) {
		return(((a * a) - (b * b)) / (b * b));
	}
	
	private static double denom(double es,double sphi) {
		double sinSphi = Math.sin(sphi);
		return(Math.sqrt(1.0 - es * (sinSphi * sinSphi)));
	}

	private static double sphsr(double a,double es,double sphi) {
		double dn = denom (es, sphi);
		return(a * (1.0 - es) / (dn * dn * dn));
	}

	private static double sphsn(double a,double es,double sphi) {
		double sinSphi = Math.sin(sphi);
		return(a / Math.sqrt(1.0 - es * (sinSphi * sinSphi)));
	}
	
	private static double  sphtmd(double ap,double bp,double cp,double dp,double ep,double sphi) {
		return((ap * sphi) - (bp * Math.sin(2.0 * sphi)) + (cp * Math.sin(4.0 * sphi)) - (dp * Math.sin(6.0 * sphi)) + (ep * Math.sin(8.0 * sphi)));
	}

	private static class LatZones {
		private static char[] letters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
				'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };

		private static int[] degrees = { -90, -84, -72, -64, -56, -48, -40, -32, -24, -16,
				-8, 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		private static char[] negLetters = { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
				'L', 'M' };

		private static int[] negDegrees = { -90, -84, -72, -64, -56, -48, -40, -32, -24,
				-16, -8 };

		private static char[] posLetters = { 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
				'X', 'Z' };

		private static int[] posDegrees = { 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		private static int arrayLength = 22;

		public static int getLatZoneDegree(String letter) {
			char ltr = letter.charAt(0);
			for (int i = 0; i < arrayLength; i++)
			{
				if (letters[i] == ltr)
				{
					return degrees[i];
				}
			}
			return -100;
		}

		public static String getLatZone(double latitude) {
			int latIndex = -2;
			int lat = (int) latitude;

			if (lat >= 0)
			{
				int len = posLetters.length;
				for (int i = 0; i < len; i++)
				{
					if (lat == posDegrees[i])
					{
						latIndex = i;
						break;
					}

					if (lat > posDegrees[i])
					{
						continue;
					}
					else
					{
						latIndex = i - 1;
						break;
					}
				}
			}
			else
			{
				int len = negLetters.length;
				for (int i = 0; i < len; i++)
				{
					if (lat == negDegrees[i])
					{
						latIndex = i;
						break;
					}

					if (lat < negDegrees[i])
					{
						latIndex = i - 1;
						break;
					}
					else
					{
						continue;
					}

				}

			}

			if (latIndex == -1)
			{
				latIndex = 0;
			}
			if (lat >= 0)
			{
				if (latIndex == -2)
				{
					latIndex = posLetters.length - 1;
				}
				return String.valueOf(posLetters[latIndex]);
			}
			else
			{
				if (latIndex == -2)
				{
					latIndex = negLetters.length - 1;
				}
				return String.valueOf(negLetters[latIndex]);

			}
		}

	}

	private static class Digraphs {
		private static final Map<Integer, String> digraph1 = new HashMap<>();
		private static final Map<Integer, String> digraph2 = new HashMap<>();

		private static final String[] digraph1Array = { "A", "B", "C", "D", "E", "F", "G", "H",
				"J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
				"Y", "Z" };

		private static final String[] digraph2Array = { "V", "A", "B", "C", "D", "E", "F", "G",
				"H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V" };

		static {
			digraph1.put(1, "A");
			digraph1.put(2, "B");
			digraph1.put(3, "C");
			digraph1.put(4, "D");
			digraph1.put(5, "E");
			digraph1.put(6, "F");
			digraph1.put(7, "G");
			digraph1.put(8, "H");
			digraph1.put(9, "J");
			digraph1.put(10, "K");
			digraph1.put(11, "L");
			digraph1.put(12, "M");
			digraph1.put(13, "N");
			digraph1.put(14, "P");
			digraph1.put(15, "Q");
			digraph1.put(16, "R");
			digraph1.put(17, "S");
			digraph1.put(18, "T");
			digraph1.put(19, "U");
			digraph1.put(20, "V");
			digraph1.put(21, "W");
			digraph1.put(22, "X");
			digraph1.put(23, "Y");
			digraph1.put(24, "Z");

			digraph2.put(0, "V");
			digraph2.put(1, "A");
			digraph2.put(2, "B");
			digraph2.put(3, "C");
			digraph2.put(4, "D");
			digraph2.put(5, "E");
			digraph2.put(6, "F");
			digraph2.put(7, "G");
			digraph2.put(8, "H");
			digraph2.put(9, "J");
			digraph2.put(10, "K");
			digraph2.put(11, "L");
			digraph2.put(12, "M");
			digraph2.put(13, "N");
			digraph2.put(14, "P");
			digraph2.put(15, "Q");
			digraph2.put(16, "R");
			digraph2.put(17, "S");
			digraph2.put(18, "T");
			digraph2.put(19, "U");
			digraph2.put(20, "V");

		}

		public static int getDigraph1Index(String letter)
		{
			for (int i = 0; i < digraph1Array.length; i++)
			{
				if (digraph1Array[i].equals(letter))
				{
					return i + 1;
				}
			}

			return -1;
		}

		public static int getDigraph2Index(String letter)
		{
			for (int i = 0; i < digraph2Array.length; i++)
			{
				if (digraph2Array[i].equals(letter))
				{
					return i;
				}
			}

			return -1;
		}

		public static String getDigraph1(int longZone, double easting)
		{
			double a2 = 8 * ((longZone - 1) % 3) + 1;
			double a4 = a2 + ((int) (easting / 100000)) - 1;
			return (String) digraph1.get((int) Math.floor(a4));
		}

		public static String getDigraph2(int longZone, double northing)
		{
			int a1 = longZone;
			double a2 = 1 + 5 * ((a1 - 1) % 2);
			double a3 = northing;
			double a4;
			a4 = (a2 + ((int) (a3 / 100000.0))) % 20;
			a4 = Math.floor(a4);
			if (a4 < 0)
			{
				a4 = a4 + 19;
			}
			return digraph2.get((int) Math.floor(a4));

		}

	}

	/**
 * pass utmXZone if you want to force conversion within a given zone, leave null for correct conversion.
 * @param a
 * @param f
 * @param lat
 * @param lon
 * @param utmXZone
 * @return
 */
	public static UTMCoordinate LatLonToUtm(double a,double f,double lat,double lon,int utmXZone) {
		char utmYZone;
		double latRad,lonRad,recf,b,eSquared,e2Squared,tn,ap,bp,cp,dp,ep,tn5,tn4,tn3,olam,dlam,s,c,t,eta,sn,tmd,t1,t2,t3,nfn,northing,t6,t7,easting;
		if(utmXZone==0) {
			if (lon <= 0.0) {
				utmXZone = 30 + (int)(lon / 6.0);
			} else {
				utmXZone = 31 + (int)(lon / 6.0);
			}
		}
		if (lat < 84.0 && lat >= 72.0) {
			// Special case: zone X is 12 degrees from north to south, not 8.
			utmYZone = cArray[19];
		} else {
			utmYZone =cArray[(int)((lat + 80.0) / 8.0)];	// FIXME: array out of bounds, should check coordinates before
		}
		if (lat >= 84.0 || lat < -80.0) {
			// Invalid coordinate; the vertical zone is set to the invalid
			utmYZone = '\0';
		}
		latRad = lat * deg2rad;
		lonRad = lon * deg2rad;
		recf = 1 / f;
		b = a * (recf - 1.0) / recf;
		eSquared = CalculateESquared (a, b);
		e2Squared = CalculateE2Squared (a, b);
		tn = (a - b) / (a + b);
		tn3=tn*tn*tn;
		tn4=tn3 * tn;
		tn5=tn4 * tn;
		ap = a * (1.0 - tn + 5.0 * ((tn * tn) - tn3) / 4.0 + 81.0 * (tn4 - tn5) / 64.0);
		bp = 3.0 * a * (tn - (tn * tn) + 7.0 * (tn3 - tn4) / 8.0 + 55.0 * tn5 / 64.0) / 2.0;
		cp = 15.0 * a * ((tn * tn) - tn3 + 3.0 * (tn4 - tn5) / 4.0) / 16.0;
		dp = 35.0 * a * (tn3 - tn4 + 11.0 * tn5 / 16.0) / 48.0;
		ep = 315.0 * a * (tn4 - tn5) / 512.0;
		olam = (utmXZone * 6 - 183) * deg2rad;
		dlam = lonRad - olam;
		s = Math.sin (latRad);
		c = Math.cos (latRad);
		t = s / c;
		eta = e2Squared * (c * c);
		sn = sphsn(a, eSquared, latRad);
		tmd = sphtmd(ap, bp, cp, dp, ep, latRad);
		t1 = tmd * ok;
		t2 = sn * s * c * ok / 2.0;
		t3 = sn * s * (c * c * c) * ok * (5.0 - (t * t) + 9.0 * eta + 4.0 * (eta * eta)) / 24.0;
		if (latRad < 0.0) nfn = 10000000.0; else nfn = 0;
		northing = (nfn + t1 + (dlam * dlam) * t2 + (dlam * dlam * dlam * dlam) * t3 + (dlam * dlam * dlam * dlam * dlam * dlam) + 0.5);
		t6 = sn * c * ok;
		t7 = sn * (c * c * c) * (1.0 - (t * t) + eta) / 6.0;
		easting = (fe + dlam * t6 + (dlam * dlam * dlam) * t7 + 0.5);
		if (northing >= 9999999.0) northing = 9999999.0;
		return(new UTMCoordinate(utmXZone,utmYZone,(long)easting,(long)northing));
	}
	/**
	 * Converts a latlong coordinate to UTM coordinate in WGS84.
	 * @param lat		Decimal latitude
	 * @param lon		Decimal longitude
	 * @param utmXZone	Pass utmXZone if you want to force conversion within a given zone, set to 0 for correct conversion.
	 * @return			The {@link UTMCoordinate}
	 */
	public static UTMCoordinate LatLonToUtmWGS84(double lat, double lon, int utmXZone) {
		return(LatLonToUtm(6378137.0, 1 / 298.257223563, lat, lon, utmXZone));
	}

	public static LatLongCoordinate UtmToLatLon (double a,double f,int utmXZone,char utmYZone,long easting,long northing) {
		double recf,b,eSquared,e2Squared,tn,ap,bp,cp,dp,ep,tn3,tn4,tn5,nfn,tmd,sr,ftphi,t10,sn,s,c,t,eta,de,t11,lat,t14,t15,dlam,olam,lon;
		int i;
		recf = 1.0 / f;
		b = a * (recf - 1) / recf;
		eSquared = CalculateESquared(a, b);
		e2Squared = CalculateE2Squared(a, b);
		tn = (a - b) / (a + b);
		tn3=tn*tn*tn;
		tn4=tn3*tn;
		tn5=tn4*tn;
		ap = a * (1.0 - tn + 5.0 * ((tn * tn) - tn3) / 4.0 + 81.0 * (tn4 - tn5) / 64.0);
		bp = 3.0 * a * (tn - (tn * tn) + 7.0 * (tn3 - tn4) / 8.0 + 55.0 * tn5 / 64.0) / 2.0;
		cp = 15.0 * a * ((tn * tn) - tn3 + 3.0 * (tn4 - tn5) / 4.0) / 16.0;
		dp = 35.0 * a * (tn3 - tn4 + 11.0 * tn5 / 16.0) / 48.0;
		ep = 315.0 * a * (tn4 - tn5) / 512.0;
		if(Character.toUpperCase(utmYZone)<='M' && Character.toUpperCase(utmYZone)>='C') nfn = 10000000.0; else nfn = 0;
		tmd = (northing - nfn) / ok;
		sr = sphsr(a, eSquared, 0.0);
		ftphi = tmd / sr;
		for(i = 0; i < 5; i++) {
			t10 = sphtmd(ap, bp, cp, dp, ep, ftphi);
			sr = sphsr(a, eSquared, ftphi);
			ftphi = ftphi + (tmd - t10) / sr;
		}
		sr = sphsr(a, eSquared, ftphi);
		sn = sphsn(a, eSquared, ftphi);
		s = Math.sin(ftphi);
		c = Math.cos(ftphi);
		t = s / c;
		eta = e2Squared * (c * c);
		de = easting - fe;
		t10 = t / (2.0 * sr * sn * (ok * ok));
		t11 = t * (5.0 + 3.0 * (t * t) + eta - 4.0 * (eta * eta) - 9.0 * (t * t) * eta) / (24.0 * sr * (sn * sn * sn) * (ok * ok * ok * ok));
		lat = ftphi - (de * de) * t10 + (de * de * de * de) * t11;
		t14 = 1.0 / (sn * c * ok);
		t15 = (1.0 + 2.0 * (t * t) + eta) / (6 * (sn * sn * sn) * c * (ok * ok * ok));
		dlam = de * t14 - (de * de * de) * t15;
		olam = (utmXZone * 6 - 183.0) * deg2rad;
		lon = olam + dlam;
		lon *= rad2deg;
		lat *= rad2deg;
		return(new LatLongCoordinate((float)lat,(float)lon));
	}
	
	public static LatLongCoordinate UtmToLatLonWGS84(int utmXZone,char utmYZone,long easting,long northing) {
		return(UtmToLatLon(6378137.0, 1 / 298.257223563, utmXZone, utmYZone,easting,northing));
	}

	/**
	 * Converts a MGRS string to the center point UTM coordinates and respective precision.
	 * @param mgrutm
	 * @return
	 */
	public static UTMCoordinate MGRSToUTM(String mgrutm) {
		MGRSCoordConverter cnv = new MGRSCoordConverter();
		UTMCoord utm = cnv.convertMGRSToUTM(mgrutm, defaultUTMZones);
		int half = (int) Math.pow(10, (5 - utm.getMGRS().precision)) / 2;
		Precision prec = new Precision();
		prec.setSquare(half * 2);
		return new UTMCoordinate(utm.getZone(), utm.getMGRS().getLatitudeBand(), (long) utm.getEasting() + half, (long) utm.getNorthing() + half, prec);
	}
}
