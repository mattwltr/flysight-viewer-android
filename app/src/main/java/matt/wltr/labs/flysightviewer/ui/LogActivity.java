package matt.wltr.labs.flysightviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.scichart.extensions.builders.SciChartBuilder;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.GZIPInputStream;
import matt.wltr.labs.flysightviewer.flysight.FlySightLog;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogMetadata;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogParseObserver;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogParseTask;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogRepository;
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

    public static final String FLY_SIGHT_LOG_METADATA_INTENT_KEY = "matt.wltr.labs.flysightviewer.flysight.FlySightLogMetadata";

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

    private FlySightLogMetadata flySightLogMetadata;

    private FlySightLog flySightLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setTitle(getString(R.string.loading));
        setupSciChart(this);
        setContentView(R.layout.activity_log);
        ButterKnife.bind(this);

        loaderProgressBar.setProgress(0);
        loaderProgressLabel.setText("0");
        loader.setVisibility(View.VISIBLE);

        flySightLogMetadata = (FlySightLogMetadata) getIntent().getSerializableExtra(FLY_SIGHT_LOG_METADATA_INTENT_KEY);
        if (flySightLogMetadata == null) {
            return;
        }
        File flySightLogFile = FlySightLogRepository.getLogFile(this, flySightLogMetadata);
        if (!flySightLogFile.exists()) {
            return;
        }

        GZIPInputStream inputStream;
        try {
            inputStream =
                    new GZIPInputStream(
                            getContentResolver().openInputStream(Uri.fromFile(flySightLogFile)),
                            ((FileInputStream) getContentResolver().openInputStream(Uri.fromFile(flySightLogFile))).getChannel().size());
        } catch (IOException e) {
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
                                    initializeViews();
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
            initializeViews();
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

    private void navigateBack() {
        Intent intent = NavUtils.getParentActivityIntent(LogActivity.this);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(LogActivity.this, intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                navigateBack();
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
            case R.id.log_menu_description:
                showTagsDialog();
                return true;
            case R.id.log_menu_timezone:
                showTimezoneDialog();
                return true;
            case R.id.log_menu_delete:
                showDeleteDialog();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void showTagsDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.view_description_dialog, null);

        List<String> existingTags = new ArrayList<>(FlySightLogRepository.getAllFlySightMetadataTags(LogActivity.this));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(LogActivity.this, android.R.layout.simple_dropdown_item_1line, existingTags);
        final NachoTextView tagsView = dialogView.findViewById(R.id.tags);
        tagsView.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN);
        tagsView.setAdapter(adapter);
        tagsView.setText(flySightLogMetadata.getTags());

        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle(getString(R.string.log_menu_tags));
        dialogBuilder.setPositiveButton(
                android.R.string.yes,
                (dialog, whichButton) -> {



                    flySightLogMetadata.setTags(new ArrayList<>(new LinkedHashSet<>(tagsView.getChipAndTokenValues())));
                    try {
                        FlySightLogRepository.saveMetadata(this, flySightLogMetadata);
                        setSubtitle(flySightLogMetadata.getTags());
                    } catch (IOException e) {
                        Log.e(LogActivity.class.getSimpleName(), "Could not save metadata", e);
                    }
                });
        dialogBuilder.setNegativeButton(android.R.string.no, null);
        dialogBuilder.create().show();
    }

    class TimezoneAdapter extends ArrayAdapter<String> {

        private final List<String> values;

        public TimezoneAdapter(Context context, List<String> values) {
            super(context, -1, values);
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_timezone_list_item, parent, false);

            TextView textView = rowView.findViewById(R.id.timezone_label);
            textView.setText(values.get(position));

            RadioButton radioButton = rowView.findViewById(R.id.timezone_radio);
            radioButton.setChecked(((ListView) parent).getCheckedItemPosition() == position);

            return rowView;
        }
    }

    private void showTimezoneDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.view_timezone_dialog, null);

        final List<String> timezones = Arrays.asList(TimeZone.getAvailableIDs());
        final List<String> visibleTimezones = new ArrayList<>(timezones);

        final TimezoneAdapter timezoneAdapter = new TimezoneAdapter(this, visibleTimezones);

        final ListView listView = dialogView.findViewById(R.id.log_timezones);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(timezoneAdapter);

        final EditText filterView = dialogView.findViewById(R.id.log_timezone_filter);
        filterView.addTextChangedListener(
                new TextWatcher() {

                    final android.os.Handler handler = new Handler();
                    Runnable runnable;

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        handler.removeCallbacks(runnable);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        runnable =
                                () -> {
                                    String input = filterView.getText().toString();
                                    visibleTimezones.clear();
                                    if (!input.trim().isEmpty()) {
                                        for (String zoneId : timezones) {
                                            if (zoneId.toLowerCase().contains(input.toLowerCase())) {
                                                visibleTimezones.add(zoneId);
                                            }
                                        }
                                    } else {
                                        visibleTimezones.addAll(timezones);
                                    }
                                    timezoneAdapter.notifyDataSetChanged();
                                    listView.clearChoices();
                                };
                        handler.postDelayed(runnable, 500);
                    }
                });

        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle(getString(R.string.log_menu_timezone));
        dialogBuilder.setPositiveButton(
                android.R.string.yes,
                (dialog, whichButton) -> {
                    String zoneId = visibleTimezones.get(listView.getCheckedItemPosition());
                    flySightLogMetadata.setZoneId(ZoneId.of(zoneId));
                    try {
                        // save
                        FlySightLogRepository.saveMetadata(this, flySightLogMetadata);

                        // update view title
                        setTitle(getFormattedDateTime());

                        // update x-axis of line chart
                        lineChartView.showDataWithZoneId(flySightLogMetadata.getZoneId());

                        // check if there're more log at this location
                        List<FlySightLogMetadata> flySightLogMetadataList =
                                FlySightLogRepository.getFlySightLogMetadataByLatLonWithoutZoneId(this, flySightLogMetadata.getLatLon(), 20000);
                        if (!flySightLogMetadataList.isEmpty()) {
                            showTimezoneDialog(flySightLogMetadataList);
                        }
                    } catch (IOException e) {
                        Log.e(LogActivity.class.getSimpleName(), "Could not save metadata", e);
                    }
                });
        dialogBuilder.setNegativeButton(android.R.string.no, null);
        dialogBuilder.create().show();
    }

    private void showTimezoneDialog(List<FlySightLogMetadata> flySightLogMetadataList) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.log_menu_timezone))
                .setMessage(getString(R.string.log_menu_timezone_description, flySightLogMetadataList.size(), flySightLogMetadata.getZoneId().toString()))
                .setPositiveButton(
                        android.R.string.yes,
                        (dialog, whichButton) -> {
                            for (FlySightLogMetadata flySightLogMetadata : flySightLogMetadataList) {
                                try {
                                    flySightLogMetadata.setZoneId(this.flySightLogMetadata.getZoneId());
                                    FlySightLogRepository.saveMetadata(LogActivity.this, flySightLogMetadata);
                                } catch (IOException e) {
                                    Log.e(LogActivity.class.getSimpleName(), "Could not save metadata", e);
                                }
                            }
                        })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.log_delete_confirmation_title))
                .setMessage(getString(R.string.log_delete_confirmation_description))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(
                        android.R.string.yes,
                        (dialog, whichButton) -> {
                            try {
                                FlySightLogRepository.deleteLogFile(LogActivity.this, flySightLogMetadata);
                                navigateBack();
                            } catch (IOException e) {
                                Log.e(LogActivity.class.getSimpleName(), "Could not save metadata", e);
                            }
                        })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void initializeViews() {
        lineChartView.initialize(flySightLog, flySightLogMetadata);
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

                        flySightLogMetadata.setOpened(true);
                        try {
                            FlySightLogRepository.saveMetadata(LogActivity.this, flySightLogMetadata);
                        } catch (IOException e) {
                            Log.e(LogActivity.class.getSimpleName(), "Could not save metadata", e);
                        }

                        setTitle(getFormattedDateTime());
                        setSubtitle(flySightLogMetadata.getTags());

                        logMenu.findItem(R.id.log_menu_zoom_out).setVisible(true);
                        logMenu.findItem(R.id.log_menu_description).setVisible(true);
                        logMenu.findItem(R.id.log_menu_timezone).setVisible(true);
                        logMenu.findItem(R.id.log_menu_delete).setVisible(true);

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

                        loaderProgressBar.setProgress(0);
                        loader.setVisibility(View.GONE);
                        logContainer.setVisibility(View.VISIBLE);
                        logContainer.setOnClickListener(
                                view -> {
                                    if (lineChartView.isRolloverModifierEnabled()) {
                                        lineChartView.disableRolloverLine();
                                        lineChartView.enableRolloverLine();
                                    }
                                });

                        topView.initialize(flySightLog);

                        FlySightRecord firstRecord = flySightLog.getFirstRecord();
                        durationView.initializeRange(firstRecord.getDate(), firstRecord.getDate());
                        distanceView.initializeRange(firstRecord.getDistance(), firstRecord.getDistance());
                        headingView.update(firstRecord.getHeading());
                        diveAngleView.update(firstRecord.getDiveAngle());
                    }
                });
    }

    private void setSubtitle(List<String> tags) {
        if (getSupportActionBar() != null && tags != null && !tags.isEmpty()) {
            getSupportActionBar().setSubtitle(TextUtils.join(" · ", tags));
        }
    }

    public String getFormattedDateTime() {
        if (flySightLogMetadata.getZoneId() != null) {
            ZonedDateTime zonedDateTime = flySightLogMetadata.getUtcDate().toZonedDateTime().withZoneSameInstant(flySightLogMetadata.getZoneId());
            return zonedDateTime.format(DATE_TIME_FORMAT);
        } else {
            return DATE_TIME_FORMAT.format(flySightLogMetadata.getUtcDate()) + " UTC";
        }
    }
}
