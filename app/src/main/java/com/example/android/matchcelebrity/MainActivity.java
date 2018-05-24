package com.example.android.matchcelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ArrayList<Celebrity> arrayCelebrities = new ArrayList<Celebrity>();
    ArrayList<Celebrity> arrayCopy = new ArrayList<Celebrity>();
    ArrayList<String> options = new ArrayList<String>();
    TextView finalScore;
    int rounds = 10;
    int total_rounds = 10;
    SeekBar seekbar;
    RelativeLayout layout_sett;
    RelativeLayout layout_game;
    RelativeLayout layout_end;
    int correctPosition;
    ImageView img;
    int score;
    int celeb;
    Button btn0;
    Button btn1;
    Button btn2;
    Button btn3;
    public class Celebrity{
        String name;
        String pic;
    }

    public void playAgain(View view){
        score = 0;
        rounds = 10;
        seekbar.setProgress(10);
        arrayCelebrities = arrayCopy;
        layout_sett.setVisibility(View.VISIBLE);
        layout_end.setVisibility(View.GONE);
        layout_game.setVisibility(View.GONE);
    }

    public void roundClick(View view){
        String btn_id = (String) view.getTag();
        if(Integer.parseInt(btn_id) == correctPosition ){
            score+=1;
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Wrong! The answer was " + options.get(correctPosition), Toast.LENGTH_SHORT).show();
        }
        gameRound();
    }

    public class DownloadTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            String result = "";
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                InputStreamReader reader = new InputStreamReader(conn.getInputStream());
                int data = reader.read();
                while( data != -1 ){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }
            return result;
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                Bitmap myBitmap = BitmapFactory.decodeStream(connection.getInputStream());
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void startGame(View view){
        layout_sett.setVisibility(View.GONE);
        layout_end.setVisibility(View.GONE);
        layout_game.setVisibility(View.VISIBLE);
        score = 0;
        total_rounds = rounds;
        gameRound();
    }

    private void gameRound(){
        Random rand = new Random();
        int incorrectAnswer;
        if( rounds > 0 ){
            options.clear();
            correctPosition = rand.nextInt(4);
            celeb = rand.nextInt(arrayCelebrities.size());
            for(int k = 0; k < 9; k++){
                celeb = rand.nextInt(arrayCelebrities.size());
            }
            for(int i = 0; i < 4; i++){
                if( i == correctPosition ){
                    options.add(arrayCelebrities.get(celeb).name);
                }
                else{
                    incorrectAnswer = rand.nextInt(arrayCopy.size());
                    while(arrayCopy.get(incorrectAnswer).name == arrayCelebrities.get(celeb).name){
                        incorrectAnswer = rand.nextInt(arrayCopy.size());
                    }
                    options.add(arrayCopy.get(incorrectAnswer).name);
                }
            }
            System.out.println(options.toString());
            btn0.setText(options.get(0).toString());
            btn1.setText(options.get(1).toString());
            btn2.setText(options.get(2).toString());
            btn3.setText(options.get(3).toString());
            ImageDownloader task = new ImageDownloader();
            try {
                Bitmap myCeleb = task.execute(arrayCelebrities.get(celeb).pic).get();
                img.setImageBitmap(myCeleb);
            } catch (Exception e) {
                e.printStackTrace();
            }
            arrayCelebrities.remove(celeb);
            rounds = rounds - 1;
        }
        else{
            layout_game.setVisibility(View.GONE);
            layout_end.setVisibility(View.VISIBLE);
            finalScore.setText( Integer.toString(score) + "/" + Integer.toString(total_rounds));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DownloadTask task = new DownloadTask();
        btn0 = (Button) findViewById(R.id.btn0);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        layout_end = (RelativeLayout) findViewById(R.id.endGame);
        layout_sett = (RelativeLayout) findViewById(R.id.layout_settings);
        layout_game = (RelativeLayout) findViewById(R.id.layout_game);
        String result;
        String [] positions;
        Pattern pattern;
        Matcher match;
        finalScore = (TextView) findViewById(R.id.finalscore);
        final TextView num = (TextView) findViewById(R.id.rounds);
        img = (ImageView) findViewById(R.id.imageView);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax(100);
        seekbar.setProgress(10);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               if(progress <= 0){
                    seekBar.setProgress(1);
                    num.setText("1");
                   rounds = 1;
                }else {
                   num.setText(Integer.toString(progress));
                   rounds = progress;
               }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        try {
            result = task.execute("http://www.posh24.com/celebrities").get();
            positions = result.split("<div class=\"channelListEntry\">");
            for(int i=1; i < positions.length; i++){
                Celebrity celeb = new Celebrity();
                pattern = Pattern.compile("<img src=\"(.*?)\" alt");
                match = pattern.matcher(positions[i]);
                while(match.find()){
                    celeb.pic = (match.group(1));
                }
                pattern = Pattern.compile("alt=\"(.*?)\"/>");
                match = pattern.matcher(positions[i]);
                while(match.find()){
                    celeb.name = (match.group(1));
                }
                arrayCelebrities.add(celeb);
                arrayCopy.add(celeb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
