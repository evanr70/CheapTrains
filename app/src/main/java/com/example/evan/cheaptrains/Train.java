package com.example.evan.cheaptrains;

import org.joda.time.DateTime;

import java.util.Date;

public class Train {
    private String time;
    private String type;
    private String price;
    private String date;
    private DateTime dateTime;
    private String startStation;
    private String endStation;
    private String url;

    Train (){}

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
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
                time.replace(":", ""));
    }

    @Override
    public boolean equals(Object other){

        if (other instanceof Train){
            if (dateTime.equals(((Train) other).getDateTime())) {
                System.out.println("While checking the trains for similarity, they were equal");
                System.out.println(dateTime.toString());
                System.out.println(((Train) other).getDateTime());
                return true;
            }
            return false;
        }
        return false;
    }


}
