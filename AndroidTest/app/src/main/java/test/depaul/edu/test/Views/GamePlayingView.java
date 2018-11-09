package test.depaul.edu.test.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import test.depaul.edu.test.MainActivity;

public class GamePlayingView extends LinearLayout {
    private MainActivity activity = null;

    public GamePlayingView(Context context) {
        super(context);
    }
    public GamePlayingView(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }

    public GamePlayingView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(MainActivity act) {
        activity = act;

    }
}
