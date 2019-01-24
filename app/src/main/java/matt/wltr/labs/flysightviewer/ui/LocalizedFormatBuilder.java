package matt.wltr.labs.flysightviewer.ui;

import android.content.Context;
import android.os.Build;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LocalizedFormatBuilder {

    public static NumberFormat numberFormat(Context context) {
        return DecimalFormat.getInstance(getLocale(context));
    }

    public static SimpleDateFormat simpleDateFormat(Context context, String pattern) {
        return new SimpleDateFormat(pattern, getLocale(context));
    }

    private static Locale getLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }
}
