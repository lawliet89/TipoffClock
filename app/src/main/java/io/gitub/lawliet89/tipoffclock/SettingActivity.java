package io.gitub.lawliet89.tipoffclock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;


public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, new GeneralSettingFragment()).commit();

        TipoffClockService.startServiceIfEnabled(this);
    }

    public class GeneralSettingFragment
            extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final Preference.OnPreferenceChangeListener enabledChangeListener;

        public GeneralSettingFragment() {
            enabledChangeListener = new EnabledChangeListener();
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            Preference enabledPreference = findPreference("setting_enabled");
            enabledPreference.setOnPreferenceChangeListener(enabledChangeListener);
            // Trigger it now
            enabledChangeListener.onPreferenceChange(enabledPreference,
                    PreferenceManager.getDefaultSharedPreferences(enabledPreference.getContext())
                            .getBoolean(enabledPreference.getKey(), true));

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("setting_enabled")) {
                TipoffClockService.startServiceIfEnabled(getActivity());
            }
        }

        private class EnabledChangeListener implements Preference.OnPreferenceChangeListener {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                boolean newValue = (boolean) value;
                if (newValue) {
                    preference.setSummary(R.string.enabled_true);
                } else {
                    preference.setSummary(R.string.enabled_false);
                }
                return true;
            }
        }
    }
}

