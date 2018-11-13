package test.depaul.edu.test.Views;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import test.depaul.edu.test.MainActivity;
import test.depaul.edu.test.R;

public class EditPlayerView extends LinearLayout {
    private MainActivity activity = null;

    public EditPlayerView(Context context) {
        super(context);
    }
    public EditPlayerView(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }

    public EditPlayerView(Context context,  AttributeSet attrs, int defStyleAttr) {
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

        Button btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textName = findViewById(R.id.textName);
                if(textName.getText().length() > 0) {
                    SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("player_name", textName.getText().toString());
                    editor.apply();

                    activity.popView();
                    activity.enterMainView();
                }
                else {
                    activity.showToast("Please enter your name", 1500);
                }
            }
        });
    }
}
