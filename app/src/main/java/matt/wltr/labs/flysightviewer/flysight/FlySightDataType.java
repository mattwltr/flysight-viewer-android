package matt.wltr.labs.flysightviewer.flysight;

public enum FlySightDataType {
    DATE(Unit.NO_UNIT),
    LATITUDE(Unit.DEGREE),
    LONGITUDE(Unit.DEGREE),
    ELEVATION(Unit.METER),
    VELOCITY_NORTH(Unit.METER_PER_SECOND),
    VELOCITY_EAST(Unit.METER_PER_SECOND),
    VELOCITY_DOWN(Unit.METER_PER_SECOND),
    HORIZONTAL_ACCURACY(Unit.METER),
    VERTICAL_ACCURACY(Unit.METER),
    SPEED_ACCURACY(Unit.METER_PER_SECOND),
    HEADING(Unit.DEGREE),
    HEADING_ACCURACY(Unit.DEGREE),
    GPS_FIX(Unit.NO_UNIT),
    NUMBER_OF_SATELLITES(Unit.NO_UNIT),
    // ===== calculated properties
    DISTANCE(Unit.METER),
    POSITION_X(Unit.NO_UNIT),
    POSITION_Y(Unit.NO_UNIT),
    HORIZONTAL_SPEED(Unit.KILOMETER_PER_HOUR),
    VERTICAL_SPEED(Unit.KILOMETER_PER_HOUR),
    SPEED_DOWN(Unit.KILOMETER_PER_HOUR),
    SPEED_UP(Unit.KILOMETER_PER_HOUR),
    TOTAL_SPEED(Unit.KILOMETER_PER_HOUR),
    GLIDE_RATIO(Unit.NO_UNIT),
    DIVE_ANGLE(Unit.DEGREE);

    Unit unit;

    FlySightDataType(Unit unit) {
        this.unit = unit;
    }

    public Unit getUnit() {
        return unit;
    }
}
