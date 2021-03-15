# Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

cmake_minimum_required(VERSION 2.8.2)
# cmake policy required to use project property VERSION
cmake_policy(SET CMP0048 NEW)

include(ExternalProject)

project(turbojpeg
    VERSION 3.13.0.0
    LANGUAGES C
)
set(${PROJECT_NAME}_YEAR 2016)
# property <PROJECT>_HOMEPAGE_URL requires cmake 3.12
# using a variable instead we can keep greater compatibility
set(${PROJECT_NAME}_HOMEPAGE_URL https://sqlite.org)

# Place a dummy source at configuration time
# if(NOT EXISTS "${CMAKE_CURRENT_BINARY_DIR}/src/sqlite3.c")
#     configure_file(sqlite3_tc.h src/sqlite3.c)
# endif()

set(JPEG_TURBO_INSTALL_PREFIX ${CMAKE_CURRENT_BINARY_DIR}/libjpeg-turbo-install)

ExternalProject_Add(turbojpegDownload
    GIT_REPOSITORY https://github.com/libjpeg-turbo/libjpeg-turbo.git
    # SOURCE_DIR "${CMAKE_CURRENT_BINARY_DIR}/src"
    UPDATE_COMMAND ""
    CMAKE_GENERATOR $<IF:$<STREQUAL:"${CMAKE_SYSTEM_NAME}","Android">,"\"Unix Makefiles\"",${CMAKE_GENERATOR}>
    CMAKE_GENERATOR_PLATFORM ${CMAKE_GENERATOR_PLATFORM}
    CMAKE_GENERATOR_TOOLSET ${CMAKE_GENERATOR_TOOLSET}
    CMAKE_ARGS
        -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
        -DWITH_CRT_DLL=${WITH_CRT_DLL}
        -DENABLE_STATIC=ON
        -DENABLE_SHARED=OFF
        -DWITH_SIMD=${WITH_SIMD}
        -DCMAKE_INSTALL_PREFIX=${JPEG_TURBO_INSTALL_PREFIX}
        -DCMAKE_POSITION_INDEPENDENT_CODE=ON
        -DCMAKE_C_COMPILER=${CMAKE_C_COMPILER}
        -DCMAKE_CXX_COMPILER=${CMAKE_CXX_COMPILER}
        -DCMAKE_C_COMPILER_LAUNCHER=${CMAKE_C_COMPILER_LAUNCHER}
        -DCMAKE_CXX_COMPILER_LAUNCHER=${CMAKE_CXX_COMPILER_LAUNCHER}
    PATCH_COMMAND ""
    # patch -p0 <  ${CMAKE_CURRENT_LIST_DIR}/sqlite3tc.patch
    # CONFIGURE_COMMAND ""
    # BUILD_COMMAND ""
    # INSTALL_COMMAND ""
    TEST_COMMAND ""
    BUILD_BYPRODUCTS 
        ${JPEG_TURBO_INSTALL_PREFIX}/lib/libturbojpeg.a
)

add_library(turbojpeg STATIC IMPORTED GLOBAL)
# make sure SQLite3Download is executed first
add_dependencies(turbojpeg turbojpegDownload)

set_target_properties(turbojpeg 
    PROPERTIES 
        IMPORTED_LOCATION ${JPEG_TURBO_INSTALL_PREFIX}/lib/libturbojpeg.a)

        target_include_directories(tcvm PRIVATE 
        ${JPEG_TURBO_INSTALL_PREFIX}/include
    )

# target_include_directories(turbojpeg INTERFACE 
#     ${JPEG_TURBO_INSTALL_PREFIX}/include
# )


# # using INTERFACE no compilation is performed
# # add_library(jpeg-turbo INTERFACE)

# # make sure SQLite3Download is executed first
# # add_dependencies(jpeg-turbo jpeg-turboDownload)

# # target_include_directories(SQLite3 INTERFACE 
# #     "${CMAKE_CURRENT_BINARY_DIR}/src"
# #     ${CMAKE_CURRENT_LIST_DIR}
# # )
# # target_sources(SQLite3 INTERFACE 
# #     "${CMAKE_CURRENT_BINARY_DIR}/src/sqlite3.c"
# # )
