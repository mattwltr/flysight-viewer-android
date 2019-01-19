package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.support.annotation.NonNull;

import java.util.Date;

public interface YAxisSelectionListener {

    void onSelect(@NonNull Date date);
}
