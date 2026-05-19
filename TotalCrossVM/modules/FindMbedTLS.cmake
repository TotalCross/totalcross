# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

include(FindPackageHandleStandardArgs)

set(MBEDTLS_DIR "${CMAKE_SOURCE_DIR}/mbedtls/local" CACHE PATH "mbedTLS prebuilt directory")

find_path(
  MBEDTLS_INCLUDE_DIR
  mbedtls/ssl.h
  HINTS "${MBEDTLS_DIR}/include"
  NO_CMAKE_FIND_ROOT_PATH
  NO_DEFAULT_PATH
)

find_library(
  MBEDTLS_LIBRARY
  NAMES mbedtls libmbedtls
  HINTS "${MBEDTLS_DIR}/lib"
  NO_CMAKE_FIND_ROOT_PATH
  NO_DEFAULT_PATH
)

find_library(
  MBEDX509_LIBRARY
  NAMES mbedx509 libmbedx509
  HINTS "${MBEDTLS_DIR}/lib"
  NO_CMAKE_FIND_ROOT_PATH
  NO_DEFAULT_PATH
)

find_library(
  MBEDCRYPTO_LIBRARY
  NAMES mbedcrypto libmbedcrypto
  HINTS "${MBEDTLS_DIR}/lib"
  NO_CMAKE_FIND_ROOT_PATH
  NO_DEFAULT_PATH
)

find_package_handle_standard_args(
  MbedTLS
  REQUIRED_VARS
    MBEDTLS_INCLUDE_DIR
    MBEDTLS_LIBRARY
    MBEDX509_LIBRARY
    MBEDCRYPTO_LIBRARY
)

if(MbedTLS_FOUND)
  set(MBEDTLS_INCLUDE_DIRS "${MBEDTLS_INCLUDE_DIR}")
  set(MBEDTLS_LIBRARIES "${MBEDTLS_LIBRARY}" "${MBEDX509_LIBRARY}" "${MBEDCRYPTO_LIBRARY}")
  get_filename_component(MBEDTLS_LIBRARY_DIR "${MBEDTLS_LIBRARY}" DIRECTORY)

  if(NOT TARGET MbedTLS::mbedtls)
    add_library(MbedTLS::mbedtls STATIC IMPORTED GLOBAL)
    set_target_properties(MbedTLS::mbedtls PROPERTIES
      IMPORTED_LOCATION "${MBEDTLS_LIBRARY}"
      INTERFACE_INCLUDE_DIRECTORIES "${MBEDTLS_INCLUDE_DIR}"
      INTERFACE_LINK_LIBRARIES "MbedTLS::mbedx509;MbedTLS::mbedcrypto"
    )
  endif()

  if(NOT TARGET MbedTLS::mbedx509)
    add_library(MbedTLS::mbedx509 STATIC IMPORTED GLOBAL)
    set_target_properties(MbedTLS::mbedx509 PROPERTIES
      IMPORTED_LOCATION "${MBEDX509_LIBRARY}"
      INTERFACE_INCLUDE_DIRECTORIES "${MBEDTLS_INCLUDE_DIR}"
      INTERFACE_LINK_LIBRARIES "MbedTLS::mbedcrypto"
    )
  endif()

  if(NOT TARGET MbedTLS::mbedcrypto)
    add_library(MbedTLS::mbedcrypto STATIC IMPORTED GLOBAL)
    set_target_properties(MbedTLS::mbedcrypto PROPERTIES
      IMPORTED_LOCATION "${MBEDCRYPTO_LIBRARY}"
      INTERFACE_INCLUDE_DIRECTORIES "${MBEDTLS_INCLUDE_DIR}"
    )
  endif()
endif()
