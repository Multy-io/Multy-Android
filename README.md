# Multy-android

Multy - cryptocurrency and assets open-source wallet

To conrtibute please check **Build instruction

[Multy](http://multy.io)



## Build instraction

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
```
git checkout master
```

5. Update path in CMakeLists.txt file in Multy-Core project
```
add_library(core_jni
       SHARED
       "/../Multy-android/app/src/main/cpp/scratch.cpp"
    )

```
6. Update constants.gradle files and paste your pases to cmake and scatch.cpp files

```
7. In Android Studio make Gradle sync

```
8. In Android Studio
```
Build -> Clean Project
Build -> Refresh Linked C++ Project
Build -> Make Project
```

