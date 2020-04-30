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

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.util.*;

/**
 * Internal use only. A tree structure used to evaluate a SQL boolean clause for a given result set.
 */
class SQLBooleanClauseTree
{
   // Pattern match types
   /**
    * Indicates if it is a comparison with the beginning of a string.
    */
   static final int PAT_MATCH_STARTS_WITH = 1;

   /**
    * Indicates if it is a comparison with the end of a string.
    */
   static final int PAT_MATCH_ENDS_WITH = 2;

   /**
    * Indicates if there are two '%' symbols to compare if a string contains another.
    */
   static final int PAT_MATCH_CONTAINS = 3;

   /**
    * Indicates if the string to be matched can be any string.
    */
   static final int PAT_MATCH_ANYTHING = 4;

   // rnovais@568_1: accept % on the middle.
   /**
    * Indicates if there is a match character in the middle of the string to be matched.
    */
   static final int PAT_MATCH_MIDDLE = 5;

   // rnovais@_568_1: accept like without %.
   /**
    * Indicates if the comparison test is just a equality.
    */
   static final int PAT_MATCH_EQUAL = 6;

   /**
    * Pattern matching character.
    */
   static final int PAT_MATCH_CHAR_ZERO_MORE = '%';

   /**
    * Tree operand type.
    */
   int operandType;
   
   /**
    * The operand name hash code.
    */
   int nameHashCode;

   /**
    * The operand name hash code used only for sql functions.
    */
   int nameSqlFunctionHashCode; // rnovais@570_108
   
   /**
    * The value data type.
    */
   int valueType = SQLElement.UNDEFINED;
   
   /**
    * Pattern matching type.
    */
   int patternMatchType;
   
   /**
    * The associated table column index of the operand.
    */
   int colIndex;
   
   /**
    * Position of the % in the string.
    */
   private int posPercent; // rnovais@_568_1

   /**
    * The index of the correspondent result set.
    */
   int indexRs = -1;
   
   /**
    * Indicate if the value type is a floating point type.
    */
   private boolean isFloatingPointType;

   /**
    * Indicates if this is a parameter.
    */
   boolean isParameter;

   /**
    * Indicates if the parameter value is defined.
    */
   boolean isParamValueDefined;
   
   /**
    * Indicates if the left and right tree are identifiers.
    */
   boolean bothAreIdentifier;

   /**
    * Indicates if it has an associated index. Used on join <code>table1.field1 = table2.field2</code>.
    */
   boolean hasIndex;
   
   /**
    * Tree operand name.
    */
   String operandName;

   /**
    * String to do the pattern match.
    */
   String strToMatch;
   
   // Subtrees
   /**
    * The left tree.
    */
   SQLBooleanClauseTree leftTree;

   /**
    * The right tree.
    */
   SQLBooleanClauseTree rightTree;

   /**
    * The parent tree.
    */
   SQLBooleanClauseTree parent;

   /**
    * The associated SQLBooleanClause.
    */
   SQLBooleanClause booleanClause;

   /**
    * Tree operand value.
    */
   SQLValue operandValue;

   /**
    * The current value. Used only on joins comparing table fields like <code>table1.field1 <operator> table2.field2</code>.
    */
   SQLValue valueJoin;

   /**
    * A temporary <code>SQLValue</code> for <code>getOperandValue()</code>
    */
   private SQLValue tempValue = new SQLValue();
   
   /**
    * Creates a <code>SQLBooleanClauseTree</code> with the associated <code>SQLBooleanClause</code>.
    *
    * @param booleanClause The associated <code>SQLBooleanClause</code>.
    */
   SQLBooleanClauseTree(SQLBooleanClause aBooleanClause)
   {
      booleanClause = aBooleanClause;
   }

   /**
    * Sets the tree operand as a string literal.
    *
    * @param value The string literal value.
    */
   void setOperandStringLiteral(String value) // juliana@201_34
   {
      valueType = SQLElement.CHARS;
      if (operandValue == null)
         operandValue = new SQLValue();
      operandValue.asString = value;
      setPatternMatchType(); // Checks the pattern match type.
   }

   /**
    * Sets a short parameter value.
    *
    * @param val The short value to be set.
    * @throws DriverException If the parameter is not of type short.
    */
   void setParamValue(short val) throws DriverException // juliana@201_34
   {
      createValueAndTestType(SQLElement.SHORT);
      operandValue.asShort = val;
   }

   /**
    * Sets a int parameter value.
    *
    * @param val The integer value to be set.
    * @throws DriverException If the parameter is not of type int.
    */
   void setParamValue(int val) throws DriverException // juliana@201_34
   {
      createValueAndTestType(SQLElement.INT);
      operandValue.asInt = val;
   }

   /**
    * Sets a long parameter value.
    *
    * @param val The long value to be set.
    * @throws DriverException If the parameter is not of type long.
    */
   void setParamValue(long val) throws DriverException // juliana@201_34
   {
      createValueAndTestType(SQLElement.LONG);
      operandValue.asLong = val;
   }

   /**
    * Sets a float parameter value.
    *
    * @param val The float value to be set.
    * @throws DriverException If the parameter is not of type float.
    */
   void setParamValue(float val) throws DriverException // juliana@201_34
   {
      createValueAndTestType(SQLElement.FLOAT);
      operandValue.asDouble = val;
   }

   /**
    * Sets a double parameter value.
    *
    * @param val The double value to be set.
    * @throws DriverException If the parameter is not of type double.
    */
   void setParamValue(double val) throws DriverException // juliana@201_34
   {
      createValueAndTestType(SQLElement.DOUBLE);
      operandValue.asDouble = val;
   }

   // juliana@201_34: setString() for a where clause wouldn't work if the table column wasn't a date, datetime, chars or chars nocase.
   // juliana@222_9: Some string conversions to numerical values could return spourious values if the string range were greater than the type range.
   /**
    * Sets a string parameter value.
    *
    * @param val The string value to be set.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    * @throws DriverException If a blob is set as a string.
    */
   void setParamValue(String val) throws InvalidDateException, InvalidNumberException
   {
      SQLValue value = operandValue;
     
      isParamValueDefined = true;
      if (value == null)
         value = operandValue = new SQLValue();
      value.asString = val;

      switch (valueType)
      {
         case SQLElement.SHORT:
            value.asShort = Convert.toShort(val);
            break;
            
         case SQLElement.INT:
            value.asInt = Convert.toInt(val);
            break;
            
         case SQLElement.LONG:
            operandValue.asLong = Convert.toLong(val);
            break;
           
         case SQLElement.FLOAT:
            value.asDouble = Utils.toFloat(val);
            break;
            
         case SQLElement.DOUBLE:
            value.asDouble = Convert.toDouble(val);
            break;
            
         case SQLElement.DATE: // rnovais@570_55: If the type is DATE, checks if it is valid and converts it to int.
            value.asInt = booleanClause.tempDate.set(val.trim(), Settings.DATE_YMD);
            break;
            
         case SQLElement.DATETIME: // If the type is DATETIME, checks if it is valid and converts it to 2 ints.
            val = val.trim();
            int pos = val.lastIndexOf(' ');
            if (pos == -1) // If it has only a date...
            {
               value.asInt = booleanClause.tempDate.set(val, Settings.DATE_YMD); // Gets the date part.
               value.asShort = 0;
            }
            else
            {
               value.asInt = booleanClause.tempDate.set(val.substring(0, pos), Settings.DATE_YMD); // Gets the date part.
               value.asShort = Utils.testAndPrepareTime(val.substring(pos + 1)); // Gets the time part.
            }
            break;
            
         case SQLElement.UNDEFINED: // If the type is not defined, it is CHARS.
            valueType = SQLElement.CHARS;
      }
      
      setPatternMatchType(); // Checks the pattern match type.
   }

   /**
    * Checks the if the operand value string contains pattern matching characters and assigns the proper matching type.
    */
   private void setPatternMatchType()
   {
      String value = operandValue.asString;

      // Checks if there is any pattern matching.
      int len = value.length();
      if (len > 0) // guich@512_5
      {
         char firstChar = value.charAt(0);
         char lastChar = value.charAt(len - 1);

         if (firstChar == PAT_MATCH_CHAR_ZERO_MORE) // '%...'
         {
            if (len == 1) // '%' // juliana@230_1: solved a bug with LIKE "%".
            {
               patternMatchType = PAT_MATCH_ANYTHING;
               strToMatch = "";
            }
            else
            if (lastChar == PAT_MATCH_CHAR_ZERO_MORE) // '%...%'
            {
               patternMatchType = PAT_MATCH_CONTAINS;
               strToMatch = value.substring(1, len - 1);
            }
            else // '%...'
            {
               patternMatchType = PAT_MATCH_ENDS_WITH;
               strToMatch = value.substring(1, len);
            }
         }
         else
         if (lastChar == PAT_MATCH_CHAR_ZERO_MORE) // '...%'
         {
            patternMatchType = PAT_MATCH_STARTS_WITH;
            strToMatch = value.substring(0, len - 1);
         }
         else  // rnovais@568_1: accepts without % or % in the middle.
         {
            int pos = value.indexOf('%');
            if (pos > 0) // In the middle.
            {
               patternMatchType = PAT_MATCH_MIDDLE;
               strToMatch = value;
               posPercent = pos;
            }
            else // Without %.
            {
               patternMatchType = PAT_MATCH_EQUAL;
               strToMatch = value;
            }
         }
      }
   }
   
   // juliana@238_2: improved join table reordering.
   /**
    * Weighs the tree to order the table on join operation.
    */
   void weightTheTree()
   {         
      switch (operandType) // Checks the type of the operand.
      {
         case SQLElement.OP_BOOLEAN_AND:
         case SQLElement.OP_BOOLEAN_OR:
         // juliana@214_4: nots were removed.
            if (leftTree != null) 
               leftTree.weightTheTree();
            if (rightTree != null)
               rightTree.weightTheTree();
            break;
                
         default: // The others.
            SQLBooleanClauseTree left = leftTree,
                                 right = rightTree;
            SQLBooleanClause booleanClauseAux = booleanClause;
            SQLResultSetField leftField = booleanClauseAux.fieldList[booleanClauseAux.fieldName2Index.get(left.nameSqlFunctionHashCode, 0)],
                              rightField = booleanClauseAux.fieldList[booleanClauseAux.fieldName2Index.get(right.nameSqlFunctionHashCode, 0)];
            Index leftIndex = leftField.table.columnIndices[leftField.tableColIndex],
                  rightIndex = rightField.table.columnIndices[rightField.tableColIndex];
            
            // field.indexRs is filled on the where clause validation. Both are identifiers.
            if (left.operandType == SQLElement.OP_IDENTIFIER && right.operandType == SQLElement.OP_IDENTIFIER)
            {               
               if (leftIndex != null)
                  leftField.table.weight++;
               if (rightIndex != null)
                  rightField.table.weight++;               
            }
            else if (left.operandType == SQLElement.OP_IDENTIFIER)
            {
               if (leftIndex != null)
                  leftField.table.weight++;  
            }
            else if (right.operandType == SQLElement.OP_IDENTIFIER)
            {
               if (rightIndex != null)
                  rightField.table.weight++;           
            }
      }
   }
   
   /**
    * Prepares the tree for the join operation.
    */
   void setIndexRsOnTree()
   {
      switch (operandType) // Checks the operand type.
      {
         case SQLElement.OP_BOOLEAN_AND:
         case SQLElement.OP_BOOLEAN_OR:
         // juliana@214_4: nots were removed.
            indexRs = -1;
            if (leftTree != null) // Sets the indexRs on the left tree.
               leftTree.setIndexRsOnTree();
            if (rightTree != null) // Sets the indexRs on the right tree.
               rightTree.setIndexRsOnTree();
            break;
         case SQLElement.OP_PAT_MATCH_LIKE:
         case SQLElement.OP_PAT_MATCH_NOT_LIKE:
         case SQLElement.OP_PAT_IS:
         case SQLElement.OP_PAT_IS_NOT:
         case SQLElement.OP_REL_LESS:
         case SQLElement.OP_REL_EQUAL:               
         case SQLElement.OP_REL_GREATER:               
         case SQLElement.OP_REL_GREATER_EQUAL:
         case SQLElement.OP_REL_LESS_EQUAL:
         case SQLElement.OP_REL_DIFF:
            SQLBooleanClauseTree left = leftTree,
                                 right = rightTree; 
            SQLBooleanClause clause = booleanClause;
            
            // field.indexRs is filled on the where clause validation. Both are identifier.
            if (left.operandType == SQLElement.OP_IDENTIFIER || right.operandType == SQLElement.OP_IDENTIFIER)
            {
               int lIdx = clause.fieldName2Index.get(left.nameSqlFunctionHashCode != 0? left.nameSqlFunctionHashCode 
                                                                                                   : left.nameHashCode, -1),
                   rIdx = clause.fieldName2Index.get(right.nameSqlFunctionHashCode != 0? right.nameSqlFunctionHashCode 
                                                                                                     : right.nameHashCode, -1);
               if (left.operandType == SQLElement.OP_IDENTIFIER && right.operandType == SQLElement.OP_IDENTIFIER)
               {
                  // Puts the highest index on the indexRs.
                  int leftIndex  = left.indexRs = clause.fieldList[lIdx].indexRs;
                  int rightIndex = right.indexRs = clause.fieldList[rIdx].indexRs;

                  if (leftIndex <= rightIndex) // Puts the least index on the left.
                     indexRs = rightIndex;
                  else
                  {
                     SQLBooleanClauseTree auxTree = left;
                     leftTree = right;
                     rightTree = auxTree;
                     indexRs = leftIndex;
                  }
                  
                  // juliana@263_2: corrected a very old bug in a join with comparision between two fields of the same table.
                  if (leftIndex != rightIndex) 
                     bothAreIdentifier = true;
               }
               else
                  indexRs = clause.fieldList[left.operandType == SQLElement.OP_IDENTIFIER ? lIdx : rIdx].indexRs;
            }
      }
   }

   /**
    * Used for composed indices to find some properties related to a branch of the expression tree.
    *
    * @param columns The columns of the expression tree.
    * @param operators The operators of the expression tree.
    * @param indexesValueTree The part of the tree that uses indices.
    * @param pos The index of branch being analyzed.
    */
   void getBranchProperties(byte[] columns, byte[] operators, SQLBooleanClauseTree[] indexesValueTree, int pos)
   {
      if (pos >= columns.length) // Does not let an <code>OutOfBoundsException</code>.
         return;
      
      // One of the elements of the branch must be an identifier.
      if (leftTree.operandType == SQLElement.OP_IDENTIFIER)
      {
         columns[pos] = (byte)leftTree.colIndex;
         indexesValueTree[pos] = rightTree;
      }
      else
      if (rightTree.operandType == SQLElement.OP_IDENTIFIER)
      {
         columns[pos] = (byte)rightTree.colIndex;
         indexesValueTree[pos] = leftTree;
      }
      else
      {
         operators[pos] = -1;
         return;
      }
      operators[pos] = (byte)operandType;
   }

   /** 
    * Gets a value from a result set and applies a sql function if there is one to be applied.
    * 
    * @return The value returned from the result set or the result of a function on a value of the result set.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   SQLValue getOperandValue() throws IOException, InvalidDateException
   {
      SQLValue v; // rnovais@568_10
      
      // If the operand type is identifier, gets the value from the current row in the result set. Otherwise, just returns the tree operand value.
      if (operandType == SQLElement.OP_IDENTIFIER)
      {
         SQLBooleanClause clause = booleanClause;
         Table table = clause.resultSet.table;
         table.readNullBytesOfRecord(0, false, 0);
         if ((table.columnNulls[0][colIndex >> 3] & (1 << (colIndex & 7))) != 0) // There is a null value.
            return null;
         
         // guich@tc100b4: replaced "new SQLValue" by tree.tempValue to avoid allocating this (+ 5000 times in AllTests).
         v = clause.resultSet.sqlwhereclausetreeGetTableColValue(colIndex, tempValue); 
         
         // rnovais@568_10: applies data type function.
         SQLResultSetField field = clause.fieldList[clause.fieldName2Index.get(nameSqlFunctionHashCode, -1)]; // rnovais@570_108
         if (field.sqlFunction != -1) // has a data type function
            v.applyDataTypeFunction(field.sqlFunction, field.parameter.dataType);
         return v;
      }
      return operandValue;
   }

   /** 
    * Checks if an operand is null.
    * 
    * @return <code>true</code> if the operand is null and a null value is being searched or vice-versa; <code>false</code>, otherwise.
    * @throws IOException If an internal method throws it.
    */
   boolean compareNullOperands() throws IOException
   {
      Table table = leftTree.booleanClause.resultSet.table;
      table.readNullBytesOfRecord(0, false, 0);
      boolean isNull = (table.columnNulls[0][leftTree.colIndex >> 3] & (1 << (leftTree.colIndex & 7))) != 0;
      return (operandType == SQLElement.OP_PAT_IS) ? isNull : !isNull;
   }

   /**
    * Compares two numerical operands.
    * 
    * @return The evaluation of the comparison expression.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If number convertion fails.
    */
   boolean compareNumericOperands() throws IOException, InvalidDateException, InvalidNumberException
   {
      boolean result = false;
      SQLBooleanClauseTree left = leftTree,
                           right = rightTree;
      SQLValue leftValue = bothAreIdentifier? left.valueJoin : left.getOperandValue(),
               rightValue = right.getOperandValue();

      if (leftValue == null || rightValue == null) // One of the values is a null value.
         return false;

      int leftValueType = left.valueType,
          rightValueType = right.valueType,
          assignType = isFloatingPointType? SQLElement.DOUBLE : leftValueType != SQLElement.LONG && rightValueType != SQLElement.LONG 
                                                             && leftValueType != SQLElement.DATETIME ? SQLElement.INT : SQLElement.LONG,
          compareType = leftValueType == SQLElement.DATETIME ? SQLElement.DATETIME : assignType,
          leftValueAsInt = 0, 
          rightValueAsInt = 0, 
          leftValueAsTime = 0, 
          rightValueAsTime = 0;
      long leftValueAsLong = 0L, 
            rightValueAsLong = 0L;
      double leftValueAsDouble = 0, 
              rightValueAsDouble = 0;

      switch (leftValueType) // Getting left value.
      {
         case SQLElement.SHORT:
            switch (assignType)
            {
               case SQLElement.DOUBLE: 
                  leftValueAsDouble = leftValue.asShort; 
                  break;
               case SQLElement.INT: 
                  leftValueAsInt = leftValue.asShort; 
                  break;
               case SQLElement.LONG: 
                  leftValueAsLong = leftValue.asShort; 
            }
            break;
            
         case SQLElement.DATE: // rnovais@567_2
         case SQLElement.INT:
            switch (assignType)
            {
               case SQLElement.DOUBLE: 
                  leftValueAsDouble = leftValue.asInt; 
                  break;
               case SQLElement.INT: 
                  leftValueAsInt = leftValue.asInt; 
                  break;
               case SQLElement.LONG: 
                  leftValueAsLong = leftValue.asInt; 
            }
            break;
            
         case SQLElement.LONG:
            switch (assignType)
            {
               case SQLElement.DOUBLE: 
                  leftValueAsDouble = (double)leftValue.asLong; 
                  break;
               case SQLElement.LONG: 
                  leftValueAsLong = leftValue.asLong; 
            }
            break;

         case SQLElement.FLOAT:
         case SQLElement.DOUBLE:
            leftValueAsDouble = leftValue.asDouble;
            break;
            
         case SQLElement.DATETIME: // rnovais@567_2
            leftValueAsInt = leftValue.asInt;
            leftValueAsTime = leftValue.asShort;
      }
      
      switch (rightValueType) // Getting right value.
      {
         case SQLElement.SHORT:
            switch (assignType)
            {
               case SQLElement.DOUBLE: 
                  rightValueAsDouble = rightValue.asShort; 
                  break;
               case SQLElement.INT: 
                  rightValueAsInt = rightValue.asShort; 
                  break;
               case SQLElement.LONG: 
                  rightValueAsLong = rightValue.asShort;
            }
            break;
            
         case SQLElement.INT:
            switch (assignType)
            {
               case SQLElement.DOUBLE: 
                  rightValueAsDouble = rightValue.asInt; 
                  break;
               case SQLElement.INT: 
                  rightValueAsInt = rightValue.asInt; 
                  break;
               case SQLElement.LONG: 
                  rightValueAsLong = rightValue.asInt; 
            }
            break;

         case SQLElement.LONG:
            switch (assignType)
            {
               case SQLElement.DOUBLE: 
                  rightValueAsDouble = (double)rightValue.asLong; 
                  break;
               case SQLElement.LONG: 
                  rightValueAsLong = rightValue.asLong; 
            }
            break;

         case SQLElement.FLOAT:
         case SQLElement.DOUBLE:
            rightValueAsDouble = rightValue.asDouble;
            break;
            
         case SQLElement.DATE: // rnovais@570_55
         case SQLElement.DATETIME:// rnovais@570_55
         case SQLElement.CHARS: // rnovais@567_2 : this is for DATE and DATETIME typed that are CHARS type in the sql.
            if (leftValueType == SQLElement.DATETIME)
            {
               rightValueAsInt = rightValue.asInt;
               rightValueAsTime = rightValue.asShort;
            }
            else 
               rightValueAsInt = rightValue.asInt;
      }

      if (leftValueType == -1 || rightValueType == -1) // Prevents problems when doing comparisons like 1 == 2.
      {
         String leftValueAsString = leftValue.asString,
                rightValueAsString = rightValue.asString;
         int last = leftValueAsString.length() - 1;
         char lastChar = leftValueAsString.charAt(last);
         
         if (lastChar == 'l' || lastChar == 'L') // Strips off the final 'L', because toDouble() does not like it.
            leftValueAsString = leftValueAsString.substring(0, last);
         last = rightValueAsString.length() - 1;
         lastChar = rightValueAsString.charAt(last);
         if (lastChar == 'l' || lastChar == 'L')
            rightValueAsString = rightValueAsString.substring(0, last);
         leftValue.asDouble = leftValueAsDouble = Convert.toDouble(leftValueAsString);
         rightValue.asDouble = rightValueAsDouble = Convert.toDouble(rightValueAsString);
         left.valueType = right.valueType = SQLElement.DOUBLE;
         isFloatingPointType = true;
         
         compareType = SQLElement.DOUBLE; // juliana@225_12: a numeric constant in a boolean clause must have its type considered to be double.
      }
      
      switch (operandType) // Then performs the comparison.
      {
         case SQLElement.OP_REL_EQUAL:
         case SQLElement.OP_REL_DIFF:
            switch (compareType)
            {
               case SQLElement.DOUBLE: 
                  result = (Convert.doubleToLongBits(leftValueAsDouble) == Convert.doubleToLongBits(rightValueAsDouble)); 
                  break;
               case SQLElement.INT: 
                  result = (leftValueAsInt == rightValueAsInt); 
                  break;
               case SQLElement.LONG: 
                  result = (leftValueAsLong == rightValueAsLong); 
                  break;
               case SQLElement.DATETIME: 
                  result = (leftValueAsInt == rightValueAsInt) && (leftValueAsTime == rightValueAsTime); 
            }
            if (operandType == SQLElement.OP_REL_DIFF)
               result = !result;
            break;

         case SQLElement.OP_REL_GREATER:
         case SQLElement.OP_REL_LESS_EQUAL:
            switch (compareType)
            {
               case SQLElement.DOUBLE: 
                  result = (leftValueAsDouble > rightValueAsDouble);
                  break;
               case SQLElement.INT: 
                  result = (leftValueAsInt  > rightValueAsInt); 
                  break;
               case SQLElement.LONG: 
                  result = (leftValueAsLong > rightValueAsLong); 
                  break;
               case SQLElement.DATETIME: 
                  result = (leftValueAsInt == rightValueAsInt)? (leftValueAsTime > rightValueAsTime): (leftValueAsInt > rightValueAsInt); 
            }
            if (operandType == SQLElement.OP_REL_LESS_EQUAL)
               result = !result;
            break;

         case SQLElement.OP_REL_LESS:
         case SQLElement.OP_REL_GREATER_EQUAL:
            switch (compareType)
            {
               case SQLElement.DOUBLE: 
                  result = (leftValueAsDouble < rightValueAsDouble); 
                  break;
               case SQLElement.INT: 
                  result = (leftValueAsInt  < rightValueAsInt); 
                  break;
               case SQLElement.LONG: 
                  result = (leftValueAsLong < rightValueAsLong); 
                  break;
               case SQLElement.DATETIME: 
                  result = (leftValueAsInt == rightValueAsInt)? (leftValueAsTime < rightValueAsTime): (leftValueAsInt < rightValueAsInt); 
            }
            if (operandType == SQLElement.OP_REL_GREATER_EQUAL)
               result = !result;
            break;

         default:
            result = false;
      }
      return result;
   }
   
   /** 
    * Compares two strings using LIKE and NOT LIKE.
    * 
    * @param ignoreCase Indicates if both strings are CHARS_NOCASE.
    * @return The evaluation of the comparison expression.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   boolean matchStringOperands(boolean ignoreCase) throws IOException, InvalidDateException
   {
      boolean result = false;
      SQLBooleanClauseTree right = rightTree;
      SQLValue leftValue = leftTree.getOperandValue();

      if (leftValue == null) // null value
         return false;

      String leftString = leftValue.asString,
             strToMatch = right.strToMatch;

      // juliana@230_3: corrected a bug of LIKE using DATE and DATETIME not returning the correct result.
      leftString = Utils.formatDateDateTime(booleanClause.resultSet.table.db.driver.sBuffer, leftTree.valueType, leftValue);

      if (ignoreCase)
      {
         leftString = leftString.toLowerCase();
         if (strToMatch != null) // juliana@220_9: solved a NPE when using like '%' with a nocase string column.
            strToMatch = strToMatch.toLowerCase();
      }

      switch (right.patternMatchType)
      {
         case SQLBooleanClauseTree.PAT_MATCH_ANYTHING:
            result = true;
            break;

         case SQLBooleanClauseTree.PAT_MATCH_STARTS_WITH:
            result = leftString.startsWith(strToMatch);
            break;

         case SQLBooleanClauseTree.PAT_MATCH_ENDS_WITH:
            result = leftString.endsWith(strToMatch);
            break;

         case SQLBooleanClauseTree.PAT_MATCH_CONTAINS:
            result = leftString.indexOf(strToMatch) >= 0;
            break;

         case SQLBooleanClauseTree.PAT_MATCH_MIDDLE: // rnovais@568_1
            int pos = right.posPercent;
            result = leftString.startsWith(strToMatch.substring(0, pos))? (leftString.endsWith(strToMatch.substring(pos + 1))) : false;
            break;

         case SQLBooleanClauseTree.PAT_MATCH_EQUAL: // rnovais@_568_1
            result = leftString.equals(strToMatch);
      }

      if (operandType == SQLElement.OP_PAT_MATCH_NOT_LIKE)
         result = !result;

      return result;
   }

   /**
    * Normal comparison between two strings.
    * 
    * @param ignoreCase Indicates if both strings are CHARS_NOCASE.
    * @return The evaluation of the comparison expression.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   boolean compareStringOperands(boolean ignoreCase) throws IOException, InvalidDateException
   {
      int result;

      SQLValue leftValue = bothAreIdentifier? leftTree.valueJoin : leftTree.getOperandValue(),
               rightValue = rightTree.getOperandValue();

      if (leftValue == null || rightValue == null) // null value
         return false;

      String leftString = leftValue.asString, 
             rightString = rightValue.asString;

      if (ignoreCase)
         result = leftString.toLowerCase().compareTo(rightString.toLowerCase());
      else
         result = leftString.compareTo(rightString);

      switch (operandType)
      {
         case SQLElement.OP_REL_LESS:
            return result < 0;
            
         case SQLElement.OP_REL_EQUAL:
            return result == 0;

         case SQLElement.OP_REL_GREATER:
            return result > 0;         

         case SQLElement.OP_REL_GREATER_EQUAL:
            return result >= 0;

         case SQLElement.OP_REL_LESS_EQUAL:
            return result <= 0;
            
         case SQLElement.OP_REL_DIFF:
            return result != 0;
      }

      return false;
   }

   /**
    * Evaluates an expression tree.
    * 
    * @return The value of the expression evaluation.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    */
   boolean booleanTreeEvaluate() throws IOException, InvalidDateException, InvalidNumberException
   {
      boolean result = false, 
               leftResult = false, 
               rightResult = false;

      switch (operandType) // Checks the operand type of the tree.
      {
         // Relational operantors.
         case SQLElement.OP_REL_LESS:
         case SQLElement.OP_REL_EQUAL:
         case SQLElement.OP_REL_GREATER:
         case SQLElement.OP_REL_GREATER_EQUAL:            
         case SQLElement.OP_REL_LESS_EQUAL:
         case SQLElement.OP_REL_DIFF:
            switch (valueType) // Calls the right operation accordingly to the values type.
            {
               case SQLElement.SHORT:
               case SQLElement.INT:
               case SQLElement.LONG:
               case SQLElement.FLOAT:
               case SQLElement.DOUBLE:
               case SQLElement.DATE: // rnovais@567_2
               case SQLElement.DATETIME: // rnovais@567_2
                  result = compareNumericOperands();
                  break;
              
               case SQLElement.CHARS:
                  result = compareStringOperands(false);
                  break;

               case SQLElement.CHARS_NOCASE:
                  result = compareStringOperands(true);
                  break;
            }
            break;

         // LIKE operators.
         case SQLElement.OP_PAT_MATCH_LIKE:
         case SQLElement.OP_PAT_MATCH_NOT_LIKE:
            result = matchStringOperands(valueType == SQLElement.CHARS_NOCASE);
            break;

         case SQLElement.OP_BOOLEAN_AND: // AND connector.
            if (leftTree != null && rightTree != null) 
            {
               leftResult = leftTree.booleanTreeEvaluate();
               
               if (leftResult) // Short circuit: only evaluates the right tree if left result is TRUE.
               {
                  rightResult = rightTree.booleanTreeEvaluate();
                  result = leftResult && rightResult;
               }
            }
            break;
            
         case SQLElement.OP_BOOLEAN_OR: // OR connector.
            if (leftTree != null && rightTree != null) // Expects both trees to be not null.
            {
               leftResult = leftTree.booleanTreeEvaluate();

               if (leftResult) // Short circuit: only evaluates the right tree if the left result is FALSE.
                  result = true;
               else
               {
                  rightResult = rightTree.booleanTreeEvaluate();
                  result = leftResult || rightResult;
               }
            }
            break;

         // juliana@214_4: nots were removed.

         // IS operators.
         case SQLElement.OP_PAT_IS:
         case SQLElement.OP_PAT_IS_NOT:
            result = compareNullOperands();
      }

      return result;
   }

   /**
    * Binds the column information of the underlying table to the boolean clause tree nodes.
    *
    * @throws SQLParseException If a column can not be bound.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    */
   void bindColumnsSQLBooleanClauseTree() throws SQLParseException, InvalidDateException, InvalidNumberException
   {
      if (operandType == SQLElement.OP_IDENTIFIER) // If operand type is identifier, binds to a column in the table.
      {
         int dtParameter; // rnovais@568_10
         
         // Stores the binding information also in the boolean clause field list.
         int fieldIndex = booleanClause.fieldName2Index.get(nameSqlFunctionHashCode, -1);

         if (fieldIndex == -1) // The column could not be found.
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_UNKNOWN_COLUMN) + operandName);

         SQLResultSetField field = booleanClause.fieldList[fieldIndex];
         colIndex = field.tableColIndex;
         
         if (colIndex < 0) // The column could not be found.
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_UNKNOWN_COLUMN) + operandName);

         dtParameter = field.dataType;
         if (valueType != SQLElement.UNDEFINED && valueType != dtParameter)
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
         if (field.sqlFunction == -1) // rnovais@568_10: if it does not have a data function, stores the correct value type.
            valueType = dtParameter;
         else
         {
            if (field.dataType == SQLElement.UNDEFINED) // rnovais@570_5
               dtParameter = field.dataType = field.parameter.dataType;
            valueType = dtParameter;
         }
      }

      SQLBooleanClauseTree left = leftTree,
                           right = rightTree;
      
      // Bind the columns of the children trees.
      if (left != null)
         left.bindColumnsSQLBooleanClauseTree();
      if (right != null)
         right.bindColumnsSQLBooleanClauseTree();

      if (left != null && right != null) // Infers the operation resulting value type.
         inferOperationValueType();
      
      // rnovais@567_2: validates date and datetime in the rightTree.
      if ((left != null) && right.operandValue != null 
       && (left.valueType == SQLElement.DATE || left.valueType == SQLElement.DATETIME))
         right.operandValue.validateDateTime(booleanClause.tempDate, left.valueType);
   }

   /**
    * Infers the operation value type, according to the left and right values involved in the operation. 
    * 
    * @throws SQLParseException If left and right values have incompatible types or there is a blob type in the comparison.
    * @throws InvalidNumberException If an internal method throws it.
    */
   void inferOperationValueType() throws SQLParseException, InvalidNumberException
   {
      SQLBooleanClauseTree left = leftTree,
                           right = rightTree;
      
      if (left == null || right == null)
      {
         valueType = SQLElement.UNDEFINED;
         return;
      }
      if (right.operandType == SQLElement.OP_PAT_NULL)
      {
         valueType = left.valueType;
         return;
      }

      if (left.operandName != null) // rnovais@568_10: if it has a data type function, verifies if it can be applied.
      {
         SQLResultSetField field = left.booleanClause.fieldList[left.booleanClause.fieldName2Index.get(left.nameSqlFunctionHashCode == 0? 
                                                                left.nameHashCode : left.nameSqlFunctionHashCode, -1)];
         if (field.sqlFunction != -1)
            Utils.bindFunctionDataType(field.parameter.dataType, field.sqlFunction); 
      }
      
      if (operandType == SQLElement.OP_BOOLEAN_AND || operandType == SQLElement.OP_BOOLEAN_OR) // Boolean type.
      {
         valueType = SQLElement.BOOLEAN;
         return;
      }

      int leftOperandType = left.operandType, 
          rightOperandType = right.operandType,
          leftValueType = left.valueType, 
          rightValueType = right.valueType;

      if (leftValueType == SQLElement.BLOB || rightValueType == SQLElement.BLOB) // Blobs can't be compared.
         throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_COMP_BLOBS));

      boolean leftIsParameter = left.isParameter,
               rightIsParameter = right.isParameter;

      // In case one of them is a parameter, the tree has the type of the one that is not a parameter.
      // If both are parameters, the type is undefined (which is the default, anyway).
      if (leftIsParameter || rightIsParameter)
      {
         if (leftIsParameter)
         {
            left.valueType = rightValueType;
            valueType = rightValueType;
         }
         else
         {
            right.valueType = leftValueType;
            valueType = leftValueType;
         }
      }
      else
      {
         // juliana@201_12: both should be compared to DATE and DATETIME.
         boolean leftIsChar = (leftValueType == SQLElement.CHARS || leftValueType == SQLElement.CHARS_NOCASE || leftValueType == SQLElement.DATE 
                            || leftValueType == SQLElement.DATETIME),
                  rightIsChar = (rightValueType == SQLElement.CHARS || rightValueType == SQLElement.CHARS_NOCASE || leftValueType == SQLElement.DATE 
                            || leftValueType == SQLElement.DATETIME);

         if (leftIsChar != rightIsChar) // Can not mix a character type with a non-character type, except for DATE and DATETIME.
            throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));

         // If one of the operands is an identifier and the other one is not, it prevails the identifier operand value type.
         if (rightOperandType == SQLElement.OP_IDENTIFIER && leftOperandType != SQLElement.OP_IDENTIFIER)
         {
            valueType = rightValueType;
            left.convertValue(rightValueType);
         }
         else
         if (leftOperandType == SQLElement.OP_IDENTIFIER && rightOperandType != SQLElement.OP_IDENTIFIER)
         {
            valueType = leftValueType;
            right.convertValue(leftValueType);
         }
         else
            switch (operandType)
            {
               // Relational operators.
               case SQLElement.OP_REL_LESS:
               case SQLElement.OP_REL_EQUAL:               
               case SQLElement.OP_REL_GREATER:               
               case SQLElement.OP_REL_GREATER_EQUAL:
               case SQLElement.OP_REL_LESS_EQUAL:
               case SQLElement.OP_REL_DIFF:
                  if (leftIsChar || rightIsChar)
                  {
                     if (leftValueType == SQLElement.DATE || rightValueType == SQLElement.DATE) // rnovais@567_2
                        valueType = SQLElement.DATE;
                     else
                     if (leftValueType == SQLElement.DATETIME || rightValueType == SQLElement.DATETIME)
                        valueType = SQLElement.DATETIME;
                     else
                     {
                        // If both are identifiers, it prevails CHARS, in case one of them is CHARS (case sensitive).
                        if (leftOperandType == SQLElement.OP_IDENTIFIER && rightOperandType == SQLElement.OP_IDENTIFIER)
                           valueType = (leftValueType == SQLElement.CHARS || rightValueType == SQLElement.CHARS)? SQLElement.CHARS 
                                                                                                                : SQLElement.CHARS_NOCASE;
                        else
                           valueType = SQLElement.CHARS;
                     }
                  }
                  else
                  {
                     // This order is important.
                     if (leftValueType == SQLElement.DOUBLE || rightValueType == SQLElement.DOUBLE)
                        valueType = SQLElement.DOUBLE;
                     else
                     if (leftValueType == SQLElement.FLOAT || rightValueType == SQLElement.FLOAT)
                        valueType = SQLElement.FLOAT;
                     else
                     if (leftValueType == SQLElement.LONG || rightValueType == SQLElement.LONG)
                        valueType = SQLElement.LONG;
                     else
                     if (leftValueType == SQLElement.INT || rightValueType == SQLElement.INT)
                        valueType = SQLElement.INT;
                     else
                        valueType = SQLElement.SHORT;
                  }
                  break;

               // Like operators.   
               case SQLElement.OP_PAT_MATCH_LIKE:
               case SQLElement.OP_PAT_MATCH_NOT_LIKE:
                  if (!leftIsChar || !rightIsChar) // Only character types are allowed here.
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));

                  if (leftValueType == SQLElement.DATE || rightValueType == SQLElement.DATE) // rnovais@567_2
                     valueType = SQLElement.DATE;
                  else
                  if (leftValueType == SQLElement.DATETIME || rightValueType == SQLElement.DATETIME)
                     valueType = SQLElement.DATETIME;
                  else
                  {
                     // If both are identifiers, it prevails CHARS, in case one of them is CHARS (case sensitive).
                     if (leftOperandType == SQLElement.OP_IDENTIFIER && rightOperandType == SQLElement.OP_IDENTIFIER)
                     {
                        if (leftValueType == SQLElement.CHARS || rightValueType == SQLElement.CHARS)
                           valueType = SQLElement.CHARS;
                        else
                           valueType = SQLElement.CHARS_NOCASE;
                     }
                     else
                        valueType = SQLElement.CHARS;
                  }
                  break;

               default:
                  valueType = SQLElement.UNDEFINED;
            }
      }

      // Checks if the operand value type has floating point.
      isFloatingPointType = (valueType == SQLElement.DOUBLE || valueType == SQLElement.FLOAT);
   }

   // juliana@222_9: Some string conversions to numerical values could return spourious values if the string range were greater than the type range.
   /** 
    * Converts a number in the string format to its numerical representation.
    * 
    * @param type The number type.
    * @throws InvalidNumberException If the conversion fails.
    */
   void convertValue(int type) throws InvalidNumberException
   {
      switch (type)
      {
         case SQLElement.SHORT:
            operandValue.asShort = Convert.toShort(operandValue.asString);
            break;
         case SQLElement.INT:
            operandValue.asInt = Convert.toInt(operandValue.asString);
            break;
         case SQLElement.LONG:
            operandValue.asLong = Convert.toLong(operandValue.asString);
            break;
         case SQLElement.FLOAT:
            operandValue.asDouble = Utils.toFloat(operandValue.asString);
            break;
         case SQLElement.DOUBLE:
            operandValue.asDouble = Convert.toDouble(operandValue.asString);
            break;
         default:
            return;
      }
      valueType = type;
   }
   
   // juliana@214_4: removes not from expression trees so that indices can be used in more situations.
   /**
    * Removes the not operators from an expression tree.
    * 
    * @param expressionTree The expression tree passed by the user.
    * @return The expression tree without nots.
    */
   static SQLBooleanClauseTree removeNots(SQLBooleanClauseTree expressionTree)
   {
      if (expressionTree == null) // Does nothing with an empty expression is used.
         return null;
      
      SQLBooleanClauseTree right = expressionTree.rightTree;
      
      if (expressionTree.operandType == SQLElement.OP_BOOLEAN_NOT)
      {
         switch (right.operandType)
         {
            case SQLElement.OP_BOOLEAN_AND: // not (A and B) == not A or not B.
               SQLBooleanClauseTree tree = new SQLBooleanClauseTree(expressionTree.booleanClause);
               tree.operandType = SQLElement.OP_BOOLEAN_OR;
               tree.leftTree = expressionTree;
               tree.rightTree = right;
               tree.rightTree.operandType = SQLElement.OP_BOOLEAN_NOT;
               expressionTree.parent = tree.rightTree.parent = tree;
               right = expressionTree.rightTree = tree.rightTree.leftTree;
               tree.rightTree.leftTree = null;
               right.parent = expressionTree;
               expressionTree = tree;
               break;
            case SQLElement.OP_BOOLEAN_OR: // not (A or B) == not A and not B.
               tree = new SQLBooleanClauseTree(expressionTree.booleanClause);
               tree.operandType = SQLElement.OP_BOOLEAN_AND;
               tree.leftTree = expressionTree;
               tree.rightTree = right;
               tree.rightTree.operandType = SQLElement.OP_BOOLEAN_NOT;
               expressionTree.parent = tree.rightTree.parent = tree;
               right = expressionTree.rightTree = tree.rightTree.leftTree;
               tree.rightTree.leftTree = null;
               right.parent = expressionTree;
               expressionTree = tree;
               break;
            case SQLElement.OP_BOOLEAN_NOT: // not not == null.
               right.rightTree.parent = expressionTree.parent;
               expressionTree = right.rightTree;
               break;
            case SQLElement.OP_REL_LESS: // not less == greater equal.
               right.operandType = SQLElement.OP_REL_GREATER_EQUAL;
               right.parent = expressionTree.parent;
               expressionTree = right;
               break;   
            case SQLElement.OP_REL_EQUAL: // not equal == dif.
               right.operandType = SQLElement.OP_REL_DIFF;
               right.parent = expressionTree.parent;
               expressionTree = right;
               break;     
            case SQLElement.OP_REL_GREATER: // not greater == less equal.
               right.operandType = SQLElement.OP_REL_LESS_EQUAL;
               right.parent = expressionTree.parent;
               expressionTree = right;
               break;   
            case SQLElement.OP_REL_GREATER_EQUAL: // not greater equal == less.
               right.operandType = SQLElement.OP_REL_LESS;
               right.parent = expressionTree.parent;
               expressionTree = right;
               break;   
            case SQLElement.OP_REL_LESS_EQUAL: // not less equal == greates. 
               right.operandType = SQLElement.OP_REL_GREATER;
               right.parent = expressionTree.parent;
               expressionTree = right;
               break;  
            case SQLElement.OP_REL_DIFF: // not dif == equal.
               right.operandType = SQLElement.OP_REL_EQUAL;
               right.parent = expressionTree.parent;
               expressionTree = right;
               break; 
            case SQLElement.OP_PAT_MATCH_LIKE: // not like == not like.
               right.operandType = SQLElement.OP_PAT_MATCH_NOT_LIKE;
               right.parent = expressionTree.parent;
               expressionTree = right;
               break;
            case SQLElement.OP_PAT_MATCH_NOT_LIKE: // not not like == like.
               right.operandType = SQLElement.OP_PAT_MATCH_LIKE;
               right.parent = expressionTree.parent;
               expressionTree = right;
               break;
            case SQLElement.OP_PAT_IS: // not is == is not.
               right.operandType = SQLElement.OP_PAT_IS_NOT;
               right.parent = expressionTree.parent;
               expressionTree = right;
               break;
            case SQLElement.OP_PAT_IS_NOT: // not is not == is.
               right.operandType = SQLElement.OP_PAT_IS;
               right.parent = expressionTree.parent;
               expressionTree = right;
         }   
      }   
      
      // Recursion.
      expressionTree.leftTree = removeNots(expressionTree.leftTree);
      expressionTree.rightTree = removeNots(expressionTree.rightTree);
      
      return expressionTree;
   }
   
   // juliana@226_15: corrected a bug that would make a prepared statement with where clause and indices not work correctly after the first 
   // execution.
   /**
    * Clones an expression tree of a where clause of a select prepared statement. 
    * 
    * @param destTree The old destination tree. If the nodes are the same of the tree cloned, the node is reused in the cloned tree.
    * @return A clone of the where clause expression tree.
    */
   SQLBooleanClauseTree cloneTree(SQLBooleanClauseTree destTree)
   {
      SQLBooleanClause clause = booleanClause;
      SQLBooleanClauseTree[] paramList = clause.paramList;
      SQLBooleanClauseTree tree = null;
      int i = clause.paramCount;
      
      while (--i >= 0)
         if (this == paramList[i])
         {
            tree = this;
            break;
         }
      if (i == -1)
         if (destTree != this)
         {
            (tree = new SQLBooleanClauseTree(clause)).booleanClause = clause;
            tree.bothAreIdentifier = bothAreIdentifier;
            tree.colIndex = colIndex;
            tree.hasIndex = hasIndex;
            tree.indexRs = indexRs;
            tree.isFloatingPointType = isFloatingPointType;
            tree.isParameter = isParameter;
            tree.isParamValueDefined = isParamValueDefined;
            tree.nameHashCode = nameHashCode;
            tree.nameSqlFunctionHashCode = nameSqlFunctionHashCode;
            tree.operandName = operandName;
            tree.operandType = operandType;
            tree.operandValue = operandValue;
            tree.patternMatchType = patternMatchType;
            tree.posPercent = posPercent;
            tree.strToMatch = strToMatch;
            tree.tempValue = tempValue;
            tree.valueType = valueType;
         }
         else
            tree = destTree;
      if (leftTree != null)
         (tree.leftTree = leftTree.cloneTree(destTree == null? null : destTree.leftTree)).parent = tree;
      if (rightTree != null)
         (tree.rightTree = rightTree.cloneTree(destTree == null? null : destTree.rightTree)).parent = tree;
      return tree;
   }
   
   /**
    * Create the operand value object if necessary and tests its type.
    * 
    * @param type The type of the operand value.
    * @throws DriverException If the types are incompatible.
    */
   private void createValueAndTestType(int type)
   {
      isParamValueDefined = true;
      if (operandValue == null)
         operandValue = new SQLValue();
      if (valueType == SQLElement.UNDEFINED)
         valueType = type;
      else if (valueType != type)
         throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_INCOMPATIBLE_TYPES));
      
      // juliana@230_37: solved a possible bug when using prepared statements without issuing PreparedStatement.clearAllParameters().      
      operandValue.asString = null;
   }
}
