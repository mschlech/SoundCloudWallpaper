package com.example.soundcloud.challange;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.soundcloud.challange.api.SoundCloudApi;
import com.example.soundcloud.challange.data.Tracks;

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
	        SoundCloudApi sca = new SoundCloudApi("mschlech","linus123");
	        try {
	            Log.d(LOG_TAG, " fetching some souncloud stuff upfront");
	            //String myId = ReadingStoriesAloudAuthService.getMyId();
	            //Log.i(LOG_TAG, "ID in soundloud main " + myId);
	            List<Tracks> tracks  = sca.getMyWaveformUrl("tracks", sca.getApiWrapper());
	            for(Tracks tracks1 : tracks) {
	                Log.i(LOG_TAG, "++++++ Tracks wave url ++++ " + tracks1.waveformUrl  );
	            }
	            //ReadingStoriesAloudAuthService.getJsonStrings("me");
	            //ReadingStoriesAloudAuthService.getMyTracksFromSoundCloud();
	        } catch (Exception e) {
	            Log.e(LOG_TAG, "Exception in main activity getMe() " + e);
	        }
	        //setTabs();
	    }

	 }
