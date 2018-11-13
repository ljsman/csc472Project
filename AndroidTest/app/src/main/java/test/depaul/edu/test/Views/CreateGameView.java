package test.depaul.edu.test.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import test.depaul.edu.test.GameClient;
import test.depaul.edu.test.MainActivity;
import test.depaul.edu.test.Message;
import test.depaul.edu.test.R;
import test.depaul.edu.test.ServerInterface;

public class CreateGameView extends LinearLayout {
    private MainActivity activity = null;

    public CreateGameView(Context context) {
        super(context);
    }
    public CreateGameView(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }

    public CreateGameView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    private int werewolfCount = 1;
    private int villagerCount = 1;
    private boolean hasSeer = true;
    public void initialize(MainActivity act) {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        activity = act;

        final TextView textWerewolf = findViewById(R.id.textWerewolf);
        final TextView textVillager = findViewById(R.id.textVillager);

        Button btnWerewolfMinus = findViewById(R.id.btnWerewolfMinus);
        btnWerewolfMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                werewolfCount = Math.max(1, werewolfCount-1);
                textWerewolf.setText(""+werewolfCount);
            }
        });

        Button btnWerewolfPlus = findViewById(R.id.btnWerewolfPlus);
        btnWerewolfPlus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                werewolfCount = Math.min(4, werewolfCount+1);
                textWerewolf.setText(""+werewolfCount);
            }
        });

        Button btnVillagerMinus = findViewById(R.id.btnVillagerMinus);
        btnVillagerMinus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                villagerCount = Math.max(1, villagerCount-1);
                textVillager.setText(""+villagerCount);
            }
        });

        Button btnVillagerPlus = findViewById(R.id.btnVillagerPlus);
        btnVillagerPlus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                villagerCount = Math.min(8, villagerCount+1);
                textVillager.setText(""+villagerCount);
            }
        });

        final Button btnSeer = findViewById(R.id.btnSeer);
        btnSeer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasSeer) {
                    hasSeer = false;
                    btnSeer.setBackgroundResource(R.color.colorGray);
                }
                else {
                    hasSeer = true;
                    btnSeer.setBackgroundResource(R.color.colorBlue);
                }
            }
        });

        Button btnCreate = findViewById(R.id.btnCreateGame);
        btnCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.showLoading();

                Message msg = new Message(ServerInterface.RequestType.CreateGame);
                msg.addParam("game_name", "game game game");
                msg.addParam("werewolf_count", werewolfCount);
                msg.addParam("villager_count", villagerCount);
                if(hasSeer) msg.addParam("other_roles", 0b100);
                else msg.addParam("other_roles", 0b0);
                GameClient.SendMessage(msg, new GameClient.OnMessageListener() {
                    @Override
                    public void onReceivedMessage(Message msg) {
                        activity.hideLoading();

                        LayoutInflater vi = activity.getLayoutInflater();
                        GamePlayingView playingView = (GamePlayingView)vi.inflate(R.layout.game_playing_view, null);
                        playingView.initialize(activity, msg);
                        activity.pushView(playingView);
                    }
                });
            }
        });

        Button btnGoBack = findViewById(R.id.btnGoBack);
        btnGoBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.popView();
            }
        });
    }
}
