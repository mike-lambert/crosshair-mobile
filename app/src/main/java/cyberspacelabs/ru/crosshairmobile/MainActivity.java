package cyberspacelabs.ru.crosshairmobile;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import cyberspacelabs.ru.crosshairmobile.contracts.ServerBrowserPresentation;
import cyberspacelabs.ru.crosshairmobile.controllers.ServerListController;

public class MainActivity extends AppCompatActivity implements ServerBrowserPresentation {
    private ListView listServers;

    private SwipeRefreshLayout container;
    private TextView textStatus;
    private ShareActionProvider shareActionProvider;
    private EditText textFilter;
    private ImageView iconSearch;
    private ServerListController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controller = ((ApplicationState)getApplication()).getServerListController();

        listServers = (ListView) findViewById(R.id.listServers);
        container = (SwipeRefreshLayout) findViewById(R.id.layoutServers);
        container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                controller.refresh(textFilter.getText().toString());
            }
        });
        textStatus = (TextView) findViewById(R.id.textStatus);
        textFilter = (EditText) findViewById(R.id.textFilter);
        textFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                controller.applyFilter(s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        iconSearch = (ImageView) findViewById(R.id.iconSearch);
        iconSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textFilter.requestFocusFromTouch();
                InputMethodManager lManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                lManager.showSoftInput(textFilter, 0);
            }
        });
        controller.setPresentation(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.actionShareApp);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareIntent(createShareIntent());
        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Recommend Crosshair Mobile to friends");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Download Crosshair Mobile - Arena Shooter Browser \r\n for Android at http://openarena.cyberspacelabs.ru");
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.actionRefresh:
                controller.refresh(textFilter.getText().toString());
                return true;
            // action with ID action_settings was selected
            case R.id.actionSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public EditText getFilterField() {
        return textFilter;
    }

    @Override
    public ListView getServerList() {
        return listServers;
    }

    @Override
    public TextView getStatusText() {
        return textStatus;
    }

    @Override
    public void setProgressState(final boolean refreshing) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                container.setRefreshing(refreshing);
            }
        });
    }
}
