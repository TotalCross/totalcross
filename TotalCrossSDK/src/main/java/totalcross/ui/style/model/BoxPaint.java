// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.model;

import java.util.Objects;

/**
 * Describes paint properties for a box, including background and border.
 */
public final class BoxPaint {
    public final int backgroundColor;
    public final int pressedColor;
    public final BoxBorder border;

    /**
     * Creates a paint configuration with no background and no visible border.
     */
    public BoxPaint() {
        this(0, 0, BoxBorder.builder().all(BorderSide.NONE).build());
    }

    private BoxPaint(int backgroundColor, int pressedColor, BoxBorder border) {
        this.backgroundColor = backgroundColor;
        this.pressedColor = pressedColor;
        this.border = Objects.requireNonNull(border, "BoxPaint.border cannot be null");
    }

    /**
     * Creates a builder for {@link BoxPaint}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link BoxPaint}.
     */
    public static final class Builder {
        private int backgroundColor;
        private int pressedColor;
        private BoxBorder border = BoxBorder.builder().all(BorderSide.NONE).build();

        private Builder() {
        }

        /**
         * Sets the normal background color.
         */
        public Builder backgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * Sets the pressed-state background color.
         */
        public Builder pressedColor(int pressedColor) {
            this.pressedColor = pressedColor;
            return this;
        }

        /**
         * Sets the border configuration.
         */
        public Builder border(BoxBorder border) {
            this.border = Objects.requireNonNull(border, "BoxPaint.Builder.border cannot be null");
            return this;
        }

        /**
         * Builds the immutable paint configuration.
         */
        public BoxPaint build() {
            return new BoxPaint(backgroundColor, pressedColor, border);
        }
    }
}
