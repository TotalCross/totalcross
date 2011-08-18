/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

/* A Bison parser, made by GNU Bison 1.875b.  */

/* Skeleton parser for Yacc-like parsing with Bison,
   Copyright (C) 1984, 1989, 1990, 2000, 2001, 2002, 2003 Free Software Foundation, Inc.

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.  */

/* As a special exception, when this file is copied by Bison into a
   Bison output file, you may use that output file without restriction.
   This special exception was added by the Free Software Foundation
   in version 1.24 of Bison.  */

/* Written by Richard Stallman by simplifying the original so called
   ``semantic'' parser.  */

/* All symbols defined below should begin with yy or YY, to avoid
   infringing on user name space.  This should be done even for local
   variables, as they might otherwise be expanded by user macros.
   There are some unavoidable exceptions within include files to
   define necessary library symbols; they are noted "INFRINGES ON
   USER NAME SPACE" below.  */

#include "Litebase.h"

/**
 * The function that parses the sql string.
 *
 * @param YYPARSE_PARAM The parser structure.
 * @return <code>true</code> if there are no parser errors; <code>false</code>, otherwise. 
 */
bool yyparse(VoidP YYPARSE_PARAM)
{
   int32 yystate = 0,
         yyn,
         yytoken = 0, // Lookahead token as an internal (translated) token number.
         number_pk = 0, // Number of primary key definitions already found.
         yylen, // When reducing, the number of symbols on the RHS of the reduced rule.
         yychar = YYEMPTY, // The lookahead symbol. Causes a token to be read.   
         size,
         j = 0,
         i = 0;
   bool error;
   IntBuf buffer;
   CharP firstFieldUpdateTableName = null, // First update table name found.
         firstFieldUpdateAlias = null, // First update alias found.
         secondFieldUpdateTableName = null, // Second update table name found.
         secondFieldUpdateAlias = null, // Second update alias found.
         tableNameAux,
         charPAux1,
         charPAux2;
   VoidP voidPAux;
   Heap heap = parserTP->heap;
   SQLResultSetTable** tableList = parserTP->tableList;
   SQLResultSetField**selectFieldList = parserTP->selectFieldList;
   SQLResultSetField* field;
   SQLResultSetField* paramField;
   SQLResultSetField** whereFieldList = parserTP->whereFieldList;
   SQLResultSetField** havingFieldList = parserTP->havingFieldList;
   SQLFieldDefinition** fieldList = parserTP->fieldList;
   CharP* fieldNames = parserTP->fieldNames;
   JCharP* fieldValues = parserTP->fieldValues;
   SQLSelectClause* select = &parserTP->select;
   Hashtable* tables = &parserTP->tables;
   Hashtable* fieldName2Index;
   SQLBooleanClauseTree* tree;
   SQLBooleanClauseTree* rightTree;
   SQLBooleanClauseTree** whereParamList = parserTP->whereParamList;
   SQLBooleanClauseTree** havingParamList = parserTP->havingParamList;
   SQLBooleanClause* booleanClause;
 
   // Three stacks and their tools:
   // 'yyss': related to states,
   // 'yyvs': related to semantic values,
   // 'yyls': related to locations.
   // Initializes the stack pointers. Wastes one element of value and location stack so that they stay on the same level as the state stack. The 
   // wasted elements are never initialized. 

   // The state stack. 
   int16 yyss[YYINITDEPTH];
   int16* yyssp = yyss;

   // The semantic value stack. 
   YYSTYPE yyvs[YYINITDEPTH];
   YYSTYPE* yyvsp = yyvs;

   // The variable used to return semantic value and location from the action routines.
   YYSTYPE yyval; 

yynewstate: // yynewstate -- Push a new state, which is found in yystate.  
   *yyssp = yystate;

   // Does appropriate processing given the current state. Reads a lookahead token if one is needed and one doens't already have one.

   // First tries to decide what to do without reference to lookahead token.  
   if ((yyn = yypact[yystate]) == YYPACT_NINF)
      goto yydefault;

   // Not known => gets a lookahead token if one doesn't already have one. 

   // YYCHAR is either YYEMPTY or YYEOF or a valid lookahead symbol. 
   if (yychar == YYEMPTY)
      yychar = yylex(parserTP);

   if (yychar <= YYEOF)
      yychar = yytoken = YYEOF;
   else
      yytoken = YYTRANSLATE(yychar);

   // If the proper action on seeing token YYTOKEN is to reduce or to detect an error, takes that action.
   if ((yyn += yytoken) < 0 || YYLAST < yyn || yycheck[yyn] != yytoken)
      goto yydefault;
   if ((yyn = yytable[yyn]) <= 0)
   {
      if (!yyn || yyn == YYTABLE_NINF)
      {
         TC_throwExceptionNamed(parserTP->context, "litebase.SQLParseException", "syntax error", parserTP->yyposition);
         return 1;
      }
      yyn = -yyn;
      goto yyreduce;
    }

   if (yyn == YYFINAL)
      return 0;

   // Shifts the lookahead token.  
  
   if (yychar != YYEOF) // Discards the token being shifted unless it is eof.
      yychar = YYEMPTY;

   *++yyvsp = yylval;

   yystate = yyn;
  
   // In all cases, when one gets here, the value and location stacks have just been pushed. so pushing a state here evens the stacks.
   yyssp++;
   goto yynewstate;

yydefault: // Does the default action for the current state.
   if (!(yyn = yydefact[yystate]))
   {
      TC_throwExceptionNamed(parserTP->context, "litebase.SQLParseException", "syntax error", parserTP->yyposition);
      return 1;
   }

yyreduce:
   // yyn is the number of a rule to reduce with. If YYLEN is nonzero, implement the default value of the action: '$$ = $1'.
   // Otherwise, the following line sets YYVAL to garbage. This behavior is undocumented and Bison users should not rely upon it. Assigning to YYVAL
   // unconditionally makes the parser a bit smaller, and it avoids a GCC warning that YYVAL may be used uninitialized. 
   yyval = yyvsp[1 - (yylen = yyr2[yyn])];

   switch (yyn)
   {
      case 2:
#line 65 "LitebaseParser.y"
         parserTP->command = CMD_CREATE_TABLE;
         tableList[0] = initSQLResultSetTable(yyvsp[-4].sval, NULL, heap); // There's no alias table name here.
         break;

      case 3:
#line 70 "LitebaseParser.y"
         parserTP->command = CMD_CREATE_INDEX;
         tableList[0] = initSQLResultSetTable(yyvsp[-3].sval, NULL, heap); // There's no alias table name here.
         break;

      case 4:
#line 75 "LitebaseParser.y"
         parserTP->command = CMD_DROP_TABLE;
         tableList[0] = initSQLResultSetTable(yyvsp[0].sval, NULL, heap); // There's no alias table name here.
         break;

      case 5:
#line 80 "LitebaseParser.y"
         parserTP->command = CMD_DROP_INDEX;
         tableList[0] = initSQLResultSetTable(yyvsp[0].sval, NULL, heap); // There's no alias table name here.
         break;

      case 6:
#line 85 "LitebaseParser.y"
         if (parserTP->fieldNamesSize && parserTP->fieldNamesSize != parserTP->fieldValuesSize)
         {
            char error[15];
            xstrprintf(error, "(%d != %d)", parserTP->fieldNamesSize, parserTP->fieldValuesSize);
            errorWithoutPosition(ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH, error, parser);
            return 1;
         }
   
         parserTP->command = CMD_INSERT;
         tableList[0] = initSQLResultSetTable(yyvsp[-5].sval, NULL, heap); // There's no alias table name here.
         break;

      case 7:
#line 98 "LitebaseParser.y"
         tableList[0] = initSQLResultSetTable(yyvsp[-1].sval, NULL, heap); // There's no alias table name here.
         break;

      case 8:
#line 102 "LitebaseParser.y"
         parserTP->command = CMD_DELETE;
         tableList[0] = initSQLResultSetTable(yyvsp[-2].sval, yyvsp[-1].sval, heap);
         if ((voidPAux = yyvsp[0].obj))
            setBooleanClauseTreeOnWhereClause(voidPAux, parser); // Where clause.
         break;

      case 9:
#line 109 "LitebaseParser.y"
         charPAux2 = yyvsp[-4].sval;
         tableNameAux = (charPAux1 = yyvsp[-3].sval)? yyvsp[-3].sval : charPAux2;

         if (secondFieldUpdateTableName) // Verifies if there was an error on field.tableName.
         {
            if (xstrcmp(tableNameAux, firstFieldUpdateTableName))
               errorWithoutPosition(ERR_INVALID_COLUMN_NAME, firstFieldUpdateAlias, parser);
            else
               errorWithoutPosition(ERR_INVALID_COLUMN_NAME, secondFieldUpdateAlias, parser);
            return 1;
         }
         else if (firstFieldUpdateTableName && xstrcmp(tableNameAux, firstFieldUpdateTableName))
         {
            errorWithoutPosition(ERR_INVALID_COLUMN_NAME, firstFieldUpdateAlias, parser);
            return 1;
         }
      
         parserTP->command = CMD_UPDATE;
         tableList[0] = initSQLResultSetTable(charPAux2, charPAux1, heap);
         if ((voidPAux = yyvsp[0].obj))
            setBooleanClauseTreeOnWhereClause(voidPAux, parser); // Where clause.
         break;

      case 10:
#line 132 "LitebaseParser.y"
         parserTP->command = CMD_SELECT;
         select->tableListSize = parserTP->tableListSize;

         // Checks if the first field is the wildcard. If so, assigns null to list, to indicate that all fields must be included.
         if (selectFieldList[0]->isWildcard)
            select->fieldsCount = 0;
      
         if ((voidPAux = yyvsp[-2].obj)) 
            setBooleanClauseTreeOnWhereClause(voidPAux, parser); // Where clause.
         break;

      case 11:
#line 147 "LitebaseParser.y"
         yyval.sval = yyvsp[0].sval;
         break;

      case 12:
#line 154 "LitebaseParser.y"
         yyval.sval = NULL;
         break;

      case 13:
#line 158 "LitebaseParser.y"
         yyval.sval = yyvsp[0].sval;
         break;

      case 14:
#line 165 "LitebaseParser.y"
         fieldNames[0] = "*";
         break;

      case 18:
#line 178 "LitebaseParser.y"
         if (parserTP->fieldListSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }   
         fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition(yyvsp[-4].sval, SHORT_TYPE, 0, yyvsp[-2].ival, yyvsp[-1].sval16, 
                                                                                                                      yyvsp[0].ival, heap);
         break;

      case 19:
#line 187 "LitebaseParser.y"
         if (parserTP->fieldListSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }      
         fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition(yyvsp[-4].sval, INT_TYPE, 0, yyvsp[-2].ival, yyvsp[-1].sval16, 
                                                                                                                    yyvsp[0].ival, heap);
         break;

      case 20:
#line 196 "LitebaseParser.y"
         if (parserTP->fieldListSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }   
         fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition(yyvsp[-4].sval, LONG_TYPE, 0, yyvsp[-2].ival, yyvsp[-1].sval16, yyvsp[0].ival,
                                                                                                                                       heap);
         break;

      case 21:
#line 205 "LitebaseParser.y"
         if (parserTP->fieldListSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }     
         fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition(yyvsp[-4].sval, FLOAT_TYPE, 0, yyvsp[-2].ival, yyvsp[-1].sval16, 
                                                                                                                      yyvsp[0].ival, heap);
         break;

      case 22:
#line 214 "LitebaseParser.y"
         if (parserTP->fieldListSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }  
         fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition(yyvsp[-4].sval, DOUBLE_TYPE, 0, yyvsp[-2].ival, yyvsp[-1].sval16, 
                                                                                                                       yyvsp[0].ival, heap);
         break;

      case 23:
#line 223 "LitebaseParser.y"
         if ((size = TC_str2int(TC_JCharP2CharPBuf(yyvsp[-5].sval16, -1, buffer), &error)) <= 0 || error) // The size must be a positive integer.
         {
            lbError(ERR_FIELD_SIZE_IS_NOT_INT, parser);
            return 1;
         }
     
         if (parserTP->fieldListSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }
         fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition(yyvsp[-8].sval, yyvsp[-3].ival? CHARS_NOCASE_TYPE : CHARS_TYPE, size, 
                                                                                       yyvsp[-2].ival, yyvsp[-1].sval16, yyvsp[0].ival, heap);
         break;

      case 24:
#line 243 "LitebaseParser.y"
         if (parserTP->fieldListSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }
         fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition(yyvsp[-4].sval, DATE_TYPE, 0, yyvsp[-2].ival, yyvsp[-1].sval16, yyvsp[0].ival, 
                                                                                                                                       heap);
         break;

      case 25:
#line 252 "LitebaseParser.y"
         if (parserTP->fieldListSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }
         fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition(yyvsp[-4].sval, DATETIME_TYPE, 0, yyvsp[-2].ival, yyvsp[-1].sval16, 
                                                                                                                         yyvsp[0].ival, heap);
         break;

      case 26:
#line 261 "LitebaseParser.y"
         if ((size = TC_str2int(TC_JCharP2CharPBuf(yyvsp[-3].sval16, -1, buffer), &error)) <= 0 || error) // The size must be a positive integer.
         {
            lbError(ERR_FIELD_SIZE_IS_NOT_INT, parser);
            return 1;
         }
      
         if ((i = yyvsp[-2].ival) == 'k') // kilobytes
            size <<= 10; 
         else if (i == 'm') // megabytes
            size <<= 20;
            
         if (size > (10 << 20)) // There is a size limit for a blob!
         {
            lbError(ERR_BLOB_TOO_BIG, parser);
            return 1;
         }
         if (parserTP->fieldListSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }
         fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition(yyvsp[-6].sval, BLOB_TYPE, size, 0, null, yyvsp[0].ival, heap);
         break;

      case 27:
#line 290 "LitebaseParser.y"
         if ((size = TC_str2int(TC_JCharP2CharPBuf(yyvsp[-5].sval16, -1, buffer), &error)) <= 0 || error) // The size must be a positive integer.
         {
            lbError(ERR_FIELD_SIZE_IS_NOT_INT, parser);
            return 1;
         }
        
         if (parserTP->fieldListSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }
         fieldList[parserTP->fieldListSize++] = initSQLFieldDefinition(yyvsp[-8].sval, yyvsp[-3].ival? CHARS_NOCASE_TYPE : CHARS_TYPE, size, 
                                                                                       yyvsp[-2].ival, yyvsp[-1].sval16, yyvsp[0].ival, heap);
         break;

      case 28:
#line 313 "LitebaseParser.y"
         yyval.ival = 0;
         break;

      case 29:
#line 317 "LitebaseParser.y"
         if ((i = yyval.ival = yyvsp[0].sval[0]) != 'k' && i != 'm') // The multiplier must be Kilo or Mega.
         {
            lbError(ERR_INVALID_MULTIPLIER, parser);
            return 1;
         }
         break;

      case 30:
#line 328 "LitebaseParser.y"
         yyval.ival = false;
         break;

      case 31:
#line 332 "LitebaseParser.y"
         if (number_pk++ == 1)
	      {
	         lbError(ERR_PRIMARY_KEY_ALREADY_DEFINED, parser);
            return 1;
	      }
         yyval.ival = true;
         break;

      case 32:
#line 344 "LitebaseParser.y"
         yyval.ival = false;
         break;

      case 33:
#line 348 "LitebaseParser.y"
         yyval.ival = true;
         break;

      case 34:
#line 355 "LitebaseParser.y"
         yyval.sval16 = null;
         break;

      case 35:
      case 36:
#line 359 "LitebaseParser.y"
#line 363 "LitebaseParser.y"
         yyval.sval16 = yyvsp[0].sval16;
         break;

      case 37:
#line 367 "LitebaseParser.y"
         yyval.sval16 = null;
         break;

      case 38:
#line 374 "LitebaseParser.y"
         yyval.ival = false;
         break;

      case 39:
#line 378 "LitebaseParser.y"
         yyval.ival = true;
         break;

      case 41:
#line 386 "LitebaseParser.y"
         if (number_pk++ == 1)
	      {
	         lbError(ERR_PRIMARY_KEY_ALREADY_DEFINED, parser);
            return 1;
	      }
         break;

      case 44:
      case 45:
#line 402 "LitebaseParser.y"
#line 412 "LitebaseParser.y"
         if (parserTP->fieldNamesSize == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         } 
         fieldNames[parserTP->fieldNamesSize++] = yyvsp[0].sval;
         break;

      case 46:
      case 47:
#line 424 "LitebaseParser.y"
#line 428 "LitebaseParser.y"
         fieldNames[parserTP->fieldNamesSize++] = yyvsp[0].sval;
         break;

      case 48:
      case 49: 
#line 435 "LitebaseParser.y"
#line 439 "LitebaseParser.y"
         fieldValues[parserTP->fieldValuesSize++] = yyvsp[0].sval16;
         break;

      case 50:
#line 443 "LitebaseParser.y"
         fieldValues[parserTP->fieldValuesSize++] = null;
         break;

      case 51:
#line 447 "LitebaseParser.y"
         fieldValues[parserTP->fieldValuesSize++] = questionMark;
         break;

      case 52:
      case 53:
#line 451 "LitebaseParser.y"
#line 455 "LitebaseParser.y"
         fieldValues[parserTP->fieldValuesSize++] = yyvsp[0].sval16;
         break;

      case 54:
#line 459 "LitebaseParser.y"
         fieldValues[parserTP->fieldValuesSize++] = null;
         break;

      case 55:
#line 463 "LitebaseParser.y"
         fieldValues[parserTP->fieldValuesSize++] = questionMark;
         break;

      case 56:
#line 470 "LitebaseParser.y"
         fieldNames[0] = yyvsp[0].sval;
         break;

      case 57:
#line 474 "LitebaseParser.y"
         parserTP->command = CMD_ALTER_ADD_PK;
         break;

      case 58:
#line 478 "LitebaseParser.y"
         parserTP->command = CMD_ALTER_DROP_PK;
         break;

      case 59:
#line 485 "LitebaseParser.y"
         parserTP->command = CMD_ALTER_RENAME_TABLE;
         break;

      case 60:
#line 489 "LitebaseParser.y"
         parserTP->command = CMD_ALTER_RENAME_COLUMN;
         fieldNames[1] = yyvsp[0].sval;
         break;

      case 65:
#line 507 "LitebaseParser.y"
         field = yyvsp[-2].obj;
        
         if (firstFieldUpdateTableName) // After the table name verification, the associated table name on the field name is discarded.
         {
            // There is an error: update has just one table. This error will raise an exception later on.
            if (xstrcmp(field->tableName, firstFieldUpdateTableName))
            {
               secondFieldUpdateTableName = field->tableName;
               secondFieldUpdateAlias = field->alias;
            }
         }
         else if (field->tableName)
         {
            firstFieldUpdateTableName = field->tableName;
            firstFieldUpdateAlias = field->alias;
         }
         
         fieldNames[parserTP->fieldNamesSize++] = field->tableColName;
         break;

      case 66:
      case 67:
#line 531 "LitebaseParser.y"
#line 535 "LitebaseParser.y"
         fieldValues[parserTP->fieldValuesSize++] = yyvsp[0].sval16;
         break;

      case 68:
#line 539 "LitebaseParser.y"
         fieldValues[parserTP->fieldValuesSize++] = null;
         break;

      case 69:
#line 543 "LitebaseParser.y"
         fieldValues[parserTP->fieldValuesSize++] = questionMark;
         break;

      case 71:
#line 553 "LitebaseParser.y"
		   if (select->fieldsCount == MAXIMUMS)
		   {
			   lbError(ERR_FIELDS_OVERFLOW, parser);
			   return 1;
		   }
		   
         // Adds a willcard field.
		   selectFieldList[select->fieldsCount++] = field = initSQLResultSetField(heap);
         field->isWildcard = select->hasWildcard = true;
         
         break;

      case 75:
#line 576 "LitebaseParser.y"
         field = yyvsp[-1].obj;
         i = select->fieldsCount - 1;
         
         // If the alias_name is null, the alias must be the name of the column. This was already done before.
         
         if (!(charPAux1 = yyvsp[0].sval)) // If the alias is null and the field is a virtual column, raises an exception, since virtual columns require explicit aliases.
         {
            if (field->isVirtual)
            {
               lbError(ERR_REQUIRED_ALIAS, parser);
               return 1;
            }
            charPAux1 = yyvsp[0].sval = field->alias; // The null alias name is filled as tableColName or tableName.tableColName, which was set before.
         }

         while (--i >= 0) // Checks if the alias has not already been used by a predecessor.
         {
            if (strEq(selectFieldList[i]->alias, charPAux1))
            {
               errorWithoutPosition(ERR_DUPLICATE_ALIAS, charPAux1, parser);
               return 1;
            }
         }
         
         field->aliasHashCode = TC_hashCode(field->alias = charPAux1); // Assigns the alias.
         break;

      case 76:
#line 608 "LitebaseParser.y"
         yyval.sval = NULL;
         break;

      case 77:
#line 612 "LitebaseParser.y"
         yyval.sval = yyvsp[0].sval;
         break;

      case 78:
#line 619 "LitebaseParser.y"
         if (select->fieldsCount == MAXIMUMS) // The number of fields has reached the maximum.
         {
            lbError(ERR_FIELDS_OVERFLOW, parser);
            return 1;
         }

	      // Sets the field.
         selectFieldList[select->fieldsCount++] = field = yyval.obj = yyvsp[0].obj;
         field->tableColHashCode = TC_hashCode(field->tableColName);
         
         select->hasRealColumns = true;
         break;

      case 79:
#line 635 "LitebaseParser.y"
         if (select->fieldsCount == MAXIMUMS)
         {
            lbError(ERR_FIELDS_OVERFLOW, parser);
            return 1;
         }
         
         // Sets the select statement.
         selectFieldList[select->fieldsCount++] = field = yyval.obj = yyvsp[0].obj;
         select->hasDTFunctions = true;

         // Sets the field.
         field->isDataTypeFunction = field->isVirtual = true;
         field->dataType = dataTypeFunctionsTypes[field->sqlFunction];

         // Sets the function parameter.
         field->parameter = paramField = initSQLResultSetField(heap);
         field->tableColHashCode = paramField->aliasHashCode = paramField->tableColHashCode = TC_hashCode(paramField->alias 
                                                             = paramField->tableColName = field->tableColName);
         
         break;

      case 80:
#line 659 "LitebaseParser.y"
         if (select->fieldsCount == MAXIMUMS)
         {
            lbError(ERR_FIELDS_OVERFLOW, parser);
            return 1;
         }

         selectFieldList[select->fieldsCount++] = field = yyval.obj = yyvsp[0].obj;
	      
         // Sets the field.
	      field->isAggregatedFunction = field->isVirtual = true;
	      field->dataType = aggregateFunctionsTypes[field->sqlFunction];

         // Sets the parameter, if there is such one.
         if (field->sqlFunction != FUNCTION_AGG_COUNT)
         {
            SQLResultSetField* paramField = field->parameter = initSQLResultSetField(((LitebaseParser*)parser)->heap);
            field->tableColHashCode = paramField->aliasHashCode = paramField->tableColHashCode = TC_hashCode(paramField->alias 
                                                                                               = paramField->tableColName = field->tableColName);
         } else
            field->parameter = null;

         // Sets the select statement.
         select->hasAggFunctions = true;

         break;

      case 81:
#line 690 "LitebaseParser.y"
         field = yyval.obj = initSQLResultSetField(heap);
         field->tableColName = field->alias = yyvsp[0].sval;
         break;

      case 82:
#line 695 "LitebaseParser.y"
         field = yyval.obj = initSQLResultSetField(heap);
         charPAux1 = field->alias 
                   = (CharP)TC_heapAlloc(heap, xstrlen(field->tableColName = yyvsp[0].sval) + xstrlen(field->tableName = yyvsp[-2].sval) + 2);
         xstrcpy(charPAux1, field->tableName);
         xstrcat(charPAux1, ".");
         xstrcat(charPAux1, field->tableColName);
         break;

      case 85:
#line 713 "LitebaseParser.y"
         if (!(charPAux1 = yyvsp[0].sval))
			   charPAux1 = yyvsp[0].sval = yyvsp[-2].sval;
   			
		   // The table name alias must be unique.
		   if (TC_htGet32Inv(tables, (i = TC_hashCode(charPAux1))) != -1)
		   {
			   errorWithoutPosition(ERR_NOT_UNIQUE_ALIAS_TABLE, yyvsp[0].sval, parser);
			   return 1;
		   }
		   TC_htPut32(tables, i, tables->size);	
		   tableList[parserTP->tableListSize++] = initSQLResultSetTable(yyvsp[-2].sval, charPAux1, heap);
         break;

      case 88:
#line 738 "LitebaseParser.y"
         yyval.obj = NULL;
         break;

      case 89:
#line 742 "LitebaseParser.y"
         yyval.obj = yyvsp[0].obj;
         break;

      case 91:
#line 750 "LitebaseParser.y"
         if (yyvsp[0].obj) // Adds the expression tree of the where clause.
	         parserTP->havingClause->expressionTree = parserTP->havingClause->origExpressionTree = yyvsp[0].obj;
         break;

      case 92:
      case 93:
#line 758 "LitebaseParser.y"
#line 764 "LitebaseParser.y"
         select->fieldsCount--; // Removes this field from the select list.
         if (!addColumnFieldOrderGroupBy(yyvsp[0].obj, true, false, parser)) // Adds this field to the group by field list.
            return 1;
         break;

      case 94:
#line 773 "LitebaseParser.y"
         yyval.obj = NULL;
         break;

      case 95:
#line 777 "LitebaseParser.y"
         yyval.obj = yyvsp[0].obj;
         break;

      case 96:
#line 784 "LitebaseParser.y"
         parserTP->isWhereClause = false;
         break;

      case 101:
#line 801 "LitebaseParser.y"
         select->fieldsCount--;
         if (!addColumnFieldOrderGroupBy(yyvsp[-1].obj, yyvsp[0].ival, true, parser))
            return 1;
         break;

      case 102:
      case 103:
#line 810 "LitebaseParser.y"
#line 814 "LitebaseParser.y"
         yyval.ival = true;
         break;

      case 104:
#line 818 "LitebaseParser.y"
         yyval.ival = false;
         break;

      case 105:
#line 825 "LitebaseParser.y"
         yyval.obj = yyvsp[-1].obj;
         break;

      case 106:
#line 829 "LitebaseParser.y"
         
         // The parent node will be the negation operator and the expression will be the right tree.
         tree = setOperandType(OP_BOOLEAN_NOT, parser);
         yyval.obj = (tree->rightTree = yyvsp[-1].obj)->parent = tree;
         break;

      case 107:
#line 835 "LitebaseParser.y"
         yyval.obj = yyvsp[0].obj;
         break;

      case 108:
#line 839 "LitebaseParser.y"
      
         // juliana@213_1: changed the way a tree with ORs is built in order to speed up queries with indices.
         tree = setOperandType(OP_BOOLEAN_OR, parser);
         (tree->leftTree = yyvsp[0].obj)->parent = tree;
         yyval.obj = (tree->rightTree = yyvsp[-2].obj)->parent = tree;
         break;

      case 109:
#line 846 "LitebaseParser.y"
         tree = setOperandType(OP_BOOLEAN_AND, parser);
         (tree->leftTree = yyvsp[-2].obj)->parent = tree;
         yyval.obj = (tree->rightTree = yyvsp[0].obj)->parent = tree;
         break;

      case 110:
#line 855 "LitebaseParser.y"

         // The parent node will be the negation operator and the expression will be the right tree.
         tree = setOperandType(OP_BOOLEAN_NOT, parser);
         yyval.obj = (tree->rightTree = yyvsp[0].obj)->parent = tree;
         break;

      case 111:
#line 861 "LitebaseParser.y"
         tree = yyvsp[-1].obj;
         (tree->leftTree = yyvsp[-2].obj)->parent = tree;
         yyval.obj = (tree->rightTree = yyvsp[0].obj)->parent = tree;
         break;

      case 112:
#line 867 "LitebaseParser.y"
         tree = setOperandType(OP_PAT_IS, parser);
         (tree->rightTree = setOperandType(OP_PAT_NULL, parser))->parent = tree;
         yyval.obj = (tree->leftTree = yyvsp[-2].obj)->parent = tree;
         break;

      case 113:
#line 873 "LitebaseParser.y"
         tree = setOperandType(OP_PAT_IS_NOT, parser);
         (tree->rightTree = setOperandType(OP_PAT_NULL, parser))->parent = tree;
         yyval.obj = (tree->leftTree = yyvsp[-3].obj)->parent = tree;
         break;

      case 114:
#line 879 "LitebaseParser.y"
         tree = setOperandType(OP_PAT_MATCH_LIKE, parser);
         setOperandStringLiteral(rightTree = initSQLBooleanClauseTree(getInstanceBooleanClause(parserTP), heap), yyvsp[0].sval16);
         (tree->rightTree = rightTree)->parent = tree;
         yyval.obj = (tree->leftTree = yyvsp[-2].obj)->parent = tree;
         break;

      case 115:
#line 888 "LitebaseParser.y"
         tree = setOperandType(OP_PAT_MATCH_NOT_LIKE, parser);
         setOperandStringLiteral(rightTree = initSQLBooleanClauseTree(getInstanceBooleanClause(parser), heap), yyvsp[0].sval16);
         (tree->rightTree = rightTree)->parent = tree;
         yyval.obj = (tree->leftTree = yyvsp[-3].obj)->parent = tree;
         break;

      case 116:
#line 897 "LitebaseParser.y"
         if ((booleanClause = getInstanceBooleanClause(parser))->paramCount == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_PARAMS_REACHED, parser);
            return 1;
         }
         
         tree = setOperandType(OP_PAT_MATCH_LIKE, parser);
         (rightTree = initSQLBooleanClauseTree(booleanClause, heap))->isParameter = true;
         if (parserTP->isWhereClause)
		      whereParamList[booleanClause->paramCount++] = rightTree;
	      else
		      havingParamList[booleanClause->paramCount++] = rightTree;
         (tree->rightTree = rightTree)->parent = tree;
         yyval.obj = (tree->leftTree = yyvsp[-2].obj)->parent = tree;
         break;

      case 117:
#line 917 "LitebaseParser.y"
         if ((booleanClause = getInstanceBooleanClause(parser))->paramCount == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_PARAMS_REACHED, parser);
            return 1;
         }
        
         tree = setOperandType(OP_PAT_MATCH_NOT_LIKE, parser);
         (rightTree = initSQLBooleanClauseTree(booleanClause, heap))->isParameter = true;
         if (parserTP->isWhereClause)
		      whereParamList[booleanClause->paramCount++] = rightTree;
	      else
		      havingParamList[booleanClause->paramCount++] = rightTree;
         (tree->rightTree = rightTree)->parent = tree;
         yyval.obj = (tree->leftTree = yyvsp[-3].obj)->parent = tree;
         break;

      case 118:
#line 940 "LitebaseParser.y"
		   tree = yyval.obj = initSQLBooleanClauseTree(booleanClause = getInstanceBooleanClause(parser), heap);
         i = 1;
		   field = yyvsp[0].obj;
		   fieldName2Index = &booleanClause->fieldName2Index;
		   charPAux1 = tree->operandName = field->tableColName;
		   tree->operandType = OP_IDENTIFIER;
		   j = field->tableColHashCode = tree->nameSqlFunctionHashCode = tree->nameHashCode = TC_hashCode(charPAux1);
   		
		   // rnovais@570_108: Generates different index to repeted columns on where clause. Ex: where year(birth) = 2000 and birth = '2008/02/11'.
		   while (TC_htGet32Inv(fieldName2Index, j) >= 0)
			   j = TC_hashCodeFmt("si", charPAux1, i++);
   		
		   if (booleanClause->fieldsCount == MAXIMUMS)
		   {
	         lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
			   return 1;
		   }
   		
		   // Puts the hash code of the function name in the hash table.
		   TC_htPut32(fieldName2Index, tree->nameSqlFunctionHashCode = j, booleanClause->fieldsCount);
   		
		   field->aliasHashCode = TC_hashCode(field->alias); // Sets the hash code of the field alias.

         // Puts the field in the field list.
		   if (parserTP->isWhereClause)
	         whereFieldList[booleanClause->fieldsCount++] = field;
	      else
            havingFieldList[booleanClause->fieldsCount++] = field;
         break;

      case 119:
#line 978 "LitebaseParser.y"
         // juliana@226a_20
         (tree = yyval.obj = initSQLBooleanClauseTree(getInstanceBooleanClause(parser), heap))->operandValue.asChars = yyvsp[0].sval16;
         break;

      case 120:
#line 988 "LitebaseParser.y"
         setOperandStringLiteral(yyval.obj = initSQLBooleanClauseTree(getInstanceBooleanClause(parser), heap), yyvsp[0].sval16);
         break;

      case 121:
#line 993 "LitebaseParser.y"
         tree = yyval.obj = initSQLBooleanClauseTree((booleanClause = getInstanceBooleanClause(parser)), heap);
        
         if (booleanClause->paramCount == MAXIMUMS)
         {
            lbError(ERR_MAX_NUM_PARAMS_REACHED, parser);
            return 1;
         }
        
         tree->isParameter = true;
         if (parserTP->isWhereClause)
		      whereParamList[booleanClause->paramCount++] = tree;
	      else
		      havingParamList[booleanClause->paramCount++] = tree;
         break;

      case 122:
#line 1010 "LitebaseParser.y"
         tree = yyval.obj = initSQLBooleanClauseTree(booleanClause = getInstanceBooleanClause(parser), heap);
         fieldName2Index = &booleanClause->fieldName2Index;
         i = 1;
         paramField = (field = yyvsp[0].obj)->parameter = initSQLResultSetField(heap); // Creates the parameter field.
	      charPAux1 = tree->operandName = paramField->alias = paramField->tableColName = field->alias = field->tableColName;
         tree->operandType = OP_IDENTIFIER; 
         j = paramField->aliasHashCode = paramField->tableColHashCode = field->tableColHashCode = field->aliasHashCode = tree->nameSqlFunctionHashCode 
                                                                                                = tree->nameHashCode = TC_hashCode(charPAux1);
         
         // generates different indexes to repeted columns on where clause.
         // Ex: where year(birth) = 2000 and day(birth) = 3.
         while (TC_htGet32Inv(fieldName2Index, j) >= 0)
            j = TC_hashCodeFmt("si", charPAux1, i++);

         if (booleanClause->fieldsCount == MAXIMUMS) // There is a maximum number of columns.
         {
            lbError(ERR_MAX_NUM_FIELDS_REACHED, parser);
            return 1;
         }
         
         // Puts the hash code of the function name in the hash table.
         TC_htPut32(fieldName2Index, tree->nameSqlFunctionHashCode = j, booleanClause->fieldsCount);

         // Sets the field and function parameter fields.
         field->dataType = dataTypeFunctionsTypes[field->sqlFunction];
         field->isVirtual = field->isDataTypeFunction = true;

         // Puts the field in the field list.
         if (parserTP->isWhereClause)
	         whereFieldList[booleanClause->fieldsCount++] = field;
	      else
            havingFieldList[booleanClause->fieldsCount++] = field;
         break;

      case 123:
#line 1056 "LitebaseParser.y"
         yyval.obj = setOperandType(OP_REL_EQUAL, parser);
         break;

      case 124:
#line 1060 "LitebaseParser.y"
         yyval.obj = setOperandType(OP_REL_DIFF, parser);
         break;

      case 125:
#line 1064 "LitebaseParser.y"
         yyval.obj = setOperandType(OP_REL_GREATER, parser);
         break;

      case 126:
#line 1068 "LitebaseParser.y"
         yyval.obj = setOperandType(OP_REL_LESS, parser);
         break;

      case 127:
#line 1072 "LitebaseParser.y"
         yyval.obj = setOperandType(OP_REL_GREATER_EQUAL, parser);
         break;

      case 128:
#line 1076 "LitebaseParser.y"
         yyval.obj = setOperandType(OP_REL_LESS_EQUAL, parser);
         break;

      case 129:
#line 1083 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_DT_ABS;
         break;

      case 130:
#line 1088 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_DT_UPPER;
         break;

      case 131:
#line 1093 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_DT_LOWER;
         break;

      case 132:
#line 1098 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_DT_YEAR;
         break;

      case 133:
#line 1103 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_DT_MONTH;
         break;

      case 134:
#line 1108 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_DT_DAY;
         break;

      case 135:
#line 1113 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_DT_HOUR;
         break;

      case 136:
#line 1118 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_DT_MINUTE;
         break;

      case 137:
#line 1123 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_DT_SECOND;
         break;

      case 138:
#line 1128 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_DT_MILLIS;
         break;

      case 139:
#line 1136 "LitebaseParser.y"
         (field = yyval.obj = initSQLResultSetField(heap))->sqlFunction = FUNCTION_AGG_COUNT;
         break;

      case 140:
#line 1141 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_AGG_MAX;
         break;

      case 141:
#line 1146 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_AGG_MIN;
         break;

      case 142:
#line 1151 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_AGG_AVG;
         break;

      case 143:
#line 1156 "LitebaseParser.y"
         (field = yyval.obj = yyvsp[-1].obj)->sqlFunction = FUNCTION_AGG_SUM;
         break;
   }

   yyvsp -= yylen;
   yyssp -= yylen;

   *++yyvsp = yyval;

   // Now 'shifts' the result of the reduction. Determines what state that goes to, based on the state popped back to and the rule number reduced by.

   if (0 <= (yystate = yypgoto[(yyn = yyr1[yyn]) - YYNTOKENS] + *yyssp) && yystate <= YYLAST && yycheck[yystate] == *yyssp)
      yystate = yytable[yystate];
   else
      yystate = yydefgoto[yyn - YYNTOKENS];

   // In all cases, when one gets here, the value and location stacks have just been pushed. so pushing a state here evens the stacks.
   yyssp++;
   goto yynewstate;
}
