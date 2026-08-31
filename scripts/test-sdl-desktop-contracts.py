#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Focused source contracts for the SDL desktop migration."""

import pathlib
import re
import unittest


ROOT = pathlib.Path(__file__).resolve().parents[1]
EVENT = ROOT / "TotalCrossVM/src/event/sdl/event_c.h"
DISPATCH = ROOT / "TotalCrossVM/src/event/specialkeys.c"
SDL_KEYS = ROOT / "TotalCrossVM/src/event/sdl/specialkeys_c.h"
LINUX_KEYS = ROOT / "TotalCrossVM/src/event/linux/specialkeys_c.h"


def has_mapping(source, device, portable):
    return re.search(
        rf"case\s+{re.escape(device)}\s*:\s*return\s+{re.escape(portable)}\s*;",
        source,
    ) is not None


class SDLDesktopContractTests(unittest.TestCase):
    def test_keyboard_event_lifecycle_keeps_text_and_special_paths(self):
        source = EVENT.read_text()
        self.assertIn("case SDL_KEYDOWN:", source)
        self.assertIn("case SDL_TEXTINPUT:", source)
        init = source[source.index("bool privateInitEvent()") :]
        self.assertIn("SDL_StartTextInput();", init[:init.index("void privateDestroyEvent()")])
        self.assertIn("SDL_StopTextInput();", init[init.index("void privateDestroyEvent()"):])

    def test_sdl_backend_owns_key_dispatch_before_platform_branches(self):
        source = DISPATCH.read_text()
        self.assertLess(source.index("#if TC_WINDOWING_SDL"), source.index("#elif defined(WINCE)"))
        self.assertIn('#include "sdl/specialkeys_c.h"', source)
        self.assertNotIn("TC_WINDOWING_SDL", LINUX_KEYS.read_text())

    def test_sdl_special_key_round_trips_and_modifiers(self):
        source = SDL_KEYS.read_text()
        portable_to_sdl = {
            "SK_PAGE_UP": "SDLK_PAGEUP",
            "SK_PAGE_DOWN": "SDLK_PAGEDOWN",
            "SK_HOME": "SDLK_HOME",
            "SK_END": "SDLK_END",
            "SK_UP": "SDLK_UP",
            "SK_DOWN": "SDLK_DOWN",
            "SK_LEFT": "SDLK_LEFT",
            "SK_RIGHT": "SDLK_RIGHT",
            "SK_INSERT": "SDLK_INSERT",
            "SK_ENTER": "SDLK_RETURN",
            "SK_TAB": "SDLK_TAB",
            "SK_BACKSPACE": "SDLK_BACKSPACE",
            "SK_ESCAPE": "SDLK_ESCAPE",
            "SK_DELETE": "SDLK_DELETE",
            "SK_HARD1": "SDLK_F1",
            "SK_HARD2": "SDLK_F2",
            "SK_HARD3": "SDLK_F3",
            "SK_HARD4": "SDLK_F4",
            "SK_MENU": "SDLK_F6",
            "SK_CALC": "SDLK_F7",
            "SK_FIND": "SDLK_F8",
            "SK_SCREEN_CHANGE": "SDLK_F9",
            "SK_KEYBOARD_ABC": "SDLK_F11",
            "SK_ACTION": "SDLK_F12",
        }
        for device, portable in portable_to_sdl.items():
            self.assertTrue(has_mapping(source, device, portable), (device, portable))

        sdl_to_portable = {
            "SDLK_PAGEUP": "SK_PAGE_UP",
            "SDLK_PAGEDOWN": "SK_PAGE_DOWN",
            "SDLK_HOME": "SK_HOME",
            "SDLK_END": "SK_END",
            "SDLK_UP": "SK_UP",
            "SDLK_DOWN": "SK_DOWN",
            "SDLK_LEFT": "SK_LEFT",
            "SDLK_RIGHT": "SK_RIGHT",
            "SDLK_INSERT": "SK_INSERT",
            "SDLK_RETURN": "SK_ENTER",
            "SDLK_TAB": "SK_TAB",
            "SDLK_BACKSPACE": "SK_BACKSPACE",
            "SDLK_ESCAPE": "SK_ESCAPE",
            "SDLK_DELETE": "SK_DELETE",
            "SDLK_F1": "SK_HARD1",
            "SDLK_F2": "SK_HARD2",
            "SDLK_F3": "SK_HARD3",
            "SDLK_F4": "SK_HARD4",
            "SDLK_F6": "SK_MENU",
            "SDLK_F7": "SK_CALC",
            "SDLK_F8": "SK_FIND",
            "SDLK_F9": "SK_SCREEN_CHANGE",
            "SDLK_F10": "SK_HOME",
            "SDLK_F11": "SK_KEYBOARD_ABC",
            "SDLK_F12": "SK_ACTION",
        }
        for device, portable in sdl_to_portable.items():
            self.assertTrue(has_mapping(source, device, portable), (device, portable))

        self.assertIn("KMOD_LSHIFT", source)
        self.assertIn("KMOD_LCTRL", source)
        self.assertIn("KMOD_LALT", source)


if __name__ == "__main__":
    unittest.main()
