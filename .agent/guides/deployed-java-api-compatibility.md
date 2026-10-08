<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Deployed Java API compatibility

TotalCross applications are compiled with a host JDK but deployed against the
TotalCross runtime and its Java compatibility model. Host-JVM availability is
not evidence that a Java API is supported after conversion.

## Source of truth

For deployed Java class, method, and constructor availability, the authoritative view is the device API model
resolved by `tc.tools.converter.MethodDeclarationResolver`. The converter
already uses that model from `TCMethod.checkIfAllowed()` to reject Java and
javax classes, methods, or constructors that are not available on the device. The resolver is
the source of truth for those availability checks, not the sole representation of every
runtime contract such as Java-to-native bridge metadata.

Do not treat one source directory or the presence of a `*4D` file as a complete
compatibility list. Resolution may use device-owned implementations under
`totalcross`, `jdkcompat`, and `jdkcompatx`, with or without a `4D` suffix,
and it also follows the mapped device hierarchy.

Manual source inspection is useful for diagnosis after the resolver reports a
missing member, but it should not be the normal compatibility procedure.

Any compatibility index or native-bridge inventory that can be derived from the
compiled device model must be generated from that model instead of maintained
as independent source data.

## Before using a new java.* or javax.* API

For code that can reach a deployed application:

1. Treat a successful host-JDK compile, JUnit run, or JavaSE/AWT Launcher run
   only as host-side evidence.
2. Run the focused bytecode validator before deploy when changing deployable
   SDK/runtime code:

   ```bash
   cd TotalCrossSDK
   ./gradlew-agent validateDeployedJavaApi
   ```

   The normal Gradle `check` lifecycle also runs this validator.
3. Use converter/deployer smoke validation only when the change needs evidence
   beyond API availability, such as lowering behavior or native execution.
4. If validation reports an unavailable class, method, or constructor, inspect the mapped
   implementation under `totalcross`, `jdkcompat`, or `jdkcompatx` to decide
   whether to use an existing supported API or add device support.
5. When adding device support, cover the mapping and any native bridge with
   focused converter/native tests.

Host-only code under tests, simulator/tooling, Android host integration, build
logic, or other explicitly non-deployed paths may use the host JDK when that is
the intended execution environment.

## Known audit examples

The session audit found deploy-time rework after host-side code introduced APIs
outside the deployed subset:

- `WeakReference(T, ReferenceQueue)`: the device model exposes only the
  one-argument constructor. Host compilation therefore succeeds even though the
  constructor is not deployable.
- Reflection access such as `setAccessible`: host reflection support does not
  establish support in the mapped device reflection API.
- Time APIs: `System.nanoTime()` is deployable only because the device model and
  native bridge provide it. A different `System` member must be resolved
  independently rather than inferred from the build JDK.

These examples are regression cases, not a blacklist. The resolver's device
model is authoritative.

## Validation boundary

A change that introduces a new Java or javax API into deployable code should
include one of these proofs:

- a focused converter validation showing that the device class/member resolves;
- a focused deploy smoke that exercises the converted call;
- a new device implementation with focused converter/native coverage.

The deterministic availability check runs over direct method and constructor
calls in compiled bytecode and reuses the same `MethodDeclarationResolver`
model as deploy. Bytecode that requires converter lowering, including
`invokedynamic`, still requires the focused converter/deployer validation for
that lowering path. Do not create a separately maintained compatibility list;
any human- or machine-readable API index should be generated from that device
model.

## Generated API index

For discovery or agent lookup, generate a deterministic index of host-JDK
method and constructor signatures that resolve against the deployed device
model:

```bash
cd TotalCrossSDK
./gradlew-agent generateDeployedJavaApiManifest
```

The output is `build/generated/deployed-java-api.txt`. It is derived by
enumerating the JDK 17 runtime image and asking `MethodDeclarationResolver`
about each public signature. The file is an index, not a second source of truth,
and must not be edited or maintained manually.
