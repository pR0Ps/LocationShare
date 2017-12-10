package ca.cmetcalfe.locationshare;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

	private static Preference.OnPreferenceChangeListener prefsListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference pref, Object value) {
                    String valueString = value.toString();

                    if (pref instanceof ListPreference) {
                        ListPreference listPreference = (ListPreference) pref;
                        int index = listPreference.findIndexOfValue(valueString);

                        pref.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                    }
                    return true;
                }
            };

    // ----------------------------------------------------
    // Android Lifecycle
    // ----------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        bindPreferenceSummaryToValue(findPreference("prefLinkType"));
    }

    //-----------------------------------------------------
    // Preferences related methods
    //-----------------------------------------------------
    private static void bindPreferenceSummaryToValue(Preference pref) {
        pref.setOnPreferenceChangeListener(prefsListener);

        prefsListener.onPreferenceChange(pref,
                                         PreferenceManager
                                                 .getDefaultSharedPreferences(pref.getContext())
                                                 .getString(pref.getKey(), ""));
    }
}
