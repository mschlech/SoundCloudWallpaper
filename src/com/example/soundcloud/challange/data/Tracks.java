package com.example.soundcloud.challange.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * marcus SoundCloudTrial
 * not necesssarily a parelable needed . 
 * 30.04.2012
 */
public class Tracks implements Parcelable {

    /**
     * "id": 13158665, "created_at": "2011/04/06 15:37:43 +0000",
     * "user_id":3699101,
     * "duration": 18109,
     * "commentable": true,
     * "state": "finished",
     * "sharing": "public",
     * "tag_list": "soundcloud:source=iphone-record",
     * "permalink": "munching-at-tiannas-house",
     * "description": null,
     * "streamable": true,
     * "downloadable": true,
     * "genre": null,
     * "release": null,
     * "purchase_url": null, "label_id": null, "label_name": null, "isrc": null,
     * "video_url": null, "track_type": "recording", "key_signature": null,
     * "bpm": null, "title": "Munching at Tiannas house", "release_year": null,
     * "release_month": null, "release_day": null, "original_format": "m4a",
     * "original_content_size": 10211857, "license": "all-rights-reserved",
     * "uri": "http://api.soundcloud.com/tracks/13158665", "permalink_url":
     * "http://soundcloud.com/user2835985/munching-at-tiannas-house",
     * "artwork_url": null, "waveform_url":
     * "http://w1.sndcdn.com/fxguEjG4ax6B_m.png",
     * "user": { "id": 3699101,
     * "permalink": "user2835985",
     * "username": "user2835985", "uri":http://api.soundcloud.com/users/3699101", "permalink_url":
     * "http://soundcloud.com/user2835985", "avatar_url":
     * "http://a1.sndcdn.com/images/default_avatar_large.png?142a848" },
     * "stream_url": "http://api.soundcloud.com/tracks/13158665/stream",
     * "download_url": "http://api.soundcloud.com/tracks/13158665/download",
     * "playback_count": 0,
     * "download_count": 0,
     * "favoritings_count": 0,
     * "comment_count": 0,
     * "created_with": { "id": 124, "name":
     * "SoundCloud iPhone",
     * "uri": "http://api.soundcloud.com/apps/124",
     * "permalink_url": "http://soundcloud.com/apps/iphone" },
     * "attachments_uri":"http://api.soundcloud.com/tracks/13158665/attachments" }
     */

    public String userName;
    public String trackName;
    public String trackOwner;
    public String dateUpdatedStamp;
    // public Bitmap userIcon;
    public int userIcon;
    public String avatarUrl;
    public String waveformUrl;
    public String permalink_url;
    public String genre;
    public Bitmap waveFormURLPng;

    
    public Tracks() {

    }

    public Tracks(Parcel source) {
        this.userName = source.readString();
        this.trackName = source.readString();
        this.trackOwner = source.readString();
        this.dateUpdatedStamp = source.readString();
        this.avatarUrl = source.readString();
        this.waveformUrl = source.readString();
        this.permalink_url = source.readString();
        this.genre = source.readString();
       // this.waveFormURL =source.readByte();

    }

    public static final Parcelable.Creator<Tracks> CREATOR = new Parcelable.Creator<Tracks>() {

        @Override
        public Tracks createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new Tracks(source);
        }

        @Override
        public Tracks[] newArray(int size) {
            // TODO Auto-generated method stub
            return new Tracks[size];
        }
    };

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub

    }


}
