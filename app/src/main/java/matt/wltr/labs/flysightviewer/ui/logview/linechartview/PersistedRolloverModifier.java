package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.graphics.PointF;
import android.view.View;

import com.scichart.charting.modifiers.RolloverModifier;
import com.scichart.charting.visuals.ChartModifierSurface;

class PersistedRolloverModifier extends RolloverModifier {

    @Override
    protected void handleMasterTouchUpEvent(PointF point) {
        // don't propagate touch up so the rollover drawing won't disappear
    }

    @Override
    protected void handleMasterTouchDownEvent(PointF point) {

        ChartModifierSurface chartModifierSurface = (ChartModifierSurface) getModifierSurface();
        if (chartModifierSurface != null) {
            for (int modifierSurfaceChildIndex = 0; modifierSurfaceChildIndex < chartModifierSurface.getChildCount(); ++modifierSurfaceChildIndex) {
                chartModifierSurface.getChildAt(modifierSurfaceChildIndex).setVisibility(View.VISIBLE);
            }
        }
        super.handleMasterTouchDownEvent(point);
    }

    public void enable() {
        setIsEnabled(true);
    }

    public void disable() {

        ChartModifierSurface chartModifierSurface = (ChartModifierSurface) getModifierSurface();
        if (chartModifierSurface != null) {
            for (int modifierSurfaceChildIndex = 0; modifierSurfaceChildIndex < chartModifierSurface.getChildCount(); ++modifierSurfaceChildIndex) {
                chartModifierSurface.getChildAt(modifierSurfaceChildIndex).setVisibility(View.INVISIBLE);
            }
        }
        setIsEnabled(false);
    }
}
