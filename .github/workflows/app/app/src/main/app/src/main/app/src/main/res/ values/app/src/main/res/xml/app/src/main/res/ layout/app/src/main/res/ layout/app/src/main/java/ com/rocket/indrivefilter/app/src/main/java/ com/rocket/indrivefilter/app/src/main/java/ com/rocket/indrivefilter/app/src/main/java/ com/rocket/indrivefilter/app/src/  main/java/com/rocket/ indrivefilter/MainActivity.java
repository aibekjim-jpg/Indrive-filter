package com.rocket.indrivefilter;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final FilterConfig cfg = FilterConfig.load(this);
        final EditText etMin = findViewById(R.id.etMinPrice);
        final EditText etPick = findViewById(R.id.etPickup);
        final EditText etMinTrip = findViewById(R.id.etMinTrip);
        final EditText etMaxTrip = findViewById(R.id.etMaxTrip);
        final EditText etDistricts = findViewById(R.id.etDistricts);
        final CheckBox cbCash = findViewById(R.id.cbCash);
        final CheckBox cbAuto = findViewById(R.id.cbAuto);

        etMin.setText(String.valueOf(cfg.minPrice));
        etPick.setText(String.valueOf(cfg.maxPickupKm));
        etMinTrip.setText(String.valueOf(cfg.minTripKm));
        etMaxTrip.setText(String.valueOf(cfg.maxTripKm));
        etDistricts.setText(join(cfg.whitelistDistricts));
        cbCash.setChecked(cfg.requireCash);
        cbAuto.setChecked(cfg.autoAccept);

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                cfg.minPrice = parseInt(etMin.getText().toString(), 0);
                cfg.maxPickupKm = parseDouble(etPick.getText().toString(), 999.0);
                cfg.minTripKm = parseDouble(etMinTrip.getText().toString(), 0.0);
                cfg.maxTripKm = parseDouble(etMaxTrip.getText().toString(), 999.0);
                cfg.whitelistDistricts.clear();
                String dstr = etDistricts.getText().toString().trim();
                if (!dstr.isEmpty()) {
                    for (String s : dstr.split(",")) {
                        String t = s.trim();
                        if (!t.isEmpty()) cfg.whitelistDistricts.add(t);
                    }
                }
                cfg.requireCash = cbCash.isChecked();
                cfg.autoAccept = cbAuto.isChecked();
                cfg.enabled = true;
                FilterConfig.save(MainActivity.this, cfg);
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnAccess = findViewById(R.id.btnAccess);
        btnAccess.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        });

        Button btnOverlay = findViewById(R.id.btnOverlay);
        btnOverlay.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                }
            }
        });
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s.trim().replace(',', '.')); } catch (Exception e) { return def; }
    }

    private static String join(java.util.List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
