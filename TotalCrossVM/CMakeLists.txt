# Copyright (C) 2020-2021 TotalCross Global Mobile Platform Ltda.
# Copyright (C) 2022-2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

cmake_minimum_required(VERSION 3.11)

set(TC_MSVC_RUNTIME_POLICY_AVAILABLE FALSE)

if(POLICY CMP0091)
  cmake_policy(SET CMP0091 NEW)
  set(CMAKE_MSVC_RUNTIME_LIBRARY
    "MultiThreaded$<$<CONFIG:Debug>:Debug>"
  )
  set(TC_MSVC_RUNTIME_POLICY_AVAILABLE TRUE)
endif()

include(FetchContent)

# set the project name
project(tcvm VERSION 7.2.2)

if(MSVC AND NOT TC_MSVC_RUNTIME_POLICY_AVAILABLE)
  foreach(flag_var
      CMAKE_C_FLAGS
      CMAKE_C_FLAGS_DEBUG
      CMAKE_C_FLAGS_RELEASE
      CMAKE_C_FLAGS_MINSIZEREL
      CMAKE_C_FLAGS_RELWITHDEBINFO
      CMAKE_CXX_FLAGS
      CMAKE_CXX_FLAGS_DEBUG
      CMAKE_CXX_FLAGS_RELEASE
      CMAKE_CXX_FLAGS_MINSIZEREL
      CMAKE_CXX_FLAGS_RELWITHDEBINFO)
    if(DEFINED ${flag_var})
      string(REGEX REPLACE "/MD" "/MT"
        ${flag_var} "${${flag_var}}")
    endif()
  endforeach()
endif()

set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

include(cmake/TCPlatform.cmake)
include(cmake/TCGraphics.cmake)

tc_generate_platform_config()

option(TC_BUILD_IR_TESTS "Build focused TCIR contract tests" OFF)
option(TC_BUILD_NATIVE_STARTUP_TEST "Build the native desktop startup test" OFF)
option(TC_BUILD_IR_BENCHMARKS "Build optional TCIR performance benchmarks" OFF)
option(TC_ENABLE_C_AOT "Build the experimental portable-C TCIR backend" OFF)
option(TC_ENABLE_SLJIT_JIT "Build the experimental SLJIT TCIR backend" OFF)
option(TC_ENABLE_COMPILED_DISPATCH "Integrate experimental TCIR dispatch into executeMethod" OFF)

if(TC_BUILD_IR_BENCHMARKS AND NOT TC_ENABLE_SLJIT_JIT)
  message(FATAL_ERROR "TC_BUILD_IR_BENCHMARKS requires TC_ENABLE_SLJIT_JIT=ON")
endif()

set(TCIR_SOURCES
  ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/ir/tcir.c
  ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/ir/tcir_decode.c
  ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/ir/tcir_dump.c
  ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/ir/tcir_frontend.c
  ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/ir/tcir_interp.c
  ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/ir/tcir_opcode_map.c
  ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/ir/tcir_verify.c
)

add_library(tcir STATIC ${TCIR_SOURCES})
target_include_directories(tcir PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/ir)
set_target_properties(tcir PROPERTIES
  C_STANDARD 99
  C_STANDARD_REQUIRED YES
  C_EXTENSIONS NO
)

if(TC_ENABLE_C_AOT)
  add_library(tcir_aot STATIC
    ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/aot/tcir_aot.c
  )
  target_include_directories(tcir_aot PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/aot
  )
  target_link_libraries(tcir_aot PUBLIC tcir)
  target_compile_options(tcir_aot PRIVATE
    $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>>:-Wall;-Wextra;-Werror>
  )
  set_target_properties(tcir_aot PROPERTIES
    C_STANDARD 11
    C_STANDARD_REQUIRED YES
    C_EXTENSIONS NO
  )

  if(NOT CMAKE_CROSSCOMPILING)
    add_executable(tcaot
      ${CMAKE_CURRENT_SOURCE_DIR}/src/tools/tcaot.c
    )
    target_include_directories(tcaot PRIVATE
      ${CMAKE_CURRENT_SOURCE_DIR}/src/tests/ir
    )
    target_link_libraries(tcaot PRIVATE tcir_aot)
    target_compile_options(tcaot PRIVATE
      $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>>:-Wall;-Wextra;-Werror>
    )
    set_target_properties(tcaot PROPERTIES
      RUNTIME_OUTPUT_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/tools"
      C_STANDARD 11
      C_STANDARD_REQUIRED YES
      C_EXTENSIONS NO
    )
  endif()
endif()

if(TC_BUILD_IR_TESTS)
  enable_testing()
  find_program(TCIR_PYTHON_EXECUTABLE NAMES python3)
  if(NOT TCIR_PYTHON_EXECUTABLE)
    message(FATAL_ERROR "TC_BUILD_IR_TESTS requires a Python 3 interpreter")
  endif()

  add_executable(tcir_tests
    ${CMAKE_CURRENT_SOURCE_DIR}/src/tests/ir/tcir_tests.c
  )
  target_link_libraries(tcir_tests tcir)
  target_compile_definitions(tcir_tests PRIVATE
    TCIR_GOLDEN_DIR="${CMAKE_CURRENT_SOURCE_DIR}/src/tests/ir/golden"
  )
  target_compile_options(tcir_tests PRIVATE
    $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>>:-Wall;-Wextra;-Werror>
  )
  set_target_properties(tcir_tests PROPERTIES
    C_STANDARD 99
    C_STANDARD_REQUIRED YES
    C_EXTENSIONS NO
  )

  add_test(NAME tcir-core COMMAND tcir_tests)
  add_test(
    NAME tcir-opcode-sources
    COMMAND ${TCIR_PYTHON_EXECUTABLE}
      ${CMAKE_CURRENT_SOURCE_DIR}/../scripts/validate-tcir-opcodes.py
      --root ${CMAKE_CURRENT_SOURCE_DIR}/..
  )
  add_custom_target(check-tcir
    COMMAND ${CMAKE_CTEST_COMMAND} --output-on-failure -R "^tcir-"
    DEPENDS tcir_tests
    USES_TERMINAL
  )
endif()


if(NOT DEFINED BUILD_SHARED_LIBS AND NOT DEFINED TCVM_SHARED)
  if(TC_GENERATOR_XCODE)
    set(TCVM_SHARED OFF) 
    message("tcvm: ${TCVM_SHARED}")  
  else() # others
    set(TCVM_SHARED ON) 
  endif()
endif()

if (NOT DEFINED SKIA_AUTO_FETCH)
  set(SKIA_AUTO_FETCH ON)
endif()

if (NOT DEFINED SDL2_AUTO_FETCH)
  set(SDL2_AUTO_FETCH ON)
endif()

if (NOT DEFINED SQLITE3_AUTO_FETCH)
  set(SQLITE3_AUTO_FETCH ON)
endif()

if (NOT DEFINED MBEDTLS_AUTO_FETCH)
  set(MBEDTLS_AUTO_FETCH ON)
endif()

if (NOT DEFINED ZLIBNG_AUTO_FETCH)
  set(ZLIBNG_AUTO_FETCH ON)
endif()

if (NOT DEFINED MINIZIP_NG_AUTO_FETCH)
  set(MINIZIP_NG_AUTO_FETCH ON)
endif()

if (NOT DEFINED LIBPNG_AUTO_FETCH)
  set(LIBPNG_AUTO_FETCH ON)
endif()

if (NOT DEFINED LIBJPEG_TURBO_AUTO_FETCH)
  set(LIBJPEG_TURBO_AUTO_FETCH ON)
endif()

if (NOT DEFINED AXTLS_AUTO_FETCH)
  set(AXTLS_AUTO_FETCH ON)
endif()

if (NOT DEFINED QRCODE_AUTO_FETCH)
  set(QRCODE_AUTO_FETCH ON)
endif()

if (NOT DEFINED QRCODEGEN_AUTO_FETCH)
  set(QRCODEGEN_AUTO_FETCH ON)
endif()

if (NOT DEFINED SLJIT_AUTO_FETCH)
  set(SLJIT_AUTO_FETCH ON)
endif()

if(TCVM_SHARED)
  add_library(tcvm SHARED)
else()
  add_library(tcvm STATIC)
endif()

target_include_directories(
  tcvm
  PRIVATE
    "${TC_PLATFORM_GENERATED_INCLUDE_DIR}"
    "${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm"
)

target_compile_definitions(
  tcvm
  PRIVATE
    TC_PLATFORM_CONFIGURED=1
)

# Android fail-fast
if(TC_TARGET_ANDROID)
  if(NOT DEFINED TC_JNI_HEADER_DIR)
    message(
      FATAL_ERROR
      "TC_JNI_HEADER_DIR must be provided for Android builds"
    )
  endif()

  target_include_directories(
    tcvm
    PRIVATE
      "${TC_JNI_HEADER_DIR}"
  )
endif()

# qrcodegen is the default backend and is mandatory for _MSC_VER.
set(TC_QRCODE_MSVC_COMPATIBLE OFF)
set(TC_QRCODE_USE_QRCODEGEN_DEFAULT ON)
if(MSVC OR CMAKE_C_SIMULATE_ID STREQUAL "MSVC")
  set(TC_QRCODE_MSVC_COMPATIBLE ON)
endif()

option(TC_QRCODE_USE_QRCODEGEN
  "Use qrcodegen instead of qrcode to generate QR codes"
  ${TC_QRCODE_USE_QRCODEGEN_DEFAULT})

if(TC_QRCODE_MSVC_COMPATIBLE AND NOT TC_QRCODE_USE_QRCODEGEN)
  message(FATAL_ERROR "TC_QRCODE_USE_QRCODEGEN must be ON when compiling with _MSC_VER")
endif()

if(TC_QRCODE_USE_QRCODEGEN)
  target_compile_definitions(tcvm PRIVATE TC_QRCODE_USE_QRCODEGEN=1)
else()
  target_compile_definitions(tcvm PRIVATE TC_QRCODE_USE_QRCODEGEN=0)
endif()

# Force using madler zlib on wince instead of zlib-ng, which isn't supported
if(NOT DEFINED USE_MADLER_ZLIB)
  if(TC_TARGET_WINCE)
    set(USE_MADLER_ZLIB ON)
  endif()
endif()

# Support defining PNG_ARM_NEON_OPT externally
if(DEFINED PNG_ARM_NEON_OPT)
  add_definitions(-DPNG_ARM_NEON_OPT=${PNG_ARM_NEON_OPT})
endif(DEFINED PNG_ARM_NEON_OPT)

# Find native dependencies from totalcross-depot-tools
set(TCVM_DEPOT_TOOLS_DIR "${CMAKE_SOURCE_DIR}/deps/totalcross-depot-tools" CACHE PATH "totalcross-depot-tools checkout")

if(NOT EXISTS "${TCVM_DEPOT_TOOLS_DIR}/deps.yml")
  find_program(TCVM_BASH_EXECUTABLE bash)
  if(NOT TCVM_BASH_EXECUTABLE)
    message(FATAL_ERROR "Unable to fetch totalcross-depot-tools because 'bash' was not found")
  endif()

  execute_process(
    COMMAND "${CMAKE_COMMAND}" -E env
      "TOTALCROSS_DEPOT_TOOLS_DIR=${TCVM_DEPOT_TOOLS_DIR}"
      "${TCVM_BASH_EXECUTABLE}" "${CMAKE_SOURCE_DIR}/deps/fetch-depot-tools.sh"
    WORKING_DIRECTORY "${CMAKE_SOURCE_DIR}"
    RESULT_VARIABLE TCVM_DEPOT_TOOLS_FETCH_RESULT
    OUTPUT_VARIABLE TCVM_DEPOT_TOOLS_FETCH_STDOUT
    ERROR_VARIABLE TCVM_DEPOT_TOOLS_FETCH_STDERR
  )

  if(NOT TCVM_DEPOT_TOOLS_FETCH_RESULT EQUAL 0)
    message(FATAL_ERROR
      "Failed to fetch totalcross-depot-tools.\n"
      "stdout:\n${TCVM_DEPOT_TOOLS_FETCH_STDOUT}\n"
      "stderr:\n${TCVM_DEPOT_TOOLS_FETCH_STDERR}"
    )
  endif()
endif()

set(CMAKE_MODULE_PATH
  "${TCVM_DEPOT_TOOLS_DIR}/sdl2/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/qrcode/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/qrcodegen/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/zlib-ng/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/minizip-ng/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/libpng/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/libjpeg-turbo/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/sqlite3/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/mbedtls/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/axtls/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/sljit/cmake"
  "${TCVM_DEPOT_TOOLS_DIR}/skia/cmake"
  "${CMAKE_SOURCE_DIR}/modules"
)

foreach(TCVM_DEP_CACHE_VAR
    ZLIB_DIR
    ZLIB_INCLUDE_DIR
    ZLIB_LIBRARY
    MINIZIP_INCLUDE_DIR
    MINIZIP_LIBRARY
    PNG_DIR
    PNG_INCLUDE_DIR
    PNG_LIBRARY
    JPEG_DIR
    JPEG_INCLUDE_DIR
    JPEG_LIBRARY
    JPEG_LIBRARIES
    SQLITE3_DIR
    SQLite3_INCLUDE_DIR
    SQLite3_LIBRARY
    MBEDTLS_DIR
    MbedTLS_INCLUDE_DIR
    MbedTLS_LIBRARY
    MbedCrypto_LIBRARY
    MbedX509_LIBRARY
    AXTLS_DIR
    AXTLS_INCLUDE_DIR
    AXTLS_LIBRARY
    QRCODE_DIR
    QRCODE_INCLUDE_DIR
    QRCODE_LIBRARY
    QRCODEGEN_DIR
    QRCODEGEN_INCLUDE_DIR
    QRCODEGEN_LIBRARY
    SLJIT_DIR
    SLJIT_INCLUDE_DIR
    SLJIT_LIBRARY
    SDL2_DIR
    SDL2_INCLUDE_DIR
    SDL2_LIBRARY
    SDL2_LIBRARY_TEMP
    SDL2MAIN_LIBRARY
    SKIA_CONFIG_INCLUDE_DIR
    SKIA_CORE_INCLUDE_DIR
    SKIA_UTILS_INCLUDE_DIR
    SKIA_EFFECTS_INCLUDE_DIR
    SKIA_GPU_INCLUDE_DIR
    SKIA_GPU2_INCLUDE_DIR
    SKIA_LIBRARY
    SKIA_LIBRARY_DIRS
    SKIA_LIBRARIES)
  unset(${TCVM_DEP_CACHE_VAR} CACHE)
  unset(${TCVM_DEP_CACHE_VAR})
endforeach()

if(TC_ENABLE_SLJIT_JIT)
  include("${TCVM_DEPOT_TOOLS_DIR}/sljit/cmake/AutoFetchSLJIT.cmake")
  if(SLJIT_AUTO_FETCH)
    tcvm_auto_fetch_sljit()
  endif()
  find_package(SLJIT REQUIRED)

  add_library(tcir_jit STATIC
    ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/jit/tcir_jit.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/jit/tcir_jit_cache.c
    ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/jit/tcir_jit_memory.c
  )
  target_include_directories(tcir_jit PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/jit
  )
  target_link_libraries(tcir_jit PUBLIC tcir PRIVATE SLJIT::SLJIT)
  target_compile_options(tcir_jit PRIVATE
    $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>>:-Wall;-Wextra;-Werror>
  )
  set_target_properties(tcir_jit PROPERTIES
    C_STANDARD 11
    C_STANDARD_REQUIRED YES
    C_EXTENSIONS NO
  )
  target_link_libraries(tcvm PRIVATE tcir_jit)
endif()

if(NOT USE_MADLER_ZLIB)
  include("${TCVM_DEPOT_TOOLS_DIR}/zlib-ng/cmake/AutoFetchZlibNg.cmake")
  if(ZLIBNG_AUTO_FETCH)
    tcvm_auto_fetch_zlibng()
  endif()
endif()

# Find axTLS
include("${TCVM_DEPOT_TOOLS_DIR}/axtls/cmake/AutoFetchAxTLS.cmake")
if(AXTLS_AUTO_FETCH)
  tcvm_auto_fetch_axtls()
endif()
find_package(AxTLS REQUIRED)

if(TC_QRCODE_USE_QRCODEGEN)
  include("${TCVM_DEPOT_TOOLS_DIR}/qrcodegen/cmake/AutoFetchQRCodeGen.cmake")
  if(QRCODEGEN_AUTO_FETCH)
    tcvm_auto_fetch_qrcodegen()
  endif()
  find_package(QRCodeGen REQUIRED)
  set(TC_QRCODE_LIBRARY QRCodeGen::QRCodeGen)
else()
  include("${TCVM_DEPOT_TOOLS_DIR}/qrcode/cmake/AutoFetchQRCode.cmake")
  if(QRCODE_AUTO_FETCH)
    tcvm_auto_fetch_qrcode()
  endif()
  find_package(QRCode REQUIRED)
  set(TC_QRCODE_LIBRARY QRCode::QRCode)
endif()

if(TC_TARGET_WINCE)
  if(NOT DEFINED MINIZIP_DIR)
    message(FATAL_ERROR
      "WinCE requires an externally supplied minizip-ng prebuilt. "
      "Set MINIZIP_DIR to a directory containing include/ and lib/."
    )
  endif()
else()
  include("${TCVM_DEPOT_TOOLS_DIR}/minizip-ng/cmake/AutoFetchMinizipNg.cmake")
  if(MINIZIP_NG_AUTO_FETCH)
    tcvm_auto_fetch_minizipng()
  endif()
endif()
find_package(MINIZIP REQUIRED)

# Find libpng
if(NOT TC_TARGET_WINCE)
  include("${TCVM_DEPOT_TOOLS_DIR}/libpng/cmake/AutoFetchLibPng.cmake")
  if(LIBPNG_AUTO_FETCH)
    tcvm_auto_fetch_libpng()
  endif()
endif()

# Find libjpeg-turbo
include("${TCVM_DEPOT_TOOLS_DIR}/libjpeg-turbo/cmake/AutoFetchLibJpegTurbo.cmake")
if(LIBJPEG_TURBO_AUTO_FETCH)
  tcvm_auto_fetch_libjpeg_turbo()
endif()
find_package(JPEG REQUIRED)
if(NOT JPEG_LIBRARY AND JPEG_LIBRARIES)
  list(GET JPEG_LIBRARIES 0 JPEG_LIBRARY)
endif()
if(NOT JPEG_LIBRARIES)
  set(JPEG_LIBRARIES "${JPEG_LIBRARY}")
endif()
if(NOT JPEG_INCLUDE_DIR AND JPEG_INCLUDE_DIRS)
  list(GET JPEG_INCLUDE_DIRS 0 JPEG_INCLUDE_DIR)
endif()
if(NOT JPEG_INCLUDE_DIRS)
  set(JPEG_INCLUDE_DIRS "${JPEG_INCLUDE_DIR}")
endif()
if(NOT TARGET JPEG::JPEG)
  add_library(JPEG::JPEG UNKNOWN IMPORTED GLOBAL)
  set_target_properties(JPEG::JPEG PROPERTIES
    IMPORTED_LOCATION "${JPEG_LIBRARY}"
    INTERFACE_INCLUDE_DIRECTORIES "${JPEG_INCLUDE_DIRS}"
  )
endif()
get_filename_component(JPEG_LIBRARY_DIR "${JPEG_LIBRARY}" DIRECTORY)

# Find SQLite3
include("${TCVM_DEPOT_TOOLS_DIR}/sqlite3/cmake/AutoFetchSQLite3.cmake")
if(SQLITE3_AUTO_FETCH)
  tcvm_auto_fetch_sqlite3()
endif()
find_package(SQLite3 REQUIRED)
set(SQLITE3_LIBRARIES "${SQLite3_LIBRARY}")
set(SQLITE3_INCLUDE_DIRS "${SQLite3_INCLUDE_DIR}")
get_filename_component(SQLITE3_LIBRARY_DIR "${SQLite3_LIBRARY}" DIRECTORY)
include_directories("${SQLite3_INCLUDE_DIR}")

# Find mbedTLS
if(NOT TC_TARGET_WINCE)
  include("${TCVM_DEPOT_TOOLS_DIR}/mbedtls/cmake/AutoFetchMbedTLS.cmake")
  if(MBEDTLS_AUTO_FETCH)
    tcvm_auto_fetch_mbedtls()
  endif()
  find_package(MbedTLS REQUIRED)
  set(MBEDTLS_LIBRARIES "${MbedTLS_LIBRARY};${MbedX509_LIBRARY};${MbedCrypto_LIBRARY}")
  set(MBEDTLS_INCLUDE_DIRS "${MbedTLS_INCLUDE_DIR}")
  get_filename_component(MBEDTLS_LIBRARY_DIR "${MbedTLS_LIBRARY}" DIRECTORY)
endif()

add_subdirectory(third_party) 

if(TC_GENERATOR_XCODE)
  target_link_libraries(tcvm PRIVATE zlibstatic png_static JPEG::JPEG SQLite::SQLite3 MINIZIP::MINIZIP AxTLS::AxTLS ${TC_QRCODE_LIBRARY})
  if(NOT TC_TARGET_WINCE)
    target_link_libraries(tcvm PRIVATE
      MbedTLS::mbedtls
      MbedTLS::mbedx509
      MbedTLS::mbedcrypto
    )
  endif()
else()
  target_link_libraries(tcvm PRIVATE zlibstatic)
  target_link_libraries(tcvm PRIVATE ${TC_QRCODE_LIBRARY})
  target_link_libraries(tcvm PRIVATE MINIZIP::MINIZIP)
  target_link_libraries(tcvm PRIVATE AxTLS::AxTLS)
  target_link_libraries(tcvm PRIVATE png_static)
  target_link_libraries(tcvm PRIVATE JPEG::JPEG)
  target_link_libraries(tcvm PRIVATE ${SQLITE3_LIBRARIES})
  if(NOT TC_TARGET_WINCE)
    target_link_libraries(tcvm PRIVATE MbedTLS::mbedtls)
  endif()
endif()

if(TC_TARGET_APPLE)
  find_library(TCVM_COREFOUNDATION_FRAMEWORK CoreFoundation REQUIRED)
  find_library(TCVM_SECURITY_FRAMEWORK Security REQUIRED)
  if(TC_GENERATOR_XCODE)
    target_link_libraries(tcvm PRIVATE
      "${TCVM_COREFOUNDATION_FRAMEWORK}"
      "${TCVM_SECURITY_FRAMEWORK}"
    )
  else()
    target_link_libraries(tcvm PRIVATE
      "${TCVM_COREFOUNDATION_FRAMEWORK}"
      "${TCVM_SECURITY_FRAMEWORK}"
    )
  endif()
endif()

# Find SDL2 from depot-tools when SDL windowing is selected.
if(TC_WINDOWING_SDL)
  include("${TCVM_DEPOT_TOOLS_DIR}/sdl2/cmake/AutoFetchSDL2.cmake")
  if(SDL2_AUTO_FETCH)
    tcvm_auto_fetch_sdl2()
  endif()
  find_package(SDL2 REQUIRED)
  target_link_libraries(tcvm PRIVATE SDL2::SDL2)
endif()

# Find Skia
include("${TCVM_DEPOT_TOOLS_DIR}/skia/cmake/AutoFetchSkia.cmake")
if(TC_RENDERER_SKIA)
  if(SKIA_AUTO_FETCH)
    tcvm_auto_fetch_skia()
  endif()
  find_package(Skia REQUIRED)
  add_definitions(-DUSE_SKIA)
  set(SKIA_DIR "${TCVM_DEPOT_TOOLS_DIR}/skia/local" CACHE PATH "Skia directory" FORCE)
  target_link_libraries(tcvm PRIVATE Skia::Skia)
endif(TC_RENDERER_SKIA)

set(TC_SRCDIR "${CMAKE_CURRENT_SOURCE_DIR}/src")
set(IOS_BUNDLE_SRCDIR "${CMAKE_CURRENT_SOURCE_DIR}/xcode")

set(SOURCES
    ${TC_SRCDIR}/nm/crypto/PBKDF2WithHmacSHA1.c

    ${TC_SRCDIR}/tcvm/tcthread.c
    ${TC_SRCDIR}/tcvm/tcclass.c
    ${TC_SRCDIR}/tcvm/objectmemorymanager.c
    ${TC_SRCDIR}/tcvm/tcmethod.c
    ${TC_SRCDIR}/tcvm/tcfield.c
    ${TC_SRCDIR}/tcvm/context.c
    ${TC_SRCDIR}/tcvm/tcexception.c
    ${TC_SRCDIR}/tcvm/tcvm.c

    ${TC_SRCDIR}/init/demo.c
    ${TC_SRCDIR}/init/globals.c
    ${TC_SRCDIR}/init/nativeProcAddressesTC.c
    ${TC_SRCDIR}/init/startup.c
    ${TC_SRCDIR}/init/settings.c

    ${TC_SRCDIR}/util/jchar.c
    ${TC_SRCDIR}/util/datastructures.c
    ${TC_SRCDIR}/util/debug.c
    ${TC_SRCDIR}/util/tcz.c
    ${TC_SRCDIR}/util/utils.c
    ${TC_SRCDIR}/util/dlmalloc.c
    ${TC_SRCDIR}/util/mem.c
    ${TC_SRCDIR}/util/errormsg.c
    ${TC_SRCDIR}/util/nativelib.c
    ${TC_SRCDIR}/util/guid.c
    ${TC_SRCDIR}/util/xtypes.c

    ${TC_SRCDIR}/ras/ras_Utils.c

    ${TC_SRCDIR}/nm/map/GoogleMaps.c

    ${TC_SRCDIR}/nm/crypto/AESCipher.c
    ${TC_SRCDIR}/nm/crypto/MD5Digest.c
    ${TC_SRCDIR}/nm/crypto/PKCS1Signature.c
    ${TC_SRCDIR}/nm/crypto/RSACipher.c
    ${TC_SRCDIR}/nm/crypto/SHA1Digest.c
    ${TC_SRCDIR}/nm/crypto/SHA256Digest.c

    ${TC_SRCDIR}/nm/io/PDBFile.c
    ${TC_SRCDIR}/nm/io/File.c
    ${TC_SRCDIR}/nm/io/device_PortConnector.c
    ${TC_SRCDIR}/nm/io/device/RadioDevice.c
    ${TC_SRCDIR}/nm/io/device/scanner/zxing.c
    ${TC_SRCDIR}/nm/io/device/gps/GPS.c

    ${TC_SRCDIR}/nm/io/device/bluetooth/DiscoveryAgent.c
    ${TC_SRCDIR}/nm/io/device/bluetooth/SerialPortClient.c
    ${TC_SRCDIR}/nm/io/device/bluetooth/SerialPortServer.c

    ${TC_SRCDIR}/nm/lang/Reflection.c
    ${TC_SRCDIR}/nm/lang/Class.c
    ${TC_SRCDIR}/nm/lang/Runtime.c
    ${TC_SRCDIR}/nm/lang/Object.c
    ${TC_SRCDIR}/nm/lang/String.c
    ${TC_SRCDIR}/nm/lang/StringBuffer.c
    ${TC_SRCDIR}/nm/lang/Thread.c
    ${TC_SRCDIR}/nm/lang/Throwable.c
    ${TC_SRCDIR}/nm/lang/Process.c
    ${TC_SRCDIR}/nm/nio/channels/FileChannelImpl.c

    ${TC_SRCDIR}/nm/net/ssl_SSL.c
    ${TC_SRCDIR}/nm/net/ServerSocket.c
    ${TC_SRCDIR}/nm/net/Socket.c
    ${TC_SRCDIR}/nm/net/ConnectionManager.c

    ${TC_SRCDIR}/nm/pim/POutlook.c
    
    ${TC_SRCDIR}/nm/notification/notification.c
    
    ${TC_SRCDIR}/nm/phone/Dial.c
    ${TC_SRCDIR}/nm/phone/SMS.c
    ${TC_SRCDIR}/nm/phone/CellInfo.c
    ${TC_SRCDIR}/nm/telephony/TelephonyManager.c
    ${TC_SRCDIR}/nm/telephony/SmsManager.c
    ${TC_SRCDIR}/nm/firebase/firebase.c

    ${TC_SRCDIR}/nm/sys/CharacterConverter.c
    ${TC_SRCDIR}/nm/sys/Registry.c
    ${TC_SRCDIR}/nm/sys/Vm.c
    ${TC_SRCDIR}/nm/sys/Time.c
    ${TC_SRCDIR}/nm/sys/Convert.c

    ${TC_SRCDIR}/nm/ui/gfx_Graphics.c
    ${TC_SRCDIR}/nm/ui/event_Event.c
    ${TC_SRCDIR}/nm/ui/Control.c
    ${TC_SRCDIR}/nm/ui/font_Font.c
    ${TC_SRCDIR}/nm/ui/font_FontMetrics.c
    ${TC_SRCDIR}/nm/ui/image_Image.c
    ${TC_SRCDIR}/nm/ui/image/ImageEncodedBag.c
    ${TC_SRCDIR}/nm/ui/MainWindow.c
    ${TC_SRCDIR}/nm/ui/media_Sound.c
    ${TC_SRCDIR}/nm/ui/media_MediaClip.c
    ${TC_SRCDIR}/nm/ui/media_Camera.c
    ${TC_SRCDIR}/nm/ui/YoutubePlayer.c
    ${TC_SRCDIR}/nm/ui/Window.c
    ${TC_SRCDIR}/nm/ui/WindowStartup.c
    ${TC_SRCDIR}/nm/ui/dialog_FilePicker.c

    ${TC_SRCDIR}/nm/util/concurrent_Lock.c
    ${TC_SRCDIR}/nm/util/zip_ZLib.c
    ${TC_SRCDIR}/nm/util/BigInteger.c
    ${TC_SRCDIR}/nm/util/Locale.c

    ${TC_SRCDIR}/nm/util/zip/CompressedStream.c
    ${TC_SRCDIR}/nm/util/zip/ZipFile.c
    ${TC_SRCDIR}/nm/util/zip/ZipEntry.c

    ${TC_SRCDIR}/event/Event.c
    ${TC_SRCDIR}/event/specialkeys.c

    ${TC_SRCDIR}/palmdb/palmdb.c

    ${TC_SRCDIR}/nm/db/NativeDB.c

    ${TC_SRCDIR}/scanner/ios/ios_barcode.c

    ${TC_SRCDIR}/nm/qrcode/qrcode_generation.c

    ${TC_SRCDIR}/nm/io/device/gpiod/Gpiod.c

    )

if(UNIX AND NOT (TC_TARGET_ANDROID OR TC_TARGET_APPLE))
  set(SOURCES
    ${SOURCES}
    ${TC_SRCDIR}/nm/io/device/gpiod/posix/gpiodLib.c
  )
endif(UNIX AND NOT (TC_TARGET_ANDROID OR TC_TARGET_APPLE))

if(MSVC)
  set(TC_MSVC_SOURCES
    ${TC_SRCDIR}/util/winsockLib.c
  )
endif(MSVC)

if(NOT MSVC)
  set(TC_NOT_MSVC_SOURCES
    ${TC_SRCDIR}/nm/lang/posix/cpproc.c
  )
endif(NOT MSVC)

if(TC_RENDERER_SKIA)
  set(TC_SKIA_SOURCES
    ${TC_SRCDIR}/nm/ui/skia/skia.cpp
    ${TC_SRCDIR}/nm/ui/skia/skia_surface.cpp
    ${TC_SRCDIR}/nm/ui/skia/skia_primitives.cpp
  )
endif(TC_RENDERER_SKIA)

if(TC_GENERATOR_XCODE)
  set(TCVM_iOS_FILES
    ${IOS_BUNDLE_SRCDIR}/tcvm/libtcvm.h
    ${IOS_BUNDLE_SRCDIR}/tcvm/libtcvm.m
    ${IOS_BUNDLE_SRCDIR}/tcvm/tcvm-Prefix.pch
    
    #event\\darwin
    ${TC_SRCDIR}/event/darwin/event.m

    #init\\darwin
    ${TC_SRCDIR}/init/darwin/demo_c.m
    ${TC_SRCDIR}/init/darwin/serialnum.m
    ${TC_SRCDIR}/init/darwin/startup_c.m
    ${TC_SRCDIR}/init/darwin/serialnum.m

    #util\\darwin
    ${TC_SRCDIR}/util/darwin/debug_c.m
    
    #nm\\ui\\darwin
    ${TC_SRCDIR}/nm/ui/darwin/mainview.m
    ${TC_SRCDIR}/nm/ui/darwin/media_Camera_c.m
    ${TC_SRCDIR}/nm/ui/darwin/Window_c.m
    ${TC_SRCDIR}/nm/ui/darwin/YoutubePlayerController.m
    ${TC_SRCDIR}/nm/ui/darwin/childview.m
    ${TC_SRCDIR}/nm/ui/darwin/sipargs.m

    #nm\\ui\\darwin
    ${TC_SRCDIR}/nm/ui/darwin/image_Image_c.m
    ${TC_SRCDIR}/nm/ui/darwin/media_Sound_c.m
  
    #nm\\notification\\darwin
    ${TC_SRCDIR}/nm/notification/darwin/notification_c.m

    #nm\\sys\\darwin
    ${TC_SRCDIR}/nm/sys/darwin/Vm_c.m

    #nm\\firebase\\darwin
    ${TC_SRCDIR}/nm/firebase/darwin/firebase_c.m

    #nm\\util\\darwin
    ${TC_SRCDIR}/nm/util/darwin/Locale.m

    #nm\\phone\\posix
    ${TC_SRCDIR}/nm/phone/posix/Dial_c.m
    
    
  )
endif()

if(TC_WINDOWING_SDL)
  set(TC_SDL_SOURCES
    ${TC_SRCDIR}/init/tcsdl.cpp
  )
endif(TC_WINDOWING_SDL)

if(TC_TARGET_ANDROID)
  set(TC_ANDROID_SOURCES
    ${TC_SRCDIR}/nm/money/money.c
  )
endif(TC_TARGET_ANDROID)

if(TC_TARGET_WINCE)
  set(TC_WCE_SOURCES
    ${TC_SRCDIR}/util/win/aygshellLib.c
  )
else()
  set(SOURCES
    ${SOURCES}
    ${TC_SRCDIR}/nm/net/ssl/SSLContextMbedtls.c
  )
endif()

include_directories(${TC_SRCDIR})
include_directories(${TC_SRCDIR}/nm/ui/darwin)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/third_party/axtls)
include_directories(${TC_SRCDIR}/dx)
include_directories(${TC_SRCDIR}/event)
include_directories(${TC_SRCDIR}/event/android)
include_directories(${TC_SRCDIR}/init)
include_directories(${TC_SRCDIR}/init/android)
include_directories(${TC_SRCDIR}/nm)
include_directories(${TC_SRCDIR}/nm/crypto)
include_directories(${TC_SRCDIR}/nm/db)
include_directories(${TC_SRCDIR}/nm/firebase)
include_directories(${TC_SRCDIR}/nm/io)
include_directories(${TC_SRCDIR}/nm/lang)
include_directories(${TC_SRCDIR}/nm/map)
include_directories(${TC_SRCDIR}/nm/money)
include_directories(${TC_SRCDIR}/nm/net)
include_directories(${TC_SRCDIR}/nm/phone)
include_directories(${TC_SRCDIR}/nm/pim)
include_directories(${TC_SRCDIR}/nm/sys)
include_directories(${TC_SRCDIR}/nm/telephony)
include_directories(${TC_SRCDIR}/nm/ui)
include_directories(${TC_SRCDIR}/nm/ui/android)
include_directories(${TC_SRCDIR}/nm/xml)
include_directories(${TC_SRCDIR}/palmdb)
include_directories(${TC_SRCDIR}/ras)
include_directories(${TC_SRCDIR}/scanner)
include_directories(${TC_SRCDIR}/scanner/android)
include_directories(${TC_SRCDIR}/sync)
include_directories(${TC_SRCDIR}/tcvm)
include_directories(${TC_SRCDIR}/util)
include_directories(${TC_SRCDIR}/util/android)

if(MSVC AND TC_TARGET_WINCE)
  include_directories(${TC_SRCDIR}/core/wince) # Only include this folder for WinCE!
endif()

if(NOT MSVC)
  include_directories(${TC_SRCDIR}/nm/lang/posix)
endif()

# add_definitions
if(TC_TARGET_ANDROID)
  add_definitions(-DANDROID)
  if ("${ANDROID_ABI}" MATCHES "arm64-v8a" OR TC_RENDERER_SKIA)
	  add_definitions(-DFORCE_LIBC_ALLOC)
  endif()
elseif(TC_TARGET_WINCE)
  add_definitions(-DUNICODE -D_WIN32_WCE=$(CEVER) -D_MSC_VER=1200 -D_REENTRANT -DLITTLE_ENDIAN)
elseif(MSVC)
  add_definitions(-DFORCE_LIBC_ALLOC -DWIN32 -D_REENTRANT -DHAVE_MREMAP=0 -DLITTLE_ENDIAN)
else()
  if(TC_GENERATOR_XCODE)
    add_definitions(-Ddarwin -DSK_GL)
    set(CMAKE_OSX_SYSROOT iphoneos)
    set(CMAKE_OSX_ARCHITECTURES "arm64")
  else()
    add_definitions(-DHEADLESS)
  endif()
  add_definitions(-Dlinux -DFORCE_LIBC_ALLOC -D_REENTRANT -DPOSIX -DHAVE_MREMAP=0 -fPIC -Wunused-function -Wno-import -fno-strict-aliasing)
endif(TC_TARGET_ANDROID)
add_definitions(-DTOTALCROSS -DTC_EXPORTS)

link_directories(${PROJECT_SOURCE_DIR})

# add the library
set(LIBRARY_ARGS ${SOURCES} ${TC_WCE_SOURCES} ${TC_MSVC_SOURCES} ${TC_NOT_MSVC_SOURCES} ${TC_SKIA_SOURCES} ${TC_SDL_SOURCES} ${TC_ANDROID_SOURCES} ${TCVM_iOS_FILES})
target_sources(tcvm PUBLIC ${LIBRARY_ARGS})

if(TC_BUILD_NATIVE_STARTUP_TEST)
  add_executable(window_startup_native_test
    ${TC_SRCDIR}/tests/window_startup_native_test.c
    ${TC_SRCDIR}/nm/ui/WindowStartup.c
  )
  target_include_directories(window_startup_native_test PRIVATE
    "${TC_PLATFORM_GENERATED_INCLUDE_DIR}"
    "${TC_SRCDIR}"
    "${TC_SRCDIR}/tcvm"
  )
  target_compile_definitions(window_startup_native_test PRIVATE
    TC_PLATFORM_CONFIGURED=1
  )
  target_link_libraries(window_startup_native_test PRIVATE
    SDL2::SDL2
    Skia::Skia
  )
  target_compile_options(window_startup_native_test PRIVATE
    $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>,$<C_COMPILER_ID:AppleClang>>:-Wall;-Wextra>
  )

  add_executable(window_startup_abi_c_test
    ${TC_SRCDIR}/tests/window_startup_abi_c_test.c
  )
  target_include_directories(window_startup_abi_c_test PRIVATE
    "${TC_PLATFORM_GENERATED_INCLUDE_DIR}"
    "${TC_SRCDIR}"
  )
  target_compile_definitions(window_startup_abi_c_test PRIVATE
    TC_PLATFORM_CONFIGURED=1
  )
  target_compile_options(window_startup_abi_c_test PRIVATE
    $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>,$<C_COMPILER_ID:AppleClang>>:-Wall;-Wextra;-Werror>
  )
  set_target_properties(window_startup_abi_c_test PROPERTIES
    C_STANDARD 11
    C_STANDARD_REQUIRED YES
    C_EXTENSIONS NO
  )

  add_executable(window_startup_abi_cpp_test
    ${TC_SRCDIR}/tests/window_startup_abi_cpp_test.cpp
  )
  target_include_directories(window_startup_abi_cpp_test PRIVATE
    "${TC_PLATFORM_GENERATED_INCLUDE_DIR}"
    "${TC_SRCDIR}"
  )
  target_compile_definitions(window_startup_abi_cpp_test PRIVATE
    TC_PLATFORM_CONFIGURED=1
  )
  target_compile_options(window_startup_abi_cpp_test PRIVATE
    $<$<OR:$<CXX_COMPILER_ID:GNU>,$<CXX_COMPILER_ID:Clang>,$<CXX_COMPILER_ID:AppleClang>>:-Wall;-Wextra;-Werror>
  )
  set_target_properties(window_startup_abi_cpp_test PROPERTIES
    CXX_STANDARD 11
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
  )

  add_executable(graphics_primitives_abi_c_test
    ${TC_SRCDIR}/tests/graphics_primitives_abi_c_test.c
  )
  target_include_directories(graphics_primitives_abi_c_test PRIVATE
    "${TC_PLATFORM_GENERATED_INCLUDE_DIR}"
    "${TC_SRCDIR}"
  )
  target_compile_definitions(graphics_primitives_abi_c_test PRIVATE
    TC_PLATFORM_CONFIGURED=1
  )
  target_link_libraries(graphics_primitives_abi_c_test PRIVATE
    SDL2::SDL2
    Skia::Skia
  )
  target_compile_options(graphics_primitives_abi_c_test PRIVATE
    $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>,$<C_COMPILER_ID:AppleClang>>:-Wall;-Wextra;-Werror>
  )
  set_target_properties(graphics_primitives_abi_c_test PROPERTIES
    C_STANDARD 11
    C_STANDARD_REQUIRED YES
    C_EXTENSIONS NO
  )

  add_executable(graphics_primitives_abi_cpp_test
    ${TC_SRCDIR}/tests/graphics_primitives_abi_cpp_test.cpp
  )
  target_include_directories(graphics_primitives_abi_cpp_test PRIVATE
    "${TC_PLATFORM_GENERATED_INCLUDE_DIR}"
    "${TC_SRCDIR}"
  )
  target_compile_definitions(graphics_primitives_abi_cpp_test PRIVATE
    TC_PLATFORM_CONFIGURED=1
  )
  target_link_libraries(graphics_primitives_abi_cpp_test PRIVATE
    SDL2::SDL2
    Skia::Skia
  )
  target_compile_options(graphics_primitives_abi_cpp_test PRIVATE
    $<$<OR:$<CXX_COMPILER_ID:GNU>,$<CXX_COMPILER_ID:Clang>,$<CXX_COMPILER_ID:AppleClang>>:-Wall;-Wextra;-Werror>
  )
  set_target_properties(graphics_primitives_abi_cpp_test PROPERTIES
    CXX_STANDARD 11
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
  )

  add_executable(graphics_primitives_abi_bridge_test
    ${TC_SRCDIR}/tests/graphics_primitives_abi_bridge.c
    ${TC_SRCDIR}/tests/graphics_primitives_abi_bridge_test.cpp
  )
  target_include_directories(graphics_primitives_abi_bridge_test PRIVATE
    "${TC_PLATFORM_GENERATED_INCLUDE_DIR}"
    "${TC_SRCDIR}"
  )
  target_compile_definitions(graphics_primitives_abi_bridge_test PRIVATE
    TC_PLATFORM_CONFIGURED=1
  )
  target_link_libraries(graphics_primitives_abi_bridge_test PRIVATE
    SDL2::SDL2
    Skia::Skia
  )
  target_compile_options(graphics_primitives_abi_bridge_test PRIVATE
    $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>,$<C_COMPILER_ID:AppleClang>>:-Wall;-Wextra;-Werror>
    $<$<OR:$<CXX_COMPILER_ID:GNU>,$<CXX_COMPILER_ID:Clang>,$<CXX_COMPILER_ID:AppleClang>>:-Wall;-Wextra;-Werror>
  )
  set_target_properties(graphics_primitives_abi_bridge_test PROPERTIES
    C_STANDARD 11
    C_STANDARD_REQUIRED YES
    C_EXTENSIONS NO
    CXX_STANDARD 11
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
  )

  if(TC_TARGET_MACOS AND TC_WINDOWING_SDL)
    add_executable(window_startup_integration_test
      ${TC_SRCDIR}/tests/window_startup_integration_test.c
    )
    target_include_directories(window_startup_integration_test PRIVATE
      "${TC_PLATFORM_GENERATED_INCLUDE_DIR}"
      "${TC_SRCDIR}"
      "${TC_SRCDIR}/tcvm"
    )
    target_compile_definitions(window_startup_integration_test PRIVATE
      TC_PLATFORM_CONFIGURED=1
    )
    add_dependencies(window_startup_integration_test tcvm)
    target_link_libraries(window_startup_integration_test PRIVATE
      "$<TARGET_FILE:tcvm>"
      SDL2::SDL2
      Skia::Skia
    )
    set_target_properties(window_startup_integration_test PROPERTIES
      BUILD_RPATH "${CMAKE_CURRENT_BINARY_DIR}"
    )
    target_compile_options(window_startup_integration_test PRIVATE
      $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>,$<C_COMPILER_ID:AppleClang>>:-Wall;-Wextra>
      $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>,$<C_COMPILER_ID:AppleClang>>:-ffunction-sections;-fdata-sections>
    )
    if(CMAKE_C_COMPILER_ID STREQUAL "AppleClang")
      set_property(TARGET window_startup_integration_test APPEND_STRING
        PROPERTY LINK_FLAGS " -Wl,-dead_strip"
      )
    endif()

    if(TC_RENDERER_SKIA)
      add_executable(skia_surface_test
        ${TC_SRCDIR}/nm/ui/skia/skia_surface_test.cpp
      )
      target_include_directories(skia_surface_test PRIVATE
        "${TC_PLATFORM_GENERATED_INCLUDE_DIR}"
        "${TC_SRCDIR}"
      )
      target_compile_definitions(skia_surface_test PRIVATE
        TC_PLATFORM_CONFIGURED=1
      )
      add_dependencies(skia_surface_test tcvm)
      target_link_libraries(skia_surface_test PRIVATE
        "$<TARGET_FILE:tcvm>"
        SDL2::SDL2
        Skia::Skia
      )
      set_target_properties(skia_surface_test PROPERTIES
        BUILD_RPATH "${CMAKE_CURRENT_BINARY_DIR}"
        CXX_STANDARD 11
        CXX_STANDARD_REQUIRED YES
        CXX_EXTENSIONS NO
      )
      target_compile_options(skia_surface_test PRIVATE
        $<$<OR:$<CXX_COMPILER_ID:GNU>,$<CXX_COMPILER_ID:Clang>,$<CXX_COMPILER_ID:AppleClang>>:-Wall;-Wextra>
      )
    endif()
  endif()
endif()

if(TC_ENABLE_COMPILED_DISPATCH)
  add_library(tcir_runtime STATIC
    ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/ir/tcir_runtime.c
  )
  target_include_directories(tcir_runtime PUBLIC
    ${CMAKE_CURRENT_SOURCE_DIR}/src/tcvm/ir
  )
  target_link_libraries(tcir_runtime PUBLIC tcir)
  if(TC_ENABLE_SLJIT_JIT)
    target_compile_definitions(tcir_runtime PRIVATE TCIR_RUNTIME_HAS_SLJIT=1)
    target_link_libraries(tcir_runtime PUBLIC tcir_jit)
  endif()
  if(TC_ENABLE_C_AOT)
    target_compile_definitions(tcir_runtime PRIVATE TCIR_RUNTIME_HAS_AOT=1)
  endif()
  target_compile_options(tcir_runtime PRIVATE
    $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>,$<C_COMPILER_ID:AppleClang>>:-Wall;-Wextra;-Werror>
  )
  set_target_properties(tcir_runtime PROPERTIES
    C_STANDARD 11
    C_STANDARD_REQUIRED YES
    C_EXTENSIONS NO
  )
  target_compile_definitions(tcvm PRIVATE TC_ENABLE_COMPILED_DISPATCH=1)
  target_link_libraries(tcvm tcir_runtime)
endif()

# Set C/CXX flags
target_compile_options(tcvm
  PRIVATE
    $<$<CXX_COMPILER_ID:Gnu>:
      # g++ warning flags
      -Wall
    >
    $<$<CXX_COMPILER_ID:Clang>:
      # clang warning flags
    >
    $<$<CXX_COMPILER_ID:MSVC>:
      # MSVC warning flags
      /W3
    >
    $<$<PLATFORM_ID:APPLE>:
      -mmacosx-version-min=10.11
    >
    $<$<STREQUAL:"${CMAKE_GENERATOR}","Xcode">:
      -Wno-everything
    >
)
# We need to use C++ 11
if(${CMAKE_VERSION} VERSION_LESS "3.8" OR MSVC) 
  set_target_properties(tcvm PROPERTIES
    CXX_STANDARD 11
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
  )
else()
  target_compile_features(tcvm PUBLIC cxx_std_17)
endif()
# XCode Properties
if(TC_GENERATOR_XCODE)
  # Fold prebuilt native dependencies into libtcvm.a at archive creation time.
  get_target_property(TC_QRCODE_LIBRARY_PATH ${TC_QRCODE_LIBRARY} IMPORTED_LOCATION)
  if(NOT TC_QRCODE_LIBRARY_PATH)
    message(FATAL_ERROR "Unable to resolve the selected QR Code static library")
  endif()
  set(TCVM_XCODE_ARCHIVE_DEPENDENCIES
    "${PNG_LIBRARY}"
    "${ZLIB_LIBRARY}"
    "${MINIZIP_LIBRARY}"
    "${JPEG_LIBRARIES}"
    "${SQLITE3_LIBRARIES}"
    "${MBEDTLS_LIBRARIES}"
    "${AXTLS_LIBRARY}"
    "${TC_QRCODE_LIBRARY_PATH}"
  )
  if(TC_RENDERER_SKIA)
    set(TCVM_XCODE_ARCHIVE_DEPENDENCIES
      "${SKIA_LIBRARIES}"
      ${TCVM_XCODE_ARCHIVE_DEPENDENCIES}
    )
  endif()
  list(REMOVE_DUPLICATES TCVM_XCODE_ARCHIVE_DEPENDENCIES)

  set(TCVM_XCODE_OTHER_LIBTOOLFLAGS "")
  foreach(TCVM_XCODE_ARCHIVE_DEPENDENCY ${TCVM_XCODE_ARCHIVE_DEPENDENCIES})
    if(TCVM_XCODE_ARCHIVE_DEPENDENCY)
      if(NOT EXISTS "${TCVM_XCODE_ARCHIVE_DEPENDENCY}")
        message(FATAL_ERROR
          "Unable to locate the iOS static dependency '${TCVM_XCODE_ARCHIVE_DEPENDENCY}' "
          "that must be folded into libtcvm.a"
        )
      endif()
      string(APPEND TCVM_XCODE_OTHER_LIBTOOLFLAGS " \"${TCVM_XCODE_ARCHIVE_DEPENDENCY}\"")
    endif()
  endforeach()
  
  # adds $(inherited) to HEADER_SEARCH_PATHS in order to get pod libraries headers
  get_target_property(INCLUDED_HEADERS tcvm INCLUDE_DIRECTORIES)
  set(INCLUDED_HEADERS "$(inherited)" ${INCLUDED_HEADERS})
  if(TC_RENDERER_SKIA)
    get_target_property(TCVM_SKIA_INCLUDED_HEADERS Skia::Skia INTERFACE_INCLUDE_DIRECTORIES)
    list(APPEND INCLUDED_HEADERS ${TCVM_SKIA_INCLUDED_HEADERS})
  endif()
  string(REPLACE ";" " " INCLUDED_HEADERS_STR "${INCLUDED_HEADERS}")
  
  set_target_properties(
    tcvm
    PROPERTIES
    XCODE_ATTRIBUTE_CODE_SIGN_IDENTITY ""
    XCODE_ATTRIBUTE_PRODUCT_BUNDLE_IDENTIFIER com.totalcross.vm
    XCODE_ATTRIBUTE_IPHONEOS_DEPLOYMENT_TARGET 12.1
    XCODE_ATTRIBUTE_CLANG_ENABLE_MODULES YES
    XCODE_ATTRIBUTE_GCC_PREFIX_HEADER "${IOS_BUNDLE_SRCDIR}/tcvm/tcvm-Prefix.pch"
    XCODE_ATTRIBUTE_GCC_PRECOMPILE_PREFIX_HEADER "YES"
    XCODE_ATTRIBUTE_HEADER_SEARCH_PATHS "${INCLUDED_HEADERS_STR}"
    XCODE_ATTRIBUTE_CLANG_CXX_LANGUAGE_STANDARD "c++0x"
    XCODE_ATTRIBUTE_GCC_C_LANGUAGE_STANDARD c99
    STATIC_LIBRARY_FLAGS "${TCVM_XCODE_OTHER_LIBTOOLFLAGS}"
    XCODE_ATTRIBUTE_OTHER_LIBTOOLFLAGS "${TCVM_XCODE_OTHER_LIBTOOLFLAGS}"
    XCODE_LINK_BUILD_PHASE_MODE KNOWN_LOCATION
  )
endif()

if(TC_TARGET_ANDROID)
  # Searches for a specified prebuilt library and stores the path as a
  # variable. Because CMake includes system libraries in the search path by
  # default, you only need to specify the name of the public NDK library
  # you want to add. CMake verifies that the library exists before
  # completing its build.
  find_library( # Sets the name of the path variable.
    log-lib

    # Specifies the name of the NDK library that
    # you want CMake to locate.
    log 
  )
  find_library(# Sets the name of the path variable.
            jnigraphics-lib

            # Specifies the name of the NDK library that
            # you want CMake to locate.
            jnigraphics 
  )  
  # Specifies libraries CMake should link to your target library. You
  # can link multiple libraries, such as libraries you define in this
  # build script, prebuilt third-party libraries, or system libraries.
  set(TCVM_ANDROID_LIBRARIES
      GLESv2
      EGL
      android
      ${log-lib}
      ${jnigraphics-lib})
  target_link_libraries(# Specifies the target library.
                        tcvm PRIVATE
                        ${TCVM_ANDROID_LIBRARIES})
elseif(MSVC)
  if(TC_TARGET_WINCE)
    target_link_libraries(tcvm PRIVATE secchk.lib coredll.lib corelibc.lib ole32.lib oleaut32.lib uuid.lib commctrl.lib)
  else()
    target_link_libraries(tcvm PRIVATE winmm.lib ws2_32.lib imm32.lib Rasapi32.lib)
  endif()
else()
  if(NOT TC_GENERATOR_XCODE)
    target_link_libraries(tcvm PRIVATE pthread stdc++)
  endif()
  if(NOT TC_TARGET_APPLE AND UNIX)
    target_link_libraries(tcvm PRIVATE fontconfig)
    if (${CMAKE_SYSTEM_PROCESSOR} MATCHES "arm")
      target_link_libraries(tcvm PRIVATE -Wl,--wrap=log2f,--wrap=powf,--wrap=expf,--wrap=exp2f) # Avoid glibc dependencies
    endif(${CMAKE_SYSTEM_PROCESSOR} MATCHES "arm")
  endif(NOT TC_TARGET_APPLE AND UNIX)
endif(TC_TARGET_ANDROID)

if(TC_BUILD_IR_TESTS)
  if(TC_ENABLE_C_AOT)
    if(NOT TARGET tcaot)
      message(FATAL_ERROR "TCIR AOT host tests require a native tcaot executable")
    endif()
    set(TCIR_AOT_GENERATED_DIR "${CMAKE_CURRENT_BINARY_DIR}/aot/poc")
    set(TCIR_AOT_GENERATED_SOURCE "${TCIR_AOT_GENERATED_DIR}/tcir_aot_generated.c")
    set(TCIR_AOT_GENERATED_HEADER "${TCIR_AOT_GENERATED_DIR}/tcir_aot_generated.h")
    set(TCIR_AOT_GENERATED_MANIFEST "${TCIR_AOT_GENERATED_DIR}/manifest.json")
    add_custom_command(
      OUTPUT
        ${TCIR_AOT_GENERATED_SOURCE}
        ${TCIR_AOT_GENERATED_HEADER}
        ${TCIR_AOT_GENERATED_MANIFEST}
      COMMAND ${CMAKE_COMMAND} -E make_directory ${TCIR_AOT_GENERATED_DIR}
      COMMAND $<TARGET_FILE:tcaot>
        --input poc-fixtures
        --output ${TCIR_AOT_GENERATED_DIR}
        --manifest ${TCIR_AOT_GENERATED_MANIFEST}
        --target-options "host-c11-poc"
      DEPENDS
        tcaot
        ${CMAKE_CURRENT_SOURCE_DIR}/src/tests/ir/fixtures/tcir_converter_fixtures.h
      VERBATIM
    )
    add_library(tcir_aot_generated STATIC
      ${TCIR_AOT_GENERATED_SOURCE}
      ${TCIR_AOT_GENERATED_HEADER}
    )
    target_include_directories(tcir_aot_generated PUBLIC ${TCIR_AOT_GENERATED_DIR})
    target_link_libraries(tcir_aot_generated PUBLIC tcir_aot)
    target_compile_options(tcir_aot_generated PRIVATE
      $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>>:-Wall;-Wextra;-Werror;-pedantic>
    )
    set_target_properties(tcir_aot_generated PROPERTIES
      C_STANDARD 11
      C_STANDARD_REQUIRED YES
      C_EXTENSIONS NO
    )

    add_executable(tcir_aot_tests
      ${CMAKE_CURRENT_SOURCE_DIR}/src/tests/ir/tcir_aot_tests.c
    )
    target_include_directories(tcir_aot_tests PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/src/tests/ir)
    target_link_libraries(tcir_aot_tests PRIVATE tcir_aot)
    target_compile_options(tcir_aot_tests PRIVATE
      $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>>:-Wall;-Wextra;-Werror>
    )
    set_target_properties(tcir_aot_tests PROPERTIES
      C_STANDARD 11
      C_STANDARD_REQUIRED YES
      C_EXTENSIONS NO
    )
    add_test(NAME tcir-aot COMMAND tcir_aot_tests)
    add_test(
      NAME tcir-aot-determinism
      COMMAND ${CMAKE_COMMAND}
        -DTCIR_AOT_TOOL=$<TARGET_FILE:tcaot>
        -DTCIR_AOT_TEST_DIR=${CMAKE_CURRENT_BINARY_DIR}/aot/determinism
        -P ${CMAKE_CURRENT_SOURCE_DIR}/modules/ValidateTCIRAotDeterminism.cmake
    )
    add_test(
      NAME tcir-aot-manifest
      COMMAND ${TCIR_PYTHON_EXECUTABLE}
        ${CMAKE_CURRENT_SOURCE_DIR}/../scripts/validate-tcir-aot-manifest.py
        --manifest ${TCIR_AOT_GENERATED_MANIFEST}
        --source ${TCIR_AOT_GENERATED_SOURCE}
        --header ${TCIR_AOT_GENERATED_HEADER}
    )
    add_dependencies(check-tcir tcir_aot_tests tcir_aot_generated)
  endif()

  add_executable(tcir_differential_tests
    ${CMAKE_CURRENT_SOURCE_DIR}/src/tests/ir/tcir_differential_tests.c
  )
  # tcvm exposes its source list as PUBLIC for legacy consumers. Link the built
  # runtime artifact directly so this focused harness does not recompile every
  # VM source as part of the executable.
  target_link_libraries(tcir_differential_tests PRIVATE tcir $<TARGET_LINKER_FILE:tcvm>)
  add_dependencies(tcir_differential_tests tcvm)
  if(TC_ENABLE_SLJIT_JIT)
    target_compile_definitions(tcir_differential_tests PRIVATE TCIR_HAS_SLJIT=1)
    target_link_libraries(tcir_differential_tests PRIVATE tcir_jit)
  endif()
  if(TC_ENABLE_C_AOT)
    target_compile_definitions(tcir_differential_tests PRIVATE TCIR_HAS_AOT=1)
    target_include_directories(tcir_differential_tests PRIVATE ${TCIR_AOT_GENERATED_DIR})
    target_link_libraries(tcir_differential_tests PRIVATE tcir_aot_generated)
  endif()
  target_compile_options(tcir_differential_tests PRIVATE
    $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>>:-Wall;-Wextra;-Werror>
  )
  set_target_properties(tcir_differential_tests PROPERTIES
    BUILD_RPATH "${CMAKE_CURRENT_BINARY_DIR}"
    C_STANDARD 99
    C_STANDARD_REQUIRED YES
    C_EXTENSIONS NO
  )
  add_test(NAME tcir-differential COMMAND tcir_differential_tests)
  add_dependencies(check-tcir tcir_differential_tests)

  if(TC_ENABLE_COMPILED_DISPATCH)
    find_package(Threads REQUIRED)
    add_executable(tcir_runtime_tests
      ${CMAKE_CURRENT_SOURCE_DIR}/src/tests/ir/tcir_runtime_tests.c
    )
    target_link_libraries(tcir_runtime_tests PRIVATE
      tcir
      Threads::Threads
      $<TARGET_LINKER_FILE:tcvm>
    )
    add_dependencies(tcir_runtime_tests tcvm)
    target_compile_definitions(tcir_runtime_tests PRIVATE
      TCIR_RUNTIME_DUMP_PATH="${CMAKE_CURRENT_BINARY_DIR}/tcir-runtime-test.tcir"
    )
    if(TC_ENABLE_SLJIT_JIT)
      target_compile_definitions(tcir_runtime_tests PRIVATE TCIR_RUNTIME_TEST_HAS_SLJIT=1)
    endif()
    if(TC_ENABLE_C_AOT)
      target_compile_definitions(tcir_runtime_tests PRIVATE TCIR_RUNTIME_TEST_HAS_AOT=1)
      target_include_directories(tcir_runtime_tests PRIVATE ${TCIR_AOT_GENERATED_DIR})
      target_link_libraries(tcir_runtime_tests PRIVATE tcir_aot_generated)
    endif()
    target_compile_options(tcir_runtime_tests PRIVATE
      $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>>:-Wall;-Wextra;-Werror>
    )
    set_target_properties(tcir_runtime_tests PROPERTIES
      BUILD_RPATH "${CMAKE_CURRENT_BINARY_DIR}"
      C_STANDARD 11
      C_STANDARD_REQUIRED YES
      C_EXTENSIONS NO
    )
    add_test(NAME tcir-runtime COMMAND tcir_runtime_tests)
    add_dependencies(check-tcir tcir_runtime_tests)
  endif()

  if(TC_ENABLE_C_AOT)
    add_custom_target(tcvm_aot_fixture
      DEPENDS tcir_aot_tests tcir_differential_tests tcir_aot_generated
    )
  endif()

  if(TC_ENABLE_SLJIT_JIT)
    add_executable(tcir_jit_tests
      ${CMAKE_CURRENT_SOURCE_DIR}/src/tests/ir/tcir_jit_tests.c
    )
    target_link_libraries(tcir_jit_tests PRIVATE tcir_jit)
    target_compile_options(tcir_jit_tests PRIVATE
      $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>>:-Wall;-Wextra;-Werror>
    )
    set_target_properties(tcir_jit_tests PROPERTIES
      C_STANDARD 11
      C_STANDARD_REQUIRED YES
      C_EXTENSIONS NO
    )
    add_test(NAME tcir-jit COMMAND tcir_jit_tests)
    add_dependencies(check-tcir tcir_jit_tests)
  endif()
endif()

if(TC_BUILD_IR_BENCHMARKS)
  find_program(TC_BENCHMARK_PYTHON_EXECUTABLE NAMES python3)
  if(NOT TC_BENCHMARK_PYTHON_EXECUTABLE)
    message(FATAL_ERROR "TC_BUILD_IR_BENCHMARKS requires a Python 3 interpreter")
  endif()
  set(TC_BENCHMARK_POWER_MODE
      "not controlled; record the host power state before comparing results"
      CACHE STRING "Power-mode note stored in TCIR benchmark artifacts")
  set(TC_BENCHMARK_BACKGROUND_LOAD
      "not isolated; no intentional background workload"
      CACHE STRING "Background-load note stored in TCIR benchmark artifacts")
  set(TC_BENCHMARK_DIRTY_PATHS
      "not recorded"
      CACHE STRING "Dirty paths stored in TCIR benchmark artifacts")

  execute_process(
    COMMAND git rev-parse HEAD
    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}/..
    RESULT_VARIABLE TC_BENCHMARK_GIT_RESULT
    OUTPUT_VARIABLE TC_BENCHMARK_GIT_REVISION
    OUTPUT_STRIP_TRAILING_WHITESPACE
    ERROR_QUIET
  )
  if(NOT TC_BENCHMARK_GIT_RESULT EQUAL 0)
    set(TC_BENCHMARK_GIT_REVISION "unknown")
  endif()
  set(TC_BENCHMARK_EFFECTIVE_C_FLAGS "${CMAKE_C_FLAGS}")
  if(CMAKE_BUILD_TYPE)
    string(TOUPPER "${CMAKE_BUILD_TYPE}" TC_BENCHMARK_BUILD_TYPE_UPPER)
    set(TC_BENCHMARK_CONFIG_FLAGS_VARIABLE "CMAKE_C_FLAGS_${TC_BENCHMARK_BUILD_TYPE_UPPER}")
    set(TC_BENCHMARK_EFFECTIVE_C_FLAGS
        "${TC_BENCHMARK_EFFECTIVE_C_FLAGS} ${${TC_BENCHMARK_CONFIG_FLAGS_VARIABLE}}")
  endif()
  set(TC_BENCHMARK_EFFECTIVE_C_FLAGS
      "${TC_BENCHMARK_EFFECTIVE_C_FLAGS} -Wall -Wextra -Werror")
  string(STRIP "${TC_BENCHMARK_EFFECTIVE_C_FLAGS}" TC_BENCHMARK_EFFECTIVE_C_FLAGS)
  string(REPLACE "\\" "\\\\" TC_BENCHMARK_EFFECTIVE_C_FLAGS "${TC_BENCHMARK_EFFECTIVE_C_FLAGS}")
  string(REPLACE "\"" "\\\"" TC_BENCHMARK_EFFECTIVE_C_FLAGS "${TC_BENCHMARK_EFFECTIVE_C_FLAGS}")

  function(tc_configure_ir_benchmark_target TARGET_NAME SAMPLE_COUNT WARMUP_COUNT)
    add_executable(${TARGET_NAME}
      ${CMAKE_CURRENT_SOURCE_DIR}/src/tests/ir/tcir_jit_benchmark.c
    )
    target_link_libraries(${TARGET_NAME} PRIVATE
      tcir_jit
      $<TARGET_LINKER_FILE:tcvm>
    )
    if(NOT MSVC)
      target_link_libraries(${TARGET_NAME} PRIVATE m)
    endif()
    add_dependencies(${TARGET_NAME} tcvm)
    target_compile_definitions(${TARGET_NAME} PRIVATE
      TC_BENCHMARK_GIT_REVISION="${TC_BENCHMARK_GIT_REVISION}"
      TC_BENCHMARK_COMPILER_ID="${CMAKE_C_COMPILER_ID}"
      TC_BENCHMARK_COMPILER_VERSION="${CMAKE_C_COMPILER_VERSION}"
      TC_BENCHMARK_BUILD_TYPE="${CMAKE_BUILD_TYPE}"
      TC_BENCHMARK_GENERATOR="${CMAKE_GENERATOR}"
      TC_BENCHMARK_TARGET_PROCESSOR="${CMAKE_SYSTEM_PROCESSOR}"
      TC_BENCHMARK_C_FLAGS="${TC_BENCHMARK_EFFECTIVE_C_FLAGS}"
      TC_BENCHMARK_CONFIG_SAMPLE_COUNT=${SAMPLE_COUNT}
      TC_BENCHMARK_CONFIG_WARMUP_COUNT=${WARMUP_COUNT}
    )
    target_compile_options(${TARGET_NAME} PRIVATE
      $<$<OR:$<C_COMPILER_ID:GNU>,$<C_COMPILER_ID:Clang>>:-Wall;-Wextra;-Werror>
    )
    set_target_properties(${TARGET_NAME} PROPERTIES
      BUILD_RPATH "${CMAKE_CURRENT_BINARY_DIR}"
      C_STANDARD 11
      C_STANDARD_REQUIRED YES
      C_EXTENSIONS NO
    )
  endfunction()

  tc_configure_ir_benchmark_target(tcir_jit_benchmark 60 5)
  tc_configure_ir_benchmark_target(tcir_jit_benchmark_200 200 10)
  tc_configure_ir_benchmark_target(tcir_jit_benchmark_1000 1000 20)

  set(TC_BENCHMARK_RESULTS_DIR "${CMAKE_CURRENT_BINARY_DIR}/results")
  string(REPLACE ";" "," TC_BENCHMARK_POWER_MODE_ARGUMENT "${TC_BENCHMARK_POWER_MODE}")
  string(REPLACE ";" "," TC_BENCHMARK_BACKGROUND_LOAD_ARGUMENT "${TC_BENCHMARK_BACKGROUND_LOAD}")
  string(REPLACE ";" "," TC_BENCHMARK_DIRTY_PATHS_ARGUMENT "${TC_BENCHMARK_DIRTY_PATHS}")
  string(SUBSTRING "${TC_BENCHMARK_GIT_REVISION}" 0 12 TC_BENCHMARK_REVISION_ID)
  set(TC_BENCHMARK_RESULT_PREFIX "tcir-jit-benchmark-${TC_BENCHMARK_REVISION_ID}")
  add_custom_target(run-tcir-jit-benchmark
    COMMAND ${CMAKE_COMMAND} -E make_directory ${TC_BENCHMARK_RESULTS_DIR}
    COMMAND $<TARGET_FILE:tcir_jit_benchmark>
      --json ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s60.json
      --csv ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s60.csv
      --power-mode "${TC_BENCHMARK_POWER_MODE_ARGUMENT}"
      --background-load "${TC_BENCHMARK_BACKGROUND_LOAD_ARGUMENT}"
      --dirty-paths "${TC_BENCHMARK_DIRTY_PATHS_ARGUMENT}"
    COMMAND ${TC_BENCHMARK_PYTHON_EXECUTABLE}
      ${CMAKE_CURRENT_SOURCE_DIR}/../scripts/validate-tcir-jit-benchmark.py
      --json ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s60.json
      --csv ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s60.csv
    COMMAND $<TARGET_FILE:tcir_jit_benchmark_200>
      --json ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s200.json
      --csv ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s200.csv
      --power-mode "${TC_BENCHMARK_POWER_MODE_ARGUMENT}"
      --background-load "${TC_BENCHMARK_BACKGROUND_LOAD_ARGUMENT}"
      --dirty-paths "${TC_BENCHMARK_DIRTY_PATHS_ARGUMENT}"
    COMMAND ${TC_BENCHMARK_PYTHON_EXECUTABLE}
      ${CMAKE_CURRENT_SOURCE_DIR}/../scripts/validate-tcir-jit-benchmark.py
      --json ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s200.json
      --csv ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s200.csv
    COMMAND $<TARGET_FILE:tcir_jit_benchmark_1000>
      --json ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s1000.json
      --csv ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s1000.csv
      --power-mode "${TC_BENCHMARK_POWER_MODE_ARGUMENT}"
      --background-load "${TC_BENCHMARK_BACKGROUND_LOAD_ARGUMENT}"
      --dirty-paths "${TC_BENCHMARK_DIRTY_PATHS_ARGUMENT}"
    COMMAND ${TC_BENCHMARK_PYTHON_EXECUTABLE}
      ${CMAKE_CURRENT_SOURCE_DIR}/../scripts/validate-tcir-jit-benchmark.py
      --json ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s1000.json
      --csv ${TC_BENCHMARK_RESULTS_DIR}/${TC_BENCHMARK_RESULT_PREFIX}-s1000.csv
    DEPENDS tcir_jit_benchmark tcir_jit_benchmark_200 tcir_jit_benchmark_1000
    USES_TERMINAL
  )
endif()

set(TC_SRCDIR "${CMAKE_CURRENT_SOURCE_DIR}/src")

include_directories(${TC_SRCDIR}/util)

if(MSVC)
  set(CMAKE_EXE_LINKER_FLAGS
    "${CMAKE_EXE_LINKER_FLAGS} /NODEFAULTLIB  /SUBSYSTEM:WINDOWS /LARGEADDRESSAWARE /DYNAMICBASE:NO"
  )
  set(LAUNCHER_PATH
    "win"
  )
  set(LAUNCHER_RESOURCES
    ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/tc.rc
  )
else()
  set(LAUNCHER_PATH
    "linux"
  ) 
endif(MSVC)

# add the executable
if(NOT TC_GENERATOR_XCODE)
add_executable(Launcher 
  ${TC_SRCDIR}/launchers/${LAUNCHER_PATH}/Launcher.c
  ${LAUNCHER_RESOURCES}
)

if(TC_TARGET_WINCE)
  target_compile_options(Launcher PRIVATE /GS-)
  target_link_options(Launcher PRIVATE /STACK:0x200000 /ENTRY:)
endif()

if(MSVC)
  target_link_libraries(Launcher MSVCRT.lib)
  if(${CMAKE_GENERATOR} STREQUAL "Visual Studio 17 2022")
    target_link_libraries(Launcher vcruntime.lib libucrt.lib)
  endif()
else()
  target_link_libraries(Launcher dl)
endif(MSVC)
endif()

# Disabled for the time being
# if(MSVC)
#   if(TC_TARGET_WINCE)
#     INCLUDE_EXTERNAL_MSPROJECT(Datalogic ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/Datalogic.vcproj)
#     INCLUDE_EXTERNAL_MSPROJECT(Dolphin ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/Dolphin.vcproj)
#     INCLUDE_EXTERNAL_MSPROJECT(Intermec ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/Intermec.vcproj)
#     INCLUDE_EXTERNAL_MSPROJECT(Motorola ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/Motorola.vcproj)
#     INCLUDE_EXTERNAL_MSPROJECT(OpticonH16 ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/OpticonH16.vcproj)
#     add_dependencies(OpticonH16 tcvm)
#     add_dependencies(Dolphin tcvm)
#     add_dependencies(Intermec tcvm)
#     INCLUDE_EXTERNAL_MSPROJECT(Pidion ${CMAKE_CURRENT_SOURCE_DIR}/vc2008/Pidion.vcproj)
#   endif()
# endif()
