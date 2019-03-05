package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import androidx.annotation.NonNull;

import org.threeten.bp.OffsetDateTime;

public interface VisibleDateRangeChangeListener {

    void onVisibleDateRangeChange(@NonNull OffsetDateTime newMin, @NonNull OffsetDateTime newMax);
}
