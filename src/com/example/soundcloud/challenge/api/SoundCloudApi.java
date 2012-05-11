package com.example.soundcloud.challenge.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.soundcloud.challenge.data.Tracks;
import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Env;
import com.soundcloud.api.Http;
import com.soundcloud.api.Request;
import com.soundcloud.api.Token;

/**
 * @author marcus
 * 
 */
public class SoundCloudApi {

	final static String LOG_TAG = "SoundCloudApi";

	final static String CLIENT_ID = "d8e6ffc58ae1a231a7f1847cf695adb2";
	final static String CLIENT_SECRET = "e9aa7b6b56d9d5056af5fe220749a62a";
	// private static String username = "mschlech";
	// private static String passwd = "linus123";
	static String mUsername;
	static String mPassword;
	static String mSource;

	final String END_USER_AUTHORIZATON_URL = "https://soundcloud.com/connect";
	final String TOKEN = "https://api.soundcloud.com/oauth2/token";

	static String TAG = "";

	static JSONObject jsonObject;
	static JSONArray jsonArray;

	AccountManager mAccountManager;
	Account mAccount;

	public SoundCloudApi(String aUsername, String aPassword, String source) {
		Log.i(LOG_TAG, "User " + aUsername + " password " + aPassword);

		mUsername = aUsername;
		mPassword = aPassword;
		mSource = source;
	}

	/**
	 * authenticate with default username and password
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Token authenticate() throws Exception {

		ApiWrapper mWrapper = new ApiWrapper(CLIENT_ID, CLIENT_SECRET, null, null, Env.LIVE);
		Token token;
		Log.d(LOG_TAG, "got a wrapper ");
		if (mWrapper.debugRequests) {
			Log.d(LOG_TAG, "Debug request");
		}
		Log.d(LOG_TAG, "get a token " + mWrapper.getToken().valid());
		token = mWrapper.login(mUsername.trim(), mPassword.trim());

		Log.d(LOG_TAG, " getToken in authenticate" + token);
		return token;
	}

	/**
	 * get an ApiWrapper of soundcloud
	 * 
	 * @return
	 * @throws Exception
	 */
	public static ApiWrapper getApiWrapper() throws Exception {
		ApiWrapper mWrapper = new ApiWrapper(CLIENT_ID, CLIENT_SECRET, null, authenticate(), Env.LIVE);

		Log.d(LOG_TAG, "got a api wrapper ");
		if (mWrapper.debugRequests) {
			Log.d(LOG_TAG, "Debug request");
		}

		Log.d(LOG_TAG, " getApiWrapper getApiWrapper" + mWrapper);
		return mWrapper;
	}

	/**
	 * /me request and the appropriate response of soundcloud not used
	 * 
	 * @throws Exception
	 */
	public static void getMe() throws Exception {
		final Request requestResource = Request.to("/me");
		ApiWrapper apiWrapper = getApiWrapper();
		try {
			HttpResponse soundCloudResponse = apiWrapper.get(requestResource);
			// soundCloudResponse.getStatusLine().getStatusCode()
			jsonObject = Http.getJSON(soundCloudResponse);
			Log.d(LOG_TAG, "got the response of soundcloud " + jsonObject);

		} catch (Exception e) {
			Log.e(LOG_TAG, "exception  in httpresponse" + e);
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public static void getUser() throws Exception {
		final Request requestResource = Request.to("/users");
		ApiWrapper apiWrapper = getApiWrapper();
		try {
			HttpResponse soundCloudResponse = apiWrapper.get(requestResource);
			// soundCloudResponse.getStatusLine().getStatusCode()
			jsonObject = Http.getJSON(soundCloudResponse);
			// id=jsonObject.get("id").toString();
			Log.d(LOG_TAG, "got the response of soundcloud " + jsonObject);

		} catch (Exception e) {
			Log.e(LOG_TAG, "exception  in httpresponse" + e);
		}
	}

	/**
	 * 
	 * @param resource
	 * @throws Exception
	 */
	public static void getJsonStrings(String resource) throws Exception {
		ApiWrapper apiWrapper = getApiWrapper();

		final Request requestResource = Request.to(resource);

		try {
			HttpResponse soundCloudResponse = apiWrapper.get(requestResource);
			final String jsonString = Http.getString(soundCloudResponse);
			jsonArray = new JSONArray(jsonString);
			Log.d(LOG_TAG, "got the response of soundcloud " + jsonArray);
		} catch (Exception e) {
			Log.e(LOG_TAG, "exception  in httpresponse" + e);
		}
		// Log.d(LOG_TAG, " KEY -  " + key + " VALUE " +
		// jsonObject.get("title"));
	}

	/**
	 * the track list of the authenticated user to be displayed on the tracklist
	 * view
	 * 
	 * @return
	 */
	public static List<Tracks> getMyTracksFromSoundCloud() throws Exception {
		ApiWrapper apiWrapper = getApiWrapper();

		final Request requestResource = Request.to("/me/tracks");
		Log.e(LOG_TAG, "json Response" + requestResource.toUrl());
		List<Tracks> trackList = new ArrayList<Tracks>();
		try {

			HttpResponse soundCloudResponse = apiWrapper.get(requestResource);

			if (soundCloudResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				final String jsonString = Http.getString(soundCloudResponse);
				jsonArray = new JSONArray(jsonString);
				for (int i = 0; i < jsonArray.length(); i++) {
					Tracks tracks = new Tracks();
					tracks.trackName = (String) jsonArray.getJSONObject(i).get("title");
					trackList.add(tracks);
				}
			} else {
				Log.e(LOG_TAG, "no valid json Response");
			}

		} catch (Exception e) {
			Log.e(LOG_TAG, "exception  in http track response " + e);
		}
		Log.i(LOG_TAG, " tracklist = " + trackList.toString());
		return trackList;

	}

	/**
	 * get a waveform url and some other flying stuff
	 * 
	 * @param source
	 *            whether from favorites or own tracks /users/<uid>/favorites or
	 *            /users/<uid>/tracks "waveform_url:<http://waveurl/
	 * @return
	 * @throws Exception
	 */
	public static List<Tracks> getMyWaveformUrl(ApiWrapper apiWrapper) throws Exception {

		// ApiWrapper apiWrapper = getApiWrapper();

		String id = getMyId();

		StringBuffer resourceUrl = new StringBuffer();
		resourceUrl.append("/users/");
		resourceUrl.append(id);
		resourceUrl.append("/");
		resourceUrl.append(mSource.trim());

		final Request requestResource = Request.to(resourceUrl.toString());

		Log.e(LOG_TAG, "json Response" + requestResource.toUrl());
		List<Tracks> trackList = new ArrayList<Tracks>();
		try {

			HttpResponse soundCloudResponse = apiWrapper.get(requestResource);

			if (soundCloudResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				final String jsonString = Http.getString(soundCloudResponse);
				jsonArray = new JSONArray(jsonString);
				for (int i = 0; i < jsonArray.length(); i++) {
					Log.i(LOG_TAG, " tracks -> " + jsonArray.toString());

					Tracks tracks = new Tracks();

					if ((String) jsonArray.getJSONObject(i).get("title") != null) {
						tracks.trackName = (String) jsonArray.getJSONObject(i).get("title");
					} else {
						tracks.trackName = "";
					}

					if ((String) jsonArray.getJSONObject(i).get("waveform_url") != null) {
						tracks.waveformUrl = (String) jsonArray.getJSONObject(i).get("waveform_url");
					} else {
						tracks.waveformUrl = "www.soundcloud.com";
					}

					if ((String) jsonArray.getJSONObject(i).getString("permalink_url") != null) {
						tracks.permalink_url = (String) jsonArray.getJSONObject(i).getString("permalink_url");
					} else {
						tracks.permalink_url = "www.soundcloud.com";
					}
					tracks.waveFormURLPng = getBitmapFromSoundCloud(tracks.waveformUrl);

					if ((String) jsonArray.getJSONObject(i).getString("genre") != null) {
						tracks.genre = (String) jsonArray.getJSONObject(i).getString("genre");
					} else {
						tracks.genre = "n A ";
					}

					JSONObject user = jsonArray.getJSONObject(i).getJSONObject("user");
					if (user!=null){
						tracks.userName = (String) user.getString("username");
					}
						 
					Log.i(LOG_TAG, " TRACKS WaveFormUrl -> " + tracks.waveformUrl);
					// tracks.genre = (String)
					// jsonArray.getJSONObject(i).get("genre");
					// tracks.permalink_url = (String)
					// jsonArray.getJSONObject(i).get("permalink_url");
					trackList.add(tracks);
				}
			} else {
				Log.e(LOG_TAG, "no valid json Response");
			}

		} catch (Exception e) {
			Log.e(LOG_TAG, "exception  in http track response " + e);
		}
		Log.i(LOG_TAG, "TRACKS URLS AND STUFF " + trackList.toString());
		return trackList;

	}

	private static Bitmap getBitmapFromSoundCloud(String url) {
		Log.i(LOG_TAG, " fetcing bitmap in sourcloudapi");
		URL mUrl = null;
		try {
			mUrl = new URL(url);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {

		}
		Bitmap result = null;
		if (url != null) {
			HttpURLConnection connection;
			try {
				Log.i(LOG_TAG, " Bitmap fetch ");
				connection = (HttpURLConnection) mUrl.openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
				result = BitmapFactory.decodeStream(input);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		Log.i(LOG_TAG, "result");

		return result;

	}

	/**
	 * @return
	 * @throws Exception
	 *             @ param apiWrapper
	 */
	private static String getMyId() throws Exception {
		String id = "";
		ApiWrapper apiWrapper = getApiWrapper();
		Log.d(LOG_TAG, " get my ID");
		final Request requestResource = Request.to("/me/");

		try {

			HttpResponse soundCloudResponse = apiWrapper.get(requestResource);

			if (soundCloudResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				final String jsonString = Http.getString(soundCloudResponse);
				jsonObject = new JSONObject(jsonString);
				final Iterator<?> keys = jsonObject.keys();

				while (keys.hasNext()) {
					String key = (String) keys.next();
					// Log.i(LOG_TAG, " keys-> " + key + " Value -> " +
					// jsonObject.getString(key));
					id = (String) jsonObject.getString("id");
				}
			} else {
				Log.e(LOG_TAG, "no valid json Response");
			}

		} catch (Exception e) {
			Log.e(LOG_TAG, "exception  in http id response " + e);
		}
		Log.i(LOG_TAG, " ID = " + id);
		return id;
	}
}
