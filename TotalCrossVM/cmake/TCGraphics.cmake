# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

include_guard(GLOBAL)

# The first option in each pair is the user-facing selector.  The inverse is
# derived below so a command-line/cache choice is not overwritten by the
# platform defaults and contradictory pairs cannot be selected accidentally.
if(TC_TARGET_ANDROID OR TC_TARGET_IOS)
  set(TC_GRAPHICS_SOFTWARE_DEFAULT OFF)
else()
  set(TC_GRAPHICS_SOFTWARE_DEFAULT ON)
endif()

if(TC_TARGET_ANDROID
   OR TC_TARGET_IOS
   OR TC_TARGET_LINUX
   OR TC_TARGET_MACOS
   OR TC_TARGET_WINDOWS)
  set(TC_RENDERER_SKIA_DEFAULT ON)
else()
  set(TC_RENDERER_SKIA_DEFAULT OFF)
endif()

if(TC_TARGET_LINUX OR TC_TARGET_MACOS OR TC_TARGET_WINDOWS)
  set(TC_WINDOWING_SDL_DEFAULT ON)
else()
  set(TC_WINDOWING_SDL_DEFAULT OFF)
endif()

# Accept the inverse names as compatibility inputs when the canonical option
# was not supplied, then publish both names for the C/C++ preprocessor.  Use
# explicit cache assignments instead of option(): old CMake policies allow
# option() to discard a normal variable set from an inverse compatibility flag.
set(_tc_graphics_software_choice ${TC_GRAPHICS_SOFTWARE_DEFAULT})
if(DEFINED TC_GRAPHICS_SOFTWARE)
  set(_tc_graphics_software_choice ${TC_GRAPHICS_SOFTWARE})
elseif(DEFINED TC_GRAPHICS_GLES)
  if(TC_GRAPHICS_GLES)
    set(_tc_graphics_software_choice OFF)
  else()
    set(_tc_graphics_software_choice ON)
  endif()
endif()
set(TC_GRAPHICS_SOFTWARE ${_tc_graphics_software_choice} CACHE BOOL
  "Use the CPU/software graphics surface" FORCE)

set(_tc_renderer_skia_choice ${TC_RENDERER_SKIA_DEFAULT})
if(DEFINED TC_RENDERER_SKIA)
  set(_tc_renderer_skia_choice ${TC_RENDERER_SKIA})
elseif(DEFINED TC_RENDERER_LEGACY)
  if(TC_RENDERER_LEGACY)
    set(_tc_renderer_skia_choice OFF)
  else()
    set(_tc_renderer_skia_choice ON)
  endif()
endif()
set(TC_RENDERER_SKIA ${_tc_renderer_skia_choice} CACHE BOOL
  "Use the Skia primitive renderer" FORCE)

set(_tc_windowing_sdl_choice ${TC_WINDOWING_SDL_DEFAULT})
if(DEFINED TC_WINDOWING_SDL)
  set(_tc_windowing_sdl_choice ${TC_WINDOWING_SDL})
elseif(DEFINED TC_WINDOWING_NATIVE)
  if(TC_WINDOWING_NATIVE)
    set(_tc_windowing_sdl_choice OFF)
  else()
    set(_tc_windowing_sdl_choice ON)
  endif()
endif()
set(TC_WINDOWING_SDL ${_tc_windowing_sdl_choice} CACHE BOOL
  "Use SDL for window creation and events" FORCE)

if(TC_GRAPHICS_SOFTWARE)
  set(TC_GRAPHICS_GLES OFF CACHE BOOL "Use the GLES graphics surface" FORCE)
else()
  set(TC_GRAPHICS_GLES ON CACHE BOOL "Use the GLES graphics surface" FORCE)
endif()

if(TC_RENDERER_SKIA)
  set(TC_RENDERER_LEGACY OFF CACHE BOOL "Use the legacy primitive renderer" FORCE)
else()
  set(TC_RENDERER_LEGACY ON CACHE BOOL "Use the legacy primitive renderer" FORCE)
endif()

if(TC_WINDOWING_SDL)
  set(TC_WINDOWING_NATIVE OFF CACHE BOOL "Use native platform windowing" FORCE)
else()
  set(TC_WINDOWING_NATIVE ON CACHE BOOL "Use native platform windowing" FORCE)
endif()

if(TC_TARGET_WINCE)
  if(TC_WINDOWING_SDL OR TC_RENDERER_SKIA OR TC_GRAPHICS_GLES)
    message(FATAL_ERROR "WinCE supports only Native + Legacy + Software")
  endif()
  set(TC_GRAPHICS_SOFTWARE ON CACHE BOOL "Use the CPU/software graphics surface" FORCE)
  set(TC_GRAPHICS_GLES OFF CACHE BOOL "Use the GLES graphics surface" FORCE)
  set(TC_RENDERER_SKIA OFF CACHE BOOL "Use the Skia primitive renderer" FORCE)
  set(TC_RENDERER_LEGACY ON CACHE BOOL "Use the legacy primitive renderer" FORCE)
  set(TC_WINDOWING_SDL OFF CACHE BOOL "Use SDL for window creation and events" FORCE)
  set(TC_WINDOWING_NATIVE ON CACHE BOOL "Use native platform windowing" FORCE)
endif()

if(TC_TARGET_WINDOWS AND TC_WINDOWING_NATIVE AND TC_RENDERER_SKIA)
  message(FATAL_ERROR "Windows Native windowing supports only the Legacy renderer")
endif()

# ---------------------------------------------------------------------------
# Validation
# ---------------------------------------------------------------------------

function(tc_require_exactly_one group_name)
  set(enabled_options)

  foreach(option_name IN LISTS ARGN)
    if(${option_name})
      list(APPEND enabled_options "${option_name}")
    endif()
  endforeach()

  list(LENGTH enabled_options enabled_count)

  if(NOT enabled_count EQUAL 1)
    if(enabled_options)
      string(JOIN ", " enabled_text ${enabled_options})
    else()
      set(enabled_text "none")
    endif()

    message(
      FATAL_ERROR
      "${group_name} requires exactly one enabled option; "
      "currently enabled: ${enabled_text}"
    )
  endif()
endfunction()

tc_require_exactly_one(
  "Graphics implementation"
  TC_GRAPHICS_GLES
  TC_GRAPHICS_SOFTWARE
)

tc_require_exactly_one(
  "Renderer"
  TC_RENDERER_SKIA
  TC_RENDERER_LEGACY
)

tc_require_exactly_one(
  "Windowing implementation"
  TC_WINDOWING_SDL
  TC_WINDOWING_NATIVE
)

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------

message(
  STATUS
  "TotalCross graphics: "
  "GLES=${TC_GRAPHICS_GLES}, "
  "Software=${TC_GRAPHICS_SOFTWARE}"
)

message(
  STATUS
  "TotalCross renderer: "
  "Skia=${TC_RENDERER_SKIA}, "
  "Legacy=${TC_RENDERER_LEGACY}"
)

message(
  STATUS
  "TotalCross windowing: "
  "SDL=${TC_WINDOWING_SDL}, "
  "Native=${TC_WINDOWING_NATIVE}"
)
