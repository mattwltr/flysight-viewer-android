package matt.wltr.labs.flysightviewer.flysight;

import android.support.annotation.NonNull;

import org.threeten.bp.OffsetDateTime;

import java.io.Serializable;


public class FlySightLogMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private OffsetDateTime localDate;

    private OffsetDateTime utcDate;

    private boolean deleted = false;

    public static FlySightLogMetadata fromFlySightLog(@NonNull FlySightLog flySightLog) {

        FlySightRecord firstRecord = flySightLog.getRecords().entrySet().iterator().next().getValue();

        FlySightLogMetadata flySightLogMetadata = new FlySightLogMetadata();
        flySightLogMetadata.utcDate = firstRecord.getDate();

        return flySightLogMetadata;
    }

    public OffsetDateTime getLocalDate() {
        return localDate;
    }

    public OffsetDateTime getUtcDate() {
        return utcDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
