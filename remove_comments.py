#!/usr/bin/env python3
"""
Script to remove comments from Kotlin files (.kt)
Handles both single-line (//) and multi-line (/* */) comments
"""

import re
import logging
import argparse
from pathlib import Path


def remove_comments(content: str) -> str:
    """
    Remove both single-line and multi-line comments from Kotlin code,
    being careful not to remove comments inside string literals.
    """
    temp_strings = []
    string_counter = 0
    
    # Pattern to match string literals: regular strings, character literals, and triple-quoted strings
    # We use (?:.|\n) for raw strings to match across multiple lines without enabling DOTALL globally
    string_pattern = r'("""(?:.|\n)*?"""|"(?:[^"\\]|\\.)*"|\'(?:[^\'\\]|\\.)*\')'
    
    def replace_string(match: re.Match) -> str:
        nonlocal string_counter
        string_val = match.group(0)
        placeholder = f"__STRING_PLACEHOLDER_{string_counter}__"
        temp_strings.append(string_val)
        string_counter += 1
        return placeholder
    
    # Temporarily replace string literals to avoid modifying comments inside strings
    content_with_placeholders = re.sub(string_pattern, replace_string, content)
    
    # Remove single-line comments: // followed by any characters until end of line
    content_no_single_comments = re.sub(r'//.*$', '', content_with_placeholders, flags=re.MULTILINE)
    
    # Remove multi-line comments: /* ... */
    # Note: This will not properly handle nested Kotlin block comments like /* /* ... */ */
    content_no_comments = re.sub(r'/\*.*?\*/', '', content_no_single_comments, flags=re.DOTALL)
    
    # Clean up trailing spaces left on lines where inline comments were removed
    content_cleaned = re.sub(r'[ \t]+$', '', content_no_comments, flags=re.MULTILINE)
    
    # Restore string literals from placeholders
    final_content = content_cleaned
    for i, string_literal in enumerate(temp_strings):
        placeholder = f"__STRING_PLACEHOLDER_{i}__"
        final_content = final_content.replace(placeholder, string_literal)
    
    return final_content


def process_kotlin_file(file_path: Path) -> bool:
    """Process a single Kotlin file to remove comments."""
    try:
        content = file_path.read_text(encoding='utf-8')
        new_content = remove_comments(content)
        
        # Only write if content has changed
        if new_content != content:
            file_path.write_text(new_content, encoding='utf-8')
            logging.info(f"Removed comments from {file_path}")
            return True
        else:
            logging.debug(f"No comments to remove from {file_path}")
            return False
            
    except Exception as e:
        logging.error(f"Failed to process {file_path}: {e}")
        return False


def main() -> None:
    """Main function to process all Kotlin files in the project."""
    parser = argparse.ArgumentParser(description="Remove comments from Kotlin (.kt) files")
    parser.add_argument("directory", type=Path, help="Directory containing Kotlin files to process")
    parser.add_argument("-v", "--verbose", action="store_true", help="Enable verbose output")
    
    args = parser.parse_args()
    
    # Configure logging
    log_level = logging.DEBUG if args.verbose else logging.INFO
    logging.basicConfig(level=log_level, format='%(message)s')
    
    if not args.directory.exists() or not args.directory.is_dir():
        logging.error(f"Error: Directory '{args.directory}' does not exist or is not a directory.")
        return
    
    logging.info(f"Searching for .kt files in {args.directory}...")
    
    # Find all .kt files - use rglob for recursive search
    kt_files = list(args.directory.rglob('*.kt'))
    
    if not kt_files:
        logging.info("No Kotlin files found in the specified directory.")
        return
        
    processed_count = 0
    for kt_file in kt_files:
        if process_kotlin_file(kt_file):
            processed_count += 1
    
    logging.info(f"Processing complete. Modified {processed_count} out of {len(kt_files)} files.")


if __name__ == "__main__":
    main()