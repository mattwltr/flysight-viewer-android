package matt.wltr.labs.flysightviewer.ui.logview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.ui.LocalizedFormatBuilder;

public class DurationView extends LinearLayout {

    @BindView(R.id.duration_value)
    TextView valueView;

    private NumberFormat valueFormatter;

    private Date begin;

    private Date end;

    public DurationView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        inflate(getContext(), R.layout.view_duration, this);
        ButterKnife.bind(this);
        valueFormatter = LocalizedFormatBuilder.numberFormat(context);
        valueFormatter.setMinimumFractionDigits(0);
        valueFormatter.setMaximumFractionDigits(0);
        valueFormatter.setGroupingUsed(false);
    }

    public void initializeRange(@NonNull Date begin, @NonNull Date end) {
        this.begin = begin;
        this.end = end;
        updateView();
    }

    public void updateEnd(@NonNull Date end) {
        this.end = end;
        updateView();
    }

    private void updateView() {
        valueView.setText(getResources().getString(R.string.second_value, valueFormatter.format((this.end.getTime() - begin.getTime()) / 1000)));
    }
}
