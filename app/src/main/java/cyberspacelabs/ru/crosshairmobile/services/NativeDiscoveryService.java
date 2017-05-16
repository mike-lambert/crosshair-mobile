package cyberspacelabs.ru.crosshairmobile.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryService;
import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryStatusListener;
import cyberspacelabs.ru.crosshairmobile.darkplaces.GameBrowser;
import cyberspacelabs.ru.crosshairmobile.darkplaces.GameServer;
import cyberspacelabs.ru.crosshairmobile.dto.Server;

/**
 * Created by mzakharov on 10.05.17.
 */
public class NativeDiscoveryService implements DiscoveryService {
    private Map<String, String> queries;
    private List<GameBrowser> browsers;
    private final AtomicLong threadCounter = new AtomicLong(0);
    private final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(NativeDiscoveryService.class.getSimpleName() + "-Worker::" + threadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    });
    private final ExecutorCompletionService<List<Server>> completionService = new ExecutorCompletionService<>(threadPool);
    private DiscoveryStatusListener statusListener;
    public NativeDiscoveryService(){
        browsers = new ArrayList<>();
        queries = new HashMap<>();
        queries.put("Xonotic", GameBrowser.QUERY_XONOTIC_DEFAULT);
        queries.put("OpenArena", GameBrowser.QUERY_OPENARENA_DEFAULT);
        queries.put("Quake3", GameBrowser.QUERY_QUAKE_3_ARENA_DEFAULT);

        addBrowsers(GameBrowser.DPMASTER);
        addBrowsers("master.ioquake3.org:27950");
        addBrowsers("master.quake3arena.com:27950");
    }

    private void addBrowsers(String endpoint){
        for(String type : queries.keySet()){
            browsers.add(new GameBrowser(endpoint, queries.get(type)).setGame(type));
        }
    }

    @Override
    public List<Server> refresh() {
        List<Server> result = new ArrayList<>();
        int i = 0;
        for(GameBrowser browser: browsers){
            i++;
            completionService.submit(createGameBrowserTask(browser));
            updateStatus(i, browsers.size());
        }

        do {
            try {
                Future<List<Server>> completed = completionService.poll(2000, TimeUnit.MILLISECONDS);
                if (completed != null){
                    try {
                        List<Server> data = completed.get();
                        i--;
                        updateStatus(i, browsers.size());
                        if (data != null && !data.isEmpty()){
                            updateData(data);
                            result.addAll(data);
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while (true);
        signalRefreshDone();
        return result;
    }

    @Override
    public void setStatusListener(DiscoveryStatusListener listener) {
        statusListener = listener;
    }

    @Override
    public DiscoveryStatusListener getStatusListener() {
        return statusListener;
    }

    private void signalRefreshDone() {
        if (statusListener != null){
            statusListener.refreshDone();
        }
    }

    private void updateData(List<Server> data) {
        if (statusListener != null){
            statusListener.updateData(data);
        }
    }

    private void updateStatus(int pending, int size) {
        if (statusListener != null){
            statusListener.updateStatus(size, pending);
        }
    }

    private List<Server> transformGameServerToServer(Set<GameServer> source) {
        List<Server> result = new ArrayList<>();
        for(GameServer entry : source){
            result.add(new Server()
                            .setAddress(entry.getAddress())
                            .setPing(entry.getRequestDuration())
                            .setName(sanitizeQuakeColors(entry.getDisplayName()))
                            .setLocation("<Location unknown>")
                            .setPlayers(entry.getPlayersPresent())
                            .setSlots(entry.getSlotsAvailable())
                            .setMode(entry.getGameType())
                            .setMap(entry.getMap())
                            .setGame(entry.getGame())
            );
        }
        return result;
    }

    private Callable<List<Server>> createGameBrowserTask(final GameBrowser subject){
        return new Callable<List<Server>>() {
            @Override
            public List<Server> call() throws Exception {
                return transformGameServerToServer(subject.refresh());
            }
        };
    }

    private String sanitizeQuakeColors(String source){
        StringBuilder result = new StringBuilder();
        char[] chars = source.toCharArray();
        for(int i = 0; i < chars.length; i++){
            char current = chars[i];
            if (current != '^'){ result.append(current); continue;}
            if (i == chars.length -1){result.append(current); continue;}
            if (Character.isDigit(chars[i + 1])){i = i+1; continue;}
            result.append(current);
        }
        return result.toString();
    }
}
