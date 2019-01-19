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

public class HeadingView extends LinearLayout {

    @BindView(R.id.heading_value)
    TextView valueView;

    @BindView(R.id.heading_icon_arrow)
    ImageView arrowView;

    private NumberFormat valueFormatter;

    public HeadingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        inflate(getContext(), R.layout.fragment_heading, this);
        ButterKnife.bind(this);
        valueFormatter = LocalizedFormatBuilder.numberFormat(context);
        valueFormatter.setMinimumFractionDigits(0);
        valueFormatter.setMaximumFractionDigits(0);
        valueFormatter.setGroupingUsed(false);
    }

    public void update(final Double heading) {

        valueView.setText(getResources().getString(R.string.degree_value, valueFormatter.format(Math.round(heading))));

        if (arrowView.getHeight() == 0) {
            // UI has not been initialized yet so we have to add a listener, wait for it & then immediately remove it
            arrowView
                    .getViewTreeObserver()
                    .addOnGlobalLayoutListener(
                            new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    arrowView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    rotateArrow(heading);
                                }
                            });
        } else {
            rotateArrow(heading);
        }
    }

    private void rotateArrow(Double heading) {

        RectF drawableRect = new RectF(0, 0, arrowView.getDrawable().getIntrinsicWidth(), arrowView.getDrawable().getIntrinsicHeight());
        RectF viewRect = new RectF(0, 0, arrowView.getWidth(), arrowView.getHeight());
        Matrix matrix = arrowView.getMatrix();
        matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);

        matrix.postRotate(heading.floatValue(), arrowView.getWidth() / 2F, (arrowView.getHeight() * 54.34433333333333F) / 100F);

        arrowView.setImageMatrix(matrix);
    }
}
