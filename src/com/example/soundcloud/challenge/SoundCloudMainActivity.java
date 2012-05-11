package com.example.soundcloud.challenge;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.soundcloud.challenge.api.SoundCloudApi;
import com.example.soundcloud.challenge.data.Tracks;

	/**
	 * @author marcus 29.04.2012
	 */
	public class SoundCloudMainActivity extends Activity {

	    private final String LOG_TAG = "SoundCloudMain";

	    /**
	     * Called when the activity is first created.
	     */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        try {
	            Log.d(LOG_TAG, " fetching some souncloud stuff upfront");
	            List<Tracks> tracks  = SoundCloudApi.getMyWaveformUrl( SoundCloudApi.getApiWrapper());
	            for(Tracks tracks1 : tracks) {
	                Log.i(LOG_TAG, "++++++ Tracks wave url ++++ " + tracks1.waveformUrl  );
	            }
	        } catch (Exception e) {
	            Log.e(LOG_TAG, "Exception in main activity getMe() " + e);
	        }
	    }
	 }
