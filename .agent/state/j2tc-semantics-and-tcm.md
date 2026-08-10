<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# J2TC semantics and TCM execution state

## Active checkpoint

- Plan-start revision: `42dec24e3353a1e3e97de5ea0288956318a55142`.
- Branch: `feature/422-create-ir-for-jniaot`.
- Active milestone: 1, float parameter correctness.
- Active subplan: `.agent/subplan-j2tc-float-and-compatibility.md`, Slices A-B.
- Last logical commit: none for this plan.
- Next action: add the data-driven `OperandReg` parameter-map matrix and a
  conversion fixture, then make `F` consume one JVM local slot while remaining
  in the TC 64-bit register bank.

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

- Completed: retained ProGuard corpus and logs are present; baseline TCZ hashes
  recorded; plan and support files satisfy the new-file limit.
- Deferred: focused float tests until Slice A implementation; SDK distribution,
  deploy smoke, and native macOS execution until Milestone 1 closure.
- Blockers: none.

## Dirty-worktree exclusions

Preserve and do not stage these unrelated untracked files:

- `.agent/sljit-depot-tools-execplan.md`
- `tcir.plist`
- `tcir_dump.plist`
- `tcir_opcode_map.plist`
- `tcir_tests.plist`
- `tcir_verify.plist`

The coordinating plan and its three subplans are user-provided inputs to this
goal and are in scope.

## Resume command

```bash
sed -n '68,194p' .agent/subplan-j2tc-float-and-compatibility.md
```
