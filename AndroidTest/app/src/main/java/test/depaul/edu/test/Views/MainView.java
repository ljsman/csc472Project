package test.depaul.edu.test.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import test.depaul.edu.test.MainActivity;
import test.depaul.edu.test.R;

public class MainView extends LinearLayout {
    private MainActivity activity = null;

    public MainView(Context context) {
        super(context);
    }
    public MainView(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }

    public MainView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(MainActivity act) {
        activity = act;

        Button btnCreate = findViewById(R.id.btnCreateGame);
        btnCreate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                LayoutInflater vi = activity.getLayoutInflater();
                CreateGameView createView = (CreateGameView)vi.inflate(R.layout.create_game_view, null);
                createView.initialize(activity);
                activity.pushView(createView);
            }
        });

        Button btnJoin = findViewById(R.id.btnJoinGame);
        btnJoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });
    }
}
