/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

// $Id: Deflater4B.java,v 1.4 2011-01-04 13:19:08 guich Exp $

/* Deflater.java - Compress a data stream
   Copyright (C) 1999, 2000, 2001, 2004 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package totalcross.util.zip;

import totalcross.sys.Vm;

/**
 * This is the Deflater class. The deflater class compresses input with the deflate algorithm described in RFC 1951. It
 * has several compression levels and three different strategies described below.
 * 
 * This class is <i>not</i> thread safe. This is inherent in the API, due to the split of deflate and setInput.
 * 
 * Changes for TotalCross:<br>
 * Replaced DeflaterPending by PendingBuffer, so we could remove the class DeflaterPending.<br>
 * System.arraycopy replaced by Vm.arrayCopy.<br>
 * Merged DeflaterEngine with Deflater for better performance.<br>
 * Disabled debugging code.<br>
 * Replaced InternalError by Error, because TotalCross does not have java.lang.InternalError.<br>
 * 
 * @author Jochen Hoenicke
 * @author Tom Tromey
 */
public class Deflater4B implements DeflaterConstants4B
{
   /**
    * The best and slowest compression level. This tries to find very long and distant string repetitions.
    */
   public static final int BEST_COMPRESSION = 9;
   /**
    * The worst but fastest compression level.
    */
   public static final int BEST_SPEED = 1;
   /**
    * The default compression level.
    */
   public static final int DEFAULT_COMPRESSION = -1;
   /**
    * This level won't compress at all but output uncompressed blocks.
    */
   public static final int NO_COMPRESSION = 0;

   /**
    * The default strategy.
    */
   public static final int DEFAULT_STRATEGY = 0;
   /**
    * This strategy will only allow longer string repetitions. It is useful for random data with a small character set.
    */
   public static final int FILTERED = 1;

   /**
    * This strategy will not look for string repetitions at all. It only encodes with Huffman trees (which means, that
    * more common characters get a smaller encoding.
    */
   public static final int HUFFMAN_ONLY = 2;

   /**
    * The compression method. This is the only method supported so far. There is no need to use this constant at all.
    */
   public static final int DEFLATED = 8;

   /*
    * The Deflater can do the following state transitions:
    *
    * (1) -> INIT_STATE   ----> INIT_FINISHING_STATE ---.
    *        /  | (2)      (5)                         |
    *       /   v          (5)                         |
    *   (3)| SETDICT_STATE ---> SETDICT_FINISHING_STATE |(3)
    *       \   | (3)                 |        ,-------'
    *        |  |                     | (3)   /
    *        v  v          (5)        v      v
    * (1) -> BUSY_STATE   ----> FINISHING_STATE
    *                                | (6)
    *                                v
    *                           FINISHED_STATE
    *    \_____________________________________/
    *          | (7)
    *          v
    *        CLOSED_STATE
    *
    * (1) If we should produce a header we start in INIT_STATE, otherwise
    *     we start in BUSY_STATE.
    * (2) A dictionary may be set only when we are in INIT_STATE, then
    *     we change the state as indicated.
    * (3) Whether a dictionary is set or not, on the first call of deflate
    *     we change to BUSY_STATE.
    * (4) -- intentionally left blank -- :)
    * (5) FINISHING_STATE is entered, when flush() is called to indicate that
    *     there is no more INPUT.  There are also states indicating, that
    *     the header wasn't written yet.
    * (6) FINISHED_STATE is entered, when everything has been flushed to the
    *     internal pending output buffer.
    * (7) At any time (7)
    * 
    */

   private static final int IS_SETDICT = 0x01;
   private static final int IS_FLUSHING = 0x04;
   private static final int IS_FINISHING = 0x08;

   private static final int INIT_STATE = 0x00;
   private static final int SETDICT_STATE = 0x01;
   //private static final int INIT_FINISHING_STATE = 0x08;
   //private static final int SETDICT_FINISHING_STATE = 0x09;
   private static final int BUSY_STATE = 0x10;
   private static final int FLUSHING_STATE = 0x14;
   private static final int FINISHING_STATE = 0x1c;
   private static final int FINISHED_STATE = 0x1e;
   private static final int CLOSED_STATE = 0x7f;

   /** Compression level. */
   private int level;

   /** should we include a header. */
   private boolean noHeader;

   /** The current state. */
   private int state;

   /** The total bytes of output written. */
   private int totalOut;

   /** The pending output. */
   private PendingBuffer4B pending;

   //   /** The deflater engine. */
   //   private DeflaterEngine engine;

   /**
    * Creates a new deflater with default compression level.
    */
   public Deflater4B()
   {
      this(DEFAULT_COMPRESSION, false);
   }

   /**
    * Creates a new deflater with given compression level.
    * 
    * @param lvl
    *           the compression level, a value between NO_COMPRESSION and BEST_COMPRESSION, or DEFAULT_COMPRESSION.
    * @exception IllegalArgumentException
    *               if lvl is out of range.
    */
   public Deflater4B(int lvl)
   {
      this(lvl, false);
   }

   /**
    * Creates a new deflater with given compression level.
    * 
    * @param lvl
    *           the compression level, a value between NO_COMPRESSION and BEST_COMPRESSION.
    * @param nowrap
    *           true, iff we should suppress the deflate header at the beginning and the adler checksum at the end of
    *           the output. This is useful for the GZIP format.
    * @exception IllegalArgumentException
    *               if lvl is out of range.
    */
   public Deflater4B(int lvl, boolean nowrap)
   {
      if (lvl == DEFAULT_COMPRESSION)
         lvl = 6;
      else if (lvl < NO_COMPRESSION || lvl > BEST_COMPRESSION)
         throw new IllegalArgumentException();

      pending = new PendingBuffer4B(DeflaterConstants4B.PENDING_BUF_SIZE);

      //    engine = new DeflaterEngine(pending);
      //    DeflaterEngine(DeflaterPending pending) 
      {
         // inocuous code:
         //         this.pending = pending;
         huffman = new DeflaterHuffman4B(pending);
         adler = new Adler32();

         window = new byte[2 * WSIZE];
         head = new short[HASH_SIZE];
         prev = new short[WSIZE];

         /* We start at index 1, to avoid a implementation deficiency, that
          * we cannot build a repeat pattern at index 0.
          */
         blockStart = strstart = 1;
      }

      this.noHeader = nowrap;
      setStrategy(DEFAULT_STRATEGY);
      setLevel(lvl);
      reset();
   }

   /**
    * Resets the deflater. The deflater acts afterwards as if it was just created with the same compression level and
    * strategy as it had before.
    */
   public void reset()
   {
      state = (noHeader ? BUSY_STATE : INIT_STATE);
      totalOut = 0;
      pending.reset();

      //      engine.reset();
      //      public void reset()
      {
         huffman.reset();
         adler.reset();
         blockStart = strstart = 1;
         lookahead = 0;
         totalIn = 0;
         prevAvailable = false;
         matchLen = MIN_MATCH - 1;
         for (int i = 0; i < HASH_SIZE; i++)
            head[i] = 0;
         for (int i = 0; i < WSIZE; i++)
            prev[i] = 0;
      }
   }

   /**
    * Frees all objects allocated by the compressor. There's no reason to call this, since you can just rely on garbage
    * collection. Exists only for compatibility against Sun's JDK, where the compressor allocates native memory. If you
    * call any method (even reset) afterwards the behaviour is <i>undefined</i>.
    * 
    * @deprecated Just clear all references to deflater instead.
    */
   public void end()
   {
      //      engine = null;
      pending = null;
      state = CLOSED_STATE;
   }

   /**
    * Gets the current adler checksum of the data that was processed so far.
    */
   public int getAdler()
   {
      //      return engine.getAdler();
      //      public final int getAdler()
      {
         int chksum = (int) adler.getValue();
         return chksum;
      }
   }

   /**
    * Gets the number of input bytes processed so far.
    */
   public int getTotalIn()
   {
      //      return engine.getTotalIn();
      //      public final int getTotalIn()
      {
         return totalIn;
      }
   }

   /**
    * Gets the number of output bytes so far.
    */
   public int getTotalOut()
   {
      return totalOut;
   }

   /**
    * Finalizes this object.
    */
   protected void finalize()
   {
      /* Exists solely for compatibility.  We don't have any native state. */
   }

   /**
    * Flushes the current input block. Further calls to deflate() will produce enough output to inflate everything in
    * the current input block. This is not part of Sun's JDK so I have made it package private. It is used by
    * DeflaterOutputStream to implement flush().
    */
   void flush()
   {
      state |= IS_FLUSHING;
   }

   /**
    * Finishes the deflater with the current input block. It is an error to give more input after this method was
    * called. This method must be called to force all bytes to be flushed.
    */
   public void finish()
   {
      state |= IS_FLUSHING | IS_FINISHING;
   }

   /**
    * Returns true iff the stream was finished and no more output bytes are available.
    */
   public boolean finished()
   {
      return state == FINISHED_STATE && pending.isFlushed();
   }

   /**
    * Returns true, if the input buffer is empty. You should then call setInput(). <br>
    * 
    * <em>NOTE</em>: This method can also return true when the stream was finished.
    */
   public boolean needsInput()
   {
      //      return engine.needsInput();
      //      public final boolean needsInput()
      {
         return inputEnd == inputOff;
      }
   }

   /**
    * Sets the data which should be compressed next. This should be only called when needsInput indicates that more
    * input is needed. If you call setInput when needsInput() returns false, the previous input that is still pending
    * will be thrown away. The given byte array should not be changed, before needsInput() returns true again. This call
    * is equivalent to <code>setInput(input, 0, input.length)</code>.
    * 
    * @param input
    *           the buffer containing the input data.
    * @exception IllegalStateException
    *               if the buffer was finished() or ended().
    */
   public void setInput(byte[] input)
   {
      setInput(input, 0, input.length);
   }

   /**
    * Sets the data which should be compressed next. This should be only called when needsInput indicates that more
    * input is needed. The given byte array should not be changed, before needsInput() returns true again.
    * 
    * @param input
    *           the buffer containing the input data.
    * @param off
    *           the start of the data.
    * @param len
    *           the length of the data.
    * @exception IllegalStateException
    *               if the buffer was finished() or ended() or if previous input is still pending.
    */
   public void setInput(byte[] input, int off, int len)
   {
      if ((state & IS_FINISHING) != 0)
         throw new IllegalStateException("finish()/end() already called");
      //      engine.setInput(input, off, len);
      //      public void setInput(byte[] buf, int off, int len)
      {
         if (inputOff < inputEnd)
            throw new IllegalStateException("Old input was not completely processed");

         int end = off + len;

         /* We want to throw an ArrayIndexOutOfBoundsException early.  The
          * check is very tricky: it also handles integer wrap around.  
          */
         if (0 > off || off > end || end > input.length)
            throw new ArrayIndexOutOfBoundsException();

         inputBuf = input;
         inputOff = off;
         inputEnd = end;
      }
   }

   /**
    * Sets the compression level. There is no guarantee of the exact position of the change, but if you call this when
    * needsInput is true the change of compression level will occur somewhere near before the end of the so far given
    * input.
    * 
    * @param lvl
    *           the new compression level.
    */
   public void setLevel(int lvl)
   {
      if (lvl == DEFAULT_COMPRESSION)
         lvl = 6;
      else if (lvl < NO_COMPRESSION || lvl > BEST_COMPRESSION)
         throw new IllegalArgumentException();

      if (level != lvl)
      {
         level = lvl;
         //         engine.setLevel(lvl);
         //         public void setLevel(int lvl)
         {
            goodLength = DeflaterConstants4B.GOOD_LENGTH[lvl];
            max_lazy = DeflaterConstants4B.MAX_LAZY[lvl];
            niceLength = DeflaterConstants4B.NICE_LENGTH[lvl];
            max_chain = DeflaterConstants4B.MAX_CHAIN[lvl];

            if (DeflaterConstants4B.COMPR_FUNC[lvl] != comprFunc)
            {
               //               if (DeflaterConstants4B.DEBUGGING)
               //                  System.err.println("Change from " + comprFunc + " to " + DeflaterConstants4B.COMPR_FUNC[lvl]);
               switch (comprFunc)
               {
                  case DEFLATE_STORED:
                     if (strstart > blockStart)
                     {
                        huffman.flushStoredBlock(window, blockStart, strstart - blockStart, false);
                        blockStart = strstart;
                     }
                     updateHash();
                  break;
                  case DEFLATE_FAST:
                     if (strstart > blockStart)
                     {
                        huffman.flushBlock(window, blockStart, strstart - blockStart, false);
                        blockStart = strstart;
                     }
                  break;
                  case DEFLATE_SLOW:
                     if (prevAvailable)
                        huffman.tallyLit(window[strstart - 1] & 0xff);
                     if (strstart > blockStart)
                     {
                        huffman.flushBlock(window, blockStart, strstart - blockStart, false);
                        blockStart = strstart;
                     }
                     prevAvailable = false;
                     matchLen = MIN_MATCH - 1;
                  break;
               }
               comprFunc = COMPR_FUNC[lvl];
            }
         }
      }
   }

   /**
    * Sets the compression strategy. Strategy is one of DEFAULT_STRATEGY, HUFFMAN_ONLY and FILTERED. For the exact
    * position where the strategy is changed, the same as for setLevel() applies.
    * 
    * @param stgy
    *           the new compression strategy.
    */
   public void setStrategy(int stgy)
   {
      if (stgy != DEFAULT_STRATEGY && stgy != FILTERED
            && stgy != HUFFMAN_ONLY)
         throw new IllegalArgumentException();
      //      engine.setStrategy(stgy);
      //      public final void setStrategy(int strat)
      {
         strategy = stgy;
      }
   }

   /**
    * Deflates the current input block to the given array. It returns the number of bytes compressed, or 0 if either
    * needsInput() or finished() returns true or length is zero.
    * 
    * @param output
    *           the buffer where to write the compressed data.
    */
   public int deflate(byte[] output)
   {
      return deflate(output, 0, output.length);
   }

   /**
    * Deflates the current input block to the given array. It returns the number of bytes compressed, or 0 if either
    * needsInput() or finished() returns true or length is zero.
    * 
    * @param output
    *           the buffer where to write the compressed data.
    * @param offset
    *           the offset into the output array.
    * @param length
    *           the maximum number of bytes that may be written.
    * @exception IllegalStateException
    *               if end() was called.
    * @exception IndexOutOfBoundsException
    *               if offset and/or length don't match the array length.
    */
   public int deflate(byte[] output, int offset, int length)
   {
      int origLength = length;

      if (state == CLOSED_STATE)
         throw new IllegalStateException("Deflater closed");

      if (state < BUSY_STATE)
      {
         /* output header */
         int header = (DEFLATED +
               ((DeflaterConstants4B.MAX_WBITS - 8) << 4)) << 8;
         int level_flags = (level - 1) >> 1;
         if (level_flags < 0 || level_flags > 3)
            level_flags = 3;
         header |= level_flags << 6;
         if ((state & IS_SETDICT) != 0)
            /* Dictionary was set */
            header |= DeflaterConstants4B.PRESET_DICT;
         header += 31 - (header % 31);

         pending.writeShortMSB(header);
         if ((state & IS_SETDICT) != 0)
         {
            //            int chksum = engine.getAdler();
            int chksum = (int) adler.getValue();

            //            engine.resetAdler();
            //            public final void resetAdler()
            {
               adler.reset();
            }

            pending.writeShortMSB(chksum >> 16);
            pending.writeShortMSB(chksum & 0xffff);
         }

         state = BUSY_STATE | (state & (IS_FLUSHING | IS_FINISHING));
      }

      for (;;)
      {
         int count = pending.flush(output, offset, length);
         offset += count;
         totalOut += count;
         length -= count;
         if (length == 0 || state == FINISHED_STATE)
            break;

         //         if (!engine.deflate((state & IS_FLUSHING) != 0,
         if (!engineDeflate((state & IS_FLUSHING) != 0,
               (state & IS_FINISHING) != 0))
         {
            if (state == BUSY_STATE)
               /* We need more input now */
               return origLength - length;
            else if (state == FLUSHING_STATE)
            {
               if (level != NO_COMPRESSION)
               {
                  /* We have to supply some lookahead.  8 bit lookahead
                   * are needed by the zlib inflater, and we must fill 
                   * the next byte, so that all bits are flushed.
                   */
                  int neededbits = 8 + ((-pending.getBitCount()) & 7);
                  while (neededbits > 0)
                  {
                     /* write a static tree block consisting solely of
                      * an EOF:
                      */
                     pending.writeBits(2, 10);
                     neededbits -= 10;
                  }
               }
               state = BUSY_STATE;
            }
            else if (state == FINISHING_STATE)
            {
               pending.alignToByte();
               /* We have completed the stream */
               if (!noHeader)
               {
                  //                  int adler = engine.getAdler();
                  int adler = (int) this.adler.getValue();

                  pending.writeShortMSB(adler >> 16);
                  pending.writeShortMSB(adler & 0xffff);
               }
               state = FINISHED_STATE;
            }
         }
      }

      return origLength - length;
   }

   /**
    * Sets the dictionary which should be used in the deflate process. This call is equivalent to
    * <code>setDictionary(dict, 0,
    * dict.length)</code>.
    * 
    * @param dict
    *           the dictionary.
    * @exception IllegalStateException
    *               if setInput () or deflate () were already called or another dictionary was already set.
    */
   public void setDictionary(byte[] dict)
   {
      setDictionary(dict, 0, dict.length);
   }

   /**
    * Sets the dictionary which should be used in the deflate process. The dictionary should be a byte array containing
    * strings that are likely to occur in the data which should be compressed. The dictionary is not stored in the
    * compressed output, only a checksum. To decompress the output you need to supply the same dictionary again.
    * 
    * @param dict
    *           the dictionary.
    * @param offset
    *           an offset into the dictionary.
    * @param length
    *           the length of the dictionary.
    * @exception IllegalStateException
    *               if setInput () or deflate () were already called or another dictionary was already set.
    */
   public void setDictionary(byte[] dict, int offset, int length)
   {
      if (state != INIT_STATE)
         throw new IllegalStateException();

      state = SETDICT_STATE;
      //      engine.setDictionary(dict, offset, length);
      //      void setDictionary(byte[] buffer, int offset, int length)
      {
         //         if (DeflaterConstants4B.DEBUGGING && strstart != 1)
         //            throw new IllegalStateException("strstart not 1");
         adler.update(dict, offset, length);
         if (length < MIN_MATCH)
            return;
         if (length > MAX_DIST)
         {
            offset += length - MAX_DIST;
            length = MAX_DIST;
         }

         Vm.arrayCopy(dict, offset, window, strstart, length);

         updateHash();
         length--;
         while (--length > 0)
         {
            insertString();
            strstart++;
         }
         strstart += 2;
         blockStart = strstart;
      }
   }

   private final static int TOO_FAR = 4096;

   private int ins_h;

   /**
    * Hashtable, hashing three characters to an index for window, so that window[index]..window[index+2] have this hash
    * code. Note that the array should really be unsigned short, so you need to and the values with 0xffff.
    */
   private short[] head;

   /**
    * prev[index & WMASK] points to the previous index that has the same hash code as the string starting at index. This
    * way entries with the same hash code are in a linked list. Note that the array should really be unsigned short, so
    * you need to and the values with 0xffff.
    */
   private short[] prev;

   private int matchStart, matchLen;
   private boolean prevAvailable;
   private int blockStart;

   /**
    * strstart points to the current character in window.
    */
   private int strstart;

   /**
    * lookahead is the number of characters starting at strstart in window that are valid. So window[strstart] until
    * window[strstart+lookahead-1] are valid characters.
    */
   private int lookahead;

   /**
    * This array contains the part of the uncompressed stream that is of relevance. The current character is indexed by
    * strstart.
    */
   private byte[] window;

   private int strategy, max_chain, max_lazy, niceLength, goodLength;

   /** The current compression function. */
   private int comprFunc;

   /** The input data for compression. */
   private byte[] inputBuf;

   /** The total bytes of input read. */
   private int totalIn;

   /** The offset into inputBuf, where input data starts. */
   private int inputOff;

   /** The end offset of the input data. */
   private int inputEnd;

   private DeflaterHuffman4B huffman;

   /** The adler checksum */
   private Adler32 adler;

   /* DEFLATE ALGORITHM:
    *
    * The uncompressed stream is inserted into the window array.  When
    * the window array is full the first half is thrown away and the
    * second half is copied to the beginning.
    *
    * The head array is a hash table.  Three characters build a hash value
    * and they the value points to the corresponding index in window of 
    * the last string with this hash.  The prev array implements a
    * linked list of matches with the same hash: prev[index & WMASK] points
    * to the previous index with the same hash.
    * 
    * 
    */

   private final void updateHash()
   {
      if (DEBUGGING)
         System.err.println("updateHash: " + strstart);
      ins_h = (window[strstart] << HASH_SHIFT) ^ window[strstart + 1];
   }

   /**
    * Inserts the current string in the head hash and returns the previous value for this hash.
    */
   private final int insertString()
   {
      short match;
      int hash = ((ins_h << HASH_SHIFT) ^ window[strstart + (MIN_MATCH - 1)])
            & HASH_MASK;

      if (DEBUGGING)
      {
         if (hash != (((window[strstart] << (2 * HASH_SHIFT))
               ^ (window[strstart + 1] << HASH_SHIFT) ^ (window[strstart + 2])) & HASH_MASK))
            throw new Error("hash inconsistent: " + hash + "/"
                  + window[strstart] + ","
                  + window[strstart + 1] + ","
                  + window[strstart + 2] + "," + HASH_SHIFT);
      }

      prev[strstart & WMASK] = match = head[hash];
      head[hash] = (short) strstart;
      ins_h = hash;
      return match & 0xffff;
   }

   private void slideWindow()
   {
      Vm.arrayCopy(window, WSIZE, window, 0, WSIZE);
      matchStart -= WSIZE;
      strstart -= WSIZE;
      blockStart -= WSIZE;

      /* Slide the hash table (could be avoided with 32 bit values
       * at the expense of memory usage).
       */
      for (int i = 0; i < HASH_SIZE; i++)
      {
         int m = head[i] & 0xffff;
         head[i] = m >= WSIZE ? (short) (m - WSIZE) : 0;
      }

      /* Slide the prev table.
       */
      for (int i = 0; i < WSIZE; i++)
      {
         int m = prev[i] & 0xffff;
         prev[i] = m >= WSIZE ? (short) (m - WSIZE) : 0;
      }
   }

   /**
    * Fill the window when the lookahead becomes insufficient. Updates strstart and lookahead.
    * 
    * OUT assertions: strstart + lookahead <= 2*WSIZE lookahead >= MIN_LOOKAHEAD or inputOff == inputEnd
    */
   private void fillWindow()
   {
      /* If the window is almost full and there is insufficient lookahead,
       * move the upper half to the lower one to make room in the upper half.
       */
      if (strstart >= WSIZE + MAX_DIST)
         slideWindow();

      /* If there is not enough lookahead, but still some input left,
       * read in the input
       */
      while (lookahead < DeflaterConstants4B.MIN_LOOKAHEAD && inputOff < inputEnd)
      {
         int more = 2 * WSIZE - lookahead - strstart;

         if (more > inputEnd - inputOff)
            more = inputEnd - inputOff;

         Vm.arrayCopy(inputBuf, inputOff,
               window, strstart + lookahead, more);
         adler.update(inputBuf, inputOff, more);
         inputOff += more;
         totalIn += more;
         lookahead += more;
      }

      if (lookahead >= MIN_MATCH)
         updateHash();
   }

   /**
    * Find the best (longest) string in the window matching the string starting at strstart.
    * 
    * Preconditions: strstart + MAX_MATCH <= window.length.
    * 
    * 
    * @param curMatch
    */
   private boolean findLongestMatch(int curMatch)
   {
      int chainLength = this.max_chain;
      int niceLength = this.niceLength;
      short[] prev = this.prev;
      int scan = this.strstart;
      int match;
      int best_end = this.strstart + matchLen;
      int best_len = Math.max(matchLen, MIN_MATCH - 1);

      int limit = Math.max(strstart - MAX_DIST, 0);

      int strend = scan + MAX_MATCH - 1;
      byte scan_end1 = window[best_end - 1];
      byte scan_end = window[best_end];

      /* Do not waste too much time if we already have a good match: */
      if (best_len >= this.goodLength)
         chainLength >>= 2;

      /* Do not look for matches beyond the end of the input. This is necessary
       * to make deflate deterministic.
       */
      if (niceLength > lookahead)
         niceLength = lookahead;

      //      if (DeflaterConstants4B.DEBUGGING
      //            && strstart > 2 * WSIZE - MIN_LOOKAHEAD)
      //         throw new InternalError("need lookahead");

      do
      {
         //         if (DeflaterConstants4B.DEBUGGING && curMatch >= strstart)
         //            throw new InternalError("future match");
         if (window[curMatch + best_len] != scan_end
               || window[curMatch + best_len - 1] != scan_end1
               || window[curMatch] != window[scan]
               || window[curMatch + 1] != window[scan + 1])
            continue;

         match = curMatch + 2;
         scan += 2;

         /* We check for insufficient lookahead only every 8th comparison;
          * the 256th check will be made at strstart+258.
          */
         while (window[++scan] == window[++match]
               && window[++scan] == window[++match]
               && window[++scan] == window[++match]
               && window[++scan] == window[++match]
               && window[++scan] == window[++match]
               && window[++scan] == window[++match]
               && window[++scan] == window[++match]
               && window[++scan] == window[++match]
               && scan < strend)
            ;

         if (scan > best_end)
         {
            //    if (DeflaterConstants.DEBUGGING && ins_h == 0)
            //      System.err.println("Found match: "+curMatch+"-"+(scan-strstart));
            matchStart = curMatch;
            best_end = scan;
            best_len = scan - strstart;
            if (best_len >= niceLength)
               break;

            scan_end1 = window[best_end - 1];
            scan_end = window[best_end];
         }
         scan = strstart;
      } while ((curMatch = (prev[curMatch & WMASK] & 0xffff)) > limit
            && --chainLength != 0);

      matchLen = Math.min(best_len, lookahead);
      return matchLen >= MIN_MATCH;
   }

   private boolean deflateStored(boolean flush, boolean finish)
   {
      if (!flush && lookahead == 0)
         return false;

      strstart += lookahead;
      lookahead = 0;

      int storedLen = strstart - blockStart;

      if ((storedLen >= DeflaterConstants4B.MAX_BLOCK_SIZE)
                  /* Block is full */
      || (blockStart < WSIZE && storedLen >= MAX_DIST)
                  /* Block may move out of window */
      || flush)
      {
         boolean lastBlock = finish;
         if (storedLen > DeflaterConstants4B.MAX_BLOCK_SIZE)
         {
            storedLen = DeflaterConstants4B.MAX_BLOCK_SIZE;
            lastBlock = false;
         }

         //         if (DeflaterConstants4B.DEBUGGING)
         //            System.err.println("storedBlock[" + storedLen + "," + lastBlock + "]");

         huffman.flushStoredBlock(window, blockStart, storedLen, lastBlock);
         blockStart += storedLen;
         return !lastBlock;
      }
      return true;
   }

   private boolean deflateFast(boolean flush, boolean finish)
   {
      if (lookahead < MIN_LOOKAHEAD && !flush)
         return false;

      while (lookahead >= MIN_LOOKAHEAD || flush)
      {
         if (lookahead == 0)
         {
            /* We are flushing everything */
            huffman.flushBlock(window, blockStart, strstart - blockStart,
                  finish);
            blockStart = strstart;
            return false;
         }

         if (strstart > 2 * WSIZE - MIN_LOOKAHEAD)
         {
            /* slide window, as findLongestMatch need this.
             * This should only happen when flushing and the window
             * is almost full.
             */
            slideWindow();
         }

         int hashHead;
         if (lookahead >= MIN_MATCH
               && (hashHead = insertString()) != 0
               && strategy != Deflater4B.HUFFMAN_ONLY
               && strstart - hashHead <= MAX_DIST
               && findLongestMatch(hashHead))
         {
            /* longestMatch sets matchStart and matchLen */
            //            if (DeflaterConstants4B.DEBUGGING)
            //            {
            //               for (int i = 0; i < matchLen; i++)
            //               {
            //                  if (window[strstart + i] != window[matchStart + i])
            //                     throw new InternalError();
            //               }
            //            }
            huffman.tallyDist(strstart - matchStart, matchLen);

            lookahead -= matchLen;
            if (matchLen <= max_lazy && lookahead >= MIN_MATCH)
            {
               while (--matchLen > 0)
               {
                  strstart++;
                  insertString();
               }
               strstart++;
            }
            else
            {
               strstart += matchLen;
               if (lookahead >= MIN_MATCH - 1)
                  updateHash();
            }
            matchLen = MIN_MATCH - 1;
            continue;
         }
         else
         {
            /* No match found */
            huffman.tallyLit(window[strstart] & 0xff);
            strstart++;
            lookahead--;
         }

         if (huffman.isFull())
         {
            boolean lastBlock = finish && lookahead == 0;
            huffman.flushBlock(window, blockStart, strstart - blockStart,
                  lastBlock);
            blockStart = strstart;
            return !lastBlock;
         }
      }
      return true;
   }

   private boolean deflateSlow(boolean flush, boolean finish)
   {
      if (lookahead < MIN_LOOKAHEAD && !flush)
         return false;

      while (lookahead >= MIN_LOOKAHEAD || flush)
      {
         if (lookahead == 0)
         {
            if (prevAvailable)
               huffman.tallyLit(window[strstart - 1] & 0xff);
            prevAvailable = false;

            /* We are flushing everything */
            //            if (DeflaterConstants4B.DEBUGGING && !flush)
            //               throw new InternalError("Not flushing, but no lookahead");
            huffman.flushBlock(window, blockStart, strstart - blockStart,
                  finish);
            blockStart = strstart;
            return false;
         }

         if (strstart >= 2 * WSIZE - MIN_LOOKAHEAD)
         {
            /* slide window, as findLongestMatch need this.
             * This should only happen when flushing and the window
             * is almost full.
             */
            slideWindow();
         }

         int prevMatch = matchStart;
         int prevLen = matchLen;
         if (lookahead >= MIN_MATCH)
         {
            int hashHead = insertString();
            if (strategy != Deflater4B.HUFFMAN_ONLY
                  && hashHead != 0 && strstart - hashHead <= MAX_DIST
                  && findLongestMatch(hashHead))
            {
               /* longestMatch sets matchStart and matchLen */

               /* Discard match if too small and too far away */
               if (matchLen <= 5
                     && (strategy == Deflater4B.FILTERED
                     || (matchLen == MIN_MATCH
                     && strstart - matchStart > TOO_FAR)))
               {
                  matchLen = MIN_MATCH - 1;
               }
            }
         }

         /* previous match was better */
         if (prevLen >= MIN_MATCH && matchLen <= prevLen)
         {
            //            if (DeflaterConstants4B.DEBUGGING)
            //            {
            //               for (int i = 0; i < matchLen; i++)
            //               {
            //                  if (window[strstart - 1 + i] != window[prevMatch + i])
            //                     throw new InternalError();
            //               }
            //            }
            huffman.tallyDist(strstart - 1 - prevMatch, prevLen);
            prevLen -= 2;
            do
            {
               strstart++;
               lookahead--;
               if (lookahead >= MIN_MATCH)
                  insertString();
            } while (--prevLen > 0);
            strstart++;
            lookahead--;
            prevAvailable = false;
            matchLen = MIN_MATCH - 1;
         }
         else
         {
            if (prevAvailable)
               huffman.tallyLit(window[strstart - 1] & 0xff);
            prevAvailable = true;
            strstart++;
            lookahead--;
         }

         if (huffman.isFull())
         {
            int len = strstart - blockStart;
            if (prevAvailable)
               len--;
            boolean lastBlock = (finish && lookahead == 0 && !prevAvailable);
            huffman.flushBlock(window, blockStart, len, lastBlock);
            blockStart += len;
            return !lastBlock;
         }
      }
      return true;
   }

   //   public boolean deflate(boolean flush, boolean finish)
   public boolean engineDeflate(boolean flush, boolean finish)
   {
      boolean progress;
      do
      {
         fillWindow();
         boolean canFlush = flush && inputOff == inputEnd;
         //         if (DeflaterConstants4B.DEBUGGING)
         //            System.err.println("window: [" + blockStart + "," + strstart + ","
         //                  + lookahead + "], " + comprFunc + "," + canFlush);
         switch (comprFunc)
         {
            case DEFLATE_STORED:
               progress = deflateStored(canFlush, finish);
            break;
            case DEFLATE_FAST:
               progress = deflateFast(canFlush, finish);
            break;
            case DEFLATE_SLOW:
               progress = deflateSlow(canFlush, finish);
            break;
            default:
               throw new Error();
         }
      } while (pending.isFlushed() /* repeat while we have no pending output */
            && progress); /* and progress was made */

      return progress;
   }
}
