/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// ### This file created by BYACC 1.8(/Java extension 1.14)
// ### Java capabilities added 7 Jan 97, Bob Jamison
// ### Updated : 27 Nov 97 -- Bob Jamison, Joe Nieten
// ### 01 Jan 98 -- Bob Jamison -- fixed generic semantic constructor
// ### 01 Jun 99 -- Bob Jamison -- added Runnable support
// ### 06 Aug 00 -- Bob Jamison -- made state variables class-global
// ### 03 Jan 01 -- Bob Jamison -- improved flags, tracing
// ### 16 May 01 -- Bob Jamison -- added custom stack sizing
// ### 04 Mar 02 -- Yuval Oren -- improved java performance, added options
// ### 14 Mar 02 -- Tomas Hurka -- -d support, static initializer workaround
// ### Please send bug reports to tom@hukatronic.cz
// ### static char yysccsid[] = "@(#)yaccpar  1.8 (Berkeley) 01/20/90";

package litebase;

// #line 2 "Litebase.y"
import totalcross.util.*;
import totalcross.sys.*;

// #line 41 "LitebaseParser.java"

/**
 * This class calls <code>yyparse()</code> and builds the parser result.
 */
class LitebaseParser
{
   /**
    * Maximum stack size.
    */
   private final static int YYSTACKSIZE = 50;

   // Tokens
   /**
    * Identifier token.
    */
   final static int TK_IDENT = 257;

   /**
    * Numerical token.
    */
   final static int TK_NUMBER = 258;

   /**
    * String token.
    */
   final static int TK_STR = 259;

   /**
    * <code>VALUES</code> keyword token.
    */
   final static int TK_VALUES = 260;

   /**
    * <code>SELECY</code> keyword token.
    */
   final static int TK_SELECT = 261;

   /**
    * <code>INDEX</code> keyword token.
    */
   final static int TK_INDEX = 262;

   /**
    * <code>INTO</code> keyword token.
    */
   final static int TK_INTO = 263;

   /**
    * <code>SUM</code> keyword token.
    */
   final static int TK_SUM = 264;

   /**
    * <code>ABS</code> keyword token.
    */
   final static int TK_ABS = 265;

   /**
    * <code>RENAME</code> keyword token.
    */
   final static int TK_RENAME = 266;

   /**
    * <code>CREATE</code> keyword token.
    */
   final static int TK_CREATE = 267;

   /**
    * <code>ORDER</code> keyword token.
    */
   final static int TK_ORDER = 268;

   /**
    * <code>HOUR</code> keyword token.
    */
   final static int TK_HOUR = 269;

   /**
    * <code>AVG</code> keyword token.
    */
   final static int TK_AVG = 270;

   /**
    * <code>DAY</code> keyword token.
    */
   final static int TK_DAY = 271;

   /**
    * <code>SECOND</code> keyword token.
    */
   final static int TK_SECOND = 272;

   /**
    * <code>NOCASE</code> keyword token.
    */
   final static int TK_NOCASE = 273;

   /**
    * <code>MONTH</code> keyword token.
    */
   final static int TK_MONTH = 274;

   /**
    * <code>DESC</code> keyword token.
    */
   final static int TK_DESC = 275;

   /**
    * <code>INT</code> keyword token.
    */
   final static int TK_INT = 276;

   /**
    * <code>MIN</code> keyword token.
    */
   final static int TK_MIN = 277;

   /**
    * <code>IS</code> keyword token.
    */
   final static int TK_IS = 278;

   /**
    * <code>MINUTE</code> keyword token.
    */
   final static int TK_MINUTE = 279;

   /**
    * <code>MILLIS</code> keyword token.
    */
   final static int TK_MILLIS = 280;

   /**
    * <code>COUNT</code> keyword token.
    */
   final static int TK_COUNT = 281;

   /**
    * <code>FROM</code> keyword token.
    */
   final static int TK_FROM = 282;

   /**
    * <code>CHAR</code> keyword token.
    */
   final static int TK_CHAR = 283;

   /**
    * <code>ADD</code> keyword token.
    */
   final static int TK_ADD = 284;

   /**
    * <code>ON</code> keyword token.
    */
   final static int TK_ON = 285;

   /**
    * <code>PRIMARY</code> keyword token.
    */
   final static int TK_PRIMARY = 286;

   /**
    * <code>DOUBLE</code> keyword token.
    */
   final static int TK_DOUBLE = 287;

   /**
    * <code>TABLE</code> keyword token.
    */
   final static int TK_TABLE = 288;

   /**
    * <code>NULL</code> keyword token.
    */
   final static int TK_NULL = 289;

   /**
    * <code>DATE</code> keyword token.
    */
   final static int TK_DATE = 290;

   /**
    * <code>NOT</code> keyword token.
    */
   final static int TK_NOT = 291;

   /**
    * <code>TO</code> keyword token.
    */
   final static int TK_TO = 292;

   /**
    * <code>VARCHAR</code> keyword token.
    */
   final static int TK_VARCHAR = 293;

   /**
    * <code>UPDATE</code> keyword token.
    */
   final static int TK_UPDATE = 294;

   /**
    * <code>WHERE</code> keyword token.
    */
   final static int TK_WHERE = 295;

   /**
    * <code>LIKE</code> keyword token.
    */
   final static int TK_LIKE = 296;

   /**
    * <code>LONG</code> keyword token.
    */
   final static int TK_LONG = 297;

   /**
    * <code>SET</code> keyword token.
    */
   final static int TK_SET = 298;

   /**
    * <code>AS</code> keyword token.
    */
   final static int TK_AS = 299;

   /**
    * <code>DEFAULT</code> keyword token.
    */
   final static int TK_DEFAULT = 300;

   /**
    * <code>DELETE</code> keyword token.
    */
   final static int TK_DELETE = 301;

   /**
    * <code>ALTER</code> keyword token.
    */
   final static int TK_ALTER = 302;

   /**
    * <code>UPPER</code> keyword token.
    */
   final static int TK_UPPER = 303;

   /**
    * <code>DROP</code> keyword token.
    */
   final static int TK_DROP = 304;

   /**
    * <code>AND</code> keyword token.
    */
   final static int TK_AND = 305;

   /**
    * <code>OR</code> keyword token.
    */
   final static int TK_OR = 306;

   /**
    * <code>DATETIME</code> keyword token.
    */
   final static int TK_DATETIME = 307;

   /**
    * <code>INSERT</code> keyword token.
    */
   final static int TK_INSERT = 308;

   /**
    * <code>GROUP</code> keyword token.
    */
   final static int TK_GROUP = 309;

   /**
    * <code>LOWER</code> keyword token.
    */
   final static int TK_LOWER = 310;

   /**
    * <code>YEAR</code> keyword token.
    */
   final static int TK_YEAR = 311;

   /**
    * <code>ASC</code> keyword token.
    */
   final static int TK_ASC = 312;

   /**
    * <code>BY</code> keyword token.
    */
   final static int TK_BY = 313;

   /**
    * <code>DISTINCT</code> keyword token.
    */
   final static int TK_DISTINCT = 314;

   /**
    * <code>HAVING</code> keyword token.
    */
   final static int TK_HAVING = 315;

   /**
    * <code>FLOAT</code> keyword token.
    */
   final static int TK_FLOAT = 316;

   /**
    * <code>SHORT</code> keyword token.
    */
   final static int TK_SHORT = 317;

   /**
    * <code>BLOB</code> keyword token.
    */
   final static int TK_BLOB = 318;

   /**
    * <code>KEY</code> keyword token.
    */
   final static int TK_KEY = 319;

   /**
    * <code>MAX</code> keyword token.
    */
   final static int TK_MAX = 320;

   /**
    * <code>>=</code> token.
    */
   final static int TK_GREATER_EQUAL = 321;

   /**
    * <code>></code> token.
    */
   final static int TK_GREATER = 322;

   /**
    * <code>,</code> token.
    */
   final static int TK_COMMA = 323;

   /**
    * <code><</code> token.
    */
   final static int TK_LESS = 324;

   /**
    * <code>?</code> token.
    */
   final static int TK_INTERROGATION = 325;

   /**
    * <code><=</code> token.
    */
   final static int TK_LESS_EQUAL = 326;

   /**
    * <code>==</code> token.
    */
   final static int TK_EQUAL = 327;

   /**
    * <code>!=</code> or <code><></code> token.
    */
   final static int TK_DIFF = 328;

   /**
    * <code>.</code> token.
    */
   final static int TK_DOT = 329;

   /**
    * Error code.
    */
   final static int YYERRCODE = 256;

   /**
    * The size of the parser table.
    */
   private final static int YYTABLESIZE = 491;

   /**
    * Final state.
    */
   private final static int YYFINAL = 8;

   /**
    * Terminal table
    */
   private final static short[] yylhs = {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 4, 4, 25, 25, 22, 22, 35, 35, 35, 35, 35, 35, 35, 35, 35, 35, 6, 6, 9, 9, 
8, 8, 1, 1, 1, 5, 5, 5, 7, 7, 23, 23, 36, 36, 26, 26, 24, 24, 27, 27, 27, 27, 27, 27, 27, 27, 28, 28, 28, 37, 37, 29, 29, 30, 30, 38, 39, 39, 39, 39, 
10, 10, 31, 31, 40, 40, 41, 2, 2, 20, 20, 20, 13, 13, 32, 32, 42, 43, 43, 12, 12, 33, 33, 44, 44, 15, 15, 45, 34, 34, 46, 46, 47, 11, 11, 11, 17, 17, 
17, 17, 17, 14, 14, 14, 14, 14, 14, 14, 14, 21, 21, 21, 21, 21, 16, 16, 16, 16, 16, 16, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 18, 18, 18, 18, 18};

   /**
    * The number of terminals.
    */
   private final static short[] yylen = {2, 7, 8, 3, 5, 8, 4, 5, 6, 8, 1, 0, 1, 1, 1, 1, 3, 5, 5, 5, 5, 5, 9, 5, 5, 7, 9, 0, 1, 0, 2, 0, 1, 0, 2, 2, 
0, 2, 2, 0, 2, 0, 6, 1, 3, 0, 3, 1, 3, 1, 1, 1, 1, 3, 3, 3, 3, 4, 6, 3, 0, 1, 0, 1, 1, 3, 3, 1, 1, 1, 1, 0, 1, 1, 1, 1, 3, 2, 0, 2, 1, 1, 1, 1, 3, 1, 
3, 3, 0, 1, 0, 2, 0, 4, 1, 3, 0, 2, 1, 0, 3, 1, 3, 2, 0, 1, 1, 1, 3, 3, 3, 4, 2, 3, 3, 4, 3, 4, 3, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 
4, 4, 4, 4, 4, 4, 4, 4, 4, 4};

   /**
    * Reduction table.
    */
   private final static short[] yydefred = {0, 0, 0, 0, 0, 0, 0, 0, 0, 72, 0, 0, 0, 10, 0, 63, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,   
0, 0, 0, 73, 80, 82, 81, 0, 0, 0, 75, 0, 0, 12, 0, 0, 0, 47, 13, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 77, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 6, 0, 0, 0, 0, 84, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 79, 0, 0, 85, 76, 0, 0, 0, 15, 0, 0, 64, 0, 7, 61, 0, 0, 0, 48, 4, 0, 0, 145, 
131, 137, 144, 136, 139, 135, 143, 138, 140, 141, 132, 133, 134, 142, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 121, 122, 0, 123, 
0, 120, 107, 0, 124, 0, 0, 0, 59, 46, 0, 87, 86, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 16, 1, 68, 67, 69, 70, 66, 65, 0, 0, 112, 0, 0, 0, 0, 
0, 0, 129, 127, 128, 130, 125, 126, 0, 57, 0, 50, 49, 51, 52, 0, 0, 0, 9, 2, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 110, 108, 109, 114, 0, 0, 
116, 118, 113, 0, 0, 5, 94, 0, 0, 34, 35, 0, 18, 0, 21, 37, 38, 23, 0, 19, 24, 20, 17, 28, 0, 0, 111, 115, 117, 119, 58, 54, 53, 55, 56, 98, 0, 93, 
0, 0, 0, 101, 40, 32, 0, 0, 0, 43, 0, 95, 0, 106, 105, 103, 0, 0, 0, 25, 0, 42, 102, 0, 0, 44, 22, 26};

   /**
    * goto table that leads to a reduce rule.
    */
   private final static short[] yydgoto = {8, 231, 74, 106, 48, 235, 274, 262, 294, 183, 10, 303, 118, 167, 168, 287, 217, 169, 39, 170, 41, 171, 
                           112, 158, 53, 54, 88, 224, 84, 16, 115, 42, 107, 180, 227, 113, 298, 120, 116, 200, 43, 44, 108, 143, 257, 288, 290, 291};

   /**
    * Shift index table.
    */
   private final static short[] yysindex = {-97, -269, -238, -148, -171, -158, -161, -146, 0, 0, 119, -143, -148, 0, -120, 0, -148, -148, -20, -148,
-148, -175, 113, 116, 128, 133, 141, 142, 143, 144, 145, 147, 151, 154, 156, 158, 162, 0, 0, 0, 0, -140, -74, -114, 0, -82, 172, 0, -84, -120, -179, 
0, 0, -108, -68, 0, 178, -38, -36, -36, -36, -36, -36, -36, -36, -36, -36, -36, 180, -36, -36, -36, -36, -32, 0, -148, 171, -148, -31, -36, -67, -23, 
-59, -57, 0, -21, -148, -14, -16, 0, 192, 194, 197, 206, 207, 208, 210, 211, 212, 213, 214, 216, 217, 223, 224, 0, -33, -195, 0, 0, 227, -77, -55, 0, 
-53, -183, 0, 76, 0, 0, -7, -10, 12, 0, 0, -35, 247, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -120, -148, 8, -14, 51, 250, 51, 51, 251, 51, 51, 
51, 51, 296, -160, 297, -218, -36, 0, 0, 0, 94, 0, 76, 0, 0, -164, 0, -176, 82, 300, 0, 0, -207, 0, 0, 29, 75, -27, 25, 46, 91, 46, 54, 99, 46, 54, 
46, 46, 100, 41, 0, 0, 0, 0, 0, 0, 0, 0, -136, 76, 0, -25, 76, 76, -181, 65, -231, 0, 0, 0, 0, 0, 0, -79, 0, -14, 0, 0, 0, 0, -22, 171, 49, 0, 0, 0, 
-209, 73, 328, 73, -193, 73, 329, 73, 73, 73, 73, 114, 332, 14, 0, 0, 0, 0, 86, -230, 0, 0, 0, -18, -186, 0, 0, -217, 171, 0, 0, 88, 0, 105, 0, 0, 0, 
0, 105, 0, 0, 0, 0, 0, 339, 124, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 171, 0, 76, -227, 59, 0, 0, 0, 51, 51, 73, 0, -15, 0, -164, 0, 0, 0, 171, 54, 54, 0, 
135, 0, 0, 73, 73, 0, 0, 0};

   /**
    * Reduce index table.
    */
   private final static short[] yyrindex = {0, 146, 0, 0, 137, 0, 0, 0, 0, 0, 0, 0, 0, 0, 97, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, -199, 0, 120, 0, 0, 0, 0, 0, 4, 0, 0, 0, 121, 0, 0, 148, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
407, 117, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 2, 0, 0, 0, 0, 371, 0, 0, 407, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 10, 0, -28, 0, -28, -28, 0, -28, -28, -28, -28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0,
0, 0, 0, 0, 0, 0, 0, 0, 413, 0, 0, -30, 0, -30, -29, 0, -30, -29, -30, -30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, 0, -8, 0, -8, 0, -8, -8, -8, -8, 373, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 0, 0, 0, 0, 
0, -41, 0, 0, 0, 0, -41, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 421, 0, 0, 0, -28, -28, -8, 0, 0, 0, 21, 0, 0, 0, 0, -29, 
-29, 0, 0, 0, 0, -8, -8, 0, 0, 0};

   /**
    * goto table for the rules.
    */
   private final static short[] yygindex = {0, -19, 0, 27, -24, -174, 0, -149, 163, -118, 0, 0, 50, -2, -110, 0, 0, -128, 0, 17, -205, 215, 0, 0, 
                                                                 -70, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 267, 0, 0, 273, 0, 0, 358, 293, 0, 0, 0, 0, 134};

   /**
    * Parser state table.
    */
   private static final short[] yytable = { 31, 83, 90, 88, 11, 91, 175, 104, 38, 11, 92, 33, 36, 29, 228, 238, 244, 125, 96, 255, 256, 97, 52, 280, 
11, 80, 309, 40, 250, 278, 14, 185, 186, 39, 188, 189, 190, 191, 205, 46, 196, 197, 83, 49, 50, 9, 55, 56, 301, 259, 12, 220, 221, 289, 204, 276, 90, 
91, 92, 93, 94, 95, 96, 97, 98, 99, 265, 101, 102, 103, 104, 198, 281, 282, 38, 243, 181, 114, 245, 246, 260, 299, 222, 78, 264, 302, 267, 81, 269, 
270, 271, 272, 204, 40, 251, 279, 266, 111, 285, 289, 117, 18, 208, 283, 110, 82, 286, 199, 247, 13, 248, 15, 117, 124, 45, 209, 166, 20, 223, 177, 
210, 21, 162, 163, 78, 83, 193, 19, 144, 23, 17, 311, 312, 24, 203, 26, 27, 47, 28, 284, 160, 206, 207, 30, 31, 211, 212, 307, 213, 253, 214, 215, 
216, 58, 57, 202, 59, 145, 114, 73, 300, 37, 314, 315, 1, 161, 233, 33, 60, 237, 2, 239, 240, 61, 34, 35, 305, 306, 21, 162, 163, 62, 63, 64, 65, 66,
23, 67, 71, 165, 24, 68, 26, 27, 69, 28, 70, 3, 71, 147, 30, 31, 72, 77, 4, 5, 148, 6, 75, 76, 149, 7, 78, 150, 79, 85, 151, 86, 87, 89, 152, 21, 
100, 38, 33, 105, 111, 121, 117, 122, 153, 34, 35, 127, 119, 128, 123, 51, 129, 154, 155, 156, 40, 51, 126, 31, 165, 130, 131, 132, 31, 133, 134, 
135, 136, 137, 38, 138, 139, 31, 88, 33, 36, 29, 140, 141, 142, 146, 157, 83, 90, 88, 29, 91, 159, 40, 83, 11, 92, 83, 206, 207, 31, 83, 38, 172, 96,
176, 85, 97, 184, 187, 83, 33, 36, 29, 85, 83, 88, 11, 83, 254, 38, 40, 11, 85, 83, 83, 308, 173, 83, 90, 88, 83, 91, 39, 83, 179, 11, 206, 207, 40, 
83, 83, 83, 83, 88, 83, 83, 83, 104, 174, 11, 21, 162, 163, 192, 182, 195, 218, 219, 23, 225, 226, 229, 24, 230, 26, 27, 232, 28, 21, 162, 163, 234, 
30, 31, 236, 241, 23, 242, 249, 258, 24, 261, 26, 27, 164, 28, 263, 268, 273, 275, 30, 31, 277, 21, 292, 293, 33, 296, 297, 304, 22, 23, 202, 34, 35,
24, 25, 26, 27, 313, 28, 62, 11, 29, 33, 30, 31, 32, 165, 74, 71, 34, 35, 14, 90, 45, 60, 71, 71, 41, 99, 27, 71, 71, 71, 71, 165, 71, 100, 33, 71, 
194, 71, 71, 71, 21, 34, 35, 295, 252, 201, 109, 22, 23, 178, 310, 36, 24, 25, 26, 27, 0, 28, 0, 0, 29, 71, 30, 31, 32, 0, 0, 0, 71, 71, 0, 0, 0, 0, 
0, 0, 0, 0, 71, 0, 0, 0, 0, 0, 0, 0, 33, 0, 0, 0, 0, 0, 0, 34, 35, 0, 0, 0, 0, 0, 0, 0, 0, 36 };

   /**
    * Check table to see if the lexer state is correct.
    */
   private static final short[] yycheck = {41, 0, 0, 0, 0, 0, 41, 0, 10, 0, 0, 41, 41, 41, 41, 189, 41, 87, 0, 41, 225, 0, 42, 41, 262, 49, 41, 10, 
259, 259, 3, 149, 150, 41, 152, 153, 154, 155, 166, 12, 258, 259, 41, 16, 17, 314, 19, 20, 275, 258, 288, 258, 259, 258, 164, 41, 58, 59, 60, 61, 62, 
63, 64, 65, 66, 67, 259, 69, 70, 71, 72, 289, 258, 259, 76, 203, 146, 79, 206, 207, 289, 286, 289, 282, 233, 312, 235, 266, 237, 238, 239, 240, 202, 
76, 325, 325, 289, 257, 315, 304, 295, 262, 278, 289, 77, 284, 323, 325, 289, 257, 291, 282, 295, 86, 257, 291, 40, 263, 325, 143, 296, 257, 258, 
259, 323, 304, 286, 288, 323, 265, 288, 305, 306, 269, 40, 271, 272, 257, 274, 325, 323, 305, 306, 279, 280, 321, 322, 296, 324, 219, 326, 327, 328,
40, 329, 291, 40, 107, 160, 299, 288, 42, 311, 312, 261, 115, 185, 303, 40, 188, 267, 190, 191, 40, 310, 311, 294, 295, 257, 258, 259, 40, 40, 40, 
40, 40, 265, 40, 42, 325, 269, 40, 271, 272, 40, 274, 40, 294, 40, 276, 279, 280, 40, 285, 301, 302, 283, 304, 282, 323, 287, 308, 40, 290, 298, 323, 
293, 285, 40, 257, 297, 257, 42, 225, 303, 257, 257, 286, 295, 286, 307, 310, 311, 41, 257, 41, 257, 257, 41, 316, 317, 318, 225, 257, 260, 286, 325, 
41, 41, 41, 291, 41, 41, 41, 41, 41, 258, 41, 41, 300, 257, 291, 291, 291, 41, 41, 299, 40, 323, 268, 268, 268, 300, 268, 327, 258, 275, 268, 268, 
278, 305, 306, 323, 282, 286, 292, 268, 40, 323, 268, 40, 40, 291, 323, 323, 323, 323, 296, 295, 295, 299, 323, 304, 286, 295, 323, 305, 306, 323, 
319, 309, 309, 309, 312, 309, 323, 315, 309, 309, 305, 306, 304, 321, 322, 323, 324, 323, 326, 327, 328, 323, 319, 323, 257, 258, 259, 40, 286, 41,
257, 40, 265, 313, 268, 319, 269, 300, 271, 272, 258, 274, 257, 258, 259, 300, 279, 280, 258, 258, 265, 319, 296, 313, 269, 291, 271, 272, 291, 274, 
41, 41, 257, 40, 279, 280, 289, 257, 289, 273, 303, 41, 257, 323, 264, 265, 291, 310, 311, 269, 270, 271, 272, 257, 274, 257, 298, 277, 303, 279, 
280, 281, 325, 282, 257, 310, 311, 285, 0, 260, 292, 264, 265, 41, 0, 41, 269, 270, 271, 272, 325, 274, 0, 303, 277, 157, 279, 280, 281, 257, 310, 
311, 268, 217, 160, 76, 264, 265, 144, 304, 320, 269, 270, 271, 272, -1, 274, -1, -1, 277, 303, 279, 280, 281, -1, -1, -1, 310, 311, -1, -1, -1, -1,
-1, -1, -1, -1, 320, -1, -1, -1, -1, -1, -1, -1, 303, -1, -1, -1, -1, -1, -1, 310, 311, -1, -1, -1, -1, -1, -1, -1, -1, 320};

   // final class LitebaseParserVal is defined in LitebaseParserVal.java
   /**
    * The 'lval' (result) got from <code>yylex()</code>.
    */
   LitebaseParserVal yylval;

   // #line 1244 "Litebase.y"
   /**
    * The lexical analyzer.
    */
   private LitebaseLex lexer;
   
   /**
    * The type of SQL command, which can be one of: <b><code>CMD_CREATE_TABLE</b></code>, <b><code>CMD_CREATE_INDEX</b></code>, 
    * <b><code>CMD_DROP_TABLE</b></code>, <b><code>CMD_DROP_INDEX</b></code>, <b><code>CMD_ALTER_DROP_PK</b></code>, 
    * <b><code>CMD_ALTER_ADD_PK</b></code>, <b><code>CMD_ALTER_RENAME_TABLE</b></code>, <b><code>CMD_ALTER_RENAME_COLUMN</b></code>, 
    * <b><code>CMD_SELECT</b></code>, <b><code>CMD_INSERT</b></code>, <b><code>CMD_UPDATE</b></code>, or <b><code>CMD_DELETE</b></code>.
    */
   int command;

   /**
    * The resulting set table list, used with all statements.
    */
   SQLResultSetTable[] tableList;

   /**
    * The number of tables in the table list.
    */
   int tableListSize;

   /**
    * The field list for the SQL commands except <code>SELECT</code>. 
    */
   SQLFieldDefinition[] fieldList;

   /**
    * The number of fields in the field list.
    */
   int fieldListSize;

   /**
    * Contains field values (strings) used on insert/update statements.
    */
   String[] fieldValues;

   /**
    * The number of fields of values in the field values list.
    */
   int fieldValuesSize;

   /**
    * The field list for inserts, updates and indices.
    */
   String[] fieldNames;

   /**
    * The number of fields of the update field list.
    */
   int fieldNamesSize;

   /**
    * This is used to differ between a where clause and a having clause. Before parsing the having clause, <code>isWhereClause</code> is set to 
    * false. So the <code>getInstanceBooleanClause()</code> method will return a having clause, otherwise it returns a where clause.
    */
   boolean isWhereClause = true;

   /**
    * The where clause of a <code>SELECT</code> statement.
    */
   SQLBooleanClause whereClause;

   /**
    * The having clause of a <code>SELECT</code> statement.
    */
   SQLBooleanClause havingClause;

   /**
    * The initial part of the <code>SELECT</code> statement
    */
   SQLSelectClause select;

   /**
    * The order by part of a <code>SELECT</code> statement.
    */
   SQLColumnListClause order_by;

   /**
    * The group by part of a <code>SELECT</code> statement.
    */
   SQLColumnListClause group_by;
   
   /**
    * A hashtable to be used on select statements to verify if it has repeated table names.
    */
   IntHashtable tables;
   
   /**
    * The lex main method.
    *
    * @return The token code, -1 if the end of file was reached or 256 if there was a lexical error.
    */
   private int yylex()
   {
      yylval = new LitebaseParserVal();
      return lexer.yylex();
   }

   /**
    * The method which executes the parser process.
    *
    * @param sql The sql command to be parsed.
    * @param parser The parser object which will be filled with the result of the parsing process.
    * @param lexer The lexical analizer.
    */
   static void parser(String sql, LitebaseParser parser, LitebaseLex lexer)
   {
      LitebaseParser yyparser = parser; // Initializes the parser.
      
      // juliana@224_2: improved memory usage on BlackBerry.
      yyparser.lexer = lexer;
      lexer.zzReaderChars = sql;
      lexer.yyparser = parser;
      lexer.yycurrent = ' ';
      lexer.yyposition = 0;      
      
      yyparser.yyparse();
   }

   /**
    * Sets the operand type.
    *
    * @param The operand type.
    * @return A boolean clause tree with this operand type.
    */
   private SQLBooleanClauseTree setOperandType(int operandType)
   {
      SQLBooleanClauseTree tree = new SQLBooleanClauseTree(getInstanceBooleanClause());
      tree.operandType = operandType;
      return tree;
   }

   /**
    * Adds a column field to the order field list.
    *
    * @param field The field to be added.
    * @param isAscending Indicates the ordering used.
    * @param isOrder by <code>true</code> if the field comes from an order be clause. <code>false</code>, otherwise.
    */
   private void addColumnFieldOrderGroupBy(SQLResultSetField field, boolean isAscending, boolean isOrderBy)
   {
      SQLColumnListClause listClause = isOrderBy? getInstanceColumnListClauseOrderBy() 
                                                : getInstanceColumnListClauseGroupBy();

      // The maximum number of columns in a list clause can't be reached.
      if (listClause.fieldsCount == SQLElement.MAX_NUM_COLUMNS)
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_OVERFLOW_GROUPBY_ORDERBY));

      field.tableColHashCode = field.aliasHashCode = field.tableColName.hashCode();
      field.isAscending = isAscending;
      listClause.fieldList[listClause.fieldsCount++] = field;
   }
   
   /**
    * Gets an instance of a where clause or a having clause, depending of what clause is used in the <code>SELECT</code> statement.
    *
    * @return The instance of a where clause or a having clause.
    */
   SQLBooleanClause getInstanceBooleanClause()
   {
      if (isWhereClause) // where clause
      {
         if (whereClause == null)
            whereClause = new SQLBooleanClause();
         return whereClause;
      }
      else // having clause
      {
         SQLBooleanClause having = havingClause;
         if (having == null)
            having = havingClause = new SQLBooleanClause();
         having.isWhereClause = false; 
         return having;
      }
   }

   /**
    * Gets an instance of an order by clause used in the <code>SELECT</code> statement.
    *
    * @return The order by clause.
    */
   SQLColumnListClause getInstanceColumnListClauseOrderBy()
   {
      if (order_by == null)
         order_by = new SQLColumnListClause();
      return order_by;
   }

   /**
    * Gets an instance of a group by clause used in the <code>SELECT</code> statement.
    *
    * @return The group by clause.
    */
   SQLColumnListClause getInstanceColumnListClauseGroupBy()
   {
      if (group_by == null)
         group_by = new SQLColumnListClause();
      return group_by;
   }

   // ###############################################################
   // method: yyparse : parse input and execute indicated items
   // ###############################################################
   private int yyparse()
   {
      boolean doaction,
              isPrimaryKey = false, // Indicates if a table field is the primary key.
              isNotNull = false, // Indicates if a field can have a <code>null</code> value.
              isNocase = false; // <code>true</code> indicates caseless comparison. <code>false</code>, otherwise.
      String tableNameAux,
             strDefault = null, // Stores a default value for a field.
             aliasTableName = null, // An alias for the table name.
             firstFieldUpdateTableName = null, // The first table name found in an update statement.
             firstFieldUpdateAlias = null, // The first table alias found in an update statement. 
             secondFieldUpdateTableName = null, // The second table name found in an update statement, which indicates an error.
             secondFieldUpdateAlias = null; // The second table alias found in an update statement, which indicates an error. 
      int size,
          hash,
          index,
          yyerrflag = 0, // Was there an error?
          yychar, // The current working character.
          stateptr = 0, // The stack pointer.
          valptr = 0, // Values stack pointer.
          yyn, // Next next thing to do.
          yym, // Count of terminals on rhs.
          yystate = 0, // Current parsing state from state table.
          number_pk = 0; // Counts the number of simple primary keys, which must be only one.
      SQLResultSetField field;
      SQLResultSetField[] resultFieldList;
      SQLBooleanClause clause;
      SQLColumnListClause group_order_by;
      SQLBooleanClauseTree tree;
      int[] statestk = new int[YYSTACKSIZE]; // State stack.
      LitebaseParserVal yyval =  new LitebaseParserVal(); // Used to return semantic values from action routines.
      LitebaseParserVal[] valstk; // Values stack.
      SQLSelectClause selectClause = select;      
      SQLBooleanClause booleanClauseAux;
      
      yychar = -1; // Impossible character which forces a read.
      statestk[0] = yystate; // Saves it.
      (valstk = new LitebaseParserVal[YYSTACKSIZE])[0] = yylval; // Saves empty value.

      while (true) // Until parsing is done, either correctly or with error.
      {
         doaction = true;

         // #### NEXT ACTION (from reduction table).
         for (yyn = yydefred[yystate]; yyn == 0; yyn = yydefred[yystate])
         {
            if (yychar < 0) // Is it a char, as expected?
            {
               yychar = yylex(); // Gets next token.

               // #### ERROR CHECK ####
               if (yychar < 0) // It it didn't work/error.
                  yychar = 0; // Changes it to default string (not -1!).
            }

            // Gets amount to shift by (shift index). 
            if (((yyn = yysindex[yystate]) != 0) && (yyn += yychar) >= 0 && yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
            {
               // #### NEXT STATE ####
               statestk[++stateptr] = yystate = yytable[yyn]; // It is in a new state, which must be saved.
               
               valstk[++valptr] = yylval; // Pushs lval as the input for the next rule.

               yychar = -1; // Since a token has been 'eaten', another one is needed.
               if (yyerrflag > 0) // Has an error been recovered?
                  --yyerrflag; // Continues.
               doaction = false; // Doesn't process yet.
               break; // Quits the yyn = 0 loop.
            }

            yyn = yyrindex[yystate]; // Reduces.

            if ((yyn != 0) && (yyn += yychar) >= 0 && yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
            {
               // Reduced!
               yyn = yytable[yyn];
               doaction = true; // Gets ready to execute.
               break; // Drops down to actions.
            }
            else  // ERROR RECOVERY
            {
               if (yyerrflag == 0)
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_SYNTAX_ERROR) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
               if (yyerrflag < 3) // Low error count?
               {
                  yyerrflag = 3;
                  while (true) // Executes until break.
                  {
                     if (stateptr < 0) // Checks for under amd overflow here.
                        throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                                     + "stack underflow. aborting..." // Note lower case 's'.
                                                     + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.'); 
                     
                     if (((yyn = yysindex[statestk[stateptr]]) != 0) && (yyn += YYERRCODE) >= 0 && yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
                     {
                        statestk[++stateptr] = yystate = yytable[yyn];
                        valstk[++valptr] = yylval;
                        doaction = false;
                        break;
                     }
                     else
                     {
                        if (stateptr < 0) // Checks for under & overflow here.
                           throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) + "Stack underflow. aborting..."
                                 + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.'); // Capital 'S'.
                        stateptr--;
                        valptr--;
                     }
                  }
               }
               else // Discards this token.
               {
                  if (yychar == 0)
                     return 1; // yyabort
                  yychar = -1; // Reads another.
               }
            }
         }
         if (!doaction) // any reason not to proceed?
            continue; // skip action
         if ((yym = yylen[yyn]) > 0) // Gets count of terminals on rhs. and tests if count of rhs is not 0.
            yyval = valstk[valptr - yym + 1]; // Gets current semantic value
         switch (yyn)
         {
            // ########## USER-SUPPLIED ACTIONS ##########
            case 1:
               // #line 66 "Litebase.y"
               command = SQLElement.CMD_CREATE_TABLE;
               tableList[0] = new SQLResultSetTable(valstk[valptr - 4].sval); // There's no alias table name here. 
               break;
               
            case 2:
               // #line 75 "Litebase.y"
               command = SQLElement.CMD_CREATE_INDEX;
               tableList[0] = new SQLResultSetTable(valstk[valptr - 3].sval); // There's no alias table name here.
               break;
               
            case 3:
               // #line 83 "Litebase.y"
               command = SQLElement.CMD_DROP_TABLE;
               tableList[0] = new SQLResultSetTable(valstk[valptr].sval); // There's no alias table name here.
               break;
               
            case 4:
               // #line 90 "Litebase.y"
               command = SQLElement.CMD_DROP_INDEX;
               tableList[0] = new SQLResultSetTable(valstk[valptr].sval); // There's no alias table name here.
               break;

            case 5:
               // #line 99 "Litebase.y"
               // If the default order is not used, the number of values must be equal to the number of fields.
               if (fieldNamesSize != 0 && fieldNamesSize != fieldValuesSize)
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH) 
                                             + '(' + fieldNamesSize + " != " + fieldValuesSize + ')');
                  
               command = SQLElement.CMD_INSERT;
               tableList[0] = new SQLResultSetTable(valstk[valptr - 5].sval); // There's no alias table name here.
               break;
               
            case 6:
               // #line 119 "Litebase.y"
               tableList[0] = new SQLResultSetTable(valstk[valptr - 1].sval); // There's no alias table name here.
               break;

            case 7:
               // #line 126 "Litebase.y"
               command = SQLElement.CMD_DELETE;
               tableList[0] = new SQLResultSetTable(valstk[valptr - 2].sval, aliasTableName);
               if (valstk[valptr].obj != null)
                  whereClause.expressionTree = (SQLBooleanClauseTree)valstk[valptr].obj;
               break;
               
            case 8:
               // #line 137 "Litebase.y"
               tableNameAux = (aliasTableName == null)? valstk[valptr - 4].sval : aliasTableName;

               if (secondFieldUpdateTableName != null) // Verifies if there was an error on field.tableName.
                  if (!tableNameAux.equals(firstFieldUpdateTableName))
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                                + LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + firstFieldUpdateAlias);
                  else
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                                + LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + secondFieldUpdateAlias);
               else
               if (firstFieldUpdateTableName != null && !tableNameAux.equals(firstFieldUpdateTableName))
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_COLUMN_NAME) + firstFieldUpdateAlias);

               command = SQLElement.CMD_UPDATE;
               tableList[0] = new SQLResultSetTable(valstk[valptr - 4].sval, aliasTableName);
               if (valstk[valptr].obj != null)
                  whereClause.expressionTree = (SQLBooleanClauseTree) valstk[valptr].obj;
               break;

            case 9:
               // #line 169 "Litebase.y"
               command = SQLElement.CMD_SELECT;
               selectClause.tableList = new SQLResultSetTable[tableListSize];
               Vm.arrayCopy(tableList, 0, selectClause.tableList, 0, tableListSize);

               // Checks if the first field is the wildcard. If so, assigns null to list, to indicate that all fields must be included.
               if (selectClause.fieldList[0].isWildcard)
               {
                  selectClause.fieldList = null;
                  selectClause.fieldsCount = 0;
               }
               else
               {
                  // Compacts the resulting field list.
                  SQLResultSetField[] compactFieldList = new SQLResultSetField[selectClause.fieldsCount];
                  Vm.arrayCopy(selectClause.fieldList, 0, compactFieldList, 0, selectClause.fieldsCount);
                  selectClause.fieldList = compactFieldList;
               }

               if (valstk[valptr - 2].obj != null) // whereClause
                  whereClause.expressionTree = ((SQLBooleanClauseTree)valstk[valptr - 2].obj);
               break;

            case 10:
               // #line 205 "Litebase.y"
               yyval.sval = valstk[valptr].sval;
               break;
               
            case 11:
               // #line 212 "Litebase.y"
               aliasTableName = null;
               break;
               
            case 12:
               // #line 216 "Litebase.y"
               aliasTableName = valstk[valptr].sval;
               break;
               
            case 13:
               // #line 223 "Litebase.y"
               fieldNames[fieldNamesSize++] = "*";
               break;

            case 17:
               // #line 253 "Litebase.y"
               fieldList[fieldListSize++] = new SQLFieldDefinition(valstk[valptr - 4].sval, SQLElement.SHORT, 0, isPrimaryKey, strDefault, 
                                                                                                                               isNotNull);
               break;

            case 18:
               // #line 258 "Litebase.y"
               fieldList[fieldListSize++] = new SQLFieldDefinition(valstk[valptr - 4].sval, SQLElement.INT, 0, isPrimaryKey, strDefault, 
                                                                                                                             isNotNull);
               break;

            case 19:
               // #line 263 "Litebase.y"
               fieldList[fieldListSize++] = new SQLFieldDefinition(valstk[valptr - 4].sval, SQLElement.LONG, 0, isPrimaryKey, strDefault, 
                                                                                                                              isNotNull);
               break;

            case 20:
               // #line 268 "Litebase.y"
               fieldList[fieldListSize++] = new SQLFieldDefinition(valstk[valptr - 4].sval, SQLElement.FLOAT, 0, isPrimaryKey, strDefault, 
                                                                                                                               isNotNull);
               break;

            case 21:
               // #line 273 "Litebase.y"
               fieldList[fieldListSize++] = new SQLFieldDefinition(valstk[valptr - 4].sval, SQLElement.DOUBLE, 0, isPrimaryKey, strDefault, 
                                                                                                                                isNotNull);
               break;

            case 22:
            case 26:
               // #line 280 "Litebase.y"
               try // The size must be a positive integer.
               {
                  if ((size = Convert.toInt(valstk[valptr - 5].sval)) <= 0)
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                                + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT)
                                                + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
               }
               catch (InvalidNumberException exception)
               {
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
               }
               fieldList[fieldListSize++] = new SQLFieldDefinition(valstk[valptr - 8].sval, (isNocase)? SQLElement.CHARS_NOCASE : SQLElement.CHARS, 
                                                                                            size, isPrimaryKey, strDefault, isNotNull);
               break;

            case 23:
               // #line 299 "Litebase.y"
               fieldList[fieldListSize++] = new SQLFieldDefinition(valstk[valptr - 4].sval, SQLElement.DATE, 0, isPrimaryKey, strDefault, isNotNull);
               break;

            case 24:
               // #line 304 "Litebase.y"
               fieldList[fieldListSize++] = new SQLFieldDefinition(valstk[valptr - 4].sval, SQLElement.DATETIME, 0, isPrimaryKey, strDefault, 
                                                                                                                                  isNotNull);
               break;

            case 25:
               // #line 310 "Litebase.y"
               try // The size must be a positive integer.
               {
                  if ((size = Convert.toInt(valstk[valptr - 3].sval)) <= 0)
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                                + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT)
                                                + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
               }
               catch (InvalidNumberException exception)
               {
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELD_SIZE_IS_NOT_INT)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
               }
               if (valstk[valptr - 2].ival == 'k') // kilobytes 
                  size <<= 10;
               else
               if (valstk[valptr - 2].ival == 'm') // megabytes 
                  size <<= 20;
               if (size > (10 << 20)) // There is a size limit for a blob!
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_BLOB_TOO_BIG)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

               fieldList[fieldListSize++] = new SQLFieldDefinition(valstk[valptr - 6].sval, SQLElement.BLOB, size, false, null, isNotNull);
               break;

            case 27:
               // #line 364 "Litebase.y"
               yyval.ival = 0;
               break;

            case 28:
               // #line 368 "Litebase.y"
               if (valstk[valptr].sval.equals("k") || valstk[valptr].sval.equals("m")) // The multiplier must be Kilo or Mega.
                  yyval.ival = valstk[valptr].sval.charAt(0);
               else
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_INVALID_MULTIPLIER)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
               break;

            case 29:
               // #line 383 "Litebase.y"
               isPrimaryKey = false; // No primary key.
               break;

            case 30:
               // #line 387 "Litebase.y"
               if (number_pk++ == 1) // There can't be two primary keys.
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_PRIMARY_KEY_ALREADY_DEFINED)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
               isPrimaryKey = true;
               break;

            case 31:
               // #line 398 "Litebase.y"
               isNocase = false;
               break;

            case 32:
               // #line 402 "Litebase.y"
               isNocase = true;
               break;

            case 33:
               // #line 409 "Litebase.y"
               strDefault = null;
               break;

            case 34:
               // #line 413 "Litebase.y"
               strDefault = valstk[valptr].sval;
               break;

            case 35:
            case 36:
               // #line 417 "Litebase.y"
               // #line 424 "Litebase.y"
               strDefault = null;
               break;

            case 37:
               // #line 428 "Litebase.y"
               strDefault = valstk[valptr].sval;
               break;

            case 38:
               // #line 432 "Litebase.y"
               strDefault = null;
               break;

            case 39:
               // #line 439 "Litebase.y"
               isNotNull = false;
               break;

            case 40:
               // #line 443 "Litebase.y"
               isNotNull = true;
               break;

            case 42:
               // #line 451 "Litebase.y"
               if (number_pk++ == 1) // There can't be two primary keys.
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_PRIMARY_KEY_ALREADY_DEFINED)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
               break;

            case 43:
            case 44:   
               // #line 461 "Litebase.y"
               // #line 465 "Litebase.y"
               fieldNames[fieldNamesSize++] = valstk[valptr].sval;
               break;

            case 47:
            case 48:
               // #line 483 "Litebase.y"
               // #line 489 "Litebase.y"
               // Adds the column name.
               fieldNames[fieldNamesSize++] = valstk[valptr].sval;
               break;

            case 49:
            case 50:
               // #line 498 "Litebase.y"
               // #line 502 "Litebase.y"
               fieldValues[fieldValuesSize++] = valstk[valptr].sval;
               break;

            case 51:
               // #line 506 "Litebase.y"
               fieldValues[fieldValuesSize++] = null;
               break;

            case 52:
               // #line 510 "Litebase.y"
               fieldValues[fieldValuesSize++] = "?";
               break;

            case 53:
            case 54:
               // #line 514 "Litebase.y"
               // #line 518 "Litebase.y"
               fieldValues[fieldValuesSize++] = valstk[valptr].sval;
               break;

            case 55:
               // #line 522 "Litebase.y"
               fieldValues[fieldValuesSize++] = null;
               break;

            case 56:
               // #line 526 "Litebase.y"
               fieldValues[fieldValuesSize++] = "?";
               break;

            case 57:
               // #line 533 "Litebase.y"
               fieldNames[0] = valstk[valptr].sval;
               break;

            case 58:
               // #line 538 "Litebase.y"
               command = SQLElement.CMD_ALTER_ADD_PK;
               break;

            case 59:
               // #line 542 "Litebase.y"
               command = SQLElement.CMD_ALTER_DROP_PK;
               break;

            case 60:
               // #line 549 "Litebase.y"
               command = SQLElement.CMD_ALTER_RENAME_TABLE;
               break;

            case 61:
               // #line 553 "Litebase.y"
               command = SQLElement.CMD_ALTER_RENAME_COLUMN;
               fieldNames[1] = valstk[valptr].sval;
               break;

            case 66:
               // #line 572 "Litebase.y"
               field = (SQLResultSetField)valstk[valptr - 2].obj;

               if (firstFieldUpdateTableName == null) // After the table name verification, the associated table name on the field name is discarded.
               {
                  if (field.tableName != null)
                  {
                     firstFieldUpdateTableName = field.tableName;
                     firstFieldUpdateAlias = field.alias;
                  }
               }
               else // Verifies if it is different.
               {
                  // There is an error: update has just one table. This error will raise an exception later on.
                  if (!field.tableName.equals(firstFieldUpdateTableName))
                  {
                     secondFieldUpdateTableName = field.tableName;
                     secondFieldUpdateAlias = field.alias;
                  }
               }
               fieldNames[fieldNamesSize++] = field.tableColName;
               break;

            case 67:
            case 68:
               // #line 601 "Litebase.y"
               // #line 605 "Litebase.y"
               fieldValues[fieldValuesSize++] = valstk[valptr].sval;
               break;
               
            case 69:
               // #line 609 "Litebase.y"
               fieldValues[fieldValuesSize++] = null;
               break;

            case 70:
               // #line 613 "Litebase.y"
               fieldValues[fieldValuesSize++] = "?";
               break;

            case 71:
               // #line 620 "Litebase.y"
               yyval.ival = 0;
               break;

            case 72:
               // #line 624 "Litebase.y"
               yyval.ival = 1;
               break;

            case 73:
               // #line 631 "Litebase.y"
               // Adds a willcard field.
               (selectClause.fieldList[selectClause.fieldsCount++] = new SQLResultSetField()).isWildcard = selectClause.hasWildcard = true;
               break;

            case 77:
               // #line 649 "Litebase.y"
               field = (SQLResultSetField)valstk[valptr - 1].obj;

               // If the alias_name is null, the alias must be the name of the column. This was already done before.

               // If the alias is null and the field is a virtual column, raises an exception, since virtual columns require explicit aliases.
               if (valstk[valptr].sval == null)
               {
                  if (field.isVirtual)
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                                + LitebaseMessage.getMessage(LitebaseMessage.ERR_REQUIRED_ALIAS)
                                                + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');
                  valstk[valptr].sval = field.alias; // Set before. The null alias name is filled as tableColName or tableName.tableColName.
               }

               resultFieldList = selectClause.fieldList;
               
               // Checks if the alias has not already been used by a predecessor.
               int i = selectClause.fieldsCount - 1;
               while (--i >= 0)
                  if (resultFieldList[i].alias.equals(valstk[valptr].sval))
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                                + LitebaseMessage.getMessage(LitebaseMessage.ERR_DUPLICATE_ALIAS) + valstk[valptr].sval);

               
               field.aliasHashCode = (field.alias = valstk[valptr].sval).hashCode(); // Assigns the alias.
               break;

            case 78:
               // #line 686 "Litebase.y"
               yyval.sval = null;
               break;

            case 79:
               // #line 690 "Litebase.y"
               yyval.sval = valstk[valptr].sval;
               break;

            case 80:
               // #line 697 "Litebase.y"
               if (selectClause.fieldsCount == SQLSelectClause.MAX_NUM_FIELDS) // The maximum number of fields can't be reached.
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELDS_OVERFLOW)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

               yyval.obj = selectClause.fieldList[selectClause.fieldsCount++] = field = (SQLResultSetField)valstk[valptr].obj;
               field.tableColHashCode = field.tableColName.hashCode();
               selectClause.hasRealColumns = true;
               break;

            case 81:
               // #line 714 "Litebase.y"
               if (selectClause.fieldsCount == SQLSelectClause.MAX_NUM_FIELDS) // The maximum number of fields can't be reached.
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELDS_OVERFLOW)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

               // Sets the field.
               yyval.obj = selectClause.fieldList[selectClause.fieldsCount++] = field = (SQLResultSetField)valstk[valptr].obj;
               field.isDataTypeFunction = field.isVirtual = true;
               field.dataType = SQLElement.dataTypeFunctionsTypes[field.sqlFunction];

               // Sets the function parameter.
               SQLResultSetField paramField = new SQLResultSetField();
               field.parameter = paramField;
               field.tableColHashCode = paramField.aliasHashCode = paramField.tableColHashCode = (paramField.alias = paramField.tableColName 
                                                                                               = field.tableColName).hashCode();

               break;

            case 82:
               // #line 747 "Litebase.y"
               yyval.obj = selectClause.fieldList[selectClause.fieldsCount++] = field = (SQLResultSetField)valstk[valptr].obj;

               if (selectClause.fieldsCount == SQLSelectClause.MAX_NUM_FIELDS) // The maximum number of fields can't be reached.
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_FIELDS_OVERFLOW)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

               // Sets the field.
               field.isAggregatedFunction = field.isVirtual = true;
               field.dataType = SQLElement.aggregateFunctionsTypes[field.sqlFunction];

               if (field.sqlFunction != SQLElement.FUNCTION_AGG_COUNT) // Sets the parameter, if there is such one.
               {
                  // Sets the function parameter.
                  paramField = new SQLResultSetField();
                  field.parameter = paramField;
                  field.tableColHashCode = paramField.aliasHashCode = paramField.tableColHashCode 
                                                                    = (paramField.alias = paramField.tableColName = field.tableColName).hashCode();
               }
               selectClause.hasAggFunctions = true; // Sets the select statement.
               break;

            case 83:
               // #line 787 "Litebase.y"
               yyval.obj = field = new SQLResultSetField();
               field.tableColName = field.alias = valstk[valptr].sval;
               break;

            case 84:
               // #line 793 "Litebase.y"
               yyval.obj = field = new SQLResultSetField();
               field.tableName = field.alias = valstk[valptr - 2].sval;
               field.alias += '.' + (field.tableColName = valstk[valptr].sval);
               break;

            case 87:
               // #line 809 "Litebase.y"
               if (aliasTableName == null)
                  aliasTableName = valstk[valptr - 2].sval;

               if (tables.exists(hash = aliasTableName.hashCode())) // The table name alias must be unique.
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_NOT_UNIQUE_ALIAS_TABLE) + aliasTableName);
               else
                  tables.put(hash, tables.size());
               tableList[tableListSize++] = new SQLResultSetTable(valstk[valptr - 2].sval, aliasTableName);
               break;

            case 90:
               // #line 837 "Litebase.y"
               yyval.obj = null;
               break;

            case 91:
               // #line 841 "Litebase.y"
               // Compacts the field list of the where clause.
               resultFieldList = new SQLResultSetField[(clause = whereClause).fieldsCount];
               Vm.arrayCopy(clause.fieldList, 0, resultFieldList, 0, clause.fieldsCount);
               clause.fieldList = resultFieldList;

               yyval.obj = valstk[valptr].obj;
               break;

            case 93:
               // #line 856 "Litebase.y"
               // Compacts the group by field list.
               resultFieldList = new SQLResultSetField[(group_order_by = group_by).fieldsCount];
               Vm.arrayCopy(group_order_by.fieldList, 0, resultFieldList, 0, group_order_by.fieldsCount);
               group_order_by.fieldList = resultFieldList;

               if (valstk[valptr].obj != null) // Adds the expression tree of the where clause.
                  havingClause.expressionTree = (SQLBooleanClauseTree) valstk[valptr].obj;
               break;

            case 94:
            case 95:
               // #line 878 "Litebase.y"
               // #line 878 "Litebase.y"
               selectClause.fieldsCount--;  // Removes this field from the select list.
               addColumnFieldOrderGroupBy((SQLResultSetField)valstk[valptr].obj, true, false); // Adds this field to the group by field list.
               break;

            case 96:
               // #line 888 "Litebase.y"
               yyval.obj = null;
               break;

            case 97:
               // #line 892 "Litebase.y"
               // Compacts the having clause field list.
               resultFieldList = new SQLResultSetField[(clause = havingClause).fieldsCount];
               Vm.arrayCopy(clause.fieldList, 0, resultFieldList, 0, clause.fieldsCount);
               clause.fieldList = resultFieldList;
               yyval.obj = valstk[valptr].obj;
               break;

            case 98:
               // #line 906 "Litebase.y"
               isWhereClause = false;
               break;

            case 100:
               // #line 914 "Litebase.y"
               // Compacts the order by field list.
               resultFieldList = new SQLResultSetField[(group_order_by = order_by).fieldsCount];
               Vm.arrayCopy(group_order_by.fieldList, 0, resultFieldList, 0, group_order_by.fieldsCount);
               group_order_by.fieldList = resultFieldList;
               break;

            case 103:
               // #line 930 "Litebase.y"
               selectClause.fieldsCount--;
               addColumnFieldOrderGroupBy((SQLResultSetField)valstk[valptr - 1].obj, (valstk[valptr].ival == 0), true);
               break;

            case 104:
            case 105:
               // #line 938 "Litebase.y"
               // #line 942 "Litebase.y"
               yyval.ival = 0;
               break;

            case 106:
               // #line 946 "Litebase.y"
               yyval.ival = 1;
               break;

            case 107:
               // #line 953 "Litebase.y"
               yyval.obj = valstk[valptr].obj;
               break;

            case 108:
               // #line 957 "Litebase.y"
               tree = setOperandType(SQLElement.OP_BOOLEAN_AND);
               (tree.leftTree = (SQLBooleanClauseTree)valstk[valptr - 2].obj).parent = tree;
               (tree.rightTree = (SQLBooleanClauseTree)valstk[valptr].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 109:
               // #line 964 "Litebase.y"
               // juliana@213_1: changed the way a tree with ORs is built in order to speed up queries with indices.
               tree = setOperandType(SQLElement.OP_BOOLEAN_OR);
               (tree.leftTree = (SQLBooleanClauseTree)valstk[valptr].obj).parent = tree;
               (tree.rightTree = (SQLBooleanClauseTree)valstk[valptr - 2].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 110:
               // #line 971 "Litebase.y"
               yyval.obj = valstk[valptr - 1].obj;
               break;

            case 111:
               // #line 975 "Litebase.y"
               // The parent node will be the negation operator and the expression will be the right tree.
               tree = setOperandType(SQLElement.OP_BOOLEAN_NOT);
               (tree.rightTree = (SQLBooleanClauseTree)valstk[valptr - 1].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 112:
               // #line 986 "Litebase.y"
               // The parent node will be the negation operator and the expression will be the right tree.
               tree = setOperandType(SQLElement.OP_BOOLEAN_NOT);
               (tree.rightTree = (SQLBooleanClauseTree)valstk[valptr].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 113:
               // #line 994 "Litebase.y"
               tree = (SQLBooleanClauseTree) valstk[valptr - 1].obj;
               (tree.leftTree = (SQLBooleanClauseTree)valstk[valptr - 2].obj).parent = tree;
               (tree.rightTree = (SQLBooleanClauseTree)valstk[valptr].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 114:
               // #line 1001 "Litebase.y"
               tree = setOperandType(SQLElement.OP_PAT_IS);
               (tree.rightTree = setOperandType(SQLElement.OP_PAT_NULL)).parent = tree;
               (tree.leftTree = (SQLBooleanClauseTree)valstk[valptr - 2].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 115:
               // #line 1008 "Litebase.y"
               tree = setOperandType(SQLElement.OP_PAT_IS_NOT);
               (tree.rightTree = setOperandType(SQLElement.OP_PAT_NULL)).parent = tree;
               (tree.leftTree = (SQLBooleanClauseTree)valstk[valptr - 3].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 116:
               // #line 1015 "Litebase.y"
               tree = setOperandType(SQLElement.OP_PAT_MATCH_LIKE);
               SQLBooleanClauseTree rightTree = new SQLBooleanClauseTree(getInstanceBooleanClause());
               rightTree.setOperandStringLiteral(valstk[valptr].sval);
               (tree.rightTree = rightTree).parent = tree;
               (tree.leftTree = (SQLBooleanClauseTree)valstk[valptr - 2].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 117:
               // #line 1025 "Litebase.y"
               tree = setOperandType(SQLElement.OP_PAT_MATCH_NOT_LIKE);
               rightTree = new SQLBooleanClauseTree(getInstanceBooleanClause());
               rightTree.setOperandStringLiteral(valstk[valptr].sval);
               (tree.rightTree = rightTree).parent = tree;
               (tree.leftTree = (SQLBooleanClauseTree)valstk[valptr - 3].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 118:
               // #line 1035 "Litebase.y"
               tree = setOperandType(SQLElement.OP_PAT_MATCH_LIKE);
               rightTree = new SQLBooleanClauseTree(clause = getInstanceBooleanClause());

               if (clause.paramCount == SQLElement.MAX_NUM_PARAMS) // There is a maximum number of parameters.
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MAX_NUM_PARAMS_REACHED)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + '.');

               rightTree.isParameter = true;
               clause.paramList[clause.paramCount++] = rightTree;
               (tree.rightTree = rightTree).parent = tree;
               (tree.leftTree = (SQLBooleanClauseTree)valstk[valptr - 2].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 119:
               // #line 1054 "Litebase.y"
               tree = setOperandType(SQLElement.OP_PAT_MATCH_NOT_LIKE);
               rightTree = new SQLBooleanClauseTree(clause = getInstanceBooleanClause());

               if (clause.paramCount == SQLElement.MAX_NUM_PARAMS) // There is a maximum number of parameters. 
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MAX_NUM_PARAMS_REACHED)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + ".");

               rightTree.isParameter = true;
               clause.paramList[clause.paramCount++] = rightTree;
               (tree.rightTree = rightTree).parent = tree;
               (tree.leftTree = (SQLBooleanClauseTree)valstk[valptr - 3].obj).parent = tree;
               yyval.obj = tree;
               break;

            case 120:
               // #line 1075 "Litebase.y"
               field = (SQLResultSetField)valstk[valptr].obj;
               index = (tree = new SQLBooleanClauseTree(booleanClauseAux = getInstanceBooleanClause())).booleanClause.fieldsCount;
               i = 1;
               tree.operandType = SQLElement.OP_IDENTIFIER;
               int hashCode = field.tableColHashCode = tree.nameSqlFunctionHashCode = tree.nameHashCode 
                                                                                    = (tree.operandName = field.tableColName).hashCode();

               // rnovais@570_108: Generates different index to repeted columns on where clause.
               // Ex: where year(birth) = 2000 and birth = '2008/02/11'.
               while (booleanClauseAux.fieldName2Index.exists(tree.nameSqlFunctionHashCode))
                  tree.nameSqlFunctionHashCode = (hashCode << 5) - hashCode + i++ - 48;
              
               if (index == SQLElement.MAX_NUM_COLUMNS)  // There is a maximum number of columns.
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MAX_NUM_FIELDS_REACHED));

               // Puts the hash code of the function name in the hash table.
               booleanClauseAux.fieldName2Index.put(tree.nameSqlFunctionHashCode, index);

               field.aliasHashCode = field.alias.hashCode(); // Sets the hash code of the field alias.

               // Puts the field in the field list.
               booleanClauseAux.fieldList[index] = field;
               booleanClauseAux.fieldsCount++; 
               
               yyval.obj = tree;
               break;

            case 121:
               // #line 1082 "Litebase.y"
               if ((tree = new SQLBooleanClauseTree(getInstanceBooleanClause())).operandValue == null)
                  tree.operandValue = new SQLValue();
               
               // Removes the '+' for positive numbers if it exists.
               tree.operandValue.asString = valstk[valptr].sval; // juliana@226a_20
               
               yyval.obj = tree;
               break;

            case 122:
               // #line 1091 "Litebase.y"
               (tree = new SQLBooleanClauseTree(getInstanceBooleanClause())).setOperandStringLiteral(valstk[valptr].sval);
               yyval.obj = tree;
               break;

            case 123:
               // #line 1098 "Litebase.y"
               tree = new SQLBooleanClauseTree(clause = getInstanceBooleanClause());

               // There is a maximum number of parameters.
               if (clause.paramCount == SQLElement.MAX_NUM_PARAMS)
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_START) 
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MAX_NUM_PARAMS_REACHED)
                                             + LitebaseMessage.getMessage(LitebaseMessage.ERR_MESSAGE_POSITION) + lexer.yyposition + ".");

               tree.isParameter = true;
               clause.paramList[clause.paramCount++] = tree;
               yyval.obj = tree;
               break;

            case 124:
               // #line 1113 "Litebase.y"
               i = 1;
               index = (tree = new SQLBooleanClauseTree(booleanClauseAux = getInstanceBooleanClause())).booleanClause.fieldsCount;
               tree.operandType = SQLElement.OP_IDENTIFIER;
               hashCode = tree.nameSqlFunctionHashCode = tree.nameHashCode = (tree.operandName 
                                                       = (field = (SQLResultSetField)valstk[valptr].obj).tableColName).hashCode();
   
               // generates different indexes to repeted columns on where clause. Ex: where year(birth) = 2000 and day(birth) = 3.
               while (booleanClauseAux.fieldName2Index.exists(tree.nameSqlFunctionHashCode))
                  tree.nameSqlFunctionHashCode = (hashCode << 5) - hashCode + i++ - 48;
              
               if (index == SQLElement.MAX_NUM_COLUMNS) // There is a maximum number of columns.
                  throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_MAX_NUM_FIELDS_REACHED));
   
               // Puts the hash code of the function name in the hash table.
               booleanClauseAux.fieldName2Index.put(tree.nameSqlFunctionHashCode, index);
   
               paramField = field.parameter = new SQLResultSetField(); // Creates the parameter field.
              
               // Sets the field and function parameter fields.
               paramField.alias = paramField.tableColName = field.alias = field.tableColName = tree.operandName;
               paramField.aliasHashCode = paramField.tableColHashCode = field.tableColHashCode = field.aliasHashCode = tree.nameHashCode;
               field.dataType = SQLElement.dataTypeFunctionsTypes[field.sqlFunction];
               field.isDataTypeFunction = field.isVirtual = true;
   
               // Puts the field in the field list.
               booleanClauseAux.fieldList[index] = field;
               booleanClauseAux.fieldsCount++;
              
               yyval.obj = tree;
               break;

            case 125:
               // #line 1122 "Litebase.y"
               yyval.obj = setOperandType(SQLElement.OP_REL_EQUAL);
               break;

            case 126:
               // #line 1126 "Litebase.y"
               yyval.obj = setOperandType(SQLElement.OP_REL_DIFF);
               break;

            case 127:
               // #line 1130 "Litebase.y"
               yyval.obj = setOperandType(SQLElement.OP_REL_GREATER);
               break;

            case 128:
               // #line 1134 "Litebase.y"
               yyval.obj = setOperandType(SQLElement.OP_REL_LESS);
               break;

            case 129:
               // #line 1138 "Litebase.y"
               yyval.obj = setOperandType(SQLElement.OP_REL_GREATER_EQUAL);
               break;

            case 130:
               // #line 1142 "Litebase.y"
               yyval.obj = setOperandType(SQLElement.OP_REL_LESS_EQUAL);
               break;

            case 131:
               // #line 1149 "Litebase.y"
               yyval.obj = field = (SQLResultSetField)valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_DT_ABS;
               break;

            case 132:
               // #line 1155 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_DT_UPPER;
               break;

            case 133:
               // #line 1161 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_DT_LOWER;
               break;

            case 134:
               // #line 1167 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_DT_YEAR;
               break;

            case 135:
               // #line 1173 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_DT_MONTH;
               break;

            case 136:
               // #line 1179 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_DT_DAY;
               break;

            case 137:
               // #line 1185 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_DT_HOUR;
               break;

            case 138:
               // #line 1191 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_DT_MINUTE;
               break;

            case 139:
               // #line 1197 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_DT_SECOND;
               break;

            case 140:
               // #line 1203 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_DT_MILLIS;
               break;

            case 141:
               // #line 1212 "Litebase.y"
               yyval.obj = field = new SQLResultSetField();
               field.sqlFunction = SQLElement.FUNCTION_AGG_COUNT;
               break;

            case 142:
               // #line 1218 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_AGG_MAX;
               break;

            case 143:
               // #line 1224 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_AGG_MIN;
               break;

            case 144:
               // #line 1230 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_AGG_AVG;
               break;

            case 145:
               // #line 1236 "Litebase.y"
               yyval.obj = field = (SQLResultSetField) valstk[valptr - 1].obj;
               field.sqlFunction = SQLElement.FUNCTION_AGG_SUM;
               
            // #line 2195 "LitebaseParser.java"
            // ########## END OF USER-SUPPLIED ACTIONS ##########
         }
         // #### Now let's reduce... ####
         stateptr -= yym; // Just reduced yylen states.
         yystate = statestk[stateptr]; // Gets the new state.
         if (valptr - yym >= 0) // Corresponding value drop.
            valptr -= yym;
         yym = yylhs[yyn]; // Selects next TERMINAL (on lhs).

         // Is it done? 'rest' state and at first TERMINAL.
         if (yystate == 0 && yym == 0)
         {
            statestk[++stateptr] = yystate = YYFINAL; // Explicitly says it's done and saves it.
            valstk[++valptr] = yyval; // Also saves the semantic value of the parsing.

            if (yychar < 0) // Is another character wanted?
            {
               yychar = yylex(); // Gets next character.
               if (yychar < 0)
                  yychar = 0; // Cleans it, if necessary.
            }
            if (yychar == 0) // Good exit (if lex returns 0 ;-).
               break; // Quits the loop--all DONE.
         }
         else // Else not done yet.
         { 
            // Gets next state and push, for next yydefred[].
            yyn = yygindex[yym]; // Finds out where to go.
            if ((yyn != 0) && (yyn += yystate) >= 0 && yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
               yystate = yytable[yyn]; // Gets new state.
            else
               yystate = yydgoto[yym]; // Else go to new defred.
            statestk[++stateptr] = yystate; // Going again, so pushs state & val...
            valstk[++valptr] = yyval; // ...for next action.
         }
      }
      return 0; // yyaccept!!
   }
}