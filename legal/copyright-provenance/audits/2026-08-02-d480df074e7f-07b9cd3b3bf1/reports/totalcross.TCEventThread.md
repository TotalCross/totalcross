# Provenance report: `totalcross.TCEventThread`

- Initial revision: `d480df074e7fb6f5a32dfcc2f1f30c3949095e73`
- Final revision: `07b9cd3b3bf154ab7ed7498b7a51a5b9e75c3cda`
- Historical source: `TotalCrossSDK/src/main/java/totalcross/TCEventThread.java`
- Status: automated evidence; human review is required before activation.

## Final targets

| Final file | Role | Result | Source coverage | Target coverage | Header |
|---|---|---|---:|---:|---|
| `TotalCrossSDK/src/main/java/tc/simulator/EventLoop.java` | `primary` | `partial-inherited`/medium (direct) | 17.1% | 15.8% | `preserved` |

## Findings

### `TotalCrossSDK/src/main/java/tc/simulator/EventLoop.java`

Classification: **partial-inherited** (medium, direct evidence; assignment `primary`).
Direct matched tokens: source 126/738 (17.1%), target 161/1018 (15.8%).
Header assessment: **preserved**.

Lineage:

`TotalCrossSDK/src/main/java/totalcross/TCEventThread.java` → `TotalCrossSDK/src/main/java/tc/simulator/EventLoop.java`

| Source member | Target member | Finding | Estimated tokens | Exact | Structural |
|---|---|---|---:|---:|---:|
| 72-74 `TCEventThread: public void pushEvent ( int type , int key , int x , int…` | 89-91 `EventLoop: void postEvent ( int type , int key , int x , int y , int` | copied-fragment/medium | 38 | 86.4% | 93.2% |
| 29-41 `TCEventThread: @ Override public void run ( )` | 162-167 `EventLoop: private static void reportFailure ( Throwable t )` | copied-fragment/high | 31 | 68.1% | 70.3% |
| 191-198 `TCEventThread.TCEvent: TCEvent ( int type , int key , int x , int y , i…` | 182-194 `EventLoop.QueuedItem: private QueuedItem ( boolean shutdown , boolean c…` | copied-fragment/medium | 57 | 74.5% | 75.8% |
| 72-74 `TCEventThread: public void pushEvent ( int type , int key , int x , int…` | 196-198 `EventLoop.QueuedItem: private static QueuedItem event ( int type , int …` | copied-fragment/medium | 35 | 73.7% | 80.0% |

## Interpretation

- `inherited`: strong material lineage.
- `partial-inherited`: a material extracted or adapted portion was detected.
- `manual-review`: multiple non-generic code identifiers moved into a newly created production file, without enough textual evidence for an automatic inheritance decision.
- `manual-review` edges never support transitive inherited classifications.
- Intermediate files document the path but receive no final decision if removed.
- This is technical provenance evidence, not an independent legal opinion.
