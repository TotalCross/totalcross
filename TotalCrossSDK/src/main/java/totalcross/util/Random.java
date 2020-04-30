// Copyright (C) 2001 Sean Luke <seanl@cs.umd.edu>
// Copyright (C) 2001-2013 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.util;

/** This is a simple Linear Congruential Generator which
    produces random numbers in the range [0,2^31), derived
    from ran0 in Numerical Recipies.  Note that ran0 isn't
    wonderfully random -- there are much better generators
    out there -- but it'll do the job, and it's fast and
    has low memory consumption.
    <p>
    Here's a sample of how to use the Random class:
    <pre>
    Random r = new Random(0x1234); // the use of the same key is sometimes desirable
    for (...)
    {
       int nextRandomInt = r.between(0,10);
       char nextRandomChar = r.between('a','z');
       ...
    }
    </pre>  
 */

public class Random {
  // Do NOT modify IA, IM, IQ, or IR
  // You are free to set ZERO_SEED_REPLACEMENT to any integer
  // greater than 0 (but NOT including 0!!)
  private static final int IA = 16807;
  private static final int IM = 2147483647;
  private static final int IQ = 127773;
  private static final int IR = 2836;
  private static final int ZERO_SEED_REPLACEMENT = 234123; // whatever

  private int seed, seed0;

  /** The only reasonable seeds are between 0 and 2^31 inclusive; if
       you're negative, it'll get the absolute value of it. */
  public Random(int _seed) {
    seed0 = _seed;
    // strip out the minus-sign if any
    seed = (_seed << 1) >>> 1;
    // if seed is 0, it's invalid, we need another seed
    if (seed == 0) {
      seed = ZERO_SEED_REPLACEMENT;
    }
  }

  /** Randomizes based on the current timestamp */
  public Random() {
    totalcross.sys.Time t = new totalcross.sys.Time(); // guich@300_30
    seed0 = seed = t.hour * 60 * 60 + t.minute * 60 + t.second + 100 * t.millis; // hope this value never gets < 0 !
  }

  /** Bits should be <= 31 */
  protected int next(final int bits) {
    int k = seed / IQ;
    seed = IA * (seed - k * IQ) - IR * k;
    if (seed < 0) {
      seed += IM;
    }
    return seed >> (31 - bits);
  }

  /** Returns a double floating-point value in the half-open range [0,1). */
  public double nextDouble() {
    return next(24) / (double) (1 << 24);
  }

  /** Returns an integer in the range [0,n).
   * n must be > 0, else -1 is returned.
   * @see #between(char, char)
   * @see #between(int, int)
   */
  public int nextInt(int n) {
    if (n <= 0) {
      return -1;
    }
    int bits, val;
    do {
      bits = next(31);
      val = bits % n;
    } while (bits - val + (n - 1) < 0);
    return val;
  }

  // guich@200b4_3
  /** Returns a random character between the given region.
   * Eg: between('a', 'e') returns a random character between 'a' and 'e'.
   * @param s the start character in the range
   * @param e the ending character in the range
   */
  public char between(char s, char e) {
    return (char) between((int) (s), (int) (e));
  }

  // guich@200b4_3
  /** Returns random integer in the given region.
   * Eg: rand(1, 10) returns a random integer in the range of 1 to 10, inclusive.
   * Note that if s == e, it will return in the range s and e+1.
   * @param s the start number in the range
   * @param e the ending number in the range
   */
  public int between(int s, int e) {
    if (s > e) {
      int t = e;
      e = s;
      s = t;
    } else if (s == e) {
      e++;
    }
    return s + nextInt(e - s + 1);
  }

  /** Returns the first seed used */
  public int getSeed() {
    return seed0;
  }
}