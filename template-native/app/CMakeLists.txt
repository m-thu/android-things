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
    native-app
    SHARED
    src/main/cpp/native-app.cpp
)

target_include_directories(
    native-app
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
    native-app
    android
    log
    native-app-glue
    androidthings
)


