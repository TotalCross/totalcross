<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Android smoke-test procedure

Use this procedure for changes that can affect the Android runtime, packaging, deployment, graphics, input, window or IME behavior, or generated Android artifacts.

The test application, deployment output, logs, screenshots, and recordings must remain in a run-specific temporary directory outside the repository.

## Execution policy

Use the minimum test scope that can answer the question.

By default, test:

1. one explicitly selected device;
2. one representative application;
3. one build;
4. one deployment and installation;
5. one launch;
6. one focused visual or behavioral assertion.

Expand the matrix only when required by the changed behavior, risk level, or first result.

To keep execution and token usage efficient:

- Redirect Gradle, deployer, `adb`, `logcat`, and recording output to files.
- Do not include complete logs in the response or plan. Show only exit status, relevant matched lines, a short tail when needed, and file paths.
- Do not repeat a successful build, deployment, installation, or capture unless one of its inputs changed.
- Reuse an existing AAB or APK when its source, build configuration, SDK, and packaging inputs have not changed.
- Stop after the first blocking failure. Diagnose that failure before running later stages.
- Capture one representative screenshot and, only when required, one short screen recording.
- Prefer filtered `dumpsys` and `logcat` output over complete device diagnostics.
- Stop at the first validation level that proves or disproves the intended smoke-test criterion.
- Record broader scenarios that were skipped and why they were unnecessary.

## 1. Prepare an isolated workspace

Resolve repository paths before changing directories.

```bash
repo_root="$(git rev-parse --show-toplevel)"
work_dir="$(mktemp -d "${TMPDIR:-/tmp}/tc-android-smoke.XXXXXX")"

sdk_root=/path/to/TotalCrossSDK
device_serial=emulator-5554

app_jar="$work_dir/TestApp.jar"
deploy_dir="$work_dir/deploy"

package_name=totalcross.testapp
activity_name=.Loader
```

Do not use shared fixed paths for backups or generated test artifacts. A unique directory prevents one smoke-test execution from consuming or restoring files produced by another.

## 2. Select and record the target device

List connected devices and explicitly select one.

```bash
adb devices -l

adb -s "$device_serial" shell getprop ro.product.manufacturer
adb -s "$device_serial" shell getprop ro.product.model
adb -s "$device_serial" shell getprop ro.build.version.release
adb -s "$device_serial" shell getprop ro.build.version.sdk
adb -s "$device_serial" shell wm size
adb -s "$device_serial" shell wm density
```

Save the selected device, model, Android version, resolution, and density with the evidence.

Always use `adb -s "$device_serial"` after selecting the target. Unqualified commands are ambiguous when multiple devices or emulators are connected.

## 3. Build the Android AAB

Build from the branch under test and redirect the complete output.

```bash
bundle_log="$work_dir/android-bundle.log"

(
  cd "$repo_root/TotalCrossVM/android"
  ./gradlew :app:bundleStandardRelease \
    --console=plain \
    --warning-mode=none
) >"$bundle_log" 2>&1

bundle_status=$?
```

The expected artifact is:

```bash
aab="$repo_root/TotalCrossVM/android/app/build/outputs/bundle/standardRelease/app-standard-release.aab"
```

Verify the exit status and artifact instead of printing the complete Gradle log.

```bash
printf 'bundle status: %s\n' "$bundle_status"
test -f "$aab" && printf 'AAB: %s\n' "$aab"
```

When the build fails, inspect only relevant lines first:

```bash
rg -n -i \
  'error|failed|failure|exception|missing|not found' \
  "$bundle_log" \
  | tail -n 80
```

Run the native-dependency task only when the failure identifies missing native artifacts:

```bash
native_deps_log="$work_dir/native-dependencies.log"

(
  cd "$repo_root/TotalCrossVM/android"
  ./gradlew :tcvm:fetchNativeDependencies \
    --console=plain \
    --warning-mode=none
) >"$native_deps_log" 2>&1
```

After fetching the dependencies, retry the bundle task once.

Do not run `clean` unless stale build state is itself part of the suspected failure. Do not repeat the build when no source, dependency, build configuration, or packaging input changed.

## 4. Stage the AAB in the matching SDK

The deployer must use an SDK compatible with the AAB and test application.

The SDK must contain:

```text
dist/totalcross-sdk.jar
dist/libs/
dist/vm/android/TotalCross.aab
```

An installed SDK cache may be used instead of the repository SDK when it contains the required files and matches the branch or version being tested.

Preserve the current SDK AAB before replacing it:

```bash
sdk_aab="$sdk_root/dist/vm/android/TotalCross.aab"
sdk_aab_backup="$work_dir/original-TotalCross.aab"
sdk_aab_existed=0

if test -f "$sdk_aab"; then
  cp "$sdk_aab" "$sdk_aab_backup"
  sdk_aab_existed=1
fi

cp "$aab" "$sdk_aab"
```

Keep this replacement scoped to the test. Restore the previous state after testing:

```bash
if test "$sdk_aab_existed" -eq 1; then
  cp "$sdk_aab_backup" "$sdk_aab"
else
  rm -f "$sdk_aab"
fi
```

Do not commit the staged AAB.

## 5. Prepare the representative application

Use the smallest application that directly exercises the changed behavior.

Compile it against the same `totalcross-sdk.jar` used by `tc.Deploy`.

Prefer Java 8 bytecode for temporary smoke-test applications unless newer bytecode is explicitly part of the test:

```bash
javac --release 8 ...
```

This avoids an unnecessary failed deploy when an older deployer or ASM version cannot read newer class-file versions.

If deployment reports an ASM `ClassReader`, unsupported class version, or bytecode-version error, verify how the application was compiled before investigating the Android runtime.

Do not add unrelated controls, screens, assets, or scenarios to the smoke-test application.

## 6. Deploy the application

Run `tc.Deploy` using the selected SDK and redirect its complete output.

```bash
deploy_log="$work_dir/android-deploy.log"

TOTALCROSS3_HOME="$sdk_root" \
java -Xmx2g \
  -cp "$sdk_root/dist/totalcross-sdk.jar:$sdk_root/dist/libs/*" \
  tc.Deploy "$app_jar" \
  -android \
  /p \
  /o "$deploy_dir" \
  >"$deploy_log" 2>&1

deploy_status=$?
printf 'deploy status: %s\n' "$deploy_status"
```

The expected APK is:

```bash
apk="$deploy_dir/install/android/TestApp.apk"
test -f "$apk" && printf 'APK: %s\n' "$apk"
```

If deployment fails, inspect only focused excerpts:

```bash
rg -n -i \
  'error|failed|failure|exception|unsupported|classreader|bytecode' \
  "$deploy_log" \
  | tail -n 80
```

Do not continue to installation when deployment failed or the APK was not generated.

An AAB is a deployment template and is not installed directly with `adb`. Use `tc.Deploy` to generate the test APK.

## 7. Install and launch

Install the generated APK on the explicitly selected device:

```bash
install_log="$work_dir/android-install.log"

adb -s "$device_serial" install -r "$apk" \
  >"$install_log" 2>&1
```

Do not uninstall an existing package unless the exact package and its removal are explicitly within scope. Prefer `install -r` to preserve unrelated device state.

Force-stop and launch the known component deterministically:

```bash
adb -s "$device_serial" shell am force-stop "$package_name"

launch_log="$work_dir/android-launch.log"

adb -s "$device_serial" shell am start -W \
  -n "$package_name/$activity_name" \
  >"$launch_log" 2>&1
```

`am start -W` should be preferred over an unconditional long sleep.

Use one short, bounded additional wait only when the application needs time to render a surface, open the IME, perform animation, or reach another asynchronous state. Do not repeatedly poll when a single deterministic check can establish the result.

## 8. Capture focused evidence

Confirm that the expected application reached the foreground:

```bash
activity_log="$work_dir/android-activity.txt"

adb -s "$device_serial" shell dumpsys activity activities \
  | rg -m 3 \
      'topResumedActivity|mResumedActivity|totalcross' \
  >"$activity_log"
```

Capture one representative screenshot:

```bash
screenshot="$work_dir/android-screen.png"

adb -s "$device_serial" exec-out screencap -p \
  >"$screenshot"
```

Capture a focused logcat excerpt:

```bash
logcat_file="$work_dir/android-logcat.txt"

adb -s "$device_serial" logcat -d -t 300 \
  | rg -i \
      'FATAL EXCEPTION|AndroidRuntime|ANR|tombstoned|DEBUG|libc|totalcross|tcvm' \
  >"$logcat_file"
```

Do not paste the complete logcat buffer into the response.

For graphics, animation, lifecycle, or IME changes, capture one short screen recording only when a static screenshot cannot prove the criterion.

For keyboard or focus behavior:

- confirm the IME state with filtered `dumpsys input_method`;
- verify that the expected control retains focus;
- verify that the relevant content remains visible above the keyboard;
- record the keyboard implementation and navigation mode when they can affect the result.

Use `uiautomator dump` only when the application exposes useful native accessibility nodes. Controls rendered on a custom surface may not appear in the native UI hierarchy.

## 9. Apply escalation rules

The default smoke test ends after:

- a successful build;
- a successful deployment;
- a successful installation and launch;
- one focused assertion;
- no relevant fatal error in the focused logs.

Expand testing only when justified.

Examples of justified expansion include:

- code paths that differ by Android API level;
- physical-device-only behavior;
- emulator-only behavior;
- GPU or graphics-backend differences;
- window-insets, navigation-mode, density, orientation, or IME differences;
- lifecycle or process-recreation behavior;
- an inconsistent or inconclusive first result;
- a regression previously observed only on a specific device family.

Do not expand the matrix merely because additional devices are available.

## Limitations and restrictions

- AVD and physical-device results are not interchangeable. Android version, model, display size, density, IME implementation, GPU backend, navigation mode, and window-insets behavior can change the result.
- Record the device model, Android version, resolution, and density with the evidence.
- OpenGL output may appear blank or white in `adb exec-out screencap -p` while a screen recording displays the correct output.
- Some physical devices reject `screenrecord` with encoder errors such as `err=-38`. Use screenshots and focused logs as fallback evidence.
- Multiple connected devices make unqualified `adb` commands ambiguous. Always specify the selected serial.
- Do not run concurrent native-dependency fetches or builds that share the same generated dependency cache. Temporary archives and generated artifacts may collide.
- Run shared-cache operations sequentially or explicitly isolate their caches.
- Preserve unrelated working-tree and device state.
- Do not commit generated AABs, APKs, deployment directories, build outputs, native-dependency caches, screenshots, recordings, or local logs.
- Do not regenerate equivalent screenshots, videos, traces, or packages after documentation-only changes.
- Do not claim that a smoke test covers devices, Android versions, backends, or scenarios that were not exercised.

## Compact result format

Report the result using a compact structure:

```text
Android smoke test: PASS | FAIL | INCONCLUSIVE

Target:
- Device:
- Android:
- Resolution/density:

Artifacts:
- AAB:
- APK:

Validation:
- Build:
- Deploy:
- Install/launch:
- Focused assertion:

Evidence:
- Screenshot:
- Recording:
- Activity state:
- Focused logcat:

Limitations or skipped scenarios:
- ...
```

Include only relevant error excerpts. Reference the full logs by path instead of reproducing them.
