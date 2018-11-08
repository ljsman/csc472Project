package csc472.depaul.edu.csc472finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import csc472.depaul.edu.csc472finalproject.gameModel.*;

public class GameMainUi extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_main_ui);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.welcome);
        tv.setText(stringFromJNI());

        final Button next = findViewById(R.id.mainUiNext);
        if (next != null){
            next.setOnClickListener(onClickNext);
        }


    }

    private final GameMainUi getGameMainUiActivity(){
        return this;
    }

    private View.OnClickListener onClickNext = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if((validateEditTextField(R.id.editTextPlayerName)) && (validateEditTextField(R.id.editTextAvatorName))){
                String playerName = getEditViewText(R.id.editTextPlayerName);
                String avatorName = getEditViewText(R.id.editTextAvatorName);

                //TODO: from here we lunch a new activity NewGame.class ussing Intent
                Intent intent = new Intent(getGameMainUiActivity(), NewGame.class);
                Player newPlayer = new Player();

            }
        }
    };


    //helper functions below
    private boolean validateEditTextField(int id)
    {
        boolean isValid = false;

        final EditText editText = findViewById(id);
        if (editText != null)
        {
            String sText = editText.getText().toString();

            isValid = ((sText != null) && (!sText.isEmpty()));
        }

        if (!isValid)
        {
            String sToastMessage = "Please make sure you filled in names for all the fields!";
            Toast toast = Toast.makeText(getApplicationContext(), sToastMessage, Toast.LENGTH_LONG);
            toast.show();
        }

        return isValid;
    }

    private final String getEditViewText(int id)
    {
        String sText = "";

        final EditText editText = findViewById(id);
        if (editText != null)
        {
            sText = editText.getText().toString();
        }

        return sText;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
