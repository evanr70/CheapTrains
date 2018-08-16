package com.example.evan.cheaptrains;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
    DateTimeFormatter trainDateFormat = DateTimeFormat.forPattern("dd/MM/yyyy");
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
    StringRequest firstStringRequest;

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

        railcard = intent.getExtras().getBoolean("RAIL_CARD");

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
                                                  Toast.makeText(GetTimesActivity2.this, "Sorted by price", Toast.LENGTH_SHORT).show();
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
        firstStringRequest = getStringRequest(startDateTime, requestQueue);
        firstStringRequest.setTag(this);
        // Add the request to the request queue
        requestQueue.add(firstStringRequest);

    }

    @NonNull
    private StringRequest getStringRequest(final DateTime startDateTime, final RequestQueue requestQueue) {

        urlForm = "https://ojp.nationalrail.co.uk/service/timesandfares/%s/%s/%s/dep";

        urlDateTimeString = urlDateTimeFormat.print(startDateTime);

        url = String.format(urlForm, startStation, endStation, urlDateTimeString);

        System.out.println(url);

        final DateTime[] departureDateTime = {startDateTime};

        //Request a string response from the provided URL.
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @SuppressLint({"DefaultLocale", "SetTextI18n"})
                    @Override
                    public void onResponse(String response) {

                        timeoutCount = 0;

                        // Start of train getting business
                        Document doc = Jsoup.parse(response);

                        String[] idList = new String[6];
                        for (int i = 0; i < idList.length; i++) {
                            idList[i] = "jsonJourney-4-" + (i + 1);
                        }

                        for (String id : idList) {
                            boolean addTrain = true;

                            Elements title = doc.select("#" + id);

                            String text = title.html();

                            JSONObject journeyBreakdown;
                            JSONArray singleJsonFareBreakdowns;

                            String arrivalTime;
                            String departureTime;

                            double ticketPriceTotal = 0;
                            List<String> ticketTypes = new ArrayList<>();
                            String ticketType;

                            // Get a train object to store values in
                            Train train = new Train();

                            try {

                                // Root object
                                JSONObject obj = new JSONObject(text);

                                // Larger Collections
                                journeyBreakdown = obj.getJSONObject("jsonJourneyBreakdown");
                                singleJsonFareBreakdowns = obj.getJSONArray("singleJsonFareBreakdowns");

                                // Parameters from journeyBreakdown
                                arrivalTime = journeyBreakdown.getString("arrivalTime");
                                departureTime = journeyBreakdown.getString("departureTime");

                                int departureTimeInt = Integer.parseInt(departureTime.replace(":", ""));

                                if ((departureTimeInt - previousTimeInt < -60)) {
                                    departureDateTime[0] = startDateTime.plusDays(1);
                                }

                                previousTimeInt = departureTimeInt;

                                // Parameters from jsonSingleFareBreakdowns
                                // Iterate over each fare in singleJsonFareBreakdowns

                                for (int i = 0; i < singleJsonFareBreakdowns.length(); i++) {
                                    // Get the ticket at index i
                                    JSONObject fareObject = singleJsonFareBreakdowns.getJSONObject(i);

                                    // Add up ticket prices
                                    ticketPriceTotal += Double.parseDouble(fareObject.getString("ticketPrice"));

                                    // Add new ticket types to ticketTypes list
                                    if (!ticketTypes.contains(fareObject.getString("ticketType"))) {
                                        ticketTypes.add(fareObject.getString("ticketType"));
                                    }
                                }

                                // build ticketType string for output
                                StringBuilder sb = new StringBuilder();
                                for (String s : ticketTypes) {
                                    if (ticketTypes.size() > 1) {
                                        sb.append(s);
                                        sb.append("\n");
                                    } else {
                                        sb.append(s);
                                    }
                                }

                                ticketType = sb.toString();
                            } catch (JSONException e) {
                                break;
                            }

                            if (railcard) {
                                ticketPriceTotal *= 0.667;
                            }

                            train.setPrice(String.format("£%.2f", ticketPriceTotal));

                            if (train.getPrice().equals("£0.00")) {
                                addTrain = false;
                            }

                            train.setArrivalTime(arrivalTime);
                            train.setDepartureTime(departureTime);
                            train.setType(ticketType);

                            train.setDate(trainDateFormat.print(departureDateTime[0]));

                            DateTime trainDateTime = departureDateTime[0];

                            trainDateTime = trainDateTime.hourOfDay().setCopy(Integer.parseInt(departureTime.substring(0, 2)));
                            trainDateTime = trainDateTime.minuteOfHour().setCopy(Integer.parseInt(departureTime.substring(3)));

                            train.setDepartureDateTime(trainDateTime);

                            train.setStartStation(startStation);
                            train.setEndStation(endStation);

                            if (addTrain) {
                                if ((trains.size() < 4) || !trains.subList(0, 4).contains(train)) {
                                    train.setUrl();
                                    trains.add(0, train);
                                    mAdapter.notifyItemInserted(0);
                                    mRecyclerView.scrollToPosition(0);
                                }
                            }
                        }

                        DateTime lastDateTime = trains.get(0).getDepartureDateTime();

                        System.out.println(reportTimeText.getText());

                        Seconds secondsElapsed = Seconds.secondsBetween(initialDateTime, lastDateTime);

                        float percentageComplete = (float) secondsElapsed.getSeconds() / totalTime * 100;
                        percentageInt = percentageComplete > 100 ? 100 : (int) percentageComplete;

                        reportTimeText.setText(percentageInt + "% complete");

                        if (endDateTime.isAfter(lastDateTime)) {
                            if (!killRequests) {
                                StringRequest sr = getStringRequest(lastDateTime, requestQueue);
                                requestQueue.add(sr);
                            } else {
                                System.out.println("Back button was pressed, so request queue was killed");
                            }
                        } else {
                            System.out.println(String.format("Exited because:\nCurrent date: %s\nis after\nEnd date: %s",
                                    startDateTime.toString(), endDateTime.toString()));
                            progressBar.setVisibility(ProgressBar.GONE);

                            sortListButton.setOnClickListener(sortButtonOnClickListener);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                if (timeoutCount < 3) {
                    StringRequest redoRequest;
                    if (trains.size() > 0) {
                        redoRequest = getStringRequest(trains.get(0).getDepartureDateTime(), requestQueue);
                        requestQueue.add(redoRequest);
                        redoButton.setVisibility(ImageButton.GONE);
                        progressBar.setIndeterminate(true);
                    } else {
                        requestQueue.add(firstStringRequest);
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
                            StringRequest redoRequest;
                            if (trains.size() > 0) {
                                redoRequest = getStringRequest(trains.get(0).getDepartureDateTime(), requestQueue);
                                requestQueue.add(redoRequest);
                                redoButton.setVisibility(ImageButton.GONE);
                                progressBar.setIndeterminate(true);
                            } else {
                                requestQueue.add(firstStringRequest);
                            }


                        }
                    });

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
}
