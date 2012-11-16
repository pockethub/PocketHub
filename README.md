# GitHub Android App

[![Build Status](https://travis-ci.org/github/android.png)](https://travis-ci.org/github/android)

This repository contains the source code for the GitHub Android app.

<a href="https://play.google.com/store/apps/details?id=com.github.mobile" alt="Download from Google Play">
  <img src="http://www.android.com/images/brand/android_app_on_play_large.png">
</a>

<a href="https://play.google.com/store/apps/details?id=com.github.mobile" alt="Download from Google Play">
  <img src="http://img.skitch.com/20120709-nkdc1yugu2qmdg1ss81m1gr9ty.jpg">
</a>


Please see the [issues](https://github.com/github/android/issues) section to
report any bugs or feature requests and to see the list of known issues.

## License

* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## Building

The build requires [Maven](http://maven.apache.org/download.html)
v3.0.3+ and the [Android SDK](http://developer.android.com/sdk/index.html)
to be installed in your development environment. In addition you'll need to set
the `ANDROID_HOME` environment variable to the location of your SDK:

    export ANDROID_HOME=/opt/tools/android-sdk

After satisfying those requirements, the build is pretty simple:

* Run `mvn clean package` from the `app` directory to build the APK only
* Run `mvn clean install` from the root directory to build the app and also run
  the integration tests, this requires a connected Android device or running
  emulator

You might find that your device doesn't let you install your build if you
already have the version from the Android Market installed.  This is standard
Android security as it it won't let you directly replace an app that's been
signed with a different key.  Manually uninstall GitHub from your device and
you will then be able to install your own built version.

See [here](https://github.com/github/android/wiki/Building-From-Eclipse) for
instructions on building from [Eclipse](http://eclipse.org).

## Acknowledgements

This project uses the [GitHub Java API](https://github.com/eclipse/egit-github/tree/master/org.eclipse.egit.github.core)
built on top of [API v3](http://developer.github.com/).

It also uses many other open source libraries such as:

* [ActionBarSherlock](https://github.com/JakeWharton/ActionBarSherlock)
* [ViewPagerIndicator](https://github.com/JakeWharton/Android-ViewPagerIndicator)
* [RoboGuice](http://code.google.com/p/roboguice/)
* [android-maven-plugin](https://github.com/jayway/maven-android-plugin)
* [CodeMirror](https://github.com/marijnh/CodeMirror)

These are just a few of the major dependencies, the entire list of dependencies
is listed in the [app's POM file](https://github.com/github/android/blob/master/app/pom.xml).

## Contributing

Please fork this repository and contribute back using
[pull requests](https://github.com/github/android/pulls).

Any contributions, large or small, major features, bug fixes, additional
language translations, unit/integration tests are welcomed and appreciated
but will be thoroughly reviewed and discussed.

