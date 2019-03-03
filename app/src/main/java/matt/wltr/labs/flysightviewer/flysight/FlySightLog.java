package matt.wltr.labs.flysightviewer.flysight;

import android.support.annotation.NonNull;

import org.threeten.bp.OffsetDateTime;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlySightLog implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final List<FlySightDataType> MIN_MAX_DATA_TYPES =
            Arrays.asList(
                    FlySightDataType.ELEVATION,
                    FlySightDataType.HORIZONTAL_SPEED,
                    FlySightDataType.VERTICAL_SPEED,
                    FlySightDataType.TOTAL_SPEED,
                    FlySightDataType.GLIDE_RATIO,
                    FlySightDataType.DIVE_ANGLE);

    private Map<OffsetDateTime, FlySightRecord> records;

    /** min values by FlySightDataType */
    private Map<FlySightDataType, FlySightRecord> min = new HashMap<>();

    /** max values by FlySightDataType */
    private Map<FlySightDataType, FlySightRecord> max = new HashMap<>();

    private FlySightRecord exit;

    private FlySightRecord opening;

    public FlySightLog(@NonNull Map<OffsetDateTime, FlySightRecord> records) {
        this.records = records;
        defineExtrema();
        defineExitAndOpening();
    }

    private void defineExtrema() {
        for (Map.Entry<OffsetDateTime, FlySightRecord> recordEntry : this.records.entrySet()) {
            for (FlySightDataType dataType : MIN_MAX_DATA_TYPES) {
                if (!min.containsKey(dataType) || (recordEntry.getValue().getValue(dataType) < min.get(dataType).getValue(dataType))) {
                    min.put(dataType, recordEntry.getValue());
                }
                if (!max.containsKey(dataType) || (recordEntry.getValue().getValue(dataType) > max.get(dataType).getValue(dataType))) {
                    max.put(dataType, recordEntry.getValue());
                }
            }
        }
    }

    private void defineExitAndOpening() {
        for (Map.Entry<OffsetDateTime, FlySightRecord> recordEntry : this.records.entrySet()) {
            if (exit == null && recordEntry.getValue().getSpeedDown() > 40D) {
                exit = recordEntry.getValue();
            } else if (exit != null && recordEntry.getValue().getSpeedDown() < 40D) {
                opening = recordEntry.getValue();
                return;
            }
        }
    }

    public FlySightRecord getFirstRecord() {
        return records.entrySet().iterator().next().getValue();
    }

    public Map<OffsetDateTime, FlySightRecord> getRecords(OffsetDateTime min, OffsetDateTime max) {

        if (this.records == null) {
            return null;
        }

        LinkedHashMap<OffsetDateTime, FlySightRecord> records = new LinkedHashMap<>();
        for (Map.Entry<OffsetDateTime, FlySightRecord> recordEntry : this.records.entrySet()) {
            if ((recordEntry.getKey().equals(min) || recordEntry.getKey().isAfter(min)) && (recordEntry.getKey().equals(max) || recordEntry.getKey().isBefore(max))) {
                records.put(recordEntry.getKey(), recordEntry.getValue());
            }
        }
        return records;
    }

    public FlySightRecord getClosestRecord(OffsetDateTime date) {

        OffsetDateTime closestDate = records.keySet().iterator().next();

        long difference = Long.MAX_VALUE;

        for (OffsetDateTime key : records.keySet()) {
            long newDifference = calculateDifference(date, key);
            if (newDifference == 0) {
                return records.get(key);
            }
            if (newDifference < difference) {
                difference = newDifference;
                closestDate = key;
            } else if (newDifference > difference) {
                return records.get(closestDate);
            }
        }
        return records.get(closestDate);
    }

    private long calculateDifference(OffsetDateTime firstValue, OffsetDateTime secondValue) {
        if (firstValue == secondValue) {
            return 0L;
        }
        return firstValue.toInstant().toEpochMilli() > secondValue.toInstant().toEpochMilli()
                ? firstValue.toInstant().toEpochMilli() - secondValue.toInstant().toEpochMilli()
                : secondValue.toInstant().toEpochMilli() - firstValue.toInstant().toEpochMilli();
    }

    public MinMax getMinMax(FlySightDataType dataType, OffsetDateTime rangeBegin, OffsetDateTime rangeEnd) {

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (Map.Entry<OffsetDateTime, FlySightRecord> entry : records.entrySet()) {

            FlySightRecord flySightRecord = entry.getValue();

            if ((rangeBegin != null && flySightRecord.getDate().isBefore(rangeBegin)) || (rangeEnd != null && flySightRecord.getDate().isAfter(rangeEnd))) {
                continue;
            }

            Double value = flySightRecord.getValue(dataType);
            if (value == null) {
                continue;
            }

            if (value != Double.NEGATIVE_INFINITY && value < min) {
                min = value;
            }
            if (value != Double.POSITIVE_INFINITY && value > max) {
                max = value;
            }
        }
        return new MinMax(min, max);
    }

    public Map<OffsetDateTime, FlySightRecord> getRecords() {
        return records;
    }

    public Map<FlySightDataType, FlySightRecord> getMin() {
        return min;
    }

    public Map<FlySightDataType, FlySightRecord> getMax() {
        return max;
    }

    public FlySightRecord getExit() {
        return exit;
    }

    public FlySightRecord getOpening() {
        return opening;
    }
}
