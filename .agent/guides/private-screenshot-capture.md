<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Private and deterministic application screenshot capture

Read this guide before taking screenshots or packaging visual evidence.

## Goal

Capture only the TotalCross application window launched for the current test.
Avoid leaking browser tabs, email subjects, chat titles, Finder paths, usernames,
other application names, notifications, or desktop content.

## Window selection

Prefer selection by the launched process identifier and a window owned by that
process. A valid alternative is an explicitly known application window handle or
ID returned directly by the launcher.

Do not choose a window by globally dumping all titles and manually matching a
string. Do not persist a list of unrelated windows in logs or evidence.

The capture helper may inspect windows internally, but normal output must contain
only sanitized data for the selected target:

    target process ID
    target window ID
    application identifier
    bounds
    capture status

Do not print the full target title unless the fixture sets a deterministic,
non-private title. Detailed enumeration is allowed only behind an explicit local
debug flag, and its output must never be copied into artifacts or evidence.

## Launch and capture sequence

1. Build the exact tested commit.
2. Launch the fixture with a deterministic non-private window title.
3. Record the launched process ID directly.
4. Wait for the expected window and rendered-ready signal rather than sleeping an
   arbitrary long interval.
5. Resolve a visible window owned by that process.
6. Verify bounds are positive and plausible.
7. Capture that window only.
8. Verify the image decodes and has the expected dimensions.
9. Crop decorative borders only when the crop rule is deterministic and recorded.
10. Inspect and sanitize before hashing.

If process-based capture is unavailable, use a dedicated clean desktop or virtual
display and crop to an explicitly selected region. Record the limitation.

## macOS considerations

Use the platform screenshot API or a small helper that accepts a window ID.
Request Screen Recording permission manually if required; do not work around OS
privacy protections.

A Retina capture may have physical dimensions larger than logical window bounds.
Record both and the reported `contentScale`. Do not resize the evidence before
dimension metadata is recorded.

If the application moves between displays, capture after the scale-change render
is complete. Reject a frame containing stale content from the previous backing
scale.

## Android considerations

Prefer application-native screenshot or device screenshot cropped to the fixture
activity. Hide notifications and use deterministic status/navigation-bar handling.
Record whether system bars are included.

Do not include other recent-app thumbnails, notification shade content, or
developer-machine windows in Android evidence.

## Privacy inspection

Before accepting an image:

- open it and visually inspect every edge;
- confirm only the fixture application is visible;
- remove unrelated desktop background where practical;
- check for notifications, menu extras, dock previews, browser windows, terminal
  paths, email, messaging, and personal filenames;
- ensure the DANFE uses synthetic data;
- inspect embedded comments or metadata if the format can carry them.

Before accepting logs:

    rg -n -i \
      "private-user-images|authorization:|bearer |token=|password|/Users/|C:\\\\Users\\\\" \
      artifacts/logical-ui-scaling

Also search for known local usernames and unrelated application titles discovered
during the run, without committing those search terms to public evidence.

## Integrity checks

For every accepted screenshot:

- file exists and size is greater than zero;
- decoder succeeds;
- dimensions are recorded;
- expected application region is present;
- capture is not fully blank, transparent, or one color;
- no unrelated window is visible;
- hash is computed after final crop and sanitization.

Do not record a hash for a failed, empty, or rejected screenshot. Delete rejected
copies from the artifact package after recording only a sanitized failure reason.

## Evidence metadata

Use a small JSON or Markdown record containing:

    commit
    platform
    renderer
    process ID if safe for local evidence
    sanitized application identifier
    window logical bounds
    screenshot physical dimensions
    contentScale
    capture method
    crop method
    privacy review status
    file hash
    known limitation

Do not include absolute local paths in the final editorial report. Repository-
relative artifact paths are sufficient.

## Failure handling

If the target window cannot be resolved, stop and record:

- attempted capture method;
- whether the process launched;
- whether a target-owned visible window was found;
- the sanitized error;
- the log path.

Do not fall back automatically to a full-desktop screenshot.

If permissions block capture, document the permission requirement and perform
manual capture in a clean environment. The implementation may still proceed, but
final visual acceptance remains incomplete until a safe window capture exists.
