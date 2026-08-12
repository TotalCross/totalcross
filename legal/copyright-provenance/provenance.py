# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only
"""Shared helpers for active copyright-provenance audits."""
from __future__ import annotations

import dataclasses
import datetime as dt
import hashlib
import json
import re
import subprocess
from pathlib import Path
from typing import Iterable

AMALGAM = "Amalgam Solucoes em TI Ltda"
TOTALCROSS = "TotalCross Global Mobile Platform Ltda"
SUPERWABA = "SuperWaba Ltda"
DAVE_SLAUGHTER = "Dave Slaughter"
WABASOFT_PREFIX = "Wabasoft"
SPDX = "SPDX-License-Identifier: LGPL-2.1-only"
PROVENANCE_ROOT = Path("legal/copyright-provenance")
ACTIVE_AUDITS = PROVENANCE_ROOT / "active-audits.json"
COPYRIGHT_RE = re.compile(r"Copyright \(C\)\s+([0-9][0-9,\- ]*)\s+(.+?)\.?$")
OPERATORS = sorted((
    ">>>=", "<<=", ">>=", "...", "::", "->", "++", "--", "==", "!=",
    "<=", ">=", "&&", "||", "+=", "-=", "*=", "/=", "%=", "&=", "|=",
    "^=", "<<", ">>>", ">>",
), key=len, reverse=True)


class ProvenanceError(RuntimeError):
    pass


@dataclasses.dataclass(frozen=True)
class Notice:
    years: frozenset[int]
    owner: str


@dataclasses.dataclass
class Rule:
    path: str
    fingerprint: str
    notices: list[Notice]
    audit_ids: list[str]


def git(args: list[str], check: bool = True) -> str:
    result = subprocess.run(
        ["git", *args], check=False, stdout=subprocess.PIPE,
        stderr=subprocess.PIPE, text=True, encoding="utf-8", errors="replace",
    )
    if check and result.returncode:
        raise ProvenanceError(result.stderr.strip() or f"git {' '.join(args)} failed")
    return result.stdout


def repo_root() -> Path:
    value = git(["rev-parse", "--show-toplevel"]).strip()
    if not value:
        raise ProvenanceError("not inside a Git repository")
    return Path(value)


def resolve_commit(value: str) -> str:
    resolved = git(["rev-parse", "--verify", f"{value}^{{commit}}"], False).strip()
    if not resolved:
        raise ProvenanceError(f"cannot resolve commit: {value}")
    return resolved


def git_text(revision: str, path: str) -> str:
    result = subprocess.run(
        ["git", "show", f"{revision}:{path}"], stdout=subprocess.PIPE,
        stderr=subprocess.PIPE, text=True, encoding="utf-8", errors="replace",
    )
    if result.returncode:
        raise ProvenanceError(
            f"cannot read {path} at {revision[:12]}: {result.stderr.strip()}"
        )
    return result.stdout


def git_blob(revision: str, path: str) -> str:
    value = git(["rev-parse", "--verify", f"{revision}:{path}"], False).strip()
    if not value:
        raise ProvenanceError(f"cannot resolve blob {revision[:12]}:{path}")
    return value


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def normalize_owner(owner: str) -> str:
    return owner.strip().rstrip(".")


def parse_years(value: str) -> frozenset[int]:
    years: set[int] = set()
    for part in value.split(","):
        part = part.strip()
        if not part:
            continue
        if "-" in part:
            start_text, end_text = part.split("-", 1)
            start, end = int(start_text.strip()), int(end_text.strip())
            if start > end:
                raise ProvenanceError(f"invalid year range: {part}")
            years.update(range(start, end + 1))
        else:
            years.add(int(part))
    return frozenset(years)


def format_years(years: Iterable[int], comma_pair: bool = False) -> str:
    ordered = sorted(set(years))
    if not ordered:
        raise ProvenanceError("empty copyright year set")
    if comma_pair and len(ordered) == 2 and ordered[1] == ordered[0] + 1:
        return f"{ordered[0]}, {ordered[1]}"
    ranges: list[str] = []
    start = previous = ordered[0]
    for year in ordered[1:]:
        if year == previous + 1:
            previous = year
            continue
        ranges.append(str(start) if start == previous else f"{start}-{previous}")
        start = previous = year
    ranges.append(str(start) if start == previous else f"{start}-{previous}")
    return ", ".join(ranges)


def parse_notices(text: str) -> list[Notice]:
    notices: list[Notice] = []
    for line in text.splitlines()[:120]:
        clean = line.strip().lstrip("/*# ").rstrip("*/ ")
        match = COPYRIGHT_RE.search(clean)
        if match:
            notices.append(Notice(parse_years(match.group(1)), normalize_owner(match.group(2))))
        elif notices and SPDX in line:
            break
    return notices


def parse_recorded_notices(value: object, context: str) -> list[Notice]:
    """Parse source notices stored in immutable provenance evidence."""
    if not isinstance(value, list) or not value:
        raise ProvenanceError(f"missing source copyright notices: {context}")
    notices: list[Notice] = []
    for index, item in enumerate(value):
        if not isinstance(item, dict):
            raise ProvenanceError(f"invalid source notice {index}: {context}")
        years = item.get("years")
        owner = item.get("owner")
        if not isinstance(years, str) or not isinstance(owner, str):
            raise ProvenanceError(f"invalid source notice {index}: {context}")
        normalized_owner = normalize_owner(owner)
        if not normalized_owner:
            raise ProvenanceError(f"empty source notice owner at {index}: {context}")
        try:
            parsed_years = parse_years(years)
        except (ProvenanceError, ValueError) as exc:
            raise ProvenanceError(
                f"invalid source notice years at {index}: {context}: {years}"
            ) from exc
        if not parsed_years:
            raise ProvenanceError(f"empty source notice years at {index}: {context}")
        notices.append(Notice(parsed_years, normalized_owner))
    return notices


def merge_years(notices: Iterable[Notice]) -> dict[str, set[int]]:
    merged: dict[str, set[int]] = {}
    for notice in notices:
        merged.setdefault(notice.owner, set()).update(notice.years)
    return merged


def canonical_inherited(source: Iterable[Notice], current_year: int | None = None) -> list[Notice]:
    current_year = current_year or dt.date.today().year
    merged = merge_years(source)
    if not merged:
        raise ProvenanceError("provenance source has no copyright notices")
    result: list[Notice] = []
    handled: set[str] = set()
    wabasoft = [owner for owner in merged if owner.startswith(WABASOFT_PREFIX)]
    for owner in wabasoft:
        result.append(Notice(frozenset(merged[owner]), owner)); handled.add(owner)
    if DAVE_SLAUGHTER in merged:
        result.append(Notice(frozenset(merged[DAVE_SLAUGHTER]), DAVE_SLAUGHTER))
        handled.add(DAVE_SLAUGHTER)
    historical = bool(wabasoft or DAVE_SLAUGHTER in merged
                      or SUPERWABA in merged or TOTALCROSS in merged)
    if SUPERWABA in merged:
        result.append(Notice(frozenset(range(min(merged[SUPERWABA]), 2014)), SUPERWABA))
        handled.add(SUPERWABA)
    if TOTALCROSS in merged:
        result.append(Notice(frozenset(range(min(merged[TOTALCROSS]), 2022)), TOTALCROSS))
        handled.add(TOTALCROSS)
    if historical:
        result.append(Notice(frozenset(range(2022, current_year + 1)), AMALGAM))
        handled.add(AMALGAM)
    elif AMALGAM in merged:
        result.append(Notice(frozenset(range(min(merged[AMALGAM]), current_year + 1)), AMALGAM))
        handled.add(AMALGAM)
    for owner, years in merged.items():
        if owner not in handled:
            result.append(Notice(frozenset(years), owner))
    return result


def creation_notices(year: int, current_year: int | None = None) -> list[Notice]:
    current_year = current_year or dt.date.today().year
    if 2000 <= year <= 2013:
        return [
            Notice(frozenset(range(year, 2014)), SUPERWABA),
            Notice(frozenset(range(2014, 2022)), TOTALCROSS),
            Notice(frozenset(range(2022, current_year + 1)), AMALGAM),
        ]
    if 2014 <= year <= 2021:
        return [
            Notice(frozenset(range(year, 2022)), TOTALCROSS),
            Notice(frozenset(range(2022, current_year + 1)), AMALGAM),
        ]
    return [Notice(frozenset(range(year, current_year + 1)), AMALGAM)]


def java_fingerprint(text: str) -> str:
    tokens: list[str] = []
    index = 0
    while index < len(text):
        char = text[index]
        if char.isspace():
            index += 1; continue
        if text.startswith("//", index):
            end = text.find("\n", index + 2)
            if end < 0: break
            index = end; continue
        if text.startswith("/*", index):
            end = text.find("*/", index + 2)
            if end < 0: break
            index = end + 2; continue
        if char in "\"'":
            quote, start, escaped = char, index, False
            index += 1
            while index < len(text):
                current = text[index]
                if escaped: escaped = False
                elif current == "\\": escaped = True
                elif current == quote:
                    index += 1; break
                index += 1
            raw = text[start:index]
            shape = "STRING" if quote == '"' else "CHAR"
            tokens.append(f"{shape}:{hashlib.sha1(raw.encode('utf-8')).hexdigest()[:10]}")
            continue
        if char.isalpha() or char in "_$":
            end = index + 1
            while end < len(text) and (text[end].isalnum() or text[end] in "_$"): end += 1
            tokens.append(text[index:end]); index = end; continue
        if char.isdigit():
            end = index + 1
            while end < len(text) and (text[end].isalnum() or text[end] in "._"): end += 1
            tokens.append(text[index:end]); index = end; continue
        operator = next((item for item in OPERATORS if text.startswith(item, index)), None)
        value = operator or char
        tokens.append(value); index += len(value)
    return hashlib.sha256("\0".join(tokens).encode("utf-8")).hexdigest()


def read_json(path: Path) -> dict[str, object]:
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        raise ProvenanceError(f"cannot read {path}: {exc}") from exc
    if not isinstance(data, dict):
        raise ProvenanceError(f"JSON root must be an object: {path}")
    return data


def write_json(path: Path, data: dict[str, object]) -> None:
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def active_refs(root: Path) -> list[str]:
    path = root / ACTIVE_AUDITS
    if not path.exists():
        return []
    data = read_json(path)
    if data.get("schemaVersion") != 1 or not isinstance(data.get("active"), list):
        raise ProvenanceError(f"invalid {ACTIVE_AUDITS} schema")
    if not all(isinstance(item, str) for item in data["active"]):
        raise ProvenanceError(f"active audit entries must be strings: {ACTIVE_AUDITS}")
    return list(data["active"])


def set_active_refs(root: Path, refs: list[str]) -> None:
    write_json(root / ACTIVE_AUDITS, {"schemaVersion": 1, "active": refs})


def manifest_path(root: Path, audit_id: str) -> Path:
    return root / PROVENANCE_ROOT / "audits" / audit_id / "manifest.json"


def relative_manifest(audit_id: str) -> str:
    return f"audits/{audit_id}/manifest.json"


def load_rules(root: Path, audit_filter: set[str] | None = None,
               require_source_snapshot: bool = False) -> dict[str, Rule]:
    rules: dict[str, Rule] = {}
    base = (root / PROVENANCE_ROOT).resolve()
    loaded_audits: set[str] = set()
    for ref in active_refs(root):
        path = (base / ref).resolve()
        if base not in path.parents or not path.is_file():
            raise ProvenanceError(f"invalid active audit manifest: {ref}")
        manifest = read_json(path)
        audit_id = manifest.get("auditId")
        if not isinstance(audit_id, str):
            raise ProvenanceError(f"active manifest missing auditId: {ref}")
        if audit_filter is not None and audit_id not in audit_filter:
            continue
        if manifest.get("reviewStatus") != "approved":
            raise ProvenanceError(f"active audit is not approved: {audit_id}")
        if int(manifest.get("schemaVersion", 0)) < 4:
            raise ProvenanceError(f"unsupported active audit schema: {audit_id}")
        initial_revision = manifest.get("initialRevision")
        if not isinstance(initial_revision, str) or not initial_revision:
            raise ProvenanceError(f"active manifest missing initialRevision: {audit_id}")
        initial = resolve_commit(initial_revision) if require_source_snapshot else None
        loaded_audits.add(audit_id)
        for result in manifest.get("results", []):
            if not isinstance(result, dict):
                raise ProvenanceError(f"invalid result in audit {audit_id}")
            source_path = result.get("sourcePath")
            source_blob = result.get("sourceBlob")
            if not isinstance(source_path, str) or not isinstance(source_blob, str):
                raise ProvenanceError(f"invalid source in audit {audit_id}")
            evidence_ref = result.get("evidence")
            if not isinstance(evidence_ref, str) or not evidence_ref:
                raise ProvenanceError(f"missing evidence in audit {audit_id}: {source_path}")
            audit_dir = path.parent.resolve()
            evidence_path = (audit_dir / evidence_ref).resolve()
            if audit_dir not in evidence_path.parents or not evidence_path.is_file():
                raise ProvenanceError(
                    f"invalid evidence reference in audit {audit_id}: {evidence_ref}"
                )
            evidence = read_json(evidence_path)
            if evidence.get("schemaVersion") != manifest.get("schemaVersion"):
                raise ProvenanceError(f"evidence schema mismatch: {audit_id}: {evidence_ref}")
            evidence_source = evidence.get("source")
            if not isinstance(evidence_source, dict):
                raise ProvenanceError(f"missing evidence source: {audit_id}: {evidence_ref}")
            source_fields = (
                ("commit", initial_revision),
                ("path", source_path),
                ("blob", source_blob),
            )
            for field, expected in source_fields:
                if field not in evidence_source:
                    continue
                actual = evidence_source[field]
                if not isinstance(actual, str):
                    raise ProvenanceError(
                        f"invalid evidence source {field}: {audit_id}: {evidence_ref}"
                    )
                if actual != expected:
                    raise ProvenanceError(
                        f"evidence source {field} mismatch: {audit_id}: {source_path}"
                    )
            evidence_fingerprint = evidence.get("sourceCodeFingerprint")
            result_fingerprint = result.get("sourceCodeFingerprint")
            if evidence_fingerprint is not None and result_fingerprint is not None:
                if (not isinstance(evidence_fingerprint, str)
                        or evidence_fingerprint != result_fingerprint):
                    raise ProvenanceError(
                        f"evidence source fingerprint mismatch: {audit_id}: {source_path}"
                    )
            source_header = evidence.get("sourceHeader")
            if not isinstance(source_header, dict):
                raise ProvenanceError(f"missing evidence source header: {audit_id}: {evidence_ref}")
            recorded_notices = parse_recorded_notices(
                source_header.get("sourceNotices"), f"{audit_id}: {evidence_ref}"
            )
            if initial is not None:
                if git_blob(initial, source_path) != source_blob:
                    raise ProvenanceError(f"source blob mismatch: {audit_id}: {source_path}")
                snapshot_notices = parse_notices(git_text(initial, source_path))
                if merge_years(snapshot_notices) != merge_years(recorded_notices):
                    raise ProvenanceError(
                        f"evidence source notices mismatch: {audit_id}: {source_path}"
                    )
            notices = canonical_inherited(recorded_notices)
            for target in result.get("finalTargets", []):
                if not isinstance(target, dict) or target.get("classification") not in {
                    "inherited", "partial-inherited",
                }:
                    continue
                target_path = target.get("path")
                fingerprint = target.get("codeFingerprint")
                if not isinstance(target_path, str) or not isinstance(fingerprint, str):
                    raise ProvenanceError(f"invalid target in audit {audit_id}")
                if Path(target_path).suffix != ".java":
                    raise ProvenanceError(f"schema 4 fingerprints support Java only: {target_path}")
                rule = rules.get(target_path)
                if rule is None:
                    rules[target_path] = Rule(target_path, fingerprint, list(notices), [audit_id])
                else:
                    # Approved provenance establishes a permanent, conservative
                    # notice requirement. Later snapshots may legitimately have
                    # different fingerprints after ordinary code evolution; merge
                    # their required notices instead of treating the snapshots as
                    # conflicting active rules.
                    merged = merge_years([*rule.notices, *notices])
                    rule.notices = [Notice(frozenset(years), owner) for owner, years in merged.items()]
                    rule.audit_ids.append(audit_id)
    if audit_filter:
        missing = audit_filter - loaded_audits
        if missing:
            raise ProvenanceError("audit is not active: " + ", ".join(sorted(missing)))
    return rules


def display_owner(owner: str) -> str:
    if owner == SUPERWABA: return "SuperWaba Ltda."
    if owner == TOTALCROSS: return "TotalCross Global Mobile Platform Ltda."
    return owner


def comment_token(path: str) -> str | None:
    suffix = Path(path).suffix
    if suffix in {".md", ".html"}: return None
    if suffix in {".sh", ".py", ".rb", ".yml", ".yaml"}: return "#"
    return "//"


def render_header(path: str, notices: Iterable[Notice]) -> str:
    lines = [
        f"Copyright (C) {format_years(n.years, n.owner.startswith(WABASOFT_PREFIX))} "
        f"{display_owner(n.owner)}" for n in notices
    ]
    if Path(path).suffix in {".md", ".html"}:
        return "<!--\n" + "\n".join(lines) + f"\n\n{SPDX}\n-->\n\n"
    token = comment_token(path)
    if token is None: raise ProvenanceError(f"unsupported header syntax: {path}")
    return "\n".join([*(f"{token} {line}" for line in lines), token, f"{token} {SPDX}", ""]) + "\n"


def header_span(text: str, path: str) -> tuple[int, int] | None:
    lines = text.splitlines(keepends=True)
    if not lines: return None
    start = 1 if lines[0].startswith("#!") else 0
    while start < len(lines) and not lines[start].strip(): start += 1
    suffix = Path(path).suffix
    if suffix in {".md", ".html"} and start < len(lines) and lines[start].lstrip().startswith("<!--"):
        end, found = start, False
        while end < len(lines):
            found |= "Copyright (C)" in lines[end] or SPDX in lines[end]
            end += 1
            if "-->" in lines[end - 1]: break
        if not found: return None
    else:
        token = comment_token(path)
        if token is None or start >= len(lines) or not lines[start].lstrip().startswith(token):
            return None
        end, found = start, False
        while end < len(lines):
            stripped = lines[end].lstrip()
            if not stripped.startswith(token) and stripped.strip(): break
            found |= "Copyright (C)" in lines[end] or SPDX in lines[end]
            end += 1
        if not found: return None
    while end < len(lines) and not lines[end].strip(): end += 1
    return sum(map(len, lines[:start])), sum(map(len, lines[:end]))


def replace_header(text: str, path: str, expected: list[Notice], preserve_extras: bool = True) -> str:
    expected_owners = {notice.owner for notice in expected}
    extras = [notice for notice in parse_notices(text) if notice.owner not in expected_owners] if preserve_extras else []
    header = render_header(path, [*expected, *extras])
    span = header_span(text, path)
    if span is None:
        if text.startswith("#!"):
            end = text.find("\n") + 1
            return text[:end] + header + text[end:]
        return header + text.lstrip("\n")
    start, end = span
    return text[:start] + header + text[end:].lstrip("\n")


def notice_mismatches(actual: Iterable[Notice], expected: Iterable[Notice]) -> list[str]:
    actual_years = merge_years(actual)
    reasons: list[str] = []
    for notice in expected:
        found = actual_years.get(notice.owner, set())
        missing = set(notice.years) - found
        extra = found - set(notice.years)
        if missing:
            reasons.append(f"missing {format_years(missing)} for {display_owner(notice.owner)}")
        if extra:
            reasons.append(f"unexpected {format_years(extra)} for {display_owner(notice.owner)}")
    return reasons
