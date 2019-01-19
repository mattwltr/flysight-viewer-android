package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.content.Context;

import com.scichart.charting.modifiers.RolloverModifier;
import com.scichart.charting.visuals.renderableSeries.hitTest.DefaultXySeriesInfoProvider;
import com.scichart.charting.visuals.renderableSeries.hitTest.XySeriesInfo;
import com.scichart.charting.visuals.renderableSeries.tooltips.ISeriesTooltip;
import com.scichart.charting.visuals.renderableSeries.tooltips.XySeriesTooltip;
import com.scichart.core.framework.IViewContainer;

import java.util.Date;

class YAxisSelectionInfoProvider extends DefaultXySeriesInfoProvider {

    private YAxisSelectionListener yAxisSelectionListener;

    YAxisSelectionInfoProvider(YAxisSelectionListener yAxisSelectionListener) {
        this.yAxisSelectionListener = yAxisSelectionListener;
    }

    @Override
    protected ISeriesTooltip getSeriesTooltipInternal(Context context, XySeriesInfo<?> seriesInfo, Class<?> modifierType) {
        if (modifierType == RolloverModifier.class) {
            return new CustomXySeriesTooltip(context, seriesInfo, yAxisSelectionListener);
        } else {
            return super.getSeriesTooltipInternal(context, seriesInfo, modifierType);
        }
    }

    private static class CustomXySeriesTooltip extends XySeriesTooltip {

        private YAxisSelectionListener yAxisSelectionListener;

        public CustomXySeriesTooltip(Context context, XySeriesInfo seriesInfo, YAxisSelectionListener yAxisSelectionListener) {
            super(context, seriesInfo);
            this.yAxisSelectionListener = yAxisSelectionListener;
        }

        @Override
        protected void internalUpdate(XySeriesInfo seriesInfo) {
            yAxisSelectionListener.onSelect((Date) seriesInfo.xValue);
        }

        @Override
        public void clear() {}

        @Override
        public void removeFrom(IViewContainer viewContainer) {}
    }
}
