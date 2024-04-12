cmake_minimum_required(VERSION 3.5)

include(FetchContent)

FetchContent_Declare(
    zlib
    GIT_REPOSITORY  https://github.com/madler/zlib.git
    GIT_TAG         v1.3.1
    GIT_SHALLOW     TRUE
    GIT_PROGRESS    TRUE
    USES_TERMINAL_DOWNLOAD TRUE)

FetchContent_GetProperties(zlib)
if(NOT zlib_POPULATED)
    message("Setting up zlib...")

    FetchContent_Populate(zlib)

    file(READ ${zlib_SOURCE_DIR}/zconf.h FILE_CONTENTS)
    string(REPLACE "#define ZCONF_H" "#define  ZCONF_H\n\n#define Z_SOLO\n" FILE_CONTENTS "${FILE_CONTENTS}")
    file(WRITE ${zlib_BINARY_DIR}/zconf.h "${FILE_CONTENTS}")
    configure_file(${zlib_SOURCE_DIR}/zlib.h ${zlib_BINARY_DIR}/zlib.h COPYONLY)

    set(ZLIB_PUBLIC_HDRS
        ${zlib_BINARY_DIR}/zconf.h
        ${zlib_BINARY_DIR}/zlib.h
    )
    set(ZLIB_PRIVATE_HDRS
        ${zlib_SOURCE_DIR}/crc32.h
        ${zlib_SOURCE_DIR}/deflate.h
        ${zlib_SOURCE_DIR}/inffast.h
        ${zlib_SOURCE_DIR}/inffixed.h
        ${zlib_SOURCE_DIR}/inflate.h
        ${zlib_SOURCE_DIR}/inftrees.h
        ${zlib_SOURCE_DIR}/trees.h
        ${zlib_SOURCE_DIR}/zutil.h
    )
    set(ZLIB_SRCS
        ${zlib_SOURCE_DIR}/adler32.c
        ${zlib_SOURCE_DIR}/compress.c
        ${zlib_SOURCE_DIR}/crc32.c
        ${zlib_SOURCE_DIR}/deflate.c
        ${zlib_SOURCE_DIR}/inflate.c
        ${zlib_SOURCE_DIR}/infback.c
        ${zlib_SOURCE_DIR}/inftrees.c
        ${zlib_SOURCE_DIR}/inffast.c
        ${zlib_SOURCE_DIR}/trees.c
        ${zlib_SOURCE_DIR}/uncompr.c
        ${zlib_SOURCE_DIR}/zutil.c
    )

    add_library(zlibstatic STATIC ${ZLIB_SRCS} ${ZLIB_PUBLIC_HDRS} ${ZLIB_PRIVATE_HDRS})

    target_include_directories(zlibstatic PUBLIC 
        ${zlib_BINARY_DIR}
    )

    target_compile_definitions(zlibstatic PRIVATE
        -DZ_SOLO
    )

    set_target_properties(
        zlibstatic
        PROPERTIES
        PUBLIC_HEADER "${zlib_BINARY_DIR}"
        XCODE_ATTRIBUTE_IPHONEOS_DEPLOYMENT_TARGET 13.0
    )
endif()
