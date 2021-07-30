# ABTraceTogether Android app

ABTraceTogether is the Government of Alberta's Contact Tracing application that uses Herald.

Herald provides reliable Bluetooth communication and range finding across a wide range of mobile devices, allowing Contact Tracing and other applications to have regular and accurate information to make them highly effective. More information is available [here](./herald/Herald.md).
 
## Note
This is the open source release of ABTraceTogether, however any components considered sensitive and specific to Alberta Health Services have been removed. The following are the changes made for this release:

1. All licensed artwork has been replace with the transparent images that are of the same resolution
2. Changed package names
3. Renamed files, variables, and references specific to Alberta Health
4. Replaced font with an open source font
5. Removed mfp certificate file in assets and commented out code that pins said certificate
6. Removed all references to server names
7. Replaced sample config and build file in README.md file to be more generic

## Setup of the app
To get started on the app, setup and configure the following:

1. ./gradle.properties
2. ./app/build.gradle
3. IBM MobileFirst Platform
4. Google Maps API
5. Cache Duration Configurations
6. Protocol version

---
               
### Configs in gradle.properties

Sample Configuration:

```
ORG="CA_CA"
STORE_URL="<Your play store URL>"
PRIVACY_URL="<Your privacy policy URL>"
FAQ_URL="<Your FAQ URL>"
SHARE_URL="<Your share message URL>"

SERVICE_FOREGROUND_NOTIFICATION_ID=771579
SERVICE_FOREGROUND_CHANNEL_ID="CT Updates"
SERVICE_FOREGROUND_CHANNEL_NAME="CT Foreground Service"

PUSH_NOTIFICATION_ID=771578
PUSH_NOTIFICATION_CHANNEL_NAME="CT Notifications"

#service configurations
PURGE_INTERVAL=86400000
PURGE_TTL=1814400000
BM_CHECK_INTERVAL=540000
HEALTH_CHECK_INTERVAL=900000

android.useAndroidX=true
android.enableJetifier=true
```

---
               
### Build Configurations in build.gradle

Change the package name and other configurations accordingly such as the resValue in in the different settings in buildTypes For example,
```groovy
    buildTypes {
        debug {
            resValue "string", "app_name", "APP NAME"
        }

        release {
            debuggable false
            jniDebuggable false
            renderscriptDebuggable false
            minifyEnabled false
            zipAlignEnabled true
            resValue "string", "app_name", "APP NAME"
        }
    }
```

---

### IBM MobileFirst Platform

Information for which MFP server each environment will access is stored in the corresponding locations:

>Debug: ./app/src/debug/assets/mfpclient.properties

>Production: ./app/src/release/assets/mfpclient.properties

Pinning certificate for all environments is stored in the following location:

>./app/src/main/assets/mfpcertificate.cer

---

### Security Enhancements
SSL pinning is not included as part of the repository.
It is recommended to add in a check for SSL certificate returned by the backend.

---

### Google Maps API

This app uses Google Maps SDK. An active Google Maps API key is required for interactive map components to work.

Google Maps API keys can be specified in both `./debug.properties` and `./release.properties` files with key name `MAPS_API_KEY`.

---

### Cache Duration Configurations

Additional configuration parameters are specified in `./debug.properties` and `./release.properties` files with key names `STATS_CACHE_DURATION` and `URLS_CACHE_DURATION`.

These configurations determine how often the app retrieves new data from the server for statistics and urls. Values are in milliseconds.

---

### Statement from Google
The following is a statement from Google:
"At Google Play we take our responsibility to provide accurate and relevant information for our users very seriously. For that reason, we are currently only approving apps that reference COVID-19 or related terms in their store listing if the app is published, commissioned, or authorized by an official government entity or public health organization, and the app does not contain any monetization mechanisms such as ads, in-app products, or in-app donations. This includes references in places such as the app title, description, release notes, or screenshots.
For more information visit [https://android-developers.googleblog.com/2020/04/google-play-updates-and-information.html](https://android-developers.googleblog.com/2020/04/google-play-updates-and-information.html)"

---

### Acknowledgements
TraceTogether uses the following third party libraries / tools.

---

### ChangeLog
[1.0.0] * First release of open source repo

[1.1.0] * Added restart logic if app flow fails. Added null checking before accessing view elements. Added prompt to get user to update app if version is unsupported.

[1.2.0] * Added vulnerability fix and anonymity hardening

[2.0.0] * Added Herald for Bluetooth communication

[2.2.0] * New look & feel, Learn More section, test & symptom dates

[2.3.0] * New statistics and map screens, widgets, and pause scheduling