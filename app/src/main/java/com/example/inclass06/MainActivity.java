package com.example.inclass06;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    TextView seekbarComplexityValue, progressBarValue, averageValue;
    ListView listView;
    SeekBar complexitySeekBar;
    int progressValue;
    ProgressDialog progressDialog;
    ExecutorService threadPool;
    ProgressBar progressBar;
    ArrayList<Double> listOfNumbers = new ArrayList<>();
    ArrayAdapter<Double> adapter;
    double sum, average;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getResources().getString(R.string.activityTitle));

        threadPool = Executors.newFixedThreadPool(2);
        progressBarValue = findViewById(R.id.progressBarValue);
        averageValue = findViewById(R.id.averageValue);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        listView = findViewById(R.id.listView);
        seekbarComplexityValue = findViewById(R.id.seekbarComplexityValue);
        complexitySeekBar = findViewById(R.id.complexitySeekBar);
        complexitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
                if (progressValue == 1){
                    seekbarComplexityValue.setText(progressValue + " Time");
                }
                else if (progressValue == 0){
                    seekbarComplexityValue.setText("");
                }
                else{
                    seekbarComplexityValue.setText(progressValue + " Times");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sum = 0;
                average = 0;
                listOfNumbers.clear();
                progressBarValue.setText("");
                averageValue.setText("");
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        findViewById(R.id.generateUsingThread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sum = 0;
                average = 0;
                listOfNumbers.clear();
                progressBarValue.setText("");
                averageValue.setText("");
                adapter.notifyDataSetChanged();

                if (progressValue == 0){
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.blankComplexity), Toast.LENGTH_SHORT).show();
                }
                else{
                    complexitySeekBar.setEnabled(false);
                    findViewById(R.id.generateUsingThread).setEnabled(false);
                    findViewById(R.id.generateUsingAsyncTask).setEnabled(false);
                    threadPool.execute(new DoThreadWork());
                }

            }
        });


        findViewById(R.id.generateUsingAsyncTask).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sum = 0;
                average = 0;
                listOfNumbers.clear();
                progressBarValue.setText("");
                averageValue.setText("");
                adapter.notifyDataSetChanged();
                if (progressValue == 0){
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.blankComplexity), Toast.LENGTH_SHORT).show();
                }
                else{
                    new WorkAsync().execute(progressValue);
                }
            }
        });

        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, listOfNumbers);
        listView.setAdapter(adapter);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {

                progressBar.setVisibility(View.VISIBLE);
                int value = msg.getData().getInt(DoThreadWork.INDEX_KEY) + 1;
                progressBar.setMax(progressValue);
                progressBar.setProgress(value);
                progressBarValue.setText(value + "/" + progressValue);
                averageValue.setText("Average: " + msg.getData().getDouble(DoThreadWork.AVERAGE_KEY));

                if (value == progressValue){
                    complexitySeekBar.setEnabled(true);
                    findViewById(R.id.generateUsingThread).setEnabled(true);
                    findViewById(R.id.generateUsingAsyncTask).setEnabled(true);
                }

                adapter.notifyDataSetChanged();
                return false;
            }
        });
    }


    class DoThreadWork implements Runnable{
        static final String AVERAGE_KEY = "AVERAGE";
        static final String INDEX_KEY = "INDEX";

        @Override
        public void run() {
            for (int i = 0; i < progressValue; i++){
                listOfNumbers.add(HeavyWork.getNumber());
                sum += listOfNumbers.get(i);
                average = sum / (i + 1);
                Bundle bundle = new Bundle();
                bundle.putInt(INDEX_KEY, i);
                bundle.putDouble(AVERAGE_KEY, average);
                Message message = new Message();
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }
    }


    class WorkAsync extends AsyncTask<Integer, Integer, Double>{

        @Override
        protected Double doInBackground(Integer... integers) {
            for (int i = 0; i < integers[0]; i++){
                listOfNumbers.add(HeavyWork.getNumber());
                sum += listOfNumbers.get(i);
                average = sum / (i + 1);
                publishProgress(i);
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.generateUsingThread).setEnabled(false);
            findViewById(R.id.generateUsingAsyncTask).setEnabled(false);
            complexitySeekBar.setEnabled(false);
        }


        @Override
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);
            findViewById(R.id.generateUsingAsyncTask).setEnabled(true);
            findViewById(R.id.generateUsingThread).setEnabled(true);
            complexitySeekBar.setEnabled(true);
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            complexitySeekBar.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setMax(progressValue);
            progressBar.setProgress(values[0] + 1);
            progressBarValue.setText((values[0] + 1) + "/" + progressValue);
            averageValue.setText("Average: " + average);
            adapter.notifyDataSetChanged();
        }
    }
}