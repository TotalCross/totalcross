# Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

# zlib-ng
add_subdirectory(zlib-ng)
get_target_property(ZLIB_INCLUDE_DIRS zlibstatic PUBLIC_HEADER)
get_target_property(ZLIB_BINARY_DIR zlibstatic BINARY_DIR)

# png
add_subdirectory(png)
get_target_property(PNG_INCLUDE_DIRS png_static PUBLIC_HEADER)
get_target_property(PNG_BINARY_DIR png_static BINARY_DIR)

# mbedTLS
add_subdirectory(mbedtls)
get_target_property(MBEDTLS_INCLUDE_DIRS mbedtls PUBLIC_HEADER)
get_target_property(MBEDTLS_BINARY_DIR mbedtls BINARY_DIR)

# SQLite3
add_subdirectory(sqlite3) 
get_target_property(SQLITE3_INCLUDE_DIRS SQLite3 INTERFACE_INCLUDE_DIRECTORIES)

if(CMAKE_GENERATOR STREQUAL Xcode)
    target_include_directories(tcvm PUBLIC "${ZLIB_INCLUDE_DIRS} ${PNG_INCLUDE_DIRS} ${MBEDTLS_INCLUDE_DIRS} ${SQLITE3_INCLUDE_DIRS}")

    set_target_properties(
        tcvm
        PROPERTIES

        # CMake ignores dependencies for static libraries, so we neeed to add their 
        # path to library search paths and later manually add the dependencies in XCode 
        # under Build Phases. More info here:
        # https://cmake.org/cmake/help/latest/prop_tgt/XCODE_LINK_BUILD_PHASE_MODE.html
        XCODE_ATTRIBUTE_LIBRARY_SEARCH_PATHS "${ZLIB_BINARY_DIR}/$(CONFIGURATION) ${PNG_BINARY_DIR}/$(CONFIGURATION) ${MBEDTLS_BINARY_DIR}/$(CONFIGURATION)"
    )
endif()
