package com.rocket.indrivefilter;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class FilterConfig {
    public boolean enabled = false;
    public int minPrice = 600;
    public double maxPickupKm = 2.0;
    public double minTripKm = 1.0;
    public double maxTripKm = 30.0;
    public List<String> whitelistDistricts = new ArrayList<String>();
    public List<String> blacklistDistricts = new ArrayList<String>();
    public boolean requireCash = false;
    public boolean autoAccept = false;

    private static final String PREF = "cfg";
    private static final String KEY = "json";

    public static FilterConfig load(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String s = sp.getString(KEY, null);
        if (s == null) return new FilterConfig();
        try {
            JSONObject o = new JSONObject(s);
            FilterConfig c = new FilterConfig();
            c.enabled = o.optBoolean("enabled", false);
            c.minPrice = o.optInt("minPrice", 600);
            c.maxPickupKm = o.optDouble("maxPickupKm", 2.0);
            c.minTripKm = o.optDouble("minTripKm", 1.0);
            c.maxTripKm = o.optDouble("maxTripKm", 30.0);
            c.requireCash = o.optBoolean("requireCash", false);
            c.autoAccept = o.optBoolean("autoAccept", false);
            c.whitelistDistricts = jsonToList(o.optJSONArray("whitelist"));
            c.blacklistDistricts = jsonToList(o.optJSONArray("blacklist"));
            return c;
        } catch (Exception e) { return new FilterConfig(); }
    }

    public static void save(Context ctx, FilterConfig c) {
        try {
            JSONObject o = new JSONObject();
            o.put("enabled", c.enabled);
            o.put("minPrice", c.minPrice);
            o.put("maxPickupKm", c.maxPickupKm);
            o.put("minTripKm", c.minTripKm);
            o.put("maxTripKm", c.maxTripKm);
            o.put("requireCash", c.requireCash);
            o.put("autoAccept", c.autoAccept);
            o.put("whitelist", listToJson(c.whitelistDistricts));
            o.put("blacklist", listToJson(c.blacklistDistricts));
            ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putString(KEY, o.toString()).apply();
        } catch (Exception ignored) {}
    }

    private static JSONArray listToJson(List<String> list) {
        JSONArray a = new JSONArray();
        for (String s : list) a.put(s);
        return a;
    }

    private static List<String> jsonToList(JSONArray a) {
        List<String> r = new ArrayList<String>();
        if (a == null) return r;
        for (int i = 0; i < a.length(); i++) r.add(a.optString(i));
        return r;
    }
  }
