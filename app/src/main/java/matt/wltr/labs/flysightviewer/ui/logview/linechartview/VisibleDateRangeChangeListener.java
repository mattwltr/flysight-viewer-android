package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.support.annotation.NonNull;

import org.threeten.bp.OffsetDateTime;

public interface VisibleDateRangeChangeListener {

    void onVisibleDateRangeChange(@NonNull OffsetDateTime newMin, @NonNull OffsetDateTime newMax);
}
