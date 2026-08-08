<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->
# What ProGuard reveals about the next TotalCross compiler investment

## Editorial conclusion

The build-generated SDK corpus supports one bounded next investment: targeted
field propagation, field marking, and field removal immediately before or
inside J2TC. It does not support beginning a broad Java-aware whole-program HIR,
shipping ProGuard as a pre-pass, prioritizing reachability/tree shaking, or
redirecting the conclusion toward TCIR/JIT/AOT without runtime workload
evidence.

This conclusion comes from final TCZ structure, not optimized JAR size. The
compatible field family reduces aggregate TotalCross code slots by 3.495% and
field accesses by 4.775%. Running the same family module-locally produces a
3.397% code-slot reduction, only 0.097 percentage point less than the
whole-runtime run. The useful effect therefore does not require a new
whole-program layer on this corpus.

The result is moderate, not transformative. It justifies a bounded optimizer
slice with the ProGuard variants retained as an oracle. A representative
application corpus should be the gate before turning that slice into a general
Java optimizer architecture.

## Evidence boundary

The untouched ordinary SDK build produced these primary artifacts at revision
`aa6b2ff3ded73a845848c014ee54fae9bcfc7a77`:

| Boundary | Aggregate result |
|---|---:|
| Runtime JAR bytes | 2,932,789 |
| Final TCZ bytes | 1,577,089 |
| TC classes recovered from TCZ | 1,125 |
| TC methods | 10,703 |
| TC code slots | 229,556 |
| Actual decompressed TC class payload | 1,306,648 bytes |

The final `proguardTczExperiment` run regenerated the corpus and verified that
all four JAR and four TCZ hashes still matched the captured baseline. Generated
configs, JARs, TCZs, logs, usage data, and reports remain below the designated
`TotalCrossSDK/build/proguard-tcz-experiment/` root.

ProGuard's optimized line tables exposed a J2TC bug: a valid table may have its
first entry after bytecode PC zero, while `Bytecode2TCCode.getLineOfPC` indexes
the previous entry unconditionally. The experiment preserved the rejected
strict attempt, then removed only `LineNumberTable` from measured runtime
classes after ProGuard.

That adjustment has a material byte effect of its own. A separately generated
no-line baseline is therefore the comparison boundary for adjusted variants:

| Line-table-only change | Delta from ordinary baseline |
|---|---:|
| JAR bytes | -8.399% |
| TCZ bytes | -4.302% |
| TC class payload | -7.277% |
| TC methods | 0% |
| TC code slots | 0% |

This separation matters. Without it, a large debug-metadata reduction would be
mistaken for optimizer benefit.

## What survived J2TC

The successful full-coverage variants are:

| Variant | TCZ bytes | TC class payload | TC code slots | TC field accesses | Status |
|---|---:|---:|---:|---:|---|
| Field family, whole runtime | -1.335% | -2.865% | -3.495% | -4.775% | success, four modules |
| Field family, module-local | -1.300% | -2.792% | -3.397% | -4.671% | success, four modules |
| Field propagation | -0.430% | -1.095% | -1.435% | -1.610% | success, four modules |
| Field marking | -0.431% | -0.966% | -1.139% | -2.587% | success, four modules |
| Field removal | -0.205% | -0.485% | -0.501% | -0.449% | success, four modules |
| Enum unboxing | -0.003% | -0.010% | -0.007% | +0.022% | success, four modules |

The combined field family also reduces virtual calls by 2.635%, normal calls by
1.803%, allocations by 0.820%, and TC methods by 0.729%. No single field
subgroup explains the complete result: propagation is largest, marking is
second, removal is smaller, and the combined result is stronger than their
isolated code-slot effects. Generalization and specialization change Java JAR
structure but do not survive as TC code-slot or class-payload changes.

The per-module field-family code-slot reductions are uneven but not confined to
one artifact: `lang` improves by 12.259%, `misc` by 4.262%, `ui` by 3.608%, and
`util` by 0.996%. The whole-runtime advantage over module-local is small in all
aggregate measures. This is evidence for a targeted analysis that can operate
within current module boundaries, not for a call-graph-centered HIR.

## What did not become usable evidence

Full conservative module-local optimization, whole-runtime optimization with
one pass, and whole-runtime optimization with three passes all completed
ProGuard but failed J2TC for every module. Iterating three times did not open a
usable upper bound; it changed which forbidden constructor appeared first in
`misc` while retaining the other rejection families.

The grouped failures are informative:

- Local code/data-flow optimization converts only `lang`, where it reduces TC
  code slots by 2.934%. `misc`, `ui`, and `util` underflow J2TC's operand-stack
  model. Because ProGuard warns that optimize-without-shrink may leave
  unverifiable classes, these failures cannot all be assigned to J2TC without a
  separate JVM verifier proof.
- Method transformations convert only `ui`, where TC code slots increase by
  5.428%. The other modules fail on transformed TotalCross replacement
  constructors or generated method names such as `Reader.read$...` and
  `BiPredicate.test$...`.
- Enum unboxing is fully compatible but structurally negligible.
- Valid owner resolution can still violate TotalCross conventions. For example,
  a call through the static type `java.util.Properties` may legally name that
  subclass as the invocation owner even when `put` is inherited; J2TC rejects
  the owner. Ordinary javac can emit inherited calls with the receiver's static
  owner, so this is not exclusively an exotic optimizer shape.

These observations argue for converter-aware transformations. A TotalCross
optimizer must preserve or explicitly normalize `4D` replacement names,
device-API owner checks, native descriptors, reflection roots, and J2TC's
supported stack shapes.

## Shrinking is not the leading opportunity

The safe-shrink configuration keeps public/protected API, descriptor classes,
native methods, `4D` classes, `Storable` callbacks, serialization-sensitive
members, and repository-proven reflection entry points. After kept API is made
non-optimizable, the baseline-versus-candidate API snapshot reports no missing
entry for any module.

ProGuard's usage report identifies eight completely removed classes and 473
removed-member lines across 86 retained classes. `lang`, `util`, and `ui`
convert, but `misc` does not. One exact `DriverManager` code pin moves the first
failure to another replacement-owner case in `Settings`; the experiment stops
there rather than accumulating keep rules until conversion happens.

Across the three accepted modules, optimize-plus-shrink remains a weak result:
0.656% fewer TC code slots and 0.882% less class payload than the normalized
baseline. Compared with compatible field-only optimization on the same module
coverage, it produces 2.604% more TC code slots. The small removal set,
incomplete conversion, and negative interaction do not justify prioritizing a
reachability framework.

## Investment ranking

1. **Targeted pre-J2TC/J2TC field optimization.** Prototype only the measured
   field propagation, marking, and removal capabilities, preserve TotalCross
   conventions explicitly, and require differential TCZ results against the
   retained ProGuard oracle.
2. **Application-level evidence before broadening.** Run the same structural
   boundary on representative closed-world applications before designing a
   general optimizer, because runtime libraries are not applications and may
   understate application reachability or interprocedural opportunity.
3. **J2TC compatibility hardening where independently valuable.** Fix the line
   lookup assumption and decide whether valid inherited-owner and optimized
   stack forms should be normalized. Do not treat broad compatibility work as
   automatic authorization to ship ProGuard.
4. **TCIR/JIT/AOT remains independent.** This experiment measures no execution
   time. TCIR or native backend investment should be selected by workloads that
   exercise runtime dispatch, allocation, calls, or other measured hot paths,
   not by these byte-count results.

A broad Java-aware whole-program HIR is not next: the only complete moderate
gain is almost entirely module-local, while method/interprocedural evidence is
incompatible or negative. ProGuard is not a suitable intermediate production
solution from this evidence: the full variants fail, debug metadata needs a
post-pass, auxiliary class modeling is nontrivial, and shrinking needs fragile
pins. Reachability does not dominate. Enum unboxing has negligible leverage.

## Reproduction and limitations

Run the complete experiment with:

```bash
cd TotalCrossSDK
./gradlew-agent proguardTczExperiment --warning-mode=none --console=plain
```

The final run passed in 264 seconds. The machine-readable summary is
`TotalCrossSDK/build/proguard-tcz-experiment/aa6b2ff3ded73a845848c014ee54fae9bcfc7a77/reports/summary.json`;
the generated human summary is the adjacent `summary.md`.

The evidence is structural. TC code slots are relevant to interpreter work but
do not predict a proportional runtime speedup. Compressed bytes can magnify or
hide changes. The SDK runtime is repository-owned and shipping-relevant, but it
does not represent closed-world applications. The compatibility-normalized
variants lack line tables, so they are not production candidates and would
degrade device stack traces. No Android, iOS, Windows, Linux packaging matrix,
JIT/AOT benchmark, or production ProGuard integration was needed or run.
