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
import matt.wltr.labs.flysightviewer.flysight.FlySightLogPreview;

public class LogbookAdapter extends RecyclerView.Adapter<LogbookAdapter.LogbookEntryViewHolder> {

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

    private List<FlySightLogPreview> logFilePaths;

    public LogbookAdapter(List<FlySightLogPreview> logFilePaths) {
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

        final FlySightLogPreview flySightLogPreview = logFilePaths.get(position);

        logbookEntryViewHolder.setLabel(flySightLogPreview.getName());

        logbookEntryViewHolder
                .getItemView()
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                Intent intent = new Intent(view.getContext(), LogActivity.class);

                                Bundle bundle = new Bundle();
                                bundle.putString(LogActivity.FLY_SIGHT_LOG_URI_INTENT_KEY, flySightLogPreview.getUri().toString());
                                intent.putExtras(bundle);

                                view.getContext().startActivity(intent);
                            }
                        });
    }

    @Override
    public int getItemCount() {
        return logFilePaths.size();
    }
}
