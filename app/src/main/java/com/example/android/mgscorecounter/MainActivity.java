package com.example.android.mgscorecounter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.text.DateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    static final String GAME_PREFS = "GameHistoryFile";
    static final String PREF_WINNERSCORE = "PREF_WINNERSCORE";
    static final String MAX_HOLES = "MAX_HOLES";
    public String namePlayerA, namePlayerB, winner, winnerScore;
    public Button enter, reset, undo;
    public ViewSwitcher switcherA, switcherB;
    public EditText edtA, edtB;
    public TextView hiddenTextA, hiddenTextB;
    public int scorePlayerA, scorePlayerB, lastScore, holesCount, maxHoles = 18;
    boolean wasPlayerA;
    SharedPreferences gamePrefs;
    SharedPreferences.Editor prefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_action_golf);

        setContentView(R.layout.activity_main);

        //Defining XML Views for Java
        switcherA = (ViewSwitcher) findViewById(R.id.view_switcherA);
        switcherB = (ViewSwitcher) findViewById(R.id.view_switcherB);

        switcherA.setSaveEnabled(true);
        switcherB.setSaveEnabled(true);

        edtA = (EditText) findViewById(R.id.edit_playerA);
        edtB = (EditText) findViewById(R.id.edit_playerB);

        hiddenTextA = (TextView) findViewById(R.id.hidden_textViewA);
        hiddenTextB = (TextView) findViewById(R.id.hidden_textViewB);

        enter = (Button) findViewById(R.id.enterNames);
        undo = (Button) findViewById(R.id.undo_button);

        reset = (Button) findViewById(R.id.reset_score);

        //onClickListener for the reset Button so I can use the resetScore method repeatedly (former resetScores (View v) couldn't be reused)
        reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetScores();
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString("NameA", namePlayerA);
        savedInstanceState.putString("NameB", namePlayerB);
        savedInstanceState.putInt("ScoreA", scorePlayerA);
        savedInstanceState.putInt("ScoreB", scorePlayerB);
        savedInstanceState.putInt("LastScore", lastScore);
        savedInstanceState.putInt("PlayedHoles", holesCount);
        savedInstanceState.putInt("MaxHoles", maxHoles);
        savedInstanceState.putString("Winner", winner);
        savedInstanceState.putString("WinnerScore", winnerScore);
        savedInstanceState.putBoolean("PlayerA?", wasPlayerA);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        namePlayerA = savedInstanceState.getString("NameA");
        namePlayerB = savedInstanceState.getString("NameB");
        scorePlayerA = savedInstanceState.getInt("ScoreA");
        scorePlayerB = savedInstanceState.getInt("ScoreB");
        lastScore = savedInstanceState.getInt("LastScore");
        holesCount = savedInstanceState.getInt("PlayedHoles");
        maxHoles = savedInstanceState.getInt("MaxHoles");
        winner = savedInstanceState.getString("Winner");
        winnerScore = savedInstanceState.getString("WinnerScore");
        wasPlayerA = savedInstanceState.getBoolean("PlayerA?");
        displayNamePlayerA(namePlayerA);
        displayNamePlayerB(namePlayerB);
        displayForPlayerA(scorePlayerA);
        displayForPlayerB(scorePlayerB);
        holesMsg();
    }

    //Doesn't work properly if screen is changed in the very beginning


    public void setPlayerNames(View v) {

        switcherA.showNext();                               // Change View from EditText to TextView, switch to next view
        namePlayerA = edtA.getText().toString();            // Convert Input from EditText into String for further use
        displayNamePlayerA(namePlayerA);

        switcherB.showNext();
        namePlayerB = edtB.getText().toString();
        displayNamePlayerB(namePlayerB);

        enter.setVisibility(View.GONE);                     // Hide Enter Button
        reset.setEnabled(true);                             // Enable Reset Button (Will be disabled with another method and here enabled again)

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);       //Hides soft keyboard
        imm.hideSoftInputFromWindow(edtB.getWindowToken(), 0);
    }


    public void addScorePlayerA(View v) {

        Button b = (Button) v;
        String buttonName = b.getText().toString();

        int numberOfStrokes = Integer.parseInt(buttonName.split(" ")[0]);

        if (numberOfStrokes == 6) {
            numberOfStrokes++;                              // numberOfStrokes++ == numberOfStrokes = numberOfStrokes + 1
        }

        wasPlayerA = true;                                  // needed for Undo Button
        lastScore = scorePlayerA;                           // needed for Undo Button

        scorePlayerA += numberOfStrokes;                    // scorePlayerA += numberOfStrokes == scorePlayerA = scorePlayerA + numberOfStrokes, auch möglich: -=, *=, /=
        displayForPlayerA(scorePlayerA);

        holesCount++;                                       // Add one to Holes played
        holesMsg();                                         // Do holesMessage() Method

        undo.setEnabled(true);                              // Enable Undo Button
    }


    public void addScorePlayerB(View v) {

        Button b = (Button) v;
        String buttonName = b.getText().toString();                         // Change Button Text into String Variable

        int numberOfStrokes = Integer.parseInt(buttonName.split(" ")[0]);   // Change Number of Button Text into Integer using an array ((" ")[0]) to only select the number part

        if (numberOfStrokes == 6) {
            numberOfStrokes++;
        }

        wasPlayerA = false;
        lastScore = scorePlayerB;

        scorePlayerB += numberOfStrokes;
        displayForPlayerB(scorePlayerB);

        undo.setEnabled(true);
    }


    //Holes Count Methode, extra for better structure

    public void holesMsg() {

        Resources res = getResources();

        String messageStart = String.format(getString(R.string.played_holes_msg_start), holesCount);
        String messageEnd = "st hole.";                                                                     //!!Asked for help in forum to put this enumeration into string.xml

        if (holesCount == 2) {
            messageEnd = "nd hole.";
        }

        if (holesCount == 3) {
            messageEnd = "rd hole.";
        }
        if (holesCount >= 4) {
            messageEnd = "th hole.";
        }

        displayHolesCount(messageStart + messageEnd);
        finishGameMessage();
    }


    //Add User Input Hole Number Button (laater)


    public void undoButton(View v) {

        if (wasPlayerA) {                                   // if (wasPlayerA) == if (wasPlayerA == true)
            displayForPlayerA(lastScore);
            scorePlayerA = lastScore;
        } else {
            displayForPlayerB(lastScore);
            scorePlayerB = lastScore;
            holesCount--;                                   // holesCount-- == holesCount = holesCount - 1
            holesMsg();
        }

        undo.setEnabled(false);                             //Disable Undo Button
    }

    //reset score

    public void resetScores() {

        if (switcherA.getCurrentView() == hiddenTextA) {
            switcherA.showPrevious();                       //switch to prior view
        }

        if (switcherB.getCurrentView() == hiddenTextB) {
            switcherB.showPrevious();
        }

        enter.setVisibility(View.VISIBLE);

        scorePlayerA = 0;
        scorePlayerB = 0;
        lastScore = 0;

        displayForPlayerA(scorePlayerA);
        displayForPlayerB(scorePlayerB);

        holesCount = 0;
        displayHolesCount("");

        reset.setEnabled(false);                            //Disable Reset Button
    }


    //Alert Dialog after 18th hole -> FINISH GAME

    public void finishGameMessage() {

        Resources res = getResources();

        if (scorePlayerA <= scorePlayerB) {
            winner = String.format(res.getString(R.string.winner), namePlayerA);
            winnerScore = String.format(res.getString(R.string.winner_score), namePlayerA, scorePlayerA, namePlayerB, scorePlayerB);
            //== winnerScore = namePlayerA + " won with " + scorePlayerA + " strokes over " + namePlayerB + "s " + scorePlayerB + " strokes.";
        } else {
            winner = String.format(res.getString(R.string.winner), namePlayerB);
            winnerScore = String.format(res.getString(R.string.winner_score), namePlayerB, scorePlayerB, namePlayerA, scorePlayerA);
            //== winnerScore = namePlayerB + " won with " + scorePlayerB + " strokes over " + namePlayerA + "s " + scorePlayerA + " strokes.";
        }

        String dialogMsg = String.format(res.getString(R.string.dialog_msg_start), maxHoles) + winner + " " + winnerScore;
        //== dialogMsg = getString(R.string.dialog_msg_start) + " " + maxHoles + getString(R.string.dialog_msg_end) + winner + winnerScore;

        //Builder for Pop Up Dialog Message

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialogMsg)
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.dialog_new_game_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveGameScores();
                        resetScores();
                    }
                });

        AlertDialog alertDialog = builder.create();

        if (holesCount == maxHoles) {
            alertDialog.show();
        }
    }

    //Various Display Methods

    public void displayForPlayerA(int score) {
        TextView scoreView = (TextView) findViewById(R.id.score_playerA);
        scoreView.setText(String.valueOf(score));
    }

    public void displayForPlayerB(int score) {
        TextView scoreView = (TextView) findViewById(R.id.score_playerB);
        scoreView.setText(String.valueOf(score));
    }

    public void displayNamePlayerA(String nameA) {
        TextView nameView = (TextView) findViewById(R.id.hidden_textViewA);
        nameView.setText(nameA);
    }

    public void displayNamePlayerB(String nameB) {
        TextView nameView = (TextView) findViewById(R.id.hidden_textViewB);
        nameView.setText(nameB);
    }

    public void displayHolesCount(String s) {
        TextView hView = (TextView) findViewById(R.id.holes_count);
        hView.setText(s);
    }


    //Inflate counter_menu from res/menu directory

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.counter_menu, menu);
        return true;
    }

    //Set actions that will happen if clicked on the menu items

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_game:
                resetScores();
                return true;

            case R.id.holes_18:
                resetScores();
                maxHoles = 18;
                toastHoles();
                return true;

            case R.id.holes_27:
                resetScores();
                maxHoles = 27;
                toastHoles();
                return true;

            case R.id.history:
                loadGameScores();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Toast Message for Change to 27 Holes

    public void toastHoles() {

        Resources res = getResources();

        Context context = getApplicationContext();
        CharSequence text = String.format(res.getString(R.string.toast), maxHoles);
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }


    //Save winnerScore using SharedPreferences

    public void saveGameScores() {

        gamePrefs = getSharedPreferences(GAME_PREFS, MODE_PRIVATE);
        prefsEditor = gamePrefs.edit();
        prefsEditor.putString(PREF_WINNERSCORE, winnerScore);
        prefsEditor.putInt(MAX_HOLES, maxHoles);
        prefsEditor.commit();
    }


    //Load winnerScore in Submenu Game History using SharedPreferences

    public void loadGameScores() {

        Resources res = getResources();

        gamePrefs = getSharedPreferences(GAME_PREFS, MODE_PRIVATE);

        String currentDate = DateFormat.getDateInstance().format(new Date());
        String gameHistory = String.format(res.getString(R.string.game_history_dialog_msg),
                                currentDate,
                                gamePrefs.getInt(MAX_HOLES, 0),
                                gamePrefs.getString(PREF_WINNERSCORE, ""));
      //String gameHistory = currentDate + " - Set with " + gamePrefs.getInt(MAX_HOLES, 0) + " holes: \n" + gamePrefs.getString(PREF_WINNERSCORE, "");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(gameHistory)
                .setTitle(R.string.game_history_dialog_title)
                .setPositiveButton(R.string.game_history_dialog_back_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //no action needed here if one only wants the dialog dismissed if button pressed
                //dismiss mehtod is already integrated
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}