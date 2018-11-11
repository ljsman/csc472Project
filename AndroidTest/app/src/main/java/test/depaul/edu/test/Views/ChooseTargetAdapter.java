package test.depaul.edu.test.Views;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.ResourceBundle;

import test.depaul.edu.test.Models.Player;
import test.depaul.edu.test.R;

public class ChooseTargetAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Player> players;

    public ChooseTargetAdapter(Context ctx, ArrayList<Player> players) {
        super();

        this.context = ctx;
        this.players = players;
    }

    public int getCount() {
        return players.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout;
        if (convertView == null) {
            LayoutInflater vi = LayoutInflater.from(context);
            layout = (LinearLayout)vi.inflate(R.layout.game_playing_player_view, null);
        } else {
            layout = (LinearLayout) convertView;
        }
        final TextView textPosition = layout.findViewById(R.id.textPosition);
        textPosition.setText("P"+players.get(position).position+" "+players.get(position).name);
        return layout;
    }
}
