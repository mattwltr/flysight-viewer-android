package matt.wltr.labs.flysightviewer.flysight;

public class SphericalMercator {

    public static final double EARTH_RADIUS_IN_METER = 6378137D;

    public static double yToLat(double y) {
        return Math.toDegrees(Math.atan(Math.exp(y / EARTH_RADIUS_IN_METER)) * 2 - Math.PI / 2);
    }

    public static double xToLon(double x) {
        return Math.toDegrees(x / EARTH_RADIUS_IN_METER);
    }

    public static double latToY(double lat) {
        return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2)) * EARTH_RADIUS_IN_METER;
    }

    public static double lonToX(double lon) {
        return Math.toRadians(lon) * EARTH_RADIUS_IN_METER;
    }

    public static double distanceBetween(LatLon latLon1, LatLon latLon2) {
        double distanceLat = Math.toRadians(latLon2.getLat() - latLon1.getLat());
        double distanceLng = Math.toRadians(latLon2.getLon() - latLon1.getLon());
        double a =
                Math.sin(distanceLat / 2) * Math.sin(distanceLat / 2)
                        + Math.cos(Math.toRadians(latLon1.getLat())) * Math.cos(Math.toRadians(latLon2.getLat())) * Math.sin(distanceLng / 2) * Math.sin(distanceLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_IN_METER * c;
    }
}
