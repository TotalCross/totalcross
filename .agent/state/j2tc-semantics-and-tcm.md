<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# J2TC semantics and TCM execution state

## Active checkpoint

- Plan-start revision: `42dec24e3353a1e3e97de5ea0288956318a55142`.
- Branch: `feature/422-create-ir-for-jniaot`.
- Active milestone: 4, deterministic TCM v1 format and emission.
- Active subplan: `.agent/subplan-tcm-format-and-emission.md`.
- Last logical commit: `0507ad84d` (`feat(compiler): collect lowering semantic
  metadata`).
- Next action: define the fixed v1 section constants and deterministic string
  table, then implement writer/reader round-trip tests before deploy integration.

## Milestone 0 baseline

- The current `OperandReg.init` groups `F`, `J`, and `D` in `hash64` and advances
  `paramIdx` by two for all three. The focused failing invariant is therefore
  `F -> Java width 2`; the required invariant is `F -> Java width 1`.
- Sparse line-table evidence is retained in the predecessor corpus: strict
  module-local optimization fails in `Bytecode2TCCode.getLineOfPC` with index
  `-1` before the first line entry.
- The inherited-owner case is independently JVM-valid: javac may name a static
  receiver subclass such as `java/util/Properties` while the selected method is
  declared by a superclass. A deterministic generated/javac fixture will become
  the regression test in Milestone 2.
- Remaining predecessor families and their stable examples are operand-stack
  underflow (`InputStreamReader.ensureFetch`, `Button.onPaint`), replacement
  constructors (`Throwable(String,Throwable,byte)`,
  `ByteArrayInputStream(byte[],int)`), and generated names
  (`BiPredicate.test$2838e5b1`). JVM verification is deferred to Milestone 2.
- The byte-identity baseline is the four TCZ files under
  `TotalCrossSDK/build/proguard-tcz-experiment/aa6b2ff3ded73a845848c014ee54fae9bcfc7a77/baseline/o`;
  hashes are recorded in the evidence index.

## Milestone 2 result

- Fixed sparse pre-first-entry line lookup, inherited declaration-owner
  validation, and valid handler-entry stacks whose first opcode is not `astore`.
- ASM 9.6 verified representative optimized UI, language, and utility classes.
  Replacement constructor descriptors and generated method names are valid JVM
  bytecode but remain unsupported because they violate canonical TotalCross 4D
  replacement contracts.
- Current converter rechecks pass both strict and optimized TCUI inputs. The
  remaining language, utility, and misc failures are those classified contracts.

## Active paths

- `TotalCrossSDK/src/main/java/tc/tools/converter/metadata/`
- `TotalCrossSDK/src/main/java/tc/Deploy.java`
- `TotalCrossSDK/src/main/java/tc/tools/deployer/DeploySettings.java`
- TCM-focused tests under `TotalCrossSDK/src/test/java/tc/tools/converter/metadata/`
- `docs/architecture/bytecode/tcm-compilation-metadata.md`

## Validation state

- Completed: Milestone 3 focused metadata and complete modern-Java converter
  sweep passed in 8 seconds. StackMap fixtures are JVM-verified and cover compact,
  chop, append, full, object, uninitialized, float, double, and long forms.
- Deferred: distribution, deploy, TCZ identity, and native validation until the
  external sidecar contract is implemented at Milestone 4 closure.
- Blockers: none.

## Dirty-worktree exclusions

Preserve and do not stage these unrelated untracked files:

- `.agent/sljit-depot-tools-execplan.md`
- `tcir.plist`
- `tcir_dump.plist`
- `tcir_opcode_map.plist`
- `tcir_tests.plist`
- `tcir_verify.plist`

Generated local smoke prerequisite, never stage:

- `TotalCrossSDK/etc/launchers/macos/Launcher`

The coordinating plan and its three subplans are user-provided inputs to this
goal and are in scope.

## Resume command

```bash
sed -n '20,230p' .agent/subplan-tcm-format-and-emission.md
```
