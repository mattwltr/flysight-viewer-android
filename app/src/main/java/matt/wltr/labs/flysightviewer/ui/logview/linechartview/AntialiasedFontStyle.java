package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.graphics.Paint;
import android.graphics.Typeface;

import com.scichart.drawing.common.FontStyle;

public class AntialiasedFontStyle extends FontStyle {

    public AntialiasedFontStyle(Typeface typeface, float textSize, int textColor) {
        super(typeface, textSize, textColor);
    }

    public AntialiasedFontStyle(float textSize, int textColor) {
        super(textSize, textColor);
    }

    @Override
    public void initPaint(Paint paint) {
        super.initPaint(paint);
        paint.setAntiAlias(true);
    }
}
