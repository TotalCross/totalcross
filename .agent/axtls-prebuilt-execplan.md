<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Migrate axTLS to depot-tools prebuilts

This ExecPlan is a living document. Maintain the `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` sections according to `.agent/PLANS.md`.

## Purpose / Big Picture

The TotalCross VM will link the axTLS 2.1.5 static archive supplied by `totalcross-depot-tools` instead of compiling `TotalCrossVM/src/axtls`. The Java provider selected by `SSLContext.getInstance("", "base")` will continue to use axTLS, while the default provider continues to use its existing implementation.

Before changing the implementation, and again afterwards, a deployed VM smoke application will prove the intended provider matrix against local TLS echo servers: `base` succeeds with TLS 1.2 RSA/AES-CBC and rejects TLS 1.3 and ECDSA; the default provider succeeds with TLS 1.2 RSA/AES-CBC and ECDSA, while its current TLS 1.3 limitation must produce `Error Code: FFFF8880`. The TLS servers use self-signed certificates, so certificate verification is intentionally not part of this test.

## Progress

- [x] (2026-07-10 03:47Z) Inspected the existing axTLS sources, socket hash table, build wiring, and depot-tools tag `axtls-2.1.5`.
- [x] (2026-07-10 03:47Z) Confirmed that the tag publishes port hooks and artifacts for the supported CMake/CI targets.
- [x] (2026-07-10) Created, deployed, and ran the pre-migration six-case provider smoke matrix on macOS; results are in `TotalCrossVM/tests/axtls/results/pre-migration-provider-smoke.log`.
- [x] (2026-07-10) Pinned depot-tools to `axtls-2.1.5`, wired prebuilt discovery/fetching into CMake and Android Gradle, and added the CI cache directory.
- [x] (2026-07-10) Added the TotalCross axTLS port-hook adapter and removed the global socket hash table, mutex, and `tcSocketReadWrite`.
- [x] (2026-07-10) Removed `TotalCrossVM/src/axtls` and migrated direct consumers to installed axTLS headers.
- [x] (2026-07-10) Built the CMake VM and Android arm64-v8a target, fetched every published target archive, and reran the identical post-migration smoke matrix.

## Surprises & Discoveries

- Observation: the new hook table is copied into `SSL_CTX`, and its `user_data` must survive until `ssl_ctx_free()`.
  Evidence: `axtls/extensions/axtls_port.h` and `axtls/README.md` in the pinned tag.

- Observation: the published artifact matrix has no WinCE archive.
  Evidence: `axtls/manifest.yml` lists Linux, Windows, Android, iOS, iOS simulator, and macOS only.

- Observation: `vc2008` and `TotalCrossVM/src/jni/Android.mk` are discontinued build paths.
  Evidence: user direction on 2026-07-10.

- Observation: before the baseline, `ssl_SSL.c` limited `HAVE_IMPLEMENTATION` to `linux`, `WIN32`, and `ANDROID`, leaving the axTLS native entry points as stubs on macOS.
  Evidence: `TotalCrossVM/src/nm/net/ssl_SSL.c` before the user-requested all-platform enablement.

- Observation: no local Linux runner is available in this workspace host.
  Evidence: `build/Launcher` is Mach-O arm64, and `docker`, `podman`, `colima`, `lima`, and `act` are unavailable.

- Observation: the default provider's macOS mbedTLS prebuilt cannot negotiate TLS 1.3.
  Evidence: `mbedtls_config.h` comments out `MBEDTLS_SSL_PROTO_TLS1_3`; the smoke completes default TLS 1.2, base TLS 1.2, and base TLS 1.3 rejection, but default TLS 1.3 fails with `0xFFFF8880` (the TLS fatal-alert error returned after a TLS 1.2-only ClientHello reaches the TLS 1.3-only server).

- Observation: both baseline and prebuilt-axTLS smoke runs receive the same `Error Code: FFFF8880` for the default provider against the TLS 1.3-only fixture.
  Evidence: `pre-migration-provider-smoke.log` and `post-migration-provider-smoke.log` both mark `default TLSv1.3 unsupported (FFFF8880)` as passed.

## Decision Log

- Decision: do not edit or validate `TotalCrossVM/vc2008/TCVM.vcproj` or `TotalCrossVM/src/jni/Android.mk`.
  Rationale: both targets are unsupported. Their stale references do not constrain the supported CMake and Android Gradle paths.
  Date/Author: 2026-07-10 / user and Codex.

- Decision: use local OpenSSL TLS fixtures: TLS 1.2 RSA/AES-CBC, TLS 1.3-only, and TLS 1.2 ECDSA.
  Rationale: public HTTPS endpoints commonly require ECDHE, ECC, or TLS 1.3, which would make the provider expectations nondeterministic.
  Date/Author: 2026-07-10 / Codex.

- Decision: bind the Java socket to `AXTLS_PORT_HOOKS.user_data` owned by the associated SSL context, rather than indexing it in a global table by file descriptor.
  Rationale: this is the extension contract supplied by axTLS 2.1.5 and removes global synchronization.
  Date/Author: 2026-07-10 / Codex.

- Decision: enable the existing `ssl_SSL.c` implementation for every platform before recording the baseline.
  Rationale: the macOS build previously compiled the SSL entry points as stubs, preventing the requested provider comparison. This is a preparatory compatibility fix requested by the user, not the prebuilt migration.
  Date/Author: 2026-07-10 / user and Codex.

## Outcomes & Retrospective

The migration is complete for supported CMake and Android Gradle builds. The CMake VM and Android arm64-v8a link the depot-tools `libaxtls.a`; no source is compiled from `TotalCrossVM/src/axtls`. The six-case smoke passed both before and after the refactor. The default provider still returns the expected TLS 1.3 error `FFFF8880`, because mbedTLS is intentionally unchanged; the default provider succeeds with TLS 1.2 ECDSA, while the base provider rejects it. The base provider still succeeds with the TLS 1.2 RSA/AES-CBC fixture and rejects TLS 1.3.

## Context and Orientation

`TotalCrossVM/CMakeLists.txt` currently builds every axTLS C source under `TotalCrossVM/src/axtls`. `TotalCrossVM/android/tcvm/build.gradle` fetches Android native dependencies and invokes the supported external CMake build. `.github/workflows/build.yml` owns Docker-created dependency cache directories.

`TotalCrossVM/src/nm/net/ssl_SSL.c` creates axTLS contexts and connections. It currently puts a `totalcross.net.Socket` object into `htSSLSocket`; `TotalCrossVM/src/nm/net/Socket.c` looks it up in `tcSocketReadWrite`. The globals and their mutex are declared in `TotalCrossVM/src/init/globals.[ch]` and `TotalCrossVM/src/tcvm/tcthread.h`.

The tag exposes `ssl_ctx_new_with_port(options, sessions, hooks)` and `AXTLS_PORT_HOOKS`. The callbacks receive a caller-owned `user_data` pointer. The TotalCross adapter will keep the socket object, method reference, byte buffer, and active VM `Context` there, invoke `Socket.readWriteBytes(byte[], int, int, boolean)` through `executeMethod`, and preserve pending Java exceptions.

## Plan of Work

### Milestone 1: establish the behavioral baseline

Add a test-only TotalCross application under `TotalCrossSDK/src/test/resources/axtls/` and Gradle tasks analogous to `compileModernJavaFeatureSmoke` and `deployModernJavaFeatureSmoke`. The application takes three fixture ports and runs the default provider first, then the base provider:

1. Default TLS 1.2 RSA/AES-CBC: must succeed.
2. Default TLS 1.3: must fail with `Error Code: FFFF8880` while mbedTLS has no TLS 1.3 support.
3. Default TLS 1.2 ECDSA: must succeed.
4. Base TLS 1.2 RSA/AES-CBC: must succeed.
5. Base TLS 1.3: must fail.
6. Base TLS 1.2 ECDSA: must fail.

Create `TotalCrossVM/tests/axtls/run-provider-smoke.sh`. It generates temporary RSA and ECDSA self-signed certificates, starts three `openssl s_server -rev` processes, runs the app with the current `Launcher`, and records concise output in `TotalCrossVM/tests/axtls/results/<phase>-provider-smoke.log`. The program emits detailed progress plus one marker for each case and exits nonzero unless all expectations hold.

Run this milestone before changing any production code. If the headless host cannot initialize a TotalCross `MainWindow`, document the exact runtime limitation and run the same generated `.tcz` on a supported graphical host before proceeding with behavioral claims.

The current implementation must first be enabled on all platforms by removing the platform gate in `ssl_SSL.c`. Then record the successful output in `TotalCrossVM/tests/axtls/results/pre-migration-provider-smoke.log` before beginning Milestone 2.

### Milestone 2: fetch and link axTLS prebuilt archives

Set `TotalCrossVM/deps/totalcross-depot-tools.ref` to `axtls-2.1.5`. In `TotalCrossVM/CMakeLists.txt`, add the `axtls/cmake` module path, `AXTLS_AUTO_FETCH`, cache cleanup, `AutoFetchAxTLS.cmake`, `find_package(AxTLS REQUIRED)`, and `AxTLS::AxTLS` linkage. Remove axTLS source files and `src/axtls` include paths from the supported CMake configuration. Add `${AXTLS_LIBRARY}` to the Xcode archive dependency list.

In `TotalCrossVM/android/tcvm/build.gradle`, add the axTLS fetch script, release/repository inputs, Android output declarations, and a fetch invocation for `android/arm64-v8a`. In `.github/workflows/build.yml`, add `axtls/local` to Docker ownership repair. Fetch each published archive once with the tag's `axtls/fetch.sh`; do not commit `local/` artifacts.

### Milestone 3: replace source modifications with port hooks

Create `TotalCrossVM/third_party/axtls/TotalCrossAxTLSPort.h` and `.c`, with current copyright headers, and add the source to `TotalCrossVM/third_party/CMakeLists.txt`.

The adapter must initialize `AXTLS_PORT_HOOKS` with TotalCross allocators (`xmalloc`, `xcalloc`, `xrealloc`, `xfree`), logging, abort handling, Java socket reads, Java socket writes, and a no-op close callback. Its `user_data` holds the direct socket association and reusable Java byte array. It locks objects retained only by native state and unlocks them when the context is freed.

The read/write callbacks must call the private Java `Socket.readWriteBytes(byte[], int, int, boolean)` using `getMethod` and `executeMethod`; copy C bytes to/from the Java byte array; map normal Java EOF to axTLS EOF; and return an error without replacing a pending Java exception.

In `ssl_SSL.c`, create contexts through `ssl_ctx_new_with_port`, associate the socket before `ssl_client_new` or `ssl_server_new`, and update the adapter `Context` for context-scoped axTLS operations and disposal. Remove all hash-table lifecycle code. The direct association permits one active socket per native `SSLCTX`; reject a conflicting second association with a clear exception.

Remove `tcSocketReadWrite` and its globals/mutex. Update crypto, RAS, and OpenBSD compatibility consumers to include installed headers such as `<axtls/axtls.h>`.

### Milestone 4: remove sources and prove no regression

Delete `TotalCrossVM/src/axtls`. Do not edit the unsupported VC2008 or Android.mk paths. Configure and build the local CMake VM, build Android arm64-v8a with Gradle, then rerun `run-provider-smoke.sh` against the newly built Launcher. Save the post-migration result at `TotalCrossVM/tests/axtls/results/post-migration-provider-smoke.log` and compare all four result markers with the baseline.

## Concrete Steps

From the repository root, baseline validation uses:

    cd TotalCrossSDK
    ./gradlew-agent dist -x test
    ./gradlew-agent deployAxtlsProviderSmoke
    cd ..
    bash TotalCrossVM/tests/axtls/run-provider-smoke.sh \
      "$PWD/build/Launcher" \
      "$PWD/TotalCrossSDK/build/axtls-provider-smoke/classes/axtls/AxTLSProviderSmoke.tcz" \
      pre-migration

After the migration, use:

    bash TotalCrossVM/deps/fetch-depot-tools.sh
    bash TotalCrossVM/deps/totalcross-depot-tools/axtls/fetch.sh --platform macos --arch arm64
    cmake -S TotalCrossVM -B build/axtls -DCMAKE_BUILD_TYPE=Release -G Ninja
    cmake --build build/axtls --target tcvm Launcher --parallel
    cd TotalCrossVM/android
    ./gradlew :tcvm:fetchNativeDependencies :tcvm:externalNativeBuildRelease
    cd ../..
    bash TotalCrossVM/tests/axtls/run-provider-smoke.sh \
      "$PWD/build/axtls/Launcher" \
      "$PWD/TotalCrossSDK/build/axtls-provider-smoke/classes/axtls/AxTLSProviderSmoke.tcz" \
      post-migration

## Validation and Acceptance

The baseline and post-migration result files must each contain:

    [PASS] default TLSv1.2
    [PASS] default TLSv1.3 unsupported (FFFF8880)
    [PASS] default TLSv1.2 ECDSA
    [PASS] base TLSv1.2
    [PASS] base TLSv1.3 rejected
    [PASS] base TLSv1.2 ECDSA rejected

The supported CMake build must find `AxTLS::AxTLS` and no longer compile a source under `TotalCrossVM/src/axtls`. Android's supported Gradle build must fetch and link `axtls/local/android/arm64-v8a/lib/libaxtls.a`. `rg --files TotalCrossVM/src/axtls` and searches for `tcSocketReadWrite`, `htSSLSocket`, `heapSSLSocket`, and `htSSL` in supported VM sources must be empty. Run `git diff --check` before completion.

## Idempotence and Recovery

The smoke script uses temporary certificates, ports, and processes; it is safe to rerun. Depot fetches replace only the requested platform directory. Do not remove unrelated build directories or cached dependency artifacts. If the graphical VM cannot run on this host, keep the generated test and result/log explaining the limitation, then rerun it on a graphical supported host.

## Artifacts and Notes

Expected tracked additions include the ExecPlan, the port adapter, the Java smoke source, Gradle smoke tasks, and the smoke script. Expected removals are only `TotalCrossVM/src/axtls/**`. The ignored unsupported files `TotalCrossVM/vc2008/TCVM.vcproj` and `TotalCrossVM/src/jni/Android.mk` are intentionally not part of this change.

## Interfaces and Dependencies

The adapter relies on the pinned ABI:

    SSL_CTX *ssl_ctx_new_with_port(uint32_t options, int num_sessions,
                                   const AXTLS_PORT_HOOKS *hooks);

`AXTLS_PORT_HOOKS.user_data` is the direct lifetime-bound association between an axTLS SSL context and the TotalCross socket state. The public Java selection contract remains:

    SSLContext.getInstance("", "base").getSocketFactory();

and the regression comparison uses:

    SSLContext.getDefault().getSocketFactory();

2026-07-10 / Codex: Initial plan saved. It explicitly excludes VC2008 and Android.mk and requires a successful four-case provider baseline before the refactor.

2026-07-10 / Codex: Added the deployable provider smoke, local TLS fixture script, and macOS baseline evidence. The user then directed that the temporary platform gate in `ssl_SSL.c` be removed so the baseline can run on the local macOS target.

2026-07-10 / Codex: Reordered the smoke matrix to run the default provider first and added persisted progress markers for factory creation, TCP socket opening, handshake, encrypted write, and close. The local fixture uses `openssl s_server -rev`, avoiding `-www` response semantics while still proving handshake and encrypted write behavior.

2026-07-10 / Codex: Removed TCP readiness probes from the fixture because their immediate EOF can make `openssl s_server` stop before the smoke application connects. Readiness now verifies only that both server processes remain alive after startup.

2026-07-10 / Codex: The enabled macOS `ssl_SSL.c` baseline passed the six-case provider matrix. Default TLS 1.3 returned `FFFF8880` because the pinned mbedTLS prebuilt disables `MBEDTLS_SSL_PROTO_TLS1_3`; changing that unrelated dependency is deferred.

2026-07-10 / user and Codex: Keep mbedTLS TLS 1.3 disabled for this work. The smoke now treats `Error Code: FFFF8880` as the expected default-provider TLS 1.3 result and adds TLS 1.2 ECDSA coverage: default must succeed and base must fail. The same error code is required after the axTLS prebuilt refactor.

2026-07-10 / Codex: The post-migration CMake smoke passed the same six cases. The Android arm64-v8a Gradle build found and linked `axtls/local/android/arm64-v8a/lib/libaxtls.a`; all published Linux, Windows, Android, iOS, iOS-simulator, and macOS archives were fetched locally without adding them to version control.

2026-07-10 / Codex: Final static checks found no supported-source references to `tcSocketReadWrite`, `htSSLSocket`, `heapSSLSocket`, or `htSSL`; `TotalCrossVM/src/axtls` is absent. `git diff --check`, the final CMake build, and the final Android native build passed.
