package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.graphics.PointF;

import com.scichart.charting.modifiers.RolloverModifier;

class PersistedRolloverModifier extends RolloverModifier {

    @Override
    protected void handleMasterTouchUpEvent(PointF point) {
        // don't propagate touch up so the rollover drawing won't disappear
    }
}
