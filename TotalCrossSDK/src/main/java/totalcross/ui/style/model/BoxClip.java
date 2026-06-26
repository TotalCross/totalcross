// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.model;

/**
 * Describes overflow clipping behavior for a box.
 */
public final class BoxClip {
    /**
     * Supported overflow modes.
     */
    public static final class Overflow {
        public static final int VISIBLE = 0;
        public static final int HIDDEN = 1;
        public static final int CLIP = 2;

        private Overflow() {
        }
    }

    public final int overflow;

    /**
     * Creates a clip configuration with visible overflow.
     */
    public BoxClip() {
        this(Overflow.VISIBLE);
    }

    private BoxClip(int overflow) {
        validateOverflow(overflow);
        this.overflow = overflow;
    }

    /**
     * Creates a builder for a clip configuration.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns whether overflow should be clipped.
     */
    public boolean shouldClip() {
        return overflow == Overflow.HIDDEN || overflow == Overflow.CLIP;
    }

    /**
     * Returns whether the given value is a valid overflow mode.
     */
    public static boolean isValidOverflow(int overflow) {
        return overflow == Overflow.VISIBLE
            || overflow == Overflow.HIDDEN
            || overflow == Overflow.CLIP;
    }

    private static void validateOverflow(int overflow) {
        if (!isValidOverflow(overflow)) {
            throw new IllegalArgumentException("Invalid BoxClip overflow: " + overflow);
        }
    }

    /**
     * Builder for {@link BoxClip}.
     */
    public static final class Builder {
        private int overflow = Overflow.VISIBLE;

        private Builder() {
        }

        /**
         * Sets the overflow mode.
         */
        public Builder overflow(int overflow) {
            validateOverflow(overflow);
            this.overflow = overflow;
            return this;
        }

        /**
         * Builds the immutable clip configuration.
         */
        public BoxClip build() {
            return new BoxClip(overflow);
        }
    }
}
