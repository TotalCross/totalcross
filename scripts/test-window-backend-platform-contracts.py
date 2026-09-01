#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Focused source contracts for Window backend/platform-service ownership."""

import pathlib
import re
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
WINDOW = ROOT / "TotalCrossVM/src/nm/ui/Window.c"
UI = ROOT / "TotalCrossVM/src/nm/ui"
BACKENDS = {
    "sdl": UI / "sdl/Window_c.h",
    "win": UI / "win/Window_c.h",
    "linux": UI / "linux/Window_c.h",
    "android": UI / "android/Window_c.h",
    "darwin": UI / "darwin/Window_c.h",
}
SERVICES = {
    "win": UI / "win/WindowServices_c.h",
    "linux": UI / "linux/WindowServices_c.h",
    "macos": UI / "macos/WindowServices_c.h",
    "android": UI / "android/WindowServices_c.h",
    "darwin": UI / "darwin/WindowServices_c.h",
}


class WindowBackendPlatformContractTests(unittest.TestCase):
    def test_window_selects_backend_and_services_independently(self):
        source = WINDOW.read_text()
        self.assertLess(source.index("#if TC_WINDOWING_SDL"),
                        source.index("#elif TC_WINDOWING_NATIVE"))
        self.assertIn('#include "sdl/Window_c.h"', source)
        self.assertIn('#include "win/Window_c.h"', source)
        self.assertIn('#include "linux/Window_c.h"', source)
        self.assertIn('#include "android/Window_c.h"', source)
        self.assertIn('#include "darwin/Window_c.h"', source)
        self.assertIn('#error Unsupported native Window backend', source)
        self.assertIn('#error No Window backend selected', source)
        self.assertIn('#include "win/WindowServices_c.h"', source)
        self.assertIn('#include "macos/WindowServices_c.h"', source)
        self.assertIn('#include "linux/WindowServices_c.h"', source)
        self.assertIn('#include "android/WindowServices_c.h"', source)
        self.assertIn('#include "darwin/WindowServices_c.h"', source)
        self.assertIn("#elif TC_OS_MACOS", source)
        self.assertIn("#elif TC_OS_IOS", source)
        self.assertIn('#error Unsupported Window platform services', source)
        self.assertNotRegex(source, r"defined\((?:WIN32|WINCE|ANDROID|linux|darwin)\)")

    def test_window_methods_call_only_standardized_adapters(self):
        source = WINDOW.read_text()
        for function in (
            "windowPlatformIsSIPShown",
            "windowPlatformSetSIP",
            "windowBackendSetDeviceTitle",
            "windowPlatformSetOrientation",
            "windowPlatformGetSafeAreaInsets",
        ):
            self.assertIn(function, source)
        self.assertNotRegex(source, r"\bwindow(?:Get|Set)(?:SIP|Orientation|SafeAreaInsets|DeviceTitle)")

    def test_all_services_expose_the_same_static_contract(self):
        signatures = (
            r"static bool windowPlatformIsSIPShown\(void\)",
            r"static void windowPlatformSetSIP\(\s*Context currentContext,\s*int32 sipOption,\s*TCObject control,\s*bool numeric\)",
            r"static void windowPlatformSetOrientation\(int32 orientation\)",
            r"static void windowPlatformGetSafeAreaInsets\(\s*int32 \*top,\s*int32 \*left,\s*int32 \*bottom,\s*int32 \*right\)",
        )
        for path in SERVICES.values():
            source = path.read_text()
            for signature in signatures:
                self.assertRegex(source, signature, path.name)

    def test_window_headers_obey_ownership_boundaries(self):
        for path in BACKENDS.values():
            source = path.read_text()
            self.assertIn("windowBackendSetDeviceTitle", source, path.name)
            self.assertNotRegex(source, r"SDL_(?:Start|Stop|IsTextInput)", path.name)
        for path in SERVICES.values():
            source = path.read_text()
            self.assertNotRegex(source, r"SDL_(?:Start|Stop|IsTextInput)", path.name)
        self.assertNotIn("windowSetSIP", BACKENDS["sdl"].read_text())
        self.assertNotIn("windowSetSIP", BACKENDS["android"].read_text())
        self.assertIn('"setSIP"', SERVICES["android"].read_text())
        self.assertIn('"getSafeAreaInsets"', SERVICES["android"].read_text())

    def test_new_service_headers_use_current_year_policy(self):
        expected = (
            "// Copyright (C) 2026 Amalgam Solucoes em TI Ltda\n"
            "//\n"
            "// SPDX-License-Identifier: LGPL-2.1-only\n"
        )
        for path in (SERVICES["android"], SERVICES["darwin"]):
            source = path.read_text()
            self.assertTrue(source.startswith(expected), path.name)
            self.assertNotIn("SuperWaba", source, path.name)
            self.assertNotIn("TotalCross Global", source, path.name)

    def test_unsupported_safe_area_services_write_explicit_zeros(self):
        for name in ("macos", "linux", "win"):
            source = SERVICES[name].read_text()
            function = source[source.index("static void windowPlatformGetSafeAreaInsets") :]
            self.assertIn("*top = *left = *bottom = *right = 0;", function, name)

        sdl = BACKENDS["sdl"].read_text()
        self.assertNotRegex(sdl, r"SIP|SDL_(?:Start|Stop|IsTextInput)")

    def test_platform_dispatch_does_not_use_linux_for_macos(self):
        source = WINDOW.read_text()
        service_dispatch = source[source.index('#if TC_OS_WINDOWS || TC_OS_WINCE') :]
        self.assertIn('#elif TC_OS_MACOS\n #include "macos/WindowServices_c.h"', service_dispatch)
        self.assertNotIn('TC_OS_MACOS\n #include "linux/WindowServices_c.h"', service_dispatch)
        native_dispatch = source[source.index("#elif TC_WINDOWING_NATIVE") :
                                 source.index("#else\n #error No Window backend selected")]
        self.assertIn('#elif TC_OS_IOS\n  #include "darwin/Window_c.h"', native_dispatch)
        self.assertNotIn('#include "linux/Window_c.h"\n #else', native_dispatch)


if __name__ == "__main__":
    unittest.main()
