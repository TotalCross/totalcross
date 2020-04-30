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
 * Internal use only. Represents a boolean clause (<code>WHERE</code> or <code>HAVING</code>) in a SQL query.
 */

#include "SQLBooleanClause.h"

/**
 * Creates and initializes a boolean clause.
 *
 * @param heap The heap to allocate a <code>SQLBooleanClause</code> structure. 
 * @return A pointer to a <code>SQLBooleanClause</code> structure. 
 */
SQLBooleanClause* initSQLBooleanClause(Heap heap)
{
	TRACE("sqlBooleanClause")
   SQLBooleanClause* sqlBooleanClause = (SQLBooleanClause*)TC_heapAlloc(heap, sizeof(SQLBooleanClause));
   sqlBooleanClause->fieldName2Index = TC_htNew(MAXIMUMS, heap);
   sqlBooleanClause->appliedIndexRs = sqlBooleanClause->type = -1;
   return sqlBooleanClause;
}

// juliana@226_3: improved index application.
/**
 * Applies the table indices to the boolean clause. The method will possibly transform the SQL boolean tree, eliminating the branches that can be 
 * resolved through the table indexes.
 *
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param tableIndices The table indices; each position in the array relates to a column in the table; a <code>null</code> value indicates no 
 * index on that column.
 * @param columnsCount The number of columns of the table.
 * @param hasComposedIndex Indicates if the table has a composed index.
 * @return <code>true</code>, if any table index was applied to the boolean clause; <code>false</code>, otherwise.
 */
bool applyTableIndexes(SQLBooleanClause* booleanClause, Index** tableIndexes, int32 columnsCount, bool hasComposedIndex)
{
	TRACE("applyTableIndexes")
   SQLResultSetField** fieldList = booleanClause->fieldList;

   // juliana@223_2: corrected a bug that would throw an exception if the where clause if of the form 1 = 1.
   Table* table = *fieldList? (*fieldList)->table : null;
   
   int32 i = columnsCount,
         j,
         curOperandType,
         leftOperandType,
         count,
         fieldsCount = booleanClause->fieldsCount,
         rightOperandType,
         numberComposedIndexes = table? table->numberComposedIndexes : 0,
         countAppliedIndices = 0;
   bool appliedComposedIndex,
        isLeft = false;
   uint8 columns[MAXIMUMS + 1];
   uint8 operators[MAXIMUMS + 1];
   SQLBooleanClauseTree* curTree;
   SQLBooleanClauseTree* leftTree;
   SQLBooleanClauseTree* rightTree;
   SQLBooleanClauseTree* originalTree;
   SQLBooleanClauseTree* indexesValueTree[MAXIMUMS + 1];
   ComposedIndex** composedIndexes = table? table->composedIndexes : null;
   ComposedIndex** appliedComposedIndexes = booleanClause->appliedComposedIndexes;
   ComposedIndex* currCompIndex;

   if (!booleanClause->isWhereClause) // Indices can only be applied to the where clause.
      return false;

   if (!hasComposedIndex) // Verifies if it has simple indices.
   {
      while (--i >= 0 && !tableIndexes[i]);

      if (i < 0) // If there are no indices, returns.
         return false;
   }

   // Traverses the tree, from the parent to the rightmost tree until the boolean operator changes. To simplify the algorithm and considering 
   // that complex boolean expressions (the ones enclosed by parenthesis) are always connected to the left branch), only the branches connected
   // to the right side of the tree are candidates to be replaced by table indexing.
   curTree = booleanClause->expressionTree;
   booleanClause->appliedIndexesBooleanOp = OP_NONE;

   while (curTree)
   {
      leftTree = curTree->leftTree;
      rightTree = curTree->rightTree;
      
      switch (curOperandType = curTree->operandType) // Checks the type of operand.
      {
         // juliana@214_4: nots were removed.

         case OP_BOOLEAN_AND:
         case OP_BOOLEAN_OR:
         {
            
            // Checks if the boolean connector is different than the previous one. If so, leaves the loop, since for now, for simplicty, the 
            // algorithm does not combine different boolean operators.
            if (booleanClause->appliedIndexesBooleanOp && booleanClause->appliedIndexesBooleanOp != curOperandType)
            {
               curTree = null;
               break;
            }

            booleanClause->appliedIndexesBooleanOp = curOperandType;

            // Checks if the left tree has a simple boolean operand. If so, try to apply an index on it.
            leftOperandType = leftTree->operandType;
            appliedComposedIndex = false;
            if ((leftOperandType >= OP_REL_EQUAL && leftOperandType <= OP_REL_LESS_EQUAL)
             || ((leftOperandType == OP_PAT_MATCH_LIKE || leftOperandType == OP_PAT_MATCH_NOT_LIKE)
              && leftTree->patternMatchType == PAT_MATCH_STARTS_WITH))
            {
               // juliana@250_2: corrected a problem of composed indices not returning the expected result.
               if (hasComposedIndex && curOperandType == OP_BOOLEAN_AND && !isLeft) // First verifies if it can apply a composed index.
               {
                  originalTree = curTree;
                  count = 0;
                  xmemzero(indexesValueTree, fieldsCount * TSIZE);
                  xmemzero(columns, fieldsCount);
                  xmemzero(operators, fieldsCount);

                  while (true)
                  {
                     
                     getBranchProperties(leftTree, columns, operators, indexesValueTree, count, fieldsCount);

                     // Limitation; Composed index only for EQUALS. A composed index can't be applied if the column is not part of the index.
                     if (count >= fieldsCount || operators[count] != OP_REL_EQUAL || operators[count] == 255) 
                     {
                        count = 0;
                        break; // Doesn't apply the composed index.
                     }

                     count++;

                     // Verifies if the right operator is one of the above.
                     if ((rightOperandType = rightTree->operandType) == OP_BOOLEAN_AND)
                     {
                        curTree = rightTree;
                        rightTree = curTree->rightTree;
                     }
                     else if ((leftOperandType >= OP_REL_EQUAL && leftOperandType <= OP_REL_LESS_EQUAL)
                           || ((leftOperandType == OP_PAT_MATCH_LIKE || leftOperandType == OP_PAT_MATCH_NOT_LIKE)
                            && leftTree->patternMatchType == PAT_MATCH_STARTS_WITH))
                     {
                        getBranchProperties(rightTree, columns, operators, indexesValueTree, count, fieldsCount);
                        
                        // Limitation: composed index only for EQUALS.
                        if (count >= fieldsCount || operators[count] != OP_REL_EQUAL || operators[count] == 255)  
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
                     i = numberComposedIndexes;
                     while (--i >= 0)
                     {
                        currCompIndex = composedIndexes[i];
                        appliedComposedIndex = false;
                        
								// juliana@202_6: A composed index can only be used if all its columns are "ANDED" in the where clause.
								if (fieldsCount >= currCompIndex->numberColumns)
								{
                           appliedComposedIndex = true;
									j = currCompIndex->numberColumns;
                           while (--j >= 0)
										if (columns[j] != currCompIndex->columns[j])
										{
											appliedComposedIndex = false;
											break;
										}
							   }
                        
                        if (appliedComposedIndex)
                        {   
                           appliedComposedIndexes[booleanClause->appliedIndexesCount] = currCompIndex;
                           curTree = applyComposedIndexToBranch(booleanClause, originalTree, columns, operators, indexesValueTree, currCompIndex);
                           break;
                        }
                        else
                           curTree = originalTree;
                     }
                  }
               }
               if (!appliedComposedIndex)
                  applyIndexToBranch(booleanClause, leftTree, tableIndexes, isLeft);
            }

            if (!appliedComposedIndex) // Goes to the right tree.
            {   
               if (isLeft)
                  curTree = leftTree;
               else
                  curTree = rightTree;
            }
            break;
         }

         // Reached the rightmost node. Triwal to apply the index and ends the loop.
         case OP_PAT_MATCH_NOT_LIKE:
         case OP_PAT_MATCH_LIKE:
            if (rightTree->patternMatchType != PAT_MATCH_STARTS_WITH
            &&  rightTree->patternMatchType != PAT_MATCH_EQUAL)
            {
               curTree = null;
               break;
            }
            // else falls through.
         case OP_REL_EQUAL:
         case OP_REL_DIFF:
         case OP_REL_GREATER:
         case OP_REL_GREATER_EQUAL:
         case OP_REL_LESS:
         case OP_REL_LESS_EQUAL:
            countAppliedIndices = booleanClause->appliedIndexesCount;
            applyIndexToBranch(booleanClause, curTree, tableIndexes, isLeft);
            if (countAppliedIndices == booleanClause->appliedIndexesCount)
               curTree = null;
            else
               curTree = booleanClause->expressionTree; 
            break;

         default: // Anything else, stops the loop.
            curTree = null;
      }

      if (booleanClause->appliedIndexesCount == MAX_NUM_INDEXES_APPLIED) // If the number of indexes to be applied reached the limit leaves the loop.
         break;
         
      if (!curTree && !booleanClause->appliedIndexesCount && !isLeft)
      {
         isLeft = true;
         curTree = booleanClause->expressionTree;
      }
   }
   return booleanClause->appliedIndexesCount > 0;
}

/**
 * Tries to apply an index to a branch of the expression tree that contains a relational expression.
 *
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param branch A branch of the expression tree.
 * @param indexMap An index bitmap.
 * @param isLeft Indicates if the index is being applied to the left branch.
 */
void applyIndexToBranch(SQLBooleanClause* booleanClause, SQLBooleanClauseTree* branch, Index** indexesMap, bool isLeft)
{
	TRACE("applyIndexToBranch")
   int32 relationalOp = branch->operandType;

   // Checks if the relational expression involves a column and a constant.
   SQLBooleanClauseTree* left  = branch->leftTree;
   SQLBooleanClauseTree* right = branch->rightTree;

   bool leftIsColumn = (left->operandType == OP_IDENTIFIER);
   bool rightIsColumn = (right->operandType == OP_IDENTIFIER);

   if (leftIsColumn != rightIsColumn) 
   {
      int32 column = (leftIsColumn? left->colIndex : right->colIndex),
            i = booleanClause->fieldsCount;
      uint8* appliedIndexesCols = booleanClause->appliedIndexesCols;
      uint8* appliedIndexesRelOps = booleanClause->appliedIndexesRelOps;
      SQLBooleanClauseTree** appliedIndexesValueTree = booleanClause->appliedIndexesValueTree;
      SQLResultSetField** fieldList = booleanClause->fieldList;

      while (--i >= 0) // An index cannot be applied to a function in the where clause.
         if (fieldList[i]->tableColIndex == column && fieldList[i]->isDataTypeFunction) 
            return;

      if (indexesMap[column]) // Checks if the column is indexed.
      {
         SQLBooleanClauseTree* parent = branch->parent;

         // Adds the index to the list of applied indexes.
         int32 n = booleanClause->appliedIndexesCount++;
         appliedIndexesCols[n] = column;
         appliedIndexesValueTree[n] = leftIsColumn? right : left;
         appliedIndexesRelOps[n] = relationalOp;

         // Remove the branch from the expression tree.
         if (parent)
         {
            SQLBooleanClauseTree* sibling;
            SQLBooleanClauseTree* grandParent;
            sibling = (branch == parent->leftTree)? parent->rightTree : parent->leftTree;
            grandParent = parent->parent;

            // Links the branch sibling to its grandparent, removing the branch from the tree, as result.
            if (grandParent)
            {
               if (isLeft)
                  grandParent->leftTree = sibling;
               else
                  grandParent->rightTree = sibling;
            }   
            else                  
               booleanClause->expressionTree = sibling;

            sibling->parent = grandParent;
         }
         else // Removes the branch from the expression tree.
            booleanClause->expressionTree = null; // The branch has no parent. So, no expression tree will be left.
      }
   }
}

/**
 * Applies the composed index and removes the correspondent branch of the tree.
 *
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param branch A branch of the expression tree.
 * @param columns The columns present in the expression tree.
 * @param operators The operators of the expression tree.
 * @param indexesValueTree The part of the tree that uses indices.
 * @param compIndex The composed index.
 * @return The current branch of the tree.
 */
SQLBooleanClauseTree* applyComposedIndexToBranch(SQLBooleanClause* booleanClause, SQLBooleanClauseTree* branch, uint8* columns, uint8* operators, 
																											 SQLBooleanClauseTree** indexesValueTree, ComposedIndex* compIndex)
{
	TRACE("applyComposedIndexToBranch")
   int32 i = -1,
         length = compIndex->numberColumns,
         n;
   uint8* appliedIndexesCols = booleanClause->appliedIndexesCols;
   uint8* appliedIndexesRelOps = booleanClause->appliedIndexesRelOps;
   SQLBooleanClauseTree** appliedIndexesValueTree = booleanClause->appliedIndexesValueTree;
   SQLBooleanClauseTree* parent = branch->parent;
   SQLBooleanClauseTree* root = branch;

   while (++i < length) // Checks if the column is indexed.
   {
      // Adds the index to the list of applied indexes.
      appliedIndexesCols[n = booleanClause->appliedIndexesCount++] = columns[i];
      appliedIndexesValueTree[n] = indexesValueTree[i];
      appliedIndexesRelOps[n] = operators[i];
   }

   while (--i >= 0)
      branch = branch->rightTree;

   if (parent)
   {
      branch->parent = parent;
      root->parent->rightTree = branch;
      return branch;
   }
   else
   {
      branch->parent = null;
      if (branch->operandType != OP_BOOLEAN_AND && branch->operandType != OP_BOOLEAN_OR) // Is the end of the root?
         branch = null;
      booleanClause->expressionTree = branch;
      return branch;
   }
}

// juliana@253_7: improved index application on filters when using joins.
// juliana@226_3: improved index application.
/**
 * Applies the table indexes to the boolean clause. The method will possibly transform the SQL boolean tree, to eliminate the branches that can be 
 * resolved through the table indexes.
 *
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @return <code>true</code>, if any table index was applied to the boolean clause; <code>false</code>, otherwise.
 */
bool applyTableIndexesJoin(SQLBooleanClause* booleanClause)
{
	TRACE("applyTableIndexesJoin")
   SQLBooleanClauseTree* curTree;
   SQLBooleanClauseTree* leftTree;
   SQLBooleanClauseTree* rightTree;
   int32 curOperandType,
         leftOperandType,
         countAppliedIndices = 0;
   bool isLeft = false;      
         
   if (!booleanClause->isWhereClause) // Indexes can only be applied to a where clause.
      return false;

   // Traverses the tree, from the parent to the rightmost tree, until the boolean operator changes. To simplify the algorithm and considering 
   // that complex boolean expressions (the ones enclosed by parenthesis) are always connected to the left branch), only the branches connected to
   // the right side of the tree are candidates to be replaced by table indexing.
   curTree = booleanClause->expressionTree;
   booleanClause->appliedIndexesBooleanOp = OP_NONE;

   while (curTree)
   {
      leftTree = curTree->leftTree;
      rightTree = curTree->rightTree;
      
      switch (curOperandType = curTree->operandType) // Checks the type of operand.
      {
         // juliana@214_4: nots were removed.

         case OP_BOOLEAN_AND:
         case OP_BOOLEAN_OR:
         {
            
            // Checks if the boolean connector is different than the previous one. If so, leaves the loop, since for now, for simplicty, the 
            // algorithm does not combine different boolean operators.
            if (booleanClause->appliedIndexesBooleanOp != OP_NONE && booleanClause->appliedIndexesBooleanOp != curOperandType)
            {
               curTree = null;
               break;
            }

            booleanClause->appliedIndexesBooleanOp = curOperandType;

            if (booleanClause->appliedIndexRs == -1)
               booleanClause->appliedIndexRs = leftTree->indexRs;

            // Checks if the left tree has a simple boolean operand. If so, tries to apply an index on it.
            leftOperandType = leftTree->operandType;

            if ((leftOperandType >= OP_REL_EQUAL && leftOperandType <= OP_REL_LESS_EQUAL)
             || ((leftOperandType == OP_PAT_MATCH_LIKE || leftOperandType == OP_PAT_MATCH_NOT_LIKE) 
              && leftTree->patternMatchType == PAT_MATCH_STARTS_WITH))
               applyIndexToBranchJoin(booleanClause, leftTree, isLeft);

            if (curTree->rightTree->indexRs != booleanClause->appliedIndexRs)
            {
               if (curOperandType == OP_BOOLEAN_AND)
                  booleanClause->type = WC_TYPE_AND_DIFF_RS;
               else // 'OR' of different resultsets, leaves the loop.
               {
                  booleanClause->type = WC_TYPE_OR_DIFF_RS;
                  curTree = null;
                  break;
               }
            }
            if (isLeft)
               curTree = leftTree;
            else
               curTree = rightTree;
            break;
         }
            
         // Reached the rightmost node. Tries to apply the index and ends the loop.
         case OP_PAT_MATCH_NOT_LIKE:
         case OP_PAT_MATCH_LIKE:
            if (rightTree->patternMatchType != PAT_MATCH_STARTS_WITH && rightTree->patternMatchType != PAT_MATCH_EQUAL)
            {
               curTree = null;
               break;
            }

            // Else falls through.
         case OP_REL_EQUAL:
         case OP_REL_DIFF:
         case OP_REL_GREATER:
         case OP_REL_GREATER_EQUAL:
         case OP_REL_LESS:
         case OP_REL_LESS_EQUAL:
            countAppliedIndices = booleanClause->appliedIndexesCount;
            applyIndexToBranchJoin(booleanClause, curTree, isLeft);
            if (countAppliedIndices == booleanClause->appliedIndexesCount)
               curTree = null;
            else
               curTree = booleanClause->expressionTree; 
            break;

         default: // Anything else, stops the loop.
            curTree = null;
      }

      // If the number of indexes to be applied reached the limit, leaves the loop.
      if (booleanClause->appliedIndexesCount == MAX_NUM_INDEXES_APPLIED)
         break;
      
      if (!curTree && !booleanClause->appliedIndexesCount && !isLeft)
      {
         isLeft = true;
         curTree = booleanClause->expressionTree;
      }
   }
   return booleanClause->appliedIndexesCount > 0;
}

// juliana@253_7: improved index application on filters when using joins.
/**
 * Tries to apply an index to a branch of the expression tree that contains a relational expression.
 *
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param branch The branch of the expression tree.
 * @param isLeft Indicates if the index is being applied to the left branch.
 */
void applyIndexToBranchJoin(SQLBooleanClause* booleanClause, SQLBooleanClauseTree* branch, bool isLeft)
{
	TRACE("applyIndexToBranchJoin")
   int32 relationalOp = branch->operandType;

   // Checks if the relational expression involves a column and a constant.
   SQLBooleanClauseTree* tree; 
   SQLBooleanClauseTree* left  = branch->leftTree;
   SQLBooleanClauseTree* right = branch->rightTree;

   bool leftIsColumn = left->operandType == OP_IDENTIFIER;
   bool rightIsColumn = right->operandType == OP_IDENTIFIER;
   SQLResultSetField** fieldList = booleanClause->fieldList;
   Table* table;
   SQLResultSetField* field;
   int32 fieldIndex;

   if (branch->bothAreIdentifier && fieldList[fieldIndex = getFieldIndex(right)]->table->columnIndexes[right->colIndex])
      right->hasIndex = true;

   if (leftIsColumn != rightIsColumn)
   {
      Table** appliedIndexTables;
      int32 column;
      uint8* appliedIndexesCols = booleanClause->appliedIndexesCols;
      SQLBooleanClauseTree** appliedIndexesValueTree = booleanClause->appliedIndexesValueTree;
      uint8* appliedIndexesRelOps = booleanClause->appliedIndexesRelOps;
      appliedIndexTables = booleanClause->appliedIndexesTables;

      if (leftIsColumn)
         column = (tree = left)->colIndex;
      else
         column = (tree = right)->colIndex;

      // juliana@285_1: solved a possible wrong result if the query had join and a filter with function in a column with an index.
      // Checks if the column is indexed.
      if ((table = (field = fieldList[fieldIndex = getFieldIndex(tree)])->table)->columnIndexes[column] && !field->isDataTypeFunction)
      {
         // Adds the index to the list of applied indexes.
         int32 n = booleanClause->appliedIndexesCount++;
         SQLBooleanClauseTree* parent = branch->parent;
         appliedIndexesCols[n] = column;
         appliedIndexesValueTree[n] = leftIsColumn? right: left;
         appliedIndexesRelOps[n] = relationalOp;
         appliedIndexTables[n] = table;

         // Remove the branch from the expression tree.
         if (parent)
         {
            SQLBooleanClauseTree* sibling = (branch == parent->leftTree)? parent->rightTree : parent->leftTree;
            SQLBooleanClauseTree* grandParent = parent->parent;

            // Links the branch sibling to its grandparent, removing the branch from the tree, as result.
            if (grandParent)
            {
               if (isLeft)
                  grandParent->leftTree = sibling;
               else
                  grandParent->rightTree = sibling;
            }   
            else                  
               booleanClause->expressionTree = sibling;
            sibling->parent = grandParent;
         }
         else
            booleanClause->expressionTree = null; // The branch has no parent. So, no expression tree will be left.
      }
   }
}

/**
 * Evaluate the boolean clause, accordingly to values of the current record of the given <code>ResultSet</code>.
 *
 * @param context The thread context where the function is being executed.
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @return <code>true</code> if all parameter are defined; <code>false</code>, otherwise.
 * @throws DriverException if a parameter is not defined.
 */
bool sqlBooleanClausePreVerify(Context context, SQLBooleanClause* booleanClause)
{
	TRACE("sqlBooleanClausePreVerify")
   if (booleanClause) // guich@504_14
   {
      // Checks if there are parameters defined in the clause and if all them had their values assigned.
      SQLBooleanClauseTree** paramList = booleanClause->paramList;
      int32 i = booleanClause->paramCount;
      while(--i >= 0)
         if (!paramList[i]->isParamValueDefined)
         {
            TC_throwExceptionNamed(context, "litebase.DriverException", getMessage(ERR_PARAMETER_NOT_DEFINED), i);
            return false;
         }
   }
   return true;
}

/**
 * Evaluates the boolean clause, accordingly to values of the current record of the given <code>ResultSet</code>.
 *
 * @param resultSet the ResultSet used for the evaluation.
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param heap A heap to alocate temporary strings in the expression tree.
 * @return 1, if the current record of the result set satisfies the boolean clause; 0  if the current record of the result set does not satisfy the 
 * boolean clause; -1, otherwise.
 */
int32 sqlBooleanClauseSatisfied(Context context, SQLBooleanClause* booleanClause, ResultSet* resultSet, Heap heap)
{
	TRACE("sqlBooleanClauseSatisfied")
   booleanClause->resultSet = resultSet;
   return booleanTreeEvaluate(context, booleanClause->expressionTree, heap);
}

/**
 * Binds the column information of the underlying table list to the boolean clause.
 *
 * @param context The thread context where the function is being executed.
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param names2Index <code>IntHashtable</code> that maps the column names to the column indexes.
 * @param columnTypes The data types of each column.
 * @param tableList The table list of the select clause.
 * @param tableListSize The number of tables of the table list.
 * @param heap A heap to allocate some new <code>SQLBooleanClauseTree</code> nodes.
 * @return <code>true</code>, if the boolean clause was bound successfully; <code>false</code>, otherwise. 
 */
bool bindColumnsSQLBooleanClause(Context context, SQLBooleanClause* booleanClause, Hashtable* names2Index, int8* columnTypes, 
											                                                  SQLResultSetTable** tableList, int32 tableListSize, Heap heap)
{
	TRACE("bindColumnsSQLBooleanClause")

   // These two are only used in the expressionTree.
   if (tableList != null) // The having clause has already been verified.
   {
      if (!verifyColumnNamesOnTableList(context, booleanClause->fieldList, booleanClause->fieldsCount, tableList, tableListSize))
         return false;
   }
   else // Rearranges the having clause.
   {
      SQLResultSetField* field;
      SQLResultSetField** fieldList = booleanClause->fieldList;
      int32 i = booleanClause->fieldsCount;
      while (--i >= 0)
      {
         field = fieldList[i];
         field->tableColIndex = TC_htGet32Inv(names2Index, field->aliasHashCode); // Sets the column indexes of the temp table.
         if (field->sqlFunction == FUNCTION_DT_NONE)
            field->dataType = (int8)columnTypes[field->tableColIndex];
      }
   }
	booleanClause->expressionTree = removeNots(booleanClause->expressionTree, heap); // juliana@214_4
   return bindColumnsSQLBooleanClauseTree(context, booleanClause->expressionTree); // Binds the field information in the tree to the table columns.
}

/**
 * Verifies if the column names are correct and belongs to the table list and is used only to verify if where clause and having clause field list 
 * is the field list of the where/having clause.
 * 
 * @param context The thread context where the function is being executed.
 * @param fieldList The field list of the where/having clause.
 * @param fieldsCount The number of fields.
 * @param tableList The table list.
 * @param tableListSize The numbers of tables of the table list.
 * @return <code>true</code>, if field name verification found no problems; <code>false</code>, otherwise. 
 * @throws SQLParseException If there is an unknown or an ambiguos column name.
 */
bool verifyColumnNamesOnTableList(Context context, SQLResultSetField** fieldList, int32 fieldsCount, SQLResultSetTable** tableList, 
											                                                                    int32 tableListSize)
{
	TRACE("verifyColumnNamesOnTableList")
   int32 index,
         hashAliasTableName,
         j;
   bool foundFirst;
   Table* currentTable;
   SQLResultSetField* field;

   while (--fieldsCount >= 0)
   {
      if ((field = fieldList[fieldsCount])->tableName)
      {
         hashAliasTableName = TC_hashCode(field->tableName);
         j = -1;
         
         // Verifies if it is a valid table name.
         currentTable = null;
         while (++j < tableListSize)
         {
            if (tableList[j]->aliasTableNameHashCode == hashAliasTableName)
            {
               currentTable = tableList[j]->table;
               break;
            }
         }
         if (!currentTable || (index = TC_htGet32Inv(&currentTable->htName2index, TC_hashCode(field->tableColName))) < 0)
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_UNKNOWN_COLUMN), field->alias);
            return false;
         }
         field->table = currentTable;
         field->tableColIndex = index;
         if (field->sqlFunction == FUNCTION_DT_NONE)
            field->dataType = (int8)currentTable->columnTypes[index];
         else
            field->parameter->dataType = (int8)currentTable->columnTypes[index];
         field->indexRs = j;
      }
      else // Verifies if the column name in field list is ambiguous.
      {
         j = -1;
         foundFirst = false;
         while (++j < tableListSize)
         {
            index = TC_htGet32Inv(&(currentTable = tableList[j]->table)->htName2index, field->tableColHashCode); 
            if (index >= 0)
            {
               if (foundFirst)
               {
                  TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_AMBIGUOUS_COLUMN_NAME), field->alias);
                  return false;
               }
               else
               {
                  foundFirst = true;
                  field->table = currentTable;
                  field->tableColIndex = index;
                  if (field->sqlFunction == FUNCTION_DT_NONE)
                     field->dataType = (int8)currentTable->columnTypes[index];
                  else
                     field->parameter->dataType = (int8)currentTable->columnTypes[index];
                  field->indexRs = j;
               }
            }
         }
         if (!foundFirst)
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_UNKNOWN_COLUMN), fieldList[fieldsCount]->alias);
            return false;
         }
      }

   }
   return true;
}

/**
 * Binds the column information of the underlying table to the boolean clause.
 *
 * @param context The thread context where the function is being executed.
 * @param booleanClause A pointer to a <code>SQLBooleanClause</code> structure.
 * @param rsTable The <code>SQLResultSetTable</code> table of the update or delete statement.
 * @param heap A heap to allocate some new <code>SQLBooleanClauseTree</code> nodes.
 * @return <code>true</code>, if the boolean clause was bound successfully; <code>false</code>, otherwise. 
 */
bool bindColumnsSQLBooleanClauseSimple(Context context, SQLBooleanClause* clause, SQLResultSetTable* rsTable, Heap heap)
{
	TRACE("bindColumnsSQLBooleanClauseSimple")

   if (!verifyColumnNamesOnTable(context, clause->fieldList, clause->fieldsCount, rsTable)) 
      return false;
  
	clause->expressionTree = removeNots(clause->expressionTree, heap); // juliana@214_4
   return bindColumnsSQLBooleanClauseTree(context, clause->expressionTree); // Binds the field information in the tree to the table columns.
}

/**
 * Verifies if the column names are correct and belongs to the table and is used only to verify if where clause field list is the field list of 
 * the where clause.
 * 
 * @param context The thread context where the function is being executed.
 * @param fieldList The field list of the where/having clause.
 * @param fieldsCount The number of fields.
 * @param rsTable The <code>SQLResultSetTable</code> table of the update or delete statement.
 * @return <code>true</code>, if field name verification found no problems; <code>false</code>, otherwise. 
 * @throws SQLParseException If there is an unknown or an ambiguos column name.
 */
bool verifyColumnNamesOnTable(Context context, SQLResultSetField** fieldList, int32 fieldsCount, SQLResultSetTable* rsTable)
{
	TRACE("verifyColumnNamesOnTable")
   int32 index;
   Table* currentTable;
   SQLResultSetField* field;

   while (--fieldsCount >= 0)
   {
      if ((field = fieldList[fieldsCount])->tableName)
      {
         // Verifies if it is a valid table name.
         if (rsTable->aliasTableNameHashCode != TC_hashCode(field->tableName)
          || (index = TC_htGet32Inv(&(currentTable = rsTable->table)->htName2index, TC_hashCode(field->tableColName))) < 0)
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_UNKNOWN_COLUMN), field->alias);
            return false;
         }
         field->table = currentTable;
         field->tableColIndex = index;
         if (field->sqlFunction == FUNCTION_DT_NONE)
            field->dataType = (int8)currentTable->columnTypes[index];
         else
            field->parameter->dataType = (int8)currentTable->columnTypes[index];
      }
      else // Verifies if the column name in field list is ambiguous.
      {
         if ((index = TC_htGet32Inv(&(currentTable = rsTable->table)->htName2index, field->tableColHashCode)) >= 0)
         {
            field->table = currentTable;
            field->tableColIndex = index;
            if (field->sqlFunction == FUNCTION_DT_NONE)
               field->dataType = (int8)currentTable->columnTypes[index];
            else
               field->parameter->dataType = (int8)currentTable->columnTypes[index];
         }
			else
         {
            TC_throwExceptionNamed(context, "litebase.SQLParseException", getMessage(ERR_UNKNOWN_COLUMN), field->alias);
            return false;
         }
      }

   }
   return true;
}
