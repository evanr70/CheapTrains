package com.oxlon.evan.cheaptrains;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class GetTimesActivity2 extends AppCompatActivity {

    boolean railcard;

    int timeoutCount;

    TextView reportTimeText;

    ProgressBar progressBar;

    int totalTime;

    private RecyclerView.Adapter mAdapter;
    RecyclerView mRecyclerView;
    Button sortListButton;
    ImageButton redoButton;

    List<Train> trains = new ArrayList<>();

    DateTime startDateTime;
    DateTime initialDateTime;

    String urlDateTimeString;

    DateTimeFormatter urlDateTimeFormat = DateTimeFormat.forPattern("ddMMyy/HHmm");
    DateTimeFormatter createDateTimeFormat = DateTimeFormat.forPattern("ddMMyyHHmm");


    String startStation;
    String endStation;

    String startDateString;
    String endDateString;

    String startTimeString;
    String endTimeString;

    String urlForm;
    String url;

    View.OnClickListener sortButtonOnClickListener;


    DateTime endDateTime;

    int previousTimeInt;
    int percentageInt;

    RequestQueue requestQueue;
    TrainGroupRequest firstTrainGroupRequest;

    boolean killRequests;

    @Override
    public void onBackPressed() {
        killRequests = true;
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_times);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Journey Results");

        Intent intent = getIntent();

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setMax(100);

        reportTimeText = findViewById(R.id.report_time);

        startStation = intent.getStringExtra("START_STATION");
        endStation = intent.getStringExtra("END_STATION");

        startDateString = intent.getStringExtra("START_DATE");
        endDateString = intent.getStringExtra("END_DATE");

        startTimeString = intent.getStringExtra("START_TIME");
        endTimeString = intent.getStringExtra("END_TIME");

        railcard = Objects.requireNonNull(intent.getExtras()).getBoolean("RAIL_CARD");

        redoButton = findViewById(R.id.redo_button);

        sortListButton = findViewById(R.id.sort_list);

        mRecyclerView = findViewById(R.id.recycler_view);

        // Improve performance for static sized RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Specify an adapter
        mAdapter = new MyAdapter(trains);
        mRecyclerView.setAdapter(mAdapter);

        // Instantiate the RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        previousTimeInt = 0;

        startDateTime = createDateTimeFormat.parseDateTime(startDateString + startTimeString);
        endDateTime = createDateTimeFormat.parseDateTime(endDateString + endTimeString);

//        endDateTime = startDateTime.plusDays(1);

        initialDateTime = startDateTime;

        totalTime = Seconds.secondsBetween(startDateTime, endDateTime).getSeconds();

        killRequests = false;

        sortButtonOnClickListener = new View.OnClickListener() {
                                              @Override
                                              public void onClick(View view) {
//                                                  Toast.makeText(GetTimesActivity2.this, "Sorted by price", Toast.LENGTH_SHORT).show();
                                                  Snackbar.make(view, "Sorted by price", Snackbar.LENGTH_SHORT).show();
                                                  Collections.sort(trains, new Comparator<Train>() {
                                                      @Override
                                                      public int compare(Train t1, Train t2) {
                                                          if (t1.getPrice().equals(t2.getPrice())) {
                                                              return t1.getDepartureDateTime().isBefore(t2.getDepartureDateTime()) ? -1 : 1;
                                                          }
                                                          return Double.parseDouble(t1.getPrice().replace("£", "")) < Double.parseDouble(t2.getPrice().replace("£", "")) ? -1 : 1;
                                                      }
                                                  });
                                                  System.out.println("trains sorted");
                                                  mAdapter.notifyDataSetChanged();
                                              }
                                          };


        progressBar.setVisibility(ProgressBar.VISIBLE);
        firstTrainGroupRequest = getTrainGroupRequest(startDateTime, requestQueue);
        firstTrainGroupRequest.setTag(this);
        // Add the request to the request queue
        requestQueue.add(firstTrainGroupRequest);

    }

    @NonNull
    private TrainGroupRequest getTrainGroupRequest(final DateTime startDateTime, final RequestQueue requestQueue) {

        sortListButton.setVisibility(ImageButton.INVISIBLE);

        urlForm = "https://ojp.nationalrail.co.uk/service/timesandfares/%s/%s/%s/dep";

        urlDateTimeString = urlDateTimeFormat.print(startDateTime);

        url = String.format(urlForm, startStation, endStation, urlDateTimeString);

        System.out.println(url);

        final DateTime[] departureDateTime = {startDateTime};

        //Request a string response from the provided URL.
        return new TrainGroupRequest(Request.Method.GET,
                url,
                previousTimeInt,
                departureDateTime,
                startDateTime,
                railcard,
                startStation,
                endStation,
                initialDateTime,
                totalTime,
                new Response.Listener<TrainGroupRequest.TrainGroup>() {
                    @SuppressLint({"DefaultLocale", "SetTextI18n"})
                    @Override
                    public void onResponse(TrainGroupRequest.TrainGroup response) {

                        timeoutCount = 0;

                        // Start of train getting business
                        List<Train> trainGroup= response.trainGroup;

                        findViewById(R.id.fetch_text).setVisibility(TextView.INVISIBLE);
                        findViewById(R.id.progress_loader).setVisibility(ProgressBar.INVISIBLE);

                        findViewById(R.id.recycler_view).setVisibility(RecyclerView.VISIBLE);

                        for (Train myTrain : trainGroup) {


                            if ((trains.size() < 4) || !trains.subList(0, 4).contains(myTrain)) {
                                trains.add(0, myTrain);
                                mAdapter.notifyItemInserted(0);
                                mRecyclerView.scrollToPosition(0);
                            }

                        }

                        DateTime lastDateTime = trains.get(0).getDepartureDateTime();

                        System.out.println(reportTimeText.getText());

                        percentageInt = response.percentageInt;

                        reportTimeText.setText(percentageInt + "% complete");

                        if (endDateTime.isAfter(lastDateTime)) {
                            if (!killRequests) {
                                TrainGroupRequest sr = getTrainGroupRequest(lastDateTime, requestQueue);
                                requestQueue.add(sr);
                            } else {
                                System.out.println("Back button was pressed, so request queue was killed");
                            }
                        } else {
                            System.out.println(String.format("Exited because:\nCurrent date: %s\nis after\nEnd date: %s",
                                    startDateTime.toString(), endDateTime.toString()));
                            progressBar.setVisibility(ProgressBar.INVISIBLE);
                            reportTimeText.setText("100% Complete");

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(GetTimesActivity2.this);
                            if(!prefs.getBoolean("firstTime", false)) {
                                explainTicketLongClick();
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("firstTime", true);
                                editor.apply();
                            }
                            sortListButton.setVisibility(ImageButton.VISIBLE);
                            sortListButton.setOnClickListener(sortButtonOnClickListener);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                if (timeoutCount < 3) {
                    TrainGroupRequest redoRequest;
                    if (trains.size() > 0) {
                        redoRequest = getTrainGroupRequest(trains.get(0).getDepartureDateTime(), requestQueue);
                        requestQueue.add(redoRequest);
                        redoButton.setVisibility(ImageButton.GONE);
                        progressBar.setIndeterminate(true);
                    } else {
                        requestQueue.add(firstTrainGroupRequest);
                    }

                    timeoutCount += 1;

                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setProgress(percentageInt);
                    progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
                    reportTimeText.setText(R.string.network_failure);
                    redoButton.setVisibility(ImageButton.VISIBLE);

                    redoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TrainGroupRequest redoRequest;
                            if (trains.size() > 0) {
                                redoRequest = getTrainGroupRequest(trains.get(0).getDepartureDateTime(), requestQueue);
                                requestQueue.add(redoRequest);
                                redoButton.setVisibility(ImageButton.GONE);
                                progressBar.setIndeterminate(true);
                            } else {
                                requestQueue.add(firstTrainGroupRequest);
                            }


                        }
                    });

                    sortListButton.setVisibility(ImageButton.VISIBLE);
                    sortListButton.setOnClickListener(sortButtonOnClickListener);
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }

    public void explainTicketLongClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GetTimesActivity2.this);
        builder.setMessage("When journeys have loaded, you can see the ticket online by pressing and holding the ticket")
                .setIcon(R.drawable.web_icon)
                .setTitle("Buying Tickets")
                .setPositiveButton("Understood", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog dialog = builder.create();

        dialog.show();
    }
}
