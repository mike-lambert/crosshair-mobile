package cyberspacelabs.ru.crosshairmobile;

import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryService;
import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryStatusListener;
import cyberspacelabs.ru.crosshairmobile.contracts.GeoIpService;
import cyberspacelabs.ru.crosshairmobile.dto.Server;
import cyberspacelabs.ru.crosshairmobile.services.GeoIpLocationService;
import cyberspacelabs.ru.crosshairmobile.services.NativeDiscoveryService;
import cyberspacelabs.ru.crosshairmobile.ui.ServerListAdapter;

public class MainActivity extends AppCompatActivity implements DiscoveryStatusListener {
    private ListView listServers;
    private ServerListAdapter serverListAdapter;
    private SwipeRefreshLayout container;
    private DiscoveryService discoveryService;
    private GeoIpService geoIpService;
    private TextView textStatus;
    private int overall;
    private int pending;
    private int filtered;
    private int discovered;
    private final AtomicLong threadCounter = new AtomicLong(0);
    private final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(MainActivity.class.getSimpleName() + "-Worker::" + threadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    });
    private ShareActionProvider shareActionProvider;
    private EditText textFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listServers = (ListView) findViewById(R.id.listServers);
        serverListAdapter = new ServerListAdapter(this);
        listServers.setAdapter(serverListAdapter);
        container = (SwipeRefreshLayout) findViewById(R.id.layoutServers);
        container.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshServersList();
            }
        });
        discoveryService = new NativeDiscoveryService();
        discoveryService.setStatusListener(this);
        geoIpService = new GeoIpLocationService();
        textStatus = (TextView) findViewById(R.id.textStatus);
        textFilter = (EditText) findViewById(R.id.textFilter);
        textFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                serverListAdapter.getFilter().filter(s);
                String filter = s.toString();
                filtered = TextUtils.isEmpty(s) ? -1 : serverListAdapter.getCount();
                updateStatus(overall, pending);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        filtered = -1;
        discovered = 0;
    }

    private void refreshServersList() {
        textStatus.setText("Querying master servers ...");
        discovered = 0;
        filtered = -1;
        container.setRefreshing(true);
        serverListAdapter.clear();
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                final List<Server> servers = discoveryService.refresh();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                refreshServersList();
                return true;
            // action with ID action_settings was selected
            case R.id.actionSettings:
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateStatus(int overallTasks, int pendingTasks) {
        overall = overallTasks;
        pending = pendingTasks;
        updateStatusText();
    }

    private void updateStatusText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int count = discovered;
                String tail = "";
                if (count == 0) {
                    tail = "No data acquired yet";
                } else {
                    tail = count + " servers discovered along";
                }
                if (filtered > -1) {
                    tail += ". Filtered: " + filtered;
                }
                if (pending > 0) {
                    textStatus.setText(pending + " of " + overall + " tasks pending. " + tail);
                } else {
                    textStatus.setText("Refresh done. " + tail);
                }

            }
        });
    }

    @Override
    public void updateData(final List<Server> arrived) {
        discovered += arrived.size();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverListAdapter.addAll(arrived);
                serverListAdapter.notifyDataSetChanged();
                resolveLocations(arrived);
            }
        });
    }

    private void resolveLocations(final List<Server> arrived) {
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                for (Server server : arrived) {
                    String ip = server.getAddress();
                    int port = ip.indexOf(":");
                    if (port > -1) {
                        ip = ip.substring(0, port).trim();
                    }
                    try {
                        server.setLocation(geoIpService.locate(ip));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void refreshDone() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                container.setRefreshing(false);
                String filter = textFilter.getText().toString();
                filtered = TextUtils.isEmpty(filter) ? -1 : serverListAdapter.getCount();
                if (!TextUtils.isEmpty(filter)) {
                    serverListAdapter.getFilter().filter(filter);
                }
                updateStatusText();
            }
        });

    }
}
