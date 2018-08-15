package com.example.evan.cheaptrains;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.DateTime;
import org.joda.time.MonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // TODO: Create buttons and forms for endDate to be passed through intent. Remeber to check that start date is before end date + default 1 day?

    private CheckBox railcard;
    private DateTimeFormatter date = DateTimeFormat.forPattern("dd/MM/yy");
    private DateTimeFormatter time = DateTimeFormat.forPattern("HH:mm");
    private TextView timeText;
    private ImageButton timeButton;
    private TextView dateText;
    private ImageButton dateButton;
    private Button submitButton;
    private TimePickerDialog.OnTimeSetListener timeSetListener;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private AutoCompleteTextView startStationText;
    private AutoCompleteTextView endStationText;
    private Map<String, String> stations;
    private ArrayList<String> stationNames = new ArrayList<>();
    private ColorStateList textColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        timeButton = findViewById(R.id.time_button);

        dateText = findViewById(R.id.date_text);
        dateButton = findViewById(R.id.date_button);

        submitButton = findViewById(R.id.submit_button);

        startStationText = findViewById(R.id.start_station);
        endStationText = findViewById(R.id.end_station);

        textColors = startStationText.getTextColors();

        String dayOfMonth = String.format("%02d", new MonthDay().getDayOfMonth());

        ((TextView) findViewById(R.id.today_text)).setText(dayOfMonth);

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

        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                @SuppressLint("DefaultLocale") String date = String.format("%02d:%02d",
                        hour, minute);
                timeText.setText(date);
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

        if (timeText.getText().equals("Time has not been set")) {
            timeText.setTextColor(Color.RED);
            inputsValid = false;
        } else {
            timeText.setTextColor(textColors);
        }

        if (dateText.getText().equals("Date has not been set")) {
            dateText.setTextColor(Color.RED);
            inputsValid = false;
        } else {
            dateText.setTextColor(textColors);
        }


        return inputsValid;

    }

    public void setDateToday(View view) {
        DateTime dateTime = new DateTime();
        dateText.setText(date.print(dateTime));
        timeText.setText(time.print(dateTime));
    }

    public void setDateTomorrow(View view) {
        DateTime dateTime = new DateTime();
        dateTime = dateTime.plusDays(1);
        dateText.setText(date.print(dateTime));
        timeText.setText(time.print(dateTime));
    }
}
