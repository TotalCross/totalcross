// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.model;

import java.util.Objects;

import totalcross.ui.Insets;

/**
 * Describes the border of a box, including all four sides.
 */
public final class BoxBorder {
    public final BorderSide top;
    public final BorderSide right;
    public final BorderSide bottom;
    public final BorderSide left;

    private BoxBorder(
            BorderSide top,
            BorderSide right,
            BorderSide bottom,
            BorderSide left) {
        this.top = Objects.requireNonNull(top, "BoxBorder.top cannot be null");
        this.right = Objects.requireNonNull(right, "BoxBorder.right cannot be null");
        this.bottom = Objects.requireNonNull(bottom, "BoxBorder.bottom cannot be null");
        this.left = Objects.requireNonNull(left, "BoxBorder.left cannot be null");
    }

    /**
     * Creates a builder for a box border.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the border widths as insets.
     */
    public Insets getInsets() {
        return new Insets(
                (int) Math.round(top.width),
                (int) Math.round(left.width),
                (int) Math.round(bottom.width),
                (int) Math.round(right.width));
    }

    /**
     * Returns whether at least one side should be painted.
     */
    public boolean isVisible() {
        return top.isVisible() || right.isVisible()
                || bottom.isVisible() || left.isVisible();
    }

    /**
     * Builder for {@link BoxBorder}.
     */
    public static final class Builder {
        private BorderSide top = BorderSide.NONE;
        private BorderSide right = BorderSide.NONE;
        private BorderSide bottom = BorderSide.NONE;
        private BorderSide left = BorderSide.NONE;

        private Builder() {
        }

        /**
         * Sets all sides to the same border side.
         */
        public Builder all(BorderSide side) {
            return top(side)
                    .right(side)
                    .bottom(side)
                    .left(side);
        }

        /**
         * Sets the top border side.
         */
        public Builder top(BorderSide top) {
            this.top = Objects.requireNonNull(top, "BoxBorder.Builder.top cannot be null");
            return this;
        }

        /**
         * Sets the right border side.
         */
        public Builder right(BorderSide right) {
            this.right = Objects.requireNonNull(right, "BoxBorder.Builder.right cannot be null");
            return this;
        }

        /**
         * Sets the bottom border side.
         */
        public Builder bottom(BorderSide bottom) {
            this.bottom = Objects.requireNonNull(bottom, "BoxBorder.Builder.bottom cannot be null");
            return this;
        }

        /**
         * Sets the left border side.
         */
        public Builder left(BorderSide left) {
            this.left = Objects.requireNonNull(left, "BoxBorder.Builder.left cannot be null");
            return this;
        }

        /**
         * Builds the immutable border.
         */
        public BoxBorder build() {
            return new BoxBorder(top, right, bottom, left);
        }
    }
}
