package matt.wltr.labs.flysightviewer.flysight;

public interface FlySightLogParseObserver {

    void onProgress(int percentage);

    void onFlySightLogParsed(FlySightLog flySightLog);
}
