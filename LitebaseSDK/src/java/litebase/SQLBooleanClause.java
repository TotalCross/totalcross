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
 * Internal use only. Represents a boolean clause (<code>WHERE</code> or <code>HAVING</code>) in a SQL query.
 */
class SQLBooleanClause
{
   /**
    * The maximum number of indexes to be applied.
    */
   static final int MAX_NUM_INDEXES_APPLIED = 32;

   /**
    * Resulting boolean clause expression tree.
    */
   SQLBooleanClauseTree expressionTree;

   /**
    * The associated result set.
    */
   ResultSet resultSet;

   // Other information used during the colum binding process.
   /**
    * A list of all fields referenced in the boolean clause.
    */
   SQLResultSetField[] fieldList = new SQLResultSetField[SQLElement.MAX_NUM_COLUMNS];

   /**
    * The number of fields.
    */
   int fieldsCount;

   /**
    * Table that maps the field name to an index in the field list.
    */
   IntHashtable fieldName2Index = new IntHashtable(SQLElement.MAX_NUM_COLUMNS);

   /**
    * The list of trees that contains the parameter list of the boolean clause.
    */
   SQLBooleanClauseTree[] paramList = new SQLBooleanClauseTree[SQLElement.MAX_NUM_PARAMS];

   /**
    * The length of the parameter list.
    */
   int paramCount;

   /**
    * The columns whose indexes were applied to the boolean clause. A column can be listed more than once, in case it is listed more than once in the 
    * boolean clause.
    */
   byte[] appliedIndexesCols = new byte[MAX_NUM_INDEXES_APPLIED];

   /**
    * The constant values to be used by the indexes that were applied to the boolean clause.
    */
   SQLBooleanClauseTree[] appliedIndexesValueTree = new SQLBooleanClauseTree[MAX_NUM_INDEXES_APPLIED];

   /**
    * The relational operators to be used by the indexes that were applied to the boolean clause.
    */
   byte[] appliedIndexesRelOps = new byte[MAX_NUM_INDEXES_APPLIED];

   /**
    * The number of indexes to be applied.
    */
   int appliedIndexesCount;

   /**
    * The boolean operator to be used to combine the result set of each index. Can be either <code>SQLElement.OP_BOOLEAN_AND</code>, 
    * <code>SQLElement.OP_BOOLEAN_OR</code>, or <code>SQLElement.OP_BOOLEAN_NONE</code> (in case only one index was used).
    */
   int appliedIndexesBooleanOp;

   /**
    * Indicates if it is a where clause or a having clause.
    */
   boolean isWhereClause = true;

   /**
    * A backup of the expression tree.
    */
   SQLBooleanClauseTree expressionTreeBak;

   /**
    * The tables of the correspondent indexes.
    */
   Table[] appliedIndexesTables = new Table[MAX_NUM_INDEXES_APPLIED];

   /**
    * Indicates if the result set will be indexed.
    */
   private int appliedIndexRs = -1;

   /**
    * Type of the where clause: <code><B>AND</B><code> of different result sets = 0, and <code><B>OR</B></code> of different result sets = 1.
    */
   int type = -1;

   /**
    * The composed indices applied.
    */
   ComposedIndex[] appliedComposedIndexes = new ComposedIndex[MAX_NUM_INDEXES_APPLIED];
   
   /** 
    * Temporary date. 
    */
   Date tempDate = new Date();
   
   // juliana@226_3: improved index application.
   /**
    * Applies the table indices to the boolean clause. The method will possibly transform the SQL boolean tree, eliminating the branches that can be 
    * resolved through the table indexes.
    *
    * @param tableIndices The table indices; each position in the array relates to a column in the table; a <code>null</code> value indicates no 
    * index on that column.
    * @param hasComposedIndex Indicates if the table has a composed index.
    * @return <code>true</code>, if any table index was applied to the boolean clause; <code>false</code>, otherwise.
    */
   boolean sqlbooleanclauseApplyTableIndices(Index[] tableIndices, boolean hasComposedIndex)
   {
      // juliana@223_2: corrected a bug that would throw an exception if the where clause if of the form 1 = 1.
      if (!isWhereClause || fieldList.length == 0) // Indices can only be applied to the where clause with fields.
         return false;

      int columnsCount = tableIndices.length;

      if (!hasComposedIndex) // Verifies if it has simple indices.
      { 
         while (--columnsCount >= 0 && tableIndices[columnsCount] == null);
         
         if (columnsCount < 0) // If there are no indices, returns.
            return false;
      }

      // Traverses the tree, from the parent to the rightmost tree until the boolean operator changes. To simplify the algorithm and considering 
      // that complex boolean expressions (the ones enclosed by parenthesis) are always connected to the left branch), only the branches connected
      // to the right side of the tree are candidates to be replaced by table indexing.
      SQLBooleanClauseTree curTree = expressionTree, 
                           originalTree;
      SQLBooleanClauseTree[] indexesValueTree;
      SQLBooleanClauseTree leftTree;
      int curOperandType, 
          leftOperandType, 
          rightOperandType, 
          count, 
          countAppliedIndices = 0, 
          i, 
          j;
      boolean appliedComposedIndex,
              isLeft = false;
      byte[] columns;
      byte[] operators;
      byte[] columnsComp;
      Table table = fieldList[0].table;
      ComposedIndex[] composedIndices = table.composedIndices;
      ComposedIndex[] appliedCI = appliedComposedIndexes;
      ComposedIndex currCompIndex;
      
      if (hasComposedIndex)
      {
         indexesValueTree = new SQLBooleanClauseTree[fieldsCount];
         columns = new byte[fieldsCount];
         operators = new byte[fieldsCount];
      }
      else
      {
         indexesValueTree = null;
         columns = operators = null;
      }

      appliedIndexesBooleanOp = SQLElement.OP_NONE;
      
      while (curTree != null)
      {
         leftTree = curTree.leftTree;

         switch (curOperandType = curTree.operandType) // Checks the operand type.
         {
            // juliana@214_4: nots were removed.

            case SQLElement.OP_BOOLEAN_AND:
            case SQLElement.OP_BOOLEAN_OR:
               
               // Checks if the boolean connector is different than the previous one. If so, leaves the loop, since for now, for simplicty, the 
               // algorithm does not combine different boolean operators.
               if (appliedIndexesBooleanOp != SQLElement.OP_NONE && appliedIndexesBooleanOp != curOperandType)
               {
                  curTree = null;
                  break;
               }

               appliedIndexesBooleanOp = curOperandType;

               // Checks if the left tree has a simple boolean operand. If so, try to apply an index on it.
               leftOperandType = leftTree.operandType;
               appliedComposedIndex = false;

               // juliana@250_2: corrected a problem of composed indices not returning the expected result.
               if ((leftOperandType >= SQLElement.OP_REL_LESS && leftOperandType <= SQLElement.OP_REL_DIFF)
                || (leftTree.patternMatchType == SQLBooleanClauseTree.PAT_MATCH_STARTS_WITH 
                 && (leftOperandType == SQLElement.OP_PAT_MATCH_LIKE || leftOperandType == SQLElement.OP_PAT_MATCH_NOT_LIKE)))
               {
                  if (hasComposedIndex && curOperandType == SQLElement.OP_BOOLEAN_AND && !isLeft) // First verifies if it can apply a composed index.
                  {
                     originalTree = curTree;
                     count = 0;
                     Convert.fill(indexesValueTree, 0, fieldsCount, null);
                     Convert.fill(columns, 0, fieldsCount, 0);
                     Convert.fill(operators, 0, fieldsCount, 0);

                     while (true)
                     {
                        leftTree.getBranchProperties(columns, operators, indexesValueTree, count);

                        // Limitation; Composed index only for EQUALS. A composed index can't be applied if the column is not part of the index.
                        if (count >= operators.length || operators[count] != SQLElement.OP_REL_EQUAL)
                        {
                           count = 0;
                           break; // Doesn't apply the composed index.
                        }

                        count++;

                        // Verifies if the right operator is one of the above.
                        if ((rightOperandType = curTree.rightTree.operandType) == SQLElement.OP_BOOLEAN_AND)
                           curTree = curTree.rightTree;
                        else
                        if ((rightOperandType >= SQLElement.OP_REL_EQUAL && rightOperandType <= SQLElement.OP_REL_LESS_EQUAL)
                         || (curTree.rightTree.patternMatchType == SQLBooleanClauseTree.PAT_MATCH_STARTS_WITH 
                          && (rightOperandType == SQLElement.OP_PAT_MATCH_LIKE || rightOperandType == SQLElement.OP_PAT_MATCH_NOT_LIKE)))
                        {
                           curTree.rightTree.getBranchProperties(columns, operators, indexesValueTree, count);
                           
                           // Limitation: composed index only for EQUALS.
                           if (count >= operators.length || operators[count] != SQLElement.OP_REL_EQUAL) 
                           {
                              count = 0;
                              break; // Doesn't apply the composed index.
                           }
                           count++;
                           break;
                        }
                        else
                           break; // The next operator is an OR, ends the loop.
                     }
                     if (count >= 2) // It has an AND operator on at least 2 fields?
                     {
                        i = table.numberComposedIndices;
                        while (--i >= 0)
                        {
                           currCompIndex = composedIndices[i];
                           appliedComposedIndex = false;
                           columnsComp = currCompIndex.columns;
                           
                           // juliana@202_6: A composed index can only be used if all its columns are "ANDED" in the where clause. 
                           j = currCompIndex.columns.length;
                           if (columns.length >= j)
                           {
                              appliedComposedIndex = true;
                              while (--j >= 0)
                                 if (columns[j] != columnsComp[j])
                                 {
                                    appliedComposedIndex = false;
                                    break;
                                 }
                           }
                              
                           if (appliedComposedIndex)
                           {
                              appliedCI[appliedIndexesCount] = currCompIndex;
                              curTree = sqlbooleanclauseApplyComposedIndexToBranch(originalTree, columns, operators, indexesValueTree, 
                                                                                                                     currCompIndex);
                              break;
                           }
                           else
                              curTree = originalTree;
                        }
                     }
                     
                  }
                  if (!appliedComposedIndex)
                     sqlbooleanclauseApplyIndexToBranch(curTree.leftTree, tableIndices, isLeft);
               }
               if (!appliedComposedIndex) // Goes to the right tree.
               {   
                  if (isLeft)
                     curTree = leftTree;
                  else
                     curTree = curTree.rightTree;
               }
               break;

            // Reached the rightmost node. Triwal to apply the index and ends the loop.
            case SQLElement.OP_PAT_MATCH_NOT_LIKE:
            case SQLElement.OP_PAT_MATCH_LIKE:
               if (curTree.rightTree.patternMatchType != SQLBooleanClauseTree.PAT_MATCH_STARTS_WITH
               &&  curTree.rightTree.patternMatchType != SQLBooleanClauseTree.PAT_MATCH_EQUAL)
               {
                  curTree = null;
                  break;
               }
               // else falls through.
            case SQLElement.OP_REL_LESS:
            case SQLElement.OP_REL_EQUAL:
            case SQLElement.OP_REL_GREATER:
            case SQLElement.OP_REL_GREATER_EQUAL:            
            case SQLElement.OP_REL_LESS_EQUAL:
            case SQLElement.OP_REL_DIFF:
               countAppliedIndices = appliedIndexesCount;
               sqlbooleanclauseApplyIndexToBranch(curTree, tableIndices, isLeft);
               if (countAppliedIndices == appliedIndexesCount)
                  curTree = null;
               else
                  curTree = expressionTree; 
               break;

            default: // Anything else, stops the loop.
               curTree = null;
         }

         if (appliedIndexesCount == MAX_NUM_INDEXES_APPLIED) // If the number of indexes to be applied reached the limit leaves the loop.
            break;
         
         if (curTree == null && appliedIndexesCount == 0 && !isLeft)
         {
            isLeft = true;
            curTree = expressionTree;
         }
      }     

      return appliedIndexesCount > 0;
   }
   
   /**
    * Tries to apply an index to a branch of the expression tree that contains a relational expression.
    *
    * @param branch A branch of the expression tree.
    * @param indexMap An index bitmap.
    * @param isLeft Indicates if the index is being applied to the left branch.
    */
   private void sqlbooleanclauseApplyIndexToBranch(SQLBooleanClauseTree branch, Index[] indexesMap, boolean isLeft)
   {
      int relationalOp = branch.operandType;

      // Checks if the relational expression involves a column and a constant.
      SQLBooleanClauseTree left = branch.leftTree, 
                           right = branch.rightTree;
      boolean leftIsColumn = (left.operandType == SQLElement.OP_IDENTIFIER), 
               rightIsColumn = (right.operandType == SQLElement.OP_IDENTIFIER);
      byte[] appliedIndexesColsAux = appliedIndexesCols;
      byte[] appliedIndexesRelOpsAux = appliedIndexesRelOps;
      SQLBooleanClauseTree[] appliedIndexesValueTreeAux = appliedIndexesValueTree;
      SQLResultSetField[] list = fieldList;
      
      if (leftIsColumn != rightIsColumn)
      {
         int column = (leftIsColumn? left.colIndex : right.colIndex);

         int i = fieldsCount;
         while (--i >= 0) 
            if (list[i].tableColIndex == column && list[i].isDataTypeFunction) // An index cannot be applied to a function in the where clause.
               return;

         if (indexesMap[column] != null) // Checks if the column is indexed.
         {
            // Adds the index to the list of applied indexes.
            int n = appliedIndexesCount++;
            appliedIndexesColsAux[n] = (byte)column;
            appliedIndexesValueTreeAux[n] = leftIsColumn? right : left;
            appliedIndexesRelOpsAux[n] = (byte)relationalOp;

            SQLBooleanClauseTree parent = branch.parent;

            if (parent == null) // Removes the branch from the expression tree.
               expressionTree = null; // The branch has no parent. So, no expression tree will be left.
            else
            {
               SQLBooleanClauseTree sibling = (branch == parent.leftTree? parent.rightTree : parent.leftTree), 
                                    grandParent = parent.parent;
               
               // Links the branch sibling to its grandparent, removing the branch from the tree, as result.
               if (grandParent == null)
                  expressionTree = sibling;
               else if (isLeft)
                  grandParent.leftTree = sibling;
               else
                  grandParent.rightTree = sibling;
               sibling.parent = grandParent;
            }
         }
      }
   }
   
   /**
    * Applies the composed index and removes the correspondent branch of the tree.
    *
    * @param branch A branch of the expression tree.
    * @param columns The columns present in the expression tree.
    * @param operators The operators of the expression tree.
    * @param indexesValueTree The part of the tree that uses indices.
    * @param ci The composed index.
    * @return The current branch of the tree.
    */
   private SQLBooleanClauseTree sqlbooleanclauseApplyComposedIndexToBranch(SQLBooleanClauseTree branch, byte[] columns, byte[] operators, 
                                                                            SQLBooleanClauseTree[] indexesValueTree, ComposedIndex ci)
   {
      int i = -1, 
          n, 
          length = ci.columns.length;
      byte[] appliedIndexesColsAux = appliedIndexesCols;
      byte[] appliedIndexesRelOpsAux = appliedIndexesRelOps;
      SQLBooleanClauseTree[] appliedIndexesValueTreeAux = appliedIndexesValueTree;
      
      // Checks if the column is indexed.
      while (++i < length)
      {
         // Adds the index to the list of applied indexes.
         appliedIndexesColsAux[n = appliedIndexesCount++] = columns[i];
         appliedIndexesValueTreeAux[n] = indexesValueTree[i];
         appliedIndexesRelOpsAux[n] = operators[i];
      }

      SQLBooleanClauseTree parent = branch.parent, 
                           root = branch;

      while (--i >= 0)
         branch = branch.rightTree;

      if (parent == null)
      {
         branch.parent = null;
         if (branch.operandType != SQLElement.OP_BOOLEAN_AND && branch.operandType != SQLElement.OP_BOOLEAN_OR) // Is the end of the root?
            branch = null;
         expressionTree = branch;
      }
      else
      {
         branch.parent = parent;
         root.parent.rightTree = branch;
      }
      return branch;
   }

   // juliana@253_7: improved index application on filters when using joins.
   // juliana@226_3: improved index application.
   /**
    * Applies the table indexes to the boolean clause. The method will possibly transform the SQL boolean tree, to eliminate the branches that can be 
    * resolved through the table indexes.
    *
    * @return <code>true</code>, if any table index was applied to the boolean clause; <code>false</code>, otherwise.
    */
   boolean sqlbooleanclauseApplyTableIndexesJoin()
   {
      if (!isWhereClause) // Indexes can only be applied to a where clause.
         return false;
      
      // Traverses the tree, from the parent to the rightmost tree, until the boolean operator changes. To simplify the algorithm and considering 
      // that complex boolean expressions (the ones enclosed by parenthesis) are always connected to the left branch), only the branches connected to
      // the right side of the tree are candidates to be replaced by table indexing.
      SQLBooleanClauseTree curTree = expressionTree,
                           leftTree;
      int curOperandType, 
          leftOperandType,
          countAppliedIndices = 0;
      boolean isLeft = false;

      appliedIndexesBooleanOp = SQLElement.OP_NONE;
      while (curTree != null)
      {
         leftTree = curTree.leftTree;
         
         switch (curOperandType = curTree.operandType) // Checks the type of operand.
         {
            // juliana@214_4: nots were removed.

            case SQLElement.OP_BOOLEAN_AND:
            case SQLElement.OP_BOOLEAN_OR:
               
               // Checks if the boolean connector is different than the previous one. If so, leaves the loop, since for now, for simplicty, the 
               // algorithm does not combine different boolean operators.
               if (appliedIndexesBooleanOp != SQLElement.OP_NONE && appliedIndexesBooleanOp != curOperandType)
               {
                  curTree = null;
                  break;
               }

               appliedIndexesBooleanOp = curOperandType;
               if (appliedIndexRs == -1)
                  appliedIndexRs = leftTree.indexRs;

               // Checks if the left tree has a simple boolean operand. If so, tries to apply an index on it.
               leftOperandType = leftTree.operandType;

               if ((leftOperandType >= SQLElement.OP_REL_EQUAL && leftOperandType <= SQLElement.OP_REL_LESS_EQUAL)
                 || ((leftOperandType == SQLElement.OP_PAT_MATCH_LIKE || leftOperandType == SQLElement.OP_PAT_MATCH_NOT_LIKE) 
                  && leftTree.patternMatchType == SQLBooleanClauseTree.PAT_MATCH_STARTS_WITH))
                  sqlbooleanclauseApplyIndexToBranchJoin(leftTree, isLeft);

               if (curTree.rightTree.indexRs != appliedIndexRs)
                  if (curOperandType == SQLElement.OP_BOOLEAN_AND)
                     type = Utils.WC_TYPE_AND_DIFF_RS;
                  else // 'OR' of different result sets, leaves the loop.
                  
                  {
                     type = Utils.WC_TYPE_OR_DIFF_RS;
                     curTree = null;
                     break;
                  }

               if (isLeft)
                  curTree = leftTree;
               else
                  curTree = curTree.rightTree;
               break;

            // Reached the rightmost node. Tries to apply the index and ends the loop.
            case SQLElement.OP_PAT_MATCH_NOT_LIKE:
            case SQLElement.OP_PAT_MATCH_LIKE:
               if (curTree.rightTree.patternMatchType != SQLBooleanClauseTree.PAT_MATCH_STARTS_WITH
               &&  curTree.rightTree.patternMatchType != SQLBooleanClauseTree.PAT_MATCH_EQUAL)
               {
                  curTree = null;
                  break;
               }
               // else fall through.
            case SQLElement.OP_REL_LESS:
            case SQLElement.OP_REL_EQUAL:
            case SQLElement.OP_REL_GREATER:
            case SQLElement.OP_REL_GREATER_EQUAL:            
            case SQLElement.OP_REL_LESS_EQUAL:
            case SQLElement.OP_REL_DIFF:
               countAppliedIndices = appliedIndexesCount;
               sqlbooleanclauseApplyIndexToBranchJoin(curTree, isLeft);
               if (countAppliedIndices == appliedIndexesCount)
                  curTree = null;
               else
                  curTree = expressionTree; 
               break;
               
            default: // Anything else, stops the loop.
               curTree = null;
         }
         
         if (appliedIndexesCount == MAX_NUM_INDEXES_APPLIED) // If the number of indexes to be applied reached the limit, leaves the loop.
            break;
         
         if (curTree == null && appliedIndexesCount == 0 && !isLeft)
         {
            isLeft = true;
            curTree = expressionTree;
         }
      }

      return appliedIndexesCount > 0;
   }

   // juliana@253_7: improved index application on filters when using joins.
   /**
    * Tries to apply an index to a branch of the expression tree that contains a relational expression.
    *
    * @param branch The branch of the expression tree.
    * @param isLeft Indicates if the index is being applied to the left branch.
    */
   private void sqlbooleanclauseApplyIndexToBranchJoin(SQLBooleanClauseTree branch, boolean isLeft)
   {
      int relationalOp = branch.operandType;

      // Checks if the relational expression involves a column and a constant.
      SQLBooleanClauseTree left = branch.leftTree, 
                           right = branch.rightTree, tree;
      boolean leftIsColumn = (left.operandType == SQLElement.OP_IDENTIFIER), 
              rightIsColumn = (right.operandType == SQLElement.OP_IDENTIFIER);

      if (branch.bothAreIdentifier)
      {
         tree = right;
         SQLResultSetField field = fieldList[fieldName2Index.get(tree.nameSqlFunctionHashCode != 0? 
                                                                                              tree.nameSqlFunctionHashCode : tree.nameHashCode, -1)];

         if (field.table.columnIndices[right.colIndex] != null)
            right.hasIndex = true;
      }

      if (leftIsColumn != rightIsColumn)
      {
         int column;
         if (leftIsColumn)
         {
            column = left.colIndex;
            tree = left;
         }
         else
         {
            column = right.colIndex;
            tree = right;
         }
         SQLResultSetField field = fieldList[fieldName2Index.get(tree.nameSqlFunctionHashCode != 0 ? 
                                                                                              tree.nameSqlFunctionHashCode : tree.nameHashCode, -1)];
         
         // juliana@285_1: solved a possible wrong result if the query had join and a filter with function in a column with an index.
         // Checks if the column is indexed.
         if (field.table.columnIndices[column] != null && field.isDataTypeFunction == false)
         {
            // Adds the index to the list of applied indexes.
            int n = appliedIndexesCount++;
            appliedIndexesCols[n] = (byte)column;
            appliedIndexesValueTree[n] = leftIsColumn ? right : left;
            appliedIndexesRelOps[n] = (byte)relationalOp;
            appliedIndexesTables[n] = field.table;

            SQLBooleanClauseTree parent = branch.parent;

            if (parent == null) // Removes the branch from the expression tree.
               expressionTree = null; // The branch has no parent. So, no expression tree will be left.
            else
            {
               SQLBooleanClauseTree sibling = (branch == parent.leftTree? parent.rightTree : parent.leftTree);
               SQLBooleanClauseTree grandParent = parent.parent;
               
               // Links the branch sibling to its grandparent, removing the branch from the tree, as result.
               if (grandParent == null)
                  expressionTree = sibling;
               else if (isLeft)
                  grandParent.leftTree = sibling;
               else
                  grandParent.rightTree = sibling;
               sibling.parent = grandParent;
            }
         }
      }
   }
   
   /**
    * Evaluate the boolean clause, accordingly to values of the current record of the given <code>ResultSet</code>.
    *
    * @throws DriverException if a parameter is not defined.
    */
   void sqlBooleanClausePreVerify() throws DriverException
   {
      // Checks if there are parameters defined in the clause and if all them had their values assigned.
      int i = paramCount;
      while (--i >= 0)
         if (!paramList[i].isParamValueDefined)
            throw new DriverException(LitebaseMessage.getMessage(LitebaseMessage.ERR_PARAMETER_NOT_DEFINED) + i);
   }

   /**
    * Evaluates the boolean clause, accordingly to values of the current record of the given <code>ResultSet</code>.
    *
    * @param rs the ResultSet used for the evaluation.
    * @return <code>true</code>, if the current record of the result set satisfies the boolean clause; <code>false</code>, otherwise.
    * @throws IOException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    * @throws InvalidNumberException If an internal method throws it.
    */
   boolean sqlBooleanClauseSatisfied(ResultSet rs) throws IOException, InvalidDateException, InvalidNumberException
   {
      resultSet = rs;
      return expressionTree.booleanTreeEvaluate();
   }
   
   /**
    * Binds the column information of the underlying table list to the boolean clause.
    *
    * @param names2Index <code>IntHashtable</code> that maps the column names to the column indexes.
    * @param columnTypes The data types of each column.
    * @param tableList The table list of the select clause.
    * @throws InvalidNumberException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void bindColumnsSQLBooleanClause(IntHashtable names2Index, byte[] columnTypes, SQLResultSetTable[] tableList) throws InvalidDateException, InvalidNumberException
   {
      // These two are only used in the expressionTree.
      if (tableList != null) // The having clause has already been verified.
         verifyColumnNamesOnTableList(fieldList, tableList);
      else // Rearranges the having clause.
      {
         int i = fieldList.length;
         SQLResultSetField field;
         while (--i >= 0)
         {
            field = fieldList[i];
            field.tableColIndex = names2Index.get(field.aliasHashCode, -1); // Sets the column indexes of the temp table.

            if (field.sqlFunction == SQLElement.FUNCTION_DT_NONE)
               field.dataType = columnTypes[field.tableColIndex];
         }
      }
      expressionTree = SQLBooleanClauseTree.removeNots(expressionTree); // juliana@214_4
      expressionTree.bindColumnsSQLBooleanClauseTree(); // Binds the field information in the tree to the table columns.
   }
   
   /**
    * Verifies if the column names are correct and belongs to the table list and is used only to verify if where clause and having clause field list 
    * is the field list of the where/having clause.
    * 
    * @param sqlBooleanClauseFieldList The field list of the where/having clause.
    * @param tableList The table list.
    * @throws SQLParseException If there is an unknown or an ambiguos column name.
    */
   void verifyColumnNamesOnTableList(SQLResultSetField[] sqlBooleanClauseFieldList, SQLResultSetTable[] tableList) throws SQLParseException
   {
      int size = sqlBooleanClauseFieldList.length,
          index = -1,
          i,
          hashAliasTableName;
      boolean foundFirst;
      Table currentTable;
      SQLResultSetField field;
      
      while (--size >= 0)
      {
         field = sqlBooleanClauseFieldList[size];

         if (field.tableName != null)
         {
            // Verifies if it is a valid table name.
            hashAliasTableName = field.tableName.hashCode();
            currentTable = null;
            i = tableList.length;
            while (--i >= 0)
               if (tableList[i].aliasTableNameHashCode == hashAliasTableName)
               {
                  currentTable = tableList[i].table;
                  break;
               }
            
            if (currentTable == null
             || (index = currentTable.htName2index.get(field.tableColName.hashCode(), -1)) == -1)
               throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_UNKNOWN_COLUMN) + field.alias);
            
            field.table = currentTable;
            field.tableColIndex = index;
            if (field.sqlFunction == SQLElement.FUNCTION_DT_NONE)
               field.dataType = currentTable.columnTypes[index];
            else
               field.parameter.dataType = currentTable.columnTypes[index];
            field.indexRs = i;
         }
         else
         {
            // Verifies if the column name in field list is ambiguous.
            currentTable = null;
            foundFirst = false;
            i = tableList.length;
            while (--i >= 0)
            {
               currentTable = tableList[i].table;
               index = currentTable.htName2index.get(field.tableColHashCode, -1);
               if (index != -1)
                  if (foundFirst)
                     throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_AMBIGUOUS_COLUMN_NAME) + field.alias);
                  else
                  {
                     foundFirst = true;
                     field.table = currentTable;
                     field.tableColIndex = index;
                     if (field.sqlFunction == SQLElement.FUNCTION_DT_NONE)
                        field.dataType = currentTable.columnTypes[index];
                     else
                        field.parameter.dataType = currentTable.columnTypes[index];
                     field.indexRs = i;
                  }
            }
            if (!foundFirst)
               throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_UNKNOWN_COLUMN) + field.alias);
         }
      }
   }
   
   /**
    * Binds the column information of the underlying table to the boolean clause.
    *
    * @param names2Index <code>IntHashtable</code> that maps the column names to the column indexes.
    * @param columnTypes The data types of each column.
    * @param rsTable The <code>SQLResultSetTable</code> table of the update or delete statement.
    * @throws InvalidNumberException If an internal method throws it.
    * @throws InvalidDateException If an internal method throws it.
    */
   void bindColumnsSQLBooleanClause(IntHashtable names2Index, byte[] columnTypes, SQLResultSetTable rsTable) throws InvalidDateException, InvalidNumberException
   {
      verifyColumnNamesOnTable(fieldList, rsTable); // These two are only used in the expressionTree.
      expressionTree = SQLBooleanClauseTree.removeNots(expressionTree); // juliana@214_4
      expressionTree.bindColumnsSQLBooleanClauseTree(); // Binds the field information in the tree to the table columns.
   }
   
   /**
    * Verifies if the column names are correct and belongs to the table and is used only to verify if where clause field list is the field list of 
    * the where clause.
    * 
    * @param sqlBooleanClauseFieldList The field list of the where clause.
    * @param rsTable The <code>SQLResultSetTable</code> table of the update or delete statement.
    */
   void verifyColumnNamesOnTable(SQLResultSetField[] sqlBooleanClauseFieldList, SQLResultSetTable rsTable)
   {
      int size = sqlBooleanClauseFieldList.length,
          index = -1;
      Table currentTable;
      SQLResultSetField field;
      
      while (--size >= 0)
      {
         field = sqlBooleanClauseFieldList[size];

         if (field.tableName != null)
         {
            // Verifies if it is a valid table name.
            if (rsTable.aliasTableNameHashCode != field.tableName.hashCode()
             || (index = (currentTable = rsTable.table).htName2index.get(field.tableColName.hashCode(), -1)) == -1)
               throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_UNKNOWN_COLUMN) + field.alias);
            field.table = currentTable;
            field.tableColIndex = index;
            if (field.sqlFunction == SQLElement.FUNCTION_DT_NONE)
               field.dataType = currentTable.columnTypes[index];
            else
               field.parameter.dataType = currentTable.columnTypes[index];
         }
         else
         {
            // Verifies if the column name in field list is ambiguous.
            currentTable = rsTable.table;
            index = currentTable.htName2index.get(field.tableColHashCode, -1);
            if (index != -1)
            {
               field.table = currentTable;
               field.tableColIndex = index;
               if (field.sqlFunction == SQLElement.FUNCTION_DT_NONE)
                  field.dataType = currentTable.columnTypes[index];
               else
                  field.parameter.dataType = currentTable.columnTypes[index];
            }  
            else
               throw new SQLParseException(LitebaseMessage.getMessage(LitebaseMessage.ERR_UNKNOWN_COLUMN) + field.alias);
         }
      }
   }

}
