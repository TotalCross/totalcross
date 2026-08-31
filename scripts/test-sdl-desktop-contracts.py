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
STARTUP = ROOT / "TotalCrossVM/src/init/startup.c"
STARTUP_TEST = ROOT / "TotalCrossVM/src/init/startup_test.h"
SDL_INIT = ROOT / "TotalCrossVM/src/init/tcsdl.cpp"
WIN_VM = ROOT / "TotalCrossVM/src/nm/sys/win/Vm_c.h"


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
        sdl_init = SDL_INIT.read_text()
        window_creation = sdl_init.index("window = SDL_CreateWindow")
        self.assertLess(sdl_init.index("SDL_StartTextInput();", window_creation),
                        sdl_init.index("#if defined(WIN32)", window_creation))

    def test_sdl_events_pass_raw_modifiers_and_mouse_modifiers(self):
        source = EVENT.read_text()
        keyboard = source[source.index("static void handleKeyboardEvent") :
                           source.index("static void handleWheelEvent")]
        text = source[source.index("static void handleTextInputEvent") :
                       source.index("void privatePumpEvent")]
        mouse = source[source.index("static void handleMouseEvent") :
                       source.index("static void handleKeyboardEvent")]
        self.assertNotIn("keyGetPortableModifiers", keyboard)
        self.assertNotIn("keyGetPortableModifiers", text)
        self.assertIn("event.key.keysym.mod", keyboard)
        self.assertIn("SDL_GetModState()", text)
        self.assertNotIn("getTimeStamp", mouse)
        self.assertEqual(4, mouse.count("event.button.y, -1") + mouse.count("event.motion.y, -1"))
        keys = SDL_KEYS.read_text()
        self.assertIn("if (mods == -1)", keys)
        event_core = (ROOT / "TotalCrossVM/src/event/Event.c").read_text()
        self.assertIn("keyGetPortableModifiers(mods)", event_core)

    def test_windows_hotkeys_use_vk_values_independently_of_sdl_events(self):
        source = WIN_VM.read_text()
        mapper = source[source.index("static int32 vmPortableKeyToWin32") :
                        source.index("void registerHotkeys")]
        for portable, vk in {
            "SK_PAGE_UP": "VK_PRIOR",
            "SK_HOME": "VK_HOME",
            "SK_ENTER": "VK_RETURN",
            "SK_HARD1": "VK_F1",
            "SK_HARD2": "VK_F2",
            "SK_HARD3": "VK_F3",
            "SK_HARD4": "VK_F4",
            "SK_MENU": "VK_F6",
            "SK_CALC": "VK_F7",
            "SK_FIND": "VK_F8",
            "SK_SCREEN_CHANGE": "VK_F9",
            "SK_KEYBOARD_ABC": "VK_F11",
            "SK_ACTION": "VK_F12",
        }.items():
            self.assertTrue(has_mapping(mapper, portable, vk), (portable, vk))
        self.assertIn("*dk = vmPortableKeyToWin32(*keys);", source)
        self.assertIn("key = vmPortableKeyToWin32(key);", source)
        self.assertNotIn("*dk = keyPortable2Device(*keys);", source)
        event = EVENT.read_text()
        tcsdl = SDL_INIT.read_text()
        self.assertIn("SDL_SetWindowsMessageHook", event)
        self.assertIn("message != WM_HOTKEY", event)
        self.assertIn("vmWin32KeyToPortable(key)", event)
        self.assertIn("sdlInstallWindowsMessageHook();", tcsdl)
        self.assertIn("sdlRemoveWindowsMessageHook();", tcsdl)
        self.assertIn("SDL_SetWindowsMessageHook(null, null);", event)
        hook = event[event.index("sdlWindowsMessageHook") :]
        self.assertNotIn("keyDevice2Portable(key)", hook)

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

    def test_application_command_line_is_filtered_from_composite_vm_line(self):
        source = STARTUP.read_text()
        parser = source[source.index("static bool parseDesktopStartupOptions") :]
        preparer = source[source.index("static bool prepareDesktopCommandLines") :]
        for option in ("/scr", "/fullscreen", "/maximized", "/sdlPixelFormat"):
            self.assertIn(f'"{option}"', source)
        for option in ('"-t"', '"-p"', '"-testsuite"'):
            self.assertIn(option, source)
        self.assertIn("while (*read != '\\0')", parser)
        self.assertNotIn("commandEnd", parser[:parser.index("#endif")])
        self.assertIn("parseDesktopStartupOptions(vmCommandLine)", preparer)
        self.assertIn('separator = xstrstr(vmCommandLine, " /cmd ")', preparer)
        self.assertIn("separator + 6", preparer)
        self.assertIn("applicationCommandLine", preparer)
        self.assertIn("filterApplicationCommandLine(applicationCommandLine", source)
        self.assertIn("commandLineToParse = filteredApplicationCommandLine", source)
        self.assertIn("testSuiteRequested", source)
        self.assertIn("xstrncpy(commandLine, c, sizeof(commandLine) - 1)", source)

        self.assertIn("position[optionLength] == ' ' || position[optionLength] == '\\0'", source)
        test_source = STARTUP_TEST.read_text()
        self.assertIn('"/cmdlike"', test_source)
        self.assertIn('"-testsuitelike"', test_source)
        self.assertIn('xstrstr(filteredApplicationCommandLine, " /cmd ") != null', test_source)


if __name__ == "__main__":
    unittest.main()
