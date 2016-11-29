package org.gowind.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by shiv.loka on 10/3/16.
 */

public class User implements Parcelable {
    private String name;
    private String phoneNumber;
    private int rating;
    private String email;
    private Ride ride;

    public User(Parcel in) {
        name = in.readString();
        phoneNumber = in.readString();
        rating = in.readInt();
        email = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User() {

    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRating() {
        return String.valueOf(rating);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeInt(rating);
        dest.writeString(email);
    }
}
