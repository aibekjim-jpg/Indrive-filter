package com.rocket.indrivefilter;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.Arrays;
import java.util.List;

public class FilterService extends AccessibilityService {

    private FilterConfig cfg;
    private int lastOrderHash = 0;
    private long lastActionAt = 0;

    private static final List<String> ACCEPT_LABELS = Arrays.asList(
        "Accept", "Take order"
    );

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        cfg = FilterConfig.load(this);
        OverlayView.show(this, "ROCKET ON");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        cfg = FilterConfig.load(this);
        if (!cfg.enabled) return;

        CharSequence pkgCs = event.getPackageName();
        if (pkgCs == null) return;
        String pkg = pkgCs.toString().toLowerCase();
        if (!pkg.contains("indrive")) return;

        long now = System.currentTimeMillis();
        if (now - lastActionAt < 800) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        OrderParser.Order order = OrderParser.parse(root);
        if (order == null) return;

        int hash = order.rawText.hashCode();
        if (hash == lastOrderHash) return;
        lastOrderHash = hash;

        if (!passes(order)) {
            OverlayView.update("X " + safe(order.price) + " / " + safe(order.pickupKm));
            return;
        }
        OverlayView.update("OK " + safe(order.price) + " / " + safe(order.pickupKm));

        if (cfg.autoAccept) {
            if (clickAccept(root)) {
                lastActionAt = now;
                Toast.makeText(this, "Accepted: " + safe(order.price),
                    Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean passes(OrderParser.Order o) {
        if (o.price != null && o.price < cfg.minPrice) return false;
        if (o.pickupKm != null && o.pickupKm > cfg.maxPickupKm) return false;
        if (o.tripKm != null && (o.tripKm < cfg.minTripKm || o.tripKm > cfg.maxTripKm)) return false;
        if (cfg.requireCash && !o.cash) return false;

        String d = o.district;
        if (cfg.whitelistDistricts != null && !cfg.whitelistDistricts.isEmpty()) {
            if (d == null) return false;
            boolean found = false;
            for (String w : cfg.whitelistDistricts) {
                if (w.equalsIgnoreCase(d)) { found = true; break; }
            }
            if (!found) return false;
        }
        return true;
    }

    private boolean clickAccept(AccessibilityNodeInfo root) {
        AccessibilityNodeInfo node = findClickableByText(root, ACCEPT_LABELS);
        if (node == null) return false;
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    private AccessibilityNodeInfo findClickableByText(
            AccessibilityNodeInfo node, List<String> labels) {
        if (node == null) return null;
        CharSequence t1 = node.getText();
        CharSequence t2 = node.getContentDescription();
        String t = t1 != null ? t1.toString() : (t2 != null ? t2.toString() : null);
        if (t != null) {
            for (String l : labels) {
                if (t.equalsIgnoreCase(l) || t.contains(l)) {
                    AccessibilityNodeInfo n = node;
                    while (n != null) {
                        if (n.isClickable()) return n;
                        n = n.getParent();
                    }
                }
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo c = node.getChild(i);
            AccessibilityNodeInfo r = findClickableByText(c, labels);
            if (r != null) return r;
        }
        return null;
    }

    private String safe(Object o) { return o == null ? "?" : o.toString(); }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        OverlayView.hide(this);
        super.onDestroy();
    }
}
