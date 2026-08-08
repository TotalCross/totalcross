<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Deterministic private macOS window screenshots

Use `/usr/sbin/screencapture` with a CoreGraphics window ID owned by the launched
process. Do not use Computer Use as the primary capture method.

## Launch without losing the owner PID

Launch the executable directly:

    "$APP_EXECUTABLE" >"$APP_LOG" 2>&1 &
    APP_PID=$!

For Java:

    java <arguments> totalcross.Launcher <fixture arguments> \
      >"$APP_LOG" 2>&1 &
    APP_PID=$!

For a native `.app`, execute:

    Fixture.app/Contents/MacOS/Fixture

Do not use `open`, because its PID may not own the window.

## CoreGraphics window-ID helper

Create a small helper such as:

    .agent/tools/macos-window-id.swift

with this behavior:

```swift
import Foundation
import CoreGraphics

guard CommandLine.arguments.count == 2,
      let requestedPID = Int32(CommandLine.arguments[1]) else {
  fputs("usage: macos-window-id <pid>\n", stderr)
  exit(2)
}

let options: CGWindowListOption = [
  .optionOnScreenOnly,
  .excludeDesktopElements
]

guard let windows =
    CGWindowListCopyWindowInfo(options, kCGNullWindowID)
      as? [[String: Any]] else {
  exit(3)
}

var best: (id: CGWindowID, bounds: CGRect, area: CGFloat)?

for window in windows {
  guard
    let owner = window[kCGWindowOwnerPID as String] as? NSNumber,
    owner.int32Value == requestedPID,
    let number = window[kCGWindowNumber as String] as? NSNumber,
    let layer = window[kCGWindowLayer as String] as? NSNumber,
    layer.intValue == 0,
    let alpha = window[kCGWindowAlpha as String] as? NSNumber,
    alpha.doubleValue > 0,
    let boundsObject = window[kCGWindowBounds as String] as? NSDictionary
  else {
    continue
  }

  var bounds = CGRect.zero
  guard CGRectMakeWithDictionaryRepresentation(
          boundsObject as CFDictionary, &bounds),
        bounds.width > 0,
        bounds.height > 0 else {
    continue
  }

  let area = bounds.width * bounds.height
  if best == nil || area > best!.area {
    best = (CGWindowID(number.uint32Value), bounds, area)
  }
}

guard let selected = best else {
  exit(4)
}

print("\(selected.id)\t\(Int(selected.bounds.origin.x))\t" +
      "\(Int(selected.bounds.origin.y))\t" +
      "\(Int(selected.bounds.width))\t" +
      "\(Int(selected.bounds.height))")
```

The helper must not read or print window titles, other application names, or
unrelated PIDs.

Compile it:

    xcrun swiftc .agent/tools/macos-window-id.swift \
      -o artifacts/logical-ui-scaling/tools/macos-window-id

The helper file remains below the plan's file-size limit.

## Wait for the target window

Poll only the target PID:

    WINDOW_INFO=""
    for attempt in $(seq 1 100); do
      if WINDOW_INFO=$(
        artifacts/logical-ui-scaling/tools/macos-window-id "$APP_PID"
      ); then
        break
      fi
      sleep 0.1
    done

    test -n "$WINDOW_INFO"
    WINDOW_ID=${WINDOW_INFO%%$'\t'*}

Do not dump global window lists while waiting.

## Capture

Pass the numeric CoreGraphics window ID to:

    /usr/sbin/screencapture -x -l "$WINDOW_ID" "$OUTPUT_PNG"

The lowercase `-l` option is followed by the window ID.

Check command status and verify that the PNG exists, decodes, is non-empty, and
has plausible dimensions.

## Screen Recording permission

If `screencapture` reports a permission failure, ask the user to grant Screen
Recording access to the terminal, IDE, or agent host that runs the command.

An unavailable OS permission is a genuine external blocker. Failure of a
Computer Use targeting integration is not a blocker until this direct
`screencapture` path has been attempted.

## Java and native captures

Capture both independently:

- Java Launcher process PID and window ID;
- deployed native macOS executable PID and window ID.

Label artifacts clearly. A Java screenshot cannot substitute for the native
application screenshot.

## Privacy

The fixture uses a deterministic non-private title and synthetic DANFE content.

Before accepting a screenshot:

- inspect every edge;
- ensure only the target application window is present;
- verify no notification, menu, browser, terminal, Finder, or desktop content;
- inspect image metadata;
- crop only by a deterministic recorded rule;
- hash only the final sanitized file.

Do not fall back to full-desktop capture. Do not retain rejected screenshots.

Scan logs and artifacts for local usernames, absolute paths, tokens,
authenticated URLs, and unrelated titles before packaging evidence.
