package matt.wltr.labs.flysightviewer.flysight;

import org.threeten.bp.OffsetDateTime;

import java.io.Serializable;

public class FlySightRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** gravity (m/s²) */
    private static final float EARTH_GRAVITY = 9.80665F;

    /** date of recording */
    private OffsetDateTime date;

    /** latitude (deg) */
    private Double lat;

    /** longitude (deg) */
    private Double lng;

    /** height above mean sea level (m) */
    private Double elevation;

    /** velocity north (m/s) */
    private Double velocityNorth;

    /** velocity east (m/s) */
    private Double velocityEast;

    /** velocity down (m/s) */
    private Double velocityDown;

    /** horizontal accuracy (m) */
    private Double horizontalAccuracy;

    /** vertical accuracy (m) */
    private Double verticalAccuracy;

    /** speed accuracy (m/s) */
    private Double speedAccuracy;

    /** (deg) */
    private Double heading;

    /** (deg) */
    private Double headingAccuracy;

    /** GPS fix type (3 = 3D) */
    private Integer gpsFix;

    /** number of satellites used in fix */
    private Integer numberOfSatellites;

    // === calculated properties

    /** absolute distance in relation to first record of log (m) */
    private Double distance;

    /** longitude projected with mercator */
    private Double x;

    /** latitude projected with mercator */
    private Double y;

    /** horizontal velocity (km/h) */
    private Double horizontalSpeed;

    /** vertical speed, no distinction for & down (km/h) */
    private Double verticalSpeed;

    /** speed down (km/h) */
    private Double speedDown;

    /** speed up (km/h) */
    private Double speedUp;

    /** total speed (km/h) */
    private Double totalSpeed;

    /** glide ratio */
    private Double glideRatio;

    /** dive angle (degree) */
    private Double diveAngle;

    FlySightRecord() {}

    public void calculateAdditionalValues() {
        horizontalSpeed = Math.sqrt(velocityEast * velocityEast + velocityNorth * velocityNorth) * 3.6F;
        double speedDown = velocityDown * 3.6F;
        totalSpeed = Math.sqrt(Math.pow(horizontalSpeed, 2) + Math.pow(speedDown - (EARTH_GRAVITY + 3.6F), 2));
        glideRatio = horizontalSpeed / speedDown;
        if (speedDown < 0) {
            verticalSpeed = speedDown * -1;
            this.speedDown = 0D;
            speedUp = verticalSpeed;
        } else {
            verticalSpeed = speedDown;
            this.speedDown = speedDown;
            speedUp = 0D;
        }
        x = SphericalMercator.lngToX(lng);
        y = SphericalMercator.latToY(lat);
        diveAngle = Math.atan2(velocityDown, Math.sqrt(velocityEast * velocityEast + velocityNorth * velocityNorth)) / Math.PI * 180;
    }

    public Double getValue(FlySightDataType dataType) {
        switch (dataType) {
            case ELEVATION:
                checkUnit(FlySightDataType.ELEVATION, Unit.METER, dataType.getUnit());
                return elevation;
            case HORIZONTAL_SPEED:
                checkUnit(FlySightDataType.HORIZONTAL_SPEED, Unit.KILOMETER_PER_HOUR, dataType.getUnit());
                return horizontalSpeed;
            case VERTICAL_SPEED:
                checkUnit(FlySightDataType.VERTICAL_SPEED, Unit.KILOMETER_PER_HOUR, dataType.getUnit());
                return verticalSpeed;
            case SPEED_DOWN:
                checkUnit(FlySightDataType.SPEED_DOWN, Unit.KILOMETER_PER_HOUR, dataType.getUnit());
                return speedDown;
            case SPEED_UP:
                checkUnit(FlySightDataType.SPEED_UP, Unit.KILOMETER_PER_HOUR, dataType.getUnit());
                return speedUp;
            case TOTAL_SPEED:
                checkUnit(FlySightDataType.TOTAL_SPEED, Unit.KILOMETER_PER_HOUR, dataType.getUnit());
                return totalSpeed;
            case GLIDE_RATIO:
                checkUnit(FlySightDataType.GLIDE_RATIO, Unit.NO_UNIT, dataType.getUnit());
                return glideRatio;
            case HEADING:
                checkUnit(FlySightDataType.HEADING, Unit.DEGREE, dataType.getUnit());
                return heading;
            case DIVE_ANGLE:
                checkUnit(FlySightDataType.DIVE_ANGLE, Unit.DEGREE, dataType.getUnit());
                return diveAngle;
            case DISTANCE:
                checkUnit(FlySightDataType.DISTANCE, Unit.METER, dataType.getUnit());
                return distance;
            default:
                throw new IllegalArgumentException(String.format("%s is not yet implemented", dataType.name()));
        }
    }

    private void checkUnit(FlySightDataType flySightDataType, Unit expectedUnit, Unit actualUnit) {
        if (!actualUnit.equals(expectedUnit)) {
            throw new IllegalArgumentException(String.format("Unit mismatch: Expected %s but got %s for %s", expectedUnit, actualUnit, flySightDataType));
        }
    }

    public OffsetDateTime getDate() {
        return date;
    }

    void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public Double getLat() {
        return lat;
    }

    void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getElevation() {
        return elevation;
    }

    void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Double getVelocityNorth() {
        return velocityNorth;
    }

    void setVelocityNorth(Double velocityNorth) {
        this.velocityNorth = velocityNorth;
    }

    public Double getVelocityEast() {
        return velocityEast;
    }

    void setVelocityEast(Double velocityEast) {
        this.velocityEast = velocityEast;
    }

    public Double getVelocityDown() {
        return velocityDown;
    }

    void setVelocityDown(Double velocityDown) {
        this.velocityDown = velocityDown;
    }

    public Double getHorizontalAccuracy() {
        return horizontalAccuracy;
    }

    void setHorizontalAccuracy(Double horizontalAccuracy) {
        this.horizontalAccuracy = horizontalAccuracy;
    }

    public Double getVerticalAccuracy() {
        return verticalAccuracy;
    }

    void setVerticalAccuracy(Double verticalAccuracy) {
        this.verticalAccuracy = verticalAccuracy;
    }

    public Double getSpeedAccuracy() {
        return speedAccuracy;
    }

    void setSpeedAccuracy(Double speedAccuracy) {
        this.speedAccuracy = speedAccuracy;
    }

    public Double getHeading() {
        return heading;
    }

    void setHeading(Double heading) {
        this.heading = heading;
    }

    public Double getHeadingAccuracy() {
        return headingAccuracy;
    }

    void setHeadingAccuracy(Double headingAccuracy) {
        this.headingAccuracy = headingAccuracy;
    }

    public Integer getGpsFix() {
        return gpsFix;
    }

    void setGpsFix(Integer gpsFix) {
        this.gpsFix = gpsFix;
    }

    public Integer getNumberOfSatellites() {
        return numberOfSatellites;
    }

    void setNumberOfSatellites(Integer numberOfSatellites) {
        this.numberOfSatellites = numberOfSatellites;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Double getHorizontalSpeed() {
        return horizontalSpeed;
    }

    public Double getVerticalSpeed() {
        return verticalSpeed;
    }

    public Double getSpeedDown() {
        return speedDown;
    }

    public Double getSpeedUp() {
        return speedUp;
    }

    public Double getTotalSpeed() {
        return totalSpeed;
    }

    public Double getGlideRatio() {
        return glideRatio;
    }

    public Double getDiveAngle() {
        return diveAngle;
    }
}
