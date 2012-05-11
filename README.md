SoundCloudWallpaper
===================
##A LiveWallpaper which retrieves SoundCloud tracks or favorites of a sound cloud user. 


# Screenshots on a low end aldi mobile
![HomeScreen](https://github.com/mschlech/SoundCloudWallpaper/raw/master/pics/soundcloudwallpaper1.png)
A homescreen wiht the livewallpaper
![HomeScreen surfing on soundcloud wave](https://github.com/mschlech/SoundCloudWallpaper/raw/master/pics/soundcloudpressbelowTitle.png)
tapping on the title rotates the title, taping on the track owner fetches randomly the next track. Double tap invokes 
a mimetype referenced app(like soundcloud app or browser)
 
## Dependencies
 * requires Android 2.3.3 
 * Add android-support-v4.jar
 * Add java-api-wrapper (https://github.com/soundcloud/java-api-wrapper) as a library project
 * Add nineoldandroids (http://nineoldandroids.com/

## Features
 
 * obtains the waveform png , track owner and track title via an java-api-wrapper  
   [SoundCloud Java API wrapper (OAuth2 only)](https://github.com/soundcloud/java-api-wrapper)call against soundcloud Live system.
 * the track will be selected randomly. The waveform png , the title and the owner will be displayed on the device
 * selectable source via preference to obtain all tracks or favorites of users soundcloud profile 
 * waveform png and additional track information changes from time to time or user interaction 
 * wipe to the next virtual screen of the device will show the next section of the waveform.
 * one tap gets randomly to the next track with its information.
 * double tap on the homescreen opens the displayed soundfile in a browser on (http://www.soundcloud.com)
   or opens the soundcloud app if installed.
 * Preferences to enable user initiated download swipe down the soundcloud logo.
 

## Installation
 * Installtion can be done via your favorite IDE (Eclipse, intellij which runs an android environment),
   command line android tool or copying apk on the sdcard (which is not tested)
 
## Limitation
 * tracks being private cannot be invoked and passed to a subsequent intent ( browser or app invocation which has a different user ) which is ok, but it is not 
   blocked or notified upfront in the soundcloud wallpaper.
 * a limitation of 8 tracks to be kept in memory. 

### LiveWallpaper

SoundCloudWallpaper is used like a standard wallpaper, installed and configured long press on the homescreen.
<http://developer.android.com/resources/articles/live-wallpapers.html>

## License

Licensed under the [GNU Lesser General Public License ](http://www.gnu.org/licenses/lgpl-3.0-standalone.html)
