package matt.wltr.labs.flysightviewer.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogMetadata;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogRepository;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogbookImportTask;

public class LogbookActivity extends AppCompatActivity {

    private static final int OPEN_FOLDER_REQUEST_CODE = 2;

    @BindView(R.id.logbook)
    ListView logbookView;

    private final List<LogbookListEntry> logbook = new ArrayList<>();

    private LogbookAdapter logbookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_logbook);
        ButterKnife.bind(this);

        logbookAdapter = new LogbookAdapter(this, logbook);
        logbookView.setAdapter(logbookAdapter);

        refreshLogbook();
    }

    private void refreshLogbook() {
        logbook.clear();
        for (FlySightLogMetadata flySightLogMetadata : FlySightLogRepository.getAllFlySightLogMetadata(this)) {
            logbook.add(new LogbookListEntry(flySightLogMetadata));
        }
        Collections.sort(logbook, (first, second) -> second.getFlySightLogMetadata().getUtcDate().compareTo(first.getFlySightLogMetadata().getUtcDate()));
        logbookAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.main_menu_choose_folder:
                importFlySightLogbook();
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void importFlySightLogbook() {
        Intent openFolderIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        openFolderIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(openFolderIntent, OPEN_FOLDER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (Activity.RESULT_OK == resultCode) {
            if (resultData == null || resultData.getData() == null) {
                return;
            }
            switch (requestCode) {
                case OPEN_FOLDER_REQUEST_CODE:
                    onFolderChosen(resultData.getData());
                    break;
            }
        }
    }

    private void onFolderChosen(@NonNull Uri uri) {
        FlySightLogbookImportTask flySightLogbookImportTask = new FlySightLogbookImportTask(this);
        flySightLogbookImportTask.setFlySightLogbookImportObserver(
                () -> {
                    refreshLogbook();
                    Toast.makeText(getApplicationContext(), "Import complete", Toast.LENGTH_LONG).show();
                });
        flySightLogbookImportTask.execute(uri);
    }

    @Override
    protected void onResume() {
        refreshLogbook();
        super.onResume();
    }
}
