package cyberspacelabs.ru.crosshairmobile.contracts;

/**
 * Created by mike on 16.05.17.
 */
public interface DiscoveryController {
    void setPresentation(ServerBrowserPresentation presentation);
    void refresh(String filter);
    void applyFilter(String filter);
}
