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
 * Declares the functions used by the lexical analizer.
 */

#ifndef LITEBASE_LITEBASELEX_H
#define LITEBASE_LITEBASELEX_H

#include "Litebase.h"

/** 
 * The function which does the lexical analisys.
 *
 * @param parser The parser structure, which will hold the tree resulting from the parsing process.
 * @return The token code.
 */
int32 yylex(LitebaseParser* parser);

/** 
 * The initializer of the lexical analyser. It initializes the reserved words hash table and the kinds of token table based on ascii code.
 *
 * @return <code>false</code> if the reserved words hash table allocation fails; <code>true</code>, otherwise. 
 */
bool initLex();

/** 
 * Finds if the token is a reserved word or just an identifier.
 *
 * @param parser The parser structure, which will hold the tree resulting from the parsing process.
 * @param initialPos The initial position of the current token in the SQL string.
 * @return The code of a reserved word token or an identifirer token.
 */
int32 findReserved(LitebaseParser* parser, int32 initialPos);

#ifdef ENABLE_TEST_SUITE

/**
 * Tests if the function <code>initLex()</code> initializes all the lex global variable properly.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
void test_initLex(TestSuite* testSuite, Context currentContext);  

#endif

#endif
