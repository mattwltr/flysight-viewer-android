package matt.wltr.labs.flysightviewer.flysight;

import android.net.Uri;

public class FlySightLogPreview {

    private Uri uri;

    private String name;

    public FlySightLogPreview(Uri uri) {
        this.uri = uri;
        name = uri.getLastPathSegment();
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }
}
