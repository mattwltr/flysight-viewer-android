package matt.wltr.labs.flysightviewer.ui;

import android.net.Uri;
import android.support.annotation.NonNull;

import matt.wltr.labs.flysightviewer.flysight.FlySightLogMetadata;

public class LogbookListEntry {

    private Uri logUri;

    private Uri metadataUri;

    private FlySightLogMetadata flySightLogMetadata;

    public LogbookListEntry(@NonNull Uri logUri, @NonNull Uri metadataUri, @NonNull FlySightLogMetadata flySightLogMetadata) {
        this.logUri = logUri;
        this.metadataUri = metadataUri;
        this.flySightLogMetadata = flySightLogMetadata;
    }

    public Uri getLogUri() {
        return logUri;
    }

    public Uri getMetadataUri() {
        return metadataUri;
    }

    public FlySightLogMetadata getFlySightLogMetadata() {
        return flySightLogMetadata;
    }
}
