package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.support.annotation.NonNull;

import java.util.Date;

public interface VisibleDateRangeChangeListener {

    void onVisibleDateRangeChange(@NonNull Date newMin, @NonNull Date newMax);
}
