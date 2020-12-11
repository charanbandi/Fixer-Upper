package com.example.datamining;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class HouseDetails implements Serializable {

    //filter for Price least to highest
    public static Comparator<HouseDetails> p1 = (o1, o2) -> {
        int x1 = (int) Math.round(o1.getPrice());
        int x2 = (int) Math.round(o2.getPrice());
        return x1 - x2;
    };
    //filter for Price highest to least
    public static Comparator<HouseDetails> p2 = (o1, o2) -> {
        int x1 = (int) Math.round(o1.getPrice());
        int x2 = (int) Math.round(o2.getPrice());
        return x2 - x1;
    };
    //filter for Area per Sqft least to highest
    public static Comparator<HouseDetails> a1 = (o1, o2) -> {
        int x1 = (int) Math.round(o1.getPrice() / o1.getArea());
        int x2 = (int) Math.round(o2.getPrice() / o2.getArea());
        return x1 - x2;
    };
    //filter for rea per Sqft highest to least
    public static Comparator<HouseDetails> a2 = (o1, o2) -> {
        int x1 = (int) Math.round(o1.getPrice() / o1.getArea());
        int x2 = (int) Math.round(o2.getPrice() / o2.getArea());
        return x2 - x1;
    };
    //filter for rent to price least to highest
    public static Comparator<HouseDetails> r1 = (o1, o2) -> {
        int x1 = (int) Math.round(10000 * (o1.getEstRent() / o1.getPrice()));//multiplying by 10000 since we are dealing with values below 1.00
        int x2 = (int) Math.round(10000 * (o2.getEstRent() / o2.getPrice()));
        return x1 - x2;
    };
    //filter for rent to price highest to least
    public static Comparator<HouseDetails> r2 = (o1, o2) -> {
        int x1 = (int) Math.round(10000 * (o1.getEstRent() / o1.getPrice()));
        int x2 = (int) Math.round(10000 * (o2.getEstRent() / o2.getPrice()));
        return x2 - x1;
    };
    private String address;
    private final String locality;
    private String state;
    private String zipcode;
    private Double price;
    private Double area;
    private Double estrent;
    private Double bedroom;
    private Double bathroom;
    private final Double latitude;
    private final Double longitude;
    private final String type;
    private final String lot;
    private final int year;
    private ArrayList<Event> historicData;
    private final Double hoa;

    public HouseDetails(String type, String address, String locality, String state,
                        String zipcode, Double price, Double area, Double estrent,
                        Double bedroom, Double bathroom, Double latitude, Double longitude, String lot, int year, Double hoa) {
        this.type = type;
        this.address = address;
        this.locality = locality;
        this.state = state;
        this.zipcode = zipcode;
        //this.price = price;
        this.price = new Double(Math.round(price));
        this.area = area;
        //this.estrent = estrent;
        this.estrent = new Double(Math.round(estrent));
        this.bedroom = bedroom;
        this.bathroom = bathroom;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lot = lot;
        //this.location = new LatLng(latitude,longitude);
        this.year = year;
        this.hoa = hoa;
    }

    public void setZipCode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getHisory2String() {
        String out = "";
        for (Event e : historicData) {
            out += e.toString() + "\n";
        }
        return out;
    }

    public ArrayList<Event> getHistoricData() {
        return historicData;
    }

    public void setHistoricData(JSONArray data) {
        historicData = new ArrayList<Event>();
        try {
            for (int i = 0; i < data.length(); i++) {
                JSONObject JO = data.getJSONObject(i);
                Date date = new SimpleDateFormat("dd/MM/yyyy").parse(JO.getString("date"));
                String description = JO.getString("event");
                String priceinfo = JO.getString("price");
                historicData.add(new Event(date, description, priceinfo));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocality() {
        return locality;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipcode() {
        return zipcode;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public Double getEstRent() {
        return estrent;
    }

    public void setEstRent(Double estrent) {
        this.estrent = estrent;
    }

    public Double getBedrooms() {
        return bedroom;
    }

    public void setBedrooms(Double bedroom) {
        this.bedroom = bedroom;
    }

    public Double getBathrooms() {
        return bathroom;
    }

    public void setBathrooms(Double bathroom) {
        this.bathroom = bathroom;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getType() {
        return type;
    }

    public String getLot() {
        return lot;
    }

    public int getYear() {
        return year;
    }

    public Double getHoa() {
        return hoa;
    }

}
