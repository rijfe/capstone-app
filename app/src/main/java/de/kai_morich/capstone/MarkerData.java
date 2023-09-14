package de.kai_morich.capstone;

public class MarkerData {
    String date;
    Double latitude;
    Double longitude;
    String type;

    int id;

    public MarkerData(String time, Double la, Double lo, String type, int id) {
        this.date = time;
        this.type = type;
        this.longitude = lo;
        this.latitude = la;
        this.id = id;
    }
}
