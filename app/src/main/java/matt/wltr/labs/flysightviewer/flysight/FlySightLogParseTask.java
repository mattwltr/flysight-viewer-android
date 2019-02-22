package matt.wltr.labs.flysightviewer.flysight;

import android.os.AsyncTask;

import java.io.FileInputStream;
import java.io.InputStream;

public class FlySightLogParseTask extends AsyncTask<InputStream, Integer, FlySightLog> {

    private FlySightLogParseObserver flySightLogParseObserver;

    public FlySightLogParseTask(FlySightLogParseObserver flySightLogParseObserver) {
        this.flySightLogParseObserver = flySightLogParseObserver;
    }

    @Override
    protected FlySightLog doInBackground(InputStream... inputStreams) {
        return FlySightLogParser.parse(inputStreams[0], ParseMode.ALL, ParsePrecision.INTELLIGENT, percentage -> flySightLogParseObserver.onProgress(percentage));
    }

    @Override
    protected void onPostExecute(FlySightLog flySightLog) {
        flySightLogParseObserver.onFlySightLogParsed(flySightLog);
    }
}
