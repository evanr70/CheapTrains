package com.example.evan.cheaptrains;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.evan.cheaptrains.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class GetTimesActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    List<Train> trains = new ArrayList<>();

    SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat outputDateFormat = new SimpleDateFormat("ddMMyy");

    SimpleDateFormat inputTimeFormat = new SimpleDateFormat("hh:mm");
    SimpleDateFormat outputTimeFormat = new SimpleDateFormat("hhmm");

    SimpleDateFormat inputDateTimeFormat = new SimpleDateFormat("dd/MM/yyyyhh:mm");
    SimpleDateFormat outputDateTimeFormat = new SimpleDateFormat("ddMMyyhhmm");

    String startStation;
    String endStation;

    String startDateString;
    String endDateString;
    String currentDateString;

    String startTimeString;
    String endTimeString;
    String currentTimeString;
    String startUrlTimeString;

    Date startDate;
    Date endDate;
    Date currentDate;

    Date startTime;
    Date endTime;
    Date currentTime;

    String urlForm;
    String url;

    String urlDateString;
    String urlTimeString;

    boolean endReached;
    Date endDateTime;

    int previousTimeInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_times);

        Intent intent = getIntent();

        startStation = "PAD";
        endStation = "AGV";

        startDateString = intent.getStringExtra("START_DATE");
//        String startDateString = "13/08/18";
        endDateString = "16/08/2018";

        startTimeString = intent.getStringExtra("START_TIME");
//        String startTimeString = "08:00";
        endTimeString = "13:00";

        try {
            System.out.println("Assigned endTimeString: " + endTimeString);
            System.out.println("Assigned endDateString: " + endDateString);
            endDateTime = inputDateTimeFormat.parse(endDateString + endTimeString);
            System.out.println("endDateTime at assignment: " + endDateTime.toString());
        } catch (ParseException p) {
            p.printStackTrace();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Improve performance for static sized RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Specify an adapter
        mAdapter = new MyAdapter(trains);
        mRecyclerView.setAdapter(mAdapter);

        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        endReached = false;
        previousTimeInt = 0;

        StringRequest stringRequest = getStringRequest(startDateString, startTimeString, queue);


        // Add the request to the request queue
        queue.add(stringRequest);

    }

    @NonNull
    private StringRequest getStringRequest(final String date, String time, final RequestQueue requestQueue) {

        urlForm = "https://ojp.nationalrail.co.uk/service/timesandfares/%s/%s/%s/%s/dep";

//        System.out.println(date);
//        System.out.println(time);

        try {
            startDate = inputDateFormat.parse(date);
            endDate = inputDateFormat.parse(endDateString);

            startTime = inputTimeFormat.parse(time);
            endTime = inputTimeFormat.parse(endTimeString);

            urlDateString = String.format("%06d", Integer.parseInt(outputDateFormat.format(startDate)));
            urlTimeString = String.format("%04d", Integer.parseInt(outputTimeFormat.format(startTime)));

            startUrlTimeString = urlTimeString;

        } catch (ParseException e) {
            System.out.println("Couldn't parse date");
            System.out.println("Assuming date already parsed");
//            e.printStackTrace();
            urlDateString = String.format("%06d", Integer.parseInt(date));
            urlTimeString = String.format("%04d", Integer.parseInt(time));

        }

        url = String.format(urlForm, startStation, endStation, urlDateString, urlTimeString);

        //Request a string response from the provided URL.
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onResponse(String response) {

                        // Start of train getting business
                        Document doc = Jsoup.parse(response);

                        String[] idList = new String[4];
                        for (int i = 0; i < idList.length; i++) {
                            idList[i] = "jsonJourney-4-" + (i + 1);
                        }

                        boolean dateModified = false;

                        for (String id : idList) {
                            Elements title = doc.select("#" + id);
                            String text = title.html();

                            JSONObject journeyBreakdown = new JSONObject();
                            JSONArray singleJsonFareBreakdowns = new JSONArray();

                            String departureTime;

                            double ticketPriceTotal = 0;
                            List<String> ticketTypes = new ArrayList<>();
                            String ticketType;

                            String date;


                            // Get a train object to store values in
                            Train train = new Train();

                            try {

                                // Root object
                                JSONObject obj = new JSONObject(text);

                                // Larger Collections
                                journeyBreakdown = obj.getJSONObject("jsonJourneyBreakdown");
                                singleJsonFareBreakdowns = obj.getJSONArray("singleJsonFareBreakdowns");

                                // Parameters from journeyBreakdown
                                departureTime = journeyBreakdown.getString("departureTime");

                                int departureTimeInt = Integer.parseInt(departureTime.replace(":", ""));

                                if (departureTimeInt < previousTimeInt) {
                                    dateModified = true;
                                    try {
                                        Date newDate = outputDateFormat.parse(urlDateString);
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(newDate);
                                        c.add(Calendar.DATE, 1);
                                        newDate = c.getTime();
                                        urlDateString = outputDateFormat.format(newDate);
                                        System.out.println("\n\n\n");
                                        System.out.println(String.format("urlDateString set line 224: %s\ndepartureTimeInt: %d\nStartUrlTimeString: %s", urlDateString, departureTimeInt, startUrlTimeString));
                                        System.out.println("\n\n\n");
                                    } catch (ParseException error) {
                                        error.printStackTrace();
                                        System.out.println("\n\n\n");
                                        System.out.println("DATE COULD NOT BE UPDATED");
                                        System.out.println("\n\n\n");
                                    }

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
//                                System.out.println(ticketTypes.size());
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
                                e.printStackTrace();
                                ticketType = "Not Found";
                                departureTime = "Not Found";
                                train.setPrice(String.format("£%.2f", ticketPriceTotal));
                            }

                            train.setPrice(String.format("£%.2f", ticketPriceTotal));
                            train.setTime(departureTime);
                            train.setType(ticketType);
                            try {
                                Date trainDate = outputDateFormat.parse(urlDateString);
                                String trainDateString = inputDateFormat.format(trainDate);
                                train.setDate(trainDateString);
                            } catch (ParseException p) {
                                p.printStackTrace();
                                System.out.println("COULDN'T PARSE URL DATE");
                            }

                            if (!trains.contains(train)) {
                                trains.add(train);
                                mAdapter.notifyItemInserted(trains.size() - 1);
                            }
                        }

                        endReached = false;

                        String lastTime = trains.get(trains.size() - 1).getTime();
                        try {
                            currentTime = inputTimeFormat.parse(lastTime);
                            String lastTimeString = outputTimeFormat.format(currentTime);
                            urlTimeString = String.format("%04d", Integer.parseInt(lastTimeString) + 1);
                            System.out.println(currentTime);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        try {
                            Date urlDate = outputDateTimeFormat.parse(urlDateString + urlTimeString);
                            System.out.println(String.format("urlDateString: %s\nurlTimeString: %s", urlDateString, urlTimeString));
                            System.out.println("program end date: " + endDateTime.toString());
                            System.out.println("program url date: " + urlDate.toString());

                            if (endDateTime.after(urlDate)) {
                                StringRequest sr = getStringRequest(urlDateString, urlTimeString, requestQueue);
                                requestQueue.add(sr);
                            } else {
                                System.out.println(String.format("Exited because:\nCurrent date: %s\nis after\nEnd date: %s", urlDate.toString(), endDateTime.toString()));
                            }
                        } catch (ParseException p) {
                            p.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
    }
}
