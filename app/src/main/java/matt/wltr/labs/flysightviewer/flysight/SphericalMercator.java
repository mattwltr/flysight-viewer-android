package matt.wltr.labs.flysightviewer.flysight;

public class SphericalMercator {

    public static final double EARTH_RADIUS_IN_METER = 6378137D;

    public static double yToLat(double y) {
        return Math.toDegrees(Math.atan(Math.exp(y / EARTH_RADIUS_IN_METER)) * 2 - Math.PI / 2);
    }

    public static double xToLng(double x) {
        return Math.toDegrees(x / EARTH_RADIUS_IN_METER);
    }

    public static double latToY(double lat) {
        return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2)) * EARTH_RADIUS_IN_METER;
    }

    public static double lngToX(double lng) {
        return Math.toRadians(lng) * EARTH_RADIUS_IN_METER;
    }
}
