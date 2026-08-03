# Copyright provenance audit

- Initial revision: `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Final revision: `07b9cd3b3bf154ab7ed7498b7a51a5b9e75c3cda`
- Automatic sources are limited to files materially changed or removed in the interval.
- Only files present at the final revision are listed as targets.
- Removed intermediate files remain in lineage evidence.
- Final targets are reconciled globally so weak alternative sources do not override stronger ones.

## Results

| Historical source | Final targets | Manual review | Report |
|---|---:|---:|---|
| `TotalCrossSDK/src/main/java/tc/tools/deployer/Deployer4Android.java` | 1 | 0 | `reports/tc.tools.deployer.Deployer4Android.md` |
| `TotalCrossSDK/src/main/java/totalcross/Launcher.java` | 14 | 0 | `reports/totalcross.Launcher.md` |
| `TotalCrossSDK/src/main/java/totalcross/TCEventThread.java` | 1 | 0 | `reports/totalcross.TCEventThread.md` |

The manifest remains `pending-review` until a human confirms the results and lists it in `active-audits.json`.
