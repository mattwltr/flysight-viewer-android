package matt.wltr.labs.flysightviewer.ui.logview.topview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.scichart.charting.model.dataSeries.IXyDataSeries;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.annotations.IAnnotation;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.AxisAlignment;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.extensions.builders.SciChartBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.flysight.FlySightLog;
import matt.wltr.labs.flysightviewer.flysight.FlySightRecord;

public class TopView extends SciChartSurface {

    private static final String X_AXIS_ID = "topViewChartXAxis";
    private static final String Y_AXIS_ID = "topViewChartYAxis";

    private final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

    private final IAnnotation topViewChartMarker =
            sciChartBuilder.newBoxAnnotation().withXAxisId(X_AXIS_ID).withYAxisId(Y_AXIS_ID).withBackgroundDrawableId(R.drawable.top_view_chart_marker).build();

    private IAxis xAxis;
    private IAxis yAxis;

    private FlySightLog flySightLog;

    public TopView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initialize(@NonNull FlySightLog flySightLog) {

        this.flySightLog = flySightLog;

        final IXyDataSeries<Double, Double> dataSeries = sciChartBuilder.newXyDataSeries(Double.class, Double.class).withAcceptsUnsortedData().build();
        for (Map.Entry<Date, FlySightRecord> recordEntry : flySightLog.getRecords().entrySet()) {
            dataSeries.append(recordEntry.getValue().getX(), recordEntry.getValue().getY());
        }

        final int color = getResources().getColor(R.color.top_view_chart);
        initializeAxes(color, dataSeries);

        UpdateSuspender.using(
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        Collections.addAll(getXAxes(), xAxis);
                        Collections.addAll(getYAxes(), yAxis);
                        Collections.addAll(
                                getRenderableSeries(),
                                sciChartBuilder
                                        .newLineSeries()
                                        .withDataSeries(dataSeries)
                                        .withXAxisId(X_AXIS_ID)
                                        .withYAxisId(Y_AXIS_ID)
                                        .withStrokeStyle(color, 1f, true)
                                        .build());
                        Collections.addAll(getAnnotations(), topViewChartMarker);
                        getChartModifiers().add(sciChartBuilder.newModifierGroupWithDefaultModifiers().build());
                    }
                });

        FlySightRecord firstFlySightRecord = flySightLog.getRecords().entrySet().iterator().next().getValue();
        moveMarkerToRecordPosition(firstFlySightRecord);
    }

    private void initializeAxes(int color, IXyDataSeries<Double, Double> dataSeries) {

        VisibleTopViewArea visibleTopViewArea = new VisibleTopViewArea(dataSeries.getXMin(), dataSeries.getXMax(), dataSeries.getYMin(), dataSeries.getYMax());

        yAxis =
                sciChartBuilder
                        .newNumericAxis()
                        .withAxisAlignment(AxisAlignment.Left)
                        .withAxisId(Y_AXIS_ID)
                        .withTextColor(color)
                        .withVisibleRange(visibleTopViewArea.getRangeY())
                        .withAutoRangeMode(AutoRange.Never)
                        .withVisibility(View.GONE)
                        .withDrawMajorGridLines(false)
                        .withDrawMinorGridLines(false)
                        .withDrawMajorBands(false)
                        .build();

        xAxis =
                sciChartBuilder
                        .newNumericAxis()
                        .withAxisAlignment(AxisAlignment.Bottom)
                        .withAxisId(X_AXIS_ID)
                        .withTextColor(color)
                        .withVisibleRange(visibleTopViewArea.getRangeX())
                        .withAutoRangeMode(AutoRange.Never)
                        .withVisibility(View.GONE)
                        .withDrawMajorGridLines(false)
                        .withDrawMinorGridLines(false)
                        .withDrawMajorBands(false)
                        .build();
    }

    public void showDateRange(Date begin, Date end) {

        final IXyDataSeries<Double, Double> dataSeries = sciChartBuilder.newXyDataSeries(Double.class, Double.class).withAcceptsUnsortedData().build();
        for (Map.Entry<Date, FlySightRecord> recordEntry : flySightLog.getRecords(begin, end).entrySet()) {
            dataSeries.append(recordEntry.getValue().getX(), recordEntry.getValue().getY());
        }

        VisibleTopViewArea visibleTopViewArea = new VisibleTopViewArea(dataSeries.getXMin(), dataSeries.getXMax(), dataSeries.getYMin(), dataSeries.getYMax());
        xAxis.setVisibleRange(visibleTopViewArea.getRangeX());
        yAxis.setVisibleRange(visibleTopViewArea.getRangeY());
    }

    public void moveMarkerToRecordPosition(@NonNull FlySightRecord flySightRecord) {

        Double diff = (Double) xAxis.getVisibleRange().getDiff();
        double size = ((diff * 4) / 100) / 2;

        topViewChartMarker.setX1(flySightRecord.getX() - size);
        topViewChartMarker.setY1(flySightRecord.getY() - size);
        topViewChartMarker.setX2(flySightRecord.getX() + size);
        topViewChartMarker.setY2(flySightRecord.getY() + size);
    }
}
