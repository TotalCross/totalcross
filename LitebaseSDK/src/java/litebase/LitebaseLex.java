// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package litebase;

import totalcross.sys.*;

/**
 * Used to do SQL lexical analysis.
 */
class LitebaseLex
{  
   // Constants used to differenciate the kinds of tokens.
   /**
    * Indicates that a character is from 'A' to 'Z' or 'a' to 'z'.
    */
   private static final int IS_ALPHA = 1;

   /**
    * Indicates that a character is from '0' to '9'.
    */
   private static final int IS_DIGIT = 2;

   /**
    * Indicates that a character is + or -.
    */
   private static final int IS_SIGN = 4;

   /**
    * Indicates that a character is a possible end of a number. Its values can be: 
    * <LU> 
    *     <LI>'d' or 'D' - double</LI> 
    *     <LI>'f' or 'F' - float</LI> 
    *     <LI>'l' or 'L' - long</LI>
    * </LU>
    */
   private static final int IS_END_NUM = 8;

   /** Indicates that a character is the part of a relational operator. It can be: '!', '>', or '<'.
    */
   private static final int IS_RELATIONAL = 16;

   /**
    * Indicates that a character is a punctuation symbol. It can be: '.', '?', ',', or '='.
    */
   private static final int IS_PUNCT = 32;

   /**
    * Indicates that a character is an operator symbol. It can be: '+', '-', '*', '\', '^', '(', or ')'.
    */
   private static final int IS_OPERATOR = 64;

   /**
    * Indicates that a character is from 'a' to 'z', from 'A' to 'Z', from '0' to '9', or is an underscore '_'.
    */
   private static final int IS_ALPHA_DIGIT = IS_ALPHA | IS_DIGIT;

   /**
    * Indicates that a character is the beginning of a number. It can be a digit or a sign.
    */
   private static final int IS_START_DIGIT = IS_DIGIT | IS_SIGN;

   /**
    * This character denotes the end of file.
    */
   static final int YYEOF = -1; // juliana@253_9: improved Litebase parser.

   /** 
    * The parser. 
    */
   LitebaseParser yyparser; // juliana@224_2: improved memory usage on BlackBerry.

   /**
    * The input device.
    */
   String zzReaderChars; // juliana@224_2: improved memory usage on BlackBerry.

   /**
    * Hash table with the reserved words.
    */
   private static ReservedHashtable reserved; // juliana@213_7: changed to Hashtable.

   /**
    * The last char read.
    */
   int yycurrent; // juliana@224_2: improved memory usage on BlackBerry.

   /**
    * The last position of the buffer read.
    */
   int yyposition;

   /**
    * The name of the token
    */
   StringBuffer nameToken;

   /**
    * An array to help the selection of the kind of token.
    */
   private static byte[] is = new byte[256];

   static
   {
      // Initiates the array to select the kind of token.
      // Gives the values for the letters.
      Convert.fill(is, 'a', 'z' + 1, IS_ALPHA);
      Convert.fill(is, 'A', 'Z' + 1, IS_ALPHA);

      // Letters denoting types of numbers can also be the end of the number.
      is['d'] |= IS_END_NUM;
      is['D'] |= IS_END_NUM;
      is['f'] |= IS_END_NUM;
      is['F'] |= IS_END_NUM;
      is['l'] |= IS_END_NUM;
      is['L'] |= IS_END_NUM;

      Convert.fill(is, '0', '9' + 1, IS_DIGIT); // Gives the values for the digits.

      is['_'] = IS_ALPHA_DIGIT; // '_' can be part of an identifier.
      is['+'] = is['-'] = IS_SIGN; // + and - are only sign of numbers.
      is['*'] = is['('] = is[')'] = IS_OPERATOR; // The other operators and brackets.
      is['<'] = is['>'] = is['!'] = IS_RELATIONAL; // The symbols that can represent double tokens.
      is['.'] = is[','] = is['?'] = is['='] = IS_PUNCT; // The symbols that are treated as punctuations and '='.

      // juliana@213_7: changed to Hashtable.
      // Creates and populates the hash table of reserved words.
      reserved = new ReservedHashtable(61);
      
      // juliana@224_2: improved memory usage on BlackBerry.
      reserved.put("abs", LitebaseParser.TK_ABS);
      reserved.put("add", LitebaseParser.TK_ADD);
      reserved.put("alter", LitebaseParser.TK_ALTER);
      reserved.put("and", LitebaseParser.TK_AND);
      reserved.put("as", LitebaseParser.TK_AS);
      reserved.put("asc", LitebaseParser.TK_ASC);
      reserved.put("avg", LitebaseParser.TK_AVG);
      reserved.put("blob", LitebaseParser.TK_BLOB);
      reserved.put("by", LitebaseParser.TK_BY);
      reserved.put("char", LitebaseParser.TK_CHAR);
      reserved.put("count", LitebaseParser.TK_COUNT);
      reserved.put("create", LitebaseParser.TK_CREATE);
      reserved.put("date", LitebaseParser.TK_DATE);
      reserved.put("datetime", LitebaseParser.TK_DATETIME);
      reserved.put("day", LitebaseParser.TK_DAY);
      reserved.put("default", LitebaseParser.TK_DEFAULT);
      reserved.put("delete", LitebaseParser.TK_DELETE);
      reserved.put("desc", LitebaseParser.TK_DESC);
      reserved.put("distinct", LitebaseParser.TK_DISTINCT);
      reserved.put("double", LitebaseParser.TK_DOUBLE);
      reserved.put("drop", LitebaseParser.TK_DROP);
      reserved.put("float", LitebaseParser.TK_FLOAT);
      reserved.put("from", LitebaseParser.TK_FROM);
      reserved.put("group", LitebaseParser.TK_GROUP);
      reserved.put("having", LitebaseParser.TK_HAVING);
      reserved.put("hour", LitebaseParser.TK_HOUR);
      reserved.put("index", LitebaseParser.TK_INDEX);
      reserved.put("insert", LitebaseParser.TK_INSERT);
      reserved.put("int", LitebaseParser.TK_INT);
      reserved.put("into", LitebaseParser.TK_INTO);
      reserved.put("is", LitebaseParser.TK_IS);
      reserved.put("key", LitebaseParser.TK_KEY);
      reserved.put("like", LitebaseParser.TK_LIKE);
      reserved.put("long", LitebaseParser.TK_LONG);
      reserved.put("lower", LitebaseParser.TK_LOWER);
      reserved.put("max", LitebaseParser.TK_MAX);
      reserved.put("millis", LitebaseParser.TK_MILLIS);
      reserved.put("min", LitebaseParser.TK_MIN);
      reserved.put("minute", LitebaseParser.TK_MINUTE);
      reserved.put("month", LitebaseParser.TK_MONTH);
      reserved.put("nocase", LitebaseParser.TK_NOCASE);
      reserved.put("not", LitebaseParser.TK_NOT);
      reserved.put("null", LitebaseParser.TK_NULL);
      reserved.put("on", LitebaseParser.TK_ON);
      reserved.put("or", LitebaseParser.TK_OR);
      reserved.put("order", LitebaseParser.TK_ORDER);
      reserved.put("primary", LitebaseParser.TK_PRIMARY);
      reserved.put("rename", LitebaseParser.TK_RENAME);
      reserved.put("second", LitebaseParser.TK_SECOND);
      reserved.put("select", LitebaseParser.TK_SELECT);
      reserved.put("set", LitebaseParser.TK_SET);
      reserved.put("short", LitebaseParser.TK_SHORT);
      reserved.put("sum", LitebaseParser.TK_SUM);
      reserved.put("table", LitebaseParser.TK_TABLE);
      reserved.put("to", LitebaseParser.TK_TO);
      reserved.put("update", LitebaseParser.TK_UPDATE);
      reserved.put("upper", LitebaseParser.TK_UPPER);
      reserved.put("values", LitebaseParser.TK_VALUES);
      reserved.put("varchar", LitebaseParser.TK_VARCHAR);
      reserved.put("where", LitebaseParser.TK_WHERE);
      reserved.put("year", LitebaseParser.TK_YEAR);
   }

   // juliana@224_2: improved memory usage on BlackBerry.
   // juliana@253_9: improved Litebase parser.
   /**
    * The method which does the lexical analizys.
    *
    * @return The token itself or a code that represents it.
    */
   int yylex()
   {
      int zzlen = zzReaderChars.length(),
          yybefore,
          initialPos;
      int value;

      while (true)
      {
         while (yycurrent <= ' ') // Skips blanks
         {
            if (yyposition < zzlen)
               yycurrent = zzReaderChars.charAt(yyposition++);
            else
            {
               yycurrent = YYEOF;
               break;
            }
         }

         if (yycurrent <= YYEOF) // Ends the lexical analysis if the end of the file is found.
            return YYEOF;

         // Finds identifiers or reserved words. 
         if ((is[yycurrent] & IS_ALPHA) != 0) // The first character must be a letter.
         {
            int hashCode = 0;

            initialPos = yyposition - 1;
            nameToken.setLength(0); // Initializes the current identifier token.
            
            while (yycurrent >= 0 && (is[yycurrent] & IS_ALPHA_DIGIT) != 0) // The other characters must be a letter, digit, or '_'.
            { 
               if ('A' <= yycurrent && yycurrent <= 'Z') // Converts to lower case.
                  nameToken.append((char)(yycurrent += 32));
               else
                  nameToken.append((char)yycurrent);
               hashCode = (hashCode << 5) - hashCode + yycurrent;
               yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
            }

            // juliana@213_7: changed to Hashtable and tests for colision.
            // Sees if the identifier is a reserved word or just an identifier.
            if ((value = reserved.get(hashCode, nameToken)) != -1)
               return value;           
            yyparser.yylval = nameToken.toString();
            return LitebaseParser.TK_IDENT;
         }

         // Finds digits.
         if ((is[yycurrent] & IS_START_DIGIT) != 0) // The start of a digit can be a digit or a sign.
         {
            if (yycurrent == '+') // juliana@226a_20: now passing +number (ex. +10) will work on sql clauses.
               yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
               
            initialPos = yyposition - 1;
            yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;

            while (yycurrent >= 0 && (is[yycurrent] & IS_DIGIT) != 0) // The second part of the number must be digits.
               yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;

            if (yycurrent == '.') // Tests if the number is not an integer.
            {
               yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;

               while (yycurrent >= 0 && (is[yycurrent] & IS_DIGIT) != 0) // Gets the rest of the digits of the non-integer number.
                  yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
            }

            // The number may finish with a letter indicating its type.
            if (yycurrent >= 0 && (is[yycurrent] & IS_END_NUM) != 0)
               yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;

            yyparser.yylval = zzReaderChars.substring(initialPos, yyposition - (yycurrent >= 0? 1 : 0));
            return LitebaseParser.TK_NUMBER;
         }
         
         if ((is[yycurrent] & IS_RELATIONAL) != 0) // Finds tokens with two characters, or '>' or '<'.
         {
            yybefore = yycurrent;
            yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
            if ((yybefore == '!' && yycurrent == '=') || (yybefore == '<' && yycurrent == '>'))
            {
               yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
               return LitebaseParser.TK_DIFF; // != or <>.
            }
            if (yybefore == '>' && yycurrent == '=')
            {
               yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
               return LitebaseParser.TK_GREATER_EQUAL; // >=.
            }
            if (yybefore == '<' && yycurrent == '=')
            {
               yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
               return LitebaseParser.TK_LESS_EQUAL; // <=.
            }
            if (yybefore == '>' || yybefore == '<') // > or <.
               return yybefore; 

            yyparser.yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
         }

         // Finds tokens with one character, punctuators or '='.
         // Sees if the tokens are arithmetic operators, '(', or ')'. In this case, returns the name of the token.
         if ((is[yycurrent] & (IS_PUNCT | IS_OPERATOR)) != 0) 
         {
            yybefore = yycurrent;
            yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
            return yybefore;
         }

         // juliana@225_6: a quote was not being correctly inserted in a string when not using prepared statements.
         if (yycurrent == '\'') // Sees if the token is a string of the type 'xxx'.
         {
            nameToken.setLength(0);
            yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
            initialPos = yyposition - 1;
            
            boolean needsNewString = false;
            
            while (yycurrent != '\'') 
            {
               // juliana@238_3: the parser now accepts strings ending with \\.
               if (yycurrent == '\\') // Sees if there is an escape in the string.
               {
                  yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
                  nameToken.append((char)yycurrent);
                  yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
                  needsNewString = true;
               }
               else if (yycurrent == YYEOF) // The string must be closed before the end of the file.
                  yyparser.yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
               else // Anything else can be inside the string.
               {
                  nameToken.append((char)yycurrent);
                  yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
               }
            }
            yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
            
            if (nameToken.length() == 0)
               yyparser.yylval = "";
            else if (needsNewString) 
               yyparser.yylval = nameToken.toString();
            else
               yyparser.yylval = zzReaderChars.substring(initialPos, yyposition - (yycurrent >= 0? 2 : 1));
            return LitebaseParser.TK_STR;
         }

         // Error: invalid token.
         yycurrent = (yyposition < zzlen)? zzReaderChars.charAt(yyposition++) : YYEOF;
         yyparser.yyerror(LitebaseMessage.ERR_SYNTAX_ERROR);
      }
   }
}
