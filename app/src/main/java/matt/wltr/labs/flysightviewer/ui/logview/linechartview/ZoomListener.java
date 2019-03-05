package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import androidx.annotation.NonNull;

public interface ZoomListener {

    void onZoom(@NonNull ZoomEvent zoomEvent);
}
