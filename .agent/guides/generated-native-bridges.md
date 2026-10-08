<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Generated native bridge model

Java declarations are the authoritative source for Java-to-native bridges.
`NativeBridgeModel` inspects compiled device classes and selects methods marked
`native` or `@ReplacedByNativeOnDeploy`, using the same device owner mapping
as the converter.

Run:

```bash
cd TotalCrossSDK
./gradlew-agent generateNativeBridgeModel
```

The task writes derived artifacts under
`build/generated/native-bridges/`:

- `native-bridges.json`: query/debug view of the Java/native contract;
- `NativeMethods.generated.h`: generated declarations;
- `nativeProcAddressesTC.generated.inc`: generated registrations;
- `legacy-comparison.txt`: shadow comparison with the currently versioned VM
  header and registration source.

This phase is intentionally non-authoritative for the native build. Differences
in the shadow report must be classified before the VM switches to generated
inputs. The report also exposes 32-character ABI symbol collisions without
requiring a manually maintained NativeMethods.txt inventory.
