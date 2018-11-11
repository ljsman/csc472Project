package test.depaul.edu.test.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import test.depaul.edu.test.GameClient;
import test.depaul.edu.test.MainActivity;
import test.depaul.edu.test.Message;
import test.depaul.edu.test.Models.Player;
import test.depaul.edu.test.R;
import test.depaul.edu.test.ServerInterface;

public class GamePlayingView extends LinearLayout {
    private MainActivity activity = null;
    private int gameId;
    private int myPosition;
    private ArrayList<Player> playerList = new ArrayList<>();
    private GamePlayerAdapter adapter;

    public GamePlayingView(Context context) {
        super(context);
    }
    public GamePlayingView(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }
    public GamePlayingView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(final MainActivity act, Message msg) {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        activity = act;

        // initialize data
        try {
            gameId = msg.jsonObj.getJSONObject("config").getInt("game_id");
            myPosition = msg.jsonObj.getInt("position");
            updatePlayerList(msg.jsonObj.getJSONArray("players"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        TextView textGameId = findViewById(R.id.textGameId);
        textGameId.setText("Game ID: "+gameId);

        GridView gridview = findViewById(R.id.playerGrid);
        gridview.setClickable(false);
        adapter = new GamePlayerAdapter(getContext(), playerList);
        gridview.setAdapter(adapter);

        final TextView textState = findViewById(R.id.textState);
        final TextView textRole = findViewById(R.id.textRole);

        final Button btnStart= findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Message msg = new Message(ServerInterface.RequestType.StartGame);
                GameClient.SendMessage(msg);
            }
        });

        // listen to server messages
        GameClient.AddListener(ServerInterface.ResponseType.UpdatePlayersInformation, new GameClient.OnMessageListener() {
            @Override
            public void onReceivedMessage(Message msg) {
                try {
                    updatePlayerList(msg.jsonObj.getJSONArray("players"));
                    adapter.notifyDataSetChanged();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        GameClient.AddListener(ServerInterface.ResponseType.GameStart, new GameClient.OnMessageListener() {
            @Override
            public void onReceivedMessage(Message msg) {
                btnStart.setVisibility(INVISIBLE);
                textState.setText("Game Start");
                try {
                    updateRoleInformation(msg.jsonObj.getJSONArray("roles"));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                textRole.setText(Player.getRoleString(playerList.get(myPosition).role));
            }
        });

        GameClient.AddListener(ServerInterface.ResponseType.GameOver, new GameClient.OnMessageListener() {
            @Override
            public void onReceivedMessage(Message msg) {
                int winner = -1;
                try {
                    winner = msg.jsonObj.getInt("winner");
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                if(winner == 0) {
                    textState.setText("Werewolf win!!!!");
                }
                else {
                    textState.setText("Human win!!!");
                }
            }
        });

        GameClient.AddListener(ServerInterface.ResponseType.WerewolfTurn, new GameClient.OnMessageListener() {
            @Override
            public void onReceivedMessage(Message msg) {
                // choose someone to kill
                int leaderIdx = -1;
                try {
                    leaderIdx = msg.jsonObj.getInt("leader");
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                textState.setText("Waiting for werewolf leader's decision");
                if(myPosition == leaderIdx) {
                    // popup
                    LayoutInflater vi = activity.getLayoutInflater();
                    final ChooseTargetView chooseTargetView = (ChooseTargetView)vi.inflate(R.layout.choose_target_view, null);
                    chooseTargetView.initialize(getAlivePlayers());

                    Button btnChoose = chooseTargetView.findViewById(R.id.btnChoose);
                    btnChoose.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            activity.popView();

                            Message msg = new Message(ServerInterface.RequestType.WerewolfChooseToKill);
                            msg.addParam("target", chooseTargetView.getSelectedPosition());
                            GameClient.SendMessage(msg);
                        }
                    });

                    activity.pushView(chooseTargetView);
                }
            }
        });

        GameClient.AddListener(ServerInterface.ResponseType.SeerTurn, new GameClient.OnMessageListener() {
            @Override
            public void onReceivedMessage(Message msg) {

            }
        });

        GameClient.AddListener(ServerInterface.ResponseType.DiscussionTurn, new GameClient.OnMessageListener() {
            @Override
            public void onReceivedMessage(Message msg) {
                // update players' states
                int killedIdx = -1;
                try {
                    killedIdx = updateStatesInformation(msg.jsonObj.getJSONArray("playersStatus"));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                activity.showToast("Player "+killedIdx+" was killed", 3000);

                // choose someone to kill
                int leaderIdx = -1;
                try {
                    leaderIdx = msg.jsonObj.getInt("leader");
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                textState.setText("Waiting for discussion result");
                if(myPosition == leaderIdx) {
                    // popup
                    LayoutInflater vi = activity.getLayoutInflater();
                    final ChooseTargetView chooseTargetView = (ChooseTargetView)vi.inflate(R.layout.choose_target_view, null);
                    chooseTargetView.initialize(getAlivePlayers());

                    Button btnChoose = chooseTargetView.findViewById(R.id.btnChoose);
                    btnChoose.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            activity.popView();
                            Message msg = new Message(ServerInterface.RequestType.VoteToKill);
                            msg.addParam("target", chooseTargetView.getSelectedPosition());
                            GameClient.SendMessage(msg);
                        }
                    });

                    activity.pushView(chooseTargetView);
                }
            }
        });

        GameClient.AddListener(ServerInterface.ResponseType.DiscussionEnd, new GameClient.OnMessageListener() {
            @Override
            public void onReceivedMessage(Message msg) {
                // update players' states
                int killedIdx = -1;
                try {
                    killedIdx = updateStatesInformation(msg.jsonObj.getJSONArray("playersStatus"));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                activity.showToast("Player "+killedIdx+" was killed", 3000);
            }
        });
    }

    public void updatePlayerList(JSONArray playerAry) throws JSONException {
        for(int i=0; i<playerAry.length(); i++) {
            if(playerList.size() <= i) playerList.add(new Player(i));
            if(!playerAry.isNull(i)) {
                JSONObject data = playerAry.getJSONObject(i);
                playerList.get(i).updateInfo(data);
            }
            else {
                playerList.get(i).name = null;  // set to empty
            }
        }
    }

    public void updateRoleInformation(JSONArray roleAry) throws JSONException {
        for(int i=0; i<roleAry.length(); i++) {
            playerList.get(i).role = roleAry.getJSONObject(i).getInt("role");
        }
    }

    //TODO: hard code, return the idx of who's killed, every turn only one people would be killed
    public int updateStatesInformation(JSONArray roleAry) throws JSONException {
        for(int i=0; i<roleAry.length(); i++) {
            if(roleAry.getJSONObject(i).getInt("status") == 1 &&
                    playerList.get(i).status == Player.Status.Alive) {
                playerList.get(i).status = Player.Status.Died;
                return i;
            }
        }
        return -1;
    }

    private ArrayList<Player> getAlivePlayers() {
        ArrayList<Player> ret = new ArrayList<>();
        for(Player player : playerList) {
            if(player.status == Player.Status.Alive) ret.add(player);
        }
        return ret;
    }
}
