package com.oxlon.evan.cheaptrains;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrainGroupRequest extends Request {

    private int previousTimeInt;
    private DateTime[] departureDateTime;
    private  DateTime startDateTime;
    boolean railcard;
    private DateTimeFormatter trainDateFormat = DateTimeFormat.forPattern("dd/MM/yyyy");
    private String startStation;
    private String endStation;
    private List<Train> trains = new ArrayList<>();
    private DateTime initialDateTime;
    private int totalTime;

    private final Response.Listener<TrainGroup> listener;


    public class TrainGroup{
        List<Train> trainGroup;
        int percentageInt;

        TrainGroup(List<Train> trainGroup, int percentageInt){
            this.trainGroup = trainGroup;
            this.percentageInt = percentageInt;
        }
    }


    TrainGroupRequest(int method,
                      String url,
                      int previousTimeInt,
                      DateTime[] departureDateTime,
                      DateTime startDateTime,
                      boolean railcard,
                      String startStation,
                      String endStation,
                      DateTime initialDateTime,
                      int totalTime,
                      Response.Listener<TrainGroup> trainListener,
                      @Nullable Response.ErrorListener listener) {
        super(method, url, listener);
        this.listener = trainListener;
        this.previousTimeInt = previousTimeInt;
        this.departureDateTime = departureDateTime;
        this.startDateTime = startDateTime;
        this.railcard = railcard;
        this.startStation = startStation;
        this.endStation = endStation;
        this.initialDateTime = initialDateTime;
        this.totalTime = totalTime;

    }




    @SuppressLint("DefaultLocale")
    @Override
    protected Response<TrainGroup> parseNetworkResponse(NetworkResponse response) {

        // Start of train getting business

        String json;
        try {
            json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            json = "";
        }

        Document doc = Jsoup.parse(json);

        System.out.println(Arrays.toString(response.data));

        String[] idList = new String[6];
        for (int i = 0; i < idList.length; i++) {
            idList[i] = "jsonJourney-4-" + (i + 1);
        }

        System.out.println("HELP ME HELP ME HELP ME");

        for (String id : idList) {
            boolean addTrain = true;

            Elements title = doc.select("#" + id);

            String text = title.html();

            System.out.println(text);

            JSONObject journeyBreakdown;
            JSONArray singleJsonFareBreakdowns;

            String arrivalTime;
            String departureTime;

            double ticketPriceTotal = 0;
            List<String> ticketTypes = new ArrayList<>();
            String ticketType;

            // Get a train object to store values in
            Train train = new Train();

            int durationHours;
            int durationMinutes;
            int changes;
            try {

                // Root object
                JSONObject obj = new JSONObject(text);

                // Larger Collections
                journeyBreakdown = obj.getJSONObject("jsonJourneyBreakdown");
                singleJsonFareBreakdowns = obj.getJSONArray("singleJsonFareBreakdowns");

                // Parameters from journeyBreakdown
                arrivalTime = journeyBreakdown.getString("arrivalTime");
                departureTime = journeyBreakdown.getString("departureTime");
                durationHours = journeyBreakdown.getInt("durationHours");
                durationMinutes = journeyBreakdown.getInt("durationMinutes");
                changes = journeyBreakdown.getInt("changes");


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

            train.setDurationHours(durationHours);
            train.setDurationMinutes(durationMinutes);
            train.setChanges(changes);

            train.setUrl();
            if (addTrain) {
                trains.add(train);
            }
        }

        DateTime lastDateTime = trains.get(0).getDepartureDateTime();

        Seconds secondsElapsed = Seconds.secondsBetween(initialDateTime, lastDateTime);

        float percentageComplete = (float) secondsElapsed.getSeconds() / totalTime * 100;
        int percentageInt = percentageComplete > 100 ? 100 : (int) percentageComplete;

        TrainGroup group = new TrainGroup(trains, percentageInt);

        return Response.success(group, HttpHeaderParser.parseCacheHeaders(response));

    }

    @Override
    protected void deliverResponse(Object response) {
        System.out.println("THIS IS THE START OF IT\n--------------\n" +
                        response.toString());
        listener.onResponse((TrainGroup) response);
    }




    @Override
    public int compareTo(@NonNull Object o) {
        return 0;
    }
}
