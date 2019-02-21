package matt.wltr.labs.flysightviewer.flysight;

import android.support.annotation.NonNull;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.Serializable;

public class FlySightLogMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ZoneId zoneId;

    private OffsetDateTime utcDate;

    private LatLon latLon;

    private String description;

    private boolean deleted = false;

    static FlySightLogMetadata fromFlySightLog(@NonNull FlySightLog flySightLog) {

        FlySightRecord firstRecord = flySightLog.getRecords().entrySet().iterator().next().getValue();

        FlySightLogMetadata flySightLogMetadata = new FlySightLogMetadata();
        flySightLogMetadata.utcDate = firstRecord.getDate();
        if (flySightLog.getExit() != null) {
            FlySightRecord exitRecord = flySightLog.getExit();
            flySightLogMetadata.latLon = new LatLon(exitRecord.getLat(), exitRecord.getLon());
        } else {
            flySightLogMetadata.latLon = new LatLon(firstRecord.getLat(), firstRecord.getLon());
        }
        return flySightLogMetadata;
    }

    public ZonedDateTime getZonedDateTime() {
        return zoneId != null ? utcDate.toZonedDateTime().withZoneSameInstant(zoneId) : utcDate.toZonedDateTime();
    }

    public String getFormattedDateTime() {
        if (zoneId != null) {
            ZonedDateTime zonedDateTime = utcDate.toZonedDateTime().withZoneSameInstant(zoneId);
            return zonedDateTime.format(DATE_TIME_FORMAT);
        } else {
            return DATE_TIME_FORMAT.format(utcDate) + " UTC";
        }
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    public OffsetDateTime getUtcDate() {
        return utcDate;
    }

    public LatLon getLatLon() {
        return latLon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
