# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

include(FindPackageHandleStandardArgs)

set(SQLITE3_DIR "${CMAKE_SOURCE_DIR}/sqlite3/local" CACHE PATH "SQLite3 prebuilt directory")

find_path(
  SQLITE3_INCLUDE_DIR
  sqlite3.h
  HINTS "${SQLITE3_DIR}/include"
  NO_CMAKE_FIND_ROOT_PATH
  NO_DEFAULT_PATH
)

find_library(
  SQLITE3_LIBRARY
  NAMES sqlite3 libsqlite3
  HINTS "${SQLITE3_DIR}/lib"
  NO_CMAKE_FIND_ROOT_PATH
  NO_DEFAULT_PATH
)

find_package_handle_standard_args(
  SQLite3
  REQUIRED_VARS SQLITE3_INCLUDE_DIR SQLITE3_LIBRARY
)

if(SQLite3_FOUND)
  set(SQLITE3_INCLUDE_DIRS "${SQLITE3_INCLUDE_DIR}")
  set(SQLITE3_LIBRARIES "${SQLITE3_LIBRARY}")
  get_filename_component(SQLITE3_LIBRARY_DIR "${SQLITE3_LIBRARY}" DIRECTORY)

  if(NOT TARGET SQLite3)
    add_library(SQLite3 STATIC IMPORTED GLOBAL)
    set_target_properties(SQLite3 PROPERTIES
      IMPORTED_LOCATION "${SQLITE3_LIBRARY}"
      INTERFACE_INCLUDE_DIRECTORIES "${SQLITE3_INCLUDE_DIR}"
    )
  endif()

  if(NOT TARGET SQLite::SQLite3)
    add_library(SQLite::SQLite3 ALIAS SQLite3)
  endif()
endif()
