package test.depaul.edu.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import test.depaul.edu.test.Views.EditPlayerView;
import test.depaul.edu.test.Views.MainView;

public class MainActivity extends AppCompatActivity {
    public FrameLayout frameLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = findViewById(R.id.rootView);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if(sharedPref.getString("player_name", null) != null) {
            enterMainView();
        }
        else {
            LayoutInflater vi = getLayoutInflater();
            EditPlayerView editPlayerView = (EditPlayerView)vi.inflate(R.layout.edit_player_view, null);
            editPlayerView.initialize(this);
            pushView(editPlayerView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GameClient.Disconnect();
    }

    public void enterMainView() {
        LayoutInflater vi = getLayoutInflater();
        MainView mainView = (MainView)vi.inflate(R.layout.main_view, null);
        mainView.initialize(this);
        pushView(mainView);

        GameClient.Connect();
        // wait until server connected
        showLoading();
        GameClient.AddListener(ServerInterface.ClientEventType.Connected, new GameClient.OnMessageListener() {
            @Override
            public void onReceivedMessage(Message msgRec) {
                hideLoading();

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                Message msg = new Message(ServerInterface.RequestType.RegisterUser);
                msg.addParam("name", sharedPref.getString("player_name", null));
                msg.addParam("avatar", 1);
                GameClient.SendMessage(msg);
            }
        });
    }

    private View loadingView = null;
    public void showLoading() {
        if(loadingView == null) {
            LayoutInflater vi = getLayoutInflater();
            loadingView = vi.inflate(R.layout.loading_view, null);
            loadingView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });
        }
        this.pushView(loadingView);
    }

    public void hideLoading() {
        if(loadingView.getParent() != null) frameLayout.removeView(loadingView);
    }

    public void pushView(View view) {
        frameLayout.addView(view);
    }

    public void popView() {
        frameLayout.removeViewAt(frameLayout.getChildCount()-1);
    }

    public void showToast(String text, int duration) {
        Toast toast = Toast.makeText(this, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
