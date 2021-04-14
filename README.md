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
4. Protocol version

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
SCAN_DURATION=10000
MIN_SCAN_INTERVAL=35000
MAX_SCAN_INTERVAL=60000

ADVERTISING_DURATION=180000
ADVERTISING_INTERVAL=5000

PURGE_INTERVAL=86400000
PURGE_TTL=1814400000
MAX_QUEUE_TIME=7000
BM_CHECK_INTERVAL=540000
HEALTH_CHECK_INTERVAL=900000
CONNECTION_TIMEOUT=6000
BLACKLIST_DURATION=100000

STAGING_SERVICE_UUID = "B82AB3FC-1595-4F6A-80F0-FE094CC218F9"

V2_CHARACTERISTIC_ID = "117BDD58-57CE-4E7A-8E87-7CCCDDA2A804"

PRODUCTION_SERVICE_UUID = "B82AB3FC-1595-4F6A-80F0-FE094CC218F9"

android.useAndroidX=true
android.enableJetifier=true
```

SERVICE_UUID and V2_CHARACTERISTIC_ID must match the iOS version or no cross platform scanning will occur.

SCAN_DURATION and SCAN_INTERVAL should be close to the iOS version or cross platform communication will be one sided 

---
               
### Build Configurations in build.gradle

Change the package name and other configurations accordingly such as the resValue in in the different settings in buildTypes For example,
```groovy
    buildTypes {
        debug {
            buildConfigField "String", "BLE_SSID", PRODUCTION_SERVICE_UUID
            resValue "string", "app_name", "TracerAppDebug"
        }

        release {
            buildConfigField "String", "BLE_SSID", PRODUCTION_SERVICE_UUID
            debuggable false
            jniDebuggable false
            renderscriptDebuggable false
            minifyEnabled false
            multiDexEnabled false
            zipAlignEnabled true
            resValue "string", "app_name", "TracerApp"
        }
    }
```

> Values such as BLE_SSID have been defined in gradle.properties as described above.

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
