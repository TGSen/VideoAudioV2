# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)
file(GLOB cpp_source src/main/cpp/*.cpp src/main/cpp/*.c)
# Gradle automatically packages shared libraries with your APK.
include_directories(src/main/jniLibs/include)
set(LibDir ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})
add_library( # Sets the name of the library.
        imageBlur
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        ${cpp_source}
        )


# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.


find_library( # Sets the name of the path variable.
        jnigraphics-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        jnigraphics)


#设置编译指令


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        imageBlur
        # Links the target library to the log library
        # included in the NDK.
        ${jnigraphics-lib}
        android
        )