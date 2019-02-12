package matt.wltr.labs.flysightviewer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RadioButton;

import com.google.android.flexbox.FlexboxLayout;

import labs.wltr.matt.flysightviewer.R;

public class TimezoneListItemView extends FlexboxLayout implements Checkable {

    private RadioButton radioButton;

    public TimezoneListItemView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        radioButton = findViewById(R.id.timezone_radio);
    }

    @Override
    public void setChecked(boolean checked) {
        radioButton.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return radioButton.isChecked();
    }

    @Override
    public void toggle() {}
}
