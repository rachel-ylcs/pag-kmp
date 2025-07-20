cmake_minimum_required(VERSION 3.16)
project(pag4j)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# https://github.com/microsoft/STL/issues/4730
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -D_DISABLE_CONSTEXPR_MUTEX_CONSTRUCTOR")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -D_DISABLE_CONSTEXPR_MUTEX_CONSTRUCTOR")

set(GRADLE_ROOT_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../..)

set(PAG_BUILD_SHARED OFF)
set(PAG_BUILD_FRAMEWORK OFF)
add_subdirectory(${GRADLE_ROOT_DIR}/libpag ${CMAKE_CURRENT_BINARY_DIR}/libpag)

find_package(Java COMPONENTS Runtime Development)
if(NOT Java_FOUND)
    message(FATAL_ERROR "PAG4J requires Java to build!")
    return()
endif()
find_package(JNI REQUIRED)

file(GLOB PAG4J_SOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/*.cpp
)

add_library(pag4j SHARED
    ${PAG4J_SOURCES}
)

target_include_directories(pag4j PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${JNI_INCLUDE_DIRS}
)

target_link_libraries(pag4j
    ${JNI_LIBRARIES}
    pag
)

set_target_properties(pag4j PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}
)

if(WIN32)
    file(REMOVE ${CMAKE_CURRENT_BINARY_DIR}/libEGL.dll)
    file(COPY ${GRADLE_ROOT_DIR}/libpag/third_party/tgfx/vendor/angle/win/x64/libEGL.dll DESTINATION ${CMAKE_CURRENT_BINARY_DIR})
    file(REMOVE ${CMAKE_CURRENT_BINARY_DIR}/libGLESv2.dll)
    file(COPY ${GRADLE_ROOT_DIR}/libpag/third_party/tgfx/vendor/angle/win/x64/libGLESv2.dll DESTINATION ${CMAKE_CURRENT_BINARY_DIR})
endif()
