package com.bestmovies.zoom.bestmovies;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Created by ZooM- on 11/23/2015.
 */
public class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_setting);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.sorting_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.viewtypekey)));
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        ListPreference listPreference = (ListPreference) preference;
        int prefIndex = listPreference.findIndexOfValue(value.toString());
        if (prefIndex >= 0) {
            preference.setSummary(listPreference.getEntries()[prefIndex]);
        }
        return true;
    }
}
