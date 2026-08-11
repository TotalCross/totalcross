# Provenance report: `TotalCrossSDK.src.test.java.tc.tools.converter.modernjava.InheritedInvocationOwnerTest`

- Initial revision: `4672da9b98ae3196cf49d8b410b14170ef6f1877`
- Final revision: `797c0913f398c1ac81e8061828376b4d81b418a7`
- Historical source: `TotalCrossSDK/src/test/java/tc/tools/converter/modernjava/InheritedInvocationOwnerTest.java`
- Status: automated evidence; human review is required before activation.

## Final targets

| Final file | Role | Result | Source coverage | Target coverage | Header |
|---|---|---|---:|---:|---|
| `TotalCrossSDK/src/test/java/tc/tools/converter/MethodDeclarationResolverTest.java` | `primary` | `partial-inherited`/medium (transitive) | 0.0% | 0.0% | `preserved` |
| `TotalCrossSDK/src/test/java/tc/tools/converter/metadata/CompilationMetadataCaptureTest.java` | `primary` | `inherited`/high (direct) | 17.9% | 12.4% | `preserved` |

## Findings

### `TotalCrossSDK/src/test/java/tc/tools/converter/MethodDeclarationResolverTest.java`

Classification: **partial-inherited** (medium, transitive evidence; assignment `primary`).
Direct matched tokens: source 0/659 (0.0%), target 0/946 (0.0%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/test/java/tc/tools/converter/modernjava/InheritedInvocationOwnerTest.java` → `TotalCrossSDK/src/test/java/tc/tools/converter/metadata/CompilationMetadataCaptureTest.java` → `TotalCrossSDK/src/test/java/tc/tools/converter/MethodDeclarationResolverTest.java`

Intermediate files are evidence only and are not final targets:

- `TotalCrossSDK/src/test/java/tc/tools/converter/metadata/CompilationMetadataCaptureTest.java`

Weaker alternatives rejected during global target reconciliation:

- `TotalCrossSDK/src/test/java/tc/tools/converter/metadata/CompilationMetadataCaptureTest.java`: `partial-inherited`/`medium`, score `0.164314`

### `TotalCrossSDK/src/test/java/tc/tools/converter/metadata/CompilationMetadataCaptureTest.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 118/659 (17.9%), target 218/1756 (12.4%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/test/java/tc/tools/converter/modernjava/InheritedInvocationOwnerTest.java` → `TotalCrossSDK/src/test/java/tc/tools/converter/metadata/CompilationMetadataCaptureTest.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 82-89 `InheritedInvocationOwnerTest: private static TCMethod findMethod ( TCCl…` | 130-137 `CompilationMetadataCaptureTest: private static MethodMetadata method ( …` | adapted/high | 41 | 79.6% | 95.1% |
| 82-89 `InheritedInvocationOwnerTest: private static TCMethod findMethod ( TCCl…` | 139-146 `CompilationMetadataCaptureTest: private static CallSiteMetadata call ( …` | adapted/high | 37 | 71.8% | 95.1% |
| 82-89 `InheritedInvocationOwnerTest: private static TCMethod findMethod ( TCCl…` | 148-155 `CompilationMetadataCaptureTest: private static OriginRange allocation (…` | adapted/high | 34 | 66.0% | 95.1% |
| 82-89 `InheritedInvocationOwnerTest: private static TCMethod findMethod ( TCCl…` | 157-164 `CompilationMetadataCaptureTest: private static OriginRange origin ( Met…` | adapted/medium | 29 | 58.0% | 88.0% |
| 91-98 `InheritedInvocationOwnerTest: private static byte [ ] fixtureClass ( )` | 166-250 `CompilationMetadataCaptureTest: private static byte [ ] fixtureClass ( )` | copied-fragment/medium | 77 | 18.6% | 20.3% |

## Interpretation

- `inherited`: strong material lineage.
- `partial-inherited`: a material extracted or adapted portion was detected.
- `manual-review`: multiple non-generic code identifiers moved into a newly created production file, without enough textual evidence for an automatic inheritance decision.
- `manual-review` edges never support transitive inherited classifications.
- Intermediate files document the path but receive no final decision if removed.
- This is technical provenance evidence, not an independent legal opinion.
