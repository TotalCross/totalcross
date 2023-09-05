# Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

cmake_minimum_required(VERSION 3.5)

include(FetchContent)

set(SQLite3_YEAR 2020)
set(SQLite3_HOMEPAGE_URL https://sqlite.org)
set(SQLite3_VERSION 3.32.03.00)
string(REPLACE "." "" SQLite3_FILE_VERSION "${SQLite3_VERSION}")

FetchContent_Declare(
    sqlite3
    URL             ${SQLite3_HOMEPAGE_URL}/${SQLite3_YEAR}/sqlite-amalgamation-${SQLite3_FILE_VERSION}.zip
    USES_TERMINAL_DOWNLOAD TRUE)

FetchContent_GetProperties(SQLite3)
if(NOT sqlite3_POPULATED)
    message("Setting up SQLite3...")

    FetchContent_Populate(SQLite3)

    # using INTERFACE to compile along the main project
    add_library(SQLite3 INTERFACE
        "${sqlite3_SOURCE_DIR}/sqlite3.c"
    )

    target_include_directories(SQLite3 INTERFACE 
        ${sqlite3_SOURCE_DIR}
    )

    target_sources(SQLite3 INTERFACE 
        "${sqlite3_SOURCE_DIR}/sqlite3.c"
    )

    target_compile_definitions(SQLite3 INTERFACE
        -DSQLITE_HAS_CODEC 
        -DSQLITE_ENABLE_COLUMN_METADATA=1
        -DSQLITE_ENABLE_FTS3=1
        -DSQLITE_ENABLE_LOAD_EXTENSION=1
        -DSQLITE_ENABLE_UPDATE_DELETE_LIMIT=1
        -DSQLITE_ENABLE_FTS3_PARENTHESIS=1
        -DSQLITE_ENABLE_RTREE=1
        -DSQLITE_ENABLE_STAT2=1
        -DSQLITE_ENABLE_UNLOCK_NOTIFY=1 
        -DSQLITE_THREADSAFE=0 
        -DSQLITE_THREAD_OVERRIDE_LOCK=0 
        -DSQLITE_WITHOUT_MSIZE=1
  
        -DSQLITE_DQS=0
        -DSQLITE_DEFAULT_MEMSTATUS=0
        -DSQLITE_DEFAULT_WAL_SYNCHRONOUS=1
        -DQLITE_LIKE_DOESNT_MATCH_BLOBS
        -DSQLITE_MAX_EXPR_DEPTH=0
        -DSQLITE_OMIT_DEPRECATED
        -DSQLITE_OMIT_PROGRESS_CALLBACK
    )
endif()
