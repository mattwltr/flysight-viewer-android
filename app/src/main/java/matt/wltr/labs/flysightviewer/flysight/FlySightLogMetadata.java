package matt.wltr.labs.flysightviewer.flysight;

import android.support.annotation.NonNull;
import android.util.Log;

import org.threeten.bp.OffsetDateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;


public class FlySightLogMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private OffsetDateTime localDate;

    private OffsetDateTime utcDate;

    public static FlySightLogMetadata fromFlySightLog(@NonNull FlySightLog flySightLog) {

        FlySightRecord firstRecord = flySightLog.getRecords().entrySet().iterator().next().getValue();

        FlySightLogMetadata flySightLogMetadata = new FlySightLogMetadata();
        flySightLogMetadata.utcDate = firstRecord.getDate();

        return flySightLogMetadata;
    }

    public static FlySightLogMetadata read(@NonNull File file) {

        FlySightLogMetadata flySightLogMetadata = null;

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            flySightLogMetadata = (FlySightLogMetadata) objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception e) {
            Log.e(FlySightLogMetadata.class.getSimpleName(), "Could not read FlySightLogMetadata from file", e);
        }
        return flySightLogMetadata;
    }

    public OffsetDateTime getLocalDate() {
        return localDate;
    }

    public OffsetDateTime getUtcDate() {
        return utcDate;
    }
}
