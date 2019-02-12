package matt.wltr.labs.flysightviewer.flysight;

import java.io.Serializable;

public class LatLon implements Serializable {

    private static final long serialVersionUID = 1L;

    private double lat;

    private double lon;

    public LatLon(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
