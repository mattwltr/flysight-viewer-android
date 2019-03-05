package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import androidx.annotation.NonNull;

import matt.wltr.labs.flysightviewer.flysight.FlySightRecord;

public interface FlySightRecordSelectionListener {

    void onFlySightRecordSelect(@NonNull FlySightRecord flySightRecord);
}
