package matt.wltr.labs.flysightviewer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogMetadata;

public class LogbookAdapter extends RecyclerView.Adapter<LogbookAdapter.LogbookEntryViewHolder> {

    static class LogbookEntryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.logbook_entry_title)
        TextView titleView;

        @BindView(R.id.logbook_entry_description)
        TextView descriptionView;

        LogbookEntryViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void setTitle(String title) {
            titleView.setText(title);
        }

        void setDescription(String description) {
            if (description != null && !description.trim().isEmpty()) {
                descriptionView.setText(description);
                descriptionView.setVisibility(View.VISIBLE);
            } else {
                descriptionView.setVisibility(View.GONE);
            }
        }

        View getItemView() {
            return itemView;
        }
    }

    private List<LogbookListEntry> logFilePaths;

    LogbookAdapter(List<LogbookListEntry> logFilePaths) {
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

        logbookEntryViewHolder.setTitle(flySightLogMetadata.getFormattedDateTime());
        logbookEntryViewHolder.setDescription(flySightLogMetadata.getDescription());
        logbookEntryViewHolder
                .getItemView()
                .setOnClickListener(
                        view -> {
                            Intent intent = new Intent(view.getContext(), LogActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(LogActivity.FLY_SIGHT_LOG_METADATA_INTENT_KEY, logbookListEntry.getFlySightLogMetadata());
                            intent.putExtras(bundle);
                            view.getContext().startActivity(intent);
                        });
    }

    @Override
    public int getItemCount() {
        return logFilePaths.size();
    }
}
