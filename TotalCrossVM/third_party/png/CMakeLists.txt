cmake_minimum_required(VERSION 3.5)
cmake_policy(SET CMP0007 NEW) # CMP0007: list command no longer ignores empty elements.

include(FetchContent)

set(SKIP_INSTALL_ALL TRUE) # Skip default libPNG installation
FetchContent_Declare(
    libpng
    GIT_REPOSITORY  https://github.com/glennrp/libpng.git
    GIT_TAG         v1.6.48
    GIT_SHALLOW     TRUE
    GIT_PROGRESS    TRUE
    USES_TERMINAL_DOWNLOAD TRUE)

FetchContent_GetProperties(libpng)
if(NOT libpng_POPULATED)
    message("Setting up PNG...")

    FetchContent_Populate(libpng)
    set(PNG_SHARED OFF CACHE BOOL "" FORCE)
    set(PNG_FRAMEWORK OFF CACHE BOOL "" FORCE)
    set(PNG_TOOLS OFF CACHE BOOL "" FORCE)
    set(PNG_TESTS OFF CACHE BOOL "" FORCE)
    set(PNG_DEBUG_POSTFIX "" CACHE STRING "" FORCE) # XCode gets confused if used

    if(TARGET_WINCE)
        # Edit pnglibconf.h
        file(READ ${libpng_BINARY_DIR}/pnglibconf.h FILE_CONTENTS)
        # Adds PNG_ABORT definition for WinCE
        string(REPLACE "#endif /* PNGLCONF_H */" "\n#ifndef PNG_ABORT\n#ifdef _WIN32_WCE\n#  define PNG_ABORT() ExitProcess(0)\n#else\n#  define PNG_ABORT() abort()\n#endif\n#endif\n#endif  /* PNGLCONF_H */" FILE_CONTENTS "${FILE_CONTENTS}")
        SET(FILE_OUTPUT "")
        STRING(REGEX REPLACE ";" "\\\\;" FILE_CONTENTS "${FILE_CONTENTS}")
        STRING(REGEX REPLACE "\r?\n" ";" FILE_CONTENTS "${FILE_CONTENTS}")
        list(LENGTH FILE_CONTENTS FILE_LENGTH)
        math(EXPR N "${FILE_LENGTH}-1")
        foreach(FILE_INDEX RANGE ${N})
            list(GET FILE_CONTENTS ${FILE_INDEX} line)
            # Disable support for PNG WRITE
            string(REGEX REPLACE "^(#define PNG_.*WRITE.*_SUPPORTED)$" "//\\1" line "${line}")
            # Disable support for PNG_SAVE_INT_32_SUPPORTED
            string(REGEX REPLACE "^(#define PNG_SAVE_INT_32_SUPPORTED)$" "//\\1" line "${line}")
            SET(FILE_OUTPUT "${FILE_OUTPUT}${line}\n")
        endforeach()
        file(WRITE ${libpng_BINARY_DIR}/pnglibconf.h "${FILE_OUTPUT}")

        # Replace abort with PNG_ABORT in pngerror.c
        file(READ ${libpng_SOURCE_DIR}/pngerror.c FILE_CONTENTS)
        string(REPLACE "abort();" "PNG_ABORT();" FILE_CONTENTS "${FILE_CONTENTS}")
        file(WRITE ${libpng_SOURCE_DIR}/pngerror.c "${FILE_CONTENTS}")

        # Fix for WinCE, which lacks errno.h - pngread.c
        file(READ ${libpng_SOURCE_DIR}/pngread.c FILE_CONTENTS)
        string(REPLACE "#  include <errno.h>" "#if defined (WINCE)\nextern int errno;\n#define strerror(x) NULL\n#else\n#   include <errno.h>\n#endif" FILE_CONTENTS "${FILE_CONTENTS}")
        file(WRITE ${libpng_SOURCE_DIR}/pngread.c "${FILE_CONTENTS}")

        # Fix for WinCE, which lacks errno.h - pngwrite.c
        # file(READ ${libpng_SOURCE_DIR}/pngwrite.c FILE_CONTENTS)
        # string(REPLACE "#  include <errno.h>" "#if defined (WINCE)\nextern int errno;\n#define strerror(x) NULL\n#define remove(x) 0\n#else\n#   include <errno.h>\n#endif" FILE_CONTENTS "${FILE_CONTENTS}")
        # file(WRITE ${libpng_SOURCE_DIR}/pngwrite.c "${FILE_CONTENTS}")
    endif()

    add_subdirectory(${libpng_SOURCE_DIR} ${libpng_BINARY_DIR})

    # Need this with SKIP_INSTALL_ALL enabled to find the headers
    target_include_directories(png_static PUBLIC 
        ${libpng_SOURCE_DIR}
        ${libpng_BINARY_DIR}
    )

    set_target_properties(
        png_static
        PROPERTIES
        POSITION_INDEPENDENT_CODE ON
        PUBLIC_HEADER "${libpng_SOURCE_DIR} ${libpng_BINARY_DIR}"
        XCODE_ATTRIBUTE_IPHONEOS_DEPLOYMENT_TARGET 13.0
    )
endif()

# Our implementation using libpng
target_sources(tcvm PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/PngLoader.c")
