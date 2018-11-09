package test.depaul.edu.test;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import test.depaul.edu.test.Views.MainView;

public class MainActivity extends AppCompatActivity {
    public FrameLayout frameLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        frameLayout = findViewById(R.id.rootView);

        LayoutInflater vi = getLayoutInflater();
        MainView mainView = (MainView)vi.inflate(R.layout.main_view, null);
        mainView.initialize(this);
        pushView(mainView);

        showLoading();
        GameClient.Connect();
        // wait until server connected
        GameClient.AddListener(ServerInterface.ClientEventType.Connected, new GameClient.OnMessageListener() {
            @Override
            public void onReceivedMessage(Message msgRec) {
                hideLoading();

                //TODO: hardcode register player
                Message msg = new Message(ServerInterface.RequestType.RegisterUser);
                msg.addParam("name", "Jason");
                msg.addParam("avatar", 1);
                GameClient.SendMessage(msg);
            }
        });


        /*
        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                GameClient.Connect();
            }
        });

        Button btnTeardown = findViewById(R.id.btnTeardown);
        btnTeardown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                GameClient.Disconnect();
            }
        });

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Message msg = new Message(ServerInterface.RequestType.RegisterUser);
                msg.addParam("name", "Jason");
                msg.addParam("avatar", 5);
                GameClient.SendMessage(msg);
            }
        });

        Button btnCreateGame = findViewById(R.id.btnCreateGame);
        btnCreateGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Message msg = new Message(ServerInterface.RequestType.CreateGame);
                msg.addParam("game_name", "game1");
                msg.addParam("werewolf_count", 2);
                msg.addParam("villager_count", 5);
                msg.addParam("other_roles", 0);
                GameClient.SendMessage(msg);
            }
        });

        Button btnGetGameList = findViewById(R.id.btnGetGameList);
        btnGetGameList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // send a request and wait for response
                Message msg = new Message(ServerInterface.RequestType.GetGamesList);
                GameClient.SendMessage(msg, new GameClient.OnMessageListener() {
                    @Override
                    public void onReceivedMessage(Message msg) {
                        Log.v("MainActivity", "get gamelist: "+msg.getJSONString());
                    }
                });
            }
        });

        Button btnJoinGame = findViewById(R.id.btnJoinGame);
        btnJoinGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Message msg = new Message(ServerInterface.RequestType.JoinGame);
                msg.addParam("game_id", 1);
                GameClient.SendMessage(msg);
            }
        });

        Button btnExitGame = findViewById(R.id.btnExitGame);
        btnExitGame.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Message msg = new Message(ServerInterface.RequestType.ExitGame);
                GameClient.SendMessage(msg);
            }
        });


        // add listener
        GameClient.AddListener(ServerInterface.ResponseType.UpdatePlayersInformation, new GameClient.OnMessageListener() {
            @Override
            public void onReceivedMessage(Message msg) {
                Log.v("MainActivity", "get players list: "+msg.getJSONString());
            }
        });
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GameClient.Disconnect();
    }

    private View loadingView = null;
    public void showLoading() {
        if(loadingView == null) {
            LayoutInflater vi = getLayoutInflater();
            loadingView = vi.inflate(R.layout.loading_view, null);
        }
        this.pushView(loadingView);
    }

    public void hideLoading() {
        this.popView();
    }

    public void pushView(View view) {
        frameLayout.addView(view);
    }

    public void popView() {
        frameLayout.removeViewAt(frameLayout.getChildCount()-1);
    }
}
