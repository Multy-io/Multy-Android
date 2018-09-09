# Multy-android

Multy - cryptocurrency and assets open-source wallet

To conrtibute please check **Build instruction

[Multy](http://multy.io)



## Build instruction

1. Clone master branch of the Multy-android project
```
git clone https://github.com/Appscrunch/Multy-android.git
```

2. Make shure you have CMake and NDK installed in Android Studio SDK Tools


3. Clone master branch of the Multy-Core project
```
git clone https://github.com/Appscrunch/Multy-Core.git
```

4. Checkout master branch of the Multy-Core project


5. Update path in CMakeLists.txt (External build files) file in Multy-Core project
```
set(MULTY_ANDROID_PATH_TO_JNI_WRAPPER "" CACHE PATH "PATH TO YOUR SCRATCH FILE HERE .../Multy-android/Multy-Android/app/src/main/cpp/scratch.cpp")
```

6. Open constants.gradle file and update your pathes to cmake and scatch.cpp files

7. In Android Studio make Gradle sync

8. In Android Studio
```
Build -> Clean Project
Build -> Refresh Linked C++ Project
Build -> Make Project
```

