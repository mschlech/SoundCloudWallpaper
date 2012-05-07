package com.example.soundcloud.challange.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;

import com.example.soundcloud.challange.data.Tracks;
import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Env;
import com.soundcloud.api.Http;
import com.soundcloud.api.Request;
import com.soundcloud.api.Token;

/**
 * marcus SoundCloudTrial
 * <p/>
 * 30.04.2012
 */
public class SoundCloudApi {

    final static String LOG_TAG = "SoundCloudApi";

    final static String CLIENT_ID = "d8e6ffc58ae1a231a7f1847cf695adb2";
    final static String CLIENT_SECRET = "e9aa7b6b56d9d5056af5fe220749a62a";
    private static String username = "mschlech";
    private static String passwd = "linus123";

    final String END_USER_AUTHORIZATON_URL = "https://soundcloud.com/connect";
    final String TOKEN = "https://api.soundcloud.com/oauth2/token";

    static String TAG = "";

    static JSONObject jsonObject;
    static JSONArray jsonArray;
    private static String id;

    AccountManager mAccountManager;
    Account mAccount;


    /**
     * authenticate with default username and password
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
        token = mWrapper.login(username, passwd);

        Log.d(LOG_TAG, " getToken in authenticate" + token);
        return token;
    }

    /**
     * invoked if preferences contains a username and password.
     * @param aUsername
     * @param aPassword
     * @return
     * @throws Exception
     */
    public static Token authenticate(String aUsername , String aPassword) throws Exception {

        ApiWrapper mWrapper = new ApiWrapper(CLIENT_ID, CLIENT_SECRET, null, null, Env.LIVE);
        Token token;
        Log.d(LOG_TAG, "got a wrapper ");
        if (mWrapper.debugRequests) {
            Log.d(LOG_TAG, "Debug request");
        }
        Log.d(LOG_TAG, "get a token " + mWrapper.getToken().valid());
        token = mWrapper.login(aUsername, aPassword);

        Log.d(LOG_TAG, " getToken in authenticate" + token);
        return token;
    }

    
    /**
     * get an ApiWrapper of soundcloud
     *
     * @return
     * @throws Exception
     */
    private static ApiWrapper getApiWrapper() throws Exception {
        ApiWrapper mWrapper = new ApiWrapper(CLIENT_ID, CLIENT_SECRET, null, authenticate(), Env.LIVE);

        Log.d(LOG_TAG, "got a api wrapper ");
        if (mWrapper.debugRequests) {
            Log.d(LOG_TAG, "Debug request");
        }

        Log.d(LOG_TAG, " getApiWrapper getApiWrapper" + mWrapper);
        return mWrapper;
    }


    /**
     * /me request and the appropriate response of soundcloud
     * not used 
     * @throws Exception
     */
    public static void getMe() throws Exception {
        final Request requestResource = Request.to("/me");
        ApiWrapper apiWrapper = getApiWrapper();
        try {
            HttpResponse soundCloudResponse = apiWrapper.get(requestResource);
            // soundCloudResponse.getStatusLine().getStatusCode()
            jsonObject = Http.getJSON(soundCloudResponse);
            id = jsonObject.get("id").toString();
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
            //   id=jsonObject.get("id").toString();
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
        //Log.d(LOG_TAG, " KEY -  " + key + " VALUE " + jsonObject.get("title"));
    }


    /**
     * the track list of the authenticated user to be displayed on the tracklist view
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
     * @param source whether from favorites  or own tracks
     *               /users/<uid>/favorites or /users/<uid>/tracks
     *               "waveform_url:<http://waveurl/
     * @return
     * @throws Exception
     */
    public static List<Tracks> getMyWaveformUrl(String source) throws Exception {

        ApiWrapper apiWrapper = getApiWrapper();

        String id = getMyId();

        StringBuffer resourceUrl = new StringBuffer();
        resourceUrl.append("/users/");
        resourceUrl.append(id);
        resourceUrl.append("/");
        resourceUrl.append(source);

        final Request requestResource = Request.to(resourceUrl.toString());

        Log.e(LOG_TAG, "json Response" + requestResource.toUrl());
        List<Tracks> trackList = new ArrayList<Tracks>();
        try {

            HttpResponse soundCloudResponse = apiWrapper.get(requestResource);

            if (soundCloudResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                final String jsonString = Http.getString(soundCloudResponse);
                jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                	Log.i(LOG_TAG, " tracks -> " +jsonArray.toString());
                	
                    Tracks tracks = new Tracks();
                  
                   tracks.trackName = (String) jsonArray.getJSONObject(i).get("title");
                    tracks.waveformUrl = (String) jsonArray.getJSONObject(i).get("waveform_url");
                    Log.i(LOG_TAG, " TRACKS WaveFormUrl -> " +tracks.waveformUrl);
                   // tracks.genre = (String) jsonArray.getJSONObject(i).get("genre");
                   // tracks.permalink_url = (String) jsonArray.getJSONObject(i).get("permalink_url");
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


    /**
     * @return
     * @throws Exception
     * @ param apiWrapper
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
                 String key = (String)keys.next();
                // Log.i(LOG_TAG, " keys-> " + key + " Value -> " + jsonObject.getString(key));
                    id = (String)jsonObject.getString("id");
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
