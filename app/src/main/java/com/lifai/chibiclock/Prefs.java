package com.lifai.chibiclock;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/** Settings storage. Everything lives in SharedPreferences, no permissions needed. */
public class Prefs {

    private static final String FILE = "chibi_clock";
    private final SharedPreferences sp;

    public Prefs(Context c) {
        sp = c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public String theme() { return sp.getString("theme", "forest"); }
    public void setTheme(String v) { sp.edit().putString("theme", v).apply(); }

    public boolean h24() { return sp.getBoolean("h24", true); }
    public void setH24(boolean v) { sp.edit().putBoolean("h24", v).apply(); }

    public boolean seconds() { return sp.getBoolean("seconds", false); }
    public void setSeconds(boolean v) { sp.edit().putBoolean("seconds", v).apply(); }

    public boolean mascot() { return sp.getBoolean("mascot", true); }
    public void setMascot(boolean v) { sp.edit().putBoolean("mascot", v).apply(); }

    public boolean hint() { return sp.getBoolean("hint", true); }
    public void setHint(boolean v) { sp.edit().putBoolean("hint", v).apply(); }

    public String lang() { return sp.getString("lang", "ru"); }
    public void setLang(String v) { sp.edit().putString("lang", v).apply(); }

    public int dayBrightPct() { return sp.getInt("day_bright", 85); }
    public void setDayBrightPct(int v) { sp.edit().putInt("day_bright", v).apply(); }

    public int nightBrightPct() { return sp.getInt("night_bright", 12); }
    public void setNightBrightPct(int v) { sp.edit().putInt("night_bright", v).apply(); }

    public int nightFrom() { return sp.getInt("night_from", 23); }
    public void setNightFrom(int v) { sp.edit().putInt("night_from", v).apply(); }

    public int nightTo() { return sp.getInt("night_to", 7); }
    public void setNightTo(int v) { sp.edit().putInt("night_to", v).apply(); }

    /** True when the current hour falls inside the dim window. */
    public boolean isNight() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int from = nightFrom();
        int to = nightTo();
        if (from == to) return false;
        if (from < to) return h >= from && h < to;
        return h >= from || h < to;
    }

    /** 0.02..1.0 window brightness for right now. */
    public float currentBrightness() {
        int pct = isNight() ? nightBrightPct() : dayBrightPct();
        float v = pct / 100f;
        if (v < 0.02f) v = 0.02f;
        if (v > 1f) v = 1f;
        return v;
    }

    /** JSON handed to the web page via window.CC_SET(...). */
    public String configJson() {
        return "{"
                + "\"theme\":\"" + theme() + "\","
                + "\"h24\":" + h24() + ","
                + "\"seconds\":" + seconds() + ","
                + "\"mascot\":" + mascot() + ","
                + "\"hint\":" + hint() + ","
                + "\"lang\":\"" + lang() + "\""
                + "}";
    }
}
