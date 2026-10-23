// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package tc.tools.deployer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Edits decoded aapt.pb.XmlNode text without disturbing unrelated manifest nodes. */
final class ManifestPermissionEditor {
  private static final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";
  private static final Set<String> OPTIONAL_PERMISSIONS = new HashSet<>();

  static {
    OPTIONAL_PERMISSIONS.add("android.permission.READ_MEDIA_IMAGES");
    OPTIONAL_PERMISSIONS.add("android.permission.READ_MEDIA_VIDEO");
    OPTIONAL_PERMISSIONS.add("android.permission.MANAGE_EXTERNAL_STORAGE");
    OPTIONAL_PERMISSIONS.add("android.permission.REQUEST_INSTALL_PACKAGES");
  }

  private static final class Block {
    final int start;
    final int open;
    final int end;

    Block(int start, int open, int end) {
      this.start = start;
      this.open = open;
      this.end = end;
    }

    String body(String text) {
      return text.substring(open + 1, end - 1);
    }
  }

  static final class PermissionMask {
    String text;
    final List<String> markers = new ArrayList<>();
    final List<String> blocks = new ArrayList<>();
  }

  static PermissionMask maskPermissionBlocks(String decoded) {
    PermissionMask mask = new PermissionMask();
    List<Block> roots = findBlocks(decoded, 0, decoded.length(), "element");
    if (roots.isEmpty()) {
      mask.text = decoded;
      return mask;
    }
    Block root = roots.get(0);
    List<Block> children = findBlocks(decoded, root.open + 1, root.end - 1, "child");
    StringBuilder masked = new StringBuilder(decoded);
    for (int i = children.size() - 1; i >= 0; i--) {
      Block child = children.get(i);
      if (permissionName(decoded, child) != null) {
        String marker = "TC_PERMISSION_BLOCK_" + mask.blocks.size() + "_";
        mask.markers.add(0, marker);
        mask.blocks.add(0, decoded.substring(child.start, child.end));
        masked.replace(child.start, child.end, "child { text: \"" + marker + "\" }");
      }
    }
    mask.text = masked.toString();
    return mask;
  }

  static String restorePermissionBlocks(String transformed, PermissionMask mask) {
    String restored = transformed;
    for (int i = 0; i < mask.markers.size(); i++) {
      restored = restored.replace("child { text: \"" + mask.markers.get(i) + "\" }", mask.blocks.get(i));
    }
    return restored;
  }

  static String edit(String decoded, Set<String> requestedPermissions) {
    List<Block> roots = findBlocks(decoded, 0, decoded.length(), "element");
    if (roots.isEmpty()) {
      throw new IllegalArgumentException("Decoded Android manifest has no aapt.pb XmlNode element.");
    }

    Block root = roots.get(0);
    List<Block> children = findBlocks(decoded, root.open + 1, root.end - 1, "child");
    Set<String> retainedNames = new HashSet<>();
    List<Block> removals = new ArrayList<>();
    for (Block child : children) {
      String name = permissionName(decoded, child);
      if (name == null) {
        continue;
      }
      if ((OPTIONAL_PERMISSIONS.contains(name) && !requestedPermissions.contains(name)) || !retainedNames.add(name)) {
        removals.add(child);
      }
    }

    StringBuilder edited = new StringBuilder(decoded);
    for (int i = removals.size() - 1; i >= 0; i--) {
      Block child = removals.get(i);
      edited.delete(child.start, child.end);
    }
    String result = edited.toString();

    List<String> missing = new ArrayList<>();
    for (String permission : requestedPermissions) {
      if (!retainedNames.contains(permission)) {
        missing.add(permission);
        retainedNames.add(permission);
      }
    }
    if (missing.isEmpty()) {
      return result;
    }

    roots = findBlocks(result, 0, result.length(), "element");
    root = roots.get(0);
    children = findBlocks(result, root.open + 1, root.end - 1, "child");
    String rootIndent = indentationAt(result, root.start);
    String childIndent = children.isEmpty() ? rootIndent + "  " : indentationAt(result, children.get(0).start);
    String nestedIndent = childIndent + "  ";
    String attributeIndent = nestedIndent + "  ";
    String compiledIndent = attributeIndent + "  ";
    String stringIndent = compiledIndent + "  ";
    int close = root.end - 1;
    int contentEnd = close;
    while (contentEnd > root.open + 1 && Character.isWhitespace(result.charAt(contentEnd - 1))) {
      contentEnd--;
    }
    String newline = result.indexOf("\r\n") >= 0 ? "\r\n" : "\n";
    StringBuilder additions = new StringBuilder();
    for (String permission : missing) {
      additions.append(newline).append(childIndent).append("child {")
          .append(newline).append(nestedIndent).append("element {")
          .append(newline).append(attributeIndent).append("namespace_uri: \"\"")
          .append(newline).append(attributeIndent).append("name: \"uses-permission\"")
          .append(newline).append(attributeIndent).append("attribute {")
          .append(newline).append(compiledIndent).append("namespace_uri: \"").append(ANDROID_NAMESPACE).append("\"")
          .append(newline).append(compiledIndent).append("name: \"name\"")
          .append(newline).append(compiledIndent).append("value: \"").append(permission).append("\"")
          .append(newline).append(compiledIndent).append("resource_id: 16842755")
          .append(newline).append(compiledIndent).append("compiled_item {")
          .append(newline).append(stringIndent).append("str {")
          .append(newline).append(stringIndent).append("  value: \"").append(permission).append("\"")
          .append(newline).append(stringIndent).append("}")
          .append(newline).append(compiledIndent).append("}")
          .append(newline).append(attributeIndent).append("}")
          .append(newline).append(nestedIndent).append("}")
          .append(newline).append(childIndent).append("}");
    }
    return result.substring(0, contentEnd) + additions + result.substring(contentEnd);
  }

  private static String permissionName(String text, Block child) {
    List<Block> elements = findBlocks(text, child.open + 1, child.end - 1, "element");
    if (elements.isEmpty()) {
      return null;
    }
    Block element = elements.get(0);
    String elementName = scalar(element.body(text), "name");
    if (elementName == null || (!"uses-permission".equals(elementName) && !elementName.startsWith("uses-permission-"))) {
      return null;
    }
    for (Block attribute : findBlocks(text, element.open + 1, element.end - 1, "attribute")) {
      if ("name".equals(scalar(attribute.body(text), "name"))) {
        String value = scalar(attribute.body(text), "value");
        if (value == null) {
          List<Block> compiled = findBlocks(text, attribute.open + 1, attribute.end - 1, "compiled_item");
          if (!compiled.isEmpty()) {
            List<Block> strings = findBlocks(text, compiled.get(0).open + 1, compiled.get(0).end - 1, "str");
            if (!strings.isEmpty()) {
              value = scalar(strings.get(0).body(text), "value");
            }
          }
        }
        return value;
      }
    }
    return null;
  }

  private static String scalar(String body, String field) {
    Pattern pattern = Pattern.compile("(?m)^\\s*" + Pattern.quote(field) + "\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
    Matcher matcher = pattern.matcher(body);
    return matcher.find() ? unescapeProtoString(matcher.group(1)) : null;
  }

  private static String unescapeProtoString(String value) {
    return value.replace("\\\\", "\\").replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r");
  }

  private static String indentationAt(String text, int position) {
    int lineStart = position;
    while (lineStart > 0 && text.charAt(lineStart - 1) != '\n' && text.charAt(lineStart - 1) != '\r') {
      lineStart--;
    }
    int end = lineStart;
    while (end < position && (text.charAt(end) == ' ' || text.charAt(end) == '\t')) {
      end++;
    }
    return text.substring(lineStart, end);
  }

  private static List<Block> findBlocks(String text, int start, int end, String field) {
    List<Block> blocks = new ArrayList<>();
    int depth = 0;
    boolean quoted = false;
    boolean escaped = false;
    for (int i = start; i < end; i++) {
      char c = text.charAt(i);
      if (quoted) {
        if (escaped) {
          escaped = false;
        } else if (c == '\\') {
          escaped = true;
        } else if (c == '"') {
          quoted = false;
        }
        continue;
      }
      if (c == '"') {
        quoted = true;
        continue;
      }
      if (depth == 0 && tokenAt(text, i, field)) {
        int open = i + field.length();
        while (open < end && Character.isWhitespace(text.charAt(open))) {
          open++;
        }
        if (open < end && text.charAt(open) == '{') {
          int close = matchingBrace(text, open, end);
          if (close >= 0) {
            blocks.add(new Block(i, open, close + 1));
            i = close;
            continue;
          }
        }
      }
      if (c == '{') {
        depth++;
      } else if (c == '}') {
        depth--;
      }
    }
    return blocks;
  }

  private static boolean tokenAt(String text, int position, String field) {
    if (!text.regionMatches(position, field, 0, field.length())) {
      return false;
    }
    if (position > 0 && !Character.isWhitespace(text.charAt(position - 1))) {
      return false;
    }
    int after = position + field.length();
    return after >= text.length() || Character.isWhitespace(text.charAt(after)) || text.charAt(after) == '{';
  }

  private static int matchingBrace(String text, int open, int end) {
    int depth = 0;
    boolean quoted = false;
    boolean escaped = false;
    for (int i = open; i < end; i++) {
      char c = text.charAt(i);
      if (quoted) {
        if (escaped) {
          escaped = false;
        } else if (c == '\\') {
          escaped = true;
        } else if (c == '"') {
          quoted = false;
        }
        continue;
      }
      if (c == '"') {
        quoted = true;
      } else if (c == '{') {
        depth++;
      } else if (c == '}' && --depth == 0) {
        return i;
      }
    }
    return -1;
  }
}
