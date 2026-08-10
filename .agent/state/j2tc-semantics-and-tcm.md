<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# J2TC semantics and TCM execution state

## Active checkpoint

- Plan-start revision: `42dec24e3353a1e3e97de5ea0288956318a55142`.
- Branch: `feature/422-create-ir-for-jniaot`.
- Active milestone: 2, J2TC compatibility hardening.
- Active subplan: `.agent/subplan-j2tc-float-and-compatibility.md`, Slice C.
- Last logical commit: `7c960237f` (`fix(compiler): correct float parameter slot
  mapping`).
- Next action: add a deterministic valid class fixture whose first line-table
  entry starts after PC zero, then make early bytecode use unknown-line semantics.

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

## Active paths

- `TotalCrossSDK/src/main/java/tc/tools/converter/oper/OperandReg.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/java/JavaMethod.java`
- `TotalCrossSDK/src/main/java/tc/tools/converter/J2TC.java`
- `TotalCrossSDK/src/test/java/tc/tools/converter/`
- aggregate modern-Java smoke fixture paths named by the subplan

## Validation state

- Completed: Milestone 1 focused matrix/converter suite, SDK `dist -x test`,
  aggregate deploy, and native macOS execution passed. Native output contained
  all nine float-case passes and no `[FAIL]` lines; deploy/build summaries showed
  zero obsolete float warnings.
- Deferred: the Milestone 2 compatibility sweep and final deploy/native execution
  until all compatibility categories are resolved.
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
sed -n '195,232p' .agent/subplan-j2tc-float-and-compatibility.md
```
