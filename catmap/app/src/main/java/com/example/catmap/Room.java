package com.example.catmap;

import org.json.JSONException;
import org.json.JSONObject;

public class Room {
    public String name;
    public String address;
    public double latitude;
    public double longitude;

    public void fromJSON(JSONObject jsonObject) {
        try {
            name = jsonObject.getString("name");
            address = jsonObject.getString("address");
            latitude = jsonObject.getDouble("latitude");
            longitude = jsonObject.getDouble("longitude");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
