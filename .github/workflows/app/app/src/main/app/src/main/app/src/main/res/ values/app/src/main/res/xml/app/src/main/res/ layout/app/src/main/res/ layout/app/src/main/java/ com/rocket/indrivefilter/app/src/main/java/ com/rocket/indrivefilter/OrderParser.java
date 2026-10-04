package com.rocket.indrivefilter;

import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderParser {

    public static class Order {
        public Integer price;
        public Double pickupKm;
        public Double tripKm;
        public String district;
        public boolean cash;
        public String rawText;
    }

    private static final Pattern PRICE = Pattern.compile(
        "(\\d[\\d\\s]{1,7})\\s*(?:\\u20B8|\\u0442\\u0433|KZT|kzt)",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern PICKUP = Pattern.compile(
        "(\\d+[.,]?\\d*)\\s*(?:\\u043a\\u043c|km)",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final Pattern TRIP = Pattern.compile(
        "(\\d+[.,]?\\d*)\\s*(?:\\u043a\\u043c|km)",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final List<String> KNOWN_DISTRICTS = Arrays.asList(
        "Almaty", "Astana", "Shymkent", "Karaganda",
        "Samal", "Orbita", "Aksai", "Esil", "Saryarka", "Baikonur"
    );

    public static Order parse(AccessibilityNodeInfo root) {
        if (root == null) return null;
        StringBuilder sb = new StringBuilder();
        collectText(root, sb);
        String text = sb.toString().replace('\u00A0', ' ').replace('\n', ' ');
        if (text.trim().isEmpty()) return null;

        Order o = new Order();
        o.rawText = text;

        Matcher m = PRICE.matcher(text);
        if (m.find()) {
            try { o.price = Integer.parseInt(m.group(1).replace(" ", "")); } catch (Exception ignored) {}
        }
        m = PICKUP.matcher(text);
        if (m.find()) {
            try { o.pickupKm = Double.parseDouble(m.group(1).replace(',', '.')); } catch (Exception ignored) {}
        }
        m = TRIP.matcher(text);
        if (m.find()) {
            try { o.tripKm = Double.parseDouble(m.group(1).replace(',', '.')); } catch (Exception ignored) {}
        }
        o.cash = text.toLowerCase().contains("cash") || text.toLowerCase().contains("nal");
        o.district = extractDistrict(text);

        if (o.price == null && o.pickupKm == null && o.tripKm == null) return null;
        return o;
    }

    private static void collectText(AccessibilityNodeInfo n, StringBuilder sb) {
        if (n == null) return;
        if (n.getText() != null) sb.append(n.getText()).append(' ');
        if (n.getContentDescription() != null) sb.append(n.getContentDescription()).append(' ');
        for (int i = 0; i < n.getChildCount(); i++) {
            AccessibilityNodeInfo c = n.getChild(i);
            if (c != null) collectText(c, sb);
        }
    }

    private static String extractDistrict(String text) {
        String lower = text.toLowerCase();
        for (String d : KNOWN_DISTRICTS) {
            if (lower.contains(d.toLowerCase())) return d;
        }
        return null;
    }
}
