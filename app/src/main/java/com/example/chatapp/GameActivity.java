package com.example.chatapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.gridlayout.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class GameActivity extends AppCompatActivity {

    int activePlayer = 0;
    boolean gameActive = true;
    int chanceCount = 0;
    int[] gameState = {2, 2, 2, 2, 2, 2, 2, 2, 2};
    int[][] winPos = {{0,1,2},{3,4,5},{6,7,8},{0,3,6},{1,4,7},{2,5,8},{0,4,8},{2,4,6}};

    Button playAgainButton;
    TextView winnerText;
    GridLayout grid;

    public void dropIn(View view) {

        ImageView imageView = (ImageView) view;
        int counter = Integer.parseInt(view.getTag().toString());
        Log.i("TAG ", "Tag: " + counter);

        if(gameState[counter] == 2 && gameActive) {

            chanceCount++;
            gameState[counter] = activePlayer;
            imageView.setTranslationY(-1500);

            if(activePlayer == 0) {

                imageView.setImageResource(R.drawable.yellow);
                imageView.animate().translationYBy(1500).rotation(3600).setDuration(500);
                activePlayer = 1;

            } else {

                imageView.setImageResource(R.drawable.red);
                imageView.animate().translationYBy(1500).rotation(3600).setDuration(500);
                activePlayer = 0;

            }

        }

        String mes = "";

        for(int[] winPosition : winPos) {

            if (gameState[winPosition[0]] == gameState[winPosition[1]] && gameState[winPosition[1]] == gameState[winPosition[2]] && gameState[winPosition[0]] !=2) {

                gameActive = false;

                if (activePlayer == 0) {

                    mes = "Red";

                } else

                    mes = "Yellow";

                winnerText.setText(mes + "Won");

                playAgainButton.setVisibility(View.VISIBLE);

                winnerText.setVisibility(View.VISIBLE);

            }

        }

        if(chanceCount == 9 && mes == "") {

            mes = "Game Tied!";

            winnerText.setText(mes);
            playAgainButton.setVisibility(View.VISIBLE);
            winnerText.setVisibility(View.VISIBLE);

        }

    }

    public void playAgain(View view) {

        playAgainButton.setVisibility(View.INVISIBLE);

        winnerText.setVisibility(View.INVISIBLE);

        for(int i=0; i<grid.getChildCount(); i++) {

            ImageView child = (ImageView) grid.getChildAt(i);

            child.setImageDrawable(null);
        }

        for(int i=0; i < gameState.length; i++) {

            gameState[i] = 2;

        }
        activePlayer = 0;

        gameActive = true;

        chanceCount = 0;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        playAgainButton = (Button) findViewById(R.id.button);
        winnerText = (TextView) findViewById(R.id.textView);
        grid = (GridLayout) findViewById(R.id.grid);

    }
}