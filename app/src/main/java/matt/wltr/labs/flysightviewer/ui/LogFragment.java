package matt.wltr.labs.flysightviewer.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.scichart.data.model.DateRange;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.flysight.FlySightDataType;
import matt.wltr.labs.flysightviewer.flysight.FlySightLog;
import matt.wltr.labs.flysightviewer.flysight.FlySightRecord;
import matt.wltr.labs.flysightviewer.ui.logview.DistanceView;
import matt.wltr.labs.flysightviewer.ui.logview.DiveAngleView;
import matt.wltr.labs.flysightviewer.ui.logview.DurationView;
import matt.wltr.labs.flysightviewer.ui.logview.HeadingView;
import matt.wltr.labs.flysightviewer.ui.logview.linechartview.FlySightRecordSelectionListener;
import matt.wltr.labs.flysightviewer.ui.logview.linechartview.LineChartView;
import matt.wltr.labs.flysightviewer.ui.logview.linechartview.VisibleDateRangeChangeListener;
import matt.wltr.labs.flysightviewer.ui.logview.linechartview.ZoomEvent;
import matt.wltr.labs.flysightviewer.ui.logview.linechartview.ZoomListener;
import matt.wltr.labs.flysightviewer.ui.logview.topview.TopView;

public class LogFragment extends Fragment {

    private static final String BUNDLE_KEY_FLYSIGHT_LOG = "matt.wltr.labs.flysightviewer.linechart.ui.LogFragment.flySightLog";
    private static final String BUNDLE_KEY_VIEW_LOCKED = "matt.wltr.labs.flysightviewer.linechart.ui.LogFragment.viewLocked";

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

    private Menu mainMenu;

    private boolean viewLocked = false;

    private FlySightLog flySightLog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (flySightLog == null) {
            flySightLog = (FlySightLog) savedInstanceState.getSerializable(BUNDLE_KEY_FLYSIGHT_LOG);
            if (flySightLog == null) {
                throw new IllegalStateException("No FlySight log set");
            }
            viewLocked = savedInstanceState.getBoolean(BUNDLE_KEY_VIEW_LOCKED);
        }

        setHasOptionsMenu(true);

        View root = inflater.inflate(R.layout.fragment_log, null);
        ButterKnife.bind(this, root);

        initializeCharts();

        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        bundle.putSerializable(BUNDLE_KEY_FLYSIGHT_LOG, flySightLog);
        bundle.putBoolean(BUNDLE_KEY_VIEW_LOCKED, viewLocked);
        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        mainMenu = menu;

        MenuItem chartModeMenuItem = mainMenu.findItem(R.id.main_menu_chart_mode);
        chartModeMenuItem.setVisible(true);
        if (viewLocked) {
            lockView(chartModeMenuItem);
        } else {
            unlockView(chartModeMenuItem);
        }

        if (flySightLog.getExit() != null && flySightLog.getOpening() != null) {
            MenuItem skydiveMenuItem = mainMenu.findItem(R.id.main_menu_skydive);
            skydiveMenuItem.setVisible(true);
        }
    }

    private void lockView(@NonNull MenuItem menuItem) {
        viewLocked = true;
        menuItem.setIcon(getResources().getDrawable(R.drawable.ic_lock, getContext().getTheme()));
        lineChartView.disableRubberBandModifier();
        lineChartView.reenableRolloverLine();
    }

    private void unlockView(@NonNull MenuItem menuItem) {
        viewLocked = false;
        menuItem.setIcon(getResources().getDrawable(R.drawable.ic_unlock, getContext().getTheme()));
        lineChartView.enableRubberBandModifier();
        lineChartView.disableRolloverLine();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.main_menu_zoom_out:
                if (viewLocked && lineChartView.isRolloverModifierEnabled()) {
                    lineChartView.reenableRolloverLine();
                }
                lineChartView.zoomExtents();
                menuItem.setVisible(false);
                break;
            case R.id.main_menu_chart_mode:
                if (viewLocked) {
                    unlockView(menuItem);
                } else {
                    lockView(menuItem);
                }
                break;
            case R.id.main_menu_skydive:
                lineChartView.showDateRange(flySightLog.getExit().getDate(), flySightLog.getOpening().getDate());
                lockView(mainMenu.findItem(R.id.main_menu_chart_mode));
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void initializeCharts() {

        lineChartView.initialize(flySightLog);
        lineChartView.setZoomListener(
                new ZoomListener() {
                    @Override
                    public void onZoom(@NonNull ZoomEvent zoomEvent) {
                        if (zoomEvent.equals(ZoomEvent.ZOOM_IN)) {
                            mainMenu.findItem(R.id.main_menu_zoom_out).setVisible(true);
                        }
                    }
                });
        lineChartView.setVisibleDateRangeChangeListener(
                new VisibleDateRangeChangeListener() {
                    @Override
                    public void onVisibleDateRangeChange(@NonNull Date newMin, @NonNull Date newMax) {
                        FlySightRecord flySightRecord = flySightLog.getClosestRecord(newMin);
                        topView.showDateRange(newMin, newMax);
                        topView.moveMarkerToRecordPosition(flySightRecord);
                        headingView.update(flySightRecord.getHeading());
                        diveAngleView.update(flySightRecord.getDiveAngle());
                        durationView.initializeRange(newMin, newMin);
                        distanceView.initializeRange(flySightRecord.getDistance(), flySightRecord.getDistance());
                    }
                });
        lineChartView.setFlySightRecordSelectionListener(
                new FlySightRecordSelectionListener() {
                    @Override
                    public void onFlySightRecordSelect(@NonNull FlySightRecord flySightRecord) {
                        topView.moveMarkerToRecordPosition(flySightRecord);
                        durationView.updateEnd(flySightRecord.getDate());
                        distanceView.updateEnd(flySightRecord.getDistance());
                        headingView.update(flySightRecord.getHeading());
                        diveAngleView.update(flySightRecord.getDiveAngle());
                    }
                });

        topView.initialize(flySightLog);

        FlySightRecord firstRecord = flySightLog.getRecords().entrySet().iterator().next().getValue();
        durationView.initializeRange(firstRecord.getDate(), firstRecord.getDate());
        distanceView.initializeRange(firstRecord.getDistance(), firstRecord.getDistance());
        headingView.update(firstRecord.getHeading());
        diveAngleView.update(firstRecord.getDiveAngle());
    }

    public void setFlySightLog(FlySightLog flySightLog) {
        this.flySightLog = flySightLog;
    }
}
