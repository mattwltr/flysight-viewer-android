package matt.wltr.labs.flysightviewer.ui.logview.linechartview;

import android.os.AsyncTask;
import androidx.annotation.NonNull;

import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.util.Map;

import matt.wltr.labs.flysightviewer.flysight.FlySightDataType;
import matt.wltr.labs.flysightviewer.flysight.FlySightRecord;

class LineChartInitializeTask extends AsyncTask<LineChartView, Integer, Void> {

    private LineChartInitializeListener lineChartInitializeListener;

    public LineChartInitializeTask(@NonNull LineChartInitializeListener lineChartInitializeListener) {
        this.lineChartInitializeListener = lineChartInitializeListener;
    }

    @Override
    protected Void doInBackground(LineChartView... lineChartViews) {

        LineChartView lineChartView = lineChartViews[0];

        int totalRecords = lineChartView.flySightLog.getRecords().size();
        int percentageProgress = 0;
        int i = 0;

        for (Map.Entry<OffsetDateTime, FlySightRecord> recordEntry : lineChartView.flySightLog.getRecords().entrySet()) {

            i++;

            for (FlySightDataType dataType : LineChartView.VISIBLE_DATA_TYPES) {

                lineChartView
                        .dataSeriesMap
                        .get(dataType.name())
                        .append(
                                DateTimeUtils.toDate(
                                        ZonedDateTime.ofInstant(recordEntry.getKey().toInstant(), recordEntry.getKey().getOffset().normalized())
                                                .withZoneSameLocal(ZoneId.systemDefault())
                                                .toInstant()),
                                recordEntry.getValue().getValue(dataType));
            }

            lineChartView
                    .dataSeriesMap
                    .get(LineChartView.Y_AXIS_ID_POSITION)
                    .append(
                            DateTimeUtils.toDate(
                                    ZonedDateTime.ofInstant(recordEntry.getKey().toInstant(), recordEntry.getKey().getOffset().normalized())
                                            .withZoneSameLocal(ZoneId.systemDefault())
                                            .toInstant()),
                            0D);

            int newPercentageProgress = (i * 100) / totalRecords;
            if (newPercentageProgress != percentageProgress) {
                percentageProgress = newPercentageProgress;
                lineChartInitializeListener.onProgress(percentageProgress);
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void object) {
        lineChartInitializeListener.onFinish();
    }
}
