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
class ReviewSignal:
    kind: str
    score: float
    shared_identifiers: tuple[str, ...]
    shared_components: tuple[str, ...]
    source_term_coverage: float
    target_term_coverage: float
    source_fragment_lines: int
    target_fragment_lines: int

    @property
    def shared_terms(self) -> tuple[str, ...]:
        return tuple(sorted(set(self.shared_identifiers) | set(self.shared_components)))


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

    @property
    def supports_lineage(self) -> bool:
        return self.reason in {"git-rename-exact", "git-copy-exact"} or bool(
            self.similarity and self.similarity.material
        )


@dataclasses.dataclass(frozen=True)
class Route:
    nodes: tuple[Node, ...]
    edges: tuple[Edge, ...]

    @property
    def rank(self) -> tuple[int, int, float, int]:
        transfers = [edge for edge in self.edges if edge.source.path != edge.target.path]
        supported = sum(edge.supports_lineage for edge in transfers)
        material = sum(1 for edge in transfers if edge.similarity and edge.similarity.material)
        score = sum(edge.similarity.score for edge in transfers if edge.similarity)
        return supported, material, score, -len(self.nodes)


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
        self._endpoint_changes: dict[tuple[str, str], Changes] = {}
        self._diff_fragments: dict[tuple[str, str, str, str], str] = {}

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

    @staticmethod
    def _parse_changes(output: str) -> Changes:
        before: set[str] = set()
        after: set[str] = set()
        hints: dict[tuple[str, str], str] = {}
        for line in output.splitlines():
            parts = line.split("\t")
            if not parts:
                continue
            status = parts[0]
            code = status[0]
            if code in {"R", "C"} and len(parts) >= 3:
                old, new = parts[1], parts[2]
                before.add(old)
                after.add(new)
                similarity = status[1:] or "unknown"
                hints[old, new] = ("git-rename" if code == "R" else "git-copy") + f":{similarity}"
            elif len(parts) >= 2:
                path = parts[1]
                if code in {"M", "D", "T"}:
                    before.add(path)
                if code in {"M", "A", "T"}:
                    after.add(path)
        return Changes(frozenset(before), frozenset(after), hints)

    def changes(self, parent: str, child: str) -> Changes:
        """Aggressive transition analysis used only while tracing lineage."""
        key = parent, child
        if key not in self._changes:
            output = self.run(
                "diff", "--name-status", "-M20%", "-C20%", "--find-copies-harder",
                "--diff-filter=ACDMRT", parent, child,
            )
            self._changes[key] = self._parse_changes(output)
        return self._changes[key]

    def endpoint_changes(self, initial: str, final: str) -> Changes:
        """Conservative endpoint discovery: never seeds audits from unchanged copy sources."""
        key = initial, final
        if key not in self._endpoint_changes:
            output = self.run(
                "diff", "--name-status", "-M50%", "--diff-filter=ADMRT", initial, final,
            )
            self._endpoint_changes[key] = self._parse_changes(output)
        return self._endpoint_changes[key]

    def diff_fragment(self, initial: str, final: str, path: str, sign: str) -> str:
        """Return removed ('-') or added ('+') lines for one path over the full interval."""
        if sign not in {"-", "+"}:
            raise ValueError("diff fragment sign must be '-' or '+'")
        key = initial, final, path, sign
        if key in self._diff_fragments:
            return self._diff_fragments[key]
        output = self.run(
            "diff", "--no-renames", "--unified=0", "--no-color", initial, final, "--", path,
            check=False,
        )
        lines: list[str] = []
        for line in output.splitlines():
            if sign == "-" and line.startswith("-") and not line.startswith("---"):
                lines.append(line[1:])
            elif sign == "+" and line.startswith("+") and not line.startswith("+++"):
                lines.append(line[1:])
        value = "\n".join(lines)
        self._diff_fragments[key] = value
        return value

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
    """Extract fields, methods and nested-type members without treating a whole nested class as one member."""
    tokens = lex_java(text)
    if not tokens:
        return []

    brace_stack: list[int] = []
    brace_pairs: dict[int, int] = {}
    for index, token in enumerate(tokens):
        if token.text == "{":
            brace_stack.append(index)
        elif token.text == "}" and brace_stack:
            opening = brace_stack.pop()
            brace_pairs[opening] = index

    type_keywords = {"class", "interface", "enum", "record"}
    class_index = next((i for i, token in enumerate(tokens) if token.text in type_keywords), None)
    if class_index is None:
        return []
    root_open = next((i for i in range(class_index, len(tokens)) if tokens[i].text == "{"), None)
    if root_open is None or root_open not in brace_pairs:
        return []

    output: list[Member] = []

    def add(first: int, last: int, container: str) -> None:
        part = tokens[first:last + 1]
        if len(part) < minimum:
            return
        signature = member_signature(part)
        if container:
            signature = f"{container}: {signature}"
        output.append(Member(
            part[0].line,
            part[-1].line,
            signature,
            tuple(token.text for token in part),
            tuple(token.shape for token in part),
        ))

    def nested_name(header: list[Token], fallback: str) -> str:
        for index, token in enumerate(header):
            if token.text in type_keywords:
                for candidate in header[index + 1:]:
                    if candidate.shape == "ID":
                        return candidate.text
        return fallback

    def parse_body(opening: int, closing: int, container: str) -> None:
        index = opening + 1
        while index < closing:
            while index < closing and tokens[index].text in {";", "}"}:
                index += 1
            if index >= closing:
                break
            first = index
            cursor = index
            while cursor < closing and tokens[cursor].text not in {";", "{"}:
                cursor += 1
            if cursor >= closing:
                break
            if tokens[cursor].text == ";":
                add(first, cursor, container)
                index = cursor + 1
                continue

            block_end = brace_pairs.get(cursor)
            if block_end is None or block_end > closing:
                break
            header = tokens[first:cursor + 1]
            if any(token.text in type_keywords for token in header):
                name = nested_name(header, "<anonymous-type>")
                nested_container = f"{container}.{name}" if container else name
                parse_body(cursor, block_end, nested_container)
            else:
                add(first, block_end, container)
            index = block_end + 1

    root_header = tokens[class_index:root_open + 1]
    parse_body(root_open, brace_pairs[root_open], nested_name(root_header, "<root>"))
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
    elif exact_block >= max(minimum, int(min(source.size, target.size) * .42)):
        kind = "copied-fragment"
        confidence = "high" if exact_block >= min(source.size, target.size) * .68 else "medium"
    elif structural >= .88 and length >= .38 and exact >= .18 and structural_block >= minimum:
        kind, confidence = "adapted", "high" if structural >= .95 else "medium"
    elif (structural_block >= max(minimum + 4, int(min(source.size, target.size) * .50))
          and exact >= .12):
        kind, confidence = "adapted-fragment", "medium"
    else:
        return None
    target_scale = max(1, min(source.size, target.size))
    score = (
        exact * .42
        + structural * .35
        + min(1.0, exact_block / target_scale) * .13
        + min(1.0, structural_block / target_scale) * .10
    )
    return MemberMatch(
        source, target, kind, confidence, score, exact, structural,
        exact_block, structural_block,
    )


def estimated_matched_tokens(match: MemberMatch) -> int:
    """Estimate actual overlap instead of counting either complete member."""
    combined = match.source.size + match.target.size
    exact_estimate = round(match.exact_ratio * combined / 2)
    structural_estimate = round(match.structural_ratio * combined / 2 * .65)
    return min(
        match.source.size,
        match.target.size,
        max(match.exact_block, match.structural_block, exact_estimate, structural_estimate),
    )


def compare_files(source_members: Iterable[Member], target_members: Iterable[Member],
                  minimum: int) -> Similarity:
    sources = tuple(source_members)
    targets = tuple(target_members)
    source_total = sum(item.size for item in sources)
    target_total = sum(item.size for item in targets)
    matches: list[MemberMatch] = []

    for target in targets:
        choices = [
            match
            for source in sources
            if (match := compare_member(source, target, minimum)) is not None
        ]
        if not choices:
            continue
        best = max(
            choices,
            key=lambda item: (
                item.confidence == "high",
                item.score,
                estimated_matched_tokens(item),
                item.exact_block,
                item.structural_block,
            ),
        )
        matches.append(best)

    source_contributions: dict[int, int] = {}
    matched_target = 0
    for match in matches:
        estimate = estimated_matched_tokens(match)
        matched_target += estimate
        source_key = id(match.source)
        source_contributions[source_key] = max(source_contributions.get(source_key, 0), estimate)
    matched_source = sum(source_contributions.values())

    source_coverage = matched_source / source_total if source_total else 0.0
    target_coverage = matched_target / target_total if target_total else 0.0
    high = sum(item.confidence == "high" for item in matches)
    longest = max((max(item.exact_block, item.structural_block) for item in matches), default=0)
    strong_min = max(45, minimum * 2)

    if matches and (
        (target_coverage >= .50 and matched_target >= strong_min)
        or (source_coverage >= .50 and matched_source >= strong_min)
        or (high >= 2 and matched_target >= max(90, minimum * 4))
    ):
        classification, confidence = "inherited", "high"
    elif matches and (
        (target_coverage >= .12 and matched_target >= strong_min)
        or (source_coverage >= .12 and matched_source >= strong_min)
        or (high >= 1 and matched_target >= max(60, minimum * 3) and longest >= max(35, minimum))
    ):
        classification = "partial-inherited"
        confidence = "high" if high and target_coverage >= .35 else "medium"
    elif matches and matched_target >= minimum:
        classification, confidence = "manual-review", "low"
    else:
        classification, confidence = "no-material-lineage", "none"

    average = sum(item.score for item in matches) / len(matches) if matches else 0.0
    score = target_coverage * .45 + source_coverage * .25 + average * .30
    return Similarity(
        classification, confidence, score, source_coverage, target_coverage,
        source_total, target_total, matched_source, matched_target, tuple(matches),
    )


RESPONSIBILITY_STOP_WORDS = {
    # Java language and standard structural vocabulary.
    "abstract", "annotation", "application", "array", "arrays", "boolean", "builder",
    "byte", "bytes", "catch", "class", "close", "code", "command", "component",
    "configuration", "container", "control", "create", "current", "data", "default",
    "directory", "directly", "does", "empty", "entry", "equals", "error", "event",
    "events", "exception", "false", "field", "file", "files", "final", "first",
    "format", "from", "get", "handle", "height", "helper", "identifier", "image",
    "implementation", "import", "input", "instance", "integer", "internal", "java",
    "lang", "length", "license", "line", "list", "loader", "main", "method", "name",
    "new", "null", "object", "only", "output", "override", "package", "parent", "path",
    "private", "process", "protected", "public", "read", "result", "return", "returns",
    "runtime", "set", "size", "source", "spdx", "start", "static", "stream", "string",
    "system", "target", "test", "this", "thread", "throw", "time", "tools", "true",
    "type", "update", "user", "util", "value", "void", "wait", "width", "window",
    "with", "without", "write",
    # Repository-wide ownership and licensing metadata. These must never establish provenance.
    "amalgam", "copyright", "cross", "global", "lgpl", "ltda", "mobile", "platform",
    "solucoes", "superwaba", "total", "totalcross", "wabasoft",
}

RESPONSIBILITY_SHORT_ANCHORS = {"apk", "aab", "jar", "tcz"}
CAMEL_RE = re.compile(r"[A-Z]+(?=[A-Z][a-z]|\d|$)|[A-Z]?[a-z]+|\d+")


@dataclasses.dataclass(frozen=True)
class SemanticProfile:
    identifiers: frozenset[str]
    components: frozenset[str]
    line_count: int

    @property
    def all_terms(self) -> frozenset[str]:
        return self.identifiers | self.components


def identifier_parts(identifier: str) -> tuple[str, ...]:
    return tuple(piece.lower() for piece in CAMEL_RE.findall(identifier.replace("_", " ")) if piece)


def semantic_profile(text: str) -> SemanticProfile:
    """Extract code-only semantic identifiers.

    ``lex_java`` removes line/block comments, so copyright/SPDX headers and Javadocs never
    contribute terms. Package and import declarations are skipped because shared namespaces
    are not evidence of responsibility transfer. String contents are intentionally ignored;
    prose and diagnostics are too noisy for a provenance decision.
    """
    tokens = lex_java(text)
    identifiers: set[str] = set()
    components: set[str] = set()
    index = 0
    while index < len(tokens):
        token = tokens[index]
        if token.text in {"package", "import"}:
            index += 1
            while index < len(tokens) and tokens[index].text != ";":
                index += 1
            index += int(index < len(tokens))
            continue
        if token.shape != "ID":
            index += 1
            continue

        raw = token.text
        parts = identifier_parts(raw)
        normalized = "".join(parts)
        if (
            normalized
            and normalized not in RESPONSIBILITY_STOP_WORDS
            and (len(normalized) >= 5 or normalized in RESPONSIBILITY_SHORT_ANCHORS)
        ):
            identifiers.add(normalized)

        for part in parts:
            if (
                part not in RESPONSIBILITY_STOP_WORDS
                and (len(part) >= 5 or part in RESPONSIBILITY_SHORT_ANCHORS)
            ):
                components.add(part)
        index += 1

    return SemanticProfile(
        frozenset(identifiers),
        frozenset(components),
        len(text.splitlines()),
    )


def weighted_terms(terms: Iterable[str]) -> float:
    return sum(1.0 + min(2.0, max(0, len(term) - 6) * .15) for term in set(terms))


def responsibility_signal(source_fragment: str, target_fragment: str) -> ReviewSignal | None:
    """Return a conservative semantic-transfer signal.

    This signal is intentionally weaker than code similarity and is used only for manual
    review. It requires multiple shared code identifiers after comments, headers, package
    declarations, imports, string prose, and generic vocabulary have been removed.
    """
    source = semantic_profile(source_fragment)
    target = semantic_profile(target_fragment)
    shared_identifiers = source.identifiers & target.identifiers
    shared_components = source.components & target.components

    # A single shared identifier is normally coincidence (for example ``font`` or ``start``).
    # Require at least two exact code identifiers, or one exact identifier plus several
    # independent compound components. Components alone are never sufficient.
    if len(shared_identifiers) < 2:
        if len(shared_identifiers) < 1 or len(shared_components) < 4:
            return None

    strong_identifiers = {
        term for term in shared_identifiers
        if len(term) >= 7 or term in RESPONSIBILITY_SHORT_ANCHORS
    }
    if not strong_identifiers:
        return None

    shared = set(shared_identifiers) | set(shared_components)
    shared_weight = weighted_terms(shared)
    source_weight = weighted_terms(source.all_terms)
    target_weight = weighted_terms(target.all_terms)
    source_coverage = shared_weight / max(1.0, source_weight)
    target_coverage = shared_weight / max(1.0, target_weight)

    # Target coverage matters most for an extracted helper. The absolute anchor requirements
    # above prevent tiny or header-only fragments from obtaining an inflated ratio.
    score = target_coverage * .70 + source_coverage * .30
    if target_coverage < .12 or source_coverage < .01 or score < .10:
        return None

    return ReviewSignal(
        "responsibility-transfer",
        score,
        tuple(sorted(shared_identifiers)),
        tuple(sorted(shared_components - shared_identifiers)),
        source_coverage,
        target_coverage,
        source.line_count,
        target.line_count,
    )


def is_test_path(path: str) -> bool:
    normalized = f"/{path.replace('\\\\', '/').lower()}/"
    return "/src/test/" in normalized or "/test/" in normalized or "/tests/" in normalized


def responsibility_signal_for_pair(
    repo: Repo,
    initial: str,
    final: str,
    source_path: str,
    target_path: str,
    source_fragment: str,
) -> ReviewSignal | None:
    """Evaluate semantic transfer only for newly created production files.

    Material code similarity can still detect copies into existing files or tests. The
    terminology-only fallback is deliberately narrower because it cannot establish copied
    code and otherwise produces large amounts of unrelated manual-review noise.
    """
    if is_test_path(source_path) or is_test_path(target_path):
        return None
    if target_path in repo.paths(initial):
        return None
    if not source_fragment.strip():
        return None
    target_fragment = repo.diff_fragment(initial, final, target_path, "+")
    if not target_fragment.strip():
        return None
    return responsibility_signal(source_fragment, target_fragment)


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


def candidates(repo: Repo, initial: str, final: str, extensions: tuple[str, ...],
               include_unchanged_copy_sources: bool = False) -> tuple[list[str], list[str]]:
    """Discover endpoint candidates without treating unchanged files as automatic sources."""
    changes = repo.endpoint_changes(initial, final)
    initial_paths = repo.paths(initial)
    final_paths = repo.paths(final)

    sources: set[str] = set()
    for path in changes.before:
        if path not in initial_paths or not allowed(path, extensions):
            continue
        if path not in final_paths or repo.blob(initial, path) != repo.blob(final, path):
            sources.add(path)

    if include_unchanged_copy_sources:
        aggressive = repo.changes(initial, final)
        sources.update(
            path for path in aggressive.before
            if path in initial_paths and allowed(path, extensions)
        )

    targets = {
        path for path in changes.after
        if path in final_paths and allowed(path, extensions)
    }
    return sorted(sources), sorted(targets)


def make_node(repo: Repo, commit: str, path: str) -> Node:
    return Node(commit, path, repo.blob(commit, path))


def extend(route: Route, target: Node, reason: str, similarity: Similarity | None) -> Route:
    edge = Edge(route.nodes[-1], target, reason, similarity)
    return Route(route.nodes + (target,), route.edges + (edge,))


def choose(current: Route | None, candidate: Route) -> Route:
    return candidate if current is None or candidate.rank > current.rank else current


def effective(direct: Similarity, route: Route) -> dict[str, object]:
    if direct.material:
        return {
            "classification": direct.classification,
            "confidence": direct.confidence,
            "basis": "direct",
            "score": direct.score,
        }

    transfers = [edge for edge in route.edges if edge.source.path != edge.target.path]
    if transfers and all(edge.supports_lineage for edge in transfers):
        exact = all(edge.reason in {"git-rename-exact", "git-copy-exact"} for edge in transfers)
        all_strong = all(
            edge.reason in {"git-rename-exact", "git-copy-exact"}
            or bool(edge.similarity and edge.similarity.classification == "inherited"
                    and edge.similarity.confidence == "high")
            for edge in transfers
        )
        scores = [
            1.0 if edge.reason in {"git-rename-exact", "git-copy-exact"}
            else edge.similarity.score
            for edge in transfers
            if edge.reason in {"git-rename-exact", "git-copy-exact"} or edge.similarity
        ]
        return {
            "classification": "inherited" if exact or all_strong else "partial-inherited",
            "confidence": "high" if exact or all_strong else "medium",
            "basis": "transitive",
            "score": sum(scores) / len(scores) if scores else 0.0,
        }

    return {
        "classification": direct.classification,
        "confidence": direct.confidence,
        "basis": "direct",
        "score": direct.score,
    }


def review_effect(signal: ReviewSignal) -> dict[str, object]:
    return {
        "classification": "manual-review",
        "confidence": "low",
        "basis": signal.kind,
        "score": signal.score,
    }


def trace(repo: Repo, graph: list[tuple[str, tuple[str, ...]]], initial: str, final: str,
          source_path: str, final_candidates: set[str], minimum: int,
          extensions: tuple[str, ...], include_responsibility_transfer: bool) -> dict[str, object]:
    source = make_node(repo, initial, source_path)
    states: dict[str, dict[str, Route]] = {initial: {source_path: Route((source,), ())}}

    for commit, parents in graph[1:]:
        combined: dict[str, Route] = {}
        child_paths = repo.paths(commit)
        for parent in parents:
            parent_states = states.get(parent, {})
            if not parent_states:
                continue
            changed = repo.changes(parent, commit)
            destinations = [
                path for path in changed.after
                if path in child_paths and allowed(path, extensions)
            ]
            for active_path, route in parent_states.items():
                if active_path in child_paths and active_path not in changed.before:
                    combined[active_path] = choose(combined.get(active_path), route)

                if active_path in child_paths and active_path in changed.after:
                    sim = compare_files(
                        repo.members(parent, active_path, minimum),
                        repo.members(commit, active_path, minimum),
                        minimum,
                    )
                    continued = extend(
                        route, make_node(repo, commit, active_path), "same-path", sim,
                    )
                    combined[active_path] = choose(combined.get(active_path), continued)

                for destination in destinations:
                    if destination == active_path:
                        continue
                    hint = changed.hints.get((active_path, destination))
                    sim = compare_files(
                        repo.members(parent, active_path, minimum),
                        repo.members(commit, destination, minimum),
                        minimum,
                    )
                    same_blob = repo.blob(parent, active_path) == repo.blob(commit, destination)
                    if same_blob and hint:
                        reason = hint.split(":", 1)[0] + "-exact"
                    elif sim.material:
                        reason = hint or "similarity"
                    else:
                        continue
                    moved = extend(route, make_node(repo, commit, destination), reason, sim)
                    combined[destination] = choose(combined.get(destination), moved)
        states[commit] = combined

    results: dict[str, dict[str, object]] = {}
    final_paths = repo.paths(final)
    source_fragment = repo.diff_fragment(initial, final, source_path, "-")
    if not source_fragment.strip():
        source_fragment = repo.text(initial, source_path)

    for path, route in states.get(final, {}).items():
        if path == source_path or path not in final_paths or path not in final_candidates:
            continue
        if route.nodes[-1].commit != final:
            route = extend(route, make_node(repo, final, path), "unchanged-to-final", None)
        if not any(edge.source.path != edge.target.path for edge in route.edges):
            continue
        direct = compare_files(
            repo.members(initial, source_path, minimum),
            repo.members(final, path, minimum),
            minimum,
        )
        result = effective(direct, route)
        signal = None
        if (
            include_responsibility_transfer
            and result["classification"] not in {"inherited", "partial-inherited"}
        ):
            signal = responsibility_signal_for_pair(
                repo, initial, final, source_path, path, source_fragment,
            )
            if signal:
                result = review_effect(signal)
        if result["classification"] in {"inherited", "partial-inherited", "manual-review"}:
            results[path] = {
                "route": route,
                "direct": direct,
                "effective": result,
                "reviewSignal": signal,
            }

    for path in sorted(final_candidates):
        if path == source_path or path not in final_paths or path in results:
            continue
        direct = compare_files(
            repo.members(initial, source_path, minimum),
            repo.members(final, path, minimum),
            minimum,
        )
        target = make_node(repo, final, path)
        route = Route((source, target), (Edge(source, target, "direct-similarity", direct),))
        if direct.material:
            results[path] = {
                "route": route,
                "direct": direct,
                "effective": effective(direct, route),
                "reviewSignal": None,
            }
            continue
        signal = None
        if include_responsibility_transfer:
            signal = responsibility_signal_for_pair(
                repo, initial, final, source_path, path, source_fragment,
            )
        if signal:
            results[path] = {
                "route": Route(
                    (source, target),
                    (Edge(source, target, "responsibility-transfer", direct),),
                ),
                "direct": direct,
                "effective": review_effect(signal),
                "reviewSignal": signal,
            }

    return {"source": source, "targets": results}


def candidate_rank(value: dict[str, object]) -> tuple[int, int, float, float, int]:
    result: dict[str, object] = value["effective"]  # type: ignore[assignment]
    direct: Similarity = value["direct"]  # type: ignore[assignment]
    classification = {"inherited": 3, "partial-inherited": 2, "manual-review": 1}.get(
        str(result["classification"]), 0,
    )
    confidence = {"high": 3, "medium": 2, "low": 1, "none": 0}.get(
        str(result["confidence"]), 0,
    )
    return (
        classification,
        confidence,
        float(result.get("score", 0.0)),
        direct.target_coverage,
        direct.matched_target_tokens,
    )


def reconcile(raw: dict[str, dict[str, dict[str, object]]]) -> tuple[
        dict[str, dict[str, dict[str, object]]], list[dict[str, object]]]:
    """Select the strongest provenance source per target and suppress weaker noise globally."""
    by_target: dict[str, list[tuple[str, dict[str, object]]]] = {}
    for source_path, targets in raw.items():
        for target_path, value in targets.items():
            by_target.setdefault(target_path, []).append((source_path, value))

    selected: dict[str, dict[str, dict[str, object]]] = {source: {} for source in raw}
    assignments: list[dict[str, object]] = []

    for target_path, entries in sorted(by_target.items()):
        ordered = sorted(entries, key=lambda item: candidate_rank(item[1]), reverse=True)
        material = [
            item for item in ordered
            if item[1]["effective"]["classification"] in {"inherited", "partial-inherited"}  # type: ignore[index]
        ]
        kept: list[tuple[str, dict[str, object]]] = []
        if material:
            best_source, best_value = material[0]
            kept.append((best_source, best_value))
            best_score = max(.000001, float(best_value["effective"].get("score", 0.0)))  # type: ignore[union-attr]
            for source_path, value in material[1:]:
                direct: Similarity = value["direct"]  # type: ignore[assignment]
                result: dict[str, object] = value["effective"]  # type: ignore[assignment]
                score = float(result.get("score", 0.0))
                if (result["classification"] == "inherited"
                        and direct.target_coverage >= .12
                        and direct.matched_target_tokens >= 45
                        and score >= best_score * .80):
                    kept.append((source_path, value))
        elif ordered:
            source_path, value = ordered[0]
            if value.get("reviewSignal") is not None:
                kept.append((source_path, value))

        kept_sources = {source for source, _ in kept}
        rejected_pool = material if material else ordered
        rejected = [
            {
                "sourcePath": source,
                "classification": value["effective"]["classification"],  # type: ignore[index]
                "confidence": value["effective"]["confidence"],  # type: ignore[index]
                "score": round(float(value["effective"].get("score", 0.0)), 6),  # type: ignore[union-attr]
            }
            for source, value in rejected_pool if source not in kept_sources
        ]
        for index, (source_path, value) in enumerate(kept):
            copied = dict(value)
            copied["assignmentRole"] = "primary" if index == 0 else "secondary"
            copied["rejectedAlternatives"] = rejected
            selected[source_path][target_path] = copied
        assignments.append({
            "targetPath": target_path,
            "selectedSources": [
                {"sourcePath": source, "role": "primary" if index == 0 else "secondary"}
                for index, (source, _) in enumerate(kept)
            ],
            "rejectedAlternatives": rejected,
        })

    return {source: targets for source, targets in selected.items() if targets}, assignments


def similarity_json(value: Similarity | None) -> dict[str, object] | None:
    if value is None:
        return None
    return {
        "classification": value.classification,
        "confidence": value.confidence,
        "score": round(value.score, 6),
        "sourceCoverage": round(value.source_coverage, 6),
        "targetCoverage": round(value.target_coverage, 6),
        "sourceTokens": value.source_tokens,
        "targetTokens": value.target_tokens,
        "matchedSourceTokens": value.matched_source_tokens,
        "matchedTargetTokens": value.matched_target_tokens,
        "matches": [
            {
                "kind": item.kind,
                "confidence": item.confidence,
                "score": round(item.score, 6),
                "estimatedMatchedTokens": estimated_matched_tokens(item),
                "exactRatio": round(item.exact_ratio, 6),
                "structuralRatio": round(item.structural_ratio, 6),
                "longestExactBlock": item.exact_block,
                "longestStructuralBlock": item.structural_block,
                "source": {
                    "first": item.source.first,
                    "last": item.source.last,
                    "signature": item.source.signature,
                    "tokens": item.source.size,
                },
                "target": {
                    "first": item.target.first,
                    "last": item.target.last,
                    "signature": item.target.signature,
                    "tokens": item.target.size,
                },
            }
            for item in value.matches
        ],
    }


def review_signal_json(value: ReviewSignal | None) -> dict[str, object] | None:
    if value is None:
        return None
    return {
        "kind": value.kind,
        "score": round(value.score, 6),
        "sharedIdentifiers": list(value.shared_identifiers),
        "sharedComponents": list(value.shared_components),
        "sharedTerms": list(value.shared_terms),
        "sourceTermCoverage": round(value.source_term_coverage, 6),
        "targetTermCoverage": round(value.target_term_coverage, 6),
        "sourceFragmentLines": value.source_fragment_lines,
        "targetFragmentLines": value.target_fragment_lines,
    }


def route_json(route: Route) -> dict[str, object]:
    return {
        "nodes": [dataclasses.asdict(item) for item in route.nodes],
        "edges": [
            {
                "source": dataclasses.asdict(edge.source),
                "target": dataclasses.asdict(edge.target),
                "reason": edge.reason,
                "supportsLineage": edge.supports_lineage,
                "similarity": similarity_json(edge.similarity),
            }
            for edge in route.edges
        ],
    }


def java_name(path: str) -> str:
    normalized = path.replace("\\", "/")
    marker = "src/main/java/"
    relative = normalized.split(marker, 1)[1] if marker in normalized else normalized
    return relative[:-5].replace("/", ".") if relative.endswith(".java") else relative.replace("/", ".")


def slug(path: str) -> str:
    return re.sub(r"[^A-Za-z0-9_.-]+", "-", java_name(path)).strip(".-") or "source"


def compact(value: str, limit: int = 72) -> str:
    value = " ".join(value.split())
    return value if len(value) <= limit else value[:limit - 1] + "…"


def route_paths(route: Route) -> list[str]:
    output = [route.nodes[0].path]
    for node in route.nodes[1:]:
        if node.path != output[-1]:
            output.append(node.path)
    return output


def source_report(source_path: str, initial: str, final: str,
                  targets: dict[str, dict[str, object]], source_text: str) -> str:
    material = {
        path: value for path, value in targets.items()
        if value["effective"]["classification"] in {"inherited", "partial-inherited"}  # type: ignore[index]
    }
    reviews = {
        path: value for path, value in targets.items()
        if value["effective"]["classification"] == "manual-review"  # type: ignore[index]
    }
    output = [
        f"# Provenance report: `{java_name(source_path)}`",
        "",
        f"- Initial revision: `{initial}`",
        f"- Final revision: `{final}`",
        f"- Historical source: `{source_path}`",
        "- Status: automated evidence; human review is required before activation.",
        "",
    ]

    if material:
        output += [
            "## Final targets",
            "",
            "| Final file | Role | Result | Source coverage | Target coverage | Header |",
            "|---|---|---|---:|---:|---|",
        ]
        for path, value in sorted(material.items()):
            direct: Similarity = value["direct"]  # type: ignore[assignment]
            result: dict[str, object] = value["effective"]  # type: ignore[assignment]
            header = assess_header(source_text, value["targetText"])  # type: ignore[arg-type]
            output.append(
                f"| `{path}` | `{value['assignmentRole']}` | "
                f"`{result['classification']}`/{result['confidence']} ({result['basis']}) | "
                f"{direct.source_coverage:.1%} | {direct.target_coverage:.1%} | "
                f"`{header['status']}` |"
            )
        output.append("")

    if reviews:
        output += [
            "## Manual review candidates",
            "",
            "These entries indicate responsibility or terminology transferred in the audited interval, "
            "but do not automatically establish copied code or an inherited-header decision.",
            "",
            "| Final file | Signal | Shared code identifiers |",
            "|---|---|---|",
        ]
        for path, value in sorted(reviews.items()):
            signal: ReviewSignal = value["reviewSignal"]  # type: ignore[assignment]
            output.append(
                f"| `{path}` | `{signal.kind}` ({signal.score:.3f}) | "
                f"{', '.join(f'`{term}`' for term in signal.shared_terms)} |"
            )
        output.append("")

    output += ["## Findings", ""]
    for path, value in sorted(targets.items()):
        direct: Similarity = value["direct"]  # type: ignore[assignment]
        route: Route = value["route"]  # type: ignore[assignment]
        result: dict[str, object] = value["effective"]  # type: ignore[assignment]
        header = assess_header(source_text, value["targetText"])  # type: ignore[arg-type]
        output += [
            f"### `{path}`",
            "",
            f"Classification: **{result['classification']}** "
            f"({result['confidence']}, {result['basis']} evidence; "
            f"assignment `{value['assignmentRole']}`).",
            f"Direct matched tokens: source {direct.matched_source_tokens}/{direct.source_tokens} "
            f"({direct.source_coverage:.1%}), target {direct.matched_target_tokens}/{direct.target_tokens} "
            f"({direct.target_coverage:.1%}).",
        ]
        if result["classification"] in {"inherited", "partial-inherited"}:
            output.append(f"Header assessment: **{header['status']}**.")
        else:
            output.append("Header assessment is informational only for this manual-review candidate.")
        output += ["", "Lineage:", "", " → ".join(f"`{item}`" for item in route_paths(route)), ""]

        if len(route_paths(route)) > 2:
            output += [
                "Intermediate files are evidence only and are not final targets:",
                "",
                *[f"- `{item}`" for item in route_paths(route)[1:-1]],
                "",
            ]

        signal: ReviewSignal | None = value.get("reviewSignal")  # type: ignore[assignment]
        if signal:
            output += [
                f"Responsibility-transfer signal: score `{signal.score:.3f}`; "
                f"source-term coverage {signal.source_term_coverage:.1%}; "
                f"target-term coverage {signal.target_term_coverage:.1%}.",
                "",
                "Shared code identifiers: "
                + ", ".join(f"`{term}`" for term in signal.shared_identifiers),
                "",
                "Shared identifier components: "
                + (", ".join(f"`{term}`" for term in signal.shared_components) or "none"),
                "",
            ]

        if direct.matches:
            output += [
                "| Source member | Target member | Finding | Estimated tokens | Exact | Structural |",
                "|---|---|---|---:|---:|---:|",
            ]
            for item in direct.matches:
                output.append(
                    f"| {item.source.first}-{item.source.last} `{compact(item.source.signature)}` | "
                    f"{item.target.first}-{item.target.last} `{compact(item.target.signature)}` | "
                    f"{item.kind}/{item.confidence} | {estimated_matched_tokens(item)} | "
                    f"{item.exact_ratio:.1%} | {item.structural_ratio:.1%} |"
                )
            output.append("")

        rejected = value.get("rejectedAlternatives", [])
        if rejected:
            output += ["Weaker alternatives rejected during global target reconciliation:", ""]
            for item in rejected:  # type: ignore[assignment]
                output.append(
                    f"- `{item['sourcePath']}`: `{item['classification']}`/"
                    f"`{item['confidence']}`, score `{item['score']}`"
                )
            output.append("")

    output += [
        "## Interpretation",
        "",
        "- `inherited`: strong material lineage.",
        "- `partial-inherited`: a material extracted or adapted portion was detected.",
        "- `manual-review`: multiple non-generic code identifiers moved into a newly created "
        "production file, without enough textual evidence for an automatic inheritance decision.",
        "- `manual-review` edges never support transitive inherited classifications.",
        "- Intermediate files document the path but receive no final decision if removed.",
        "- This is technical provenance evidence, not an independent legal opinion.",
        "",
    ]
    return "\n".join(output)


def summary(initial: str, final: str, results: list[dict[str, object]]) -> str:
    output = [
        "# Copyright provenance audit",
        "",
        f"- Initial revision: `{initial}`",
        f"- Final revision: `{final}`",
        "- Automatic sources are limited to files materially changed or removed in the interval.",
        "- Only files present at the final revision are listed as targets.",
        "- Removed intermediate files remain in lineage evidence.",
        "- Final targets are reconciled globally so weak alternative sources do not override stronger ones.",
        "",
        "## Results",
        "",
        "| Historical source | Final targets | Manual review | Report |",
        "|---|---:|---:|---|",
    ]
    for item in results:
        output.append(
            f"| `{item['sourcePath']}` | {len(item['finalTargets'])} | "
            f"{len(item['reviewCandidates'])} | `{item['report']}` |"
        )
    output += [
        "",
        "The manifest remains `pending-review` until a human confirms the results and lists it "
        "in `active-audits.json`.",
        "",
    ]
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
    parser.add_argument(
        "--include-unchanged-copy-sources",
        action="store_true",
        help="also seed automatic audits from unchanged files suggested by Git copy detection",
    )
    parser.add_argument(
        "--include-responsibility-transfer",
        action="store_true",
        help=(
            "also emit conservative terminology-based manual-review candidates for newly "
            "created production files; disabled by default"
        ),
    )
    return parser.parse_args(argv)


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.rstrip() + "\n", encoding="utf-8")


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv or sys.argv[1:])
    repo = Repo(args.repo)
    initial = repo.resolve(args.initial_commit)
    final = repo.resolve(args.final_commit)
    if initial == final:
        raise AuditError("initial and final commits must differ")
    if not repo.is_ancestor(initial, final):
        raise AuditError("initial commit must be an ancestor of final commit")

    extensions = tuple(
        item.strip() if item.strip().startswith(".") else f".{item.strip()}"
        for item in args.extensions.split(",") if item.strip()
    )
    auto_sources, auto_targets = candidates(
        repo, initial, final, extensions, args.include_unchanged_copy_sources,
    )
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
            raise AuditError(
                f"automatic mode found {len(sources)} sources; narrow the interval, provide "
                "SOURCE_PATH, or raise --max-auto-sources"
            )
    if not sources:
        raise AuditError("no source candidates found")

    graph = repo.graph(initial, final)
    final_candidates = set(auto_targets)
    traced_sources: dict[str, Node] = {}
    raw: dict[str, dict[str, dict[str, object]]] = {}

    for index, source_path in enumerate(sources, 1):
        print(f"[{index}/{len(sources)}] tracing {source_path}", file=sys.stderr)
        traced = trace(
            repo, graph, initial, final, source_path, final_candidates,
            args.min_tokens, extensions, args.include_responsibility_transfer,
        )
        targets: dict[str, dict[str, object]] = traced["targets"]  # type: ignore[assignment]
        if targets:
            traced_sources[source_path] = traced["source"]  # type: ignore[assignment]
            raw[source_path] = targets

    selected, assignments = reconcile(raw)
    if not selected:
        print("No material or interval-specific review lineage was detected.")
        return 0

    manifest_results: list[dict[str, object]] = []
    details: list[tuple[str, str, dict[str, dict[str, object]], str, dict[str, object]]] = []

    for source_path, targets in sorted(selected.items()):
        source_text = repo.text(initial, source_path)
        for path, value in targets.items():
            value["targetText"] = repo.text(final, path)
        name = slug(source_path)
        evidence_rel = Path("evidence") / f"{name}.json"
        report_rel = Path("reports") / f"{name}.md"
        final_entries: list[dict[str, object]] = []
        review_entries: list[dict[str, object]] = []
        evidence_targets: list[dict[str, object]] = []

        for path, value in sorted(targets.items()):
            direct: Similarity = value["direct"]  # type: ignore[assignment]
            route: Route = value["route"]  # type: ignore[assignment]
            result: dict[str, object] = value["effective"]  # type: ignore[assignment]
            target_text: str = value["targetText"]  # type: ignore[assignment]
            header = assess_header(source_text, target_text)
            common = {
                "path": path,
                "blob": repo.blob(final, path),
                "codeFingerprint": code_fingerprint(target_text),
                "classification": result["classification"],
                "confidence": result["confidence"],
                "evidenceBasis": result["basis"],
                "evidenceScore": round(float(result.get("score", 0.0)), 6),
                "assignmentRole": value["assignmentRole"],
                "headerStatus": header["status"],
            }
            if result["classification"] in {"inherited", "partial-inherited"}:
                final_entries.append(common)
            else:
                common["reviewSignal"] = review_signal_json(value.get("reviewSignal"))
                review_entries.append(common)
            evidence_targets.append({
                **common,
                "effectiveEvidence": result,
                "directEvidence": similarity_json(direct),
                "reviewSignal": review_signal_json(value.get("reviewSignal")),
                "header": header,
                "lineage": route_json(route),
                "rejectedAlternatives": value.get("rejectedAlternatives", []),
            })

        manifest_results.append({
            "sourcePath": source_path,
            "sourceBlob": repo.blob(initial, source_path),
            "sourceCodeFingerprint": code_fingerprint(source_text),
            "evidence": str(evidence_rel),
            "report": str(report_rel),
            "finalTargets": final_entries,
            "reviewCandidates": review_entries,
        })
        evidence = {
            "schemaVersion": 4,
            "source": dataclasses.asdict(traced_sources[source_path]),
            "sourceCodeFingerprint": code_fingerprint(source_text),
            "sourceHeader": assess_header(source_text, source_text),
            "targets": evidence_targets,
        }
        details.append((source_path, name, targets, source_text, evidence))

    now = dt.datetime.now().astimezone()
    audit_id = args.audit_id or f"{now.date().isoformat()}-{initial[:12]}-{final[:12]}"
    output = args.output or (repo.root / ROOT / "audits" / audit_id)
    if not output.is_absolute():
        output = repo.root / output
    if output.exists() and any(output.iterdir()) and not args.overwrite:
        raise AuditError(f"output is not empty: {output}; use --overwrite")
    output.mkdir(parents=True, exist_ok=True)

    script = Path(__file__).resolve()
    script_hash = hashlib.sha256(script.read_bytes()).hexdigest()
    command = " ".join(shlex.quote(item) for item in [sys.executable, *sys.argv])
    try:
        tool_path = str(script.relative_to(repo.root))
    except ValueError:
        tool_path = str(script)

    manifest = {
        "schemaVersion": 4,
        "auditId": audit_id,
        "reviewStatus": "pending-review",
        "createdAt": now.isoformat(),
        "initialRevision": initial,
        "finalRevision": final,
        "mode": "single-source" if args.source_path else "automatic",
        "requestedSourcePath": args.source_path,
        "automaticSourcePolicy": (
            "changed-or-removed-plus-unchanged-copy-sources"
            if args.include_unchanged_copy_sources else "changed-or-removed-only"
        ),
        "responsibilityTransferPolicy": (
            "enabled-conservative" if args.include_responsibility_transfer else "disabled"
        ),
        "tool": {"path": tool_path, "sha256": script_hash, "command": command},
        "targetAssignments": assignments,
        "results": manifest_results,
        "supersedes": [],
    }
    write(output / "manifest.json", json.dumps(manifest, indent=2, ensure_ascii=False))
    write(output / "summary.md", summary(initial, final, manifest_results))
    for source_path, name, targets, source_text, evidence in details:
        write(
            output / "evidence" / f"{name}.json",
            json.dumps(evidence, indent=2, ensure_ascii=False),
        )
        write(
            output / "reports" / f"{name}.md",
            source_report(source_path, initial, final, targets, source_text),
        )

    print(output)
    missing = [
        target["path"]
        for result in manifest_results
        for target in result["finalTargets"]
        if target["headerStatus"] != "preserved"
    ]
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
