package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.content.Context;
import android.graphics.Typeface;

import com.scichart.charting.modifiers.RolloverModifier;
import com.scichart.charting.visuals.renderableSeries.hitTest.DefaultXySeriesInfoProvider;
import com.scichart.charting.visuals.renderableSeries.hitTest.XySeriesInfo;
import com.scichart.charting.visuals.renderableSeries.tooltips.ISeriesTooltip;
import com.scichart.charting.visuals.renderableSeries.tooltips.XySeriesTooltip;
import com.scichart.core.framework.IViewContainer;
import com.scichart.drawing.utility.ColorUtil;

import java.text.DecimalFormat;

class YAxisInfoProvider extends DefaultXySeriesInfoProvider {

    private String decimalFormatPattern;

    private int color;

    YAxisInfoProvider(String decimalFormatPattern, int color) {
        this.decimalFormatPattern = decimalFormatPattern;
        this.color = color;
    }

    @Override
    protected ISeriesTooltip getSeriesTooltipInternal(Context context, XySeriesInfo<?> seriesInfo, Class<?> modifierType) {
        if (modifierType == RolloverModifier.class) {
            return new CustomXySeriesTooltip(context, seriesInfo, decimalFormatPattern, color);
        } else {
            return super.getSeriesTooltipInternal(context, seriesInfo, modifierType);
        }
    }

    private static class CustomXySeriesTooltip extends XySeriesTooltip {

        private static final float BACKGROUND_OPACITY = 0.9F;

        private DecimalFormat decimalFormat;

        private int color;

        public CustomXySeriesTooltip(Context context, XySeriesInfo seriesInfo, String decimalFormatPattern, int color) {
            super(context, seriesInfo);
            decimalFormat = new DecimalFormat(decimalFormatPattern);
            this.color = color;
        }

        @Override
        protected void internalUpdate(XySeriesInfo seriesInfo) {

            double value = (Double) seriesInfo.yValue;
            setText(decimalFormat.format(value));

            setTooltipBackgroundColor(ColorUtil.argb(color, BACKGROUND_OPACITY));
            setTooltipTextColor(ColorUtil.White);
            setTypeface(Typeface.MONOSPACE);
            setTextSize(11);
            setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        }

        @Override
        public void clear() {}

        @Override
        public void removeFrom(IViewContainer viewContainer) {}
    }
}
