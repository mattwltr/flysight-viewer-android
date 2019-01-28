package matt.wltr.labs.flysightviewer.flysight;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import org.threeten.bp.OffsetDateTime;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlySightLogbookImportTask extends AsyncTask<Uri, Integer, Void> {

    private static final List<String> CSV_MIME_TYPES = Arrays.asList("application/csv", "text/csv", "text/comma-separated-values");

    private final WeakReference<Activity> weakActivity;

    private FlySightLogbookImportObserver flySightLogbookImportObserver;

    public FlySightLogbookImportTask(Activity activity) {
        this.weakActivity = new WeakReference<>(activity);
    }

    @Override
    protected Void doInBackground(Uri... uris) {

        Activity activity = weakActivity.get();

        List<Uri> csvFileUris = getCsvFileUris(DocumentsContract.buildChildDocumentsUriUsingTree(uris[0], DocumentsContract.getTreeDocumentId(uris[0])));

        for (Uri uri : csvFileUris) {
            try {
                FileInputStream inputStream = (FileInputStream) activity.getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    // skip, uri is not readable
                    continue;
                }
                FlySightLog flySightLog = FlySightLogParser.parse(inputStream, ParseMode.FIRST_DATA_LINE);
                if (flySightLog == null) {
                    // skip, can't parse FlySightLog -> no need to import
                    continue;
                }
                OffsetDateTime utcDateTime = flySightLog.getRecords().keySet().iterator().next();
                File logFile = FlySightLogRepository.getLogFile(activity, utcDateTime);
                if (!logFile.exists()) {
                    FlySightLogRepository.copyLogFile(activity, uri, logFile);
                }
                if (!FlySightLogRepository.metadataFileExists(activity, utcDateTime)) {
                    FlySightLogMetadata flySightLogMetadata = FlySightLogMetadata.fromFlySightLog(flySightLog);
                    FlySightLogRepository.saveMetadata(activity, flySightLogMetadata);
                }
            } catch (Exception e) {
                Log.e(FlySightLogbookImportTask.class.getSimpleName(), String.format("Could not read file %s", uri.toString()), e);
            }
        }
        return null;
    }

    private List<Uri> getCsvFileUris(@NonNull Uri parentDirectoryUri) {

        List<Uri> csvUris = new ArrayList<>();

        Activity activity = weakActivity.get();

        Cursor childCursor =
                activity.getContentResolver()
                        .query(
                                parentDirectoryUri,
                                new String[] {
                                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                                    DocumentsContract.Document.COLUMN_MIME_TYPE
                                },
                                null,
                                null,
                                null);
        if (childCursor != null) {
            try {
                while (childCursor.moveToNext()) {
                    String documentId = childCursor.getString(0);
                    String name = childCursor.getString(1);
                    String mimeType = childCursor.getString(2);
                    Uri childUri = Uri.withAppendedPath(parentDirectoryUri, name);
                    if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)) {
                        csvUris.addAll(getCsvFileUris(DocumentsContract.buildChildDocumentsUriUsingTree(childUri, documentId)));
                    } else if (CSV_MIME_TYPES.contains(mimeType)) {
                        csvUris.add(DocumentsContract.buildChildDocumentsUriUsingTree(childUri, documentId));
                    }
                }
            } finally {
                childCursor.close();
            }
        }
        return csvUris;
    }

    public void setFlySightLogbookImportObserver(FlySightLogbookImportObserver flySightLogbookImportObserver) {
        this.flySightLogbookImportObserver = flySightLogbookImportObserver;
    }

    @Override
    protected void onPostExecute(Void object) {
        if (flySightLogbookImportObserver != null) {
            flySightLogbookImportObserver.onFlySightLogbookImported();
        }
    }
}
