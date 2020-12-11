package com.example.datamining;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * This DDC implements the structure of a transaction event to show historic data.
 */
public class Event implements Parcelable {
    private Date date;
    private String description;
    private String priceInfo;

    public Event(Date date, String description, String priceInfo){
        this.date = date;
        this.description = description;
        this.priceInfo = priceInfo;
    }

    public Date getDate() { return date; }
    public String getDescription() { return description; }
    public String getPriceInfo() { return priceInfo; }
    public String toString() {
        //date.toString() returns Wed May 06 00:00:00 PDT 2020
        String dateStr = date.toString().substring(0,date.toString().indexOf("00:")) +
                date.toString().substring(date.toString().length()-4);

        return dateStr  + "\t\t" + description + "\t\t" + priceInfo;
    }

    //write object values to parcel for storage
    public void writeToParcel(Parcel dest, int flags){
        //write all properties to the parcle
        dest.writeValue(date);
        dest.writeString(description);
        dest.writeString(priceInfo);
    }

    //constructor used for parcel
    public Event(Parcel parcel){
        //read and set saved values from parcel
        date = (Date)parcel.readValue(null);
        description = parcel.readString();
        priceInfo = parcel.readString();
    }

    //creator - used when un-parceling our parcle (creating the object)
    public static final Creator<Event> CREATOR = new Creator<Event>(){

        @Override
        public Event createFromParcel(Parcel parcel) {
            return new Event(parcel);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[0];
        }
    };

    //return hashcode of object
    public int describeContents() {
        return hashCode();
    }

}
