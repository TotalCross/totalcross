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
 * Internal use only. A tree structure used to evaluate a SQL boolean clause for a given result set.
 */

#include "SQLBooleanClauseTree.h"

/**
 * Creates a <code>SQLBooleanClauseTree</code> with the associated <code>SQLBooleanClause</code>.
 *
 * @param booleanClause The associated <code>SQLBooleanClause</code>.
 * @param heap The heap to allocate the <code>SQLBooleanClauseTree</code> structure.
 * @return A pointer to a <code>SQLBooleanClauseTree</code> structure.
 */
SQLBooleanClauseTree* initSQLBooleanClauseTree(SQLBooleanClause* booleanClause, Heap heap)
{
	TRACE("initSQLBooleanClauseTree")
   SQLBooleanClauseTree* tree = (SQLBooleanClauseTree*)TC_heapAlloc(heap, sizeof(SQLBooleanClauseTree));

   tree->valueType = tree->indexRs = -1;
   tree->booleanClause = booleanClause;
	return tree;
}

/**
 * Sets the tree operand as a string literal.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure.
 * @param value The string literal value.
 */
void setOperandStringLiteral(SQLBooleanClauseTree* booleanClauseTree, JCharP value)
{
	TRACE("setOperandStringLiteral")
   booleanClauseTree->valueType = CHARS_TYPE;
   booleanClauseTree->operandValue.asChars = value;
   booleanClauseTree->operandValue.length = TC_JCharPLen(value);
   setPatternMatchType(booleanClauseTree); // Checks the pattern match type.
}

// juliana@201_34
/**
 * Sets a numeric parameter value.
 *
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure.
 * @param value The numeric value to be set.
 * @param type The type of the value.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 * @throws DriverException If the parameter type is different from the value type.
 */
bool setNumericParamValue(Context context, SQLBooleanClauseTree* booleanClauseTree, VoidP value, int32 type)
{  
	TRACE("setNumericParamValue")
   booleanClauseTree->isParamValueDefined = true;
   if (booleanClauseTree->valueType == UNDEFINED_TYPE)
      booleanClauseTree->valueType = type;
   else if (booleanClauseTree->valueType != type)
   {
      TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_INCOMPATIBLE_TYPES));
      return false;
   }
   switch (type)
   {
      case SHORT_TYPE: 
         booleanClauseTree->operandValue.asShort = *((int16*)value); 
         break;
      case INT_TYPE: 
         booleanClauseTree->operandValue.asInt = *((int32*)value); 
         break;
      case LONG_TYPE: 
         booleanClauseTree->operandValue.asLong = *((int64*)value); 
         break;
      case FLOAT_TYPE: 
         booleanClauseTree->operandValue.asFloat = (float)*((double*)value); 
         break;
      case DOUBLE_TYPE: 
         booleanClauseTree->operandValue.asDouble = *((double*)value); 
   }
   return true;
}

// juliana@222_9: Some string conversions to numerical values could return spourious values if the string range were greater than the type range.
/**
 * Sets a string parameter value.
 *
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure.
 * @param value The string value to be set.
 * @param len The length of the string.
 * @throws SQLParseException If the value is not a valid number.
 * @return <code>false</code> if an error occurs; <code>true</code>, otherwise.
 */
bool setParamValueString(Context context, SQLBooleanClauseTree* booleanClauseTree, JCharP value, int32 length)
{
	TRACE("setParamValueString")
   DoubleBuf buffer;
	bool error = false;
   int32 type = booleanClauseTree->valueType;
   SQLValue* operandValue = &booleanClauseTree->operandValue;
   
   booleanClauseTree->operandType = OP_STRING_LITERAL;
   booleanClauseTree->isParamValueDefined = true;

   // juliana@223_4: Solved a bug that could make a prepared statement string parameter in a where or having clause cause a strange 
   // exception of invalid number. 
   // If the string has length greater than any type in string format, throws an exception.
   if (type != CHARS_TYPE && type != CHARS_NOCASE_TYPE && type != UNDEFINED_TYPE)
   {
      if (length > 39)
      {
         CharP invalid = TC_JCharP2CharP(value, length);
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INVALID_NUMBER), invalid? invalid : "", "number");
         xfree(invalid);
         return false;
      }
      TC_JCharP2CharPBuf(value, length, buffer);
   }
   operandValue->asChars = value;
   operandValue->length = length;

   switch (type) // Converts the string to the correct type.
   {
	   case SHORT_TYPE:
         operandValue->asShort = str2short(buffer, &error);
			break;
      case INT_TYPE:
			operandValue->asInt = TC_str2int(buffer, &error);
			break;
	   case LONG_TYPE:
         operandValue->asLong = TC_str2long(buffer, &error);
			break;
		case FLOAT_TYPE:
         operandValue->asFloat = str2float(buffer, &error);
			break;
		case DOUBLE_TYPE:
			operandValue->asDouble = TC_str2double(buffer, &error);
			break;
      case DATE_TYPE: // rnovais@570_55: If the type is DATE, checks if it is valid and converts it to int.
      case DATETIME_TYPE: // If the type is DATETIME, checks if it is valid and converts it to 2 ints.
         if (!testAndPrepareDateAndTime(context, operandValue, buffer, type))
            return false; 
         break;
  
      case UNDEFINED_TYPE:
         booleanClauseTree->valueType = CHARS_TYPE;
   }

	if (error)
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INVALID_NUMBER), buffer, "number");
      return false;
   }
   else
      setPatternMatchType(booleanClauseTree); // Checks the pattern match type.
   return true;
}

/**
 * Checks the if the operand value string contains pattern matching characters and assigns the proper matching type.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 */
void setPatternMatchType(SQLBooleanClauseTree* booleanClauseTree)
{
	TRACE("setPatternMatchType")
   JCharP value = booleanClauseTree->operandValue.asChars;
   int32 length = booleanClauseTree->operandValue.length;

   if (length > 0) // guich@512_5: checks if there is any pattern matching.
   {
      JChar firstChar = value[0],
            lastChar = value[length - 1];

      if (firstChar == PAT_MATCH_CHAR_ZERO_MORE) // '%...'
      {
         if (length == 1) // '%' // juliana@230_1: solved a bug with like %.
         {
            booleanClauseTree->patternMatchType = PAT_MATCH_ANYTHING;
            booleanClauseTree->lenToMatch = 0;
         }
         else if (lastChar == PAT_MATCH_CHAR_ZERO_MORE) // '%...%'
         {
            booleanClauseTree->strToMatch = &value[1];
            booleanClauseTree->lenToMatch = length - 2;
            booleanClauseTree->patternMatchType = PAT_MATCH_CONTAINS;
         }
         else // '%...'
         {
            booleanClauseTree->strToMatch = &value[1];
            booleanClauseTree->lenToMatch = length - 1;
            booleanClauseTree->patternMatchType = PAT_MATCH_ENDS_WITH;
         }
      }
      else if (lastChar == PAT_MATCH_CHAR_ZERO_MORE) // '...%'
      {
         booleanClauseTree->strToMatch = value;
         booleanClauseTree->lenToMatch = length - 1;
         booleanClauseTree->patternMatchType = PAT_MATCH_STARTS_WITH;
      }
      else // rnovais@568_1: accepts without % or % in the middle.
      {
         int32 pos = TC_JCharPIndexOfJChar(value, (JChar)'%', 0, length);
         booleanClauseTree->strToMatch = value;
         booleanClauseTree->lenToMatch = length;

         if (pos > 0) // In the middle.
         {
            booleanClauseTree->patternMatchType = PAT_MATCH_MIDDLE;
            booleanClauseTree->posPercent = pos;
         }
         else // Without %.
            booleanClauseTree->patternMatchType = PAT_MATCH_EQUAL;
      }
   }
}

// juliana@238_2: improved join table reordering.
/**
 * Weighs the tree to order the table on join operation.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 */
void weightTheTree(SQLBooleanClauseTree* booleanClauseTree)
{
	TRACE("weightTheTree")
   SQLBooleanClauseTree* leftTree = booleanClauseTree->leftTree;
   SQLBooleanClauseTree* rightTree = booleanClauseTree->rightTree;

   switch (booleanClauseTree->operandType) // Checks the type of the operand.
   {
      // juliana@214_4: nots were removed.
      
      case OP_BOOLEAN_AND:
      case OP_BOOLEAN_OR:
         if (leftTree)
            weightTheTree(leftTree);
         if (rightTree)
            weightTheTree(rightTree);
         break;
         
      default: // The others.
      {
         SQLResultSetField** fieldList = booleanClauseTree->booleanClause->fieldList;
         Hashtable* fieldName2Index = &booleanClauseTree->booleanClause->fieldName2Index;
         SQLResultSetField* leftField = fieldList[TC_htGet32(fieldName2Index, leftTree->nameSqlFunctionHashCode)];
         SQLResultSetField* rightField = fieldList[TC_htGet32(fieldName2Index, rightTree->nameSqlFunctionHashCode)];
         Index* leftIndexStr = leftField->table->columnIndexes[leftField->tableColIndex];
         Index* rightIndexStr = rightField->table->columnIndexes[rightField->tableColIndex]; 
         
         // field.indexRs is filled on the where clause validation. Both are identifiers.
         if (leftTree->operandType == OP_IDENTIFIER && rightTree->operandType == OP_IDENTIFIER) 
         {
            if (leftIndexStr)
               leftField->table->weight++;
            if (rightIndexStr)
               rightField->table->weight++;
         }
         else if (leftTree->operandType == OP_IDENTIFIER)
         {
            if (leftIndexStr)
               leftField->table->weight++;
         }
         else if (rightTree->operandType == OP_IDENTIFIER)
         {
            if (rightIndexStr)
               rightField->table->weight++;
         }
      }
   }
}

/**
 * Prepares the tree for the join operation.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 */
void setIndexRsOnTree(SQLBooleanClauseTree* booleanClauseTree)
{
	TRACE("setIndexRsOnTree")
   SQLBooleanClauseTree* leftTree = booleanClauseTree->leftTree;
   SQLBooleanClauseTree* rightTree = booleanClauseTree->rightTree;
   
   switch (booleanClauseTree->operandType) // Checks the type of operand.
   {
      case OP_BOOLEAN_AND:
      case OP_BOOLEAN_OR:
      // juliana@214_4: nots were removed.
      {
         booleanClauseTree->indexRs = -1;
         if (leftTree)  
            setIndexRsOnTree(leftTree); // Sets the indexRs on the left tree.
         if (rightTree) 
            setIndexRsOnTree(rightTree); // Sets the indexRs on the right tree.
         break;
      }
      case OP_REL_EQUAL:
      case OP_REL_DIFF:
      case OP_REL_GREATER:
      case OP_REL_LESS:
      case OP_REL_GREATER_EQUAL:
      case OP_REL_LESS_EQUAL:
      case OP_PAT_MATCH_LIKE:
      case OP_PAT_MATCH_NOT_LIKE:
      case OP_PAT_IS:
      case OP_PAT_IS_NOT:
      {
         int32 leftIndex, 
               rightIndex, 
               fieldIndex;
         SQLBooleanClause* booleanClause = booleanClauseTree->booleanClause;
         SQLResultSetField** fieldList = booleanClause->fieldList;

         // field.indexRs is filled on the where clause validation. Both are identifier.
         if (leftTree->operandType == OP_IDENTIFIER && rightTree->operandType == OP_IDENTIFIER) // Both are identifier.
         {
            // Puts the highest index on the indexRs.
            if ((fieldIndex = getFieldIndex(leftTree)) < 0) 
               break;
            leftIndex = fieldList[fieldIndex]->indexRs;

            if ((fieldIndex = getFieldIndex(rightTree)) < 0) 
               break;
            rightIndex = fieldList[fieldIndex]->indexRs;

            leftTree->indexRs = leftIndex;
            rightTree->indexRs = rightIndex;

            if (leftIndex > rightIndex) // Puts the least index on the left. 
            {
               SQLBooleanClauseTree* auxTree = leftTree;
               booleanClauseTree->leftTree = rightTree;
               booleanClauseTree->rightTree = auxTree;
               booleanClauseTree->indexRs = leftIndex;
            }
            else
               booleanClauseTree->indexRs = rightIndex;
            
            // juliana@263_2: corrected a very old bug in a join with comparision between two fields of the same table.
            if (leftIndex != rightIndex)
               booleanClauseTree->bothAreIdentifier = true;
         }
         else if (leftTree->operandType == OP_IDENTIFIER)
         {
            if ((fieldIndex = getFieldIndex(leftTree)) < 0) 
               break;
            booleanClauseTree->indexRs = fieldList[fieldIndex]->indexRs;
         }
         else if (rightTree->operandType == OP_IDENTIFIER)
         {
            if ((fieldIndex = getFieldIndex(rightTree)) < 0) 
               break;
            booleanClauseTree->indexRs = fieldList[fieldIndex]->indexRs;
         }
      }
   }
}

/**
 * Used for composed indices to find some properties related to a branch of the expression tree.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param columns The columns of the expression tree.
 * @param operators The operators of the expression tree.
 * @param indexesValueTree The part of the tree that uses indices.
 * @param position The index of branch being analized.
 * @param fieldsCount The number of fields of the boolean clause.
 */
void getBranchProperties(SQLBooleanClauseTree* booleanClauseTree, uint8* columns, uint8* operators, SQLBooleanClauseTree** indexesValueTree,  
								                                                                            int32 position, int32 fieldsCount)
{
	TRACE("getBranchProperties")
   SQLBooleanClauseTree* leftTree = booleanClauseTree->leftTree;
   SQLBooleanClauseTree* rightTree = booleanClauseTree->rightTree;

   if (position >= fieldsCount) // Does not let an <code>OutOfBoundsException</code>.
      return;

   if (leftTree->operandType == OP_IDENTIFIER) // One of the elements of the branch must be an identifier.
   {
      columns[position] = leftTree->colIndex;
      indexesValueTree[position] = rightTree;
   }
   else if (rightTree->operandType == OP_IDENTIFIER)
   {
      columns[position] = rightTree->colIndex;
      indexesValueTree[position] = leftTree;
   }
   else
   {
      columns[position] = -1;
      return;
   }
   operators[position] = booleanClauseTree->operandType;
}

/** 
 * Gets a value from a result set and applies a sql function if there is one to be applied.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param value The value returned from the result set or the result of a function on a value of the result set.
 * @return <code>true</code> if the value could be fetched correctly; <code>false</code>, otherwise.
 */
bool getOperandValue(Context context, SQLBooleanClauseTree* booleanClauseTree, SQLValue* value)
{
	TRACE("getOperandValue")

   // If the operand type is identifier, gets the value from the current row in the result set. Otherwise, just returns the tree operand value.
   if (booleanClauseTree->operandType == OP_IDENTIFIER)
   {
      SQLResultSetField* field;
      SQLBooleanClause* booleanClause = booleanClauseTree->booleanClause;
      ResultSet* resultSet = booleanClause->resultSet;
      Table* table = resultSet->table;
      
      xmemmove(table->columnNulls, table->db.basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
      if (isBitSet(table->columnNulls, booleanClauseTree->colIndex)) // There is a null value.
      {
         value->isNull = true;
         return true;
      }
      
      // rnovais@_570_1: created the last parameter for <code>getTableColValue()</code>. See this function.
      if (!getTableColValue(context, resultSet, booleanClauseTree->colIndex, value))
         return false;

      // rnovais@568_10: applies data type function.
      if ((field = booleanClause->fieldList[getFieldIndex(booleanClauseTree)])->sqlFunction != FUNCTION_DT_NONE) 
         applyDataTypeFunction(value, field->sqlFunction, field->parameter->dataType);
      
      value->isNull = false;
   }
   else 
      *value = booleanClauseTree->operandValue;
   return true;
}

/** 
 * Checks if an operand is null.
 * 
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @return <code>true</code> if the operand is null and a null value is being searched or vice-versa; <code>false</code>, otherwise.
 */
bool compareNullOperands(SQLBooleanClauseTree* booleanClauseTree)
{
	TRACE("compareNullOperands")
   bool isNull;
   Table* table = booleanClauseTree->booleanClause->resultSet->table;
   xmemmove(table->columnNulls, table->db.basbuf + table->columnOffsets[table->columnCount], NUMBEROFBYTES(table->columnCount));
   isNull = isBitSet(table->columnNulls, booleanClauseTree->leftTree->colIndex);
   return (booleanClauseTree->operandType == OP_PAT_IS)? isNull : !isNull;
}

/**
 * Compares two numerical operands.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @return The evaluation of the comparison expression or -1 if an error occurs.
 */
int32 compareNumericOperands(Context context, SQLBooleanClauseTree* booleanClauseTree)
{
	TRACE("compareNumericOperands")
   bool result = false;
   SQLBooleanClauseTree* leftTree = booleanClauseTree->leftTree;
   SQLBooleanClauseTree* rightTree = booleanClauseTree->rightTree;
   SQLValue leftValue,
            rightValue;
   int32 leftValueType = leftTree->valueType,
         rightValueType = rightTree->valueType,
         assignType = booleanClauseTree->isFloatingPointType? DOUBLE_TYPE 
                    : leftValueType != LONG_TYPE && rightValueType != LONG_TYPE && leftValueType != DATETIME_TYPE? INT_TYPE : LONG_TYPE,
         compareType = leftValueType == DATETIME_TYPE? DATETIME_TYPE : assignType,
         leftValueAsInt = 0,
         rightValueAsInt = 0, 
         leftValueAsTime = 0,
         rightValueAsTime = 0;
   int64 leftValueAsLong = 0,
         rightValueAsLong = 0;
   double leftValueAsDouble = 0,
          rightValueAsDouble = 0;
   
   // Gets the values.
   if (booleanClauseTree->bothAreIdentifier)
      leftValue = leftTree->valueJoin;
   else 
   {
      xmemzero(&leftValue, sizeof(SQLValue));
      if (!getOperandValue(context, leftTree, &leftValue))
         return -1;
   }
   xmemzero(&rightValue, sizeof(SQLValue));
   if (!getOperandValue(context, rightTree, &rightValue))
      return -1;

   if (leftValue.isNull || rightValue.isNull) // One of the values is a null value.
      return false;

   switch (leftValueType) // Getting left value.
   {
      case SHORT_TYPE:
         switch (assignType)
         {
            case DOUBLE_TYPE: 
               leftValueAsDouble = leftValue.asShort; 
               break;
            case INT_TYPE: 
               leftValueAsInt = leftValue.asShort; 
               break;
            case LONG_TYPE: 
               leftValueAsLong = leftValue.asShort; 
         }
         break;

      case DATE_TYPE: // rnovais@567_2
      case INT_TYPE:
         switch (assignType)
         {
            case DOUBLE_TYPE: 
               leftValueAsDouble = leftValue.asInt; 
               break;
            case INT_TYPE: 
               leftValueAsInt = leftValue.asInt; 
               break;
            case LONG_TYPE: 
               leftValueAsLong = leftValue.asInt; 
         }
         break;

      case LONG_TYPE:
         switch (assignType)
         {
            case DOUBLE_TYPE: 
               leftValueAsDouble = (double)leftValue.asLong; 
               break;
            case LONG_TYPE: 
               leftValueAsLong = leftValue.asLong; 
         }
         break;

      case FLOAT_TYPE:
         leftValueAsDouble = leftValue.asFloat;
         break;

      case DOUBLE_TYPE:
         leftValueAsDouble = leftValue.asDouble;
         break;

      case DATETIME_TYPE: // rnovais@567_2
         leftValueAsInt = leftValue.asDate;
         leftValueAsTime = leftValue.asTime;
         break;
   }

   switch (rightValueType) // Getting right value.
   {
      case SHORT_TYPE:
         switch (assignType)
         {
            case DOUBLE_TYPE: 
               rightValueAsDouble = rightValue.asShort; 
               break;
            case INT_TYPE: 
               rightValueAsInt = rightValue.asShort; 
               break;
            case LONG_TYPE: 
               rightValueAsLong = rightValue.asShort; 
         }
         break;

      case INT_TYPE:
         switch (assignType)
         {
            case DOUBLE_TYPE: 
               rightValueAsDouble = rightValue.asInt; 
               break;
            case INT_TYPE: 
               rightValueAsInt = rightValue.asInt; 
               break;
            case LONG_TYPE: 
               rightValueAsLong = rightValue.asInt; 
         }
         break;

      case LONG_TYPE:
         switch (assignType)
         {
            case DOUBLE_TYPE: 
               rightValueAsDouble = (double)rightValue.asLong; 
               break;
            case LONG_TYPE: 
               rightValueAsLong = rightValue.asLong; 
         }
         break;

      case FLOAT_TYPE:
         rightValueAsDouble = rightValue.asFloat;
         break;

      case DOUBLE_TYPE:
         rightValueAsDouble = rightValue.asDouble;
         break;

      case DATE_TYPE: // rnovais@570_55
      case DATETIME_TYPE: // rnovais@570_55
      case CHARS_TYPE: // rnovais@567_2 : this is for DATE and DATETIME typed that are CHARS type in the sql.
         if (leftValueType == DATETIME_TYPE)
         {
            rightValueAsInt = rightValue.asDate;
            rightValueAsTime = rightValue.asTime;
         }
         else 
            rightValueAsInt = rightValue.asInt;
   }

   if (leftValueType == -1 || rightValueType == -1) // Prevents problems when doing comparisons like 1 == 2.
   {
      bool error;
      DoubleBuf buffer;
      JCharP leftValueAsString = leftValue.asChars,
             rightValueAsString = rightValue.asChars;
      uint32 last = TC_JCharPLen(leftValueAsString) - 1;

      // Strips off the final L, because <code>str2double()</code> does not like it.
      if (leftValueAsString[last] == 'l' || leftValueAsString[last] == 'L') 
         leftValueAsString[last] = 0;
      if (last >= sizeof(DoubleBuf)) // Prevents buffer overrun.
         leftValueAsString[sizeof(DoubleBuf) - 1] = 0;
      if (rightValueAsString[last = TC_JCharPLen(rightValueAsString) - 1] == 'l' || rightValueAsString[last] == 'L')
         rightValueAsString[last] = 0;
      if (last >= sizeof(DoubleBuf)) // Prevents buffer overrun.
         rightValueAsString[sizeof(DoubleBuf) - 1] = 0;

      // juliana@225_12: a numeric constant in a boolean clause must have its type considered to be double.
      TC_JCharP2CharPBuf(leftValueAsString, -1, buffer);
      leftTree->operandValue.asDouble = leftValue.asDouble = leftValueAsDouble = TC_str2double(buffer, &error);
      if (error)
      {
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INVALID_NUMBER), buffer, "double");
         return -1;
      }
      TC_JCharP2CharPBuf(rightValueAsString, -1, buffer);
      rightTree->operandValue.asDouble = rightValue.asDouble = rightValueAsDouble = TC_str2double(buffer, &error);
      if (error)
      {
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INVALID_NUMBER), buffer, "double");
         return -1;
      }
      
      booleanClauseTree->leftTree->valueType = booleanClauseTree->rightTree->valueType = DOUBLE_TYPE;
      booleanClauseTree->isFloatingPointType = true;
      compareType = DOUBLE_TYPE;
   }

   switch (booleanClauseTree->operandType) // Then perform the comparison.
   {
      case OP_REL_EQUAL:
      case OP_REL_DIFF:
         switch (compareType)
         {
            case DOUBLE_TYPE: 
               result = (leftValueAsDouble == rightValueAsDouble); 
               break;
            case INT_TYPE: 
               result = (leftValueAsInt == rightValueAsInt); 
               break;
            case LONG_TYPE: 
               result = (leftValueAsLong == rightValueAsLong); 
               break;
            case DATETIME_TYPE: 
               result = (leftValueAsInt == rightValueAsInt) && (leftValueAsTime == rightValueAsTime); 
         }
         if (booleanClauseTree->operandType == OP_REL_DIFF)
            result = !result;
         break;

      case OP_REL_GREATER:
      case OP_REL_LESS_EQUAL:
         switch (compareType)
         {
            case DOUBLE_TYPE: 
               result = (leftValueAsDouble > rightValueAsDouble); 
               break;
            case INT_TYPE: 
               result = (leftValueAsInt > rightValueAsInt); 
               break;
            case LONG_TYPE: 
               result = (leftValueAsLong > rightValueAsLong); 
               break;
            case DATETIME_TYPE: 
               result = (leftValueAsInt == rightValueAsInt)? (leftValueAsTime > rightValueAsTime): (leftValueAsInt > rightValueAsInt); 
         }
         if (booleanClauseTree->operandType == OP_REL_LESS_EQUAL)
            result = !result;
         break;

      case OP_REL_LESS:
      case OP_REL_GREATER_EQUAL:
         switch (compareType)
         {
            case DOUBLE_TYPE: 
               result = (leftValueAsDouble < rightValueAsDouble); 
               break;
            case INT_TYPE: 
               result = (leftValueAsInt  < rightValueAsInt); 
               break;
            case LONG_TYPE: 
               result = (leftValueAsLong < rightValueAsLong); 
               break;
            case DATETIME_TYPE: 
               result = (leftValueAsInt == rightValueAsInt)? (leftValueAsTime < rightValueAsTime): (leftValueAsInt < rightValueAsInt); 
         }
         if (booleanClauseTree->operandType == OP_REL_GREATER_EQUAL)
            result = !result;
   }
   return result;
}

/** 
 * Compares two strings using LIKE and NOT LIKE.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param ignoreCase Indicates if both strings are CHARS_NOCASE.
 * @return The evaluation of the comparison expression.
 * @param heap A heap to alocate temporary strings in the expression tree.
 * @return The evaluation of the comparison expression or -1 if an error occurs. 
 */
int32 matchStringOperands(Context context, SQLBooleanClauseTree* booleanClauseTree, bool ignoreCase, Heap heap)
{
	TRACE("matchStringOperands")
   SQLBooleanClauseTree* leftTree = booleanClauseTree->leftTree;
   SQLBooleanClauseTree* rightTree = booleanClauseTree->rightTree;
   SQLValue leftValue = leftTree->valueJoin;
   JCharP leftStringStr,
          strToMatchStr = rightTree->strToMatch;
	int32 leftStringLen,
         strToMatchLen = rightTree->lenToMatch,
         matchType = rightTree->patternMatchType,
         pos,
	      strEndLen;
   bool result = false;
   JChar dateTimeBuf16[27];

   if (!leftValue.asChars)
		leftTree->valueJoin.asChars = leftValue.asChars = (JCharP)TC_heapAlloc(heap, (booleanClauseTree->booleanClause->resultSet->table->columnSizes[leftTree->colIndex] << 1) + 2);
   if (!getOperandValue(context, leftTree, &leftValue))
      return -1;
   if (leftValue.isNull) // null value
      return false; // juliana@230_34: corrected possible wrong results when comparing with nulls.

   leftStringStr = leftValue.asChars;
   leftStringLen = leftValue.length;
   
   // juliana@230_3: corrected a bug of LIKE using DATE and DATETIME not returning the correct result.
   if (leftTree->valueType == DATE_TYPE)
   {
      int32 asDate = leftValue.asInt;    
      date2JCharP(asDate / 10000, asDate / 100 % 100, asDate % 100, leftStringStr = dateTimeBuf16);
      leftStringLen = 10;
   }
   else if (leftTree->valueType == DATETIME_TYPE)
   {
      int32 asDate = leftValue.asDate,
            asTime = leftValue.asTime;
      dateTime2JCharP(asDate / 10000, asDate / 100 % 100, asDate % 100, 
                  asTime / 10000000, asTime / 100000 % 100, asTime / 1000 % 100, asTime % 1000, leftStringStr = dateTimeBuf16);
      leftStringLen = 23;
   }
   switch (matchType)
   {
      case PAT_MATCH_ANYTHING:
         result = 1;
         break;

      case PAT_MATCH_STARTS_WITH:
			result = str16StartsWith(leftStringStr, strToMatchStr, leftStringLen, strToMatchLen, 0, ignoreCase);
         break;

      case PAT_MATCH_ENDS_WITH:
			result = str16StartsWith(leftStringStr, strToMatchStr, leftStringLen, strToMatchLen, leftStringLen - strToMatchLen, ignoreCase);
         break;

      case PAT_MATCH_CONTAINS:
			result = str16IndexOf(leftStringStr, strToMatchStr, leftStringLen, strToMatchLen, ignoreCase) >= 0;
         break;

      case PAT_MATCH_MIDDLE: // rnovais@568_1
         strEndLen = strToMatchLen - (pos = rightTree->posPercent) - 1;
			result = str16StartsWith(leftStringStr, strToMatchStr, leftStringLen, strToMatchLen = pos - 1, 0, ignoreCase)? 
                  str16StartsWith(leftStringStr, &strToMatchStr[pos + 1], leftStringLen, strEndLen, leftStringLen - strEndLen, ignoreCase) : 0;
         break;

      case PAT_MATCH_EQUAL: // rnovais@568_1
         if (ignoreCase)
				result = TC_JCharPEqualsIgnoreCaseJCharP(leftStringStr, strToMatchStr, leftStringLen, strToMatchLen);
			else
            result = TC_JCharPEqualsJCharP(leftStringStr, strToMatchStr, leftStringLen, strToMatchLen);
   }

   if (booleanClauseTree->operandType == OP_PAT_MATCH_NOT_LIKE)
      result = !result;

   return result;
}

/**
 * Normal comparison between two strings.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param ignoreCase Indicates if both strings are CHARS_NOCASE.
 * @param heap A heap to alocate temporary strings in the expression tree.
 * @return The evaluation of the comparison expression or -1 if an error occurs.
 */
int32 compareStringOperands(Context context, SQLBooleanClauseTree* booleanClauseTree, bool ignoreCase, Heap heap)
{
	TRACE("compareStringOperands")
   bool result;
   SQLBooleanClauseTree* leftTree = booleanClauseTree->leftTree;
   SQLBooleanClauseTree* rightTree = booleanClauseTree->rightTree;
   int32* columnSizes = booleanClauseTree->booleanClause->resultSet->table->columnSizes;

   SQLValue leftValue,
            rightValue;
   
   if (!rightTree->valueJoin.asChars)
      rightTree->valueJoin.asChars = (JCharP)TC_heapAlloc(heap, (columnSizes[rightTree->colIndex] << 1) + 2);
      
   rightValue = rightTree->valueJoin;
   if (!getOperandValue(context, rightTree, &rightValue))
      return -1;

   if (!booleanClauseTree->bothAreIdentifier)
   {
      if (!leftTree->valueJoin.asChars && leftTree->operandType == OP_IDENTIFIER)
         leftTree->valueJoin.asChars = (JCharP)TC_heapAlloc(heap, (columnSizes[leftTree->colIndex] << 1) + 2);
      if (!getOperandValue(context, leftTree, &leftTree->valueJoin))
         return -1;
   }

   leftValue = leftTree->valueJoin;

   if (leftValue.isNull || rightValue.isNull) // null value
      return false;

   result = str16CompareTo(leftValue.asChars, rightValue.asChars, leftValue.length, rightValue.length, ignoreCase);

   switch (booleanClauseTree->operandType)
   {
      case OP_REL_EQUAL:
         return !result;
      case OP_REL_DIFF:
         return result != 0;
      case OP_REL_GREATER:
         return result > 0;
      case OP_REL_LESS:
         return result < 0;
      case OP_REL_GREATER_EQUAL:
         return result >= 0;
      case OP_REL_LESS_EQUAL:
         return result <= 0;
   }
   return false;
}

/**
 * Evaluates an expression tree.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param heap A heap to alocate temporary strings in the expression tree.
 * @return The value of the expression evaluation or -1 if an error occurs.
 */
int32 booleanTreeEvaluate(Context context, SQLBooleanClauseTree* booleanClauseTree, Heap heap)
{
	TRACE("booleanTreeEvaluate")
   int32 result = false;

   switch (booleanClauseTree->operandType) // Checks the operand type of the tree.
   {
      // Relational operantors.
      case OP_REL_EQUAL:
      case OP_REL_DIFF:
      case OP_REL_GREATER:
      case OP_REL_LESS:
      case OP_REL_GREATER_EQUAL:
      case OP_REL_LESS_EQUAL:
      {
         switch (booleanClauseTree->valueType) // Calls the right operation accordingly to the values type.
         {
            case SHORT_TYPE:
            case INT_TYPE:
            case LONG_TYPE:
            case FLOAT_TYPE:
            case DOUBLE_TYPE:
            case DATE_TYPE:
            case DATETIME_TYPE:
               return compareNumericOperands(context, booleanClauseTree);

            case BLOB_TYPE:
               TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_COMP_BLOBS));
               return false;

            case CHARS_TYPE:
               return compareStringOperands(context, booleanClauseTree, false, heap);

            case CHARS_NOCASE_TYPE:
               return compareStringOperands(context, booleanClauseTree, true, heap);
         }
         return false;
      }

      // LIKE operators.
      case OP_PAT_MATCH_LIKE:
      case OP_PAT_MATCH_NOT_LIKE:
         return matchStringOperands(context, booleanClauseTree, booleanClauseTree->valueType == CHARS_NOCASE_TYPE, heap);

      case OP_BOOLEAN_AND: // AND connector.
      {
         // Expects both trees to be not null.
         // Short circuit: only evaluates the right tree if left result is TRUE.
         if (booleanClauseTree->leftTree && booleanClauseTree->rightTree && (result = booleanTreeEvaluate(context, booleanClauseTree->leftTree, heap)) == true) 
            return booleanTreeEvaluate(context, booleanClauseTree->rightTree, heap);              
         return result;
      }

      case OP_BOOLEAN_OR: // OR connector.
      {
         // Expects both trees not be null.
         if (booleanClauseTree->leftTree && booleanClauseTree->rightTree) // Expects both trees to be not null.
         {
            // Short circuit: only evaluates the right tree if the left result is FALSE.
            if ((result = booleanTreeEvaluate(context, booleanClauseTree->leftTree, heap)) == true)
               return true;
            if (!result)
               return booleanTreeEvaluate(context, booleanClauseTree->rightTree, heap);
            return -1;
         }
         return false;
      }

      // juliana@214_4: nots were removed.

      //rnovais@200_1
      case OP_PAT_IS:
      case OP_PAT_IS_NOT:
         return compareNullOperands(booleanClauseTree);
   }
   return false;
}

/**
 * Binds the column information of the underlying table to the boolean clause tree nodes.
 *
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @return <code>false</code> If an exception is thrown; <code>true</code>, otherwise.
 * @throws DriverException If there are imcompatible types or a column cannot be bound.
 */
bool bindColumnsSQLBooleanClauseTree(Context context, SQLBooleanClauseTree* booleanClauseTree)
{
	TRACE("bindColumnsSQLBooleanClauseTree")
   SQLBooleanClauseTree* leftTree;
   SQLBooleanClauseTree* rightTree;

   if (booleanClauseTree->operandType == OP_IDENTIFIER) // If operand type is identifier, binds to a column in the table.
   {
      int32 dtParameter, // rnovais@568_10
            fieldIndex = getFieldIndex(booleanClauseTree), // Stores the binding information also in the boolean clause field list.
            colIndex;
      SQLResultSetField* field;
      SQLBooleanClause* booleanClause = booleanClauseTree->booleanClause; // Stores the binding information also in the boolean clause field list.

      if (fieldIndex < 0) // The column could not be found.
		{
			TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_UNKNOWN_COLUMN), booleanClauseTree->operandName);
         return false;
		}
		
      booleanClauseTree->colIndex = colIndex = (field = booleanClause->fieldList[fieldIndex])->tableColIndex;

      if (field->sqlFunction == FUNCTION_DT_NONE) // rnovais@568_10: if it does not have a data function, stores the correct value type. 
      {
         dtParameter = field->dataType;
         if (booleanClauseTree->valueType != UNDEFINED_TYPE && booleanClauseTree->valueType != dtParameter)
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INCOMPATIBLE_TYPES));
            return false;
         }
         booleanClauseTree->valueType = dtParameter;
      }
      else
      {
         if (field->dataType == UNDEFINED_TYPE)  // rnovais@570_5
            field->dataType = field->parameter->dataType;
         dtParameter = field->dataType;
         
         if (booleanClauseTree->valueType != UNDEFINED_TYPE && booleanClauseTree->valueType != dtParameter)
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INCOMPATIBLE_TYPES));
            return false;
         }
         booleanClauseTree->valueType = dtParameter;
      }

      field->dataType = dtParameter;
   }

   // Bind the columns of the children trees.
   if ((leftTree = booleanClauseTree->leftTree) && !bindColumnsSQLBooleanClauseTree(context, leftTree))
      return false;

   if ((rightTree = booleanClauseTree->rightTree) && !bindColumnsSQLBooleanClauseTree(context, rightTree))
      return false;

   // Infers the operation resulting value type.
   if (leftTree && rightTree && !inferOperationValueType(context, booleanClauseTree))
      return false;

   // rnovais@567_2: validates date and datetime in the rightTree.
   if (leftTree && rightTree && (leftTree->valueType == DATE_TYPE || leftTree->valueType == DATETIME_TYPE))
      return validateDateTime(context, &rightTree->operandValue, leftTree->valueType);
   return true;
}

/**
 * Infers the operation value type, according to the left and right values involved in the operation. 
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @return <code>false</code> If an exception is thrown; <code>true</code>, otherwise.
 * @throws SQLParseException If left and right values have incompatible types  or there is a blob type in the comparison.
 */
bool inferOperationValueType(Context context, SQLBooleanClauseTree* booleanClauseTree)
{
	TRACE("inferOperationValueType")
   SQLBooleanClauseTree* leftTree  = booleanClauseTree->leftTree;
   SQLBooleanClauseTree* rightTree = booleanClauseTree->rightTree;
   int32 leftOperandType,
         rightOperandType,
         leftValueType,
         rightValueType;
   bool leftIsParameter,
        rightIsParameter;
   if (!leftTree || !rightTree)
   {
      booleanClauseTree->valueType = UNDEFINED_TYPE;
      return true;
   }

   if (rightTree->operandType == OP_PAT_NULL)
   {
      booleanClauseTree->valueType = leftTree->operandType;
      return true;
   }

   if (leftTree->operandName) // rnovais@568_10: if it has a data type function, verifies if it can be applied.
   {
      SQLBooleanClause* booleanClause = leftTree->booleanClause;
      int32 fieldIndex = getFieldIndex(leftTree);
      SQLResultSetField* field = booleanClause->fieldList[fieldIndex];
      
      if (field->sqlFunction != FUNCTION_DT_NONE && !bindFunctionDataType(field->parameter->dataType, field->sqlFunction)) // Incompatible function.
      {
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_DATA_TYPE_FUNCTION), dataTypeFunctionsName(field->sqlFunction));
         return false;
      }
   }

   if (booleanClauseTree->operandType == OP_BOOLEAN_AND || booleanClauseTree->operandType == OP_BOOLEAN_OR) // Boolean type.
   {
      booleanClauseTree->valueType = BOOLEAN_TYPE;
      return true;
   }

   leftOperandType = leftTree->operandType;
   rightOperandType = rightTree->operandType;
   leftValueType = leftTree->valueType;
   rightValueType = rightTree->valueType;
   leftIsParameter = leftTree->isParameter;
   rightIsParameter = rightTree->isParameter;

   if (leftValueType == BLOB_TYPE || rightValueType == BLOB_TYPE) // Blobs can't be compared.
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_COMP_BLOBS));
      return false;
   }

   // In case one of them is a parameter, the tree has the type of the one that is not a parameter.
   // If both are parameters, the type is undefined (which is the default, anyway).
   if (leftIsParameter || rightIsParameter)
   {
      if (leftIsParameter)
         booleanClauseTree->valueType = leftTree->valueType = rightValueType;
      else
         booleanClauseTree->valueType = rightTree->valueType = leftValueType;
   }
   else
   {
      // rnovais@567_2: date and dateTime are string values in a sql statement.
      bool leftIsChar = (leftValueType == CHARS_TYPE || leftValueType == CHARS_NOCASE_TYPE 
                     || leftValueType == DATE_TYPE || leftValueType == DATETIME_TYPE),
      
      // juliana@201_12: both should be compared to DATE and DATETIME.
           rightIsChar = (rightValueType == CHARS_TYPE || rightValueType == CHARS_NOCASE_TYPE
							  || leftValueType == DATE_TYPE || leftValueType == DATETIME_TYPE);

      
      if (leftIsChar != rightIsChar) // Can not mix a character type with a non-character type, except for DATE and DATETIME.
      {
         TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INCOMPATIBLE_TYPES));
         return false;
      }

      if ((leftValueType == DATE_TYPE || leftValueType == DATETIME_TYPE) && rightValueType == CHARS_TYPE) // rnovais@567_2
         rightValueType = leftValueType;

      // If one of the operands is an identifier and the other one is not, it prevails the identifier operand value type.
      if (rightOperandType == OP_IDENTIFIER && leftOperandType != OP_IDENTIFIER)
      {
         if (!convertValue(context, booleanClauseTree->leftTree, booleanClauseTree->valueType = rightValueType))
            return false;
      }
      else if (leftOperandType == OP_IDENTIFIER && rightOperandType != OP_IDENTIFIER)
      {
         if (!convertValue(context, booleanClauseTree->rightTree, booleanClauseTree->valueType = leftValueType))
            return false;
      }
      else switch (booleanClauseTree->operandType)
      {
         // Relational operators.
         case OP_REL_EQUAL:
         case OP_REL_DIFF:
         case OP_REL_GREATER:
         case OP_REL_LESS:
         case OP_REL_GREATER_EQUAL:
         case OP_REL_LESS_EQUAL:
            if (leftIsChar || rightIsChar)
            {
               if (leftValueType == DATE_TYPE || rightValueType == DATE_TYPE) // rnovais@567_2
                  booleanClauseTree->valueType = DATE_TYPE;
               else if (leftValueType == DATETIME_TYPE || rightValueType == DATETIME_TYPE)
                  booleanClauseTree->valueType = DATETIME_TYPE;
               else
               {
                  // If both are identifiers, it prevails CHARS, in case one of them is CHARS (case sensitive).
                  if (leftOperandType == OP_IDENTIFIER && rightOperandType == OP_IDENTIFIER)
                  {
                     if (leftValueType == CHARS_TYPE || rightValueType == CHARS_TYPE)
                        booleanClauseTree->valueType = CHARS_TYPE;
                     else
                        booleanClauseTree->valueType = CHARS_NOCASE_TYPE;
                  }
                  else
                     booleanClauseTree->valueType = CHARS_TYPE;
               }
            }
            else
            {
               // This order is important.
               if (leftValueType == DOUBLE_TYPE || rightValueType == DOUBLE_TYPE)
                  booleanClauseTree->valueType = DOUBLE_TYPE;
               else if (leftValueType == FLOAT_TYPE || rightValueType == FLOAT_TYPE)
                  booleanClauseTree->valueType = FLOAT_TYPE;
               else if (leftValueType == LONG_TYPE || rightValueType == LONG_TYPE)
                  booleanClauseTree->valueType = LONG_TYPE;
               else if (leftValueType == INT_TYPE || rightValueType == INT_TYPE)
                  booleanClauseTree->valueType = INT_TYPE;
               else
                  booleanClauseTree->valueType = SHORT_TYPE;
            }
            break;

         // Like operators.   
         case OP_PAT_MATCH_LIKE:
         case OP_PAT_MATCH_NOT_LIKE:
            if (!leftIsChar || !rightIsChar) // Only character types are allowed here.
            {
               TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INCOMPATIBLE_TYPES));
               return false;
            }

            if (leftValueType == DATE_TYPE || rightValueType == DATE_TYPE) // rnovais@567_2
               booleanClauseTree->valueType = DATE_TYPE;
            else if (leftValueType == DATETIME_TYPE || rightValueType == DATETIME_TYPE)
               booleanClauseTree->valueType = DATETIME_TYPE;
            else
            {
               // If both are identifiers, it prevails CHARS, in case one of them is CHARS (case sensitive).
               if (leftOperandType == OP_IDENTIFIER && rightOperandType == OP_IDENTIFIER)
               {
                  if (leftValueType == CHARS_TYPE || rightValueType == CHARS_TYPE)
                     booleanClauseTree->valueType = CHARS_TYPE;
                  else
                     booleanClauseTree->valueType = CHARS_NOCASE_TYPE;
               }
               else
                  booleanClauseTree->valueType = CHARS_TYPE;
            }

            break;

         default:
            booleanClauseTree->valueType = UNDEFINED_TYPE;
      }
   }

   // Checks if the operand value type has floating point.
   booleanClauseTree->isFloatingPointType = booleanClauseTree->valueType == DOUBLE_TYPE || booleanClauseTree->valueType == FLOAT_TYPE;
   return true;
}

// juliana@222_9: Some string conversions to numerical values could return spourious values if the string range were greater than the type range.
/** 
 * Converts a number in the string format to its numerical representation.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param type The number type.
 * @throws SQLParseException If the value to be converted is not a valid number.
 */
bool convertValue(Context context, SQLBooleanClauseTree* booleanClauseTree, int32 type)
{
	TRACE("convertValue")
   DoubleBuf buffer; // Widest type.
   SQLValue* operandValue = &booleanClauseTree->operandValue;
   bool error = false;

   if (type < SHORT_TYPE || type > DOUBLE_TYPE) // juliana@201_10: non-numerical types don't need to be converted to numbers.
		return true;

   if ((operandValue->length = TC_JCharPLen(operandValue->asChars)) > 39)
   {
      CharP invalid = TC_JCharP2CharP(operandValue->asChars, operandValue->length);
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INVALID_NUMBER), invalid? invalid : "", "number");
      xfree(invalid);
      return false;
   }

   TC_JCharP2CharPBuf(operandValue->asChars, -1, buffer); 

   switch (type)
   {
      case SHORT_TYPE:
         operandValue->asShort = str2short(buffer, &error);
         booleanClauseTree->valueType = SHORT_TYPE;
			break;
      case INT_TYPE:
         operandValue->asInt = TC_str2int(buffer, &error);
         booleanClauseTree->valueType = INT_TYPE;
         break;
      case LONG_TYPE:
         operandValue->asLong = TC_str2long(buffer, &error);
         booleanClauseTree->valueType = LONG_TYPE;
         break;
      case FLOAT_TYPE:
         operandValue->asFloat = str2float(buffer, &error);
         booleanClauseTree->valueType = FLOAT_TYPE;
         break;
      case DOUBLE_TYPE:
         operandValue->asDouble = TC_str2double(buffer, &error);
         booleanClauseTree->valueType = DOUBLE_TYPE;
   }

   if (error)
	{
		TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_INVALID_NUMBER), buffer, "number");
		return false;
	}
   return true;
}

// juliana@214_4: removes not from expression trees so that indices can be used in more situations.
/**
 * Removes the not operators from an expression tree.
 * 
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param heap The heap to allocate a <code>SQLBooleanClauseTree</code> node. 
 * @return The expression tree without nots.
 */
SQLBooleanClauseTree* removeNots(SQLBooleanClauseTree* booleanClauseTree, Heap heap)
{
	TRACE("removeNots")
	SQLBooleanClauseTree* tree;
   SQLBooleanClauseTree* rightTree;

   if (!booleanClauseTree) // Does nothing with an empty expression is used.
      return null;

   rightTree = booleanClauseTree->rightTree;
      
   if (booleanClauseTree->operandType == OP_BOOLEAN_NOT)
   {
      switch (rightTree->operandType)
      {
         case OP_REL_EQUAL: // not equal == dif.
            rightTree->operandType = OP_REL_DIFF;
            rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree;
            break;   
         case OP_REL_DIFF: // not dif == equal.
            rightTree->operandType = OP_REL_EQUAL;
            rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree;
            break;   
         case OP_REL_GREATER: // not greater == less equal.
            rightTree->operandType = OP_REL_LESS_EQUAL;
            rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree;
            break;   
         case OP_REL_LESS: // not less == greater equal.
            rightTree->operandType = OP_REL_GREATER_EQUAL;
            rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree;
            break;   
         case OP_REL_GREATER_EQUAL: // not greater equal == less.
            rightTree->operandType = OP_REL_LESS;
            rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree;
            break;   
         case OP_REL_LESS_EQUAL: // not less equal == greates. 
            rightTree->operandType = OP_REL_GREATER;
            rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree;
            break;  
         case OP_PAT_IS: // not is == is not.
            rightTree->operandType = OP_PAT_IS_NOT;
            rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree;
            break;
         case OP_PAT_IS_NOT: // not is not == is.
            rightTree->operandType = OP_PAT_IS;
            rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree;
            break;
         case OP_PAT_MATCH_LIKE: // not like == not like.
            rightTree->operandType = OP_PAT_MATCH_NOT_LIKE;
            rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree;
            break;
         case OP_PAT_MATCH_NOT_LIKE: // not not like == like.
            rightTree->operandType = OP_PAT_MATCH_LIKE;
            rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree;
            break;
         case OP_BOOLEAN_NOT: // not not == null.
            rightTree->rightTree->parent = booleanClauseTree->parent;
            booleanClauseTree = rightTree->rightTree;
            break;
         case OP_BOOLEAN_AND: // not (A and B) == not A or not B.
            tree = initSQLBooleanClauseTree(booleanClauseTree->booleanClause, heap);
            tree->operandType = OP_BOOLEAN_OR;
            tree->leftTree = booleanClauseTree;
            tree->rightTree = rightTree;
            tree->rightTree->operandType = OP_BOOLEAN_NOT;
            booleanClauseTree->parent = tree->rightTree->parent = tree;
            booleanClauseTree->rightTree = tree->rightTree->leftTree;
            tree->rightTree->leftTree = null;
            rightTree->parent = booleanClauseTree;
            booleanClauseTree = tree;
            break;
         case OP_BOOLEAN_OR: // not (A or B) == not A and not B.
            tree = initSQLBooleanClauseTree(booleanClauseTree->booleanClause, heap);
            tree->operandType = OP_BOOLEAN_AND;
            tree->leftTree = booleanClauseTree;
            tree->rightTree = rightTree;
            tree->rightTree->operandType = OP_BOOLEAN_NOT;
            booleanClauseTree->parent = tree->rightTree->parent = tree;
            booleanClauseTree->rightTree = tree->rightTree->leftTree;
            tree->rightTree->leftTree = null;
            rightTree->parent = booleanClauseTree;
            booleanClauseTree = tree;
      }   
   }   
      
   // Recursion.
   booleanClauseTree->leftTree = removeNots(booleanClauseTree->leftTree, heap);
   booleanClauseTree->rightTree = removeNots(booleanClauseTree->rightTree, heap);
   
   return booleanClauseTree;
}

/**
 * Gets the field index given the hash code of the SQL function used or the field if there is no function applied.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @return The index of the field used by this branch of the tree.
 */
int32 getFieldIndex(SQLBooleanClauseTree* booleanClauseTree)
{
   TRACE("getFieldIndex")
   return (TC_htGet32Inv(&(booleanClauseTree)->booleanClause->fieldName2Index, (booleanClauseTree)->nameSqlFunctionHashCode? (booleanClauseTree)->nameSqlFunctionHashCode : (booleanClauseTree)->nameHashCode));
}

/**
 * Validates a string value as a date or datetime according with its type. If it is well-formed, its value is transformed into one or two ints.
 *
 * @param context The thread context where the function is being executed.
 * @param value The value that will receive the date or datetime as integers.
 * @param valueType The expected type: date or datetime.
 * @return <code>true</code> if the string storing a date or a datetime is well-formed; <code>false</code>, otherwise.
 * @throws SQLParseException If the string is not well-formed.
 */
bool validateDateTime(Context context, SQLValue* value, int32 valueType)
{
	TRACE("validateDateTime")
   int32 length = value->length;
   DateTimeBuf buffer;

   // First, converts and trims.
   if (!value->asChars)
      return true;
   
   if (!length)
   {
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_VALUE_ISNOT_DATETIME), "");
      return false;
   }
   if (length >= 27)
   {
      CharP str = TC_JCharP2CharP(value->asChars, length);
      TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_VALUE_ISNOT_DATETIME), (str? str : ""));
      xfree(str);
      return false;
   }   

   TC_JCharP2CharPBuf(value->asChars, length, buffer);
   if (xstrchr(buffer,'%')) // A % is used in a like operation.
      return true;

   return testAndPrepareDateAndTime(context, value, buffer, valueType);
}

// juliana@226_15: corrected a bug that would make a prepared statement with where clause and indices not work correctly after the first execution.
/**
 * Clones an expression tree of a where clause of a select prepared statement. 
 * 
 * @param booleanClauseTree The expression tree to be cloned.
 * @param destTree The old destination tree. If the nodes are the same of the tree cloned, the node is reused in the cloned tree.
 * @param heap A heap to allocate the new tree.
 * @return A clone of the where clause expression tree.
 */
SQLBooleanClauseTree* cloneTree(SQLBooleanClauseTree* booleanClauseTree, SQLBooleanClauseTree* destTree, Heap heap)
{
   TRACE("cloneTree")
   SQLBooleanClause* booleanClause = booleanClauseTree->booleanClause;
   SQLBooleanClauseTree** paramList = booleanClause->paramList;
   SQLBooleanClauseTree* tree = null;
   int32 i = booleanClause->paramCount;
   
   while (--i >= 0)
      if (booleanClauseTree == paramList[i])
      {
         tree = booleanClauseTree;
         break;
      }
   if (!tree)
   {
      if (destTree != booleanClauseTree)
      {
         (tree = initSQLBooleanClauseTree(booleanClause, heap))->booleanClause = booleanClause;
         tree->bothAreIdentifier = booleanClauseTree->bothAreIdentifier;
         tree->colIndex = booleanClauseTree->colIndex;
         tree->hasIndex = booleanClauseTree->hasIndex;
         tree->indexRs = booleanClauseTree->indexRs;
         tree->isFloatingPointType = booleanClauseTree->isFloatingPointType;
         tree->isParameter = booleanClauseTree->isParameter;
         tree->isParamValueDefined = booleanClauseTree->isParamValueDefined;
         tree->nameHashCode = booleanClauseTree->nameHashCode;
         tree->nameSqlFunctionHashCode = booleanClauseTree->nameSqlFunctionHashCode;
         tree->operandName = booleanClauseTree->operandName;
         tree->operandType = booleanClauseTree->operandType;
         tree->operandValue = booleanClauseTree->operandValue;
         tree->patternMatchType = booleanClauseTree->patternMatchType;
         tree->posPercent = booleanClauseTree->posPercent;
         tree->strToMatch = booleanClauseTree->strToMatch;
         
         // juliana@227_8:  Solved a bug on select prepared statement with like not returning the correct result.
         tree->lenToMatch = booleanClauseTree->lenToMatch;
         
         tree->valueType = booleanClauseTree->valueType;
      }
      else
         tree = destTree;
   }
   if (booleanClauseTree->leftTree)
      (tree->leftTree = cloneTree(booleanClauseTree->leftTree, destTree? destTree->leftTree : null, heap))->parent = tree;
   if (booleanClauseTree->rightTree)
      (tree->rightTree = cloneTree(booleanClauseTree->rightTree, destTree? destTree->rightTree : null,  heap))->parent = tree;
   return tree;
}
