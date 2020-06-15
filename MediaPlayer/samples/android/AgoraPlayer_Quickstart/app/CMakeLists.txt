# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

include_directories(src/main/cpp/include)
add_library( # Sets the name of the library.

             media_player_test
             # Sets the library as a shared library.
             SHARED
             src/main/cpp/media_player_test.cpp
              )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
set(PLAYER_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
add_library(agora_media_player SHARED IMPORTED)
set_target_properties(agora_media_player PROPERTIES IMPORTED_LOCATION ${PLAYER_PATH}/libAgoraMediaPLayer.so)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       media_player_test
                       android agora_media_player
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )