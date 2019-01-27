package matt.wltr.labs.flysightviewer.flysight;

import android.support.annotation.NonNull;
import android.util.Log;

import org.threeten.bp.OffsetDateTime;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import matt.wltr.labs.flysightviewer.ui.ProgressListener;

public class FlySightLogParser {

    private static final List<FlySightDataType> MANDATORY_CSV_COLUMNS =
            Arrays.asList(
                    FlySightDataType.DATE,
                    FlySightDataType.LATITUDE,
                    FlySightDataType.LONGITUDE,
                    FlySightDataType.ELEVATION,
                    FlySightDataType.VELOCITY_NORTH,
                    FlySightDataType.VELOCITY_EAST,
                    FlySightDataType.VELOCITY_DOWN);

    public static FlySightLog parse(@NonNull FileInputStream inputStream, @NonNull ParseMode parseMode) {
        return parse(inputStream, parseMode, null);
    }

    public static FlySightLog parse(@NonNull FileInputStream inputStream, @NonNull ParseMode parseMode, ProgressListener progressListener) {

        Map<OffsetDateTime, FlySightRecord> records = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            long fileSize = inputStream.getChannel().size();
            long bytesRead = 0;
            int percentageProgress = 0;
            int lineBreakLength = 2;

            String headline = reader.readLine();
            bytesRead += headline.getBytes(StandardCharsets.UTF_8).length + lineBreakLength;

            Map<FlySightDataType, Integer> columnMapping = getColumnMapping(headline);
            if (!columnMapping.keySet().containsAll(MANDATORY_CSV_COLUMNS)) {
                // mandatory column(s) missing
                return null;
            }

            String headlineDescription = reader.readLine(); // skip second headline with measurements
            bytesRead += headlineDescription.getBytes(StandardCharsets.UTF_8).length + lineBreakLength;

            FlySightRecord lastRecord = null;

            String csvLine;
            while ((csvLine = reader.readLine()) != null) {

                bytesRead += csvLine.getBytes(StandardCharsets.UTF_8).length + lineBreakLength;

                String[] columns = csvLine.split(",");

                OffsetDateTime date;
                try {
                    date = OffsetDateTime.parse(columns[columnMapping.get(FlySightDataType.DATE)]);
                } catch (Exception e) {
                    // invalid date format
                    return null;
                }

                FlySightRecord flySightRecord = new FlySightRecord();
                flySightRecord.setDate(date);
                flySightRecord.setLat(Double.parseDouble(columns[columnMapping.get(FlySightDataType.LATITUDE)]));
                flySightRecord.setLng(Double.parseDouble(columns[columnMapping.get(FlySightDataType.LONGITUDE)]));
                flySightRecord.setElevation(Double.parseDouble(columns[columnMapping.get(FlySightDataType.ELEVATION)]));
                flySightRecord.setVelocityNorth(Double.parseDouble(columns[columnMapping.get(FlySightDataType.VELOCITY_NORTH)]));
                flySightRecord.setVelocityEast(Double.parseDouble(columns[columnMapping.get(FlySightDataType.VELOCITY_EAST)]));
                flySightRecord.setVelocityDown(Double.parseDouble(columns[columnMapping.get(FlySightDataType.VELOCITY_DOWN)]));

                if (columnMapping.containsKey(FlySightDataType.HORIZONTAL_ACCURACY)) {
                    flySightRecord.setHorizontalAccuracy(Double.parseDouble(columns[columnMapping.get(FlySightDataType.HORIZONTAL_ACCURACY)]));
                }
                if (columnMapping.containsKey(FlySightDataType.VERTICAL_ACCURACY)) {
                    flySightRecord.setVerticalAccuracy(Double.parseDouble(columns[columnMapping.get(FlySightDataType.VERTICAL_ACCURACY)]));
                }
                if (columnMapping.containsKey(FlySightDataType.SPEED_ACCURACY)) {
                    flySightRecord.setSpeedAccuracy(Double.parseDouble(columns[columnMapping.get(FlySightDataType.SPEED_ACCURACY)]));
                }
                if (columnMapping.containsKey(FlySightDataType.HEADING)) {
                    flySightRecord.setHeading(Double.parseDouble(columns[columnMapping.get(FlySightDataType.HEADING)]));
                }
                if (columnMapping.containsKey(FlySightDataType.HEADING_ACCURACY)) {
                    flySightRecord.setHeadingAccuracy(Double.parseDouble(columns[columnMapping.get(FlySightDataType.HEADING_ACCURACY)]));
                }
                if (columnMapping.containsKey(FlySightDataType.GPS_FIX)) {
                    flySightRecord.setGpsFix(Integer.parseInt(columns[columnMapping.get(FlySightDataType.GPS_FIX)]));
                }
                if (columnMapping.containsKey(FlySightDataType.NUMBER_OF_SATELLITES)) {
                    flySightRecord.setNumberOfSatellites(Integer.parseInt(columns[columnMapping.get(FlySightDataType.NUMBER_OF_SATELLITES)]));
                }

                if (lastRecord == null) {
                    flySightRecord.setDistance(0D);
                } else {
                    flySightRecord.setDistance(
                            lastRecord.getDistance() + calculateDistance(lastRecord.getLat(), lastRecord.getLng(), flySightRecord.getLat(), flySightRecord.getLng()));
                }

                flySightRecord.calculateAdditionalValues();

                lastRecord = flySightRecord;

                records.put(date, flySightRecord);

                if (parseMode.equals(ParseMode.FIRST_DATA_LINE) && records.size() == 2) {
                    break;
                }

                int newPercentageProgress = (int) ((bytesRead * 100) / fileSize);
                if (newPercentageProgress != percentageProgress) {
                    percentageProgress = newPercentageProgress;
                    if (progressListener != null) {
                        progressListener.onProgress(percentageProgress);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return new FlySightLog(records);
    }

    private static Map<FlySightDataType, Integer> getColumnMapping(String headline) {

        Map<FlySightDataType, Integer> columnMapping = new HashMap<>();

        String[] columns = headline.split(",");
        for (int i = 0; i < columns.length; i++) {
            String header = columns[i];
            switch (header) {
                case "time":
                    columnMapping.put(FlySightDataType.DATE, i);
                    break;
                case "lat":
                    columnMapping.put(FlySightDataType.LATITUDE, i);
                    break;
                case "lon":
                    columnMapping.put(FlySightDataType.LONGITUDE, i);
                    break;
                case "hMSL":
                    columnMapping.put(FlySightDataType.ELEVATION, i);
                    break;
                case "velN":
                    columnMapping.put(FlySightDataType.VELOCITY_NORTH, i);
                    break;
                case "velE":
                    columnMapping.put(FlySightDataType.VELOCITY_EAST, i);
                    break;
                case "velD":
                    columnMapping.put(FlySightDataType.VELOCITY_DOWN, i);
                    break;
                case "hAcc":
                    columnMapping.put(FlySightDataType.HORIZONTAL_ACCURACY, i);
                    break;
                case "vAcc":
                    columnMapping.put(FlySightDataType.VERTICAL_ACCURACY, i);
                    break;
                case "sAcc":
                    columnMapping.put(FlySightDataType.SPEED_ACCURACY, i);
                    break;
                case "heading":
                    columnMapping.put(FlySightDataType.HEADING, i);
                    break;
                case "cAcc":
                    columnMapping.put(FlySightDataType.HEADING_ACCURACY, i);
                    break;
                case "gpsFix":
                    columnMapping.put(FlySightDataType.GPS_FIX, i);
                    break;
                case "numSV":
                    columnMapping.put(FlySightDataType.NUMBER_OF_SATELLITES, i);
                    break;
                default:
                    // unknown type -> just ignore
            }
        }
        return columnMapping;
    }

    private static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double latDifference = Math.toRadians(lat2 - lat1);
        double lngDifference = Math.toRadians(lng2 - lng1);
        double a =
                Math.sin(latDifference / 2) * Math.sin(latDifference / 2)
                        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lngDifference / 2) * Math.sin(lngDifference / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return SphericalMercator.EARTH_RADIUS_IN_METER * c;
    }
}
