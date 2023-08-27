# Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

# zlib-ng
add_subdirectory(zlib-ng)
get_target_property(ZLIB_INCLUDE_DIRS zlibstatic PUBLIC_HEADER)
get_target_property(ZLIB_BINARY_DIR zlibstatic BINARY_DIR)

# mbedTLS
add_subdirectory(mbedtls)
get_target_property(MBEDTLS_INCLUDES mbedtls PUBLIC_HEADER)
get_target_property(MBEDTLS_LIBRARIES mbedtls BINARY_DIR)

if(CMAKE_GENERATOR STREQUAL Xcode)
    target_include_directories(tcvm PUBLIC "${ZLIB_INCLUDE_DIRS} ${MBEDTLS_INCLUDES}")

    set_target_properties(
        tcvm
        PROPERTIES

        # CMake ignores dependencies for static libraries, so we neeed to add their 
        # path to library search paths and later manually add the dependencies in XCode 
        # under Build Phases. More info here:
        # https://cmake.org/cmake/help/latest/prop_tgt/XCODE_LINK_BUILD_PHASE_MODE.html
        XCODE_ATTRIBUTE_LIBRARY_SEARCH_PATHS "${ZLIB_BINARY_DIR}/$(CONFIGURATION) ${MBEDTLS_LIBRARIES}/$(CONFIGURATION)"
    )
endif()

add_subdirectory(sqlite3) 
add_dependencies(tcvm SQLite3)

if(CMAKE_GENERATOR STREQUAL Xcode)
   get_target_property(SQLITE3_INCLUDES SQLite3 INTERFACE_INCLUDE_DIRECTORIES)
   target_include_directories(tcvm PRIVATE "${SQLITE3_INCLUDES}")
endif(CMAKE_GENERATOR STREQUAL Xcode)
