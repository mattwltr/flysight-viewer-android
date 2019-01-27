package matt.wltr.labs.flysightviewer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogMetadata;

public class LogbookAdapter extends RecyclerView.Adapter<LogbookAdapter.LogbookEntryViewHolder> {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static class LogbookEntryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.logbook_entry_label)
        TextView textView;

        public LogbookEntryViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void setLabel(String label) {
            textView.setText(label);
        }

        public View getItemView() {
            return itemView;
        }
    }

    private List<LogbookListEntry> logFilePaths;

    public LogbookAdapter(List<LogbookListEntry> logFilePaths) {
        this.logFilePaths = logFilePaths;
    }

    @NonNull
    @Override
    public LogbookAdapter.LogbookEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_logbook_entry, parent, false);
        return new LogbookEntryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LogbookEntryViewHolder logbookEntryViewHolder, int position) {

        final LogbookListEntry logbookListEntry = logFilePaths.get(position);

        FlySightLogMetadata flySightLogMetadata = logbookListEntry.getFlySightLogMetadata();

        String formattedDate =
                logbookEntryViewHolder.getItemView().getResources().getString(R.string.utc_date_format, DATE_TIME_FORMAT.format(flySightLogMetadata.getUtcDate()));
        logbookEntryViewHolder.setLabel(formattedDate);

        logbookEntryViewHolder
                .getItemView()
                .setOnClickListener(
                        view -> {
                            Intent intent = new Intent(view.getContext(), LogActivity.class);

                            Bundle bundle = new Bundle();
                            bundle.putString(LogActivity.FLY_SIGHT_LOG_URI_INTENT_KEY, logbookListEntry.getLogUri().toString());
                            intent.putExtras(bundle);

                            view.getContext().startActivity(intent);
                        });
    }

    @Override
    public int getItemCount() {
        return logFilePaths.size();
    }
}
