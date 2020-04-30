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

/**
 * Defines the functions used by the lexical analizer.
 */

#include "LitebaseLex.h"

// juliana@253_9: improved Litebase parser.
/** 
 * The function which does the lexical analisys.
 *
 * @param parser The parser structure, which will hold the tree resulting from the parsing process.
 * @return The token code.
 */
int32 yylex(LitebaseParser* parser)
{
	TRACE("yyLex")
   int32 initialPos,
	      yybefore,
         hash = parser->select.sqlHashCode;
   JCharP zzReaderChars = parser->zzReaderChars;

   while (true)
   {
      while (parser->yycurrent <= ' ') // Skips blanks.
      {
         CALCULATE_HASH(parser->yycurrent);
         GET_YYCURRENT(parser->yycurrent);
      }

		// juliana@221_4: unicode strings were not dealt properly on Windows 32, Windows CE, Palm, iPhone, and Android.
      // Ends the lexycal analysis if the end of the file is found
      if (parser->yycurrent == 65535)
      {
         parser->select.sqlHashCode = hash;
         return PARSER_EOF;
      }

      if (is[parser->yycurrent] & IS_ALPHA)// Finds identifiers or reserved words. The first character must be a letter.
      {
         initialPos = parser->yyposition - 1;
         while (parser->yycurrent <= 255 && is[parser->yycurrent] & IS_ALPHA_DIGIT) // The other characters must be a letter, digit or '_'.
         {
            CALCULATE_HASH(parser->yycurrent);
            INSERT_CHAR(parser->yycurrent);
         }
         parser->select.sqlHashCode = hash;
         return findReserved(parser, initialPos); // Sees if the identifier is a reserved word or just an identifier.
      }

      if (is[parser->yycurrent] & IS_START_DIGIT) // Finds digits. The start of a digit can be a digit or a sign.
      {
         if (parser->yycurrent == '+') // juliana@226a_20: now passing +number (ex. +10) will work on sql clauses.
            GET_YYCURRENT(parser->yycurrent);

         initialPos = parser->yyposition - 1;
         CALCULATE_HASH('?');
         GET_YYCURRENT(parser->yycurrent);
         while (parser->yycurrent <= 255 && is[parser->yycurrent] & IS_DIGIT) // The second part of the nunber must be digits.
            INSERT_CHAR(parser->yycurrent);

         if (parser->yycurrent == '.') // Test if the number is not an integer.
         {
            INSERT_CHAR(parser->yycurrent);
            while (parser->yycurrent <= 255 && is[parser->yycurrent] & IS_DIGIT) // Gets the rest of the digits of the non-integer number.
               INSERT_CHAR(parser->yycurrent);
         }

         if (parser->yycurrent <= 255 && is[parser->yycurrent] & IS_END_NUM) // The number may finish with a letter indicating its type.
            INSERT_CHAR(parser->yycurrent);

         // Copies the number to yacc.
         parser->yylval = TC_heapAlloc(parser->heap, (yybefore = (parser->yyposition - initialPos)) << 1);
			xmemmove(parser->yylval, &zzReaderChars[initialPos], yybefore > 2? (yybefore - 1) << 1 : 2);
         parser->select.sqlHashCode = hash;
         return TK_NUMBER;
      }

      if (is[parser->yycurrent] & IS_RELATIONAL) // Finds tokens with two characters, or '>' or '<'.
      {
			yybefore = parser->yycurrent;
         CALCULATE_HASH(parser->yycurrent);
         GET_YYCURRENT(parser->yycurrent);

         if ((yybefore == '!' && parser->yycurrent == '=') || (yybefore == '<' && parser->yycurrent == '>')) // Sees if it is the different operator.
         {
            CALCULATE_HASH(parser->yycurrent);
            GET_YYCURRENT(parser->yycurrent);
            parser->select.sqlHashCode = hash;
            return TK_DIFF;
         }
         if (yybefore == '>' && parser->yycurrent == '=') // Sees if it is the greater or equal operator.
			{
            CALCULATE_HASH(parser->yycurrent);
            GET_YYCURRENT(parser->yycurrent);
            parser->select.sqlHashCode = hash;
            return TK_GREATER_EQUAL;
         }
         if (yybefore == '<' && parser->yycurrent == '=') // Sees if it is the less operator.
         {
            CALCULATE_HASH(parser->yycurrent);
            GET_YYCURRENT(parser->yycurrent);
            parser->select.sqlHashCode = hash;
            return TK_LESS_EQUAL;
         }
         if (yybefore == '>' || yybefore == '<') // > or <. 
         {
            parser->select.sqlHashCode = hash;
            return yybefore;
			}

         // Invalid operator.
         lbError(ERR_SYNTAX_ERROR, parser);
         return PARSER_ERROR; 
      }

      // Finds tokens with one character, punctuators or '='.
      // Sees if the tokens are arithmetic operators, '(', or ')'. In this case, returns the name of the token.
      if (is[parser->yycurrent] & (IS_PUNCT | IS_OPERATOR)) 
      {
         yybefore = parser->yycurrent;
         CALCULATE_HASH(parser->yycurrent);
         GET_YYCURRENT(parser->yycurrent);
         parser->select.sqlHashCode = hash;
         return yybefore;
      }

      // Sees if the token is an escape.
      // Sees if the token is a string of the type 'xxx'.
      if (parser->yycurrent == '\'')
      {
         int32 counter = 0,
               finalPos;
         JCharP str16;

         initialPos = parser->yyposition;
         CALCULATE_HASH('?');
         GET_YYCURRENT(parser->yycurrent);
         
         // juliana@225_6: a quote was not being correctly inserted in a string when not using prepared statements.
         while (parser->yycurrent != '\'')
         {
            if (parser->yycurrent == '\\') // Sees if there is an escape in the string.
            {
               GET_YYCURRENT(parser->yycurrent);
               INSERT_CHAR(parser->yycurrent);
               counter++;
            }

            // juliana@221_4: unicode strings were not dealt properly on Windows 32, Windows CE, Palm, iPhone, and Android.
            else if (parser->yycurrent == 65535) // The string must be closed before the end of the file.
            {
               GET_YYCURRENT(parser->yycurrent);
               parser->select.sqlHashCode = hash;
               lbError(ERR_SYNTAX_ERROR, parser);
               return PARSER_ERROR;
            }

            else
               INSERT_CHAR(parser->yycurrent); // Anything else can be inside the string.
         }
         INSERT_CHAR(parser->yycurrent);
         str16 = parser->yylval = TC_heapAlloc(parser->heap, (parser->yyposition - initialPos - 1 - counter) << 1);
         counter = 0;
         finalPos = parser->yyposition - 2;
         while (initialPos < finalPos)
         {
            if (zzReaderChars[initialPos] == '\\')
               initialPos++;
            str16[counter++] =zzReaderChars[initialPos++];
         }
         parser->select.sqlHashCode = hash;
         return TK_STR;
      }

      // Error.
      GET_YYCURRENT(parser->yycurrent);
      parser->select.sqlHashCode = hash;
      lbError(ERR_SYNTAX_ERROR, parser);
      return PARSER_ERROR;
   }
}

/** 
 * The initializer of the lexical analyser. It initializes the reserved words hash table and the kinds of token table based on ascii code.
 *
 * @return <code>false</code> if the reserved words hash table allocation fails; <code>true</code>, otherwise. 
 */
bool initLex()
{
	TRACE("initLex")
   int32 length = 'z' - 'a' + 1;

   // Initiate the array to select the kind of token.
   xmemzero(is, 255);

	// Give the values for the letters.
   xmemset(&is['a'], IS_ALPHA, length);
	xmemset(&is['A'], IS_ALPHA, length);

   // Letters denoting types of numbers can also be the end of the number.
   is['d'] |= IS_END_NUM;
   is['D'] |= IS_END_NUM;
   is['f'] |= IS_END_NUM;
   is['F'] |= IS_END_NUM;
   is['l'] |= IS_END_NUM;
   is['L'] |= IS_END_NUM;

   xmemset(&is['0'], IS_DIGIT, 10); // Gives the values for the digits.
   is['_'] = IS_ALPHA_DIGIT; // '_' can be part of an identifier.

   // + and - can be operators or sign of numbers.
   is['+'] = IS_SIGN;
   is['-'] = IS_SIGN;

   // The other operators and brackets.
   is['*'] = IS_OPERATOR;
   is['('] = IS_OPERATOR;
   is[')'] = IS_OPERATOR;

   // The symbols that can represent double tokens.
   is['<'] = IS_RELATIONAL;
   is['>'] = IS_RELATIONAL;
   is['!'] = IS_RELATIONAL;

   // The symbols that are treated as punctuators and =.
   is['.'] = IS_PUNCT;
   is[','] = IS_PUNCT;
   is['?'] = IS_PUNCT;
   is['='] = IS_PUNCT;

   // Populates the table with the reserved words.
   if ((reserved = TC_htNew(NUM_RESERVED + 1, null)).items)
   {
      TC_htPut32(&reserved, HT_ABS, TK_ABS);
      TC_htPut32(&reserved, HT_ADD, TK_ADD);
      TC_htPut32(&reserved, HT_ALTER, TK_ALTER);
      TC_htPut32(&reserved, HT_AND, TK_AND);
      TC_htPut32(&reserved, HT_AS, TK_AS);
      TC_htPut32(&reserved, HT_ASC, TK_ASC);
      TC_htPut32(&reserved, HT_AVG, TK_AVG);
      TC_htPut32(&reserved, HT_BLOB, TK_BLOB);
      TC_htPut32(&reserved, HT_BY, TK_BY);
      TC_htPut32(&reserved, HT_CHAR, TK_CHAR);
      TC_htPut32(&reserved, HT_COUNT, TK_COUNT);
      TC_htPut32(&reserved, HT_CREATE, TK_CREATE);
      TC_htPut32(&reserved, HT_DATE, TK_DATE);
      TC_htPut32(&reserved, HT_DATETIME, TK_DATETIME);
      TC_htPut32(&reserved, HT_DAY, TK_DAY);
      TC_htPut32(&reserved, HT_DEFAULT, TK_DEFAULT);
      TC_htPut32(&reserved, HT_DELETE, TK_DELETE);
      TC_htPut32(&reserved, HT_DESC, TK_DESC);
      TC_htPut32(&reserved, HT_DISTINCT, TK_DISTINCT);
      TC_htPut32(&reserved, HT_DOUBLE, TK_DOUBLE);
      TC_htPut32(&reserved, HT_DROP, TK_DROP);
      TC_htPut32(&reserved, HT_FLOAT, TK_FLOAT);
      TC_htPut32(&reserved, HT_FROM, TK_FROM);
      TC_htPut32(&reserved, HT_GROUP, TK_GROUP);
      TC_htPut32(&reserved, HT_HAVING, TK_HAVING);
      TC_htPut32(&reserved, HT_HOUR, TK_HOUR);
      TC_htPut32(&reserved, HT_INDEX, TK_INDEX);
      TC_htPut32(&reserved, HT_INSERT, TK_INSERT);
      TC_htPut32(&reserved, HT_INT, TK_INT);
      TC_htPut32(&reserved, HT_INTO, TK_INTO);
      TC_htPut32(&reserved, HT_IS, TK_IS);
      TC_htPut32(&reserved, HT_KEY, TK_KEY);
      TC_htPut32(&reserved, HT_LIKE, TK_LIKE);
      TC_htPut32(&reserved, HT_LONG, TK_LONG);
      TC_htPut32(&reserved, HT_LOWER, TK_LOWER);
      TC_htPut32(&reserved, HT_MAX, TK_MAX);
      TC_htPut32(&reserved, HT_MILLIS, TK_MILLIS);
      TC_htPut32(&reserved, HT_MIN, TK_MIN);
      TC_htPut32(&reserved, HT_MINUTE, TK_MINUTE);
      TC_htPut32(&reserved, HT_MONTH, TK_MONTH);
      TC_htPut32(&reserved, HT_NOCASE, TK_NOCASE);
      TC_htPut32(&reserved, HT_NOT, TK_NOT);
      TC_htPut32(&reserved, HT_NULL, TK_NULL) ;
      TC_htPut32(&reserved, HT_ON, TK_ON);
      TC_htPut32(&reserved, HT_OR, TK_OR);
      TC_htPut32(&reserved, HT_ORDER, TK_ORDER);
      TC_htPut32(&reserved, HT_PRIMARY, TK_PRIMARY);
      TC_htPut32(&reserved, HT_RENAME, TK_RENAME);
      TC_htPut32(&reserved, HT_SECOND, TK_SECOND);
      TC_htPut32(&reserved, HT_SELECT, TK_SELECT);
      TC_htPut32(&reserved, HT_SET, TK_SET);
      TC_htPut32(&reserved, HT_SHORT, TK_SHORT);
      TC_htPut32(&reserved, HT_SUM, TK_SUM);
      TC_htPut32(&reserved, HT_TABLE, TK_TABLE);
      TC_htPut32(&reserved, HT_TO, TK_TO);
      TC_htPut32(&reserved, HT_UPDATE, TK_UPDATE);
      TC_htPut32(&reserved, HT_UPPER, TK_UPPER);
      TC_htPut32(&reserved, HT_VALUES, TK_VALUES);
      TC_htPut32(&reserved, HT_VARCHAR, TK_VARCHAR);
      TC_htPut32(&reserved, HT_WHERE, TK_WHERE);
      TC_htPut32(&reserved, HT_YEAR, TK_YEAR);
      return true;
   }
   return false;
}

// juliana@253_9: improved Litebase parser.
/** 
 * Finds if the token is a reserved word or just an identifier.
 *
 * @param parser The parser structure, which will hold the tree resulting from the parsing process.
 * @param initialPos The initial position of the current token in the SQL string.
 * @return The code of a reserved word token or an identifirer token.
 */
int32 findReserved(LitebaseParser* parser, int32 initialPos)
{
	TRACE("findReserved")
   int32 found = -1;
	bool hasNumber = false;
	char buf[MAX_RESERVED_SIZE];
	 
	TC_JCharP2CharPBuf(&parser->zzReaderChars[initialPos], (8 < (parser->yyposition - initialPos - 1))? 8 : parser->yyposition - initialPos - 1, buf);
   TC_CharPToLower(buf);

	// juliana@213_7: using numbers in the identifiers may cause a colision with the reserved word hash table.
   while (++found < 8 && buf[found])
		if (buf[found] >= '0' && buf[found] <= '9')
			hasNumber = true;

   if (!hasNumber && (found = TC_htGet32Inv(&reserved, TC_hashCode(buf))) >= 0)
      return found;

   parser->yylval = TC_heapAlloc(parser->heap, found = parser->yyposition - initialPos);
   TC_JCharP2CharPBuf(&parser->zzReaderChars[initialPos], found - 1, parser->yylval);
   TC_CharPToLower(parser->yylval);
   return TK_IDENT;
}

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if the function <code>initLex()</code> initializes all the lex global variable properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(initLex)
{
   int32 i = '@';
   UNUSED(currentContext)

   while (++i < '[') // The values for the letters.
   {
      ASSERT2_EQUALS(I32, is[i] & IS_ALPHA, IS_ALPHA);
      ASSERT2_EQUALS(I32, is[i + 32] & IS_ALPHA, IS_ALPHA);
   }
   
   // Letters denoting types of numbers can also be the end of the number.
   ASSERT2_EQUALS(I32, is['d'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['D'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['f'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['F'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['l'] & IS_END_NUM, IS_END_NUM);
   ASSERT2_EQUALS(I32, is['L'] & IS_END_NUM, IS_END_NUM);
   
   // The values for the digits.
   i = '/';
   while (++i < ':') 
      ASSERT2_EQUALS(U8, is[i], IS_DIGIT);

   ASSERT2_EQUALS(I32, is['_'] & (IS_ALPHA | IS_DIGIT), (IS_ALPHA | IS_DIGIT)); // '_' can be part of an identifier.

   // + and - can be operators or sign of numbers.
   ASSERT2_EQUALS(U8, is['+'], IS_SIGN);
   ASSERT2_EQUALS(U8, is['-'], IS_SIGN);

   // The other operators and brackets.
   ASSERT2_EQUALS(U8, is['*'], IS_OPERATOR);
   ASSERT2_EQUALS(U8, is['('], IS_OPERATOR);
   ASSERT2_EQUALS(U8, is[')'], IS_OPERATOR);

   // The symbols that can represent double tokens.
   ASSERT2_EQUALS(U8, is['<'], IS_RELATIONAL);
   ASSERT2_EQUALS(U8, is['>'], IS_RELATIONAL);
   ASSERT2_EQUALS(U8, is['!'], IS_RELATIONAL);

   // The symbols that are treated as punctuators and =.
   ASSERT2_EQUALS(U8, is['.'], IS_PUNCT);
   ASSERT2_EQUALS(U8, is[','], IS_PUNCT);
   ASSERT2_EQUALS(U8, is['?'], IS_PUNCT);
   ASSERT2_EQUALS(U8, is['='], IS_PUNCT);

   // Checks if the hash codes of the reserved words are in the hash table.
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("abs")), TK_ABS); 
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("add")), TK_ADD);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("alter")), TK_ALTER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("and")), TK_AND);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("as")), TK_AS);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("asc")), TK_ASC);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("avg")), TK_AVG);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("blob")), TK_BLOB);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("by")), TK_BY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("char")), TK_CHAR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("count")), TK_COUNT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("create")), TK_CREATE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("date")), TK_DATE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("datetime")), TK_DATETIME);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("day")), TK_DAY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("default")), TK_DEFAULT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("delete")), TK_DELETE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("desc")), TK_DESC);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("distinct")), TK_DISTINCT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("double")), TK_DOUBLE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("drop")), TK_DROP);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("float")), TK_FLOAT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("from")), TK_FROM);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("group")), TK_GROUP);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("having")), TK_HAVING);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("hour")), TK_HOUR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("index")), TK_INDEX);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("insert")), TK_INSERT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("int")), TK_INT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("into")), TK_INTO);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("is")), TK_IS);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("key")), TK_KEY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("like")), TK_LIKE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("long")), TK_LONG);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("lower")), TK_LOWER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("max")), TK_MAX);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("millis")), TK_MILLIS);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("min")), TK_MIN);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("minute")), TK_MINUTE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("month")), TK_MONTH);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("nocase")), TK_NOCASE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("not")), TK_NOT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("null")), TK_NULL) ;
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("on")), TK_ON);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("or")), TK_OR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("order")), TK_ORDER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("primary")), TK_PRIMARY);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("rename")), TK_RENAME);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("second")), TK_SECOND);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("select")), TK_SELECT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("set")), TK_SET);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("short")), TK_SHORT);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("sum")), TK_SUM);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("table")), TK_TABLE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("to")), TK_TO);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("update")), TK_UPDATE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("upper")), TK_UPPER);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("values")), TK_VALUES);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("varchar")), TK_VARCHAR);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("where")), TK_WHERE);
   ASSERT2_EQUALS(I32, TC_htGet32(&reserved, TC_hashCode("year")), TK_YEAR);

finish: ;
}

#endif
