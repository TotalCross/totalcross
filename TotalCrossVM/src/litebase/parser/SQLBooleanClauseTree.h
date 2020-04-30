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

#ifndef SQLBOOLEANCLAUSETREE_H
#define SQLBOOLEANCLAUSETREE_H

#include "Litebase.h"

/**
 * Creates a <code>SQLBooleanClauseTree</code> with the associated <code>SQLBooleanClause</code>.
 *
 * @param booleanClause The associated <code>SQLBooleanClause</code>.
 * @param heap The heap to allocate the <code>SQLBooleanClauseTree</code> structure.
 * @return A pointer to a <code>SQLBooleanClauseTree</code> structure.
 */
SQLBooleanClauseTree* initSQLBooleanClauseTree(SQLBooleanClause* booleanClause, Heap heap);

/**
 * Sets the tree operand as a string literal.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure.
 * @param value The string literal value.
 */
void setOperandStringLiteral(SQLBooleanClauseTree* booleanClauseTree, JCharP value);

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
bool setNumericParamValue(Context context, SQLBooleanClauseTree* booleanClauseTree, VoidP value, int32 type);

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
bool setParamValueString(Context context, SQLBooleanClauseTree* booleanClauseTree, JCharP value, int32 length);

/**
 * Checks the if the operand value string contains pattern matching characters and assigns the proper matching type.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 */
void setPatternMatchType(SQLBooleanClauseTree* booleanClauseTree);

/**
 * Weighs the tree to order the table on join operation.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 */
void weightTheTree(SQLBooleanClauseTree* booleanClauseTree);

/**
 * Prepares the tree for the join operation.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 */
void setIndexRsOnTree(SQLBooleanClauseTree* booleanClauseTree);

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
								                                                                            int32 position, int32 fieldsCount);
/** 
 * Gets a value from a result set and applies a sql function if there is one to be applied.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param value The value returned from the result set or the result of a function on a value of the result set.
 * @return <code>true</code> if the value could be fetched correctly; <code>false</code>, otherwise.
 */
bool getOperandValue(Context context, SQLBooleanClauseTree* booleanClauseTree, SQLValue* value);

/** 
 * Checks if an operand is null.
 * 
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @return <code>true</code> if the operand is null and a null value is being searched or vice-versa; <code>false</code>, otherwise.
 */
bool compareNullOperands(SQLBooleanClauseTree* booleanClauseTree);

/**
 * Compares two numerical operands.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @return The evaluation of the comparison expression or -1 if an error occurs.
 */
int32 compareNumericOperands(Context context, SQLBooleanClauseTree* booleanClauseTree);

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
int32 matchStringOperands(Context context, SQLBooleanClauseTree* booleanClauseTree, bool ignoreCase, Heap heap);

/**
 * Normal comparison between two strings.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param ignoreCase Indicates if both strings are CHARS_NOCASE.
 * @param heap A heap to alocate temporary strings in the expression tree.
 * @return The evaluation of the comparison expression or -1 if an error occurs.
 */
int32 compareStringOperands(Context context, SQLBooleanClauseTree* booleanClauseTree, bool ignoreCase, Heap heap);

/**
 * Evaluates an expression tree.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param heap A heap to alocate temporary strings in the expression tree.
 * @return The value of the expression evaluation or -1 if an error occurs.
 */
int32 booleanTreeEvaluate(Context context, SQLBooleanClauseTree* booleanClauseTree, Heap heap);

/**
 * Binds the column information of the underlying table to the boolean clause tree nodes.
 *
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @return <code>false</code> If an exception is thrown; <code>true</code>, otherwise.
 * @throws DriverException If there are imcompatible types or a column cannot be bound.
 */
bool bindColumnsSQLBooleanClauseTree(Context context, SQLBooleanClauseTree* booleanClauseTree);

/**
 * Infers the operation value type, according to the left and right values involved in the operation. 
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @return <code>false</code> If an exception is thrown; <code>true</code>, otherwise.
 * @throws SQLParseException If left and right values have incompatible types  or there is a blob type in the comparison.
 */
bool inferOperationValueType(Context context, SQLBooleanClauseTree* booleanClauseTree);

/** 
 * Converts a number in the string format to its numerical representation.
 * 
 * @param context The thread context where the function is being executed.
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param type The number type.
 * @throws SQLParseException If the value to be converted is not a valid number.
 */
bool convertValue(Context context, SQLBooleanClauseTree* booleanClauseTree, int32 type);

// juliana@214_4: removes not from expression trees so that indices can be used in more situations.
/**
 * Removes the not operators from an expression tree.
 * 
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @param heap The heap to allocate a <code>SQLBooleanClauseTree</code> node. 
 * @return The expression tree without nots.
 */
SQLBooleanClauseTree* removeNots(SQLBooleanClauseTree* booleanClauseTree, Heap heap);

/**
 * Gets the field index given the hash code of the SQL function used or the field if there is no function applied.
 *
 * @param booleanClauseTree A pointer to a <code>SQLBooleanClauseTree</code> structure. 
 * @return The index of the field used by this branch of the tree.
 */
int32 getFieldIndex(SQLBooleanClauseTree* booleanClauseTree);

/**
 * Clones an expression tree of a where clause of a select prepared statement. 
 * 
 * @param booleanClauseTree The expression tree to be cloned.
 * @param destTree The old destination tree. If the nodes are the same of the tree cloned, the node is reused in the cloned tree.
 * @param heap A heap to allocate the new tree.
 * @return A clone of the where clause expression tree.
 */
SQLBooleanClauseTree* cloneTree(SQLBooleanClauseTree* booleanClauseTree, SQLBooleanClauseTree* destTree, Heap heap);

#endif
