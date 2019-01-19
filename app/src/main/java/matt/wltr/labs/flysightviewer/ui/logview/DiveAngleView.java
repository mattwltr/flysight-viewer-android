package matt.wltr.labs.flysightviewer.ui.logview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import labs.wltr.matt.flysightviewer.R;

public class DiveAngleView extends LinearLayout {

    @BindView(R.id.dive_angle_value)
    TextView valueView;

    @BindView(R.id.dive_angle_icon_arrow)
    ImageView arrowView;

    private NumberFormat valueFormatter;

    public DiveAngleView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        inflate(getContext(), R.layout.fragment_dive_angle, this);
        ButterKnife.bind(this);
        valueFormatter = LocalizedFormatBuilder.numberFormat(context);
        valueFormatter.setMinimumFractionDigits(0);
        valueFormatter.setMaximumFractionDigits(0);
        valueFormatter.setGroupingUsed(false);
    }

    public void update(final Double diveAngle) {

        valueView.setText(getContext().getString(R.string.degree_value, valueFormatter.format(Math.round(diveAngle))));

        if (arrowView.getHeight() == 0) {
            // UI has not been initialized yet so we have to add a listener, wait for it & then immediately remove it
            arrowView
                    .getViewTreeObserver()
                    .addOnGlobalLayoutListener(
                            new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    arrowView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    rotateArrow(diveAngle);
                                }
                            });
        } else {
            rotateArrow(diveAngle);
        }
    }

    private void rotateArrow(Double diveAngle) {

        RectF drawableRect = new RectF(0, 0, arrowView.getDrawable().getIntrinsicWidth(), arrowView.getDrawable().getIntrinsicHeight());
        RectF viewRect = new RectF(0, 0, arrowView.getWidth(), arrowView.getHeight());
        Matrix matrix = arrowView.getMatrix();
        matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);

        matrix.postRotate(diveAngle.floatValue(), arrowView.getWidth() / 2F, arrowView.getHeight() / 2F);

        arrowView.setImageMatrix(matrix);
    }
}
