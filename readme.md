# CarstFM

![](app/src/main/res/mipmap-hdpi/ic_launcher.png)

See information about your currently playing track from Last.fm as an Android Auto car dashboard.

## Requirements
* Android 13 'Tiramisu' or above
* Android Auto

## Usage

Once installed, enter your Last.fm username in the mobile app form.
Then, open the app in Android Auto, and it will display information about your current or latest track scrobbled to Last.fm.

The dashboard will display the artist, album, and song information as well as the first and last play dates and any tags you have given to this track.

## Development

**Requirements**

* Android Studio
* Android Auto Desktop Head Unit Emulator

**Steps**

All commands here are relative to `AppData\Local\Android\Sdk\`.

1. Go to Android Auto settings on phone and ensure developer mode is on.
2. Start the Android Auto head unit server.
3. Tether phone to desktop, enabling USB debugging.
4. Check device is connected using `platform-tools\adb.exe devices`.
5. Set up tunnel using `platform-tools\adb.exe forward tcp:5277 tcp:5277`.
6. Start the HDU using `extras\google\auto\desktop-head-unit.exe`.
7. Run application from Android Studio.
   - Application will be viewable on desktop inside the DHU emulator.
