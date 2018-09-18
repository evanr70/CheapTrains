package com.oxlon.evan.cheaptrains;

import android.annotation.SuppressLint;

import org.joda.time.DateTime;

import java.util.Objects;

public class Train {
    private String departureTime;
    private String arrivalTime;
    private String type;
    private String price;
    private String date;
    private DateTime departureDateTime;
    private String startStation;
    private String endStation;
    private String url;
    private int durationHours;
    private int durationMinutes;
    private int changes;
    private boolean expanded = false;

    Train (){}

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String time) {
        this.departureTime = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public DateTime getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(DateTime dateTime) {
        this.departureDateTime = dateTime;
    }

    public String getStartStation() {
        return startStation;
    }

    public void setStartStation(String startStation) {
        this.startStation = startStation;
    }

    public String getEndStation() {
        return endStation;
    }

    public void setEndStation(String endStation) {
        this.endStation = endStation;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl() {
        url = String.format("https://ojp.nationalrail.co.uk/service/timesandfares/%s/%s/%s/%s/dep",
                startStation,
                endStation,
                date.replace("/", ""),
                departureTime.replace(":", ""));
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getChanges() {
        return String.valueOf(changes);
    }

    public void setChanges(int changes) {
        this.changes = changes;
    }

    public String getDuration() {
        @SuppressLint("DefaultLocale") String duration = String.format("%dh %02dm", durationHours, durationMinutes);
        return duration;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public boolean equals(Object other){

        if (other instanceof Train){
            if (departureDateTime.equals(((Train) other).getDepartureDateTime())) {
                System.out.println("While checking the trains for similarity, they were equal");
                System.out.println(departureDateTime.toString());
                System.out.println(((Train) other).getDepartureDateTime());
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(departureDateTime);
    }


}
