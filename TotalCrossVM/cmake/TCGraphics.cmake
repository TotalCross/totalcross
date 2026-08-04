# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

include_guard(GLOBAL)

#
# Graphics API used to access the destination surface.
#
set(TC_GRAPHICS_GLES OFF)
set(TC_GRAPHICS_SOFTWARE OFF)

#
# Renderer responsible for drawing TotalCross primitives.
#
set(TC_RENDERER_SKIA OFF)
set(TC_RENDERER_LEGACY OFF)

#
# Window and native surface integration.
#
set(TC_WINDOWING_SDL OFF)
set(TC_WINDOWING_NATIVE OFF)

# ---------------------------------------------------------------------------
# Graphics
# ---------------------------------------------------------------------------

if(TC_TARGET_ANDROID OR TC_TARGET_IOS)
  set(TC_GRAPHICS_GLES ON)
else()
  set(TC_GRAPHICS_SOFTWARE ON)
endif()

# ---------------------------------------------------------------------------
# Renderer
# ---------------------------------------------------------------------------

if(TC_TARGET_ANDROID
   OR TC_TARGET_IOS
   OR TC_TARGET_LINUX
   OR TC_TARGET_MACOS)
  set(TC_RENDERER_SKIA ON)
else()
  set(TC_RENDERER_LEGACY ON)
endif()

# ---------------------------------------------------------------------------
# Windowing
# ---------------------------------------------------------------------------

if(TC_TARGET_LINUX OR TC_TARGET_MACOS)
  set(TC_WINDOWING_SDL ON)
else()
  set(TC_WINDOWING_NATIVE ON)
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
