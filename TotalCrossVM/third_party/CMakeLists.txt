# Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

# Download and unpack at configure time
configure_file(sqlite3/CMakeLists.txt.in sqlite3/CMakeLists.txt)
configure_file(sqlite3/sqlite3_tc.h sqlite3/build/sqlite3_tc.h)

execute_process(COMMAND "${CMAKE_COMMAND}" -G "${CMAKE_GENERATOR}" .
    WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/sqlite3"
)
execute_process(COMMAND "${CMAKE_COMMAND}" --build .
    WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/sqlite3"
)

SET(SQLITE3_INCLUDE_DIR "${CMAKE_CURRENT_BINARY_DIR}/sqlite3/build" PARENT_SCOPE)
SET(SQLITE3_SOURCES "${CMAKE_CURRENT_BINARY_DIR}/sqlite3/build/sqlite3.c" PARENT_SCOPE)
