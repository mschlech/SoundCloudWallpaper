package com.example.soundcloud.challange;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * 
 * @author marcus
 *
 */
public class SoundCloudWallpaperSettings extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		
		
		Preference loginPreference = getPreferenceScreen().findPreference(
				"login");

		Preference passwordPreference = getPreferenceScreen().findPreference(
				"password");
		

		// Add the validator and listener on certain preferences
		loginPreference.setOnPreferenceChangeListener(loginCheckListener);
		passwordPreference.setOnPreferenceChangeListener(passwordChecker);
	}
	
	

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		return super.onMenuItemSelected(featureId, item);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Preference source = getPreferenceScreen().findPreference("source");

		source.setOnPreferenceChangeListener(sourceListener);
		return super.onOptionsItemSelected(item);
	}



	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	Preference.OnPreferenceChangeListener passwordChecker = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if (newValue != null && newValue.toString().length() > 0) {
				return true;
			}
			// If now create a message to the user
			Toast.makeText(SoundCloudWallpaperSettings.this, "Invalid Input",
					Toast.LENGTH_SHORT).show();
			return false;
		}

	};

	Preference.OnPreferenceChangeListener loginCheckListener = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
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
			// If now create a message to the user
			Toast.makeText(SoundCloudWallpaperSettings.this, "You choose " + newValue.toString(),
					Toast.LENGTH_SHORT).show();
			return false;
		}

	};

}
