package test.depaul.edu.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                Message msg = new Message(ServerInterface.RequestType.GetGamesList);
                GameClient.SendMessage(msg);
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GameClient.Disconnect();
    }
}
