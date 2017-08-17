package cyberspacelabs.ru.crosshairmobile.controllers;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryController;
import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryService;
import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryStatusListener;
import cyberspacelabs.ru.crosshairmobile.contracts.GeoIpService;
import cyberspacelabs.ru.crosshairmobile.contracts.ServerBrowserPresentation;
import cyberspacelabs.ru.crosshairmobile.dto.Server;
import cyberspacelabs.ru.crosshairmobile.services.factory.DiscoveryServiceFactory;
import cyberspacelabs.ru.crosshairmobile.services.GeoIpLocationService;
import cyberspacelabs.ru.crosshairmobile.ui.ServerListAdapter;

public class ServerListController implements DiscoveryController, DiscoveryStatusListener {
    private ServerBrowserPresentation browserView;
    private Activity context;
    private ServerListAdapter serverListAdapter;
    private DiscoveryService discoveryService;
    private GeoIpService geoIpService;
    private int overall;
    private int pending;
    private int filtered;
    private int discovered;
    private final AtomicLong threadCounter = new AtomicLong(0);
    private final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            Thread t = new Thread(r);
            t.setName(ServerListController.class.getSimpleName() + "-Worker::" + threadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    });

    public ServerListController(){
        discoveryService = DiscoveryServiceFactory.getDiscoveryService();
        discoveryService.setStatusListener(this);
        geoIpService = new GeoIpLocationService();
        filtered = -1;
        discovered = 0;
    }

    @Override
    public void setPresentation(ServerBrowserPresentation presentation) {
        browserView = presentation;
        context = (Activity)browserView;
        serverListAdapter = new ServerListAdapter(context);
        presentation.getServerList().setAdapter(serverListAdapter);
    }

    @Override
    public void refresh(String filter) {
        browserView.getStatusText().setText("Querying master servers ...");
        discovered = 0;
        filtered = -1;
        browserView.setProgressState(true);
        serverListAdapter.clear();
        threadPool.submit(new Runnable() {
            @Override
            public void run() {
                final List<Server> servers = discoveryService.refresh();
            }
        });
    }

    @Override
    public void applyFilter(String filter) {
        serverListAdapter.getFilter().filter(filter);
        filtered = serverListAdapter.getCount();
        updateStatusText();
    }

    @Override
    public void updateStatus(int overallTasks, int pendingTasks) {
        overall = overallTasks;
        pending = pendingTasks;
        updateStatusText();
    }

    @Override
    public void updateData(final List<Server> arrived) {
        discovered += arrived.size();
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverListAdapter.addAll(arrived);
                serverListAdapter.notifyDataSetChanged();
                resolveLocations(arrived);
            }
        });
        updateStatusText();
    }

    @Override
    public void refreshDone() {
        browserView.setProgressState(false);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverListAdapter.getFilter().filter(browserView.getFilterField().getText().toString());
                filtered = serverListAdapter.getCount();
                updateStatusText();
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
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateStatusText(){
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                browserView.getStatusText().setText(createStatusText());
            }
        });
    }

    private String createStatusText() {
        StringBuilder result = new StringBuilder();
        if (pending == 0){
            result.append("Refresh done.");
        } else {
            result.append("Pending ")
                    .append(pending).append(" of ").append(overall)
                    .append(" tasks.");
        }
        if (discovered == 0){
            result.append(" No servers found.");
        } else {
            result.append(" ").append(discovered).append(" servers found.");
        }
        if (filtered > 0){
            result.append(" ").append(filtered).append(" servers filtered.");
        }
        return result.toString();
    }
}
