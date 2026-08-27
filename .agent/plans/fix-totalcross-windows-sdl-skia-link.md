<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# ExecPlan — Fix the TotalCross Windows SDL + Skia link

Repository: `TotalCross/totalcross`  
Executor: Luna  
Plan state: `.agent/state/fix-windows-sdl-skia-link.md`

Execute this plan imperatively. Treat it as complete task context. Do not depend
on chat history. Follow `AGENTS.md` and `.agent/PLANS.md`.

## Purpose / Big Picture

Make the Windows desktop build link successfully with the migration branch's
default backend combination:

```text
TC_WINDOWING_SDL=ON
TC_WINDOWING_NATIVE=OFF
TC_RENDERER_SKIA=ON
TC_RENDERER_LEGACY=OFF
TC_GRAPHICS_SOFTWARE=ON
TC_GRAPHICS_GLES=OFF
```

The previous CI attempt compiled all relevant C/C++ sources and failed only at
link time for two independent reasons:

1. TotalCrossVM CMake output used MSVC `/MD`, while depot Skia and SDL2 Windows
   static libraries use `/MT`. Skia produced `LNK2038`:
   `MT_StaticRelease` versus `MD_DynamicRelease`, followed by C++ CRT duplicate
   symbols and `LNK4098`.
2. The previously published Windows SDL2 artifact embedded SDL's fallback
   `dlmalloc`. `SDL2-static.lib(SDL_malloc.obj)` therefore collided with
   TotalCrossVM's own `dlmalloc.obj`, producing `LNK2005`.

Fix both ownership boundaries:

- restore TotalCrossVM's Windows CRT policy to `/MT` for non-Debug
  configurations and `/MTd` for Debug;
- advance the depot-tools pin to an immutable revision publishing SDL2 with
  Windows `SDL_LIBC=ON`.

Do not suppress linker errors. Do not change TotalCross allocator semantics. Do
not rebuild Skia as `/MD`. Do not use `/FORCE:MULTIPLE` or `/NODEFAULTLIB`.

Final acceptance:

- a fresh Win32 Release build selects SDL + Skia + software;
- `tcvm.dll` links;
- no `LNK2005`, `LNK2038`, `LNK4098`, or `LNK1169` from these defects remains;
- Native + Legacy + software still builds as fallback.

## Working Set and Resume Protocol

Use:

- `.agent/state/fix-windows-sdl-skia-link.md`
- `.agent/evidence/fix-windows-sdl-skia-link.md`
- `.agent/evidence/logs/`
- `.agent/reports/fix-windows-sdl-skia-link-editorial.md`

First execution:

1. Read `AGENTS.md`.
2. Read `.agent/PLANS.md`.
3. Read only:
   - `TotalCrossVM/CMakeLists.txt`
   - `TotalCrossVM/cmake/TCGraphics.cmake`
   - `TotalCrossVM/deps/totalcross-depot-tools.ref`
   - `TotalCrossVM/deps/fetch-depot-tools.sh`
   - the Windows lane in `.github/workflows/build.yml`.
4. Search narrowly for an existing MSVC runtime policy.
5. Create state before editing.

Resume:

1. Read state first.
2. Continue from `Next concrete action`.
3. Read only paths named in state.
4. Do not reread the failed CI log; its relevant evidence is below.
5. Do not reread the earlier Windows migration plan unless a new architecture
   contradiction is discovered.

Use this state shape:

```text
Active milestone:
Current TotalCross HEAD:
Last logical commit:
Current depot-tools pin:
Candidate SDL2 release tag:
Verified depot release commit:
Active build directory:
Focused validation completed:
Deferred validation:
Blockers:
Next concrete action:
Resume command:
```

Redirect full logs to `.agent/evidence/logs/`. Preserve unrelated local changes
and generated dependency state.

## Established Diagnosis

The failed Windows job configured:

```cmd
cmake ..\ -G"Visual Studio 17 2022" -A Win32
cmake --build . --config Release
```

and selected:

```text
TotalCross graphics: GLES=OFF, Software=ON
TotalCross renderer: Skia=ON, Legacy=OFF
TotalCross windowing: SDL=ON, Native=OFF
```

All relevant C/C++ sources compiled, including Skia and SDL windowing sources.
The final link showed:

```text
SDL2-static.lib(SDL_malloc.obj) : error LNK2005: _dlmalloc already defined in dlmalloc.obj
SDL2-static.lib(SDL_malloc.obj) : error LNK2005: _dlfree already defined in dlmalloc.obj
```

and:

```text
error LNK2038: mismatch detected for 'RuntimeLibrary':
value 'MT_StaticRelease' doesn't match value 'MD_DynamicRelease' in skia.obj
```

Treat these as established facts.

Windows depot static artifacts intentionally use `/MT`. Historical TotalCross
Windows Release projects also used static CRT. The current CMake path has no
equivalent explicit runtime selection, so Visual Studio defaults to `/MD`.

`TotalCrossVM/src/util/mem.h` and the VM build intentionally own a `dlmalloc`
implementation. Do not remove it to accommodate SDL.

## Preconditions

The CRT correction can be implemented and committed immediately.

The final depot pin requires a published allocator-safe SDL2 release from
`TotalCross/totalcross-depot-tools`.

A valid candidate release must:

1. be an immutable annotated SDL2 revision tag;
2. contain a Windows-only `-DSDL_LIBC=ON` in `sdl2/CMakeLists.txt`;
3. record the same effective tag in `sdl2/manifest.yml`;
4. publish Windows SDL2 static artifacts under the existing contract;
5. pass the depot `/MT` verifier and omit SDL `dlmalloc` definitions.

The first expected revised tag is `sdl2-2.32.8-r1`, but never assume it. Use the
actual published revision.

If no valid release exists, finish the CRT commit and leave state as:

```text
Blockers: BLOCKED_ON_DEPOT_RELEASE
Next concrete action: verify the next allocator-safe SDL2 depot release
```

Do not change the pin to an unverified commit.

## Progress

- [x] Baseline and state established.
- [x] CRT policy restored.
- [x] Generated Visual Studio runtime verified in CI (`MultiThreaded` Release,
      `MultiThreadedDebug` Debug).
- [x] CRT correction committed (`4fdb6b044`).
- [x] Allocator-safe SDL2 release located and verified (`sdl2-2.32.8-r2`).
- [x] Immutable depot pin updated.
- [x] Depot pin committed (`044515ca0`).
- [x] Fresh SDL + Skia Win32 Release links in CI; `tcvm.dll` produced with no
      known linker failures.
- [x] Native + Legacy fallback links in a separate CI job; `tcvm.dll` produced.
- [x] Windows CI confirmed when authorized, including generated Release/Debug
      static CRT assertions.
- [x] Editorial report completed with host limitation recorded.

## Decision Rules

### D1 — Avoid duplicate runtime policy

Run:

```bash
rg -n \
  'CMAKE_MSVC_RUNTIME_LIBRARY|MSVC_RUNTIME_LIBRARY|/MTd?|/MDd?' \
  TotalCrossVM/CMakeLists.txt TotalCrossVM/cmake
```

If an existing centralized policy already guarantees:

```text
Debug                         -> /MTd
Release/RelWithDebInfo/MinSizeRel -> /MT
```

for the targets created by `TotalCrossVM/CMakeLists.txt`, do not add another
policy. Validate generated `.vcxproj` runtime values instead.

Otherwise apply the exact patch below.

### D2 — Preserve CMake 3.11 compatibility

Do not raise `cmake_minimum_required(VERSION 3.11)` solely for this fix.

Use `CMP0091` when available. For older CMake, rewrite legacy `/MD` flags to
`/MT` after `project()` and before targets are created.

Only remove that fallback if the repository already has a higher effective
minimum supporting `CMP0091`.

### D3 — Align the VM with dependencies, not vice versa

Do not rebuild Skia or SDL2 as `/MD`. Depot Windows static artifacts use `/MT`.
Restore the VM to that contract.

### D4 — Interpret intermediate linker results correctly

After the CRT patch but before the new SDL2 artifact:

- `LNK2038` must disappear;
- SDL `dlmalloc` `LNK2005` may remain.

If `LNK2038` remains, inspect generated `tcvm.vcxproj` and compiler flags before
continuing.

If only SDL allocator collisions remain, accept the CRT commit and continue to
the depot release.

### D5 — Verify before pinning

Never update `totalcross-depot-tools.ref` based only on a newer-looking tag.

Resolve the candidate annotated tag to its peeled commit and verify the checked
out depot source contains the SDL fix and matching release metadata.

Reject a candidate that lacks `SDL_LIBC=ON`.

### D6 — Preserve immutable pin style

The current ref file uses a 40-character commit SHA. Preserve that style. Pin
the peeled commit of the verified SDL2 release tag. Never pin `main`.

### D7 — Reject linker suppression

Never use:

- `/FORCE:MULTIPLE`;
- `/NODEFAULTLIB`;
- `LNK4098` suppression;
- link-order manipulation to choose one allocator;
- removal of TotalCross `dlmalloc.obj`.

## Plan of Work

### Milestone 0 — Establish baseline

From repository root:

```bash
git status --short -- \
  TotalCrossVM/CMakeLists.txt \
  TotalCrossVM/deps/totalcross-depot-tools.ref \
  TotalCrossVM/cmake/TCGraphics.cmake \
  .github/workflows/build.yml

git log -8 --oneline -- \
  TotalCrossVM/CMakeLists.txt \
  TotalCrossVM/deps/totalcross-depot-tools.ref

rg -n \
  'CMAKE_MSVC_RUNTIME_LIBRARY|MSVC_RUNTIME_LIBRARY|/MTd?|/MDd?' \
  TotalCrossVM/CMakeLists.txt TotalCrossVM/cmake
```

Record current branch, HEAD, existing depot pin, and unrelated dirty changes.
Do not reset or clean the worktree.

### Milestone 1 — Restore static MSVC runtime

If Decision D1 finds no equivalent policy, edit
`TotalCrossVM/CMakeLists.txt`.

Replace the opening:

```cmake
cmake_minimum_required(VERSION 3.11)

include(FetchContent)

# set the project name
project(tcvm VERSION 7.2.2)
```

with exactly:

```cmake
cmake_minimum_required(VERSION 3.11)

set(TC_MSVC_RUNTIME_POLICY_AVAILABLE FALSE)

if(POLICY CMP0091)
  cmake_policy(SET CMP0091 NEW)
  set(CMAKE_MSVC_RUNTIME_LIBRARY
    "MultiThreaded$<$<CONFIG:Debug>:Debug>"
  )
  set(TC_MSVC_RUNTIME_POLICY_AVAILABLE TRUE)
endif()

include(FetchContent)

# set the project name
project(tcvm VERSION 7.2.2)

if(MSVC AND NOT TC_MSVC_RUNTIME_POLICY_AVAILABLE)
  foreach(flag_var
      CMAKE_C_FLAGS
      CMAKE_C_FLAGS_DEBUG
      CMAKE_C_FLAGS_RELEASE
      CMAKE_C_FLAGS_MINSIZEREL
      CMAKE_C_FLAGS_RELWITHDEBINFO
      CMAKE_CXX_FLAGS
      CMAKE_CXX_FLAGS_DEBUG
      CMAKE_CXX_FLAGS_RELEASE
      CMAKE_CXX_FLAGS_MINSIZEREL
      CMAKE_CXX_FLAGS_RELWITHDEBINFO)
    if(DEFINED ${flag_var})
      string(REGEX REPLACE "/MD" "/MT"
        ${flag_var} "${${flag_var}}")
    endif()
  endforeach()
endif()
```

Why:

- `CMP0091` must be NEW before `project()` enables MSVC languages;
- `MultiThreaded` maps to `/MT`;
- the `Debug` generator-expression suffix maps Debug to `/MTd`;
- on CMake versions predating `CMP0091`, replacing `/MD` with `/MT` also
  converts `/MDd` to `/MTd`;
- fallback runs only for MSVC;
- non-MSVC builds are not rewritten.

Do not scatter `/MT` across individual targets unless this centralized method is
proven incompatible with the current project.

Validate:

```bash
git diff --check
git diff -- TotalCrossVM/CMakeLists.txt
```

### Milestone 2 — Validate generated runtime selection

Use a new build directory, not the previous failed one.

PowerShell in a Visual Studio 2022 developer environment:

```powershell
$BuildDir = "TotalCrossVM\vc2022-runtime-fix"

cmake -S TotalCrossVM -B $BuildDir `
  -G "Visual Studio 17 2022" `
  -A Win32

if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
```

Inspect:

```powershell
Select-String `
  -Path "$BuildDir\tcvm.vcxproj" `
  -Pattern '<RuntimeLibrary>MultiThreaded(Debug)?</RuntimeLibrary>'
```

Require:

```xml
Release: <RuntimeLibrary>MultiThreaded</RuntimeLibrary>
Debug:   <RuntimeLibrary>MultiThreadedDebug</RuntimeLibrary>
```

If Release says `MultiThreadedDLL`, stop before commit.

Build Release with bounded diagnostics:

```powershell
New-Item -ItemType Directory -Force .agent\evidence\logs | Out-Null

cmake --build $BuildDir --config Release -- /m `
  *> .agent\evidence\logs\windows-runtime-fix-release.log

$rc = $LASTEXITCODE
if ($rc -ne 0) {
  Select-String `
    -Path .agent\evidence\logs\windows-runtime-fix-release.log `
    -Pattern 'LNK2005|LNK2038|LNK4098|LNK1169|fatal error| error ' |
    Select-Object -First 40
  Get-Content .agent\evidence\logs\windows-runtime-fix-release.log -Tail 100
}
```

Apply Decision D4. Do not require final link success yet if the old SDL2 artifact
is still pinned.

### Commit Checkpoint 1 — CRT correction

Run current focused header/copyright validation, then:

```bash
git diff --check
git status --short -- TotalCrossVM/CMakeLists.txt
git diff -- TotalCrossVM/CMakeLists.txt
git add TotalCrossVM/CMakeLists.txt
```

Commit exactly:

```text
fix(cmake,windows): restore static MSVC runtime

Windows static native dependencies, including Skia and SDL2, are built with
the /MT runtime, but CMake-generated TotalCrossVM targets were left on MSVC's
default /MD runtime. Linking the Windows SDL + Skia configuration therefore
fails with LNK2038 and duplicate C++ runtime symbols.

Restore the historical TotalCross Windows runtime selection by using /MT for
non-Debug configurations and /MTd for Debug. Use CMP0091 when available and
retain a flag-rewrite fallback for the repository's CMake 3.11 minimum.

This aligns the VM with depot static libraries without changing non-MSVC
builds.
```

Command:

```bash
git commit -F - <<'EOF'
fix(cmake,windows): restore static MSVC runtime

Windows static native dependencies, including Skia and SDL2, are built with
the /MT runtime, but CMake-generated TotalCrossVM targets were left on MSVC's
default /MD runtime. Linking the Windows SDL + Skia configuration therefore
fails with LNK2038 and duplicate C++ runtime symbols.

Restore the historical TotalCross Windows runtime selection by using /MT for
non-Debug configurations and /MTd for Debug. Use CMP0091 when available and
retain a flag-rewrite fallback for the repository's CMake 3.11 minimum.

This aligns the VM with depot static libraries without changing non-MSVC
builds.
EOF
```

Rewrite state with commit SHA and the remaining linker class, if any.

### Milestone 3 — Locate the corrected SDL2 release

If an effective release tag was supplied by the depot-tools execution, use it as
a candidate.

Otherwise discover revised SDL2 releases:

```bash
gh release list \
  --repo TotalCross/totalcross-depot-tools \
  --limit 100 \
  --json tagName,publishedAt \
  --jq '.[] | select(.tagName | startswith("sdl2-2.32.8-r"))'
```

Choose the newest candidate only for verification.

Example only:

```powershell
$SdlReleaseTag = "sdl2-2.32.8-r1"
```

Use the actual published tag.

Resolve its annotated tag:

```powershell
$remote = git ls-remote `
  https://github.com/TotalCross/totalcross-depot-tools.git `
  "refs/tags/$SdlReleaseTag" `
  "refs/tags/$SdlReleaseTag^{}"

$peeled = $remote | Where-Object { $_ -match '\^\{\}$' }
if (-not $peeled) {
  throw "Annotated depot release tag could not be peeled"
}

$DepotReleaseCommit = ($peeled -split "`t")[0]
if ($DepotReleaseCommit -notmatch '^[0-9a-f]{40}$') {
  throw "Invalid depot release commit"
}
$DepotReleaseCommit
```

Do not edit the pin yet.

### Milestone 4 — Verify depot source through the consumer bootstrap

Check generated depot checkout status first:

```bash
git -C TotalCrossVM/deps/totalcross-depot-tools status --short 2>/dev/null || true
```

If it contains user modifications, do not switch it. Use a temporary clean
checkout for source verification.

Otherwise use the supported override:

```powershell
$env:TOTALCROSS_DEPOT_TOOLS_REF = $SdlReleaseTag
bash TotalCrossVM/deps/fetch-depot-tools.sh
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
```

Confirm resolved commit:

```powershell
$resolved = git -C TotalCrossVM/deps/totalcross-depot-tools rev-parse HEAD
if ($resolved.Trim() -ne $DepotReleaseCommit.Trim()) {
  throw "Depot checkout does not match the release commit"
}
```

Verify source:

```bash
rg -n -C 5 'SDL_LIBC=ON' \
  TotalCrossVM/deps/totalcross-depot-tools/sdl2/CMakeLists.txt
```

Require `SDL_LIBC=ON` to be under `if(WIN32)`.

Verify metadata:

```powershell
Select-String `
  -Path TotalCrossVM\deps\totalcross-depot-tools\sdl2\manifest.yml `
  -Pattern "release:\s*$([regex]::Escape($SdlReleaseTag))"
```

Reject the candidate if either check fails.

Clear the override:

```powershell
Remove-Item Env:TOTALCROSS_DEPOT_TOOLS_REF -ErrorAction SilentlyContinue
```

### Milestone 5 — Update immutable depot pin

Edit only:

```text
TotalCrossVM/deps/totalcross-depot-tools.ref
```

Replace the current non-comment ref with the verified:

```text
<40-character DepotReleaseCommit>
```

Do not commit the placeholder; write the real SHA.

Do not pin `main` or an unverified tag. Do not commit anything under the
generated `TotalCrossVM/deps/totalcross-depot-tools/` checkout.

Validate file shape:

```bash
test "$(grep -v '^[[:space:]]*#' \
  TotalCrossVM/deps/totalcross-depot-tools.ref |
  sed '/^[[:space:]]*$/d' |
  wc -l | tr -d ' ')" = "1"

git diff --check
git diff -- TotalCrossVM/deps/totalcross-depot-tools.ref
```

Fetch with no override and prove the committed pin resolves:

```powershell
Remove-Item Env:TOTALCROSS_DEPOT_TOOLS_REF -ErrorAction SilentlyContinue

bash TotalCrossVM/deps/fetch-depot-tools.sh
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$resolved = git -C TotalCrossVM/deps/totalcross-depot-tools rev-parse HEAD
$expected = (
  Get-Content TotalCrossVM\deps\totalcross-depot-tools.ref |
  Where-Object { $_ -notmatch '^\s*(#|$)' } |
  Select-Object -First 1
).Trim()

if ($resolved.Trim() -ne $expected) {
  throw "Bootstrap did not resolve the committed depot pin"
}
```

### Commit Checkpoint 2 — Depot pin

Run focused repository validation, then:

```bash
git diff --check
git status --short -- TotalCrossVM/deps/totalcross-depot-tools.ref
git diff -- TotalCrossVM/deps/totalcross-depot-tools.ref
git add TotalCrossVM/deps/totalcross-depot-tools.ref
```

Commit exactly:

```text
build(deps): consume allocator-safe SDL2 release

The previous Windows SDL2 artifact embeds SDL's fallback dlmalloc, so linking
it with TotalCrossVM produces duplicate dlmalloc-family symbols and fails with
LNK2005.

Advance the depot-tools pin to the immutable revision that publishes SDL2 with
SDL_LIBC=ON. This keeps SDL2 static and /MT-compatible while leaving
TotalCrossVM as the single owner of its existing dlmalloc implementation.
```

Command:

```bash
git commit -F - <<'EOF'
build(deps): consume allocator-safe SDL2 release

The previous Windows SDL2 artifact embeds SDL's fallback dlmalloc, so linking
it with TotalCrossVM produces duplicate dlmalloc-family symbols and fails with
LNK2005.

Advance the depot-tools pin to the immutable revision that publishes SDL2 with
SDL_LIBC=ON. This keeps SDL2 static and /MT-compatible while leaving
TotalCrossVM as the single owner of its existing dlmalloc implementation.
EOF
```

Rewrite state with both logical commit SHAs, effective SDL2 tag, and pinned depot
commit.

### Milestone 6 — Decisive default Windows build

Use a fresh build directory so stale `/MD` project files cannot survive.

```powershell
$BuildDir = "TotalCrossVM\vc2022-sdl-skia-link-fix"
New-Item -ItemType Directory -Force .agent\evidence\logs | Out-Null

cmake -S TotalCrossVM -B $BuildDir `
  -G "Visual Studio 17 2022" `
  -A Win32 `
  *> .agent\evidence\logs\windows-sdl-skia-configure.log

if ($LASTEXITCODE -ne 0) {
  Get-Content .agent\evidence\logs\windows-sdl-skia-configure.log -Tail 100
  exit $LASTEXITCODE
}

cmake --build $BuildDir --config Release -- /m `
  *> .agent\evidence\logs\windows-sdl-skia-release.log

$rc = $LASTEXITCODE
if ($rc -ne 0) {
  Select-String `
    -Path .agent\evidence\logs\windows-sdl-skia-release.log `
    -Pattern 'LNK2005|LNK2038|LNK4098|LNK1169|fatal error| error ' |
    Select-Object -First 50
  Get-Content .agent\evidence\logs\windows-sdl-skia-release.log -Tail 100
  exit $rc
}
```

Confirm backend selection:

```powershell
Select-String `
  -Path .agent\evidence\logs\windows-sdl-skia-configure.log `
  -Pattern `
    'TotalCross graphics: GLES=OFF, Software=ON',
    'TotalCross renderer: Skia=ON, Legacy=OFF',
    'TotalCross windowing: SDL=ON, Native=OFF'
```

Require all three.

Assert known linker failures are absent:

```powershell
$bad = Select-String `
  -Path .agent\evidence\logs\windows-sdl-skia-release.log `
  -Pattern 'LNK2005|LNK2038|LNK4098|LNK1169'

if ($bad) {
  $bad | Select-Object -First 50
  throw "Known Windows link failures remain"
}
```

Require output:

```powershell
if (-not (Test-Path "$BuildDir\Release\tcvm.dll")) {
  throw "tcvm.dll was not produced"
}
```

Confirm Release runtime:

```powershell
Select-String `
  -Path "$BuildDir\tcvm.vcxproj" `
  -Pattern '<RuntimeLibrary>MultiThreaded</RuntimeLibrary>'
```

If a new unrelated linker error appears only after both known defect classes are
gone, retain these corrections. Record the new first blocker and classify it
before expanding scope.

### Milestone 7 — Validate Native + Legacy fallback

Use another fresh directory:

```powershell
$FallbackDir = "TotalCrossVM\vc2022-native-legacy-runtime-fix"

cmake -S TotalCrossVM -B $FallbackDir `
  -G "Visual Studio 17 2022" `
  -A Win32 `
  -DTC_WINDOWING_NATIVE=ON `
  -DTC_WINDOWING_SDL=OFF `
  -DTC_RENDERER_LEGACY=ON `
  -DTC_RENDERER_SKIA=OFF `
  -DTC_GRAPHICS_SOFTWARE=ON `
  *> .agent\evidence\logs\windows-native-legacy-configure.log

if ($LASTEXITCODE -ne 0) {
  Get-Content .agent\evidence\logs\windows-native-legacy-configure.log -Tail 100
  exit $LASTEXITCODE
}

cmake --build $FallbackDir --config Release -- /m `
  *> .agent\evidence\logs\windows-native-legacy-release.log

if ($LASTEXITCODE -ne 0) {
  Select-String `
    -Path .agent\evidence\logs\windows-native-legacy-release.log `
    -Pattern 'LNK[0-9]+|fatal error| error ' |
    Select-Object -First 50
  Get-Content .agent\evidence\logs\windows-native-legacy-release.log -Tail 100
  exit $LASTEXITCODE
}
```

Require fallback `tcvm.dll`.

Do not broaden this checkpoint into Android/iOS builds solely because
`CMakeLists.txt` changed. The compatibility fallback is `MSVC`-guarded and the
modern runtime variable affects MSVC targets.

### Milestone 8 — CI confirmation

When remote CI is authorized, push both commits normally to the active PR
branch. Do not force-push or rewrite existing migration commits.

Require the Windows CI lane using:

```cmd
cmake ..\ -G"Visual Studio 17 2022" -A Win32
cmake --build . --config Release
```

to pass.

If CI fails, inspect the first fatal error and bounded context. Do not rerun the
old investigation unless the error actually reintroduces one of the known
failure classes.

## Validation and Acceptance

CRT checkpoint:

- generated Release runtime = `MultiThreaded`;
- generated Debug runtime = `MultiThreadedDebug`;
- `LNK2038` absent.

Depot checkpoint:

- candidate SDL2 tag is annotated and immutable;
- source contains Windows `SDL_LIBC=ON`;
- manifest names the same effective release;
- committed pin resolves to the peeled release commit.

Functional Windows gate:

- default SDL + Skia + software Win32 Release links;
- Native + Legacy + software Win32 Release links;
- resulting DLL exists;
- no known LNK2005/LNK2038/LNK4098/LNK1169 failure remains.

Milestone/PR gate:

- Windows CI passes after push.

Do not run the entire cross-platform matrix unless current repository policy or a
new regression requires it.

## Risks, Recovery, and Idempotence

- If an equivalent CRT policy already exists, validate instead of duplicating.
- If no allocator-safe SDL2 release exists, stop after commit 1.
- If the expected `-r1` tag has advanced, verify and use the actual revision.
- If generated depot checkout is dirty, preserve it and verify via a temporary
  checkout.
- If stale Visual Studio files preserve `/MD`, use a new build directory.
- If commit 1 already exists, resume at depot-release verification.
- If both commits exist, resume at the decisive Windows build.
- Never hard reset, amend, rebase, force-push, or discard unrelated work.
- Never commit generated depot checkout contents.

## Explicit Non-Goals

Do not change:

- SDL/Skia windowing architecture;
- Skia raster pipeline;
- HiDPI behavior;
- pixel formats;
- SDL event handling;
- Windows default backend selection;
- Native + Legacy fallback design;
- TotalCross allocator behavior;
- SDL or Skia build recipes inside this repository.

Existing non-fatal compiler warnings are out of scope unless they become the
first fatal blocker after the two known linker defects are removed.

## Surprises & Discoveries

Initial facts:

- compilation completes; this task starts at link compatibility;
- Skia is built as `MT_StaticRelease`, while TotalCross CMake output was
  `MD_DynamicRelease`;
- SDL and TotalCross both defined the same prefixed `dlmalloc` symbols.
- After the planned `/MT` and allocator-safe SDL2 corrections, CI exposed a
  separate closure mismatch: published `axtls` and `minizip-ng` Windows
  archives, plus the private SQLite SEE override, still use the dynamic MSVC
  CRT. The correct remediation is external dependency release work, not a
  linker suppression in TotalCross.
- After those releases were corrected, qrcodegen was found to be the remaining
  published Windows archive carrying an `MSVCRT` directive. It was rebuilt as
  `qrcodegen-20250123-r2`; the final consumer CI was then clean.

Record only new discoveries that change remaining execution.

## Decision Log

- Restore `/MT` in TotalCrossVM rather than changing depot Skia/SDL.
- Preserve CMake 3.11 using `CMP0091` plus a legacy fallback.
- Keep TotalCross as owner of its existing allocator.
- Consume a revised SDL2 artifact instead of suppressing duplicate symbols.
- Preserve the existing immutable 40-hex depot pin style.
- Rebuild the complete Windows static dependency closure when CI exposes an
  additional published `/MD` artifact; the consumer must not suppress the CRT
  conflict.

## Outcomes & Retrospective

At completion record:

- CRT-fix commit SHA: `4fdb6b044`.
- Depot pin commit SHA: `e2fe6c2a4` is the final CI assertion commit; the
  dependency pin was introduced by `044515ca0`, `4efb13e0d`, and `cae516c90`.
- Effective SDL2 release tag: `sdl2-2.32.8-r2`.
- Pinned depot commit: `0ebff1d7202fab6e61758344219f60fa757fe6ce`.
- Generated runtime values: `MultiThreaded` for Release and
  `MultiThreadedDebug` for Debug, asserted in Visual Studio projects by CI.
- Default SDL + Skia Win32 result: passed; `Release/tcvm.dll` produced.
- Native + Legacy fallback result: passed; `Release/tcvm.dll` produced.
- Windows CI run/result: `33040181465`, complete success.
- New blocker: none. Local Visual Studio execution was skipped per user
  instruction; CI provided the required validation.

Write `.agent/reports/fix-windows-sdl-skia-link-editorial.md` using the editorial
headings required by `.agent/PLANS.md`.

## Revision Note

Initial revision. This plan embeds the established linker diagnosis, exact CMake
patch, immutable depot verification/pinning procedure, commit subjects and
motivation bodies, and focused validation flow so Luna can resume without
rereading the original GitHub Actions log or reconstructing the preceding
Windows migration investigation.
