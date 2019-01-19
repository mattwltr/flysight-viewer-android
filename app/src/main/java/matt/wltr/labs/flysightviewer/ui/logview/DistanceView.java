package matt.wltr.labs.flysightviewer.ui.logview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import labs.wltr.matt.flysightviewer.R;

public class DistanceView extends LinearLayout {

    @BindView(R.id.distance_value)
    TextView valueView;

    private NumberFormat valueFormatter;

    private Double begin;

    private Double end;

    public DistanceView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        inflate(context, R.layout.fragment_distance, this);
        ButterKnife.bind(this);
        valueFormatter = LocalizedFormatBuilder.numberFormat(context);
        valueFormatter.setMinimumFractionDigits(0);
        valueFormatter.setMaximumFractionDigits(0);
        valueFormatter.setGroupingUsed(true);
    }

    public void initializeRange(Double begin, Double end) {
        this.begin = begin;
        this.end = end;
        updateView();
    }

    public void updateEnd(Double end) {
        this.end = end;
        updateView();
    }

    private void updateView() {
        valueView.setText(getResources().getString(R.string.meter_value, valueFormatter.format(Math.round(this.end - begin))));
    }
}
