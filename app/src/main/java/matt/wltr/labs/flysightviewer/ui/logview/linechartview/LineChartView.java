package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;

import com.scichart.charting.model.dataSeries.IXyDataSeries;
import com.scichart.charting.modifiers.RubberBandXyZoomModifier;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.AxisAlignment;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.axes.IAxisCore;
import com.scichart.charting.visuals.axes.VisibleRangeChangeListener;
import com.scichart.charting.visuals.renderableSeries.LineDrawMode;
import com.scichart.charting.visuals.renderableSeries.XyRenderableSeriesBase;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.data.model.DateRange;
import com.scichart.data.model.DoubleRange;
import com.scichart.data.model.IRange;
import com.scichart.drawing.canvas.RenderSurface;
import com.scichart.drawing.common.FontStyle;
import com.scichart.extensions.builders.SciChartBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.flysight.FlySightDataType;
import matt.wltr.labs.flysightviewer.flysight.FlySightLog;
import matt.wltr.labs.flysightviewer.flysight.FlySightRecord;
import matt.wltr.labs.flysightviewer.flysight.MinMax;
import matt.wltr.labs.flysightviewer.flysight.Unit;
import matt.wltr.labs.flysightviewer.ui.logview.SciChartLicenceLoader;

public class LineChartView extends SciChartSurface {

    private static final String X_AXIS_ID = "mainChartXAxis";
    private static final String Y_AXIS_ID_POSITION = "mainChartPositionYAxis";

    /** Rubber band modifier for main chart for zooming */
    private static final RubberBandXyZoomModifier RUBBER_BAND_XY_ZOOM_MODIFIER = new RubberBandXyZoomModifier();

    /** Rollover modifier for main chart to display highlighted values */
    private static final PersistedRolloverModifier ROLLOVER_MODIFIER = new PersistedRolloverModifier();

    static {
        RUBBER_BAND_XY_ZOOM_MODIFIER.setIsXAxisOnly(true);
        RUBBER_BAND_XY_ZOOM_MODIFIER.setReceiveHandledEvents(true);
        RUBBER_BAND_XY_ZOOM_MODIFIER.setIsAnimated(false);

        ROLLOVER_MODIFIER.setIsEnabled(false);
    }

    private static final List<FlySightDataType> VISIBLE_DATA_TYPES =
            Arrays.asList(
                    FlySightDataType.GLIDE_RATIO,
                    FlySightDataType.TOTAL_SPEED,
                    FlySightDataType.VERTICAL_SPEED,
                    FlySightDataType.HORIZONTAL_SPEED,
                    FlySightDataType.ELEVATION);

    private final SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

    private IAxis xAxis;
    private final List<IAxis> yAxes = new ArrayList<>();
    private final List<XyRenderableSeriesBase> xyRenderableSeriesBases = new ArrayList<>();

    private ZoomListener zoomListener;
    private VisibleDateRangeChangeListener visibleDateRangeChangeListener;
    private FlySightRecordSelectionListener flySightRecordSelectionListener;

    private FlySightLog flySightLog;

    public LineChartView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setRenderSurface(new RenderSurface(context.getApplicationContext())); // avoid black flickering on initial draw
    }

    public void initialize(@NonNull FlySightLog flySightLog) {

        this.flySightLog = flySightLog;

        initializeXAxis();
        initializeYAxes();

        UpdateSuspender.using(
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        Collections.addAll(getXAxes(), xAxis);
                        Collections.addAll(getYAxes(), yAxes.toArray(new IAxis[0]));
                        Collections.addAll(getRenderableSeries(), xyRenderableSeriesBases.toArray(new XyRenderableSeriesBase[0]));
                        Collections.addAll(getChartModifiers(), sciChartBuilder.newModifierGroup().withModifier(RUBBER_BAND_XY_ZOOM_MODIFIER).build(), ROLLOVER_MODIFIER);
                    }
                });
    }

    private void initializeXAxis() {

        int xAxisColor = getResources().getColor(R.color.main_chart_x_axis);
        xAxis =
                sciChartBuilder
                        .newDateAxis()
                        .withAxisAlignment(AxisAlignment.Bottom)
                        .withAxisId(X_AXIS_ID)
                        .withTextColor(xAxisColor)
                        .withTickLabelStyle(new FontStyle(Typeface.MONOSPACE, 12, xAxisColor))
                        .withTextFormatting("HH:mm:ss")
                        .withAxisInfoProvider(new XAxisInfoProvider())
                        .build();

        xAxis.setVisibleRangeChangeListener(
                new VisibleRangeChangeListener() {

                    private boolean initialized = false;

                    @Override
                    public void onVisibleRangeChanged(IAxisCore axis, IRange oldRange, IRange newRange, boolean isAnimating) {

                        if (!initialized) {
                            // there's an initial - somehow confusing - call to this listener when
                            // the chart gets rendered.
                            // we ignore it since there is no human interacted range change; it's
                            // just the initial drawing.
                            initialized = true;
                            return;
                        }

                        Date newMin = (Date) newRange.getMin();
                        Date oldMin = (Date) oldRange.getMin();
                        Date newMax = (Date) newRange.getMax();
                        Date oldMax = (Date) oldRange.getMax();

                        if (zoomListener != null && hasZoomedIn(newMin, oldMin, newMax, oldMax)) {
                            zoomListener.onZoom(ZoomEvent.ZOOM_IN);
                        }

                        if (ROLLOVER_MODIFIER.getIsEnabled()) {
                            reenableRolloverLine();
                        } else {
                            disableRolloverLine();
                        }

                        IAxis glideRatioYAxis = getYAxes().getAxisById(FlySightDataType.GLIDE_RATIO.name());
                        MinMax glideRatioMinMax = flySightLog.getMinMax(FlySightDataType.GLIDE_RATIO, newMin, newMax);
                        glideRatioYAxis.setVisibleRange(new DoubleRange(glideRatioMinMax.getMin(), glideRatioMinMax.getMax()));

                        if (visibleDateRangeChangeListener != null) {
                            visibleDateRangeChangeListener.onVisibleDateRangeChange(newMin, newMax);
                        }
                    }

                    private boolean hasZoomedIn(Date newMin, Date oldMin, Date newMax, Date oldMax) {
                        return newMin.after(oldMin) || newMax.before(oldMax);
                    }
                });
    }

    private void initializeYAxes() {

        for (FlySightDataType dataType : VISIBLE_DATA_TYPES) {

            int color = getColor(dataType);

            xyRenderableSeriesBases.add(
                    sciChartBuilder
                            .newLineSeries()
                            .withDataSeries(createXyDataSeries(flySightLog, dataType))
                            .withSeriesInfoProvider(new YAxisInfoProvider(getValuePattern(dataType, dataType.getUnit()), color))
                            .withXAxisId(xAxis.getAxisId())
                            .withYAxisId(dataType.name())
                            .withStrokeStyle(color, 1f, true)
                            .withDrawLineMode(LineDrawMode.Gaps)
                            .build());

            FontStyle axisFontStyle = new FontStyle(Typeface.MONOSPACE, 12, color);

            IAxis yAxis =
                    sciChartBuilder
                            .newNumericAxis()
                            .withGrowBy(new DoubleRange(0.1d, 0.1d))
                            .withAxisAlignment(AxisAlignment.Left)
                            .withAxisId(dataType.name())
                            .withTextFormatting("0.#")
                            .withTextColor(color)
                            .withAxisTitle(getAxisTitle(dataType, dataType.getUnit()))
                            .withAxisTitleStyle(axisFontStyle)
                            .withAutoRangeMode(AutoRange.Always)
                            .withTickLabelStyle(axisFontStyle)
                            .withMinorTickLineStyle(getResources().getColor(R.color.main_chart_minor_tick), 1, true)
                            .withMajorTickLineStyle(getResources().getColor(R.color.main_chart_major_tick), 1, true)
                            .build();

            if (dataType == FlySightDataType.GLIDE_RATIO) {
                yAxis.setAutoRange(AutoRange.Never); // don't use AutoRange here because values might reach Infinity and will be cut off to "10"
                MinMax glideRatioMinMax = flySightLog.getMinMax(FlySightDataType.GLIDE_RATIO, null, null);
                yAxis.setVisibleRange(new DoubleRange(glideRatioMinMax.getMin(), glideRatioMinMax.getMax()));
            } else {
                yAxis.setVisibleRangeChangeListener(
                        new VisibleRangeChangeListener() {
                            @Override
                            public void onVisibleRangeChanged(IAxisCore iAxisCore, IRange oldRange, IRange newRange, boolean isAnimating) {
                                if (oldRange.getMinAsDouble() == newRange.getMinAsDouble() && oldRange.getMaxAsDouble() == newRange.getMaxAsDouble()) {
                                    return;
                                }
                                if (iAxisCore.getDataRange().getMinAsDouble() >= 0 && newRange.getMinAsDouble() < 0) {
                                    iAxisCore.setVisibleRange(new DoubleRange(0D, newRange.getMaxAsDouble()));
                                }
                            }
                        });
            }
            yAxes.add(yAxis);
        }

        // position y-axis
        IAxis positionYAxis =
                sciChartBuilder
                        .newNumericAxis()
                        .withAxisAlignment(AxisAlignment.Left)
                        .withAxisId(Y_AXIS_ID_POSITION)
                        .withAutoRangeMode(AutoRange.Never)
                        .withVisibleRange(10, 20)
                        .withVisibility(View.GONE)
                        .build();
        positionYAxis.setVisibleRangeChangeListener(
                new VisibleRangeChangeListener() {
                    @Override
                    public void onVisibleRangeChanged(IAxisCore iAxisCore, IRange oldRange, IRange newRange, boolean isAnimating) {
                        iAxisCore.setVisibleRange(new DoubleRange(10D, 20D));
                    }
                });
        yAxes.add(positionYAxis);

        // position (fake) data with selection observer that will "animate" the topViewChart
        final IXyDataSeries<Date, Double> positionDataSeries = sciChartBuilder.newXyDataSeries(Date.class, Double.class).build();
        for (Map.Entry<Date, FlySightRecord> recordEntry : flySightLog.getRecords().entrySet()) {
            positionDataSeries.append(recordEntry.getKey(), 0D);
        }
        xyRenderableSeriesBases.add(
                sciChartBuilder
                        .newLineSeries()
                        .withDataSeries(positionDataSeries)
                        .withSeriesInfoProvider(
                                new YAxisSelectionInfoProvider(
                                        new YAxisSelectionListener() {
                                            @Override
                                            public void onSelect(@NonNull Date date) {
                                                if (flySightRecordSelectionListener != null) {
                                                    FlySightRecord flySightRecord = flySightLog.getRecords().get(date);
                                                    if (flySightRecord != null) {
                                                        flySightRecordSelectionListener.onFlySightRecordSelect(flySightRecord);
                                                    }
                                                }
                                            }
                                        }))
                        .withXAxisId(X_AXIS_ID)
                        .withYAxisId(Y_AXIS_ID_POSITION)
                        .build());
    }

    public IXyDataSeries<Date, Double> createXyDataSeries(@NonNull FlySightLog flySightLog, @NonNull FlySightDataType dataType) {
        final IXyDataSeries<Date, Double> dataSeries = sciChartBuilder.newXyDataSeries(Date.class, Double.class).withSeriesName(dataType.name()).build();
        for (Map.Entry<Date, FlySightRecord> recordEntry : flySightLog.getRecords().entrySet()) {
            dataSeries.append(recordEntry.getKey(), recordEntry.getValue().getValue(dataType));
        }
        return dataSeries;
    }

    private String getAxisTitle(@NonNull FlySightDataType dataType, @NonNull Unit unit) {
        String title;
        switch (dataType) {
            case ELEVATION:
                title = getResources().getString(R.string.elevation);
                break;
            case HORIZONTAL_SPEED:
                title = getResources().getString(R.string.horizontal_speed);
                break;
            case VERTICAL_SPEED:
                title = getResources().getString(R.string.vertical_speed);
                break;
            case TOTAL_SPEED:
                title = getResources().getString(R.string.total_speed);
                break;
            case GLIDE_RATIO:
                title = getResources().getString(R.string.glide_ratio);
                break;
            case HEADING:
                title = getResources().getString(R.string.heading);
                break;
            default:
                title = "";
        }
        title += formatUnit(unit, " (%s)");
        return title;
    }

    private int getColor(@NonNull FlySightDataType dataType) {
        switch (dataType) {
            case ELEVATION:
                return getResources().getColor(R.color.main_chart_elevation);
            case HORIZONTAL_SPEED:
                return getResources().getColor(R.color.main_chart_horizontal_speed);
            case VERTICAL_SPEED:
                return getResources().getColor(R.color.main_chart_vertical_speed);
            case TOTAL_SPEED:
                return getResources().getColor(R.color.main_chart_total_speed);
            case GLIDE_RATIO:
                return getResources().getColor(R.color.main_chart_glide_ratio);
            case HEADING:
                return getResources().getColor(R.color.main_chart_course);
            default:
                return getResources().getColor(R.color.primary);
        }
    }

    private String getValuePattern(@NonNull FlySightDataType dataType, @NonNull Unit unit) {
        String pattern;
        switch (dataType) {
            case ELEVATION:
                pattern = "0";
                break;
            case HORIZONTAL_SPEED:
            case VERTICAL_SPEED:
            case TOTAL_SPEED:
            case HEADING:
                pattern = "0.0";
                break;
            case GLIDE_RATIO:
                pattern = "0.000";
                break;
            default:
                pattern = "#";
        }
        pattern += formatUnit(unit, " %s");
        return pattern;
    }

    private String formatUnit(@NonNull Unit unit, @NonNull String pattern) {
        String formattedValue;
        switch (unit) {
            case METER:
                formattedValue = String.format(pattern, getResources().getString(R.string.meter));
                break;
            case KILOMETER_PER_HOUR:
                formattedValue = String.format(pattern, getResources().getString(R.string.kilometer_per_hour));
                break;
            case DEGREE:
                formattedValue = String.format(pattern, getResources().getString(R.string.degree));
                break;
            case NO_UNIT:
            default:
                formattedValue = "";
        }
        return formattedValue;
    }

    public void showDateRange(@NonNull Date begin, @NonNull Date end) {
        xAxis.setVisibleRange(new DateRange(begin, end));
    }

    public void reenableRolloverLine() {
        getChartModifiers().remove(ROLLOVER_MODIFIER);
        getChartModifiers().add(ROLLOVER_MODIFIER);
        ROLLOVER_MODIFIER.setIsEnabled(true);
        ROLLOVER_MODIFIER.getVerticalLinePaint().setColor(Color.WHITE);
        ROLLOVER_MODIFIER.getVerticalLinePaint().setStrokeWidth(0);
    }

    public boolean isRolloverModifierEnabled() {
        return ROLLOVER_MODIFIER.getIsEnabled();
    }

    public void disableRolloverLine() {
        getChartModifiers().remove(ROLLOVER_MODIFIER);
        getChartModifiers().add(ROLLOVER_MODIFIER);
        ROLLOVER_MODIFIER.setIsEnabled(false);
    }

    public void enableRubberBandModifier() {
        RUBBER_BAND_XY_ZOOM_MODIFIER.setIsEnabled(true);
    }

    public void disableRubberBandModifier() {
        RUBBER_BAND_XY_ZOOM_MODIFIER.setIsEnabled(false);
    }

    public ZoomListener getZoomListener() {
        return zoomListener;
    }

    public void setZoomListener(ZoomListener zoomListener) {
        this.zoomListener = zoomListener;
    }

    public VisibleDateRangeChangeListener getVisibleDateRangeChangeListener() {
        return visibleDateRangeChangeListener;
    }

    public void setVisibleDateRangeChangeListener(VisibleDateRangeChangeListener visibleDateRangeChangeListener) {
        this.visibleDateRangeChangeListener = visibleDateRangeChangeListener;
    }

    public FlySightRecordSelectionListener getFlySightRecordSelectionListener() {
        return flySightRecordSelectionListener;
    }

    public void setFlySightRecordSelectionListener(FlySightRecordSelectionListener flySightRecordSelectionListener) {
        this.flySightRecordSelectionListener = flySightRecordSelectionListener;
    }
}
