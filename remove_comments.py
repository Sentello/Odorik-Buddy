#!/usr/bin/env python3
"""
Remove comments from Kotlin files (.kt)

Handles single-line (//) and nested multi-line (/* /* */ */) comments with a
single left-to-right scan that tracks lexical state, so quotes inside comments
and comment markers inside strings can never be confused for one another.
"""

import argparse
import logging
import os
import re
import shutil
import sys
import tempfile
from collections import Counter
from pathlib import Path
from typing import Dict, List, Set, Tuple

# Directories that hold generated or tooling output rather than real sources.
DEFAULT_EXCLUDE_DIRS = ("build", ".gradle", ".idea", "out", "generated")

# A character literal: 'a', '\n', 'A'. Matched only at an opening quote.
CHAR_LITERAL = re.compile(r"'(?:\\(?:u[0-9a-fA-F]{4}|.)|[^'\\\n])'")


class UnterminatedComment(ValueError):
    """Raised when a block comment runs off the end of the file."""


def skip_block_comment(text: str, start: int) -> int:
    """Return the index just past the block comment at *start*, honouring nesting."""
    depth = 0
    i = start
    n = len(text)
    while i < n:
        if text.startswith("/*", i):
            depth += 1
            i += 2
        elif text.startswith("*/", i):
            depth -= 1
            i += 2
            if depth == 0:
                return i
        else:
            i += 1
    raise UnterminatedComment(f"unterminated block comment at offset {start}")


def scan(content: str) -> Tuple[str, Set[int]]:
    """
    Strip comments in one pass.

    Returns the stripped text and the set of line numbers (indexed into that
    text) a comment was actually removed from, so only those lines get tidied
    up afterwards.
    """
    out: List[str] = []
    dirty: Set[int] = set()
    line = 0
    i = 0
    n = len(content)

    mode = "code"
    depth = 0                              # brace depth inside a ${...} expression
    stack: List[Tuple[str, int]] = []      # (mode, depth) saved when entering ${...}

    def emit(chunk: str) -> None:
        nonlocal line
        out.append(chunk)
        line += chunk.count("\n")

    while i < n:
        ch = content[i]

        if mode == "code":
            if content.startswith("//", i):
                end = content.find("\n", i)
                dirty.add(line)
                i = n if end == -1 else end    # leave the newline itself in place
                continue
            if content.startswith("/*", i):
                dirty.add(line)
                i = skip_block_comment(content, i)
                continue
            if content.startswith('"""', i):
                emit('"""')
                i += 3
                mode = "raw"
                continue
            if ch == '"':
                emit(ch)
                i += 1
                mode = "str"
                continue
            if ch == "'":
                # A lone apostrophe is emitted verbatim rather than starting a
                # literal that would swallow everything up to the next quote.
                match = CHAR_LITERAL.match(content, i)
                emit(match.group(0) if match else ch)
                i += len(match.group(0)) if match else 1
                continue
            if stack and ch == "{":
                depth += 1
            elif stack and ch == "}":
                if depth == 0:
                    emit(ch)
                    i += 1
                    mode, depth = stack.pop()
                    continue
                depth -= 1
            emit(ch)
            i += 1
            continue

        if mode == "str":
            if ch == "\\" and i + 1 < n:
                emit(content[i:i + 2])
                i += 2
                continue
            if content.startswith("${", i):
                emit("${")
                i += 2
                stack.append((mode, depth))
                mode, depth = "code", 0
                continue
            if ch == '"':
                emit(ch)
                i += 1
                mode = "code"
                continue
            emit(ch)
            i += 1
            # A regular string cannot span lines; bail out at a newline so one
            # stray quote cannot mask the rest of the file.
            if ch == "\n":
                mode = "code"
            continue

        # mode == "raw": no escapes, closed by a run of three or more quotes.
        if content.startswith("${", i):
            emit("${")
            i += 2
            stack.append((mode, depth))
            mode, depth = "code", 0
            continue
        if ch == '"':
            j = i
            while j < n and content[j] == '"':
                j += 1
            emit(content[i:j])
            if j - i >= 3:
                mode = "code"
            i = j
            continue
        emit(ch)
        i += 1

    return "".join(out), dirty


def remove_comments(content: str, keep_blank_lines: bool = False) -> str:
    """Remove comments from Kotlin code, leaving strings and blank lines alone."""
    stripped, dirty = scan(content)
    lines = stripped.split("\n")
    kept: List[str] = []
    for index, text in enumerate(lines):
        if index in dirty:
            text = text.rstrip()
            # Drop a line a comment vacated entirely; blank lines that were
            # already there are untouched.
            if not text and not keep_blank_lines:
                continue
        kept.append(text)
    return "\n".join(kept)


def atomic_write(path: Path, text: str) -> None:
    """Write through a sibling temp file so a crash cannot truncate the original."""
    fd, tmp_name = tempfile.mkstemp(dir=str(path.parent), prefix=path.name, suffix=".tmp")
    tmp = Path(tmp_name)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as handle:
            handle.write(text)
        shutil.copymode(str(path), str(tmp))
        os.replace(str(tmp), str(path))
    except BaseException:
        tmp.unlink()
        raise


def process_kotlin_file(file_path: Path, dry_run: bool = False,
                        keep_blank_lines: bool = False) -> str:
    """Process a single Kotlin file. Returns 'modified', 'unchanged' or 'error'."""
    try:
        content = file_path.read_text(encoding="utf-8")
    except (OSError, UnicodeDecodeError) as e:
        logging.error("Failed to read %s: %s", file_path, e)
        return "error"

    try:
        new_content = remove_comments(content, keep_blank_lines)
    except UnterminatedComment as e:
        logging.error("Skipping %s: %s", file_path, e)
        return "error"

    if new_content == content:
        logging.debug("No comments to remove from %s", file_path)
        return "unchanged"

    if dry_run:
        logging.info("Would remove comments from %s", file_path)
        return "modified"

    try:
        atomic_write(file_path, new_content)
    except OSError as e:
        logging.error("Failed to write %s: %s", file_path, e)
        return "error"

    logging.info("Removed comments from %s", file_path)
    return "modified"


def find_kotlin_files(directory: Path, excluded: Set[str]) -> List[Path]:
    """Find .kt files recursively, skipping excluded directories."""
    found = []
    for path in directory.rglob("*.kt"):
        parents = set(path.parent.relative_to(directory).parts)
        if not parents & excluded:
            found.append(path)
    return sorted(found)


def main() -> int:
    """Process all Kotlin files in the given directory. Returns an exit code."""
    parser = argparse.ArgumentParser(description="Remove comments from Kotlin (.kt) files")
    parser.add_argument("directory", type=Path, help="Directory containing Kotlin files to process")
    parser.add_argument("-n", "--dry-run", action="store_true",
                        help="Report what would change without writing anything")
    parser.add_argument("-v", "--verbose", action="store_true", help="Enable verbose output")
    parser.add_argument("--exclude", action="append", metavar="DIR",
                        help="Directory name to skip; repeat to override the defaults "
                             f"({' '.join(DEFAULT_EXCLUDE_DIRS)})")
    parser.add_argument("--keep-blank-lines", action="store_true",
                        help="Keep the blank line left behind by a whole-line comment")

    args = parser.parse_args()

    log_level = logging.DEBUG if args.verbose else logging.INFO
    logging.basicConfig(level=log_level, format="%(message)s")

    if not args.directory.is_dir():
        logging.error("Error: Directory '%s' does not exist or is not a directory.", args.directory)
        return 1

    excluded = set(args.exclude) if args.exclude is not None else set(DEFAULT_EXCLUDE_DIRS)

    logging.info("Searching for .kt files in %s...", args.directory)
    kt_files = find_kotlin_files(args.directory, excluded)

    if not kt_files:
        logging.info("No Kotlin files found in the specified directory.")
        return 0

    counts: Dict[str, int] = Counter(
        process_kotlin_file(f, args.dry_run, args.keep_blank_lines) for f in kt_files
    )

    verb = "Would modify" if args.dry_run else "Modified"
    logging.info("Processing complete. %s %d out of %d files.",
                 verb, counts["modified"], len(kt_files))

    if counts["error"]:
        logging.error("%d file(s) could not be processed; see messages above.", counts["error"])
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
