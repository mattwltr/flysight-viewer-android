package matt.wltr.labs.flysightviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.scichart.extensions.builders.SciChartBuilder;

import org.threeten.bp.format.DateTimeFormatter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import butterknife.BindView;
import butterknife.ButterKnife;
import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.flysight.FlySightLog;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogParseObserver;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogParseTask;
import matt.wltr.labs.flysightviewer.flysight.FlySightRecord;
import matt.wltr.labs.flysightviewer.ui.logview.DistanceView;
import matt.wltr.labs.flysightviewer.ui.logview.DiveAngleView;
import matt.wltr.labs.flysightviewer.ui.logview.DurationView;
import matt.wltr.labs.flysightviewer.ui.logview.HeadingView;
import matt.wltr.labs.flysightviewer.ui.logview.SciChartLicenceLoader;
import matt.wltr.labs.flysightviewer.ui.logview.linechartview.LineChartInitializeListener;
import matt.wltr.labs.flysightviewer.ui.logview.linechartview.LineChartView;
import matt.wltr.labs.flysightviewer.ui.logview.topview.TopView;

public class LogActivity extends AppCompatActivity {

    public static final String FLY_SIGHT_LOG_URI_INTENT_KEY = "matt.wltr.labs.flysightviewer.flysight.FlySightLog";

    private static final String BUNDLE_KEY_FLY_SIGHT_LOG = "matt.wltr.labs.flysightviewer.linechart.ui.LogActivity.flySightLog";
    private static final String BUNDLE_KEY_VIEW_LOCKED = "matt.wltr.labs.flysightviewer.linechart.ui.LogActivity.viewLocked";

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @BindView(R.id.loader)
    RelativeLayout loader;

    @BindView(R.id.loader_progress)
    ProgressBar loaderProgressBar;

    @BindView(R.id.loader_progress_label)
    TextView loaderProgressLabel;

    @BindView(R.id.log_container)
    FrameLayout logContainer;

    @BindView(R.id.line_chart)
    LineChartView lineChartView;

    @BindView(R.id.top_view)
    TopView topView;

    @BindView(R.id.heading)
    HeadingView headingView;

    @BindView(R.id.dive_angle)
    DiveAngleView diveAngleView;

    @BindView(R.id.duration)
    DurationView durationView;

    @BindView(R.id.distance)
    DistanceView distanceView;

    private Handler progressHandler = new Handler();

    private Menu logMenu;

    private boolean viewLocked = false;

    private FlySightLog flySightLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AndroidThreeTen.init(this);

        setTitle(getString(R.string.loading));
        setupSciChart(this);

        setContentView(R.layout.activity_log);

        ButterKnife.bind(this);

        loader.setVisibility(View.VISIBLE);
        loaderProgressBar.setProgress(0);

        String flySightLogUri = getIntent().getStringExtra(FLY_SIGHT_LOG_URI_INTENT_KEY);
        if (flySightLogUri == null) {
            return;
        }
        FileInputStream inputStream;
        try {
            inputStream = (FileInputStream) getContentResolver().openInputStream(Uri.parse(flySightLogUri));
        } catch (FileNotFoundException e) {
            return;
        }

        new FlySightLogParseTask(
                        new FlySightLogParseObserver() {
                            @Override
                            public void onProgress(int percentage) {
                                final int absolutePercentage = percentage / 2;
                                progressHandler.post(
                                        () -> {
                                            loaderProgressBar.setProgress(absolutePercentage);
                                            loaderProgressLabel.setText(String.valueOf(absolutePercentage));
                                        });
                            }

                            @Override
                            public void onFlySightLogParsed(FlySightLog parsedFlySightLog) {
                                if (parsedFlySightLog == null) {
                                    return;
                                }
                                flySightLog = parsedFlySightLog;
                                if (logMenu != null) {
                                    initializeCharts();
                                }
                            }
                        })
                .execute(inputStream);
    }

    private void setupSciChart(@NonNull Context context) {
        SciChartBuilder.init(context);
        SciChartLicenceLoader.initializeSciChartLicense();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        bundle.putSerializable(BUNDLE_KEY_FLY_SIGHT_LOG, flySightLog);
        bundle.putBoolean(BUNDLE_KEY_VIEW_LOCKED, viewLocked);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        logMenu = menu;
        getMenuInflater().inflate(R.menu.log_menu, menu);
        if (flySightLog != null) {
            initializeCharts();
        }
        return true;
    }

    private void lockView(@NonNull MenuItem menuItem) {
        viewLocked = true;
        menuItem.setIcon(getResources().getDrawable(R.drawable.ic_lock, getTheme()));
        lineChartView.disableRubberBandModifier();
        lineChartView.enableRolloverLine();
    }

    private void unlockView(@NonNull MenuItem menuItem) {
        viewLocked = false;
        menuItem.setIcon(getResources().getDrawable(R.drawable.ic_unlock, getTheme()));
        lineChartView.enableRubberBandModifier();
        lineChartView.disableRolloverLine();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.navigateUpTo(this, intent);
                }
                return true;
            case R.id.log_menu_zoom_out:
                if (viewLocked && lineChartView.isRolloverModifierEnabled()) {
                    lineChartView.enableRolloverLine();
                }
                lineChartView.zoomExtents();
                return true;
            case R.id.log_menu_chart_mode:
                if (viewLocked) {
                    unlockView(menuItem);
                } else {
                    lockView(menuItem);
                }
                return true;
            case R.id.log_menu_skydive:
                lineChartView.showDateRange(flySightLog.getExit().getDate(), flySightLog.getOpening().getDate());
                lockView(logMenu.findItem(R.id.log_menu_chart_mode));
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void initializeCharts() {

        lineChartView.initialize(flySightLog);
        lineChartView.setVisibleDateRangeChangeListener(
                (newMin, newMax) -> {
                    FlySightRecord flySightRecord = flySightLog.getClosestRecord(newMin);
                    topView.showDateRange(newMin, newMax);
                    topView.moveMarkerToRecordPosition(flySightRecord);
                    headingView.update(flySightRecord.getHeading());
                    diveAngleView.update(flySightRecord.getDiveAngle());
                    durationView.initializeRange(newMin, newMin);
                    distanceView.initializeRange(flySightRecord.getDistance(), flySightRecord.getDistance());
                });
        lineChartView.setFlySightRecordSelectionListener(
                flySightRecord -> {
                    topView.moveMarkerToRecordPosition(flySightRecord);
                    durationView.updateEnd(flySightRecord.getDate());
                    distanceView.updateEnd(flySightRecord.getDistance());
                    headingView.update(flySightRecord.getHeading());
                    diveAngleView.update(flySightRecord.getDiveAngle());
                });
        lineChartView.setLineChartInitializeListener(
                new LineChartInitializeListener() {
                    @Override
                    public void onProgress(int percentage) {
                        final int absolutePercentage = 50 + (percentage / 2);
                        progressHandler.post(
                                () -> {
                                    loaderProgressBar.setProgress(absolutePercentage);
                                    loaderProgressLabel.setText(String.valueOf(absolutePercentage));
                                });
                    }

                    @Override
                    public void onFinish() {

                        setTitle(getString(R.string.utc_date_format, DATE_TIME_FORMAT.format(flySightLog.getRecords().keySet().iterator().next())));

                        logMenu.findItem(R.id.log_menu_zoom_out).setVisible(true);

                        MenuItem chartModeMenuItem = logMenu.findItem(R.id.log_menu_chart_mode);
                        chartModeMenuItem.setVisible(true);
                        if (viewLocked) {
                            lockView(chartModeMenuItem);
                        } else {
                            unlockView(chartModeMenuItem);
                        }

                        if (flySightLog.getExit() != null && flySightLog.getOpening() != null) {
                            MenuItem skydiveMenuItem = logMenu.findItem(R.id.log_menu_skydive);
                            skydiveMenuItem.setVisible(true);
                        }

                        loader.setVisibility(View.GONE);
                        logContainer.setVisibility(View.VISIBLE);

                        topView.initialize(flySightLog);

                        FlySightRecord firstRecord = flySightLog.getRecords().entrySet().iterator().next().getValue();
                        durationView.initializeRange(firstRecord.getDate(), firstRecord.getDate());
                        distanceView.initializeRange(firstRecord.getDistance(), firstRecord.getDistance());
                        headingView.update(firstRecord.getHeading());
                        diveAngleView.update(firstRecord.getDiveAngle());
                    }
                });
    }
}
