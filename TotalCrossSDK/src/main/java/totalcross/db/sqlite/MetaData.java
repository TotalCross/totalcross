/*
 * Copyright (c) 2007 David Crawshaw <david@zentus.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package totalcross.db.sqlite;

import java.sql.SQLException;
import totalcross.sql.Connection;
import totalcross.sql.DatabaseMetaData;
import totalcross.sql.PreparedStatement;
import totalcross.sql.ResultSet;
import totalcross.sql.Statement;
import totalcross.sql.Types;
import totalcross.util.IntHashtable;
import totalcross.util.Vector;
import totalcross.util.regex.Matcher;
import totalcross.util.regex.Pattern;

class MetaData implements DatabaseMetaData {
  private SQLiteConnection conn;
  private PreparedStatement getTables, getTableTypes, getTypeInfo, getCatalogs, getSchemas, getUDTs, getColumnsTblName,
      getSuperTypes, getSuperTables, getTablePrivileges, getIndexInfo, getProcedures, getProcedureColumns,
      getAttributes, getBestRowIdentifier, getVersionColumns, getColumnPrivileges;

  /**
   * Used to save generating a new statement every call.
   */
  private PreparedStatement getGeneratedKeys;

  /**
   * Reference count.
   */
  int refCount = 1;

  /**
   * Constructor that applies the Connection object.
   * @param conn Connection object.
   */
  MetaData(SQLiteConnection conn) {
    this.conn = conn;
  }

  /**
   * @throws SQLException
   */
  void checkOpen() throws SQLException {
    if (conn == null) {
      throw new SQLException("connection closed");
    }
  }

  /**
   * @throws SQLException
   */
  void close() throws SQLException {
    if (conn == null || refCount > 0) {
      return;
    }

    try {
      if (getTables != null) {
        getTables.close();
      }
      if (getTableTypes != null) {
        getTableTypes.close();
      }
      if (getTypeInfo != null) {
        getTypeInfo.close();
      }
      if (getCatalogs != null) {
        getCatalogs.close();
      }
      if (getSchemas != null) {
        getSchemas.close();
      }
      if (getUDTs != null) {
        getUDTs.close();
      }
      if (getColumnsTblName != null) {
        getColumnsTblName.close();
      }
      if (getSuperTypes != null) {
        getSuperTypes.close();
      }
      if (getSuperTables != null) {
        getSuperTables.close();
      }
      if (getTablePrivileges != null) {
        getTablePrivileges.close();
      }
      if (getIndexInfo != null) {
        getIndexInfo.close();
      }
      if (getProcedures != null) {
        getProcedures.close();
      }
      if (getProcedureColumns != null) {
        getProcedureColumns.close();
      }
      if (getAttributes != null) {
        getAttributes.close();
      }
      if (getBestRowIdentifier != null) {
        getBestRowIdentifier.close();
      }
      if (getVersionColumns != null) {
        getVersionColumns.close();
      }
      if (getColumnPrivileges != null) {
        getColumnPrivileges.close();
      }
      if (getGeneratedKeys != null) {
        getGeneratedKeys.close();
      }

      getTables = null;
      getTableTypes = null;
      getTypeInfo = null;
      getCatalogs = null;
      getSchemas = null;
      getUDTs = null;
      getColumnsTblName = null;
      getSuperTypes = null;
      getSuperTables = null;
      getTablePrivileges = null;
      getIndexInfo = null;
      getProcedures = null;
      getProcedureColumns = null;
      getAttributes = null;
      getBestRowIdentifier = null;
      getVersionColumns = null;
      getColumnPrivileges = null;
      getGeneratedKeys = null;
    } finally {
      conn = null;
    }
  }

  /**
   * @see java.sql.DatabaseMetaData#getConnection()
   */
  @Override
  public Connection getConnection() {
    return conn;
  }

  /**
   * @see java.sql.DatabaseMetaData#getDatabaseMajorVersion()
   */
  @Override
  public int getDatabaseMajorVersion() {
    return 3;
  }

  /**
   * @see java.sql.DatabaseMetaData#getDatabaseMinorVersion()
   */
  @Override
  public int getDatabaseMinorVersion() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getDriverMajorVersion()
   */
  @Override
  public int getDriverMajorVersion() {
    return 1;
  }

  /**
   * @see java.sql.DatabaseMetaData#getDriverMinorVersion()
   */
  @Override
  public int getDriverMinorVersion() {
    return 1;
  }

  /**
   * @see java.sql.DatabaseMetaData#getJDBCMajorVersion()
   */
  @Override
  public int getJDBCMajorVersion() {
    return 2;
  }

  /**
   * @see java.sql.DatabaseMetaData#getJDBCMinorVersion()
   */
  @Override
  public int getJDBCMinorVersion() {
    return 1;
  }

  /**
   * @see java.sql.DatabaseMetaData#getDefaultTransactionIsolation()
   */
  @Override
  public int getDefaultTransactionIsolation() {
    return Connection.TRANSACTION_SERIALIZABLE;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxBinaryLiteralLength()
   */
  @Override
  public int getMaxBinaryLiteralLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxCatalogNameLength()
   */
  @Override
  public int getMaxCatalogNameLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxCharLiteralLength()
   */
  @Override
  public int getMaxCharLiteralLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnNameLength()
   */
  @Override
  public int getMaxColumnNameLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInGroupBy()
   */
  @Override
  public int getMaxColumnsInGroupBy() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInIndex()
   */
  @Override
  public int getMaxColumnsInIndex() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInOrderBy()
   */
  @Override
  public int getMaxColumnsInOrderBy() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInSelect()
   */
  @Override
  public int getMaxColumnsInSelect() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxColumnsInTable()
   */
  @Override
  public int getMaxColumnsInTable() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxConnections()
   */
  @Override
  public int getMaxConnections() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxCursorNameLength()
   */
  @Override
  public int getMaxCursorNameLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxIndexLength()
   */
  @Override
  public int getMaxIndexLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxProcedureNameLength()
   */
  @Override
  public int getMaxProcedureNameLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxRowSize()
   */
  @Override
  public int getMaxRowSize() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxSchemaNameLength()
   */
  @Override
  public int getMaxSchemaNameLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxStatementLength()
   */
  @Override
  public int getMaxStatementLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxStatements()
   */
  @Override
  public int getMaxStatements() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxTableNameLength()
   */
  @Override
  public int getMaxTableNameLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxTablesInSelect()
   */
  @Override
  public int getMaxTablesInSelect() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getMaxUserNameLength()
   */
  @Override
  public int getMaxUserNameLength() {
    return 0;
  }

  /**
   * @see java.sql.DatabaseMetaData#getResultSetHoldability()
   */
  @Override
  public int getResultSetHoldability() {
    return ResultSet.CLOSE_CURSORS_AT_COMMIT;
  }

  /**
   * @see java.sql.DatabaseMetaData#getSQLStateType()
   */
  @Override
  public int getSQLStateType() {
    return sqlStateSQL99;
  }

  /**
   * @see java.sql.DatabaseMetaData#getDatabaseProductName()
   */
  @Override
  public String getDatabaseProductName() {
    return "SQLite";
  }

  /**
   * @see java.sql.DatabaseMetaData#getDatabaseProductVersion()
   */
  @Override
  public String getDatabaseProductVersion() throws SQLException {
    return conn.libversion();
  }

  /**
   * @see java.sql.DatabaseMetaData#getDriverName()
   */
  @Override
  public String getDriverName() {
    return "SQLiteJDBC";
  }

  /**
   * @see java.sql.DatabaseMetaData#getDriverVersion()
   */
  @Override
  public String getDriverVersion() {
    return conn.getDriverVersion();
  }

  /**
   * @see java.sql.DatabaseMetaData#getExtraNameCharacters()
   */
  @Override
  public String getExtraNameCharacters() {
    return "";
  }

  /**
   * @see java.sql.DatabaseMetaData#getCatalogSeparator()
   */
  @Override
  public String getCatalogSeparator() {
    return ".";
  }

  /**
   * @see java.sql.DatabaseMetaData#getCatalogTerm()
   */
  @Override
  public String getCatalogTerm() {
    return "catalog";
  }

  /**
   * @see java.sql.DatabaseMetaData#getSchemaTerm()
   */
  @Override
  public String getSchemaTerm() {
    return "schema";
  }

  /**
   * @see java.sql.DatabaseMetaData#getProcedureTerm()
   */
  @Override
  public String getProcedureTerm() {
    return "not_implemented";
  }

  /**
   * @see java.sql.DatabaseMetaData#getSearchStringEscape()
   */
  @Override
  public String getSearchStringEscape() {
    return null;
  }

  /**
   * @see java.sql.DatabaseMetaData#getIdentifierQuoteString()
   */
  @Override
  public String getIdentifierQuoteString() {
    return " ";
  }

  /**
   * @see java.sql.DatabaseMetaData#getSQLKeywords()
   */
  @Override
  public String getSQLKeywords() {
    return "";
  }

  /**
   * @see java.sql.DatabaseMetaData#getNumericFunctions()
   */
  @Override
  public String getNumericFunctions() {
    return "";
  }

  /**
   * @see java.sql.DatabaseMetaData#getStringFunctions()
   */
  @Override
  public String getStringFunctions() {
    return "";
  }

  /**
   * @see java.sql.DatabaseMetaData#getSystemFunctions()
   */
  @Override
  public String getSystemFunctions() {
    return "";
  }

  /**
   * @see java.sql.DatabaseMetaData#getTimeDateFunctions()
   */
  @Override
  public String getTimeDateFunctions() {
    return "";
  }

  /**
   * @see java.sql.DatabaseMetaData#getURL()
   */
  @Override
  public String getURL() {
    return conn.url();
  }

  /**
   * @see java.sql.DatabaseMetaData#getUserName()
   */
  @Override
  public String getUserName() {
    return null;
  }

  /**
   * @see java.sql.DatabaseMetaData#allProceduresAreCallable()
   */
  @Override
  public boolean allProceduresAreCallable() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#allTablesAreSelectable()
   */
  @Override
  public boolean allTablesAreSelectable() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#dataDefinitionCausesTransactionCommit()
   */
  @Override
  public boolean dataDefinitionCausesTransactionCommit() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#dataDefinitionIgnoredInTransactions()
   */
  @Override
  public boolean dataDefinitionIgnoredInTransactions() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#doesMaxRowSizeIncludeBlobs()
   */
  @Override
  public boolean doesMaxRowSizeIncludeBlobs() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#deletesAreDetected(int)
   */
  @Override
  public boolean deletesAreDetected(int type) {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#insertsAreDetected(int)
   */
  @Override
  public boolean insertsAreDetected(int type) {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#isCatalogAtStart()
   */
  @Override
  public boolean isCatalogAtStart() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#locatorsUpdateCopy()
   */
  @Override
  public boolean locatorsUpdateCopy() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#nullPlusNonNullIsNull()
   */
  @Override
  public boolean nullPlusNonNullIsNull() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedAtEnd()
   */
  @Override
  public boolean nullsAreSortedAtEnd() {
    return !nullsAreSortedAtStart();
  }

  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedAtStart()
   */
  @Override
  public boolean nullsAreSortedAtStart() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedHigh()
   */
  @Override
  public boolean nullsAreSortedHigh() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#nullsAreSortedLow()
   */
  @Override
  public boolean nullsAreSortedLow() {
    return !nullsAreSortedHigh();
  }

  /**
   * @see java.sql.DatabaseMetaData#othersDeletesAreVisible(int)
   */
  @Override
  public boolean othersDeletesAreVisible(int type) {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#othersInsertsAreVisible(int)
   */
  @Override
  public boolean othersInsertsAreVisible(int type) {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#othersUpdatesAreVisible(int)
   */
  @Override
  public boolean othersUpdatesAreVisible(int type) {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#ownDeletesAreVisible(int)
   */
  @Override
  public boolean ownDeletesAreVisible(int type) {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#ownInsertsAreVisible(int)
   */
  @Override
  public boolean ownInsertsAreVisible(int type) {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#ownUpdatesAreVisible(int)
   */
  @Override
  public boolean ownUpdatesAreVisible(int type) {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#storesLowerCaseIdentifiers()
   */
  @Override
  public boolean storesLowerCaseIdentifiers() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#storesLowerCaseQuotedIdentifiers()
   */
  @Override
  public boolean storesLowerCaseQuotedIdentifiers() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#storesMixedCaseIdentifiers()
   */
  @Override
  public boolean storesMixedCaseIdentifiers() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#storesMixedCaseQuotedIdentifiers()
   */
  @Override
  public boolean storesMixedCaseQuotedIdentifiers() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#storesUpperCaseIdentifiers()
   */
  @Override
  public boolean storesUpperCaseIdentifiers() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#storesUpperCaseQuotedIdentifiers()
   */
  @Override
  public boolean storesUpperCaseQuotedIdentifiers() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsAlterTableWithAddColumn()
   */
  @Override
  public boolean supportsAlterTableWithAddColumn() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsAlterTableWithDropColumn()
   */
  @Override
  public boolean supportsAlterTableWithDropColumn() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsANSI92EntryLevelSQL()
   */
  @Override
  public boolean supportsANSI92EntryLevelSQL() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsANSI92FullSQL()
   */
  @Override
  public boolean supportsANSI92FullSQL() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsANSI92IntermediateSQL()
   */
  @Override
  public boolean supportsANSI92IntermediateSQL() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsBatchUpdates()
   */
  @Override
  public boolean supportsBatchUpdates() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInDataManipulation()
   */
  @Override
  public boolean supportsCatalogsInDataManipulation() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInIndexDefinitions()
   */
  @Override
  public boolean supportsCatalogsInIndexDefinitions() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInPrivilegeDefinitions()
   */
  @Override
  public boolean supportsCatalogsInPrivilegeDefinitions() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInProcedureCalls()
   */
  @Override
  public boolean supportsCatalogsInProcedureCalls() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCatalogsInTableDefinitions()
   */
  @Override
  public boolean supportsCatalogsInTableDefinitions() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsColumnAliasing()
   */
  @Override
  public boolean supportsColumnAliasing() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsConvert()
   */
  @Override
  public boolean supportsConvert() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsConvert(int, int)
   */
  @Override
  public boolean supportsConvert(int fromType, int toType) {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCorrelatedSubqueries()
   */
  @Override
  public boolean supportsCorrelatedSubqueries() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsDataDefinitionAndDataManipulationTransactions()
   */
  @Override
  public boolean supportsDataDefinitionAndDataManipulationTransactions() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsDataManipulationTransactionsOnly()
   */
  @Override
  public boolean supportsDataManipulationTransactionsOnly() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsDifferentTableCorrelationNames()
   */
  @Override
  public boolean supportsDifferentTableCorrelationNames() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsExpressionsInOrderBy()
   */
  @Override
  public boolean supportsExpressionsInOrderBy() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMinimumSQLGrammar()
   */
  @Override
  public boolean supportsMinimumSQLGrammar() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsCoreSQLGrammar()
   */
  @Override
  public boolean supportsCoreSQLGrammar() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsExtendedSQLGrammar()
   */
  @Override
  public boolean supportsExtendedSQLGrammar() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsLimitedOuterJoins()
   */
  @Override
  public boolean supportsLimitedOuterJoins() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsFullOuterJoins()
   */
  @Override
  public boolean supportsFullOuterJoins() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsGetGeneratedKeys()
   */
  @Override
  public boolean supportsGetGeneratedKeys() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsGroupBy()
   */
  @Override
  public boolean supportsGroupBy() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsGroupByBeyondSelect()
   */
  @Override
  public boolean supportsGroupByBeyondSelect() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsGroupByUnrelated()
   */
  @Override
  public boolean supportsGroupByUnrelated() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsIntegrityEnhancementFacility()
   */
  @Override
  public boolean supportsIntegrityEnhancementFacility() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsLikeEscapeClause()
   */
  @Override
  public boolean supportsLikeEscapeClause() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMixedCaseIdentifiers()
   */
  @Override
  public boolean supportsMixedCaseIdentifiers() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMixedCaseQuotedIdentifiers()
   */
  @Override
  public boolean supportsMixedCaseQuotedIdentifiers() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMultipleOpenResults()
   */
  @Override
  public boolean supportsMultipleOpenResults() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMultipleResultSets()
   */
  @Override
  public boolean supportsMultipleResultSets() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsMultipleTransactions()
   */
  @Override
  public boolean supportsMultipleTransactions() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsNamedParameters()
   */
  @Override
  public boolean supportsNamedParameters() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsNonNullableColumns()
   */
  @Override
  public boolean supportsNonNullableColumns() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossCommit()
   */
  @Override
  public boolean supportsOpenCursorsAcrossCommit() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOpenCursorsAcrossRollback()
   */
  @Override
  public boolean supportsOpenCursorsAcrossRollback() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossCommit()
   */
  @Override
  public boolean supportsOpenStatementsAcrossCommit() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOpenStatementsAcrossRollback()
   */
  @Override
  public boolean supportsOpenStatementsAcrossRollback() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOrderByUnrelated()
   */
  @Override
  public boolean supportsOrderByUnrelated() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsOuterJoins()
   */
  @Override
  public boolean supportsOuterJoins() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsPositionedDelete()
   */
  @Override
  public boolean supportsPositionedDelete() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsPositionedUpdate()
   */
  @Override
  public boolean supportsPositionedUpdate() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsResultSetConcurrency(int, int)
   */
  @Override
  public boolean supportsResultSetConcurrency(int t, int c) {
    return t == ResultSet.TYPE_FORWARD_ONLY && c == ResultSet.CONCUR_READ_ONLY;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsResultSetHoldability(int)
   */
  @Override
  public boolean supportsResultSetHoldability(int h) {
    return h == ResultSet.CLOSE_CURSORS_AT_COMMIT;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsResultSetType(int)
   */
  @Override
  public boolean supportsResultSetType(int t) {
    return t == ResultSet.TYPE_FORWARD_ONLY;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSavepoints()
   */
  @Override
  public boolean supportsSavepoints() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInDataManipulation()
   */
  @Override
  public boolean supportsSchemasInDataManipulation() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInIndexDefinitions()
   */
  @Override
  public boolean supportsSchemasInIndexDefinitions() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInPrivilegeDefinitions()
   */
  @Override
  public boolean supportsSchemasInPrivilegeDefinitions() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInProcedureCalls()
   */
  @Override
  public boolean supportsSchemasInProcedureCalls() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSchemasInTableDefinitions()
   */
  @Override
  public boolean supportsSchemasInTableDefinitions() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSelectForUpdate()
   */
  @Override
  public boolean supportsSelectForUpdate() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsStatementPooling()
   */
  @Override
  public boolean supportsStatementPooling() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsStoredProcedures()
   */
  @Override
  public boolean supportsStoredProcedures() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInComparisons()
   */
  @Override
  public boolean supportsSubqueriesInComparisons() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInExists()
   */
  @Override
  public boolean supportsSubqueriesInExists() {
    return true;
  } // TODO: check

  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInIns()
   */
  @Override
  public boolean supportsSubqueriesInIns() {
    return true;
  } // TODO: check

  /**
   * @see java.sql.DatabaseMetaData#supportsSubqueriesInQuantifieds()
   */
  @Override
  public boolean supportsSubqueriesInQuantifieds() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsTableCorrelationNames()
   */
  @Override
  public boolean supportsTableCorrelationNames() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsTransactionIsolationLevel(int)
   */
  @Override
  public boolean supportsTransactionIsolationLevel(int level) {
    return level == Connection.TRANSACTION_SERIALIZABLE;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsTransactions()
   */
  @Override
  public boolean supportsTransactions() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsUnion()
   */
  @Override
  public boolean supportsUnion() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#supportsUnionAll()
   */
  @Override
  public boolean supportsUnionAll() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#updatesAreDetected(int)
   */
  @Override
  public boolean updatesAreDetected(int type) {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#usesLocalFilePerTable()
   */
  @Override
  public boolean usesLocalFilePerTable() {
    return false;
  }

  /**
   * @see java.sql.DatabaseMetaData#usesLocalFiles()
   */
  @Override
  public boolean usesLocalFiles() {
    return true;
  }

  /**
   * @see java.sql.DatabaseMetaData#isReadOnly()
   */
  @Override
  public boolean isReadOnly() throws SQLException {
    return conn.isReadOnly();
  }

  /**
   * @see java.sql.DatabaseMetaData#getAttributes(java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getAttributes(String c, String s, String t, String a) throws SQLException {
    if (getAttributes == null) {
      getAttributes = conn.prepareStatement(
          "select null as TYPE_CAT, null as TYPE_SCHEM, " + "null as TYPE_NAME, null as ATTR_NAME, null as DATA_TYPE, "
              + "null as ATTR_TYPE_NAME, null as ATTR_SIZE, null as DECIMAL_DIGITS, "
              + "null as NUM_PREC_RADIX, null as NULLABLE, null as REMARKS, null as ATTR_DEF, "
              + "null as SQL_DATA_TYPE, null as SQL_DATETIME_SUB, null as CHAR_OCTET_LENGTH, "
              + "null as ORDINAL_POSITION, null as IS_NULLABLE, null as SCOPE_CATALOG, "
              + "null as SCOPE_SCHEMA, null as SCOPE_TABLE, null as SOURCE_DATA_TYPE limit 0;");
    }

    return getAttributes.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getBestRowIdentifier(java.lang.String, java.lang.String,
   *      java.lang.String, int, boolean)
   */
  @Override
  public ResultSet getBestRowIdentifier(String c, String s, String t, int scope, boolean n) throws SQLException {
    if (getBestRowIdentifier == null) {
      getBestRowIdentifier = conn.prepareStatement(
          "select null as SCOPE, null as COLUMN_NAME, " + "null as DATA_TYPE, null as TYPE_NAME, null as COLUMN_SIZE, "
              + "null as BUFFER_LENGTH, null as DECIMAL_DIGITS, null as PSEUDO_COLUMN limit 0;");
    }

    return getBestRowIdentifier.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getColumnPrivileges(java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getColumnPrivileges(String c, String s, String t, String colPat) throws SQLException {
    if (getColumnPrivileges == null) {
      getColumnPrivileges = conn.prepareStatement("select null as TABLE_CAT, null as TABLE_SCHEM, "
          + "null as TABLE_NAME, null as COLUMN_NAME, null as GRANTOR, null as GRANTEE, "
          + "null as PRIVILEGE, null as IS_GRANTABLE limit 0;");
    }

    return getColumnPrivileges.executeQuery();
  }

  // Column type patterns
  protected static final Pattern TYPE_INTEGER = Pattern.compile(".*(INT|BOOL).*");
  protected static final Pattern TYPE_VARCHAR = Pattern.compile(".*(CHAR|CLOB|TEXT|BLOB).*");
  protected static final Pattern TYPE_FLOAT = Pattern.compile(".*(REAL|FLOA|DOUB|DEC|NUM).*");

  /**
   * @see java.sql.DatabaseMetaData#getColumns(java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getColumns(String c, String s, String tblNamePattern, String colNamePattern) throws SQLException {
    Statement stat = conn.createStatement();
    ResultSet rs;
    StringBuffer sql = new StringBuffer(700);

    checkOpen();

    if (getColumnsTblName == null) {
      getColumnsTblName = conn.prepareStatement("select tbl_name from sqlite_master where tbl_name like ?;");
    }

    // determine exact table name
    getColumnsTblName.setString(1, tblNamePattern);
    rs = getColumnsTblName.executeQuery();

    if (rs.next()) {
      tblNamePattern = rs.getString(1);
      rs.close();
      // the command "pragma table_info('tablename')" does not embed
      // like a normal select statement so we must extract the information
      // and then build a resultset from unioned select statements
      rs = stat.executeQuery("pragma table_info ('" + escape(tblNamePattern) + "');");
    }

    sql.append("select null as TABLE_CAT, null as TABLE_SCHEM, '").append(escape(tblNamePattern))
        .append("' as TABLE_NAME, ")
        .append("cn as COLUMN_NAME, ct as DATA_TYPE, tn as TYPE_NAME, 2000000000 as COLUMN_SIZE, ")
        .append("2000000000 as BUFFER_LENGTH, 10   as DECIMAL_DIGITS, 10   as NUM_PREC_RADIX, ")
        .append("colnullable as NULLABLE, null as REMARKS, colDefault as COLUMN_DEF, ")
        .append("0    as SQL_DATA_TYPE, 0    as SQL_DATETIME_SUB, 2000000000 as CHAR_OCTET_LENGTH, ")
        .append("ordpos as ORDINAL_POSITION, (case colnullable when 0 then 'NO' when 1 then 'YES' else '' end)")
        .append("    as IS_NULLABLE, null as SCOPE_CATLOG, null as SCOPE_SCHEMA, ")
        .append("null as SCOPE_TABLE, null as SOURCE_DATA_TYPE from (");

    boolean colFound = false;

    for (int i = 0; rs.next(); i++) {
      String colName = rs.getString(2);
      String colType = rs.getString(3);
      String colNotNull = rs.getString(4);
      String colDefault = rs.getString(5);

      int colNullable = 2;

      if (colNotNull != null) {
        colNullable = colNotNull.equals("0") ? 1 : 0;
      }

      if (colFound) {
        sql.append(" union all ");
      }

      colFound = true;

      /*
       * improved column types
       * ref http://www.sqlite.org/datatype3.html - 2.1 Determination Of Column
            Affinity
       * plus some degree of artistic-license applied
       */
      colType = colType == null ? "TEXT" : colType.toUpperCase();
      int colJavaType = -1;
      // rule #1 + boolean
      if (TYPE_INTEGER.matcher(colType).find()) {
        colJavaType = Types.INTEGER;
      } else if (TYPE_VARCHAR.matcher(colType).find()) {
        colJavaType = Types.VARCHAR;
      } else if (TYPE_FLOAT.matcher(colType).find()) {
        colJavaType = Types.FLOAT;
      } else {
        // catch-all
        colJavaType = Types.VARCHAR;
      }

      sql.append("select ").append(i).append(" as ordpos, ").append(colNullable).append(" as colnullable, '")
          .append(colJavaType).append("' as ct, '").append(escape(colName)).append("' as cn, '").append(escape(colType))
          .append("' as tn, ").append(quote(colDefault == null ? null : escape(colDefault))).append(" as colDefault");

      if (colNamePattern != null) {
        sql.append(" where upper(cn) like upper('").append(escape(colNamePattern)).append("')");
      }
    }

    rs.close();

    if (colFound) {
      sql.append(") order by TABLE_SCHEM, TABLE_NAME, ORDINAL_POSITION;");
    } else {
      sql.append(
          "select null as ordpos, null as colnullable, null as ct, null as cn, null as tn, null as colDefault) limit 0;");
    }

    return ((Stmt) stat).executeQuery(sql.toString(), true);
  }

  /**
   * @see java.sql.DatabaseMetaData#getCrossReference(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getCrossReference(String pc, String ps, String pt, String fc, String fs, String ft)
      throws SQLException {
    if (pt == null) {
      return getExportedKeys(fc, fs, ft);
    }

    if (ft == null) {
      return getImportedKeys(pc, ps, pt);
    }

    StringBuffer query = new StringBuffer();
    query.append("select ").append(quote(pc)).append(" as PKTABLE_CAT, ").append(quote(ps))
        .append(" as PKTABLE_SCHEM, ").append(quote(pt)).append(" as PKTABLE_NAME, ").append("'' as PKCOLUMN_NAME, ")
        .append(quote(fc)).append(" as FKTABLE_CAT, ").append(quote(fs)).append(" as FKTABLE_SCHEM, ").append(quote(ft))
        .append(" as FKTABLE_NAME, ")
        .append(
            "'' as FKCOLUMN_NAME, -1 as KEY_SEQ, 3 as UPDATE_RULE, 3 as DELETE_RULE, '' as FK_NAME, '' as PK_NAME, ")
        .append(importedKeyInitiallyDeferred).append(" as DEFERRABILITY limit 0 ");

    return ((Stmt) conn.createStatement()).executeQuery(query.toString(), true);
  }

  /**
   * @see java.sql.DatabaseMetaData#getSchemas()
   */
  @Override
  public ResultSet getSchemas() throws SQLException {
    if (getSchemas == null) {
      getSchemas = conn.prepareStatement("select null as TABLE_SCHEM, null as TABLE_CATALOG limit 0;");
    }

    return getSchemas.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getCatalogs()
   */
  @Override
  public ResultSet getCatalogs() throws SQLException {
    if (getCatalogs == null) {
      getCatalogs = conn.prepareStatement("select null as TABLE_CAT limit 0;");
    }

    return getCatalogs.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getPrimaryKeys(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public ResultSet getPrimaryKeys(String c, String s, String table) throws SQLException {
    PrimaryKeyFinder pkFinder = new PrimaryKeyFinder(table);
    String[] columns = pkFinder.getColumns();

    Statement stat = conn.createStatement();
    StringBuffer sql = new StringBuffer(512);
    sql.append("select null as TABLE_CAT, null as TABLE_SCHEM, '").append(escape(table))
        .append("' as TABLE_NAME, cn as COLUMN_NAME, ks as KEY_SEQ, pk as PK_NAME from (");

    if (columns == null) {
      sql.append("select null as cn, null as pk, 0 as ks) limit 0;");

      return ((Stmt) stat).executeQuery(sql.toString(), true);
    }

    String pkName = pkFinder.getName();

    for (int i = 0; i < columns.length; i++) {
      if (i > 0) {
        sql.append(" union ");
      }
      sql.append("select ").append(pkName).append(" as pk, '").append(escape(columns[i].trim())).append("' as cn, ")
          .append(i).append(" as ks");
    }

    return ((Stmt) stat).executeQuery(sql.append(") order by cn;").toString(), true);
  }

  /**
   * Adds SQL string quotes to the given string.
   * @param tableName The string to quote.
   * @return The quoted string.
   */
  private static String quote(String tableName) {
    if (tableName == null) {
      return "null";
    } else {
      return "'" + tableName + "'";
    }
  }

  private final static IntHashtable RULE_MAP = new IntHashtable(5);

  static {
    RULE_MAP.put("NO ACTION", importedKeyNoAction);
    RULE_MAP.put("CASCADE", importedKeyCascade);
    RULE_MAP.put("RESTRICT", importedKeyRestrict);
    RULE_MAP.put("SET NULL", importedKeySetNull);
    RULE_MAP.put("SET DEFAULT", importedKeySetDefault);
  }

  /**
   * Pattern used to extract a named primary key.
   */
  protected final static Pattern FK_NAMED_PATTERN = Pattern.compile(".* constraint +(.*?) +foreign +key *\\((.*?)\\).*",
      Pattern.IGNORE_CASE);

  /**
   * @see java.sql.DatabaseMetaData#getExportedKeys(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
    PrimaryKeyFinder pkFinder = new PrimaryKeyFinder(table);
    String[] pkColumns = pkFinder.getColumns();
    Stmt stat = (Stmt) conn.createStatement();

    catalog = (catalog != null) ? quote(catalog) : null;
    schema = (schema != null) ? quote(schema) : null;

    StringBuffer exportedKeysQuery = new StringBuffer(512);

    int count = 0;
    if (pkColumns != null) {
      // retrieve table list
      ResultSet rs = stat.executeQuery("select name from sqlite_master where type = 'table'");
      Vector tableList = new Vector();

      while (rs.next()) {
        tableList.addElement(rs.getString(1));
      }

      rs.close();

      ResultSet fk = null;
      String target = table.toLowerCase();
      // find imported keys for each table
      for (int i = 0, n = tableList.size(); i < n; i++) {
        String tbl = (String) tableList.items[i];
        try {
          fk = stat.executeQuery("pragma foreign_key_list('" + escape(tbl) + "')");
        } catch (SQLException e) {
          if (e.getErrorCode() == Codes.SQLITE_DONE) {
            continue; // expected if table has no foreign keys
          }

          throw e;
        }

        Stmt stat2 = null;
        try {
          stat2 = (Stmt) conn.createStatement();

          while (fk.next()) {
            int keySeq = fk.getInt(2) + 1;
            String PKTabName = fk.getString(3).toLowerCase();

            if (PKTabName == null || !PKTabName.equals(target)) {
              continue;
            }

            String PKColName = fk.getString(5);
            PKColName = (PKColName == null) ? pkColumns[0] : PKColName.toLowerCase();

            exportedKeysQuery.append(count > 0 ? " union all select " : "select ").append(keySeq)
                .append(" as ks, lower('").append(escape(tbl)).append("') as fkt, lower('")
                .append(escape(fk.getString(4))).append("') as fcn, '").append(escape(PKColName)).append("' as pcn, ")
                .append(RULE_MAP.get(fk.getString(6).hashCode(), -1)).append(" as ur, ")
                .append(RULE_MAP.get(fk.getString(7).hashCode(), -1)).append(" as dr, ");

            rs = stat2
                .executeQuery("select sql from sqlite_master where" + " lower(name) = lower('" + escape(tbl) + "')");

            if (rs.next()) {
              Matcher matcher = FK_NAMED_PATTERN.matcher(rs.getString(1));

              if (matcher.find()) {
                exportedKeysQuery.append("'").append(escape(matcher.group(1).toLowerCase())).append("' as fkn");
              } else {
                exportedKeysQuery.append("'' as fkn");
              }
            }

            rs.close();
            count++;
          }
        } finally {
          try {
            if (rs != null) {
              rs.close();
            }
          } catch (SQLException e) {
          }
          try {
            if (stat2 != null) {
              stat2.close();
            }
          } catch (SQLException e) {
          }
          try {
            if (fk != null) {
              fk.close();
            }
          } catch (SQLException e) {
          }
        }
      }
    }

    boolean hasImportedKey = (count > 0);
    StringBuffer sql = new StringBuffer(512);
    sql.append("select ").append(catalog).append(" as PKTABLE_CAT, ").append(schema).append(" as PKTABLE_SCHEM, ")
        .append(quote(table)).append(" as PKTABLE_NAME, ").append(hasImportedKey ? "pcn" : "''")
        .append(" as PKCOLUMN_NAME, ").append(catalog).append(" as FKTABLE_CAT, ").append(schema)
        .append(" as FKTABLE_SCHEM, ").append(hasImportedKey ? "fkt" : "''").append(" as FKTABLE_NAME, ")
        .append(hasImportedKey ? "fcn" : "''").append(" as FKCOLUMN_NAME, ").append(hasImportedKey ? "ks" : "-1")
        .append(" as KEY_SEQ, ").append(hasImportedKey ? "ur" : "3").append(" as UPDATE_RULE, ")
        .append(hasImportedKey ? "dr" : "3").append(" as DELETE_RULE, ").append(hasImportedKey ? "fkn" : "''")
        .append(" as FK_NAME, ").append(pkFinder.getName() != null ? pkFinder.getName() : "''").append(" as PK_NAME, ")
        .append(importedKeyInitiallyDeferred) // FIXME: Check for pragma foreign_keys = true ?
        .append(" as DEFERRABILITY ");

    if (hasImportedKey) {
      sql.append("from (").append(exportedKeysQuery).append(") order by fkt");
    } else {
      sql.append("limit 0");
    }

    return ((Stmt) stat).executeQuery(sql.toString(), true);
  }

  /**
   * @see java.sql.DatabaseMetaData#getImportedKeys(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
    ResultSet rs = null;
    Statement stat = conn.createStatement();
    StringBuffer sql = new StringBuffer(700);

    sql.append("select ").append(quote(catalog)).append(" as PKTABLE_CAT, ").append(quote(schema))
        .append(" as PKTABLE_SCHEM, ").append("ptn as PKTABLE_NAME, pcn as PKCOLUMN_NAME, ").append(quote(catalog))
        .append(" as FKTABLE_CAT, ").append(quote(schema)).append(" as FKTABLE_SCHEM, ").append(quote(table))
        .append(" as FKTABLE_NAME, ")
        .append(
            "fcn as FKCOLUMN_NAME, ks as KEY_SEQ, ur as UPDATE_RULE, dr as DELETE_RULE, '' as FK_NAME, '' as PK_NAME, ")
        .append(importedKeyInitiallyDeferred).append(" as DEFERRABILITY from (");

    // Use a try catch block to avoid "query does not return ResultSet" error
    try {
      rs = stat.executeQuery("pragma foreign_key_list('" + escape(table) + "');");
    } catch (SQLException e) {
      sql.append("select -1 as ks, '' as ptn, '' as fcn, '' as pcn, ").append(importedKeyNoAction).append(" as ur, ")
          .append(importedKeyNoAction).append(" as dr) limit 0;");

      return ((Stmt) stat).executeQuery(sql.toString(), true);
    }

    for (int i = 0; rs.next(); i++) {
      int keySeq = rs.getInt(2) + 1;
      String PKTabName = rs.getString(3);
      String FKColName = rs.getString(4);
      String PKColName = rs.getString(5);

      if (PKColName == null) {
        PKColName = new PrimaryKeyFinder(PKTabName).getColumns()[0];
      }

      String updateRule = rs.getString(6);
      String deleteRule = rs.getString(7);

      if (i > 0) {
        sql.append(" union all ");
      }

      sql.append("select ").append(keySeq).append(" as ks,").append("'").append(escape(PKTabName)).append("' as ptn, '")
          .append(escape(FKColName)).append("' as fcn, '").append(escape(PKColName)).append("' as pcn,")
          .append("case '").append(escape(updateRule)).append("'").append(" when 'NO ACTION' then ")
          .append(importedKeyNoAction).append(" when 'CASCADE' then ").append(importedKeyCascade)
          .append(" when 'RESTRICT' then ").append(importedKeyRestrict).append(" when 'SET NULL' then ")
          .append(importedKeySetNull).append(" when 'SET DEFAULT' then ").append(importedKeySetDefault)
          .append(" end as ur, ").append("case '").append(escape(deleteRule)).append("'")
          .append(" when 'NO ACTION' then ").append(importedKeyNoAction).append(" when 'CASCADE' then ")
          .append(importedKeyCascade).append(" when 'RESTRICT' then ").append(importedKeyRestrict)
          .append(" when 'SET NULL' then ").append(importedKeySetNull).append(" when 'SET DEFAULT' then ")
          .append(importedKeySetDefault).append(" end as dr");
    }
    rs.close();

    return ((Stmt) stat).executeQuery(sql.append(");").toString(), true);
  }

  /**
   * @see java.sql.DatabaseMetaData#getIndexInfo(java.lang.String, java.lang.String,
   *      java.lang.String, boolean, boolean)
   */
  @Override
  public ResultSet getIndexInfo(String c, String s, String t, boolean u, boolean approximate) throws SQLException {
    ResultSet rs = null;
    Statement stat = conn.createStatement();
    StringBuffer sql = new StringBuffer(500);

    sql.append("select null as TABLE_CAT, null as TABLE_SCHEM, '").append(escape(t))
        .append("' as TABLE_NAME, un as NON_UNIQUE, null as INDEX_QUALIFIER, n as INDEX_NAME, ").append(tableIndexOther)
        .append(" as TYPE, op as ORDINAL_POSITION, ").append(
            "cn as COLUMN_NAME, null as ASC_OR_DESC, 0 as CARDINALITY, 0 as PAGES, null as FILTER_CONDITION from (");

    // Use a try catch block to avoid "query does not return ResultSet" error
    try {
      rs = stat.executeQuery("pragma index_list('" + escape(t) + "');");
    } catch (SQLException e) {
      sql.append("select null as un, null as n, null as op, null as cn) limit 0;");

      return ((Stmt) stat).executeQuery(sql.toString(), true);
    }

    Vector indexList = new Vector(20);
    while (rs.next()) {
      indexList.addElement(new Object[] { rs.getString(2), new Integer(rs.getInt(3)) });
    }
    rs.close();

    int i = 0;
    Object[] currentIndex;

    for (int j = 0, n = indexList.size(); j < n; j++) {
      currentIndex = (Object[]) indexList.items[i];
      String indexName = (String) currentIndex[0];
      int indexValue = ((Integer) currentIndex[1]).intValue();
      rs = stat.executeQuery("pragma index_info('" + escape(indexName) + "');");

      while (rs.next()) {
        if (i++ > 1) {
          sql.append(" union all ");
        }

        sql.append("select ").append(1 - indexValue).append(" as un,'").append(escape(indexName)).append("' as n,")
            .append(rs.getInt(1) + 1).append(" as op,'").append(escape(rs.getString(3))).append("' as cn");
      }

      rs.close();
    }

    return ((Stmt) stat).executeQuery(sql.append(");").toString(), true);
  }

  /**
   * @see java.sql.DatabaseMetaData#getProcedureColumns(java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String)
   */
  @Override
  public ResultSet getProcedureColumns(String c, String s, String p, String colPat) throws SQLException {
    if (getProcedures == null) {
      getProcedureColumns = conn.prepareStatement(
          "select null as PROCEDURE_CAT, " + "null as PROCEDURE_SCHEM, null as PROCEDURE_NAME, null as COLUMN_NAME, "
              + "null as COLUMN_TYPE, null as DATA_TYPE, null as TYPE_NAME, null as PRECISION, "
              + "null as LENGTH, null as SCALE, null as RADIX, null as NULLABLE, " + "null as REMARKS limit 0;");
    }
    return getProcedureColumns.executeQuery();

  }

  /**
   * @see java.sql.DatabaseMetaData#getProcedures(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public ResultSet getProcedures(String c, String s, String p) throws SQLException {
    if (getProcedures == null) {
      getProcedures = conn.prepareStatement("select null as PROCEDURE_CAT, null as PROCEDURE_SCHEM, "
          + "null as PROCEDURE_NAME, null as UNDEF1, null as UNDEF2, null as UNDEF3, "
          + "null as REMARKS, null as PROCEDURE_TYPE limit 0;");
    }
    return getProcedures.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getSuperTables(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public ResultSet getSuperTables(String c, String s, String t) throws SQLException {
    if (getSuperTables == null) {
      getSuperTables = conn.prepareStatement(
          "select null as TABLE_CAT, null as TABLE_SCHEM, " + "null as TABLE_NAME, null as SUPERTABLE_NAME limit 0;");
    }
    return getSuperTables.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getSuperTypes(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public ResultSet getSuperTypes(String c, String s, String t) throws SQLException {
    if (getSuperTypes == null) {
      getSuperTypes = conn.prepareStatement("select null as TYPE_CAT, null as TYPE_SCHEM, "
          + "null as TYPE_NAME, null as SUPERTYPE_CAT, null as SUPERTYPE_SCHEM, " + "null as SUPERTYPE_NAME limit 0;");
    }
    return getSuperTypes.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getTablePrivileges(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public ResultSet getTablePrivileges(String c, String s, String t) throws SQLException {
    if (getTablePrivileges == null) {
      getTablePrivileges = conn.prepareStatement(
          "select  null as TABLE_CAT, " + "null as TABLE_SCHEM, null as TABLE_NAME, null as GRANTOR, null "
              + "GRANTEE,  null as PRIVILEGE, null as IS_GRANTABLE limit 0;");
    }
    return getTablePrivileges.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getTables(java.lang.String, java.lang.String,
   *      java.lang.String, java.lang.String[])
   */
  @Override
  public ResultSet getTables(String c, String s, String tblNamePattern, String types[]) throws SQLException {
    checkOpen();

    tblNamePattern = (tblNamePattern == null || "".equals(tblNamePattern)) ? "%" : escape(tblNamePattern);

    StringBuffer sql = new StringBuffer();
    sql.append("select null as TABLE_CAT, null as TABLE_SCHEM, name as TABLE_NAME,")
        .append(" upper(type) as TABLE_TYPE, null as REMARKS, null as TYPE_CAT, null as TYPE_SCHEM,")
        .append(" null as TYPE_NAME, null as SELF_REFERENCING_COL_NAME, null as REF_GENERATION")
        .append(" from (select name, type from sqlite_master union all select name, type from sqlite_temp_master)")
        .append(" where TABLE_NAME like '").append(tblNamePattern).append("' and TABLE_TYPE in (");

    if (types == null || types.length == 0) {
      sql.append("'TABLE','VIEW'");
    } else {
      sql.append("'").append(types[0].toUpperCase()).append("'");

      for (int i = 1; i < types.length; i++) {
        sql.append(",'").append(types[i].toUpperCase()).append("'");
      }
    }

    sql.append(") order by TABLE_TYPE, TABLE_NAME;");

    return ((Stmt) conn.createStatement()).executeQuery(sql.toString(), true);
  }

  /**
   * @see java.sql.DatabaseMetaData#getTableTypes()
   */
  @Override
  public ResultSet getTableTypes() throws SQLException {
    checkOpen();
    if (getTableTypes == null) {
      getTableTypes = conn.prepareStatement("select 'TABLE' as TABLE_TYPE " + "union select 'VIEW' as TABLE_TYPE;");
    }
    getTableTypes.clearParameters();
    return getTableTypes.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getTypeInfo()
   */
  @Override
  public ResultSet getTypeInfo() throws SQLException {
    if (getTypeInfo == null) {
      getTypeInfo = conn.prepareStatement("select " + "tn as TYPE_NAME, " + "dt as DATA_TYPE, " + "0 as PRECISION, "
          + "null as LITERAL_PREFIX, " + "null as LITERAL_SUFFIX, " + "null as CREATE_PARAMS, " + typeNullable
          + " as NULLABLE, " + "1 as CASE_SENSITIVE, " + typeSearchable + " as SEARCHABLE, "
          + "0 as UNSIGNED_ATTRIBUTE, " + "0 as FIXED_PREC_SCALE, " + "0 as AUTO_INCREMENT, "
          + "null as LOCAL_TYPE_NAME, " + "0 as MINIMUM_SCALE, " + "0 as MAXIMUM_SCALE, " + "0 as SQL_DATA_TYPE, "
          + "0 as SQL_DATETIME_SUB, " + "10 as NUM_PREC_RADIX from (" + "    select 'BLOB' as tn, " + Types.BLOB
          + " as dt union" + "    select 'NULL' as tn, " + Types.NULL + " as dt union" + "    select 'REAL' as tn, "
          + Types.REAL + " as dt union" + "    select 'TEXT' as tn, " + Types.VARCHAR + " as dt union"
          + "    select 'INTEGER' as tn, " + Types.INTEGER + " as dt" + ") order by TYPE_NAME;");
    }

    getTypeInfo.clearParameters();
    return getTypeInfo.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getUDTs(java.lang.String, java.lang.String, java.lang.String,
   *      int[])
   */
  @Override
  public ResultSet getUDTs(String c, String s, String t, int[] types) throws SQLException {
    if (getUDTs == null) {
      getUDTs = conn.prepareStatement("select  null as TYPE_CAT, null as TYPE_SCHEM, "
          + "null as TYPE_NAME,  null as CLASS_NAME,  null as DATA_TYPE, null as REMARKS, " + "null as BASE_TYPE "
          + "limit 0;");
    }

    getUDTs.clearParameters();
    return getUDTs.executeQuery();
  }

  /**
   * @see java.sql.DatabaseMetaData#getVersionColumns(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public ResultSet getVersionColumns(String c, String s, String t) throws SQLException {
    if (getVersionColumns == null) {
      getVersionColumns = conn.prepareStatement(
          "select null as SCOPE, null as COLUMN_NAME, " + "null as DATA_TYPE, null as TYPE_NAME, null as COLUMN_SIZE, "
              + "null as BUFFER_LENGTH, null as DECIMAL_DIGITS, null as PSEUDO_COLUMN limit 0;");
    }
    return getVersionColumns.executeQuery();
  }

  /**
   * @return Generated row id of the last INSERT command.
   * @throws SQLException
   */
  ResultSet getGeneratedKeys() throws SQLException {
    if (getGeneratedKeys == null) {
      getGeneratedKeys = conn.prepareStatement("select last_insert_rowid();");
    }

    return getGeneratedKeys.executeQuery();
  }

  /**
   * Applies SQL escapes for special characters in a given string.
   * @param val The string to escape.
   * @return The SQL escaped string.
   */
  private String escape(final String val) {
    // TODO: this function is ugly, pass this work off to SQLite, then we
    //       don't have to worry about Unicode 4, other characters needing
    //       escaping, etc.
    int len = val.length();
    StringBuffer buf = new StringBuffer(len);
    for (int i = 0; i < len; i++) {
      if (val.charAt(i) == '\'') {
        buf.append('\'');
      }
      buf.append(val.charAt(i));
    }
    return buf.toString();
  }

  /** Not implemented yet. */
  /*    public Struct createStruct(String t, Object[] attr) throws SQLException {
        throw new SQLException("Not yet implemented by SQLite JDBC driver");
    }
   */
  /** Not implemented yet. */
  public ResultSet getFunctionColumns(String a, String b, String c, String d) throws SQLException {
    throw new SQLException("Not yet implemented by SQLite JDBC driver");
  }

  // inner classes

  /**
   * Pattern used to extract column order for an unnamed primary key.
   */
  protected final static Pattern PK_UNNAMED_PATTERN = Pattern.compile(".* primary +key *\\((.*?,+.*?)\\).*",
      Pattern.IGNORE_CASE);

  /**
   * Pattern used to extract a named primary key.
   */
  protected final static Pattern PK_NAMED_PATTERN = Pattern.compile(".* constraint +(.*?) +primary +key *\\((.*?)\\).*",
      Pattern.IGNORE_CASE);

  /**
   * Parses the sqlite_master table for a table's primary key
   */
  class PrimaryKeyFinder {
    /** The table name. */
    String table;

    /** The primary key name. */
    String pkName = null;

    /** The column(s) for the primary key. */
    String pkColumns[] = null;

    /**
     * Constructor.
     * @param table The table for which to get find a primary key.
     * @throws SQLException
     */
    public PrimaryKeyFinder(String table) throws SQLException {
      this.table = table;

      if (table == null || table.trim().length() == 0) {
        throw new SQLException("Invalid table name: '" + this.table + "'");
      }

      Statement stat = null;
      ResultSet rs = null;

      try {
        stat = conn.createStatement();
        // read create SQL script for table
        rs = stat.executeQuery(
            "select sql from sqlite_master where" + " lower(name) = lower('" + escape(table) + "') and type = 'table'");

        if (!rs.next()) {
          throw new SQLException("Table not found: '" + table + "'");
        }

        Matcher matcher = PK_NAMED_PATTERN.matcher(rs.getString(1));
        if (matcher.find()) {
          pkName = '\'' + escape(matcher.group(1).toLowerCase()) + '\'';
          pkColumns = matcher.group(2).split(",");
        } else {
          matcher = PK_UNNAMED_PATTERN.matcher(rs.getString(1));
          if (matcher.find()) {
            pkColumns = matcher.group(1).split(",");
          }
        }

        if (pkColumns == null) {
          rs = stat.executeQuery("pragma table_info('" + escape(table) + "');");
          while (rs.next()) {
            if (rs.getBoolean(6)) {
              pkColumns = new String[] { rs.getString(2) };
            }
          }
        }

        if (pkColumns != null) {
          for (int i = 0; i < pkColumns.length; i++) {
            pkColumns[i] = pkColumns[i].toLowerCase().trim();
          }
        }
      } finally {
        try {
          if (rs != null) {
            rs.close();
          }
        } catch (Exception e) {
        }
        try {
          if (stat != null) {
            stat.close();
          }
        } catch (Exception e) {
        }
      }
    }

    /**
     * @return The primary key name if any.
     */
    public String getName() {
      return pkName;
    }

    /**
     * @return Array of primary key column(s) if any.
     */
    public String[] getColumns() {
      return pkColumns;
    }
  }

  /**
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    close();
  }
}
