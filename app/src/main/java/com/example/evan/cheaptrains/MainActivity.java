package com.example.evan.cheaptrains;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.MonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // TODO: Create buttons and forms for endDate to be passed through intent. Remeber to check that start date is before end date + default 1 day?

    private CheckBox railcard;
    private DateTimeFormatter date = DateTimeFormat.forPattern("dd/MM/yy");
    private DateTimeFormatter time = DateTimeFormat.forPattern("HH:mm");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyHH:mm");

    private TextView timeText;
    private TextView endTimeText;

    private ImageButton timeButton;
    private ImageButton endTimeButton;

    private TextView dateText;
    private TextView endDateText;

    private ImageButton dateButton;
    private ImageButton endDateButton;

    private Button submitButton;

    private ImageButton endTodayButton;
    private ImageButton endTomorrowButton;

    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private TimePickerDialog.OnTimeSetListener endTimeSetListener;

    private DatePickerDialog.OnDateSetListener dateSetListener;
    private DatePickerDialog.OnDateSetListener endDateSetListener;

    private AutoCompleteTextView startStationText;
    private AutoCompleteTextView endStationText;

    private Map<String, String> stations;
    private ArrayList<String> stationNames = new ArrayList<>();

    private ColorStateList textColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Journey Details");

        try {
            stations = getStations();
            Set<String> keys = stations.keySet();
            System.out.println(keys);
            System.out.println(stationNames);
            stationNames.addAll(keys);
        } catch (IOException e) {
            e.printStackTrace();
        }

        railcard = findViewById(R.id.railcard);

        timeText = findViewById(R.id.time_text);
        endTimeText = findViewById(R.id.end_time_text);

        timeButton = findViewById(R.id.time_button);
        endTimeButton = findViewById(R.id.end_time_button);

        dateText = findViewById(R.id.date_text);
        endDateText = findViewById(R.id.end_date_text);

        endTodayButton = findViewById(R.id.end_today);
        endTomorrowButton = findViewById(R.id.end_tomorrow);

        dateButton = findViewById(R.id.date_button);
        endDateButton = findViewById(R.id.end_date_button);

        submitButton = findViewById(R.id.submit_button);

        startStationText = findViewById(R.id.start_station);
        endStationText = findViewById(R.id.end_station);

        textColors = startStationText.getTextColors();

        String dayOfMonth = String.format("%02d", new MonthDay().getDayOfMonth());

        ((TextView) findViewById(R.id.today_text)).setText(dayOfMonth);
        ((TextView) findViewById(R.id.end_today_text)).setText(dayOfMonth);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                stationNames);

        startStationText.setAdapter(adapter);
        endStationText.setAdapter(adapter);


        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);

                TimePickerDialog dialog = new TimePickerDialog(
                        MainActivity.this,
                        timeSetListener,
                        hour, minute, true);

                dialog.show();
            }
        });

        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);

                TimePickerDialog dialog = new TimePickerDialog(
                        MainActivity.this,
                        endTimeSetListener,
                        hour, minute, true);

                dialog.show();
            }
        });

        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                @SuppressLint("DefaultLocale") String date = String.format("%02d:%02d",
                        hour, minute);
                timeText.setText(date);

            }
        };

        endTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                @SuppressLint("DefaultLocale") String date = String.format("%02d:%02d",
                        hour, minute);
                endTimeText.setText(date);
            }
        };

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        dateSetListener,
                        year, month, day);
                dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
                Calendar calEnd = Calendar.getInstance();
                calEnd.add(Calendar.MONTH, 3);
                dialog.getDatePicker().setMaxDate(calEnd.getTimeInMillis());
                dialog.show();
            }
        });

        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();

                int year;
                int month;
                int day;

                if (!dateText.getText().toString().equals("Date has not been set") && !timeText.getText().toString().equals("Time has not been set")) {
                    year = Integer.parseInt(dateText.getText().toString().substring(6, 8));
                    month = Integer.parseInt(dateText.getText().toString().substring(3, 5));
                    day = Integer.parseInt(dateText.getText().toString().substring(0,2));

                    cal.set(Calendar.YEAR, 2000 + year);
                    cal.set(Calendar.MONTH, month - 1);
                    cal.set(Calendar.DAY_OF_MONTH, day);

                } else {

                    year = cal.get(Calendar.YEAR);
                    month = cal.get(Calendar.MONTH);
                    day = cal.get(Calendar.DAY_OF_MONTH);
                }

                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        endDateSetListener,
                        year, month, day);
                dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
                Calendar calEnd = Calendar.getInstance();
                calEnd.add(Calendar.MONTH, 3);
                dialog.getDatePicker().setMaxDate(calEnd.getTimeInMillis());
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month += 1;
                String yearString = Integer.toString(year).substring(2);
                System.out.println(yearString);
                @SuppressLint("DefaultLocale") String date = String.format("%02d/%02d/%s",
                        day, month, yearString);
                dateText.setText(date);

                DateTime today = new DateTime().withTime(23, 59, 0, 0);

                new DateTime();

                DateTime tomorrow = today.plusDays(1);

                DateTime chosen = new DateTime().withDate(year, month, day).withTimeAtStartOfDay();

                if (chosen.isAfter(today)) {
                    redToday();
                } else {
                    blackToday();
                }

                if (chosen.isAfter(tomorrow)) {
                    redTomorrow();
                } else {
                    blackTomorrow();
                }

            }
        };

        endDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month += 1;
                String yearString = Integer.toString(year).substring(2);
                System.out.println(yearString);
                @SuppressLint("DefaultLocale") String date = String.format("%02d/%02d/%s",
                        day, month, yearString);
                endDateText.setText(date);
            }
        };

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputsValid()) {
                    sendMessage(view);
                }
            }
        });

    }

    public void redToday(){
        endTodayButton.setColorFilter(Color.RED);
        endTodayButton.setClickable(false);
    }

    public void blackToday() {
        endTodayButton.setColorFilter(Color.BLACK);
        endTodayButton.setClickable(true);
    }

    public void redTomorrow(){
        endTomorrowButton.setColorFilter(Color.RED);
        endTomorrowButton.setClickable(false);
    }

    public void blackTomorrow() {
        endTomorrowButton.setColorFilter(Color.BLACK);
        endTomorrowButton.setClickable(true);
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    /* Called when the user taps the Send button */
    public void sendMessage(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, GetTimesActivity2.class);

        intent.putExtra("START_TIME", timeText.getText().toString().replace(":", ""));
        intent.putExtra("START_DATE", dateText.getText().toString().replace("/", ""));

        intent.putExtra("END_TIME", endTimeText.getText().toString().replace(":", ""));
        intent.putExtra("END_DATE", endDateText.getText().toString().replace("/", ""));

        intent.putExtra("START_STATION", stations.get(startStationText.getText().toString()));
        intent.putExtra("END_STATION", stations.get(endStationText.getText().toString()));

        intent.putExtra("RAIL_CARD", railcard.isChecked());

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);


        startActivity(intent);

    }

    private Map<String, String> getStations() throws IOException {

        Map<String, String> stations = new HashMap<>();
        InputStream is = getResources().openRawResource(R.raw.station_codes);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        reader.readLine();
        String line;
        String[] splitLine;
        while ((line = reader.readLine()) != null) {
            splitLine = line.split(",");
            stations.put(splitLine[0], splitLine[1]);
        }
        System.out.println(stations);
        return stations;


    }

    private boolean inputsValid() {
        boolean inputsValid = true;

        // Although it might seem less efficient to check all of the fields rather than returning false at the first issue,
        // this approach allows required changes to be highlighted
        if (!stationNames.contains(startStationText.getText().toString())) {
            startStationText.setTextColor(Color.RED);
            inputsValid = false;
        } else {
            startStationText.setTextColor(textColors);
        }

        if (!stationNames.contains(endStationText.getText().toString())) {
            endStationText.setTextColor(Color.RED);
            inputsValid = false;
        } else {
            endStationText.setTextColor(textColors);
        }

        if (startStationText.getText().equals(endStationText.getText())) {
            startStationText.setTextColor(Color.RED);
            endStationText.setTextColor(Color.RED);
            return false;
        } else {
            startStationText.setTextColor(textColors);
            endStationText.setTextColor(textColors);
        }

        if (timeText.getText().equals("Time has not been set")) {
            timeText.setTextColor(Color.RED);
            inputsValid = false;
        } else {
            timeText.setTextColor(textColors);
        }

        if (endTimeText.getText().equals("Time has not been set")) {
            endTimeText.setTextColor(Color.RED);
            inputsValid = false;
        } else {
            endTimeText.setTextColor(textColors);
        }

        if (dateText.getText().equals("Date has not been set")) {
            dateText.setTextColor(Color.RED);
            inputsValid = false;
        } else {
            dateText.setTextColor(textColors);
        }

        if (endDateText.getText().equals("Date has not been set")) {
            endDateText.setTextColor(Color.RED);
            inputsValid = false;
        } else {
            endDateText.setTextColor(textColors);
        }

        // Check dates for issues

        try {
            DateTime start = dateTimeFormatter.parseDateTime(dateText.getText().toString() + timeText.getText().toString());
            DateTime end = dateTimeFormatter.parseDateTime(endDateText.getText().toString() + endTimeText.getText().toString());

            if (end.isBefore(start)) {
                String tempDate = dateText.getText().toString();
                String tempTime = timeText.getText().toString();

                dateText.setText(endDateText.getText().toString());
                timeText.setText(endTimeText.getText().toString());

                endDateText.setText(tempDate);
                endTimeText.setText(tempTime);

                Toast.makeText(this, "Dates were swapped", Toast.LENGTH_SHORT).show();
            }

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            inputsValid = false;
        }

        return inputsValid;

    }

    public void setDateToday(View view) {
        DateTime dateTime = new DateTime();
        dateText.setText(date.print(dateTime));
        timeText.setText(time.print(dateTime));
        blackToday();
    }

    public void setEndDateToday(View view) {
        DateTime dateTime = new DateTime();
        endDateText.setText(date.print(dateTime));
        endTimeText.setText(R.string.nearly_midnight);
    }

    public void setDateTomorrow(View view) {
        DateTime dateTime = new DateTime();
        dateTime = dateTime.plusDays(1);
        dateText.setText(date.print(dateTime));
        timeText.setText(R.string.midnight);
        redToday();
        blackTomorrow();
    }

    public void setEndDateTomorrow(View view) {
        DateTime dateTime = new DateTime();
        dateTime = dateTime.plusDays(1);
        endDateText.setText(date.print(dateTime));
        endTimeText.setText(R.string.nearly_midnight);
    }
}
