cmake_minimum_required(
    VERSION 3.4.1
)

# Add directory containing FindAndroidThings.cmake
set(
    CMAKE_MODULE_PATH
    ${CMAKE_MODULE_PATH}
    ${PROJECT_DIR}/libandroidthings
)

# Android Things headers and libraries
find_package(
    AndroidThings
    REQUIRED
)

add_library(
    lm75-native
    SHARED
    src/main/cpp/lm75-native.cpp
    src/main/cpp/lm75.cpp
)

target_include_directories(
    lm75-native
    PRIVATE
    ${ANDROID_NDK}/sources/android/native_app_glue
    ${ANDROIDTHINGS_INCLUDE_DIRS}
)

add_library(
    native-app-glue
    STATIC
    ${ANDROID_NDK}/sources/android/native_app_glue/android_native_app_glue.c
)

add_library(
    androidthings
    SHARED
    IMPORTED
)
set_target_properties(
    androidthings
    PROPERTIES IMPORTED_LOCATION
    ${ANDROIDTHINGS_LIBRARIES}
)

target_link_libraries(
    lm75-native
    android
    log
    native-app-glue
    androidthings
)


