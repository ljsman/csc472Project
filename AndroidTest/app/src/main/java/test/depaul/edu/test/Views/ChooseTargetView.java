package test.depaul.edu.test.Views;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import test.depaul.edu.test.Models.Player;
import test.depaul.edu.test.R;

public class ChooseTargetView extends LinearLayout {
    private ChooseTargetAdapter adapter;
    private ArrayList<Player> playerList;
    private View selectedView = null;
    private int selectedIdx = -1;

    public ChooseTargetView(Context context) {
        super(context);
    }
    public ChooseTargetView(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }
    public ChooseTargetView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(ArrayList<Player> list) {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        playerList = list;

        GridView gridview = findViewById(R.id.targetGrid);
        adapter = new ChooseTargetAdapter(getContext(), list);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(selectedView != null) selectedView.setBackgroundResource(R.color.colorBackground);
                selectedView = view;
                selectedIdx = i;
                view.setBackgroundResource(R.drawable.shape_border_blue);
            }
        });
    }

    public int getSelectedPosition() {
        return playerList.get(selectedIdx).position;
    }
}
