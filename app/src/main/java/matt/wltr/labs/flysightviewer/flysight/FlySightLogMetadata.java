package matt.wltr.labs.flysightviewer.flysight;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Since;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FlySightLogMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    @Since(1.0)
    private ZoneId zoneId;

    @Since(1.0)
    private OffsetDateTime utcDate;

    @Since(1.0)
    private LatLon latLon;

    @Since(1.0)
    private List<String> tags = new ArrayList<>();

    @Since(1.0)
    private boolean opened = false;

    @Since(1.0)
    private boolean deleted = false;

    static FlySightLogMetadata fromFlySightLog(@NonNull FlySightLog flySightLog) {

        FlySightRecord firstRecord = flySightLog.getFirstRecord();

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
