package com.example.soundcloud.challange;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author marcus
 * 
 */
public class SoundCloudWallpaperSettings extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	final String LOG_TAG = "SoundCloudWallpaperSettings";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setTheme(R.style.Preferencestyle);

		getPreferenceManager().setSharedPreferencesName(
				SoundCloudLiveWallpaperService.SOUNDCLOUD_SETTINGS);
		addPreferencesFromResource(R.xml.preference);
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		PreferenceManager.setDefaultValues(this, R.xml.preference,false);
		
		
		Preference loginPreference = getPreferenceScreen().findPreference(
				"login");

		Preference passwordPreference = getPreferenceScreen().findPreference(
				"password");

		ListPreference source = (ListPreference) getPreferenceScreen()
				.findPreference("source");

		source.setSummary(source.getEntry());
		

		Preference downloadFeature = getPreferenceScreen().findPreference(
				"enableDownload");
		downloadFeature.getSharedPreferences().getBoolean("enableDownload", true);

		// Add the validator and listener on certain preferences
		loginPreference.setOnPreferenceChangeListener(loginCheckListener);
		passwordPreference.setOnPreferenceChangeListener(passwordChecker);
		source.setOnPreferenceChangeListener(sourceListener);
		downloadFeature.setOnPreferenceChangeListener(enableDownloadFeature);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub

	}

	//
	Preference.OnPreferenceChangeListener passwordChecker = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.i(LOG_TAG, " onPreferences Changed passwordChecker");
			if (newValue != null && newValue.toString().length() > 0) {
				return true;
			}
			// If now create a message to the user
			Toast.makeText(SoundCloudWallpaperSettings.this, "Invalid Input",
					Toast.LENGTH_SHORT).show();
			return false;
		}

	};
	//
	Preference.OnPreferenceChangeListener loginCheckListener = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.i(LOG_TAG, " onPreferences Changed loginCheckListener");
			if (newValue != null && newValue.toString().length() > 0) {
				return true;
			}
			// If now create a message to the user
			Toast.makeText(SoundCloudWallpaperSettings.this, "Invalid Input",
					Toast.LENGTH_SHORT).show();
			return false;
		}

	};

	Preference.OnPreferenceChangeListener sourceListener = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.i(LOG_TAG, " onPreferences Changed sourceListener");

			if (newValue != null && newValue.toString().length() > 0) {
				preference.setSummary(newValue.toString());
				return true;
			}
			Toast.makeText(SoundCloudWallpaperSettings.this,
					"You choose " + newValue.toString(), Toast.LENGTH_SHORT)
					.show();
			return false;
		}

	};

	Preference.OnPreferenceChangeListener enableDownloadFeature = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			Log.i(LOG_TAG, " onPreferences Changed enableDownloadFeature");

			if (newValue != null && newValue.toString().length() > 0) {
				preference.setSummary(newValue.toString());
				return true;
			}
			Toast.makeText(SoundCloudWallpaperSettings.this,
					"You choose " + newValue.toString(), Toast.LENGTH_SHORT)
					.show();
			return false;
		}

	};
}
