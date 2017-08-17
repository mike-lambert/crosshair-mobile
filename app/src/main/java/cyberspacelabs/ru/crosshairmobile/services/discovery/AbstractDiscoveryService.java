package cyberspacelabs.ru.crosshairmobile.services.discovery;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import cyberspacelabs.ru.crosshairmobile.ApplicationState;
import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryService;
import cyberspacelabs.ru.crosshairmobile.contracts.DiscoveryStatusListener;
import cyberspacelabs.ru.crosshairmobile.darkplaces.GameServer;
import cyberspacelabs.ru.crosshairmobile.dto.Server;

/**
 * Created by mike on 17.08.17.
 */
public abstract class AbstractDiscoveryService implements DiscoveryService {
    protected final AtomicLong threadCounter = new AtomicLong(0);
    protected final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(DiscoveryService.class.getSimpleName() + "-Worker::" + threadCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    });
    protected final ExecutorCompletionService<List<Server>> completionService = new ExecutorCompletionService<>(threadPool);

    protected ApplicationState app;
    protected DiscoveryStatusListener statusListener;
    protected AbstractDiscoveryService(Context context){
        app = ApplicationState.getInstance(context);
    }

    @Override
    public void setStatusListener(DiscoveryStatusListener listener) {
        statusListener = listener;
    }

    @Override
    public DiscoveryStatusListener getStatusListener() {
        return statusListener;
    }

    protected void signalRefreshDone() {
        if (statusListener != null){
            statusListener.refreshDone();
        }
    }

    protected void updateData(List<Server> data) {
        if (statusListener != null){
            statusListener.updateData(data);
        }
    }

    protected void updateStatus(int pending, int size) {
        if (statusListener != null){
            statusListener.updateStatus(size, pending);
        }
    }

    protected List<Server> transformGameServerToServer(Set<GameServer> source) {
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

    protected List<Server> transformGameServerToServer(List<GameServer> source) {
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

    protected static String sanitizeQuakeColors(String source){
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

    @Override
    public List<Server> refresh() {
        List<Server> result = new ArrayList<>();
        int i = submitTasks();
        do {
            try {
                Future<List<Server>> completed = completionService.poll(2000, TimeUnit.MILLISECONDS);
                if (completed != null){
                    try {
                        List<Server> data = completed.get();
                        i--;
                        updateStatus(i, getTotalTasksCount());
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

    protected abstract int submitTasks();

    protected abstract int getTotalTasksCount();
}
