# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)

project(iPaySecurityKit LANGUAGES C CXX)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden")

add_library(security-lib SHARED
        src/main/cpp/security-lib.cpp)

find_library(log-lib log)

target_link_libraries(security-lib ${log-lib})