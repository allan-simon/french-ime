#!/bin/bash
set -e

# Project name and package
PROJECT_NAME="FrenchIme"
PACKAGE_NAME="dot.dot"
PACKAGE_PATH="app/src/main/java/dot/dot/"

# Create directory structure
mkdir -p $PROJECT_NAME
cd $PROJECT_NAME
mkdir -p app/src/main/{java,res/{layout,values,drawable},kotlin}
mkdir -p $PACKAGE_PATH

# Create settings.gradle
cat << EOF > settings.gradle
rootProject.name = "$PROJECT_NAME"
include ':app'
EOF

# Create root build.gradle
cat << EOF > build.gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.4.2'
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
EOF

# Create app/build.gradle
cat << EOF > app/build.gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

android {
    namespace '$PACKAGE_NAME'
    compileSdk 33

    defaultConfig {
        applicationId "$PACKAGE_NAME"
        minSdk 31
        targetSdk 31
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
}

dependencies {
    implementation 'androidx.core:core-ktx:1.8.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
EOF

# Create AndroidManifest.xml
cat << EOF > app/src/main/AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Material3.DayNight">  <!-- Using Material3 theme -->

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">  <!-- Better handling of screen space -->
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
EOF

# Create MainActivity.kt
cat << EOF > $PACKAGE_PATH/MainActivity.kt
package $PACKAGE_NAME

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
EOF

# Create strings.xml
mkdir -p app/src/main/res/values
cat << EOF > app/src/main/res/values/strings.xml
<resources>
    <string name="app_name">$PROJECT_NAME</string>
</resources>
EOF

# Create activity_main.xml
mkdir -p app/src/main/res/layout
cat << EOF > app/src/main/res/layout/activity_main.xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Hello World!"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
EOF

# Create local.properties (assuming ANDROID_HOME is set)
echo "sdk.dir=$ANDROID_HOME" > local.properties

# Initialize Gradle wrapper
gradle wrapper

echo "Project $PROJECT_NAME created successfully!"
echo "To build the project:"
echo "  ./gradlew assembleDebug"
echo "To install on a connected device:"
echo "  ./gradlew installDebug"
