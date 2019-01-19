package matt.wltr.labs.flysightviewer.ui.logview;

import android.content.Context;
import android.os.Build;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

class LocalizedFormatBuilder {

    static NumberFormat numberFormat(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        return DecimalFormat.getInstance(locale);
    }
}
