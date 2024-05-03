# Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

# zlib
if (USE_MADLER_ZLIB)
    add_subdirectory(zlib)
else()
    add_subdirectory(zlib-ng)
endif()
get_target_property(ZLIB_INCLUDE_DIRS zlibstatic PUBLIC_HEADER)
get_target_property(ZLIB_BINARY_DIR zlibstatic BINARY_DIR)

# png
add_subdirectory(png)
get_target_property(PNG_INCLUDE_DIRS png_static PUBLIC_HEADER)
get_target_property(PNG_BINARY_DIR png_static BINARY_DIR)

# mbedTLS
if(NOT MSVC OR NOT ${CMAKE_GENERATOR} STREQUAL "Visual Studio 9 2008 Pocket PC 2003 (ARMV4)")
    add_subdirectory(mbedtls)
    get_target_property(MBEDTLS_INCLUDE_DIRS mbedtls PUBLIC_HEADER)
    get_target_property(MBEDTLS_BINARY_DIR mbedtls BINARY_DIR)
endif()

# SQLite3
add_subdirectory(sqlite3) 
get_target_property(SQLITE3_INCLUDE_DIRS SQLite3 PUBLIC_HEADER)
get_target_property(SQLITE3_BINARY_DIR SQLite3 BINARY_DIR)

if(CMAKE_GENERATOR STREQUAL Xcode)
    target_include_directories(tcvm PUBLIC "${ZLIB_INCLUDE_DIRS} ${PNG_INCLUDE_DIRS} ${MBEDTLS_INCLUDE_DIRS} ${SQLITE3_INCLUDE_DIRS}")

    set_target_properties(
        tcvm
        PROPERTIES

        # CMake ignores dependencies for static libraries, so we neeed to add their 
        # path to library search paths and later manually add the dependencies in XCode 
        # under Build Phases. More info here:
        # https://cmake.org/cmake/help/latest/prop_tgt/XCODE_LINK_BUILD_PHASE_MODE.html
        XCODE_ATTRIBUTE_LIBRARY_SEARCH_PATHS "${ZLIB_BINARY_DIR}/$(CONFIGURATION) ${PNG_BINARY_DIR}/$(CONFIGURATION) ${MBEDTLS_BINARY_DIR}/$(CONFIGURATION) ${SQLITE3_BINARY_DIR}/$(CONFIGURATION)"
    )
endif()
