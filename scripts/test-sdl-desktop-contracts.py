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
WINDOW = ROOT / "TotalCrossVM/src/nm/ui/Window.c"
WINDOW_H = ROOT / "TotalCrossVM/src/nm/ui/Window.h"
SDL_WINDOW = ROOT / "TotalCrossVM/src/nm/ui/sdl/Window_c.h"
WIN_WINDOW = ROOT / "TotalCrossVM/src/nm/ui/win/Window_c.h"
WIN_GFX = ROOT / "TotalCrossVM/src/nm/ui/win/gfx_Graphics_c.h"
GRAPHICS = ROOT / "TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h"
DISPATCH = ROOT / "TotalCrossVM/src/event/specialkeys.c"
EVENT_DISPATCH = ROOT / "TotalCrossVM/src/event/Event.c"
SDL_KEYS = ROOT / "TotalCrossVM/src/event/sdl/specialkeys_c.h"
LINUX_KEYS = ROOT / "TotalCrossVM/src/event/linux/specialkeys_c.h"
STARTUP = ROOT / "TotalCrossVM/src/init/startup.c"
STARTUP_TEST = ROOT / "TotalCrossVM/src/init/startup_test.h"
SDL_INIT = ROOT / "TotalCrossVM/src/init/tcsdl.cpp"
SDL_HEADER = ROOT / "TotalCrossVM/src/init/tcsdl.h"
SDL_EVENT_HEADER = ROOT / "TotalCrossVM/src/event/sdl/event_sdl.h"
WIN_VM = ROOT / "TotalCrossVM/src/nm/sys/win/Vm_c.h"


def has_mapping(source, device, portable):
    return re.search(
        rf"case\s+{re.escape(device)}\s*:\s*return\s+{re.escape(portable)}\s*;",
        source,
    ) is not None


class SDLDesktopContractTests(unittest.TestCase):
    def test_event_loop_throttle_excludes_only_ios(self):
        source = EVENT_DISPATCH.read_text()
        pump = source[source.index("static bool pumpEvent") :
                      source.index("bool isEventAvailable")]
        self.assertRegex(pump, r"#if !TC_OS_IOS\s+Sleep\(1\);")
        self.assertNotIn("TC_OS_APPLE", pump)
        self.assertNotIn("#ifndef darwin", pump)

    def test_keyboard_event_lifecycle_keeps_text_and_special_paths(self):
        source = EVENT.read_text()
        self.assertIn("case SDL_KEYDOWN:", source)
        self.assertIn("case SDL_TEXTINPUT:", source)
        init = source[source.index("bool privateInitEvent()") :
                      source.index("void sdlEventWindowCreated(void)")]
        destroy = source[source.index("void privateDestroyEvent()") :]
        self.assertNotIn("SDL_StartTextInput();", init)
        self.assertNotIn("SDL_StopTextInput();", destroy)
        self.assertIn("SDL_FlushEvents(SDL_FIRSTEVENT, SDL_LASTEVENT);", destroy)
        self.assertIn("void sdlEventWindowCreated(void)", source)
        self.assertIn("void sdlEventWindowDestroying(void)", source)
        header = SDL_EVENT_HEADER.read_text()
        self.assertIn("void sdlEventWindowCreated(void);", header)
        self.assertIn("void sdlEventWindowDestroying(void);", header)
        sdl_init = SDL_INIT.read_text()
        window_creation = sdl_init.index("window = SDL_CreateWindow")
        created = sdl_init.index("sdlEventWindowCreated();", window_creation)
        self.assertGreater(created, sdl_init.index("return false;", window_creation))
        self.assertLess(created, sdl_init.index("renderer = SDL_CreateRenderer", window_creation))
        self.assertNotIn("SDL_StartTextInput();", sdl_init)
        window_destruction = sdl_init.index("void TCSDL_DestroyWindow")
        destroying = sdl_init.index("sdlEventWindowDestroying();", window_destruction)
        destroyed = sdl_init.index("SDL_DestroyWindow(window)", window_destruction)
        self.assertLess(destroying, destroyed)
        self.assertNotIn("SDL_StopTextInput();", sdl_init)

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
        shortcuts = source[source.index("static bool isControlShortcut") :
                           source.index("static void handleKeyboardEvent")]
        self.assertIn("modifiers & KMOD_CTRL", shortcuts)
        for shortcut in ("SDLK_a", "SDLK_c", "SDLK_p", "SDLK_v", "SDLK_x", "SDLK_SPACE"):
            self.assertIn(shortcut, shortcuts)
        self.assertNotIn(
            "postEvent(mainContext, KEYEVENT_KEY_PRESS, key, 0, 0,",
            keyboard,
        )
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
        self.assertIn("dispatchPortableSpecialKey(vmWin32KeyToPortable(key), -1)", event)

    def test_sdl_special_dispatch_preserves_screen_change_behavior(self):
        source = EVENT.read_text()
        helper = source[source.index("static void dispatchPortableSpecialKey") :
                        source.index("#if defined(WIN32)")]
        self.assertIn("if (key == SK_SCREEN_CHANGE)", helper)
        self.assertIn("*tcSettings.screenWidthPtr != *tcSettings.screenHeightPtr", helper)
        self.assertIn("screen.minScreenW", helper)
        self.assertIn("screen.minScreenH", helper)
        self.assertIn("TCSDL_GetWindowSize(&screen, &width, &height)", helper)
        self.assertIn("TCSDL_SetWindowSize(height, width)", helper)
        self.assertNotIn("screenChange(", helper)
        self.assertNotIn("screenChangeCommitted(", helper)
        self.assertNotIn("screenApplyConfiguration(", helper)
        self.assertIn("postEvent(mainContext, KEYEVENT_SPECIALKEY_PRESS", helper)
        keyboard = source[source.index("static void handleKeyboardEvent") :
                           source.index("static void handleWheelEvent")]
        self.assertIn("dispatchPortableSpecialKey(key, event.key.keysym.mod)", keyboard)
        self.assertNotIn("KEYEVENT_SPECIALKEY_PRESS, key", keyboard)

    def test_sdl_rotation_uses_one_event_driven_hidpi_commit(self):
        event = EVENT.read_text()
        resize = event[event.index("case SDL_WINDOWEVENT_SIZE_CHANGED") :
                       event.index("case SDL_WINDOWEVENT_MINIMIZED")]
        for operation in (
            "TCSDL_QueryWindowMetrics(&screen, &configuration)",
            "screenApplyConfiguration(",
            "screenConsumePendingChanges(&screen)",
            "screenChangeCommitted(mainContext, changes)",
        ):
            self.assertIn(operation, resize)
        self.assertEqual(1, resize.count("screenChangeCommitted("))
        helper = event[event.index("static void dispatchPortableSpecialKey") :
                       event.index("#if defined(WIN32)")]
        self.assertNotIn("screenChangeCommitted(", helper)
        self.assertNotIn("graphicsDestroy(", helper)

        sdl = SDL_INIT.read_text()
        metrics = sdl[sdl.index("bool TCSDL_QueryWindowMetrics") :
                      sdl.index("bool TCSDL_Init")]
        self.assertIn("SDL_GetWindowSize(window, &logicalWidth, &logicalHeight)", metrics)
        self.assertIn("SDL_GetRendererOutputSize(renderer, &physicalWidth, &physicalHeight)", metrics)
        self.assertIn("configuration->width = physicalWidth", metrics)
        self.assertIn("configuration->height = physicalHeight", metrics)
        self.assertNotIn("tcSettings.screenWidthPtr", metrics)

    def test_window_backends_own_resize_and_native_rotation(self):
        window = WINDOW.read_text()
        header = WINDOW_H.read_text()
        sdl_window = SDL_WINDOW.read_text()
        win_window = WIN_WINDOW.read_text()
        graphics = GRAPHICS.read_text()
        win_graphics = WIN_GFX.read_text()

        self.assertIn("bool windowBackendSetSize(int32 width, int32 height)", header)
        self.assertIn("return windowBackendSetSizeImpl(width, height);", window)
        self.assertIn("TCSDL_SetWindowSize(width, height)", sdl_window)
        self.assertIn("void adjustWindowSizeWithBorders", win_window)
        self.assertIn("SetWindowPos(mainHWnd, 0, 0, 0, width, height", win_window)
        self.assertNotIn("privateScreenChange", graphics)
        self.assertNotIn("privateScreenChange", win_graphics)
        graphics_hook = win_graphics[:win_graphics.index("#if defined (WINCE)")]
        self.assertNotIn("adjustWindowSizeWithBorders", graphics_hook)

        native_event = (ROOT / "TotalCrossVM/src/event/win/event_c.h").read_text()
        self.assertIn("windowBackendSetSize(*tcSettings.screenHeightPtr", native_event)
        self.assertIn("screenChange(mainContext, *tcSettings.screenHeightPtr", native_event)

    def test_tcsdl_window_size_operation_is_generic(self):
        header = SDL_HEADER.read_text()
        source = SDL_INIT.read_text()
        operation = source[source.index("bool TCSDL_SetWindowSize") :
                           source.index("bool TCSDL_CreateBackBuffer")]
        self.assertIn("bool TCSDL_SetWindowSize(int32 width, int32 height)", header)
        self.assertIn("SDL_SetWindowSize(window, width, height)", operation)
        self.assertNotIn("screenChange", operation)
        self.assertNotIn("F9", operation)

    def test_sdl_backend_owns_key_dispatch_before_platform_branches(self):
        source = DISPATCH.read_text()
        self.assertLess(source.index("#if TC_WINDOWING_SDL"), source.index("#elif TC_WINDOWING_NATIVE"))
        self.assertIn('#include "sdl/specialkeys_c.h"', source)
        self.assertIn("#if TC_OS_WINDOWS || TC_OS_WINCE", source)
        self.assertIn("#elif TC_OS_ANDROID", source)
        self.assertIn("#elif TC_OS_IOS", source)
        self.assertIn("#elif TC_OS_LINUX", source)
        self.assertIn("#error Unsupported native special-key backend", source)
        self.assertIn("#error No special-key backend selected", source)
        self.assertNotRegex(source, r"defined\((?:WIN32|WINCE|ANDROID|linux|darwin)\)")
        self.assertNotIn("TC_WINDOWING_SDL", LINUX_KEYS.read_text())

        event = EVENT_DISPATCH.read_text()
        self.assertLess(event.index("#if TC_WINDOWING_SDL"), event.index("#elif TC_WINDOWING_NATIVE"))
        self.assertIn('#include "sdl/event_c.h"', event)
        self.assertIn('#include "win/event_c.h"', event)
        self.assertIn('#include "android/event_c.h"', event)
        self.assertIn('#include "darwin/event_c.h"', event)
        self.assertIn('#include "linux/event_c.h"', event)
        self.assertIn("#error Unsupported native event backend", event)
        self.assertIn("#error No event backend selected", event)
        self.assertNotRegex(event, r"defined\((?:WIN32|WINCE|ANDROID|linux|darwin)\)")

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
        parser = source[source.index("static bool filterDesktopCommandLine") :]
        preparer = source[source.index("static bool prepareDesktopCommandLines") :]
        for option in ("/scr", "/fullscreen", "/maximized", "/sdlPixelFormat"):
            self.assertIn(f'"{option}"', source)
        for option in ('"-t"', '"-p"', '"-testsuite"'):
            self.assertIn(option, source)
        self.assertIn("while (*read != '\\0')", parser)
        self.assertNotIn("commandEnd", parser[:parser.index("#endif")])
        self.assertIn("filterDesktopCommandLine(vmCommandLine, options)", preparer)
        self.assertIn("separator = findCommandSeparator(vmCommandLine)", preparer)
        self.assertIn('xstrstr(search, " /cmd")', source)
        self.assertIn("separator + 6", preparer)
        self.assertIn("applicationCommandLine", preparer)
        self.assertIn("commandLineToParse = cmdline == null ? null : applicationCommandLine", source)
        self.assertIn("desktopCommandLineOptions.testSuiteRequested", source)
        self.assertIn("xstrncpy(commandLine, c, sizeof(commandLine) - 1)", source)
        self.assertIn("appendCommandToken", source)
        self.assertIn("search = separator + 5", source)
        separator = source[source.index("static CharP findCommandSeparator") :
                           source.index("static bool filterDesktopCommandLine")]
        self.assertIn("#if TC_OS_DESKTOP", separator)
        self.assertIn('#else\n   return xstrstr(command, " /cmd ");', separator)
        self.assertIn('"App.tcz -t /cmdlike /scr -2,-2,800,600 /cmd foo /fullscreen bar "',
                      STARTUP_TEST.read_text())
        self.assertIn("/scrSomething /cmdlike -testsuitelike", STARTUP_TEST.read_text())

        self.assertIn("position[optionLength] == ' ' || position[optionLength] == '\\0'", source)
        test_source = STARTUP_TEST.read_text()
        self.assertIn("/cmdlike", test_source)
        self.assertIn("-testsuitelike", test_source)
        self.assertIn('"foo bar baz qux /scrSomething /cmdlike -testsuitelike"',
                      test_source)


if __name__ == "__main__":
    unittest.main()
