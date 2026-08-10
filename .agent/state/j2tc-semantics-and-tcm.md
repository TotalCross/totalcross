<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# J2TC semantics and TCM execution state

## Active checkpoint

- Plan-start revision: `42dec24e3353a1e3e97de5ea0288956318a55142`.
- Branch: `feature/422-create-ir-for-jniaot`.
- Active milestone: 5, final integration and handoff.
- Active subplan: parent plan final gate.
- Last logical commit: `66daee561` (`fix(deploy): retain tcz artifacts for tcm`).
- Next action: audit every plan-created file for size/header compliance, run final
  diff and scoped status checks, then complete Outcomes and the editorial handoff.

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

- all files changed since the plan-start revision, excluding preserved unrelated
  untracked files and generated smoke prerequisites
- plan state/evidence/history/editorial files

## Validation state

- Completed: Milestone 4 focused tests and `dist -x test` passed. Isolated macOS
  deploys produced identical TCZ SHA-256
  `21f48888a0817eefe94cbc0e51ec4a775edcf8f6a3e20c6b9aec0b3df2be081c`;
  the reader validated the TCM manifest, and native execution passed 97 checks
  with zero failures.
- Deferred: no expensive platform matrix; TCM has no runtime consumer and the
  plan requires only the affected macOS aggregate smoke.
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
sed -n '250,390p' .agent/exec-plan-j2tc-semantics-and-tcm.md
```
