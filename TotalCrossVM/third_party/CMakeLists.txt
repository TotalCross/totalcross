# Copyright (C) 2021 TotalCross Global Mobile Platform Ltda.
#
# SPDX-License-Identifier: LGPL-2.1-only

add_subdirectory(sqlite3) 
add_dependencies(tcvm SQLite3)

if(CMAKE_GENERATOR STREQUAL Xcode)
   get_target_property(SQLITE3_INCLUDES SQLite3 INTERFACE_INCLUDE_DIRECTORIES)
   target_include_directories(tcvm PRIVATE "${SQLITE3_INCLUDES}")
endif(CMAKE_GENERATOR STREQUAL Xcode)

add_subdirectory(turbojpeg)
add_dependencies(tcvm turbojpeg)


# Download and unpack at configure time
# configure_file(turbojpeg/CMakeLists.txt.in turbojpeg/CMakeLists.txt)

# execute_process(COMMAND "${CMAKE_COMMAND}" -G "${CMAKE_GENERATOR}" .
#     WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/turbojpeg"
# )
# execute_process(COMMAND "${CMAKE_COMMAND}" --build .
#     WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/turbojpeg"
# )

# add_library(turbojpeg STATIC IMPORTED GLOBAL)
# set_target_properties(turbojpeg 
#     PROPERTIES 
#         IMPORTED_LOCATION ${CMAKE_CURRENT_BINARY_DIR}/turbojpeg/install/lib/libturbojpeg.a)
# target_include_directories(turbojpeg INTERFACE 
#     "${CMAKE_CURRENT_BINARY_DIR}/turbojpeg/install/include"
# )



