// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.model;

import java.util.Objects;

/**
 * Aggregates all style properties used to paint and clip a box.
 */
public final class BoxStyle {
    public final BoxLayout layout;
    public final BoxShape shape;
    public final BoxPaint paint;
    public final BoxClip clip;
    public final Elevation elevation;

    /**
     * Creates a default box style.
     */
    public BoxStyle() {
        this(new BoxLayout(), new BoxShape(), new BoxPaint(), new BoxClip(), Elevation.NONE);
    }

    /**
     * Creates a box style with explicit values for all parts.
     */
    public BoxStyle(BoxLayout layout, BoxShape shape, BoxPaint paint, BoxClip clip, Elevation elevation) {
        this.layout = Objects.requireNonNull(layout, "BoxStyle.layout cannot be null");
        this.shape = Objects.requireNonNull(shape, "BoxStyle.shape cannot be null");
        this.paint = Objects.requireNonNull(paint, "BoxStyle.paint cannot be null");
        this.clip = Objects.requireNonNull(clip, "BoxStyle.clip cannot be null");
        this.elevation = Objects.requireNonNull(elevation, "BoxStyle.elevation cannot be null");
    }

    /**
     * Creates a builder for {@link BoxStyle}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link BoxStyle}.
     */
    public static final class Builder {
        private BoxLayout layout = new BoxLayout();
        private BoxShape shape = new BoxShape();
        private BoxPaint paint = new BoxPaint();
        private BoxClip clip = new BoxClip();
        private Elevation elevation = Elevation.NONE;

        private Builder() {
        }

        /**
         * Sets layout properties.
         */
        public Builder layout(BoxLayout layout) {
            this.layout = Objects.requireNonNull(layout, "BoxStyle.Builder.layout cannot be null");
            return this;
        }

        /**
         * Sets shape properties.
         */
        public Builder shape(BoxShape shape) {
            this.shape = Objects.requireNonNull(shape, "BoxStyle.Builder.shape cannot be null");
            return this;
        }

        /**
         * Sets paint properties.
         */
        public Builder paint(BoxPaint paint) {
            this.paint = Objects.requireNonNull(paint, "BoxStyle.Builder.paint cannot be null");
            return this;
        }

        /**
         * Sets clip properties.
         */
        public Builder clip(BoxClip clip) {
            this.clip = Objects.requireNonNull(clip, "BoxStyle.Builder.clip cannot be null");
            return this;
        }

        /**
         * Sets elevation properties.
         */
        public Builder elevation(Elevation elevation) {
            this.elevation = Objects.requireNonNull(elevation, "BoxStyle.Builder.elevation cannot be null");
            return this;
        }

        /**
         * Sets elevation from a list of shadows.
         */
        public Builder shadows(Shadow... shadows) {
            this.elevation = Elevation.of(shadows);
            return this;
        }

        /**
         * Builds the immutable box style.
         */
        public BoxStyle build() {
            return new BoxStyle(layout, shape, paint, clip, elevation);
        }
    }
}
