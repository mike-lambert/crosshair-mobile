package cyberspacelabs.ru.crosshairmobile.contracts;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by mike on 16.05.17.
 */
public interface ServerBrowserPresentation {
    EditText getFilterField();
    ListView getServerList();
    TextView getStatusText();
    void setProgressState(boolean refreshing);
}
