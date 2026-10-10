<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Generated native bridges

Compiled Java declarations, after device deploy selection, define the Java to
native bridge ABI. `NativeBridgeModel` uses the converter's shared replacement
rules for deployed classes and methods. It collects methods marked `native` or
`@ReplacedByNativeOnDeploy`, drops Java implementations replaced during deploy,
and deduplicates the resulting owner, method, and descriptor identities.

Keep a JavaSE implementation inside the method marked
`@ReplacedByNativeOnDeploy` when that method has a device-native replacement.
The converter drops the Java body from device output, while bridge discovery
derives the native symbol from the same declaration. If one behavior works on
both runtimes, use an ordinary Java implementation and do not create a native
bridge for it.

In the PR #502 image path, `Image.createJpg` materializes its canonical backing
and calls the replacement method `createJpgImpl`; the generated symbol is
`tuiI_createJpgImpl_si`, implemented in `TotalCrossVM/src/nm/ui/image_Image.c`.
The old `tuiI_createJpgNative_si` name has no compatibility alias. The JavaSE
ImageIO implementation lives inside replacement method
`decodeEncodedSourceDirect`, while its device implementation keeps the existing
`tuiI_decodeEncodedSourceDirect_e` symbol. `Class4D.getSigners()` is ordinary
Java code returning `null`, so it creates no bridge. These methods do not need
`@JavaSEOnly`; the annotation and its converter-specific support have been
removed.

Generate the native build inputs with:

```bash
cd TotalCrossSDK
./gradlew-agent generateNativeBridgeSources
```

The task writes derived files under
`TotalCrossSDK/build/generated/native-bridges/`:

- `native-bridges.json` records every bridge and its owning native module;
- `NativeMethods.generated.h` declares core VM bridges;
- `nativeProcAddressesTC.generated.inc` registers core VM bridges.

By default, entries belong to the `core` module. An owner prefix in
`TotalCrossVM/src/nm/native-bridge-compat.txt` can assign an optional module.
The current `sync` prefix classifies 19 Sync DLL exports in the manifest while
keeping them out of the core VM header and address table. The Sync project
retains its own exports. Add an optional module only when a separate native
artifact builds and owns those symbols.

Strict generation scans `TotalCrossVM/src` for concrete `TC_API` or `SYNC_API`
function definitions for every generated or compatibility bridge. A prototype
or a symbol in a comment is insufficient. This source check catches missing
implementations early; it does not process platform macros or prove that a
source is included in every platform build. The native link is the platform
check for missing or unresolved symbols. Conditional bridge registrations
remain listed with their matching `guard` macro in the compatibility file.

Normal Gradle `check` and SDK `dist` run strict generation. Native builds
consume the generated files through the stable wrapper files and fail clearly
when the generated inputs are missing. CI generates the files once in a shared
job and supplies that artifact to parallel native jobs, so each native job does
not need to build the full SDK package first.

## Adding a native method

1. Add the declaration to the Java SDK and mark it `native` or
   `@ReplacedByNativeOnDeploy` according to how JavaSE behaves.
2. Add a concrete `TC_API` C or `SYNC_API` C++ definition using the ABI symbol
   derived from the deployed owner, method, and descriptor. Check the generated
   manifest when an overload, replacement class, or long signature is involved.
3. Run bridge generation and the focused converter tests, then build the native
   target for each affected platform. Strict generation checks source presence;
   successful linking checks that the platform target includes the definition.
4. If a shipped ABI spelling differs from the derived symbol, add a documented
   `override` entry and keep the historical spelling. Do not rename an exported
   symbol just to match the generator.
5. Use `bridge` only for a concrete VM or legacy entry point absent from the
   current Java model, `header` for a declaration-only helper, `guard` for a
   compile-time optional registration, and `module` for a separate native
   artifact. These entries are compatibility facts, not a second Java method
   inventory.

`TotalCrossVM/src/nm/NativeMethods.h` and
`TotalCrossVM/src/init/nativeProcAddressesTC.c` remain thin stable wrappers.
Their bridge contents are generated and must not be maintained by hand.
Generated outputs live under `build/` and should not be committed.
