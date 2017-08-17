package cyberspacelabs.ru.crosshairmobile.services.factory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cyberspacelabs.ru.crosshairmobile.darkplaces.GameBrowser;

/**
 * Created by mike on 17.08.17.
 */
public class GameBrowserFactory {
    private static List<GameBrowser> browsers;
    public static List<GameBrowser> getGameBrowsers() {
        if (browsers == null){
            CopyOnWriteArrayList<GameBrowser> result = new CopyOnWriteArrayList<>();
            for(String master : getMasters()){
                for(String query : getQueries()){
                    result.add(new GameBrowser(master, query));
                }
            }
            browsers = result;
        }
        return browsers;
    }

    public static List<String> getMasters() {
        return Collections.EMPTY_LIST;
    }

    public static List<String> getQueries() {
        return Collections.EMPTY_LIST;
    }
}
