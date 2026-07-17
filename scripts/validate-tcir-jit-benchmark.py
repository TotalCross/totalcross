# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

import argparse
import csv
import json
import math
import statistics
from collections import Counter, defaultdict
from pathlib import Path


BACKENDS = ("executeMethod", "tcir", "sljit")
WORKLOADS = ("add", "abs", "sumTo")
PROFILES = {
    60: 5,
    200: 10,
    1000: 20,
}
EXPECTED_PERMUTATIONS = {
    ("executeMethod", "tcir", "sljit"),
    ("tcir", "sljit", "executeMethod"),
    ("sljit", "executeMethod", "tcir"),
    ("sljit", "tcir", "executeMethod"),
    ("tcir", "executeMethod", "sljit"),
    ("executeMethod", "sljit", "tcir"),
}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def close_enough(actual, expected):
    return math.isclose(float(actual), float(expected), rel_tol=1e-9, abs_tol=1e-3)


def validate_stats(stats, samples, invocation_count, sample_count):
    require(stats["unit"] == "nanoseconds per batch", "unexpected statistics unit")
    require(stats["lower_is_better"] is True, "execution statistics must be lower-is-better")
    require(stats["sample_count"] == sample_count, "unexpected statistics sample count")
    require(close_enough(stats["mean"], statistics.fmean(samples)), "mean mismatch")
    require(close_enough(stats["median"], statistics.median(samples)), "median mismatch")
    require(
        close_enough(stats["standard_deviation"], statistics.stdev(samples)),
        "standard deviation mismatch",
    )
    require(stats["minimum"] == min(samples), "minimum mismatch")
    require(stats["maximum"] == max(samples), "maximum mismatch")
    require(
        close_enough(
            stats["mean_nanoseconds_per_invocation"],
            statistics.fmean(samples) / invocation_count,
        ),
        "per-invocation mean mismatch",
    )


def load_csv(path):
    with path.open(newline="", encoding="utf-8") as stream:
        return list(csv.DictReader(stream))


def validate_artifacts(json_path, csv_path):
    data = json.loads(json_path.read_text(encoding="utf-8"))
    rows = load_csv(csv_path)

    require(data["schema_version"] == 1, "unsupported schema version")
    require(data["artifact_kind"] == "tcir_jit_benchmark", "unexpected artifact kind")
    require(data["benchmark_source"].endswith("tcir_jit_benchmark.c"), "benchmark source missing")
    require(data["fixture_source"].endswith("tcir_converter_fixtures.h"), "fixture source missing")
    require(data["repository_revision"] not in ("", "unknown"), "repository revision missing")
    require(data["dirty_paths"] != "", "dirty-path metadata missing")

    host = data["host"]
    for field in (
        "os",
        "kernel",
        "architecture",
        "cpu_model",
        "logical_cpu_count",
        "memory_bytes",
        "power_mode",
        "affinity_policy",
        "background_load",
    ):
        require(host[field] not in (None, "", 0), f"host metadata missing: {field}")

    build = data["build"]
    require(build["type"] == "Release", "performance artifacts must use a Release build")
    for field in ("compiler_id", "compiler_version", "c_flags", "generator", "target_processor"):
        require(build[field] not in (None, "", "unknown"), f"build metadata missing: {field}")
    require("TC_ENABLE_SLJIT_JIT=ON" in build["options"], "SLJIT option missing")
    require("TC_BUILD_IR_BENCHMARKS=ON" in build["options"], "benchmark option missing")

    protocol = data["protocol"]
    sample_count = protocol["sample_count"]
    require(sample_count in PROFILES, "sample count is not a required benchmark profile")
    require(
        protocol["warmup_count"] == PROFILES[sample_count],
        f"the {sample_count}-sample profile requires {PROFILES[sample_count]} warmups",
    )
    require(
        protocol["order_policy"]
        == "six backend permutations in round-robin order; counts differ by at most one",
        "backend order policy mismatch",
    )
    require(protocol["outlier_policy"] == "no samples excluded or filtered", "outlier policy mismatch")
    require(json_path.stem == csv_path.stem, "JSON and CSV artifact names must match")
    require(json_path.stem.endswith(f"-s{sample_count}"), "artifact name does not identify its profile")
    require(len(data["workloads"]) == len(WORKLOADS), "unexpected workload count")
    require(len(rows) == len(WORKLOADS) * sample_count * 4, "unexpected CSV row count")
    require(all(row["validated"] == "true" for row in rows), "unvalidated CSV sample")
    require(all(row["revision"] == data["repository_revision"] for row in rows), "revision mismatch")

    grouped = defaultdict(list)
    for row in rows:
        grouped[(row["workload"], row["metric"], row["backend"])].append(row)

    observed_workloads = tuple(workload["name"] for workload in data["workloads"])
    require(observed_workloads == WORKLOADS, "workload order or identity mismatch")
    for workload in data["workloads"]:
        name = workload["name"]
        invocation_count = workload["invocations_per_batch"]
        require(workload["correctness"] == {"validated": True, "oracle": "executeMethod"},
                f"correctness evidence missing for {name}")

        compile_rows = grouped[(name, "jit_compile", "sljit")]
        require(len(compile_rows) == sample_count, f"compile sample count mismatch for {name}")
        compile_rows.sort(key=lambda row: int(row["sample"]))
        compile_samples = [int(row["duration_nanoseconds"]) for row in compile_rows]
        require(
            compile_samples == workload["jit_compile"]["raw_nanoseconds"],
            f"compile raw samples differ between JSON and CSV for {name}",
        )
        require(workload["jit_compile"]["baseline"] == "not_applicable", "compile baseline mismatch")
        require(workload["jit_compile"]["code_bytes"] > 0, f"missing code size for {name}")
        validate_stats(workload["jit_compile"]["stats"], compile_samples, 1, sample_count)

        sample_orders = []
        for sample in range(1, sample_count + 1):
            ordered = []
            for backend in BACKENDS:
                execution_rows = grouped[(name, "execution", backend)]
                require(len(execution_rows) == sample_count, f"execution sample count mismatch: {name}/{backend}")
                row = next(row for row in execution_rows if int(row["sample"]) == sample)
                require(int(row["invocations"]) == invocation_count, "CSV invocation count mismatch")
                ordered.append((int(row["order_position"]), backend))
            sample_orders.append(tuple(backend for _, backend in sorted(ordered)))
        counts = Counter(sample_orders)
        require(set(counts) == EXPECTED_PERMUTATIONS, f"missing backend permutation for {name}")
        require(
            max(counts.values()) - min(counts.values()) <= 1,
            f"unbalanced backend order for {name}",
        )

        execution_samples = {}
        for backend in BACKENDS:
            execution_rows = grouped[(name, "execution", backend)]
            execution_rows.sort(key=lambda row: int(row["sample"]))
            samples = [int(row["duration_nanoseconds"]) for row in execution_rows]
            execution_samples[backend] = samples
            require(
                samples == workload["execution"][backend]["raw_nanoseconds"],
                f"execution raw samples differ between JSON and CSV for {name}/{backend}",
            )
            validate_stats(
                workload["execution"][backend]["stats"], samples, invocation_count, sample_count
            )

        baseline_mean = statistics.fmean(execution_samples["executeMethod"])
        for backend in ("tcir", "sljit"):
            candidate_mean = statistics.fmean(execution_samples[backend])
            comparison = workload["comparisons_vs_executeMethod"][backend]
            require(
                close_enough(comparison["mean_difference_nanoseconds"], candidate_mean - baseline_mean),
                f"mean difference mismatch for {name}/{backend}",
            )
            require(
                close_enough(
                    comparison["percent_change"],
                    (candidate_mean - baseline_mean) * 100.0 / baseline_mean,
                ),
                f"percent change mismatch for {name}/{backend}",
            )
            require(
                close_enough(comparison["speedup_ratio"], baseline_mean / candidate_mean),
                f"speedup mismatch for {name}/{backend}",
            )

    return data, rows


def main():
    parser = argparse.ArgumentParser(description="Validate TCIR JIT benchmark JSON and CSV artifacts.")
    parser.add_argument("--json", required=True, type=Path)
    parser.add_argument("--csv", required=True, type=Path)
    args = parser.parse_args()
    data, rows = validate_artifacts(args.json, args.csv)
    print(
        "TCIR JIT benchmark artifacts validated: "
        f"{len(data['workloads'])} workloads, {data['protocol']['sample_count']} samples, "
        f"{len(rows)} CSV rows."
    )


if __name__ == "__main__":
    main()
