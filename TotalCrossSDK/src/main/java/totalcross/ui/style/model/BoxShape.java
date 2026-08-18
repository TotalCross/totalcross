// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.model;

import java.util.Objects;

/**
 * Describes the geometric shape of a box.
 */
public final class BoxShape {
    public final CornerRadii radii;

    /**
     * Creates a shape with zero radii.
     */
    public BoxShape() {
        this(CornerRadii.ZERO);
    }

    private BoxShape(CornerRadii radii) {
        this.radii = Objects.requireNonNull(radii, "BoxShape.radii cannot be null");
    }

    /**
     * Creates a builder for {@link BoxShape}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link BoxShape}.
     */
    public static final class Builder {
        private CornerRadii radii = CornerRadii.ZERO;

        private Builder() {
        }

        /**
         * Sets the same radius for all corners.
         */
        public Builder radius(double all) {
            radii = CornerRadii.all(all);
            return this;
        }

        /**
         * Sets one radius per corner, using the same value for x and y.
         */
        public Builder radius(double topLeft, double topRight, double bottomRight, double bottomLeft) {
            radii = CornerRadii.of(topLeft, topRight, bottomRight, bottomLeft);
            return this;
        }

        /**
         * Sets the full corner radii object.
         */
        public Builder radii(CornerRadii radii) {
            this.radii = Objects.requireNonNull(radii, "BoxShape.Builder.radii cannot be null");
            return this;
        }

        /**
         * Builds the immutable shape.
         */
        public BoxShape build() {
            return new BoxShape(radii);
        }
    }
}
