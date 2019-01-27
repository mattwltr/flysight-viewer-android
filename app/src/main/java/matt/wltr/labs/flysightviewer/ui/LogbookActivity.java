package matt.wltr.labs.flysightviewer.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import labs.wltr.matt.flysightviewer.R;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogMetadata;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogSettings;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogbookImportObserver;
import matt.wltr.labs.flysightviewer.flysight.FlySightLogbookImportTask;

public class LogbookActivity extends AppCompatActivity {

    private static final int OPEN_FOLDER_REQUEST_CODE = 2;

    @BindView(R.id.logbook)
    RecyclerView logbookView;

    private final List<LogbookListEntry> logbook = new ArrayList<>();

    private final LogbookAdapter logbookAdapter = new LogbookAdapter(logbook);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        AndroidThreeTen.init(this);

        setContentView(R.layout.activity_logbook);
        ButterKnife.bind(this);

        logbookView.setHasFixedSize(true);
        logbookView.setLayoutManager(new LinearLayoutManager(this));
        logbookView.setAdapter(logbookAdapter);
        logbookView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        refreshLogbook();
    }

    private void refreshLogbook() {
        logbook.clear();
        File directory = FlySightLogSettings.getLogDirectory(this);
        for (File file : directory.listFiles()) {
            if (!file.getName().endsWith(FlySightLogSettings.METADATA_FILE_EXTENSION)) {
                File metadataFile = new File(file.getAbsolutePath() + FlySightLogSettings.METADATA_FILE_EXTENSION);
                if (metadataFile.exists()) {
                    FlySightLogMetadata flySightLogMetadata = FlySightLogMetadata.read(metadataFile);
                    if (flySightLogMetadata != null) {
                        logbook.add(new LogbookListEntry(Uri.fromFile(file), Uri.fromFile(metadataFile), flySightLogMetadata));
                    }
                }
            }
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

    @Deprecated
    private void onFileChosen(@NonNull Uri uri) {

        Intent intent = new Intent(this, LogActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(LogActivity.FLY_SIGHT_LOG_URI_INTENT_KEY, uri.toString());
        intent.putExtras(bundle);

        startActivity(intent);
    }

    private void onFolderChosen(@NonNull Uri uri) {

        FlySightLogbookImportTask flySightLogbookImportTask = new FlySightLogbookImportTask(this);
        flySightLogbookImportTask.setFlySightLogbookImportObserver(
                new FlySightLogbookImportObserver() {
                    @Override
                    public void onFlySightLogbookImported() {
                        refreshLogbook();
                        Toast.makeText(getApplicationContext(), "finished sync", Toast.LENGTH_LONG).show();
                    }
                });
        flySightLogbookImportTask.execute(uri);
    }
}
