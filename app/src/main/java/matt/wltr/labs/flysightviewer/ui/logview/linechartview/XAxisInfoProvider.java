package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.content.Context;

import com.scichart.charting.visuals.axes.AxisInfo;
import com.scichart.charting.visuals.axes.AxisTooltip;
import com.scichart.charting.visuals.axes.DefaultAxisInfoProvider;
import com.scichart.charting.visuals.axes.IAxisTooltip;

import labs.wltr.matt.flysightviewer.R;

class XAxisInfoProvider extends DefaultAxisInfoProvider {

    @Override
    protected IAxisTooltip getAxisTooltipInternal(Context context, AxisInfo axisInfo, Class<?> modifierType) {
        return new CustomAxisTooltip(context, axisInfo);
    }

    private static class CustomAxisTooltip extends AxisTooltip {

        public CustomAxisTooltip(Context context, AxisInfo axisInfo) {
            super(context, axisInfo);
            setTooltipBackground(R.drawable.x_axis_tooltip_background);
        }

        @Override
        protected boolean updateInternal(AxisInfo axisInfo) {
            setVisibility(GONE);
            return true;
        }
    }
}
