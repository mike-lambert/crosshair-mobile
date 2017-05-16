package cyberspacelabs.ru.crosshairmobile.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cyberspacelabs.ru.crosshairmobile.R;
import cyberspacelabs.ru.crosshairmobile.dto.Server;

/**
 * Created by mzakharov on 10.05.17.
 */
public class ServerListAdapter extends ArrayAdapter<Server> {
    private List<Server> rawData;
    private List<Server> filteredData;
    public ServerListAdapter(Context context) {
        super(context, R.layout.server_list_row);
        rawData = new ArrayList<>();
        filteredData = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return filteredData == null ? 0 : filteredData.size();
    }

    @Override
    public Server getItem(int position) {
        return filteredData == null ? null : filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void addAll(Collection<? extends Server> data){
        rawData.addAll(data);
        filteredData.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public void clear(){
        rawData.clear();
        filteredData.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.server_list_row, parent, false);
        TextView serverName = (TextView) rowView.findViewById(R.id.textServerName);
        TextView serverLocation = (TextView) rowView.findViewById(R.id.textServerLocation);
        TextView serverPing = (TextView) rowView.findViewById(R.id.textPing);
        TextView serverLoad = (TextView) rowView.findViewById(R.id.textServerLoad);
        TextView serverAddress = (TextView) rowView.findViewById(R.id.textServerAddress);
        TextView gameMode = (TextView) rowView.findViewById(R.id.textGameMode);
        TextView mapName = (TextView) rowView.findViewById(R.id.textMap);
        ImageView imageGame = (ImageView)rowView.findViewById(R.id.imageGameIcon);

        Server data = getItem(position);

        serverAddress.setText(data.getAddress());
        serverLoad.setText(data.getPlayers() + "/" + data.getSlots());
        serverLocation.setText(data.getLocation());
        serverName.setText(data.getName());
        serverPing.setText(Long.toString(data.getPing()));
        gameMode.setText(data.getMode());
        mapName.setText(data.getMap());
        imageGame.setImageResource(getGameIconResource(data));
        return rowView;
    }

    private int getGameIconResource(Server data) {
        if ("xonotic".equals(data.getGame().toLowerCase())){
            return R.drawable.xonotic;
        }

        if ("quake3".equals(data.getGame().toLowerCase())){
            return R.drawable.quake3;
        }

        if ("openarena".equals(data.getGame().toLowerCase())){
            return R.drawable.openarena;
        }

        return 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                final List<Server> list = rawData;

                final ArrayList<Server> target = new ArrayList<>();
                for (Server server : rawData) {
                    filterTextFields(server, target, filterString);
                }
                results.values = target;
                results.count = target.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData = (ArrayList<Server>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private void filterTextFields(Server server, ArrayList<Server> target, String constraint) {
        if (TextUtils.isEmpty(constraint)){
            target.add(server);
        } else {
            if (
                server.getAddress().toLowerCase().contains(constraint) ||
                    server.getGame().toLowerCase().contains(constraint) ||
                    server.getLocation().toLowerCase().contains(constraint) ||
                    server.getMap().toLowerCase().contains(constraint) ||
                    server.getMode().toLowerCase().contains(constraint) ||
                    server.getName().toLowerCase().contains(constraint)
                    ){
                target.add(server);
            }
        }
    }
}
