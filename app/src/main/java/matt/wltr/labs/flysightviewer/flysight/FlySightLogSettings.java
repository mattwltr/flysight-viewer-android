package matt.wltr.labs.flysightviewer.flysight;

import android.content.Context;
import android.support.annotation.NonNull;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;

public abstract class FlySightLogSettings {

    public static final String LOG_DIRECTORY = "logs";
    public static final String LOG_FILE_NAME = "%s.csv";
    public static final String METADATA_FILE_EXTENSION = ".metadata";
    public static final String METADATA_FILE_NAME = LOG_FILE_NAME + METADATA_FILE_EXTENSION;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @NonNull
    public static String getLogDirectoryPath(@NonNull Context context) {
        return context.getFilesDir() + File.separator + LOG_DIRECTORY;
    }

    @NonNull
    public static File getLogDirectory(@NonNull Context context) {
        File directory = new File(getLogDirectoryPath(context));
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    @NonNull
    public static File getLogFile(@NonNull Context context, OffsetDateTime date) {
        return new File(getLogDirectory(context).getPath() + File.separator + getLogFileName(date));
    }

    @NonNull
    private static String getLogFileName(OffsetDateTime date) {
        return String.format(LOG_FILE_NAME, DATE_TIME_FORMAT.format(date));
    }

    @NonNull
    public static File getMetadataFile(@NonNull Context context, OffsetDateTime date) {
        return new File(getLogDirectory(context).getPath() + File.separator + String.format(METADATA_FILE_NAME, DATE_TIME_FORMAT.format(date)));
    }
}
