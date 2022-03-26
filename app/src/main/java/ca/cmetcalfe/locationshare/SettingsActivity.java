package ca.cmetcalfe.locationshare;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import ca.cmetcalfe.locationshare.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActivitySettingsBinding binding =
                ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.settingsToolbar.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction()
                .replace(binding.settingsContainer.getId(), new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
