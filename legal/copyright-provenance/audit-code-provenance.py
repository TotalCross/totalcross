#!/usr/bin/env python3
# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Audit source-code provenance across an immutable Git commit interval.

Usage:
  audit-code-provenance.py INITIAL FINAL [SOURCE_PATH]

When SOURCE_PATH is omitted, changed historical sources are discovered
automatically. The report lists only descendants present at FINAL, while
removed intermediate files remain in the lineage evidence.
"""
from __future__ import annotations

import argparse
import dataclasses
import datetime as dt
import difflib
import hashlib
import json
import os
import re
import shlex
import subprocess
import sys
from pathlib import Path
from typing import Iterable

ROOT = Path("legal/copyright-provenance")
SPDX = "SPDX-License-Identifier: LGPL-2.1-only"
DEFAULT_EXTENSIONS = (".java",)
EXCLUDED_PARTS = {".git", ".gradle", "build", "target", "node_modules", "vendor", "deps"}
COPYRIGHT_RE = re.compile(r"Copyright \(C\)\s+([0-9][0-9,\- ]*)\s+(.+?)\.?$")
KEYWORDS = set(("abstract assert boolean break byte case catch char class const continue default "
                "do double else enum extends final finally float for goto if implements import "
                "instanceof int interface long native new package private protected public return "
                "short static strictfp super switch synchronized this throw throws transient try "
                "void volatile while true false null record sealed permits non-sealed var yield").split())
OPERATORS = sorted((">>>=", "<<=", ">>=", "...", "::", "->", "++", "--", "==", "!=",
                    "<=", ">=", "&&", "||", "+=", "-=", "*=", "/=", "%=", "&=", "|=",
                    "^=", "<<", ">>>", ">>"), key=len, reverse=True)


class AuditError(RuntimeError):
    pass


@dataclasses.dataclass(frozen=True)
class Token:
    text: str
    shape: str
    line: int


@dataclasses.dataclass(frozen=True)
class Member:
    first: int
    last: int
    signature: str
    exact: tuple[str, ...]
    shape: tuple[str, ...]

    @property
    def size(self) -> int:
        return len(self.exact)


@dataclasses.dataclass(frozen=True)
class MemberMatch:
    source: Member
    target: Member
    kind: str
    confidence: str
    score: float
    exact_ratio: float
    structural_ratio: float
    exact_block: int
    structural_block: int


@dataclasses.dataclass(frozen=True)
class Similarity:
    classification: str
    confidence: str
    score: float
    source_coverage: float
    target_coverage: float
    source_tokens: int
    target_tokens: int
    matched_source_tokens: int
    matched_target_tokens: int
    matches: tuple[MemberMatch, ...]

    @property
    def material(self) -> bool:
        return self.classification in {"inherited", "partial-inherited"}


@dataclasses.dataclass(frozen=True)
class Node:
    commit: str
    path: str
    blob: str


@dataclasses.dataclass(frozen=True)
class Edge:
    source: Node
    target: Node
    reason: str
    similarity: Similarity | None


@dataclasses.dataclass(frozen=True)
class Route:
    nodes: tuple[Node, ...]
    edges: tuple[Edge, ...]

    @property
    def rank(self) -> tuple[int, float, int]:
        material = sum(1 for edge in self.edges if edge.similarity and edge.similarity.material)
        score = sum(edge.similarity.score for edge in self.edges if edge.similarity)
        return material, score, -len(self.nodes)


@dataclasses.dataclass(frozen=True)
class Changes:
    before: frozenset[str]
    after: frozenset[str]
    hints: dict[tuple[str, str], str]


class Repo:
    def __init__(self, location: Path):
        self.root = Path(self._run_at(location, "rev-parse", "--show-toplevel").strip())
        self._text: dict[tuple[str, str], str] = {}
        self._blob: dict[tuple[str, str], str] = {}
        self._paths: dict[str, frozenset[str]] = {}
        self._members: dict[tuple[str, str, int], tuple[Member, ...]] = {}
        self._changes: dict[tuple[str, str], Changes] = {}

    @staticmethod
    def _run_at(location: Path, *args: str, check: bool = True) -> str:
        process = subprocess.run(["git", "-C", str(location), *args], text=True,
                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                 encoding="utf-8", errors="replace")
        if check and process.returncode:
            raise AuditError(f"git {' '.join(args)} failed: {process.stderr.strip()}")
        return process.stdout

    def run(self, *args: str, check: bool = True) -> str:
        return self._run_at(self.root, *args, check=check)

    def resolve(self, revision: str) -> str:
        value = self.run("rev-parse", "--verify", f"{revision}^{{commit}}", check=False).strip()
        if not value:
            raise AuditError(f"cannot resolve commit: {revision}")
        return value

    def is_ancestor(self, initial: str, final: str) -> bool:
        result = subprocess.run(["git", "-C", str(self.root), "merge-base", "--is-ancestor",
                                 initial, final], stdout=subprocess.DEVNULL,
                                stderr=subprocess.DEVNULL)
        return result.returncode == 0

    def paths(self, commit: str) -> frozenset[str]:
        if commit not in self._paths:
            self._paths[commit] = frozenset(self.run("ls-tree", "-r", "--name-only", commit).splitlines())
        return self._paths[commit]

    def blob(self, commit: str, path: str) -> str:
        key = commit, path
        if key not in self._blob:
            value = self.run("rev-parse", "--verify", f"{commit}:{path}", check=False).strip()
            if not value:
                raise AuditError(f"missing {path} at {commit[:12]}")
            self._blob[key] = value
        return self._blob[key]

    def text(self, commit: str, path: str) -> str:
        key = commit, path
        if key not in self._text:
            self._text[key] = self.run("show", f"{commit}:{path}")
        return self._text[key]

    def members(self, commit: str, path: str, minimum: int) -> tuple[Member, ...]:
        key = commit, path, minimum
        if key not in self._members:
            self._members[key] = tuple(java_members(self.text(commit, path), minimum))
        return self._members[key]

    def changes(self, parent: str, child: str) -> Changes:
        key = parent, child
        if key in self._changes:
            return self._changes[key]
        output = self.run("diff", "--name-status", "-M20%", "-C20%", "--find-copies-harder",
                          "--diff-filter=ACDMRT", parent, child)
        before: set[str] = set()
        after: set[str] = set()
        hints: dict[tuple[str, str], str] = {}
        for line in output.splitlines():
            parts = line.split("\t")
            code = parts[0][0]
            if code in {"R", "C"} and len(parts) >= 3:
                old, new = parts[1], parts[2]
                before.add(old); after.add(new)
                hints[old, new] = "git-rename" if code == "R" else "git-copy"
            elif len(parts) >= 2:
                path = parts[1]
                if code in {"M", "D", "T"}: before.add(path)
                if code in {"M", "A", "T"}: after.add(path)
        self._changes[key] = Changes(frozenset(before), frozenset(after), hints)
        return self._changes[key]

    def graph(self, initial: str, final: str) -> list[tuple[str, tuple[str, ...]]]:
        lines = self.run("rev-list", "--reverse", "--topo-order", "--parents",
                         "--ancestry-path", f"{initial}..{final}").splitlines()
        known = {initial}
        graph: list[tuple[str, tuple[str, ...]]] = [(initial, ())]
        for line in lines:
            fields = line.split()
            commit = fields[0]
            parents = tuple(parent for parent in fields[1:]
                            if parent in known or self.is_ancestor(initial, parent))
            graph.append((commit, parents)); known.add(commit)
        return graph


def lex_java(text: str) -> list[Token]:
    output: list[Token] = []
    index = 0
    line = 1
    while index < len(text):
        char = text[index]
        if char.isspace():
            line += int(char == "\n"); index += 1; continue
        if text.startswith("//", index):
            end = text.find("\n", index + 2)
            if end < 0: break
            index = end; continue
        if text.startswith("/*", index):
            end = text.find("*/", index + 2)
            if end < 0: break
            line += text[index:end + 2].count("\n"); index = end + 2; continue
        token_line = line
        if char in "\"'":
            quote = char; start = index; escaped = False; index += 1
            while index < len(text):
                current = text[index]; line += int(current == "\n")
                if escaped: escaped = False
                elif current == "\\": escaped = True
                elif current == quote:
                    index += 1; break
                index += 1
            raw = text[start:index]
            shape = "STRING" if quote == '"' else "CHAR"
            digest = hashlib.sha1(raw.encode("utf-8")).hexdigest()[:10]
            output.append(Token(f"{shape}:{digest}", shape, token_line)); continue
        if char.isalpha() or char in "_$":
            end = index + 1
            while end < len(text) and (text[end].isalnum() or text[end] in "_$"): end += 1
            value = text[index:end]
            output.append(Token(value, value if value in KEYWORDS else "ID", token_line))
            index = end; continue
        if char.isdigit():
            end = index + 1
            while end < len(text) and (text[end].isalnum() or text[end] in "._"): end += 1
            output.append(Token(text[index:end], "NUMBER", token_line)); index = end; continue
        operator = next((item for item in OPERATORS if text.startswith(item, index)), None)
        value = operator or char
        output.append(Token(value, value, token_line)); index += len(value)
    return output


def member_signature(tokens: list[Token]) -> str:
    result: list[str] = []
    for token in tokens:
        if token.text in {"{", ";", "="}: break
        result.append(token.text)
        if len(result) == 16: break
    return " ".join(result) or "<initializer>"


def java_members(text: str, minimum: int) -> list[Member]:
    tokens = lex_java(text)
    class_index = next((i for i, token in enumerate(tokens)
                        if token.text in {"class", "interface", "enum", "record"}), None)
    if class_index is None: return []
    open_index = next((i for i in range(class_index, len(tokens)) if tokens[i].text == "{"), None)
    if open_index is None: return []
    output: list[Member] = []
    depth = 1
    start: int | None = None
    block = False

    def add(end: int) -> None:
        nonlocal start, block
        assert start is not None
        part = tokens[start:end + 1]
        if len(part) >= minimum:
            output.append(Member(part[0].line, part[-1].line, member_signature(part),
                                 tuple(token.text for token in part),
                                 tuple(token.shape for token in part)))
        start = None; block = False

    for index in range(open_index + 1, len(tokens)):
        token = tokens[index]
        if depth == 1 and start is None and token.text not in {";", "}"}: start = index
        if token.text == "{":
            if depth == 1 and start is not None: block = True
            depth += 1
        elif token.text == "}":
            depth -= 1
            if depth == 1 and start is not None and block: add(index)
            elif depth == 0: break
        elif token.text == ";" and depth == 1 and start is not None:
            add(index)
    return output


def seq_similarity(left: tuple[str, ...], right: tuple[str, ...]) -> tuple[float, int]:
    matcher = difflib.SequenceMatcher(None, left, right, autojunk=False)
    return matcher.ratio(), matcher.find_longest_match().size


def compare_member(source: Member, target: Member, minimum: int) -> MemberMatch | None:
    exact, exact_block = seq_similarity(source.exact, target.exact)
    structural, structural_block = seq_similarity(source.shape, target.shape)
    length = min(source.size, target.size) / max(source.size, target.size)
    if exact >= .93 and length >= .45 and exact_block >= minimum:
        kind, confidence = "copied", "high"
    elif exact_block >= max(minimum, int(target.size * .42)):
        kind = "copied-fragment"
        confidence = "high" if exact_block >= target.size * .68 else "medium"
    elif structural >= .88 and length >= .38 and exact >= .18 and structural_block >= minimum:
        kind, confidence = "adapted", "high" if structural >= .95 else "medium"
    elif structural_block >= max(minimum + 4, int(target.size * .50)) and exact >= .12:
        kind, confidence = "adapted-fragment", "medium"
    else:
        return None
    score = (exact * .42 + structural * .35 +
             min(1.0, exact_block / max(1, target.size)) * .13 +
             min(1.0, structural_block / max(1, target.size)) * .10)
    return MemberMatch(source, target, kind, confidence, score, exact, structural,
                       exact_block, structural_block)


def compare_files(source_members: Iterable[Member], target_members: Iterable[Member],
                  minimum: int) -> Similarity:
    sources = tuple(source_members); targets = tuple(target_members)
    source_total = sum(item.size for item in sources)
    target_total = sum(item.size for item in targets)
    matches: list[MemberMatch] = []
    used_sources: set[int] = set()
    for target in targets:
        choices = [match for source in sources
                   if (match := compare_member(source, target, minimum)) is not None]
        if not choices: continue
        best = max(choices, key=lambda item: (item.confidence == "high", item.score,
                                              item.exact_block, item.structural_block))
        matches.append(best); used_sources.add(id(best.source))
    matched_target = sum(item.target.size for item in matches)
    matched_source = sum(item.size for item in sources if id(item) in used_sources)
    source_coverage = matched_source / source_total if source_total else 0.0
    target_coverage = matched_target / target_total if target_total else 0.0
    high = sum(item.confidence == "high" for item in matches)
    longest = max((max(item.exact_block, item.structural_block) for item in matches), default=0)
    strong_min = max(36, minimum * 2)
    if matches and ((target_coverage >= .55 and matched_target >= strong_min) or
                    (source_coverage >= .55 and matched_source >= strong_min) or
                    (high >= 2 and matched_target >= minimum * 3)):
        classification, confidence = "inherited", "high"
    elif matches and ((target_coverage >= .16 and matched_target >= strong_min) or
                      (source_coverage >= .16 and matched_source >= strong_min) or
                      (high >= 1 and longest >= max(30, minimum))):
        classification = "partial-inherited"
        confidence = "high" if high and target_coverage >= .35 else "medium"
    elif matches:
        classification, confidence = "manual-review", "low"
    else:
        classification, confidence = "no-material-lineage", "none"
    average = sum(item.score for item in matches) / len(matches) if matches else 0.0
    score = target_coverage * .45 + source_coverage * .25 + average * .30
    return Similarity(classification, confidence, score, source_coverage, target_coverage,
                      source_total, target_total, matched_source, matched_target, tuple(matches))


def code_fingerprint(text: str) -> str:
    payload = "\0".join(token.text for token in lex_java(text))
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def notices(text: str) -> tuple[tuple[str, str], ...]:
    output: list[tuple[str, str]] = []
    for line in text.splitlines()[:100]:
        clean = line.strip().lstrip("/*# ").rstrip("*/ ")
        match = COPYRIGHT_RE.search(clean)
        if match: output.append((" ".join(match.group(1).split()), match.group(2).rstrip(".")))
        elif output and SPDX in line: break
    return tuple(output)


def year_set(value: str) -> set[int]:
    output: set[int] = set()
    for piece in value.split(","):
        piece = piece.strip()
        if not piece: continue
        if "-" in piece:
            start, end = (int(item.strip()) for item in piece.split("-", 1))
            output.update(range(start, end + 1))
        else:
            output.add(int(piece))
    return output


def assess_header(source_text: str, target_text: str) -> dict[str, object]:
    source = notices(source_text); target = notices(target_text)
    target_years: dict[str, set[int]] = {}
    for years, owner in target:
        target_years.setdefault(owner, set()).update(year_set(years))
    missing = [{"years": years, "owner": owner} for years, owner in source
               if not year_set(years).issubset(target_years.get(owner, set()))]
    spdx = SPDX in "\n".join(target_text.splitlines()[:100])
    if not source: status = "source-header-unavailable"
    elif not missing and spdx: status = "preserved"
    elif target and spdx: status = "incomplete"
    else: status = "missing"
    return {"status": status,
            "sourceNotices": [{"years": years, "owner": owner} for years, owner in source],
            "targetNotices": [{"years": years, "owner": owner} for years, owner in target],
            "missingNotices": missing, "spdx": spdx}


def allowed(path: str, extensions: tuple[str, ...]) -> bool:
    item = Path(path)
    return item.suffix in extensions and not any(part in EXCLUDED_PARTS for part in item.parts)


def candidates(repo: Repo, initial: str, final: str,
               extensions: tuple[str, ...]) -> tuple[list[str], list[str]]:
    changes = repo.changes(initial, final)
    initial_paths = repo.paths(initial); final_paths = repo.paths(final)
    sources = sorted(path for path in changes.before if path in initial_paths and allowed(path, extensions))
    targets = sorted(path for path in changes.after if path in final_paths and allowed(path, extensions))
    return sources, targets


def make_node(repo: Repo, commit: str, path: str) -> Node:
    return Node(commit, path, repo.blob(commit, path))


def extend(route: Route, target: Node, reason: str, similarity: Similarity | None) -> Route:
    edge = Edge(route.nodes[-1], target, reason, similarity)
    return Route(route.nodes + (target,), route.edges + (edge,))


def choose(current: Route | None, candidate: Route) -> Route:
    return candidate if current is None or candidate.rank > current.rank else current


def effective(direct: Similarity, route: Route) -> dict[str, str]:
    if direct.material:
        return {"classification": direct.classification, "confidence": direct.confidence,
                "basis": "direct"}
    transfers = [edge for edge in route.edges if edge.source.path != edge.target.path]
    supported = [edge for edge in transfers if edge.similarity and
                 edge.similarity.classification != "no-material-lineage"]
    if transfers and len(supported) == len(transfers):
        all_strong = all(edge.similarity and edge.similarity.classification == "inherited"
                         and edge.similarity.confidence == "high" for edge in supported)
        return {"classification": "inherited" if all_strong else "partial-inherited",
                "confidence": "high" if all_strong else "medium", "basis": "transitive"}
    return {"classification": direct.classification, "confidence": direct.confidence,
            "basis": "direct"}


def trace(repo: Repo, graph: list[tuple[str, tuple[str, ...]]], initial: str, final: str,
          source_path: str, final_candidates: set[str], minimum: int,
          extensions: tuple[str, ...]) -> dict[str, object]:
    source = make_node(repo, initial, source_path)
    states: dict[str, dict[str, Route]] = {initial: {source_path: Route((source,), ())}}
    for commit, parents in graph[1:]:
        combined: dict[str, Route] = {}
        child_paths = repo.paths(commit)
        for parent in parents:
            parent_states = states.get(parent, {})
            if not parent_states: continue
            changed = repo.changes(parent, commit)
            destinations = [path for path in changed.after if path in child_paths and allowed(path, extensions)]
            for active_path, route in parent_states.items():
                if active_path in child_paths and active_path not in changed.before:
                    combined[active_path] = choose(combined.get(active_path), route)
                if active_path in child_paths and active_path in changed.after:
                    sim = compare_files(repo.members(parent, active_path, minimum),
                                        repo.members(commit, active_path, minimum), minimum)
                    continued = extend(route, make_node(repo, commit, active_path), "same-path", sim)
                    combined[active_path] = choose(combined.get(active_path), continued)
                for destination in destinations:
                    if destination == active_path: continue
                    hint = changed.hints.get((active_path, destination))
                    sim = compare_files(repo.members(parent, active_path, minimum),
                                        repo.members(commit, destination, minimum), minimum)
                    same_blob = repo.blob(parent, active_path) == repo.blob(commit, destination)
                    if hint and not same_blob and sim.classification == "no-material-lineage": continue
                    if not hint and not sim.material: continue
                    moved = extend(route, make_node(repo, commit, destination),
                                   hint or "similarity", sim)
                    combined[destination] = choose(combined.get(destination), moved)
        states[commit] = combined

    results: dict[str, dict[str, object]] = {}
    final_paths = repo.paths(final)
    for path, route in states.get(final, {}).items():
        if path == source_path or path not in final_paths or path not in final_candidates: continue
        if route.nodes[-1].commit != final:
            route = extend(route, make_node(repo, final, path), "unchanged-to-final", None)
        if not any(edge.source.path != edge.target.path for edge in route.edges): continue
        direct = compare_files(repo.members(initial, source_path, minimum),
                               repo.members(final, path, minimum), minimum)
        if effective(direct, route)["classification"] not in {"inherited", "partial-inherited"}:
            continue
        results[path] = {"route": route, "direct": direct}

    for path in sorted(final_candidates):
        if path == source_path or path not in final_paths or path in results: continue
        direct = compare_files(repo.members(initial, source_path, minimum),
                               repo.members(final, path, minimum), minimum)
        if not direct.material: continue
        target = make_node(repo, final, path)
        results[path] = {"route": Route((source, target),
                                         (Edge(source, target, "direct-similarity", direct),)),
                         "direct": direct}
    return {"source": source, "targets": results}


def similarity_json(value: Similarity | None) -> dict[str, object] | None:
    if value is None: return None
    return {"classification": value.classification, "confidence": value.confidence,
            "score": round(value.score, 6),
            "sourceCoverage": round(value.source_coverage, 6),
            "targetCoverage": round(value.target_coverage, 6),
            "sourceTokens": value.source_tokens, "targetTokens": value.target_tokens,
            "matchedSourceTokens": value.matched_source_tokens,
            "matchedTargetTokens": value.matched_target_tokens,
            "matches": [{"kind": item.kind, "confidence": item.confidence,
                         "score": round(item.score, 6),
                         "exactRatio": round(item.exact_ratio, 6),
                         "structuralRatio": round(item.structural_ratio, 6),
                         "longestExactBlock": item.exact_block,
                         "longestStructuralBlock": item.structural_block,
                         "source": {"first": item.source.first, "last": item.source.last,
                                    "signature": item.source.signature, "tokens": item.source.size},
                         "target": {"first": item.target.first, "last": item.target.last,
                                    "signature": item.target.signature, "tokens": item.target.size}}
                        for item in value.matches]}


def route_json(route: Route) -> dict[str, object]:
    return {"nodes": [dataclasses.asdict(item) for item in route.nodes],
            "edges": [{"source": dataclasses.asdict(edge.source),
                       "target": dataclasses.asdict(edge.target), "reason": edge.reason,
                       "similarity": similarity_json(edge.similarity)} for edge in route.edges]}


def java_name(path: str) -> str:
    normalized = path.replace("\\", "/")
    marker = "src/main/java/"
    relative = normalized.split(marker, 1)[1] if marker in normalized else normalized
    return relative[:-5].replace("/", ".") if relative.endswith(".java") else relative.replace("/", ".")


def slug(path: str) -> str:
    return re.sub(r"[^A-Za-z0-9_.-]+", "-", java_name(path)).strip(".-") or "source"


def compact(value: str, limit: int = 58) -> str:
    value = " ".join(value.split())
    return value if len(value) <= limit else value[:limit - 1] + "…"


def source_report(source_path: str, initial: str, final: str,
                  targets: dict[str, dict[str, object]], source_text: str) -> str:
    output = [f"# Provenance report: `{java_name(source_path)}`", "",
              f"- Initial revision: `{initial}`", f"- Final revision: `{final}`",
              f"- Historical source: `{source_path}`",
              "- Status: automated evidence; human review is required before activation.", "",
              "## Final targets", "", "| Final file | Result | Source coverage | Target coverage | Header |",
              "|---|---|---:|---:|---|"]
    for path, value in sorted(targets.items()):
        direct: Similarity = value["direct"]  # type: ignore[assignment]
        route: Route = value["route"]  # type: ignore[assignment]
        result = effective(direct, route)
        header = assess_header(source_text, value["targetText"])  # type: ignore[arg-type]
        output.append(f"| `{path}` | `{result['classification']}`/{result['confidence']} "
                      f"({result['basis']}) | {direct.source_coverage:.1%} | "
                      f"{direct.target_coverage:.1%} | `{header['status']}` |")
    output += ["", "## Findings", ""]
    for path, value in sorted(targets.items()):
        direct: Similarity = value["direct"]  # type: ignore[assignment]
        route: Route = value["route"]  # type: ignore[assignment]
        result = effective(direct, route)
        header = assess_header(source_text, value["targetText"])  # type: ignore[arg-type]
        lineage = [route.nodes[0].path]
        for item in route.nodes[1:]:
            if item.path != lineage[-1]: lineage.append(item.path)
        output += [f"### `{path}`", "",
                   f"Classification: **{result['classification']}** "
                   f"({result['confidence']}, {result['basis']} evidence).",
                   f"Direct coverage: source {direct.source_coverage:.1%}, "
                   f"target {direct.target_coverage:.1%}.",
                   f"Header assessment: **{header['status']}**.", "", "Lineage:", "",
                   " → ".join(f"`{item}`" for item in lineage), ""]
        if len(lineage) > 2:
            output += ["Intermediate files are evidence only and are not final targets:", "",
                       *[f"- `{item}`" for item in lineage[1:-1]], ""]
        if direct.matches:
            output += ["| Source member | Target member | Finding | Exact | Structural |",
                       "|---|---|---|---:|---:|"]
            for item in direct.matches:
                output.append(f"| {item.source.first}-{item.source.last} "
                              f"`{compact(item.source.signature)}` | "
                              f"{item.target.first}-{item.target.last} "
                              f"`{compact(item.target.signature)}` | "
                              f"{item.kind}/{item.confidence} | {item.exact_ratio:.1%} | "
                              f"{item.structural_ratio:.1%} |")
            output.append("")
    output += ["## Interpretation", "",
               "- `inherited`: strong material lineage.",
               "- `partial-inherited`: a material extracted or adapted portion was detected.",
               "- Intermediate files document the path but receive no final decision if removed.",
               "- This is technical provenance evidence, not an independent legal opinion.", ""]
    return "\n".join(output)


def summary(initial: str, final: str, results: list[dict[str, object]]) -> str:
    output = ["# Copyright provenance audit", "", f"- Initial revision: `{initial}`",
              f"- Final revision: `{final}`",
              "- Only files present at the final revision are listed as targets.",
              "- Removed intermediate files remain in the lineage evidence.", "",
              "## Results", "", "| Historical source | Final targets | Report |",
              "|---|---:|---|"]
    for item in results:
        output.append(f"| `{item['sourcePath']}` | {len(item['finalTargets'])} | "
                      f"`{item['report']}` |")
    output += ["", "The manifest remains `pending-review` until a human confirms the results "
                    "and lists it in `active-audits.json`.", ""]
    return "\n".join(output)


def parse_args(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("initial_commit")
    parser.add_argument("final_commit")
    parser.add_argument("source_path", nargs="?")
    parser.add_argument("--repo", type=Path, default=Path("."))
    parser.add_argument("--extensions", default=",".join(DEFAULT_EXTENSIONS))
    parser.add_argument("--min-tokens", type=int, default=18)
    parser.add_argument("--output", type=Path)
    parser.add_argument("--audit-id")
    parser.add_argument("--overwrite", action="store_true")
    parser.add_argument("--fail-on-missing-notice", action="store_true")
    parser.add_argument("--max-auto-sources", type=int, default=100)
    return parser.parse_args(argv)


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.rstrip() + "\n", encoding="utf-8")


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv or sys.argv[1:])
    repo = Repo(args.repo)
    initial = repo.resolve(args.initial_commit); final = repo.resolve(args.final_commit)
    if initial == final: raise AuditError("initial and final commits must differ")
    if not repo.is_ancestor(initial, final):
        raise AuditError("initial commit must be an ancestor of final commit")
    extensions = tuple(item.strip() if item.strip().startswith(".") else f".{item.strip()}"
                       for item in args.extensions.split(",") if item.strip())
    auto_sources, auto_targets = candidates(repo, initial, final, extensions)
    if args.source_path:
        source_path = args.source_path.replace(os.sep, "/")
        if source_path not in repo.paths(initial):
            raise AuditError(f"source path does not exist at initial commit: {source_path}")
        if not allowed(source_path, extensions):
            raise AuditError(f"unsupported or excluded source path: {source_path}")
        sources = [source_path]
    else:
        sources = auto_sources
        if len(sources) > args.max_auto_sources:
            raise AuditError(f"automatic mode found {len(sources)} sources; narrow the interval, "
                             "provide SOURCE_PATH, or raise --max-auto-sources")
    if not sources: raise AuditError("no source candidates found")
    graph = repo.graph(initial, final)
    final_candidates = set(auto_targets)
    manifest_results: list[dict[str, object]] = []
    details: list[tuple[str, str, dict[str, dict[str, object]], str, dict[str, object]]] = []

    for index, source_path in enumerate(sources, 1):
        print(f"[{index}/{len(sources)}] tracing {source_path}", file=sys.stderr)
        traced = trace(repo, graph, initial, final, source_path, final_candidates,
                       args.min_tokens, extensions)
        targets: dict[str, dict[str, object]] = traced["targets"]  # type: ignore[assignment]
        if not targets: continue
        source_text = repo.text(initial, source_path)
        for path, value in targets.items(): value["targetText"] = repo.text(final, path)
        name = slug(source_path)
        evidence_rel = Path("evidence") / f"{name}.json"
        report_rel = Path("reports") / f"{name}.md"
        final_entries = []
        evidence_targets = []
        for path, value in sorted(targets.items()):
            direct: Similarity = value["direct"]  # type: ignore[assignment]
            route: Route = value["route"]  # type: ignore[assignment]
            result = effective(direct, route)
            target_text: str = value["targetText"]  # type: ignore[assignment]
            header = assess_header(source_text, target_text)
            final_entries.append({"path": path, "blob": repo.blob(final, path),
                                  "codeFingerprint": code_fingerprint(target_text),
                                  "classification": result["classification"],
                                  "confidence": result["confidence"],
                                  "evidenceBasis": result["basis"],
                                  "headerStatus": header["status"]})
            evidence_targets.append({"path": path, "blob": repo.blob(final, path),
                                     "codeFingerprint": code_fingerprint(target_text),
                                     "effectiveEvidence": result,
                                     "directEvidence": similarity_json(direct),
                                     "header": header, "lineage": route_json(route)})
        manifest_results.append({"sourcePath": source_path,
                                 "sourceBlob": repo.blob(initial, source_path),
                                 "sourceCodeFingerprint": code_fingerprint(source_text),
                                 "evidence": str(evidence_rel), "report": str(report_rel),
                                 "finalTargets": final_entries})
        evidence = {"schemaVersion": 2, "source": dataclasses.asdict(traced["source"]),
                    "sourceCodeFingerprint": code_fingerprint(source_text),
                    "sourceHeader": assess_header(source_text, source_text),
                    "targets": evidence_targets}
        details.append((source_path, name, targets, source_text, evidence))

    if not manifest_results:
        print("No material final-file lineage was detected.")
        return 0

    now = dt.datetime.now().astimezone()
    audit_id = args.audit_id or f"{now.date().isoformat()}-{initial[:12]}-{final[:12]}"
    output = args.output or (repo.root / ROOT / "audits" / audit_id)
    if not output.is_absolute(): output = repo.root / output
    if output.exists() and any(output.iterdir()) and not args.overwrite:
        raise AuditError(f"output is not empty: {output}; use --overwrite")
    output.mkdir(parents=True, exist_ok=True)
    script = Path(__file__).resolve()
    script_hash = hashlib.sha256(script.read_bytes()).hexdigest()
    command = " ".join(shlex.quote(item) for item in [sys.executable, *sys.argv])
    try: tool_path = str(script.relative_to(repo.root))
    except ValueError: tool_path = str(script)
    manifest = {"schemaVersion": 2, "auditId": audit_id,
                "reviewStatus": "pending-review", "createdAt": now.isoformat(),
                "initialRevision": initial, "finalRevision": final,
                "mode": "single-source" if args.source_path else "automatic",
                "requestedSourcePath": args.source_path,
                "tool": {"path": tool_path, "sha256": script_hash, "command": command},
                "results": manifest_results, "supersedes": []}
    write(output / "manifest.json", json.dumps(manifest, indent=2, ensure_ascii=False))
    write(output / "summary.md", summary(initial, final, manifest_results))
    for source_path, name, targets, source_text, evidence in details:
        write(output / "evidence" / f"{name}.json",
              json.dumps(evidence, indent=2, ensure_ascii=False))
        write(output / "reports" / f"{name}.md",
              source_report(source_path, initial, final, targets, source_text))
    print(output)
    missing = [target["path"] for result in manifest_results for target in result["finalTargets"]
               if target["classification"] in {"inherited", "partial-inherited"}
               and target["headerStatus"] != "preserved"]
    if args.fail_on_missing_notice and missing:
        print("Missing or incomplete inherited notice: " + ", ".join(missing), file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (AuditError, OSError, ValueError) as error:
        print(f"error: {error}", file=sys.stderr)
        raise SystemExit(1)
