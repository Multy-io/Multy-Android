# Multy-android

Multy - cryptocurrency and assets open-source wallet

To conrtibute please check **Build instructions

[Multy](http://multy.io)



## Build instructions

1. Clone master branch of the Multy-android project
```
git clone https://github.com/Multy-io/Multy-Android.git
```

2. Make shure you have CMake and NDK installed in Android Studio SDK Tools

3. Clone master branch of the Multy-Core project
```
git clone https://github.com/Multy-io/Multy-Core
```
4. Checkout master branch of the Multy-Core project
```
git checkout master
```

5. Update path in constants.gradle file in Multy-Core project
```
ext {
    DMULTY_ANDROID_PATH_TO_JNI_WRAPPER = '-DMULTY_ANDROID_PATH_TO_JNI_WRAPPER=/UPDATE_THIS_PATH/Multy-Android/app/src/main/cpp/scratch.cpp'
    CMAKE_PATH = '/UPDATE_THIS_PATH/Multy-Core/CMakeLists.txt'
}

```
6. In Android Studio make Gradle sync

```
7. In Android Studio
```
Build -> Clean Project
Build -> Refresh Linked C++ Project
Build -> Make Project
```

