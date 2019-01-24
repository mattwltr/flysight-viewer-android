package matt.wltr.labs.flysightviewer.flysight;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class FlySightLogSettings {

    public static final String LOG_DIRECTORY = "logs";
    public static final String FILE_NAME = "%s.csv";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);

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
    public static File getLogFile(@NonNull Context context, Date date) {
        return new File(getLogDirectory(context).getPath() + File.separator + String.format(FILE_NAME, DATE_FORMAT.format(date)));
    }
}
