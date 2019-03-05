package matt.wltr.labs.flysightviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;

import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogMetadata;

public class LogbookAdapter extends ArrayAdapter<LogbookListEntry> {

    private static final DateTimeFormatter GROUP_TITLE_FORMAT = DateTimeFormatter.ofPattern("MMMM YYYY");
    private static final DateTimeFormatter TITLE_FORMAT = DateTimeFormatter.ofPattern("MM/dd HH:mm");

    private List<LogbookListEntry> logFilePaths;

    public LogbookAdapter(Context context, List<LogbookListEntry> logFilePaths) {
        super(context, -1, logFilePaths);
        this.logFilePaths = logFilePaths;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_logbook_entry, parent, false);

        TextView groupTitleView = rootView.findViewById(R.id.logbook_entry_group_title);
        LinearLayout logbookEntryView = rootView.findViewById(R.id.logbook_entry);
        TextView titleView = rootView.findViewById(R.id.logbook_entry_title);
        TextView descriptionView = rootView.findViewById(R.id.logbook_entry_description);

        final FlySightLogMetadata flySightLogMetadata = logFilePaths.get(position).getFlySightLogMetadata();
        if (isFirstEntryOfMonth(position)) {
            groupTitleView.setText(GROUP_TITLE_FORMAT.format(flySightLogMetadata.getZonedDateTime()));
            groupTitleView.setVisibility(View.VISIBLE);
        }
        String title = TITLE_FORMAT.format(flySightLogMetadata.getZonedDateTime());
        if (flySightLogMetadata.getZoneId() == null) {
            title += " UTC";
        }
        titleView.setText(title);
        if (!flySightLogMetadata.isOpened()) {
            titleView.setTypeface(null, Typeface.BOLD);
        }
        if (flySightLogMetadata.getTags() != null && !flySightLogMetadata.getTags().isEmpty()) {
            descriptionView.setText(TextUtils.join(" · ", flySightLogMetadata.getTags()));
            descriptionView.setVisibility(View.VISIBLE);
        }

        logbookEntryView.setOnClickListener(
                view -> {
                    Intent intent = new Intent(parent.getContext(), LogActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(LogActivity.FLY_SIGHT_LOG_METADATA_INTENT_KEY, flySightLogMetadata);
                    intent.putExtras(bundle);
                    parent.getContext().startActivity(intent);
                });

        return rootView;
    }

    private boolean isFirstEntryOfMonth(int position) {
        if (position == 0) {
            return true;
        }
        int previousPosition = position - 1;
        FlySightLogMetadata previousFlySightLogMetadata = logFilePaths.get(previousPosition).getFlySightLogMetadata();
        FlySightLogMetadata flySightLogMetadata = logFilePaths.get(position).getFlySightLogMetadata();
        return !previousFlySightLogMetadata.getZonedDateTime().getMonth().equals(flySightLogMetadata.getZonedDateTime().getMonth());
    }

    //    static class LogbookEntryViewHolder extends RecyclerView.ViewHolder {
    //
    //        @BindView(R.id.logbook_entry)
    //        LinearLayout logbookEntryView;
    //
    //        @BindView(R.id.logbook_entry_group_title)
    //        TextView groupTitleView;
    //
    //        @BindView(R.id.logbook_entry_title)
    //        TextView titleView;
    //
    //        @BindView(R.id.logbook_entry_description)
    //        TextView descriptionView;
    //
    //        LogbookEntryViewHolder(View view) {
    //            super(view);
    //            ButterKnife.bind(this, view);
    //        }
    //
    //        void setGroupTitle(String groupTitle) {
    //            groupTitleView.setText(groupTitle);
    //            groupTitleView.setVisibility(View.VISIBLE);
    //        }
    //
    //        void setTitle(String title) {
    //            titleView.setText(title);
    //        }
    //
    //        void setDescription(String description) {
    //            if (description != null && !description.trim().isEmpty()) {
    //                descriptionView.setText(description);
    //                descriptionView.setVisibility(View.VISIBLE);
    //            } else {
    //                descriptionView.setVisibility(View.GONE);
    //            }
    //        }
    //
    //        LinearLayout getLogbookEntryView() {
    //            return logbookEntryView;
    //        }
    //
    //        View getItemView() {
    //            return itemView;
    //        }
    //    }
    //
    //    private List<LogbookListEntry> logFilePaths;
    //
    //    LogbookAdapter(List<LogbookListEntry> logFilePaths) {
    //        this.logFilePaths = logFilePaths;
    //    }
    //
    //    @NonNull
    //    @Override
    //    public LogbookAdapter.LogbookEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    //        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_logbook_entry, parent, false);
    //        return new LogbookEntryViewHolder(itemView);
    //    }
    //
    //    @Override
    //    public void onBindViewHolder(@NonNull LogbookEntryViewHolder logbookEntryViewHolder, int position) {
    //
    //        final LogbookListEntry logbookListEntry = logFilePaths.get(position);
    //
    //        FlySightLogMetadata flySightLogMetadata = logbookListEntry.getFlySightLogMetadata();
    //
    //        if (isFirstEntryOfMonth(position)) {
    //            logbookEntryViewHolder.setGroupTitle(flySightLogMetadata.getZonedDateTime().getMonth().getDisplayName(TextStyle.FULL, Locale.US));
    //        }
    //        logbookEntryViewHolder.setTitle(flySightLogMetadata.getFormattedDateTime());
    //        logbookEntryViewHolder.setDescription(flySightLogMetadata.getDescription());
    //
    //        logbookEntryViewHolder.getLogbookEntryView().setOnClickListener(
    //                view -> {
    //                    Intent intent = new Intent(view.getContext(), LogActivity.class);
    //                    Bundle bundle = new Bundle();
    //                    bundle.putSerializable(LogActivity.FLY_SIGHT_LOG_METADATA_INTENT_KEY, logbookListEntry.getFlySightLogMetadata());
    //                    intent.putExtras(bundle);
    //                    view.getContext().startActivity(intent);
    //                });
    //    }
    //
    //    private boolean isFirstEntryOfMonth(int position) {
    //        if (position == 0) {
    //            return true;
    //        }
    //        int previousPosition = position - 1;
    //        FlySightLogMetadata previousFlySightLogMetadata = logFilePaths.get(previousPosition).getFlySightLogMetadata();
    //        FlySightLogMetadata flySightLogMetadata = logFilePaths.get(position).getFlySightLogMetadata();
    //        return !previousFlySightLogMetadata.getZonedDateTime().getMonth().equals(flySightLogMetadata.getZonedDateTime().getMonth());
    //    }
    //
    //    @Override
    //    public int getItemCount() {
    //        return logFilePaths.size();
    //    }
}
