package matt.wltr.labs.flysightviewer.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.scichart.extensions.builders.SciChartBuilder;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.ui.logview.SciChartLicenceLoader;
import matt.wltr.labs.flysightviewer.flysight.FlySightLog;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogParseTask;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogParserObserver;

public class MainActivity extends AppCompatActivity {

    private static final int READ_FILE_REQUEST_CODE = 1;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupSciChart();
    }

    private void setupSciChart() {
        SciChartBuilder.init(this);
        SciChartLicenceLoader.initializeSciChartLicense();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.main_menu_choose_file:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                startActivityForResult(intent, READ_FILE_REQUEST_CODE);
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (Activity.RESULT_OK == resultCode) {
            switch (requestCode) {
                case READ_FILE_REQUEST_CODE:
                    if (resultData != null) {
                        onFileChosen(resultData.getData());
                    }
                    break;
            }
        }
    }

    private void onFileChosen(Uri uri) {

        final LoaderFragment loaderFragment = new LoaderFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.chart_container, loaderFragment).commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();

        InputStream inputStream;
        try {
            inputStream = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            return;
        }

        FlySightLogParserObserver flySightLogParserObserver =
                new FlySightLogParserObserver() {
                    @Override
                    public void onFlySightLogParsed(FlySightLog flySightLog) {

                        if (flySightLog == null) {
                            return;
                        }

                        setTitle(DATE_FORMAT.format(flySightLog.getRecords().keySet().iterator().next()));

                        LogFragment logFragment = new LogFragment();
                        logFragment.setFlySightLog(flySightLog);

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.chart_container, logFragment).commitAllowingStateLoss();
                        fragmentManager.executePendingTransactions();
                    }
                };

        new FlySightLogParseTask(flySightLogParserObserver).execute(inputStream);
    }
}
