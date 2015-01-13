# GitHub Android App  [![Google Play](http://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=com.github.mobile) [![Build Status](https://travis-ci.org/github/android.png)](https://travis-ci.org/github/android)

This repository contains the source code for the GitHub Android app.

[![Download from Google Play](https://cloud.githubusercontent.com/assets/3838734/3855877/4cf2a2dc-1eec-11e4-9634-2a1adf8f1c39.jpg)](https://play.google.com/store/apps/details?id=com.github.mobile)


Please see the [issues](https://github.com/github/android/issues) section to
report any bugs or feature requests and to see the list of known issues.

## License

* [Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

## Building

### With Gradle

The easiest way to build is to install [Android Studio](https://developer.android.com/sdk/index.html) v1.+
with [Gradle](https://www.gradle.org/) v2.2.1.
Once installed, then you can import the project into Android Studio:

1. Open `File`
2. Import Project
3. Select `build.gradle` under the project directory
4. Click `OK`

Then, Gradle will do everything for you.

### With Maven

Notes: Although Maven support is not dropped as yet, to say the least, we have shifted our focus to use Gradle as our
main build system.

The build requires [Maven](http://maven.apache.org/download.html)
v3.1.1+ and the [Android SDK](http://developer.android.com/sdk/index.html)
to be installed in your development environment. In addition you'll need to set
the `ANDROID_HOME` environment variable to the location of your SDK:

```bash
export ANDROID_HOME=/opt/tools/android-sdk
```

After satisfying those requirements, the build is pretty simple:

* Run `mvn clean package` from the `app` directory to build the APK only
* Run `mvn clean install` from the root directory to build the app and also run
  the integration tests, this requires a connected Android device or running
  emulator

You might find that your device doesn't let you install your build if you
already have the version from Google Play installed.  This is standard
Android security as it it won't let you directly replace an app that's been
signed with a different key.  Manually uninstall GitHub from your device and
you will then be able to install your own built version.

See [here](https://github.com/github/android/wiki/Building-From-Eclipse) for
instructions on building from [Eclipse](http://eclipse.org).

## Acknowledgements

This project uses the [GitHub Java API](https://github.com/eclipse/egit-github/tree/master/org.eclipse.egit.github.core)
built on top of [API v3](http://developer.github.com/).

It also uses many other open source libraries such as:

* [android-maven-plugin](https://github.com/jayway/maven-android-plugin)
* [CodeMirror](https://github.com/codemirror/CodeMirror)
* [RoboGuice](https://github.com/roboguice/roboguice)
* [ViewPagerIndicator](https://github.com/JakeWharton/Android-ViewPagerIndicator)

These are just a few of the major dependencies, the entire list of dependencies
is listed in the [app's POM file](https://github.com/github/android/blob/master/app/pom.xml).

## Contributing

Please fork this repository and contribute back using
[pull requests](https://github.com/github/android/pulls).

Any contributions, large or small, major features, bug fixes, additional
language translations, unit/integration tests are welcomed and appreciated
but will be thoroughly reviewed and discussed.
