// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.model;

import java.util.Objects;

import totalcross.ui.Insets;

/**
 * Describes box layout properties such as padding.
 */
public final class BoxLayout {
    public final Insets padding;

    /**
     * Creates a layout with zero padding.
     */
    public BoxLayout() {
        this(new Insets());
    }

    private BoxLayout(Insets padding) {
        Objects.requireNonNull(padding, "BoxLayout.padding cannot be null");
        validateNonNegative("top", padding.top);
        validateNonNegative("right", padding.right);
        validateNonNegative("bottom", padding.bottom);
        validateNonNegative("left", padding.left);
        this.padding = new Insets(padding.top, padding.left, padding.bottom, padding.right);
    }

    /**
     * Creates a builder for {@link BoxLayout}.
     */
    public static Builder builder() {
        return new Builder();
    }

    private static void validateNonNegative(String edge, int value) {
        if (value < 0) {
            throw new IllegalArgumentException("BoxLayout padding " + edge + " must be >= 0: " + value);
        }
    }

    /**
     * Builder for {@link BoxLayout}.
     */
    public static final class Builder {
        private Insets padding = new Insets();

        private Builder() {
        }

        /**
         * Sets the same padding on all sides.
         */
        public Builder padding(int all) {
            return padding(all, all, all, all);
        }

        /**
         * Sets vertical and horizontal padding.
         */
        public Builder padding(int vertical, int horizontal) {
            return padding(vertical, horizontal, vertical, horizontal);
        }

        /**
         * Sets padding for each side.
         */
        public Builder padding(int top, int right, int bottom, int left) {
            validateNonNegative("top", top);
            validateNonNegative("right", right);
            validateNonNegative("bottom", bottom);
            validateNonNegative("left", left);
            padding = new Insets(top, left, bottom, right);
            return this;
        }

        /**
         * Copies padding from an existing {@link Insets} instance.
         */
        public Builder padding(Insets padding) {
            this.padding = Objects.requireNonNull(padding, "BoxLayout.Builder.padding cannot be null");
            validateNonNegative("top", padding.top);
            validateNonNegative("right", padding.right);
            validateNonNegative("bottom", padding.bottom);
            validateNonNegative("left", padding.left);
            return this;
        }

        /**
         * Builds the immutable layout object.
         */
        public BoxLayout build() {
            return new BoxLayout(padding);
        }
    }
}
