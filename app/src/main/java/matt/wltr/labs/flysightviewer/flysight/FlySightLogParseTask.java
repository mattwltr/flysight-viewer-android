package matt.wltr.labs.flysightviewer.flysight;

import android.os.AsyncTask;

import java.io.FileInputStream;

public class FlySightLogParseTask extends AsyncTask<FileInputStream, Integer, FlySightLog> {

    private FlySightLogParseObserver flySightLogParseObserver;

    public FlySightLogParseTask(FlySightLogParseObserver flySightLogParseObserver) {
        this.flySightLogParseObserver = flySightLogParseObserver;
    }

    @Override
    protected FlySightLog doInBackground(FileInputStream... inputStreams) {
        return FlySightLogParser.parse(inputStreams[0], ParseMode.ALL, ParsePrecision.INTELLIGENT, percentage -> flySightLogParseObserver.onProgress(percentage));
    }

    @Override
    protected void onPostExecute(FlySightLog flySightLog) {
        flySightLogParseObserver.onFlySightLogParsed(flySightLog);
    }
}
