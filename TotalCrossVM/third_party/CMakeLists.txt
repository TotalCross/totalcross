# Download and unpack at configure time
configure_file(zlib/CMakeLists.txt.in zlib/CMakeLists.txt)

execute_process(COMMAND "${CMAKE_COMMAND}" -G "${CMAKE_GENERATOR}" .
    WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/zlib"
)
execute_process(COMMAND "${CMAKE_COMMAND}" --build .
    WORKING_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/zlib"
)

IF (NOT(NOT APPLE AND UNIX 
   AND (CMAKE_SYSTEM_NAME STREQUAL "Linux") 
   AND ${CMAKE_SYSTEM_PROCESSOR} MATCHES "armv7l"))
  add_subdirectory("${CMAKE_CURRENT_BINARY_DIR}/zlib/src"
                   "${CMAKE_CURRENT_BINARY_DIR}/zlib/build"
  )
  set(ZLIB_SHOULD_LINK ON CACHE BOOL "" FORCE)
ELSE()
   set(ZLIB_SHOULD_LINK OFF CACHE BOOL "" FORCE)
ENDIF()


set(ZLIB_INCLUDE_DIR "${CMAKE_CURRENT_BINARY_DIR}/zlib/src" CACHE PATH "zlib include dir" FORCE)