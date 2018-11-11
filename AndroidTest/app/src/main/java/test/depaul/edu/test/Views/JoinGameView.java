package test.depaul.edu.test.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import test.depaul.edu.test.GameClient;
import test.depaul.edu.test.MainActivity;
import test.depaul.edu.test.Message;
import test.depaul.edu.test.R;
import test.depaul.edu.test.ServerInterface;

public class JoinGameView extends LinearLayout {
    private MainActivity activity = null;

    public JoinGameView(Context context) {
        super(context);
    }
    public JoinGameView(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }
    public JoinGameView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(final MainActivity act) {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        activity = act;

        Button btnJoin = findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                activity.showLoading();

                EditText textView = findViewById(R.id.gameIdInput);
                int gameId = Integer.parseInt(textView.getText().toString());
                Message msg = new Message(ServerInterface.RequestType.JoinGame);
                msg.addParam("game_id", gameId);
                GameClient.SendMessage(msg, new GameClient.OnMessageListener() {
                    @Override
                    public void onReceivedMessage(Message msg) {
                        activity.hideLoading();

                        LayoutInflater vi = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
