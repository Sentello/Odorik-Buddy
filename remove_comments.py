#!/usr/bin/env python3
"""
Script to remove comments from Kotlin files (.kt)
Handles both single-line (//) and multi-line (/* */) comments
"""

import re
import os
from pathlib import Path


def remove_comments(content):
    """
    Remove both single-line and multi-line comments from Kotlin code,
    being careful not to remove comments inside string literals.
    """
    # First, identify all string literals and replace them temporarily to avoid modifying comments inside them
    temp_strings = []
    string_counter = 0
    
    # Pattern to match string literals: regular strings, character literals, and triple-quoted strings
    string_pattern = r'("""(?:[^\\]|\\.)*?(?:""")|"(?:[^"\\]|\\.)*"|\'(?:[^\'\\]|\\.)*\')'
    
    def replace_string(match):
        nonlocal string_counter
        string_val = match.group(0)
        placeholder = f"__STRING_PLACEHOLDER_{string_counter}__"
        temp_strings.append(string_val)
        string_counter += 1
        return placeholder
    
    # Temporarily replace string literals to avoid modifying comments inside strings
    content_with_placeholders = re.sub(string_pattern, replace_string, content, flags=re.DOTALL)
    
    # Remove single-line comments: // followed by any characters until end of line
    # Use MULTILINE flag so $ matches end of each line
    content_no_single_comments = re.sub(r'//.*$', '', content_with_placeholders, flags=re.MULTILINE)
    
    # Remove multi-line comments: /* ... */
    content_no_comments = re.sub(r'/\*.*?\*/', '', content_no_single_comments, flags=re.DOTALL)
    
    # Restore string literals from placeholders
    final_content = content_no_comments
    for i, string_literal in enumerate(temp_strings):
        placeholder = f"__STRING_PLACEHOLDER_{i}__"
        final_content = final_content.replace(placeholder, string_literal)
    
    return final_content


def process_kotlin_file(file_path):
    """Process a single Kotlin file to remove comments."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    new_content = remove_comments(content)
    
    # Only write if content has changed
    if new_content != content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Removed comments from {file_path}")
        return True
    else:
        print(f"No comments to remove from {file_path}")
        return False


def main():
    """Main function to process all Kotlin files in the project."""
    import sys
    if len(sys.argv) < 2:
        print("Usage: python remove_comments.py <directory>")
        sys.exit(1)
    
    directory = Path(sys.argv[1])
    
    # Find all .kt files - use rglob for recursive search
    kt_files = []
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith('.kt'):
                kt_files.append(Path(os.path.join(root, file)))
    
    processed_count = 0
    for kt_file in kt_files:
        if process_kotlin_file(kt_file):
            processed_count += 1
    
    print(f"Processing complete. Modified {processed_count} files.")


if __name__ == "__main__":
    main()