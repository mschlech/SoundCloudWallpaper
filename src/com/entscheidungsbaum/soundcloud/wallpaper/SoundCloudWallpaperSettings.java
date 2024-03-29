package com.entscheidungsbaum.soundcloud.wallpaper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author marcus
 * entscheidungsbaum
 */
public class SoundCloudWallpaperSettings extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(
				SoundCloudLiveWallpaperService.SOUNDCLOUD_SETTINGS);
		addPreferencesFromResource(R.xml.preference);
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		Preference loginPreference = getPreferenceScreen().findPreference(
				"login");

		Preference passwordPreference = getPreferenceScreen().findPreference(
				"password");
		ListPreference source = (ListPreference) getPreferenceScreen()
				.findPreference("source");
		source.setSummary(source.getEntry());

		Preference downloadFeature = getPreferenceScreen().findPreference(
				"enableDownload");
		downloadFeature.getSharedPreferences().getBoolean("enableDownload",
				true);

		Preference downloadLimit = getPreferenceScreen().findPreference(
				"downloadlimit");

		// Add the validator and listener on certain preferences
		loginPreference.setOnPreferenceChangeListener(loginCheckListener);
		passwordPreference.setOnPreferenceChangeListener(passwordChecker);
		source.setOnPreferenceChangeListener(sourceListener);
		downloadFeature.setOnPreferenceChangeListener(enableDownloadFeature);
		downloadLimit.setOnPreferenceChangeListener(downloadLimitCheck);
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
			Log.i("Preferences",
					" onPreferences password  Changed" + newValue.toString());
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
			Log.i("Preferences",
					" onPreferences login Changed" + newValue.toString());
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
	Preference.OnPreferenceChangeListener downloadLimitCheck = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {

			if (newValue != null && newValue.toString().length() > 0 && newValue.toString().matches("\\d+")) {
				preference.setSummary(newValue.toString());
				return true;
			}
			Toast.makeText(SoundCloudWallpaperSettings.this,
					"only digits" + newValue.toString() , Toast.LENGTH_SHORT)
					.show();
			return false;
		}

	};

}
