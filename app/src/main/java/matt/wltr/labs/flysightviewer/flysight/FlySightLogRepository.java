package matt.wltr.labs.flysightviewer.flysight;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public abstract class FlySightLogRepository {

    private static final int BUFFER_SIZE = 1024 * 2;

    private static final String LOG_DIRECTORY = "logs";
    private static final String RAW_LOG_FILE_NAME = "%s.csv.gz";
    private static final String METADATA_FILE_EXTENSION = ".metadata";
    private static final String METADATA_FILE_NAME = RAW_LOG_FILE_NAME + METADATA_FILE_EXTENSION;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static final Gson GSON =
            new Gson()
                    .newBuilder()
                    .serializeSpecialFloatingPointValues()
                    .registerTypeAdapter(
                            ZoneId.class,
                            new TypeAdapter<ZoneId>() {
                                @Override
                                public void write(JsonWriter jsonWriter, ZoneId value) throws IOException {
                                    if (value != null) {
                                        jsonWriter.value(value.toString());
                                    } else {
                                        jsonWriter.nullValue();
                                    }
                                }

                                @Override
                                public ZoneId read(JsonReader jsonReader) throws IOException {
                                    if (!jsonReader.hasNext()) {
                                        return null;
                                    }
                                    return ZoneId.of(jsonReader.nextString());
                                }
                            })
                    .create();

    @NonNull
    public static File getLogFile(@NonNull Context context, @NonNull FlySightLogMetadata flySightLogMetadata) {
        return getLogFile(context, flySightLogMetadata.getUtcDate());
    }

    @NonNull
    static File getLogFile(@NonNull Context context, OffsetDateTime utcDateTime) {
        return new File(getLogDirectory(context).getPath() + File.separator + String.format(RAW_LOG_FILE_NAME, DATE_TIME_FORMAT.format(utcDateTime)));
    }

    @NonNull
    private static File getLogDirectory(@NonNull Context context) {
        File directory = new File(context.getFilesDir() + File.separator + LOG_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private static boolean logFileExists(File metadataFile) {
        return new File(metadataFile.toString().replace(METADATA_FILE_EXTENSION, "")).exists();
    }

    /**
     * Deletes an existing FlySight log file by its {@code FlySightLogMetadata}. The file will get emptied/cleared instead of hard deleted so it won't be reimported.
     *
     * @param context activity context
     * @param flySightLogMetadata metadata that belongs to the FlySight log file
     * @throws IOException
     */
    public static void deleteLogFile(@NonNull Context context, @NonNull FlySightLogMetadata flySightLogMetadata) throws IOException {
        flySightLogMetadata.setDeleted(true);
        saveMetadata(context, flySightLogMetadata);
        PrintWriter writer = new PrintWriter(getLogFile(context, flySightLogMetadata));
        writer.print("");
        writer.close();
    }

    /**
     * @param context activity context
     * @param source {@code Uri} of the file to copy
     * @param targetFile target {@code File} that the source file will be copied to
     * @throws Exception
     */
    static void copyLogFile(Context context, Uri source, File targetFile) throws Exception {
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(targetFile, false));
        copy(context.getContentResolver().openInputStream(source), gzipOutputStream);
    }

    /**
     * Copies the given {@code inputStream} to the given {@code outputStream}
     * @param inputStream
     * @param outputStream
     * @throws Exception
     */
    private static void copy(InputStream inputStream, OutputStream outputStream) throws Exception {

        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, BUFFER_SIZE);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, BUFFER_SIZE);

        int n;

        try {
            while ((n = bufferedInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
                bufferedOutputStream.write(buffer, 0, n);
            }
            bufferedOutputStream.flush();
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (IOException e) {
                Log.e(FlySightLogRepository.class.getSimpleName(), e.getMessage(), e);
            }
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                Log.e(FlySightLogRepository.class.getSimpleName(), e.getMessage(), e);
            }
        }
    }

    /**
     * @param context activity context
     * @return {@code count} of all FlySight logs
     */
    static long count(@NonNull Context context) {
        long count = 0;
        File directory = getLogDirectory(context);
        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(METADATA_FILE_EXTENSION) && logFileExists(file)) {
                count++;
            }
        }
        return count;
    }

    static boolean metadataFileExists(@NonNull Context context, OffsetDateTime utcDateTime) {
        return getMetadataFile(context, utcDateTime).exists();
    }

    @NonNull
    private static File getMetadataFile(@NonNull Context context, OffsetDateTime utcDateTime) {
        return new File(getLogDirectory(context).getPath() + File.separator + String.format(METADATA_FILE_NAME, DATE_TIME_FORMAT.format(utcDateTime)));
    }

    @NonNull
    public static List<FlySightLogMetadata> getAllFlySightLogMetadata(@NonNull Context context) {
        List<FlySightLogMetadata> flySightLogMetadataList = new ArrayList<>();
        for (File metadataFile : getAllMetadataFiles(context)) {
            try {
                JsonReader reader = new JsonReader(new FileReader(metadataFile));
                FlySightLogMetadata flySightLogMetadata = GSON.fromJson(reader, FlySightLogMetadata.class);
                if (!flySightLogMetadata.isDeleted()) {
                    flySightLogMetadataList.add(flySightLogMetadata);
                }
            } catch (Exception e) {
                Log.e(FlySightLogMetadata.class.getSimpleName(), String.format("Could not read FlySightLogMetadata from file %s", metadataFile.toString()), e);
            }
        }
        return flySightLogMetadataList;
    }

    @NonNull
    private static List<File> getAllMetadataFiles(@NonNull Context context) {
        List<File> metadataFiles = new ArrayList<>();
        File directory = getLogDirectory(context);
        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(METADATA_FILE_EXTENSION) && logFileExists(file)) {
                metadataFiles.add(file);
            }
        }
        return metadataFiles;
    }

    /**
     * @param context activity context
     * @param latLon latitude & longitude
     * @param radiusInMeter radius in meters with {@code latLon} as center
     * @return a list of {@code FlySightMetaData} within a given lat/lon radius that <strong>don't</strong> have a timezone set
     */
    @NonNull
    public static List<FlySightLogMetadata> getFlySightLogMetadataByLatLonWithoutZoneId(@NonNull Context context, @NonNull LatLon latLon, int radiusInMeter) {
        List<FlySightLogMetadata> flySightLogMetadataList = getAllFlySightLogMetadata(context);
        Iterator<FlySightLogMetadata> flySightLogMetadataIterator = flySightLogMetadataList.iterator();
        while (flySightLogMetadataIterator.hasNext()) {
            FlySightLogMetadata flySightLogMetadata = flySightLogMetadataIterator.next();
            if (SphericalMercator.distanceBetween(flySightLogMetadata.getLatLon(), latLon) > radiusInMeter || flySightLogMetadata.getZoneId() != null) {
                flySightLogMetadataIterator.remove();
            }
        }
        return flySightLogMetadataList;
    }

    /**
     * @param context activity context
     * @param latLon latitude & longitude
     * @param radiusInMeter radius in meters with {@code latLon} as center
     * @return a list of {@code FlySightMetaData} within a given lat/lon radius that have a timezone set
     */
    @NonNull
    public static List<FlySightLogMetadata> getFlySightLogMetadataByLatLonWithZoneId(@NonNull Context context, @NonNull LatLon latLon, int radiusInMeter) {
        List<FlySightLogMetadata> flySightLogMetadataList = getAllFlySightLogMetadata(context);
        Iterator<FlySightLogMetadata> flySightLogMetadataIterator = flySightLogMetadataList.iterator();
        while (flySightLogMetadataIterator.hasNext()) {
            FlySightLogMetadata flySightLogMetadata = flySightLogMetadataIterator.next();
            if (SphericalMercator.distanceBetween(flySightLogMetadata.getLatLon(), latLon) > radiusInMeter || flySightLogMetadata.getZoneId() == null) {
                flySightLogMetadataIterator.remove();
            }
        }
        Collections.sort(flySightLogMetadataList, (first, second) -> second.getUtcDate().compareTo(first.getUtcDate()));
        return flySightLogMetadataList;
    }

    public static void saveMetadata(@NonNull Context context, @NonNull FlySightLogMetadata flySightLogMetadata) throws IOException {
        File file = getMetadataFile(context, flySightLogMetadata.getUtcDate());
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(flySightLogMetadata, writer);
        }
    }
}
