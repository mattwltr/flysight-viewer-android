package matt.wltr.labs.flysightviewer.ui;

import androidx.annotation.NonNull;

import matt.wltr.labs.flysightviewer.flysight.FlySightLogMetadata;

class LogbookListEntry {

    private FlySightLogMetadata flySightLogMetadata;

    LogbookListEntry(@NonNull FlySightLogMetadata flySightLogMetadata) {
        this.flySightLogMetadata = flySightLogMetadata;
    }

    FlySightLogMetadata getFlySightLogMetadata() {
        return flySightLogMetadata;
    }
}
