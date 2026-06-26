// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.style.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the elevation of a box as one or more shadow layers.
 */
public final class Elevation {
    /** Constant for no elevation. */
    public static final Elevation NONE = new Elevation(new Shadow[] {Shadow.NONE});

    public final Shadow[] shadows;
    public final double outset;

    private Elevation(Shadow[] shadows) {
        this.shadows = copyShadows(shadows);
        this.outset = computeOutset(this.shadows);
    }

    /**
     * Creates an elevation from explicit shadow layers.
     */
    public static Elevation of(Shadow... shadows) {
        return new Elevation(shadows);
    }

    /**
     * Returns a preset elevation for the given elevation level.
     */
    public static Elevation of(int elevation) {
        switch (elevation) {
            case 0:
                return NONE;
            case 1:
                return of(
                    Shadow.of(0, 1, 2f, 18, 0),
                    Shadow.of(0, 1, 3f, 10, 0)
                );
            case 2:
                return of(
                    Shadow.of(0, 2, 4f, 18, 0),
                    Shadow.of(0, 1, 2f, 12, 0)
                );
            case 3:
                return of(
                    Shadow.of(0, 3, 6f, 18, 0),
                    Shadow.of(0, 1, 3f, 12, 0)
                );
            case 4:
                return of(
                    Shadow.of(0, 2, 4f, 46, 0),
                    Shadow.of(0, 3, 8f, 31, 0)
                );
            case 6:
                return of(
                    Shadow.of(0, 4, 8f, 42, 0),
                    Shadow.of(0, 6, 16f, 24, 0)
                );
            case 8:
                return of(
                    Shadow.of(0, 6, 12f, 40, 0),
                    Shadow.of(0, 8, 20f, 22, 0)
                );
            default:
                return of(
                    Shadow.of(0, elevation, elevation * 2f, 48, 0),
                    Shadow.of(0, Math.max(1, elevation / 2), Math.max(1f, elevation * 1.25f), 24, 0)
                );
        }
    }

    private static Shadow[] copyShadows(Shadow[] shadows) {
        Objects.requireNonNull(shadows, "Elevation.shadows cannot be null");
        if (shadows.length == 0) {
            return new Shadow[] {Shadow.NONE};
        }

        Shadow[] copy = Arrays.copyOf(shadows, shadows.length);
        for (int i = 0; i < copy.length; i++) {
            copy[i] = Objects.requireNonNull(copy[i], "Elevation.shadows[" + i + "] cannot be null");
        }
        return copy;
    }

    private static double computeOutset(Shadow[] shadows) {
        double expand = 0;
        for (int i = 0; i < shadows.length; i++) {
            Shadow shadow = shadows[i];
            if (shadow == null || shadow == Shadow.NONE || shadow.alpha <= 0) {
                continue;
            }
            expand = Math.max(
                expand,
                Math.max(Math.abs(shadow.dx), Math.abs(shadow.dy))
                    + Math.max(0d, shadow.spread)
                    + Math.max(0d, shadow.blurRadius));
        }
        return expand;
    }
}
