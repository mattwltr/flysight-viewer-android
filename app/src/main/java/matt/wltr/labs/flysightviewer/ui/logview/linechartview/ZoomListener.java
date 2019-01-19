package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.support.annotation.NonNull;

public interface ZoomListener {

    void onZoom(@NonNull ZoomEvent zoomEvent);
}
