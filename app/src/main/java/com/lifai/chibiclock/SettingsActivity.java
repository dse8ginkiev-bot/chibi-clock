package com.lifai.chibiclock;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

/** Plain programmatic settings screen — no XML, no libraries. */
public class SettingsActivity extends Activity {

    private Prefs p;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        p = new Prefs(this);

        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(22);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);
        setContentView(scroll);

        title(getString(R.string.settings_title));
        caption(getString(R.string.settings_note));

        title(getString(R.string.group_look));
        RadioGroup themes = new RadioGroup(this);
        radio(themes, "forest", getString(R.string.theme_forest), "forest".equals(p.theme()));
        radio(themes, "none", getString(R.string.theme_plain), "none".equals(p.theme()));
        themes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup g, int id) {
                p.setTheme(((RadioButton) findViewById(id)).getTag().toString());
            }
        });
        root.addView(themes);

        RadioGroup langs = new RadioGroup(this);
        radio(langs, "ru", "Русский", "ru".equals(p.lang()));
        radio(langs, "en", "English", "en".equals(p.lang()));
        langs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(RadioGroup g, int id) {
                p.setLang(((RadioButton) findViewById(id)).getTag().toString());
            }
        });
        root.addView(langs);

        check(getString(R.string.opt_seconds), p.seconds(), new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean v) { p.setSeconds(v); }
        });
        check(getString(R.string.opt_12h), !p.h24(), new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean v) { p.setH24(!v); }
        });
        check(getString(R.string.opt_mascot), p.mascot(), new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean v) { p.setMascot(v); }
        });
        check(getString(R.string.opt_hint), p.hint(), new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean v) { p.setHint(v); }
        });

        title(getString(R.string.group_brightness));
        slider(getString(R.string.opt_day_bright), 5, 100, p.dayBrightPct(), new IntSetter() {
            @Override public void set(int v) { p.setDayBrightPct(v); }
        });
        slider(getString(R.string.opt_night_bright), 2, 100, p.nightBrightPct(), new IntSetter() {
            @Override public void set(int v) { p.setNightBrightPct(v); }
        });
        title(getString(R.string.group_night));
        slider(getString(R.string.opt_night_from), 0, 23, p.nightFrom(), new IntSetter() {
            @Override public void set(int v) { p.setNightFrom(v); }
        });
        slider(getString(R.string.opt_night_to), 0, 23, p.nightTo(), new IntSetter() {
            @Override public void set(int v) { p.setNightTo(v); }
        });

        title("");
        Button done = new Button(this);
        done.setText(R.string.btn_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        root.addView(done, new LinearLayout.LayoutParams(-1, -2));

        Button quit = new Button(this);
        quit.setText(R.string.btn_quit);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finishAffinity(); }
        });
        root.addView(quit, new LinearLayout.LayoutParams(-1, -2));
    }

    private interface IntSetter { void set(int v); }

    private void title(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(18f);
        t.setPadding(0, dp(18), 0, dp(6));
        root.addView(t);
    }

    private void caption(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(13f);
        t.setAlpha(0.7f);
        root.addView(t);
    }

    private void radio(RadioGroup group, String tag, String label, boolean checked) {
        RadioButton b = new RadioButton(this);
        b.setText(label);
        b.setTag(tag);
        b.setChecked(checked);
        group.addView(b);
    }

    private void check(String label, boolean value, CompoundButton.OnCheckedChangeListener l) {
        CheckBox c = new CheckBox(this);
        c.setText(label);
        c.setChecked(value);
        c.setOnCheckedChangeListener(l);
        root.addView(c);
    }

    private void slider(final String label, final int min, final int max, int value, final IntSetter setter) {
        final TextView caption = new TextView(this);
        caption.setTextSize(14f);
        caption.setGravity(Gravity.START);
        caption.setPadding(0, dp(12), 0, 0);
        caption.setText(label + ": " + value);
        root.addView(caption);

        SeekBar bar = new SeekBar(this);
        bar.setMax(max - min);
        bar.setProgress(Math.max(0, Math.min(max - min, value - min)));
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar b, int progress, boolean fromUser) {
                int v = progress + min;
                caption.setText(label + ": " + v);
                if (fromUser) setter.set(v);
            }
            @Override public void onStartTrackingTouch(SeekBar b) { }
            @Override public void onStopTrackingTouch(SeekBar b) { setter.set(b.getProgress() + min); }
        });
        root.addView(bar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(40)));
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
