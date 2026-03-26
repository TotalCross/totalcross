// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.css;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import totalcross.ui.Insets;
import totalcross.ui.gfx.Color;
import totalcross.ui.style.model.BorderSide;
import totalcross.ui.style.model.BoxBorder;
import totalcross.ui.style.model.BoxClip;
import totalcross.ui.style.model.BoxLayout;
import totalcross.ui.style.model.BoxPaint;
import totalcross.ui.style.model.BoxShape;
import totalcross.ui.style.model.BoxStyle;
import totalcross.ui.style.model.CornerRadii;
import totalcross.ui.style.model.Elevation;
import totalcross.ui.style.model.Shadow;

/**
 * Parses a CSS-like subset into a {@link BoxStyle}.
 */
public final class BoxStyleCssParser {
    private BoxStyleCssParser() {
    }

    /**
     * Parses the given CSS-like declaration block into a box style.
     */
    public static BoxStyle parse(String css) {
        String body = extractRuleBody(css);

        BorderDecl top = new BorderDecl();
        BorderDecl right = new BorderDecl();
        BorderDecl bottom = new BorderDecl();
        BorderDecl left = new BorderDecl();

        BoxStyle defaults = new BoxStyle();
        CornerRadii radii = defaults.shape.radii;
        Insets padding = new Insets();
        int backgroundColor = defaults.paint.backgroundColor;
        int pressedColor = defaults.paint.pressedColor;
        int overflow = defaults.clip.overflow;
        Elevation elevation = defaults.elevation;

        for (String decl : splitDeclarations(body)) {
            int colon = decl.indexOf(':');
            if (colon <= 0) {
                continue;
            }

            String property = normalize(decl.substring(0, colon));
            String value = decl.substring(colon + 1).trim();
            if (value.isEmpty()) {
                continue;
            }

            switch (property) {
                case "border":
                    applyBorderShorthand(value, top, right, bottom, left);
                    break;
                case "border-top":
                    applyBorderSideShorthand(value, top);
                    break;
                case "border-right":
                    applyBorderSideShorthand(value, right);
                    break;
                case "border-bottom":
                    applyBorderSideShorthand(value, bottom);
                    break;
                case "border-left":
                    applyBorderSideShorthand(value, left);
                    break;
                case "border-width":
                    applyWidths(parse1To4BorderWidths(value), top, right, bottom, left);
                    break;
                case "border-style":
                    applyStyles(parse1To4Keywords(value), top, right, bottom, left);
                    break;
                case "border-color":
                    applyColors(parse1To4Colors(value), top, right, bottom, left);
                    break;
                case "border-top-width":
                    top.width = parseLengthDouble(value);
                    break;
                case "border-right-width":
                    right.width = parseLengthDouble(value);
                    break;
                case "border-bottom-width":
                    bottom.width = parseLengthDouble(value);
                    break;
                case "border-left-width":
                    left.width = parseLengthDouble(value);
                    break;
                case "border-top-style":
                    top.style = parseBorderStyle(value);
                    break;
                case "border-right-style":
                    right.style = parseBorderStyle(value);
                    break;
                case "border-bottom-style":
                    bottom.style = parseBorderStyle(value);
                    break;
                case "border-left-style":
                    left.style = parseBorderStyle(value);
                    break;
                case "border-top-color":
                    top.color = parseColor(value);
                    break;
                case "border-right-color":
                    right.color = parseColor(value);
                    break;
                case "border-bottom-color":
                    bottom.color = parseColor(value);
                    break;
                case "border-left-color":
                    left.color = parseColor(value);
                    break;
                case "border-radius":
                    radii = applyBorderRadius(value);
                    break;
                case "border-top-left-radius":
                    radii = CornerRadii.of(
                        parseLengthDouble(firstToken(value)),
                        parseLengthDouble(firstToken(value)),
                        radii.topRightX,
                        radii.topRightY,
                        radii.bottomRightX,
                        radii.bottomRightY,
                        radii.bottomLeftX,
                        radii.bottomLeftY
                    );
                    break;
                case "border-top-right-radius":
                    radii = CornerRadii.of(
                        radii.topLeftX,
                        radii.topLeftY,
                        parseLengthDouble(firstToken(value)),
                        parseLengthDouble(firstToken(value)),
                        radii.bottomRightX,
                        radii.bottomRightY,
                        radii.bottomLeftX,
                        radii.bottomLeftY
                    );
                    break;
                case "border-bottom-right-radius":
                    radii = CornerRadii.of(
                        radii.topLeftX,
                        radii.topLeftY,
                        radii.topRightX,
                        radii.topRightY,
                        parseLengthDouble(firstToken(value)),
                        parseLengthDouble(firstToken(value)),
                        radii.bottomLeftX,
                        radii.bottomLeftY
                    );
                    break;
                case "border-bottom-left-radius":
                    radii = CornerRadii.of(
                        radii.topLeftX,
                        radii.topLeftY,
                        radii.topRightX,
                        radii.topRightY,
                        radii.bottomRightX,
                        radii.bottomRightY,
                        parseLengthDouble(firstToken(value)),
                        parseLengthDouble(firstToken(value))
                    );
                    break;
                case "background":
                case "background-color":
                    Integer bg = tryParseBackgroundColor(value);
                    if (bg != null) {
                        backgroundColor = bg.intValue();
                    }
                    break;
                case "padding":
                    applyPadding(padding, normalize1To4Ints(parseLengths(value)));
                    break;
                case "padding-top":
                    padding.top = parseLength(value);
                    break;
                case "padding-right":
                    padding.right = parseLength(value);
                    break;
                case "padding-bottom":
                    padding.bottom = parseLength(value);
                    break;
                case "padding-left":
                    padding.left = parseLength(value);
                    break;
                case "overflow":
                    overflow = parseOverflow(value);
                    break;
                case "box-shadow":
                    elevation = parseBoxShadow(value);
                    break;
                default:
                    break;
            }
        }

        BoxShape shape = BoxShape.builder()
            .radii(radii)
            .build();
        BoxLayout layout = BoxLayout.builder()
            .padding(padding)
            .build();
        BoxClip clip = BoxClip.builder()
            .overflow(overflow)
            .build();
        BoxBorder border = BoxBorder.builder()
            .top(top.toBorderSide())
            .right(right.toBorderSide())
            .bottom(bottom.toBorderSide())
            .left(left.toBorderSide())
            .build();
        BoxPaint paint = BoxPaint.builder()
            .backgroundColor(backgroundColor)
            .pressedColor(pressedColor)
            .border(border)
            .build();

        return BoxStyle.builder()
            .layout(layout)
            .shape(shape)
            .paint(paint)
            .clip(clip)
            .elevation(elevation)
            .build();
    }

    private static void applyPadding(Insets padding, int[] values) {
        padding.top = values[0];
        padding.right = values[1];
        padding.bottom = values[2];
        padding.left = values[3];
    }

    private static String extractRuleBody(String css) {
        if (css == null) {
            return "";
        }
        String s = removeComments(css).trim();
        int open = s.indexOf('{');
        int close = s.lastIndexOf('}');
        if (open >= 0 && close > open) {
            return s.substring(open + 1, close).trim();
        }
        return s;
    }

    private static String removeComments(String s) {
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            if (i + 1 < s.length() && s.charAt(i) == '/' && s.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < s.length() && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/')) {
                    i++;
                }
                i = Math.min(i + 2, s.length());
            } else {
                out.append(s.charAt(i++));
            }
        }
        return out.toString();
    }

    private static List<String> splitDeclarations(String body) {
        List<String> list = new ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        int parenDepth = 0;

        for (int i = 0; i < body.length(); i++) {
            char ch = body.charAt(i);
            if (ch == '(') {
                parenDepth++;
            } else if (ch == ')') {
                parenDepth = Math.max(0, parenDepth - 1);
            }

            if (ch == ';' && parenDepth == 0) {
                String item = cur.toString().trim();
                if (!item.isEmpty()) {
                    list.add(item);
                }
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }

        String item = cur.toString().trim();
        if (!item.isEmpty()) {
            list.add(item);
        }
        return list;
    }

    private static String normalize(String s) {
        return s.trim().toLowerCase(/* Locale.ROOT */);
    }

    private static void applyBorderShorthand(String value, BorderDecl top, BorderDecl right, BorderDecl bottom, BorderDecl left) {
        BorderDecl d = new BorderDecl();
        applyBorderSideShorthand(value, d);
        copyDecl(d, top);
        copyDecl(d, right);
        copyDecl(d, bottom);
        copyDecl(d, left);
    }

    private static void applyBorderSideShorthand(String value, BorderDecl decl) {
        String[] tokens = tokenize(value);
        for (int i = 0; i < tokens.length; i++) {
            String t = normalize(tokens[i]);
            if (isBorderStyleToken(t)) {
                decl.style = parseBorderStyle(t);
            } else if (looksLikeLength(t)) {
                decl.width = parseLengthDouble(t);
            } else if (looksLikeColor(t)) {
                decl.color = parseColor(t);
            }
        }
    }

    private static void applyWidths(double[] values, BorderDecl top, BorderDecl right, BorderDecl bottom, BorderDecl left) {
        top.width = values[0];
        right.width = values[1];
        bottom.width = values[2];
        left.width = values[3];
    }

    private static void applyStyles(String[] values, BorderDecl top, BorderDecl right, BorderDecl bottom, BorderDecl left) {
        top.style = parseBorderStyle(values[0]);
        right.style = parseBorderStyle(values[1]);
        bottom.style = parseBorderStyle(values[2]);
        left.style = parseBorderStyle(values[3]);
    }

    private static void applyColors(int[] values, BorderDecl top, BorderDecl right, BorderDecl bottom, BorderDecl left) {
        top.color = values[0];
        right.color = values[1];
        bottom.color = values[2];
        left.color = values[3];
    }

    private static CornerRadii applyBorderRadius(String value) {
        String[] parts = value.split("/");
        double[] horizontal = normalize1To4Doubles(parseLengthsAsDouble(parts[0]));
        if (parts.length == 1) {
            return CornerRadii.of(horizontal[0], horizontal[1], horizontal[2], horizontal[3]);
        }
        double[] vertical = normalize1To4Doubles(parseLengthsAsDouble(parts[1]));
        return CornerRadii.of(
            horizontal[0], vertical[0],
            horizontal[1], vertical[1],
            horizontal[2], vertical[2],
            horizontal[3], vertical[3]
        );
    }

    private static double[] parseLengthsAsDouble(String value) {
        String[] tokens = tokenize(value);
        double[] values = new double[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            values[i] = parseLengthDouble(tokens[i]);
        }
        return values;
    }

    private static int[] parseLengths(String value) {
        String[] tokens = tokenize(value);
        int[] values = new int[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            values[i] = parseLength(tokens[i]);
        }
        return values;
    }

    private static int[] parse1To4Lengths(String value) {
        return normalize1To4Ints(parseLengths(value));
    }

    private static double[] parse1To4BorderWidths(String value) {
        return normalize1To4Doubles(parseLengthsAsDouble(value));
    }

    private static String[] parse1To4Keywords(String value) {
        return normalize1To4Strings(tokenize(value));
    }

    private static int[] parse1To4Colors(String value) {
        String[] t = tokenize(value);
        int[] v = new int[t.length];
        for (int i = 0; i < t.length; i++) {
            v[i] = parseColor(t[i]);
        }
        return normalize1To4Ints(v);
    }

    private static int[] normalize1To4Ints(int[] in) {
        if (in.length == 0) {
            return new int[] {0, 0, 0, 0};
        }
        if (in.length == 1) {
            return new int[] {in[0], in[0], in[0], in[0]};
        }
        if (in.length == 2) {
            return new int[] {in[0], in[1], in[0], in[1]};
        }
        if (in.length == 3) {
            return new int[] {in[0], in[1], in[2], in[1]};
        }
        return new int[] {in[0], in[1], in[2], in[3]};
    }

    private static double[] normalize1To4Doubles(double[] in) {
        if (in.length == 0) {
            return new double[] {0, 0, 0, 0};
        }
        if (in.length == 1) {
            return new double[] {in[0], in[0], in[0], in[0]};
        }
        if (in.length == 2) {
            return new double[] {in[0], in[1], in[0], in[1]};
        }
        if (in.length == 3) {
            return new double[] {in[0], in[1], in[2], in[1]};
        }
        return new double[] {in[0], in[1], in[2], in[3]};
    }

    private static String[] normalize1To4Strings(String[] in) {
        if (in.length == 0) {
            return new String[] {"none", "none", "none", "none"};
        }
        if (in.length == 1) {
            return new String[] {in[0], in[0], in[0], in[0]};
        }
        if (in.length == 2) {
            return new String[] {in[0], in[1], in[0], in[1]};
        }
        if (in.length == 3) {
            return new String[] {in[0], in[1], in[2], in[1]};
        }
        return new String[] {in[0], in[1], in[2], in[3]};
    }

    private static String[] tokenize(String value) {
        String s = value.trim();
        if (s.isEmpty()) {
            return new String[0];
        }
        return s.split("\\s+");
    }

    private static String firstToken(String value) {
        String[] t = tokenize(value);
        return t.length == 0 ? "0" : t[0];
    }

    private static boolean looksLikeLength(String s) {
        s = normalize(s);
        return s.endsWith("px") || isNumber(s);
    }

    private static boolean looksLikeSignedLength(String s) {
        s = normalize(s);
        if (s.endsWith("px")) {
            s = s.substring(0, s.length() - 2).trim();
        }
        return isNumber(s);
    }

    private static int parseLength(String s) {
        s = normalize(s);
        if (s.endsWith("px")) {
            s = s.substring(0, s.length() - 2).trim();
        }
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Math.max(0, (int) Math.round(Double.parseDouble(s)));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseLengthDouble(String s) {
        s = normalize(s);
        if (s.endsWith("px")) {
            s = s.substring(0, s.length() - 2).trim();
        }
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Math.max(0, Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int parseSignedLength(String s) {
        s = normalize(s);
        if (s.endsWith("px")) {
            s = s.substring(0, s.length() - 2).trim();
        }
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return (int) Math.round(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseSignedLengthDouble(String s) {
        s = normalize(s);
        if (s.endsWith("px")) {
            s = s.substring(0, s.length() - 2).trim();
        }
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isBorderStyleToken(String s) {
        s = normalize(s);
        return "none".equals(s) || "solid".equals(s) || "dashed".equals(s) || "dotted".equals(s);
    }

    private static int parseBorderStyle(String s) {
        s = normalize(s);
        if ("solid".equals(s)) {
            return BorderSide.Style.SOLID;
        }
        if ("dashed".equals(s)) {
            return BorderSide.Style.DASHED;
        }
        if ("dotted".equals(s)) {
            return BorderSide.Style.DOTTED;
        }
        return BorderSide.Style.NONE;
    }

    private static int parseOverflow(String value) {
        String normalized = normalize(value);
        if ("hidden".equals(normalized)) {
            return BoxClip.Overflow.HIDDEN;
        }
        if ("clip".equals(normalized)) {
            return BoxClip.Overflow.CLIP;
        }
        return BoxClip.Overflow.VISIBLE;
    }

    private static Elevation parseBoxShadow(String value) {
        String normalized = normalize(value);
        if (normalized.length() == 0 || "none".equals(normalized)) {
            return Elevation.NONE;
        }

        List<String> layers = splitTopLevelCommaSeparated(value);
        List<Shadow> parsed = new ArrayList<Shadow>(layers.size());
        for (int i = 0; i < layers.size(); i++) {
            Shadow shadow = parseSingleBoxShadow(layers.get(i));
            if (shadow != Shadow.NONE) {
                parsed.add(shadow);
            }
        }

        if (parsed.isEmpty()) {
            return Elevation.NONE;
        }
        return Elevation.of(parsed.toArray(new Shadow[parsed.size()]));
    }

    private static Shadow parseSingleBoxShadow(String value) {
        String[] tokens = tokenize(value);
        double[] lengths = new double[4];
        int lengthCount = 0;
        int alpha = 76;
        int color = Color.BLACK;

        for (int i = 0; i < tokens.length; i++) {
            String token = normalize(tokens[i]);
            if ("inset".equals(token)) {
                continue;
            }
            if (looksLikeColor(token)) {
                color = parseColor(token);
                alpha = parseShadowAlpha(token);
                continue;
            }
            if (looksLikeSignedLength(token) && lengthCount < lengths.length) {
                lengths[lengthCount++] = parseSignedLengthDouble(token);
            }
        }

        if (lengthCount < 2) {
            return Shadow.NONE;
        }

        double dx = lengths[0];
        double dy = lengths[1];
        double blurRadius = lengthCount >= 3 ? Math.max(0, lengths[2]) : 0;
        double spread = lengthCount >= 4 ? lengths[3] : 0;
        return Shadow.of(dx, dy, blurRadius, alpha, spread, color);
    }

    private static List<String> splitTopLevelCommaSeparated(String value) {
        List<String> items = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        int parenDepth = 0;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '(') {
                parenDepth++;
            } else if (ch == ')') {
                parenDepth = Math.max(0, parenDepth - 1);
            }

            if (ch == ',' && parenDepth == 0) {
                String item = current.toString().trim();
                if (!item.isEmpty()) {
                    items.add(item);
                }
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }

        String item = current.toString().trim();
        if (!item.isEmpty()) {
            items.add(item);
        }
        return items;
    }

    private static Integer tryParseBackgroundColor(String value) {
        String[] tokens = tokenize(value);
        for (int i = 0; i < tokens.length; i++) {
            if (looksLikeColor(tokens[i])) {
                return Integer.valueOf(parseColor(tokens[i]));
            }
        }
        return null;
    }

    private static boolean looksLikeColor(String s) {
        s = normalize(s);
        return s.startsWith("#") || s.startsWith("rgb(") || s.startsWith("rgba(") || NAMED_COLORS.containsKey(s);
    }

    private static int parseColor(String s) {
        s = normalize(s);
        if (s.startsWith("#")) {
            return parseHexColor(s);
        }
        if (s.startsWith("rgb(") && s.endsWith(")")) {
            String inner = s.substring(4, s.length() - 1).trim();
            String[] parts = inner.split(",");
            if (parts.length == 3) {
                int r = clamp255(parseInt(parts[0].trim(), 0));
                int g = clamp255(parseInt(parts[1].trim(), 0));
                int b = clamp255(parseInt(parts[2].trim(), 0));
                return (r << 16) | (g << 8) | b;
            }
        }
        if (s.startsWith("rgba(") && s.endsWith(")")) {
            String inner = s.substring(5, s.length() - 1).trim();
            String[] parts = inner.split(",");
            if (parts.length >= 3) {
                int r = clamp255(parseInt(parts[0].trim(), 0));
                int g = clamp255(parseInt(parts[1].trim(), 0));
                int b = clamp255(parseInt(parts[2].trim(), 0));
                return (r << 16) | (g << 8) | b;
            }
        }
        Integer named = NAMED_COLORS.get(s);
        return named != null ? named.intValue() : 0;
    }

    private static int parseShadowAlpha(String s) {
        s = normalize(s);
        if (s.startsWith("rgba(") && s.endsWith(")")) {
            String inner = s.substring(5, s.length() - 1).trim();
            String[] parts = inner.split(",");
            if (parts.length == 4) {
                try {
                    double alpha = Double.parseDouble(parts[3].trim());
                    return clamp255((int) Math.round(alpha * 255));
                } catch (Exception e) {
                    return 76;
                }
            }
        }
        if (s.startsWith("#")) {
            String hex = s.substring(1).trim();
            if (hex.length() == 4) {
                return Integer.parseInt("" + hex.charAt(0) + hex.charAt(0), 16);
            }
            if (hex.length() == 8) {
                return Integer.parseInt(hex.substring(0, 2), 16);
            }
        }
        return 76;
    }

    private static int parseHexColor(String s) {
        String hex = s.substring(1).trim();
        if (hex.length() == 3) {
            int r = Integer.parseInt("" + hex.charAt(0) + hex.charAt(0), 16);
            int g = Integer.parseInt("" + hex.charAt(1) + hex.charAt(1), 16);
            int b = Integer.parseInt("" + hex.charAt(2) + hex.charAt(2), 16);
            return (r << 16) | (g << 8) | b;
        }
        if (hex.length() == 6) {
            return Integer.parseInt(hex, 16);
        }
        if (hex.length() == 8) {
            return Integer.parseInt(hex.substring(2), 16);
        }
        return 0;
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static int clamp255(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    private static void copyDecl(BorderDecl src, BorderDecl dst) {
        dst.width = src.width;
        dst.style = src.style;
        dst.color = src.color;
        dst.align = src.align;
    }

    private static final class BorderDecl {
        double width = 0;
        int style = BorderSide.Style.NONE;
        int color = 0;
        int align = BorderSide.Align.INSIDE;

        BorderSide toBorderSide() {
            return BorderSide.with(width, style, color, align);
        }
    }

    private static final Map<String, Integer> NAMED_COLORS = createNamedColors();

    private static Map<String, Integer> createNamedColors() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("black", Integer.valueOf(Color.BLACK));
        map.put("white", Integer.valueOf(Color.WHITE));
        map.put("red", Integer.valueOf(Color.RED));
        map.put("green", Integer.valueOf(0x008000));
        map.put("blue", Integer.valueOf(Color.BLUE));
        map.put("yellow", Integer.valueOf(Color.YELLOW));
        map.put("orange", Integer.valueOf(0xFFA500));
        map.put("gray", Integer.valueOf(0x808080));
        map.put("grey", Integer.valueOf(0x808080));
        map.put("silver", Integer.valueOf(0xC0C0C0));
        map.put("maroon", Integer.valueOf(0x800000));
        map.put("purple", Integer.valueOf(0x800080));
        map.put("fuchsia", Integer.valueOf(0xFF00FF));
        map.put("lime", Integer.valueOf(0x00FF00));
        map.put("olive", Integer.valueOf(0x808000));
        map.put("navy", Integer.valueOf(0x000080));
        map.put("teal", Integer.valueOf(0x008080));
        map.put("aqua", Integer.valueOf(0x00FFFF));
        map.put("transparent", Integer.valueOf(0x000000));
        return map;
    }
}
