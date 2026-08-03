# Provenance report: `tc.tools.deployer.Deployer4Android`

- Initial revision: `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Final revision: `07b9cd3b3bf154ab7ed7498b7a51a5b9e75c3cda`
- Historical source: `TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4Android.java`
- Status: automated evidence; human review is required before activation.

## Final targets

| Final file | Role | Result | Source coverage | Target coverage | Header |
|---|---|---|---:|---:|---|
| `TotalCrossSDK/src/main/java/tc/tools/deployer/AndroidPackageFiles.java` | `primary` | `inherited`/high (direct) | 8.1% | 50.7% | `preserved` |

## Findings

### `TotalCrossSDK/src/main/java/tc/tools/deployer/AndroidPackageFiles.java`

Classification: **inherited** (high, direct evidence; assignment `primary`).
Direct matched tokens: source 344/4261 (8.1%), target 344/679 (50.7%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4Android.java` → `TotalCrossSDK/src/main/java/tc/tools/deployer/AndroidPackageFiles.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 618-649 `Deployer4Android: private void insertTCFilesZip ( OutputStream z ) thro…` | 29-43 `AndroidPackageFiles: static void write ( OutputStream output , String t…` | adapted/medium | 146 | 77.2% | 90.5% |
| 651-658 `Deployer4Android: public static < E > Set < E > vector2set ( Vector vec…` | 45-51 `AndroidPackageFiles: static < E > Set < E > vector2set ( Vector vector …` | copied/high | 73 | 95.2% | 99.3% |
| 708-715 `Deployer4Android: private static void setEntryAsStored ( ZipEntry entry…` | 84-91 `AndroidPackageFiles: private static void stored ( ZipEntry entry , byte…` | copied/high | 68 | 98.5% | 100.0% |
| 717-727 `Deployer4Android: private static File getFirstReadableFile ( String ...…` | 93-101 `AndroidPackageFiles: private static File readable ( String ... paths )` | adapted/high | 57 | 89.1% | 98.4% |

## Interpretation

- `inherited`: strong material lineage.
- `partial-inherited`: a material extracted or adapted portion was detected.
- `manual-review`: multiple non-generic code identifiers moved into a newly created production file, without enough textual evidence for an automatic inheritance decision.
- `manual-review` edges never support transitive inherited classifications.
- Intermediate files document the path but receive no final decision if removed.
- This is technical provenance evidence, not an independent legal opinion.
