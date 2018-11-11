package test.depaul.edu.test.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

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

    public void initialize(MainActivity act) {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        activity = act;

        Button btnCreate = findViewById(R.id.btnCreateGame);
        btnCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.showLoading();
                //TODO: hardcode
                Message msg = new Message(ServerInterface.RequestType.CreateGame);
                msg.addParam("game_name", "test game");
                msg.addParam("werewolf_count", 1);
                msg.addParam("villager_count", 1);
                msg.addParam("other_roles", 0b100);
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
