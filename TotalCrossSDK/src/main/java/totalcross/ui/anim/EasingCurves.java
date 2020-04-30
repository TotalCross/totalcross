// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.anim;

/**
 * This class provides static functions for the most common easing curves
 * used around the world and on most design guides, such as Google's
 * Material Design [1]
 * 
 * In curves are used to approach a value at maximum speed
 * Out curves are used to approach a value at zero speed
 * InOut curves mixes the in and out curves, creating a S-shaped curve
 * Elastic curves extrapolate both begin and end values a little for a
 * visually pleasant effect
 * Bounce curves arrive at the end value multiple times before settling in, 
 * creating a bouncy effect.
 * 
 * Illustrations of these curves can be seen at [2]
 * For a reference implementation, see [3]
 * 
 * All easing curves take 4 parameters: the begin and end values, the
 * animation duration and the position we want to evaluate on the curve
 * 
 * References:
 * [1] https://material.io/guidelines/motion/duration-easing.html
 * [2] http://easings.net
 * [3] https://github.com/daimajia/AnimationEasingFunctions
 */
public final class EasingCurves {
   /**
    * Quadratic In Curve.
    * @param begin starting value
    * @param end ending value
    * @param duration duration
    * @param t current time (0 <= t < duration)
    * @return
    */
   public static double InQuadratic(double begin, double end, double duration, double t) {
      return end*(t/=duration)*t + begin;
  }
   
   /**
    * Quadratic Out Curve.
    * @param begin starting value
    * @param end ending value
    * @param duration duration
    * @param t current time (0 <= t < duration)
    * @return
    */
   public static double OutQuadratic(double begin, double end, double duration, double t) {
      return -end *(t/=duration)*(t-2) + begin;
   }
   
   /**
    * Quadratic InOut Curve.
    * @param begin starting value
    * @param end ending value
    * @param duration duration
    * @param t current time (0 <= t < duration)
    * @return
    */
   public static double InOutQuadratic(double begin, double end, double duration, double t) {
      return -end *(t/=duration)*(t-2) + begin;
  }
}
