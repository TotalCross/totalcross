# Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

add_subdirectory(sqlite3) 
add_dependencies(tcvm SQLite3)

if(CMAKE_GENERATOR STREQUAL Xcode)
   get_target_property(SQLITE3_INCLUDES SQLite3 INTERFACE_INCLUDE_DIRECTORIES)
   target_include_directories(tcvm PRIVATE "${SQLITE3_INCLUDES}")
endif(CMAKE_GENERATOR STREQUAL Xcode)
