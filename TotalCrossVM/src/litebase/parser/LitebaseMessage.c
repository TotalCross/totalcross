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
 * Defines the functions and globals used to display Litebase error messages.
 */

#include "LitebaseMessage.h"

/**
 * Initializes the error message arrays.
 */
void initLitebaseMessage(void)
{
   TRACE("initLitebaseMessage")

   // Some errors have space at the end. This can't be changed.

   // English messages.
   // General errors.
   errorMsgs_en[ERR_MESSAGE_START] = "Error: ";
   errorMsgs_en[ERR_MESSAGE_POSITION] = " Near position %d.";
   errorMsgs_en[ERR_SYNTAX_ERROR] = "Syntax error.";
   
	// Limit errors.
   errorMsgs_en[ERR_MAX_NUM_FIELDS_REACHED] = "Maximum number of different fields was reached.";
   errorMsgs_en[ERR_MAX_NUM_PARAMS_REACHED] = "Maximum number of parameters in the 'WHERE/HAVING' clause was reached.";
   errorMsgs_en[ERR_MAX_COMP_INDICES] = "Maximum number of composed indices 32 was reached.";
   errorMsgs_en[ERR_MAX_TABLE_NAME_LENGTH] = "Table name too big: must be <= 23.";
	errorMsgs_en[ERR_FIELDS_OVERFLOW] = "The maximum number of fields in a SELECT clause was exceeded.";
	errorMsgs_en[ERR_FIELD_OVERFLOW_GROUPBY_ORDERBY] = "Maximum number of columns exceeded in the 'ORDER BY/GROUP BY' clause.";

	// Column errors.
	errorMsgs_en[ERR_UNKNOWN_COLUMN] = "Unknown column %s.";
   errorMsgs_en[ERR_INVALID_COLUMN_NAME] = "Invalid column name: %s.";
	errorMsgs_en[ERR_INVALID_COLUMN_NUMBER] = "Invalid column number: %d.";
   errorMsgs_en[ERR_COLUMN_DOESNOT_HAVE_AN_INDEX] = "The following column(s) does (do) not have an associated index %s.";
   errorMsgs_en[ERR_AMBIGUOUS_COLUMN_NAME] = "Column name in field list is ambiguous: %s.";
   errorMsgs_en[ERR_COLUMN_NOT_FOUND] = "Column not found %s.";
	errorMsgs_en[ERR_DUPLICATED_COLUMN_NAME] = "Duplicated column name: %s.";
	
	// Primary key errors.
	errorMsgs_en[ERR_PRIMARY_KEY_ALREADY_DEFINED] = "A primary key was already defined for this table.";
	errorMsgs_en[ERR_TABLE_DOESNOT_HAVE_PRIMARY_KEY] = "Table does not have a primary key.";
   errorMsgs_en[ERR_STATEMENT_CREATE_DUPLICATED_PK] = "Statement creates a duplicated primary key in %s.";

   // Type errors.
   errorMsgs_en[ERR_INCOMPATIBLE_TYPES] = "Incompatible types.";
	errorMsgs_en[ERR_FIELD_SIZE_IS_NOT_INT] = "Field size must be a positive interger value.";
   errorMsgs_en[ERR_INVALID_NUMBER] = "Value %s is not a valid number for the desired type: %s.";
   errorMsgs_en[ERR_DATA_TYPE_FUNCTION] = "Incompatible data type for the function call: %s";

	// Number of fields errors.
	errorMsgs_en[ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH] = "The number of fields does not match the number of values ";
	errorMsgs_en[ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION] = "The given number of values does not match the table definition %d.";

   // Default value errors.
   errorMsgs_en[ERR_LENGTH_DEFAULT_VALUE_IS_BIGGER] = "Length of default value is bigger than column size.";
   errorMsgs_en[ERR_NOT_NULL_DEFAULT] = "An added column declared as NOT NULL must have a not null default value.";
 
	// Driver errors. 
	errorMsgs_en[ERR_DRIVER_CLOSED] = "This driver instance was closed and cannot be used anymore. Please get a new instance of it.";
   errorMsgs_en[ERR_RESULTSET_CLOSED] = "ResultSet already closed!";
   errorMsgs_en[ERR_RESULTSETMETADATA_CLOSED] = "ResultSetMetaData cannot be used after the ResultSet is closed.";
   errorMsgs_en[ERR_INVALID_CRID] = "The application id must be four characters long.";
   errorMsgs_en[ERR_INVALID_INC] = "The increment must be greater than 0 or -1.";
   errorMsgs_en[ERR_ROWITERATOR_CLOSED] = "Iterator already closed.";
   errorMsgs_en[ERR_PREPARED_STMT_CLOSED] = "Prepared statement closed. Please prepare it again.";
   errorMsgs_en[ERR_INVALID_PARAMETER] = "Invalid connection parameter: %s.";

   // Table errors.
	errorMsgs_en[ERR_TABLE_NAME_NOT_FOUND] = "Table name not found: %s.";
	errorMsgs_en[ERR_TABLE_ALREADY_CREATED] = "Table already created: %s.";
	errorMsgs_en[ERR_WRONG_STRING_FORMAT] = "It is not possible to open a table within a connection with a different string format.";
   errorMsgs_en[ERR_WRONG_CRYPTO_FORMAT] = "It is not possible to open a table within a connection with a different cryptography format.";

   // ROWID errors.
   errorMsgs_en[ERR_ROWID_CANNOT_BE_CHANGED] = "ROWID can't be changed by the user!";

   // Prepared Statement errors.
	errorMsgs_en[ERR_QUERY_DOESNOT_RETURN_RESULTSET] = "Query does not return result set.";
   errorMsgs_en[ERR_QUERY_DOESNOT_PERFORM_UPDATE] = "Query does not perform updates in the database.";
   errorMsgs_en[ERR_NOT_ALL_PARAMETERS_DEFINED] = "Not all parameters of the query had their values defined.";
   errorMsgs_en[ERR_PARAMETER_NOT_DEFINED] = "A value was not defined for the parameter %d.";
	errorMsgs_en[ERR_INVALID_PARAMETER_INDEX] = "Invalid parameter index.";

	// Rename errors. 
	errorMsgs_en[ERR_TABLE_ALREADY_EXIST] = "Can't rename table. This table already exists: %s.";
   errorMsgs_en[ERR_COLUMN_ALREADY_EXIST] = "Column already exists: %s.";

	// Alias errors.
   errorMsgs_en[ERR_NOT_UNIQUE_ALIAS_TABLE] = "Not unique table/alias: %s.";
   errorMsgs_en[ERR_DUPLICATE_ALIAS] = "This alias is already being used in this expression: %s.";
   errorMsgs_en[ERR_REQUIRED_ALIAS] = "An alias is required for the aggregate function column.";

	// Litebase.execute() error.
   errorMsgs_en[ERR_ONLY_CREATE_TABLE_INDEX_IS_ALLOWED] = "Only CREATE TABLE and CREATE INDEX can be used in Litebase.execute().";
   
	// Order by and group by errors.
	errorMsgs_en[ERR_ORDER_GROUPBY_MUST_MATCH] = "ORDER BY and GROUP BY clauses must match.";
   errorMsgs_en[ERR_VIRTUAL_COLUMN_ON_GROUPBY] = "No support for virtual columns in SQL queries with GROUP BY clause.";
   
   // Function errors.
	errorMsgs_en[ERR_AGGREG_FUNCTION_ISNOT_ON_SELECT] 
	            = "All non-aggregation function columns in the SELECT clause must also be in the GROUP BY clause.";
	errorMsgs_en[ERR_IS_NOT_AGGREG_FUNCTION] 
	 = "%s is not an aggregation function. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation functions.";
	errorMsgs_en[ERR_CANNOT_MIX_AGGREG_FUNCTION] = "Can't mix aggregation functions with real columns in the SELECT clause without a GROUP BY clause.";
   errorMsgs_en[ERR_CANNOT_HAVE_AGGREG_AND_NO_GROUPBY] = "Can't have aggregation functions with ORDER BY clause and no GROUP BY clause.";
   errorMsgs_en[ERR_WAS_NOT_LISTED_ON_AGGREG_FUNCTION] 
= "%s was not listed in the SELECT clause. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation funtions.";
   errorMsgs_en[ERR_SUM_AVG_WITH_DATE_DATETIME] = "SUM and AVG aggregation functions are not used with DATE and DATETIME type fields.";

   // DATE and DATETIME errors.
   errorMsgs_en[ERR_VALUE_ISNOT_DATE] = "Value is not a DATE: %s.";
   errorMsgs_en[ERR_VALUE_ISNOT_DATETIME] = "Value is not a DATETIME: %s.";

   // Index error.
   errorMsgs_en[ERR_INDEX_ALREADY_CREATED] = "Index already created for column %s.";
   errorMsgs_en[ERR_DROP_PRIMARY_KEY] = "Can't drop a primary key index withdrop index.";
   errorMsgs_en[ERR_INDEX_LARGE] = "Index too large. It can't have more than 65534 nodes.";
      
   // NOT NULL errors.
   errorMsgs_en[ERR_PK_CANT_BE_NULL] = "Primary key can't have null.";
   errorMsgs_en[ERR_FIELD_CANT_BE_NULL] = "Field can't be null: %s.";
   errorMsgs_en[ERR_PARAM_NULL] = "A parameter in a where clause can't be null.";

   // Result set errors.
   errorMsgs_en[ERR_RS_INV_POS] = "ResultSet in invalid record position: %d.";
   errorMsgs_en[ERR_RS_DEC_PLACES_START] = "Invalid value for decimal places: %d. It must range from -1 to 40.";

   // File errors.
   errorMsgs_en[ERR_CANT_READ] = "Can't read from table %s.";
   errorMsgs_en[ERR_CANT_LOAD_NODE] = "Can't load leaf node!";
   errorMsgs_en[ERR_TABLE_CORRUPTED] = "Table is corrupted: %s.";
   errorMsgs_en[ERR_TABLE_NOT_CLOSED] = "Table not closed properly: %s."; // juliana@220_2
   errorMsgs_en[ERR_TABLE_CLOSED] = "A properly closed table can't be used in recoverTable(): %s."; // juliana@222_2
	errorMsgs_en[ERR_IDX_RECORD_DEL] = "Can't find index record position on delete.";
   errorMsgs_en[ERR_WRONG_VERSION] = "The table format (%d) is incompatible with Litebase version. Please update your tables.";
   errorMsgs_en[ERR_WRONG_PREV_VERSION] = "The table format is not the previous one: %s."; // juliana@220_11
	errorMsgs_en[ERR_INVALID_PATH] = "Invalid path: %s."; // juliana@214_1
	errorMsgs_en[ERR_INVALID_POS] = "Invalid file position: %d.";
   errorMsgs_en[ERR_DB_NOT_FOUND] = "Database not found."; // juliana@226_10
   errorMsgs_en[ERR_TABLE_OPENED] = "An opened table can't be recovered or converted: %s."; // juliana@230_12

   // BLOB errors.
   errorMsgs_en[ERR_BLOB_TOO_BIG] = "The total size of a blob can't be greater then 10 Mb.";
   errorMsgs_en[ERR_INVALID_MULTIPLIER] = "This is not a valid size multiplier.";
   errorMsgs_en[ERR_BLOB_PRIMARY_KEY] = "A blob type can't be part of a primary key.";
   errorMsgs_en[ERR_BLOB_INDEX] = "A BLOB column can't be indexed.";
   errorMsgs_en[ERR_BLOB_WHERE] = "A BLOB can't be in the where clause.";
   errorMsgs_en[ERR_BLOB_STRING] = "A BLOB can't be converted to a string.";
   errorMsgs_en[ERR_BLOB_ORDER_GROUP] = "Blobs types can't be in ORDER BY or GROUP BY clauses.";
   errorMsgs_en[ERR_COMP_BLOBS] = "It is not possible to compare BLOBs.";
   errorMsgs_en[ERR_BLOBS_PREPARED] = "It is only possible to insert or update a BLOB through prepared statements using setBlob().";

   // Portuguese messages.
	// General errors.
   errorMsgs_pt[ERR_MESSAGE_START] = "Erro: ";
   errorMsgs_pt[ERR_MESSAGE_POSITION] = " Pr�ximo � posi��o %s.";
   errorMsgs_pt[ERR_SYNTAX_ERROR] = "Erro de sintaxe.";

	// Limit errors.
	errorMsgs_pt[ERR_MAX_NUM_FIELDS_REACHED] = "N�mero m�ximo de campos diferentes foi alcan�ado.";
   errorMsgs_pt[ERR_MAX_NUM_PARAMS_REACHED] = "N�mero m�ximo da lista de par�metros na cl�usula 'WHERE/HAVING' foi alcan�ado.";
   errorMsgs_pt[ERR_MAX_COMP_INDICES] = "Numero m�ximo de �ndices compostos 32 foi alcan�ado.";
   errorMsgs_pt[ERR_MAX_TABLE_NAME_LENGTH] = "Nome da tabela muito grande: deve ser <= 23";
   errorMsgs_pt[ERR_FIELDS_OVERFLOW] = "O n�mero m�ximo de campos na cl�usula SELECT foi excedido.";
   errorMsgs_pt[ERR_FIELD_OVERFLOW_GROUPBY_ORDERBY] = "O n�mero m�ximo de campos na cl�usula 'ORDER BY/GROUP BY' foi excedido.";

   // Column errors.
   errorMsgs_pt[ERR_UNKNOWN_COLUMN] = "Coluna desconhecida %s."; 
	errorMsgs_pt[ERR_INVALID_COLUMN_NAME] = "Nome de coluna inv�lido: %s.";
   errorMsgs_pt[ERR_INVALID_COLUMN_NUMBER] = "N�mero de coluna inv�lido: %d.";
   errorMsgs_pt[ERR_COLUMN_DOESNOT_HAVE_AN_INDEX] = "A(s) coluna(s) a seguir n�o tem (t�m) um ind�ce associado %s.";
   errorMsgs_pt[ERR_AMBIGUOUS_COLUMN_NAME]= "Nome de coluna amb�guo: %s.";
	errorMsgs_pt[ERR_COLUMN_NOT_FOUND] = "Coluna n�o encontrada: %s.";
	errorMsgs_pt[ERR_DUPLICATED_COLUMN_NAME] = "Nome de coluna duplicado: %s.";

   // Primary key errors.
   errorMsgs_pt[ERR_PRIMARY_KEY_ALREADY_DEFINED] = "Uma chave prim�ria j� foi definida para esta tabela.";
   errorMsgs_pt[ERR_TABLE_DOESNOT_HAVE_PRIMARY_KEY] = "Tabela n�o tem chave prim�ria.";
	errorMsgs_pt[ERR_STATEMENT_CREATE_DUPLICATED_PK] = "Comando cria uma chave prim�ria duplicada em %s.";

   // Type errors.
   errorMsgs_pt[ERR_INCOMPATIBLE_TYPES] = "Tipos incompativeis.";
	errorMsgs_pt[ERR_FIELD_SIZE_IS_NOT_INT] = "Tamanho do campo deve ser um valor inteiro positivo.";
   errorMsgs_pt[ERR_INVALID_NUMBER] = "O valor %s n�o � um n�mero v�lido para o tipo desejado: %s.";
   errorMsgs_pt[ERR_DATA_TYPE_FUNCTION] = "Tipo de dados incompat�vel para a chamada de fun��o: %s";

	// Number of fields errors.
   errorMsgs_pt[ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH] = "O n�mero de campos � diferente do n�mero de valores ";
   errorMsgs_pt[ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION] = "O n�mero de valores dado n�o coincide com a defini��o da tabela %d.";

   // Default value errors.
	errorMsgs_pt[ERR_LENGTH_DEFAULT_VALUE_IS_BIGGER] = "Tamanho do valor padr�o � maior que o tamanho definido para a coluna.";
   errorMsgs_pt[ERR_NOT_NULL_DEFAULT] = "Uma coluna adicionada declarada como NOT NULL deve ter um valor padr�o n�o nulo.";

	// Driver errors. 
	errorMsgs_pt[ERR_DRIVER_CLOSED] = "Esta inst�ncia do driver est� fechada e n�o pode ser mais utilizada. Por favor, obtenha uma nova inst�ncia.";
   errorMsgs_pt[ERR_RESULTSET_CLOSED] = "ResultSet j� est� fechado!";
	errorMsgs_pt[ERR_RESULTSETMETADATA_CLOSED] = "ResultSetMetaData n�o pode ser usado depois que o ResultSet estiver fechado.";
   errorMsgs_pt[ERR_INVALID_CRID] = "O id da aplica��o de ter 4 characteres.";
	errorMsgs_pt[ERR_INVALID_INC] = "O incremento deve ser maior do que 0 ou -1.";
	errorMsgs_pt[ERR_ROWITERATOR_CLOSED] = "Iterador j� foi fechado.";
   errorMsgs_pt[ERR_PREPARED_STMT_CLOSED] = "Prepared statement fechado. Por favor, prepare-o novamente.";
   errorMsgs_pt[ERR_INVALID_PARAMETER] = "Par�metro de conex�o inv�lido: %s.";

	// Table errors.
	errorMsgs_pt[ERR_TABLE_NAME_NOT_FOUND] = "Nome da tabela n�o encontrado: %s.";
   errorMsgs_pt[ERR_TABLE_ALREADY_CREATED] = "Tabela j� existe: %s.";
	errorMsgs_pt[ERR_WRONG_STRING_FORMAT] = "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de strings diferente.";
   errorMsgs_pt[ERR_WRONG_CRYPTO_FORMAT] = "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de criptografia diferente.";

	// ROWID errors.
   errorMsgs_pt[ERR_ROWID_CANNOT_BE_CHANGED] = "ROWID n�o pode ser mudado pelo usu�rio!";

   // Prepared Statement errors.
   errorMsgs_pt[ERR_QUERY_DOESNOT_RETURN_RESULTSET] = "Comando SQL n�o retorna um ResultSet.";
   errorMsgs_pt[ERR_QUERY_DOESNOT_PERFORM_UPDATE] = "Comando SQL n�o executa uma atualiza��o no banco de dados.";
   errorMsgs_pt[ERR_NOT_ALL_PARAMETERS_DEFINED] = "Nem todos os par�metros da consulta tiveram seus valores definidos.";
   errorMsgs_pt[ERR_PARAMETER_NOT_DEFINED] = "N�o foi definido um valor para o par�metro %d.";
   errorMsgs_pt[ERR_INVALID_PARAMETER_INDEX] = "Invalid parameter index.";
   
   // Rename errors. 
   errorMsgs_pt[ERR_TABLE_ALREADY_EXIST] = "N�o � poss�vel renomear a tabela. Esta tabela j� existe: %s.";
   errorMsgs_pt[ERR_COLUMN_ALREADY_EXIST] = "Coluna j� existe: %s.";

	// Alias errors.
   errorMsgs_pt[ERR_NOT_UNIQUE_ALIAS_TABLE] = "Nome de tabela/alias repetido: %s.";
   errorMsgs_pt[ERR_DUPLICATE_ALIAS] = "Este alias j� est� sendo utilizado no sql: %s.";
   errorMsgs_pt[ERR_REQUIRED_ALIAS] = "Um alias � necess�rio para colunas com fun��o de agrega��o.";
   
	// Litebase.execute() error.
	errorMsgs_pt[ERR_ONLY_CREATE_TABLE_INDEX_IS_ALLOWED] = "Apenas CREATE TABLE e CREATE INDEX s�o permitidos no Litebase.execute()";
   
   // Order by and group by errors.
	errorMsgs_pt[ERR_ORDER_GROUPBY_MUST_MATCH] = "Cl�usulas ORDER BY e GROUP BY devem coincidir.";
   errorMsgs_pt[ERR_VIRTUAL_COLUMN_ON_GROUPBY] = "SQL com cl�usula GROUP BY n�o tem suporte para colunas virtuais.";
   
   // Function errors.
	errorMsgs_pt[ERR_AGGREG_FUNCTION_ISNOT_ON_SELECT] 
	            = "Todas colunas que n�os�o fun��es de agrega��o na cl�usula SELECT devem estar na cl�usula GROUP BY.";
	errorMsgs_pt[ERR_IS_NOT_AGGREG_FUNCTION] 
	           = "%s n�o � uma fun��o de agrega��o. Todos as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias.";
	errorMsgs_pt[ERR_CANNOT_MIX_AGGREG_FUNCTION] = "N�o � possivel misturar colunas reais e de agrega��o no SELECT sem cl�usula GROUP BY.";
   errorMsgs_pt[ERR_CANNOT_HAVE_AGGREG_AND_NO_GROUPBY] = "N�o � poss�vel ter fun��es de agrega��o com cl�usula ORDER BY sem cl�usula GROUP BY.";
   errorMsgs_pt[ERR_WAS_NOT_LISTED_ON_AGGREG_FUNCTION] 
	            = "%s n�o foi listado no SELECT. Todas as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias.";
   errorMsgs_pt[ERR_SUM_AVG_WITH_DATE_DATETIME] = "Fun��es de agrega��o SUM e AVG n�o s�o usadas com colunas do tipo DATE e DATETIME.";

   // DATE and DATETIME errors.
	errorMsgs_pt[ERR_VALUE_ISNOT_DATE] = "Valor n�o � um tipo DATE v�lido: %s.";
   errorMsgs_pt[ERR_VALUE_ISNOT_DATETIME] = "Valor n�o � um tipo DATETIME v�lido: %s.";

   // Index error.
   errorMsgs_pt[ERR_INDEX_ALREADY_CREATED] = "�ndice j� criado para a coluna %s.";
   errorMsgs_pt[ERR_DROP_PRIMARY_KEY] = "N�o � poss�vel remover uma chave prim�ria usando drop index.";
   errorMsgs_pt[ERR_INDEX_LARGE] = "�ndice muito grande. Ele n�o pode ter mais do que 65534 n�s.";
      
   // NOT NULL errors.
   errorMsgs_pt[ERR_PK_CANT_BE_NULL] = "Chave prim�ria n�o pode ter NULL.";
   errorMsgs_pt[ERR_FIELD_CANT_BE_NULL] = "Coluna n�o pode ser NULL: %s.";
   errorMsgs_pt[ERR_PARAM_NULL] = "Um par�metro em uma where clause n�o pode ser NULL.";

   // Result set errors.
   errorMsgs_pt[ERR_RS_INV_POS] = "ResultSet em uma posi��o de registro inv�lida %d.";
   errorMsgs_pt[ERR_RS_DEC_PLACES_START] = "Valor inv�lido para casas decimais: %d. Deve ficar entre - 1 e 40.";

   // File errors.
   errorMsgs_pt[ERR_CANT_READ] = "N�o � poss�vel ler da tabela %s.";
   errorMsgs_pt[ERR_CANT_LOAD_NODE] = "N�o � poss�vel carregar n� folha!";
   errorMsgs_pt[ERR_TABLE_CORRUPTED] = "Tabela est� corrompida: %s.";
	errorMsgs_pt[ERR_TABLE_NOT_CLOSED] = "Tabela n�o foi fechada corretamente: %s."; // juliana@220_2
   errorMsgs_pt[ERR_TABLE_CLOSED] = "Uma tabela fechada corretamente n�o pode ser usada no recoverTable(): %s."; // juliana@222_2
	errorMsgs_pt[ERR_IDX_RECORD_DEL] = "N�o � poss�vel achar a posi��o de registro no �ndice na exclus�o.";
   errorMsgs_pt[ERR_WRONG_VERSION] = "O formato de tabela (%d) n�o � compat�vel com a vers�o do Litebase. Por favor, atualize suas tabelas.";
   errorMsgs_pt[ERR_WRONG_PREV_VERSION] = "O formato de tabela n�o � o anterior: %s."; // juliana@220_11
   errorMsgs_pt[ERR_INVALID_PATH] = "Caminho inv�lido: %s."; // juliana@214_1
   errorMsgs_pt[ERR_INVALID_POS] = "Posi��o inv�lida no arquivo: %d.";
   errorMsgs_pt[ERR_DB_NOT_FOUND] = "Base de dados n�o encontrada."; // juliana@226_10
   errorMsgs_pt[ERR_TABLE_OPENED] = "Uma tabela aberta n�o pode ser recuperada ou convertida: %s."; // juliana@230_12

   // BLOB errors.
   errorMsgs_pt[ERR_BLOB_TOO_BIG] = "O tamanho total de um BLOB n�o pode ser maior do que 10 Mb.";
   errorMsgs_pt[ERR_INVALID_MULTIPLIER] = "O multiplicador de tamanho n�o � v�lido.";
   errorMsgs_pt[ERR_BLOB_PRIMARY_KEY] = "Um tipo BLOB n�o pode ser parte de uma chave prim�ria.";
   errorMsgs_pt[ERR_BLOB_INDEX] = "Uma coluna do tipo BLOB n�o pode ser indexada.";
   errorMsgs_pt[ERR_BLOB_WHERE] = "Um BLOB n�o pode estar na cl�usula WHERE.";
   errorMsgs_pt[ERR_BLOB_STRING] = "Um BLOB n�o pode ser convertido em uma string.";
   errorMsgs_pt[ERR_BLOB_ORDER_GROUP] = "Tipos BLOB n�o podem estar em cl�usulas ORDER BY ou GROUP BY.";
   errorMsgs_pt[ERR_COMP_BLOBS] = "N�o � poss�vel comparar BLOBs.";
   errorMsgs_pt[ERR_BLOBS_PREPARED] = "S� � poss�vel inserir ou atualizar um BLOB atrav�s prepared statements usando setBlob().";
}

/**
 * Gets the correct error message.
 * 
 * @param messageNumber The error message code.
 * @return The string with the desired error message.
 */
CharP getMessage(int32 messageNumber)
{
	TRACE("getMessage")
	if (litebaseConnectionClass->i32StaticValues[4] == LANGUAGE_PT)
      return errorMsgs_pt[messageNumber];
   return errorMsgs_en[messageNumber];
}

#ifdef ENABLE_TEST_SUITE

/**
 * Tests that <code>getMessage()</code> returns the correct error message.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(getMessage)
{
   UNUSED(currentContext)

   // English messages.
   // General errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_MESSAGE_START), "Error: ");
   ASSERT2_EQUALS(Sz, getMessage(ERR_MESSAGE_POSITION), " Near position %d.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_SYNTAX_ERROR), "Syntax error.");
   
	// Limit errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_MAX_NUM_FIELDS_REACHED), "Maximum number of different fields was reached.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_MAX_NUM_PARAMS_REACHED), "Maximum number of parameters in the 'WHERE/HAVING' clause was reached.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_MAX_COMP_INDICES), "Maximum number of composed indices 32 was reached.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_MAX_TABLE_NAME_LENGTH), "Table name too big: must be <= 23.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_FIELDS_OVERFLOW), "The maximum number of fields in a SELECT clause was exceeded.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_FIELD_OVERFLOW_GROUPBY_ORDERBY), "Maximum number of columns exceeded in the 'ORDER BY/GROUP BY' clause.");

	// Column errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_UNKNOWN_COLUMN), "Unknown column %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_COLUMN_NAME), "Invalid column name: %s.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_COLUMN_NUMBER), "Invalid column number: %d.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_COLUMN_DOESNOT_HAVE_AN_INDEX), "The following column(s) does (do) not have an associated index %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_AMBIGUOUS_COLUMN_NAME), "Column name in field list is ambiguous: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_COLUMN_NOT_FOUND), "Column not found %s.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_DUPLICATED_COLUMN_NAME), "Duplicated column name: %s.");
	
	// Primary key errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_PRIMARY_KEY_ALREADY_DEFINED), "A primary key was already defined for this table.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_DOESNOT_HAVE_PRIMARY_KEY), "Table does not have a primary key.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_STATEMENT_CREATE_DUPLICATED_PK), "Statement creates a duplicated primary key in %s.");

   // Type errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_INCOMPATIBLE_TYPES), "Incompatible types.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_FIELD_SIZE_IS_NOT_INT), "Field size must be a positive interger value.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_NUMBER), "Value %s is not a valid number for the desired type: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_DATA_TYPE_FUNCTION), "Incompatible data type for the function call: %s");

	// Number of fields errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH), "The number of fields does not match the number of values ");
	ASSERT2_EQUALS(Sz, getMessage(ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION), "The given number of values does not match the table definition %d.");

   // Default value errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_LENGTH_DEFAULT_VALUE_IS_BIGGER), "Length of default value is bigger than column size.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_NOT_NULL_DEFAULT), "An added column declared as NOT NULL must have a not null default value.");

	// Driver errors. 
	ASSERT2_EQUALS(Sz, getMessage(ERR_DRIVER_CLOSED), "This driver instance was closed and cannot be used anymore. Please get a new instance of it.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_RESULTSET_CLOSED), "ResultSet already closed!");
   ASSERT2_EQUALS(Sz, getMessage(ERR_RESULTSETMETADATA_CLOSED), "ResultSetMetaData cannot be used after the ResultSet is closed.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_CRID), "The application id must be four characters long.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_INC), "The increment must be greater than 0 or -1.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_ROWITERATOR_CLOSED), "Iterator already closed.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_PREPARED_STMT_CLOSED), "Prepared statement closed. Please prepare it again.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_PARAMETER), "Invalid connection parameter: %s.");

   // Table errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_NAME_NOT_FOUND), "Table name not found: %s.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_ALREADY_CREATED), "Table already created: %s.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_WRONG_STRING_FORMAT), "It is not possible to open a table within a connection with a different string format.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_WRONG_CRYPTO_FORMAT), "It is not possible to open a table within a connection with a different cryptography format.");

   // ROWID errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_ROWID_CANNOT_BE_CHANGED), "ROWID can't be changed by the user!");

   // Prepared Statement errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_QUERY_DOESNOT_RETURN_RESULTSET), "Query does not return result set.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_QUERY_DOESNOT_PERFORM_UPDATE), "Query does not perform updates in the database.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_NOT_ALL_PARAMETERS_DEFINED), "Not all parameters of the query had their values defined.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_PARAMETER_NOT_DEFINED), "A value was not defined for the parameter %d.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_PARAMETER_INDEX), "Invalid parameter index.");

	// Rename errors. 
	ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_ALREADY_EXIST), "Can't rename table. This table already exists: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_COLUMN_ALREADY_EXIST), "Column already exists: %s.");

	// Alias errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_NOT_UNIQUE_ALIAS_TABLE), "Not unique table/alias: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_DUPLICATE_ALIAS), "This alias is already being used in this expression: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_REQUIRED_ALIAS), "An alias is required for the aggregate function column.");

	// Litebase.execute() error.
   ASSERT2_EQUALS(Sz, getMessage(ERR_ONLY_CREATE_TABLE_INDEX_IS_ALLOWED), "Only CREATE TABLE and CREATE INDEX can be used in Litebase.execute().");
   
	// Order by and group by errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_ORDER_GROUPBY_MUST_MATCH), "ORDER BY and GROUP BY clauses must match.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_VIRTUAL_COLUMN_ON_GROUPBY), "No support for virtual columns in SQL queries with GROUP BY clause.");
   
   // Function errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_AGGREG_FUNCTION_ISNOT_ON_SELECT), 
                                                     "All non-aggregation function columns in the SELECT clause must also be in the GROUP BY clause.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_IS_NOT_AGGREG_FUNCTION), 
	   "%s is not an aggregation function. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation functions.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_CANNOT_MIX_AGGREG_FUNCTION), 
                                                "Can't mix aggregation functions with real columns in the SELECT clause without a GROUP BY clause.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_CANNOT_HAVE_AGGREG_AND_NO_GROUPBY), 
                                                               "Can't have aggregation functions with ORDER BY clause and no GROUP BY clause.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_WAS_NOT_LISTED_ON_AGGREG_FUNCTION), 
"%s was not listed in the SELECT clause. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation funtions."
);
   ASSERT2_EQUALS(Sz, getMessage(ERR_SUM_AVG_WITH_DATE_DATETIME), 
                                                       "SUM and AVG aggregation functions are not used with DATE and DATETIME type fields.");

   // DATE and DATETIME errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_VALUE_ISNOT_DATE), "Value is not a DATE: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_VALUE_ISNOT_DATETIME), "Value is not a DATETIME: %s.");

   // Index error.
   ASSERT2_EQUALS(Sz, getMessage(ERR_INDEX_ALREADY_CREATED), "Index already created for column %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_DROP_PRIMARY_KEY), "Can't drop a primary key index withdrop index.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INDEX_LARGE), "Index too large. It can't have more than 65534 nodes.");
      
   // NOT NULL errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_PK_CANT_BE_NULL), "Primary key can't have null.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_FIELD_CANT_BE_NULL), "Field can't be null: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_PARAM_NULL), "A parameter in a where clause can't be null.");

   // Result set errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_RS_INV_POS), "ResultSet in invalid record position: %d.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_RS_DEC_PLACES_START), "Invalid value for decimal places: %d. It must range from -1 to 40.");

   // File errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_CANT_READ), "Can't read from table %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_CANT_LOAD_NODE), "Can't load leaf node!");
   ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_CORRUPTED), "Table is corrupted: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_NOT_CLOSED), "Table not closed properly: %s."); // juliana@220_2
   ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_CLOSED), "A properly closed table can't be used in recoverTable(): %s."); // juliana@222_2
	ASSERT2_EQUALS(Sz, getMessage(ERR_IDX_RECORD_DEL), "Can't find index record position on delete.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_WRONG_VERSION), "The table format (%d) is incompatible with Litebase version. Please update your tables.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_WRONG_PREV_VERSION), "The table format is not the previous one: %s."); // juliana@220_11
	ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_PATH), "Invalid path: %s."); // juliana@214_1
	ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_POS), "Invalid file position: %d.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_DB_NOT_FOUND), "Database not found."); // juliana@226_10
   ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_OPENED), "An opened table can't be recovered or converted: %s."); // juliana@230_12

   // BLOB errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_TOO_BIG), "The total size of a blob can't be greater then 10 Mb.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_MULTIPLIER), "This is not a valid size multiplier.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_PRIMARY_KEY), "A blob type can't be part of a primary key.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_INDEX), "A BLOB column can't be indexed.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_WHERE), "A BLOB can't be in the where clause.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_STRING), "A BLOB can't be converted to a string.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_ORDER_GROUP), "Blobs types can't be in ORDER BY or GROUP BY clauses.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_COMP_BLOBS), "It is not possible to compare BLOBs.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOBS_PREPARED), "It is only possible to insert or update a BLOB through prepared statements using setBlob().");

   // Portuguese messages.
   litebaseConnectionClass->i32StaticValues[4] = LANGUAGE_PT;

	// General errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_MESSAGE_START), "Erro: ");
   ASSERT2_EQUALS(Sz, getMessage(ERR_MESSAGE_POSITION), " Pr�ximo � posi��o %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_SYNTAX_ERROR), "Erro de sintaxe.");

	// Limit errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_MAX_NUM_FIELDS_REACHED), "N�mero m�ximo de campos diferentes foi alcan�ado.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_MAX_NUM_PARAMS_REACHED), "N�mero m�ximo da lista de par�metros na cl�usula 'WHERE/HAVING' foi alcan�ado.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_MAX_COMP_INDICES), "Numero m�ximo de �ndices compostos 32 foi alcan�ado.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_MAX_TABLE_NAME_LENGTH), "Nome da tabela muito grande: deve ser <= 23");
   ASSERT2_EQUALS(Sz, getMessage(ERR_FIELDS_OVERFLOW), "O n�mero m�ximo de campos na cl�usula SELECT foi excedido.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_FIELD_OVERFLOW_GROUPBY_ORDERBY), "O n�mero m�ximo de campos na cl�usula 'ORDER BY/GROUP BY' foi excedido.");

   // Column errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_UNKNOWN_COLUMN), "Coluna desconhecida %s."); 
	ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_COLUMN_NAME), "Nome de coluna inv�lido: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_COLUMN_NUMBER), "N�mero de coluna inv�lido: %d.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_COLUMN_DOESNOT_HAVE_AN_INDEX), "A(s) coluna(s) a seguir n�o tem (t�m) um ind�ce associado %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_AMBIGUOUS_COLUMN_NAME), "Nome de coluna amb�guo: %s.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_COLUMN_NOT_FOUND), "Coluna n�o encontrada: %s.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_DUPLICATED_COLUMN_NAME), "Nome de coluna duplicado: %s.");

   // Primary key errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_PRIMARY_KEY_ALREADY_DEFINED), "Uma chave prim�ria j� foi definida para esta tabela.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_DOESNOT_HAVE_PRIMARY_KEY), "Tabela n�o tem chave prim�ria.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_STATEMENT_CREATE_DUPLICATED_PK), "Comando cria uma chave prim�ria duplicada em %s.");

   // Type errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_INCOMPATIBLE_TYPES), "Tipos incompativeis.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_FIELD_SIZE_IS_NOT_INT), "Tamanho do campo deve ser um valor inteiro positivo.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_NUMBER), "O valor %s n�o � um n�mero v�lido para o tipo desejado: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_DATA_TYPE_FUNCTION), "Tipo de dados incompat�vel para a chamada de fun��o: %s");

	// Number of fields errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_NUMBER_FIELDS_AND_VALUES_DOES_NOT_MATCH), "O n�mero de campos � diferente do n�mero de valores ");
   ASSERT2_EQUALS(Sz, getMessage(ERR_NUMBER_VALUES_DIFF_TABLE_DEFINITION), "O n�mero de valores dado n�o coincide com a defini��o da tabela %d.");

   // Default value errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_LENGTH_DEFAULT_VALUE_IS_BIGGER), "Tamanho do valor padr�o � maior que o tamanho definido para a coluna.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_NOT_NULL_DEFAULT), "Uma coluna adicionada declarada como NOT NULL deve ter um valor padr�o n�o nulo.");

	// Driver errors. 
	ASSERT2_EQUALS(Sz, getMessage(ERR_DRIVER_CLOSED), 
                                     "Esta inst�ncia do driver est� fechada e n�o pode ser mais utilizada. Por favor, obtenha uma nova inst�ncia.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_RESULTSET_CLOSED), "ResultSet j� est� fechado!");
	ASSERT2_EQUALS(Sz, getMessage(ERR_RESULTSETMETADATA_CLOSED), "ResultSetMetaData n�o pode ser usado depois que o ResultSet estiver fechado.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_CRID), "O id da aplica��o de ter 4 characteres.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_INC), "O incremento deve ser maior do que 0 ou -1.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_ROWITERATOR_CLOSED), "Iterador j� foi fechado.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_PREPARED_STMT_CLOSED), "Prepared statement fechado. Por favor, prepare-o novamente.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_PARAMETER), "Par�metro de conex�o inv�lido: %s.");

	// Table errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_NAME_NOT_FOUND), "Nome da tabela n�o encontrado: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_ALREADY_CREATED), "Tabela j� existe: %s.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_WRONG_STRING_FORMAT), "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de strings diferente.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_WRONG_CRYPTO_FORMAT), "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de criptografia diferente.");

	// ROWID errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_ROWID_CANNOT_BE_CHANGED), "ROWID n�o pode ser mudado pelo usu�rio!");

   // Prepared Statement errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_QUERY_DOESNOT_RETURN_RESULTSET), "Comando SQL n�o retorna um ResultSet.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_QUERY_DOESNOT_PERFORM_UPDATE), "Comando SQL n�o executa uma atualiza��o no banco de dados.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_NOT_ALL_PARAMETERS_DEFINED), "Nem todos os par�metros da consulta tiveram seus valores definidos.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_PARAMETER_NOT_DEFINED), "N�o foi definido um valor para o par�metro %d.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_PARAMETER_INDEX), "Invalid parameter index.");
   
   // Rename errors. 
   ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_ALREADY_EXIST), "N�o � poss�vel renomear a tabela. Esta tabela j� existe: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_COLUMN_ALREADY_EXIST), "Coluna j� existe: %s.");

	// Alias errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_NOT_UNIQUE_ALIAS_TABLE), "Nome de tabela/alias repetido: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_DUPLICATE_ALIAS), "Este alias j� est� sendo utilizado no sql: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_REQUIRED_ALIAS), "Um alias � necess�rio para colunas com fun��o de agrega��o.");
   
	// Litebase.execute() error.
	ASSERT2_EQUALS(Sz, getMessage(ERR_ONLY_CREATE_TABLE_INDEX_IS_ALLOWED), "Apenas CREATE TABLE e CREATE INDEX s�o permitidos no Litebase.execute()");
   
   // Order by and group by errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_ORDER_GROUPBY_MUST_MATCH), "Cl�usulas ORDER BY e GROUP BY devem coincidir.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_VIRTUAL_COLUMN_ON_GROUPBY), "SQL com cl�usula GROUP BY n�o tem suporte para colunas virtuais.");
   
   // Function errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_AGGREG_FUNCTION_ISNOT_ON_SELECT),
	                                         "Todas colunas que n�os�o fun��es de agrega��o na cl�usula SELECT devem estar na cl�usula GROUP BY.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_IS_NOT_AGGREG_FUNCTION), 
	                   "%s n�o � uma fun��o de agrega��o. Todos as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_CANNOT_MIX_AGGREG_FUNCTION), 
                                                       "N�o � possivel misturar colunas reais e de agrega��o no SELECT sem cl�usula GROUP BY.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_CANNOT_HAVE_AGGREG_AND_NO_GROUPBY), 
                                                            "N�o � poss�vel ter fun��es de agrega��o com cl�usula ORDER BY sem cl�usula GROUP BY.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_WAS_NOT_LISTED_ON_AGGREG_FUNCTION), 
	                              "%s n�o foi listado no SELECT. Todas as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_SUM_AVG_WITH_DATE_DATETIME), 
                                                       "Fun��es de agrega��o SUM e AVG n�o s�o usadas com colunas do tipo DATE e DATETIME.");

   // DATE and DATETIME errors.
	ASSERT2_EQUALS(Sz, getMessage(ERR_VALUE_ISNOT_DATE), "Valor n�o � um tipo DATE v�lido: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_VALUE_ISNOT_DATETIME), "Valor n�o � um tipo DATETIME v�lido: %s.");

   // Index error.
   ASSERT2_EQUALS(Sz, getMessage(ERR_INDEX_ALREADY_CREATED), "�ndice j� criado para a coluna %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_DROP_PRIMARY_KEY), "N�o � poss�vel remover uma chave prim�ria usando drop index.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INDEX_LARGE), "�ndice muito grande. Ele n�o pode ter mais do que 65534 n�s.");
      
   // NOT NULL errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_PK_CANT_BE_NULL), "Chave prim�ria n�o pode ter NULL.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_FIELD_CANT_BE_NULL), "Coluna n�o pode ser NULL: %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_PARAM_NULL), "Um par�metro em uma where clause n�o pode ser NULL.");

   // Result set errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_RS_INV_POS), "ResultSet em uma posi��o de registro inv�lida %d.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_RS_DEC_PLACES_START), "Valor inv�lido para casas decimais: %d. Deve ficar entre - 1 e 40.");

   // File errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_CANT_READ), "N�o � poss�vel ler da tabela %s.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_CANT_LOAD_NODE), "N�o � poss�vel carregar n� folha!");
   ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_CORRUPTED), "Tabela est� corrompida: %s.");
	ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_NOT_CLOSED), "Tabela n�o foi fechada corretamente: %s."); // juliana@220_2
   ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_CLOSED), "Uma tabela fechada corretamente n�o pode ser usada no recoverTable(): %s."); // juliana@222_2
	ASSERT2_EQUALS(Sz, getMessage(ERR_IDX_RECORD_DEL), "N�o � poss�vel achar a posi��o de registro no �ndice na exclus�o.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_WRONG_VERSION), 
                                           "O formato de tabela (%d) n�o � compat�vel com a vers�o do Litebase. Por favor, atualize suas tabelas.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_WRONG_PREV_VERSION), "O formato de tabela n�o � o anterior: %s."); // juliana@220_11
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_PATH), "Caminho inv�lido: %s."); // juliana@214_1
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_POS), "Posi��o inv�lida no arquivo: %d.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_DB_NOT_FOUND), "Base de dados n�o encontrada."); // juliana@226_10
   ASSERT2_EQUALS(Sz, getMessage(ERR_TABLE_OPENED), "Uma tabela aberta n�o pode ser recuperada ou convertida: %s."); // juliana@230_12

   // BLOB errors.
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_TOO_BIG), "O tamanho total de um BLOB n�o pode ser maior do que 10 Mb.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_INVALID_MULTIPLIER), "O multiplicador de tamanho n�o � v�lido.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_PRIMARY_KEY), "Um tipo BLOB n�o pode ser parte de uma chave prim�ria.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_INDEX), "Uma coluna do tipo BLOB n�o pode ser indexada.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_WHERE), "Um BLOB n�o pode estar na cl�usula WHERE.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_STRING), "Um BLOB n�o pode ser convertido em uma string.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOB_ORDER_GROUP), "Tipos BLOB n�o podem estar em cl�usulas ORDER BY ou GROUP BY.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_COMP_BLOBS), "N�o � poss�vel comparar BLOBs.");
   ASSERT2_EQUALS(Sz, getMessage(ERR_BLOBS_PREPARED), "S� � poss�vel inserir ou atualizar um BLOB atrav�s prepared statements usando setBlob().");

   litebaseConnectionClass->i32StaticValues[4] = LANGUAGE_EN;

finish: ;
}

/**
 * Tests that <code>initLitebaseMessage()</code> correctly initializes the error messages.
 * 
 * @param testSuite The test structure.
 * @param currentContext The thread context where the test is being executed.
 */
TESTCASE(initLitebaseMessage)
{  
   UNUSED(currentContext)

   // English messages.
   // General errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[0], "Error: ");
   ASSERT2_EQUALS(Sz, errorMsgs_en[1], " Near position %d.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[2], "Syntax error.");
   
	// Limit errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[3], "Maximum number of different fields was reached.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[4], "Maximum number of parameters in the 'WHERE/HAVING' clause was reached.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[5], "Maximum number of composed indices 32 was reached.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[6], "Table name too big: must be <= 23.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[7], "The maximum number of fields in a SELECT clause was exceeded.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[8], "Maximum number of columns exceeded in the 'ORDER BY/GROUP BY' clause.");

	// Column errors.
	ASSERT2_EQUALS(Sz, errorMsgs_en[9], "Unknown column %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[10], "Invalid column name: %s.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[11], "Invalid column number: %d.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[12], "The following column(s) does (do) not have an associated index %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[13], "Column name in field list is ambiguous: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[14], "Column not found %s.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[15], "Duplicated column name: %s.");
	
	// Primary key errors.
	ASSERT2_EQUALS(Sz, errorMsgs_en[16], "A primary key was already defined for this table.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[17], "Table does not have a primary key.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[18], "Statement creates a duplicated primary key in %s.");

   // Type errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[19], "Incompatible types.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[20], "Field size must be a positive interger value.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[21], "Value %s is not a valid number for the desired type: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[22], "Incompatible data type for the function call: %s");

	// Number of fields errors.
	ASSERT2_EQUALS(Sz, errorMsgs_en[23], "The number of fields does not match the number of values ");
	ASSERT2_EQUALS(Sz, errorMsgs_en[24], "The given number of values does not match the table definition %d.");

   // Default value errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[25], "Length of default value is bigger than column size.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[26], "An added column declared as NOT NULL must have a not null default value.");

	// Driver errors. 
	ASSERT2_EQUALS(Sz, errorMsgs_en[27], "This driver instance was closed and cannot be used anymore. Please get a new instance of it.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[28], "ResultSet already closed!");
   ASSERT2_EQUALS(Sz, errorMsgs_en[29], "ResultSetMetaData cannot be used after the ResultSet is closed.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[30], "The application id must be four characters long.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[31], "The increment must be greater than 0 or -1.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[32], "Iterator already closed.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[33], "Prepared statement closed. Please prepare it again.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[34], "Invalid connection parameter: %s.");

   // Table errors.
	ASSERT2_EQUALS(Sz, errorMsgs_en[35], "Table name not found: %s.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[36], "Table already created: %s.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[37], "It is not possible to open a table within a connection with a different string format.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[38], "It is not possible to open a table within a connection with a different cryptography format.");
 
   // ROWID errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[39], "ROWID can't be changed by the user!");

   // Prepared Statement errors.
	ASSERT2_EQUALS(Sz, errorMsgs_en[40], "Query does not return result set.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[41], "Query does not perform updates in the database.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[42], "Not all parameters of the query had their values defined.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[43], "A value was not defined for the parameter %d.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[44], "Invalid parameter index.");

	// Rename errors. 
	ASSERT2_EQUALS(Sz, errorMsgs_en[45], "Can't rename table. This table already exists: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[46], "Column already exists: %s.");

	// Alias errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[47], "Not unique table/alias: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[48], "This alias is already being used in this expression: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[49], "An alias is required for the aggregate function column.");

	// Litebase.execute() error.
   ASSERT2_EQUALS(Sz, errorMsgs_en[50], "Only CREATE TABLE and CREATE INDEX can be used in Litebase.execute().");
   
	// Order by and group by errors.
	ASSERT2_EQUALS(Sz, errorMsgs_en[51], "ORDER BY and GROUP BY clauses must match.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[52], "No support for virtual columns in SQL queries with GROUP BY clause.");
   
   // Function errors.
	ASSERT2_EQUALS(Sz, errorMsgs_en[53], "All non-aggregation function columns in the SELECT clause must also be in the GROUP BY clause.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[54], 
	  "%s is not an aggregation function. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation functions.");
	ASSERT2_EQUALS(Sz, errorMsgs_en[55], "Can't mix aggregation functions with real columns in the SELECT clause without a GROUP BY clause.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[56], "Can't have aggregation functions with ORDER BY clause and no GROUP BY clause.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[57], 
 "%s was not listed in the SELECT clause. All fields present in a HAVING clause must be listed in the SELECT clause as aliased aggregation funtions."
);
   ASSERT2_EQUALS(Sz, errorMsgs_en[58], "SUM and AVG aggregation functions are not used with DATE and DATETIME type fields.");

   // DATE and DATETIME errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[59], "Value is not a DATE: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[60], "Value is not a DATETIME: %s.");

   // Index error.
   ASSERT2_EQUALS(Sz, errorMsgs_en[61], "Index already created for column %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[62], "Can't drop a primary key index withdrop index.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[63], "Index too large. It can't have more than 65534 nodes.");
      
   // NOT NULL errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[64], "Primary key can't have null.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[65], "Field can't be null: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[66], "A parameter in a where clause can't be null.");

   // Result set errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[67], "ResultSet in invalid record position: %d.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[68], "Invalid value for decimal places: %d. It must range from -1 to 40.");

   // File errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[69], "Can't read from table %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[70], "Can't load leaf node!");
   ASSERT2_EQUALS(Sz, errorMsgs_en[71], "Table is corrupted: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[72], "Table not closed properly: %s."); // juliana@220_2
   ASSERT2_EQUALS(Sz, errorMsgs_en[73], "A properly closed table can't be used in recoverTable(): %s."); // juliana@222_2
	ASSERT2_EQUALS(Sz, errorMsgs_en[74], "Can't find index record position on delete.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[75], "The table format (%d) is incompatible with Litebase version. Please update your tables.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[76], "The table format is not the previous one: %s."); // juliana@220_11
	ASSERT2_EQUALS(Sz, errorMsgs_en[77], "Invalid path: %s."); // juliana@214_1
	ASSERT2_EQUALS(Sz, errorMsgs_en[78], "Invalid file position: %d.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[79], "Database not found.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[80], "An opened table can't be recovered or converted: %s."); // juliana@230_12
   
   // BLOB errors.
   ASSERT2_EQUALS(Sz, errorMsgs_en[81], "The total size of a blob can't be greater then 10 Mb.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[82], "This is not a valid size multiplier.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[83], "A blob type can't be part of a primary key.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[84], "A BLOB column can't be indexed.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[85], "A BLOB can't be in the where clause.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[86], "A BLOB can't be converted to a string.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[87], "Blobs types can't be in ORDER BY or GROUP BY clauses.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[88], "It is not possible to compare BLOBs.");
   ASSERT2_EQUALS(Sz, errorMsgs_en[89], "It is only possible to insert or update a BLOB through prepared statements using setBlob().");

   // Portuguese messages.
	// General errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[0], "Erro: ");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[1], " Pr�ximo � posi��o %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[2], "Erro de sintaxe.");

	// Limit errors.
	ASSERT2_EQUALS(Sz, errorMsgs_pt[3], "N�mero m�ximo de campos diferentes foi alcan�ado.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[4], "N�mero m�ximo da lista de par�metros na cl�usula 'WHERE/HAVING' foi alcan�ado.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[5], "Numero m�ximo de �ndices compostos 32 foi alcan�ado.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[6], "Nome da tabela muito grande: deve ser <= 23");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[7], "O n�mero m�ximo de campos na cl�usula SELECT foi excedido.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[8], "O n�mero m�ximo de campos na cl�usula 'ORDER BY/GROUP BY' foi excedido.");

   // Column errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[9], "Coluna desconhecida %s."); 
	ASSERT2_EQUALS(Sz, errorMsgs_pt[10], "Nome de coluna inv�lido: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[11], "N�mero de coluna inv�lido: %d.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[12], "A(s) coluna(s) a seguir n�o tem (t�m) um ind�ce associado %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[13], "Nome de coluna amb�guo: %s.");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[14], "Coluna n�o encontrada: %s.");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[15], "Nome de coluna duplicado: %s.");

   // Primary key errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[16], "Uma chave prim�ria j� foi definida para esta tabela.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[17], "Tabela n�o tem chave prim�ria.");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[18], "Comando cria uma chave prim�ria duplicada em %s.");

   // Type errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[19], "Tipos incompativeis.");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[20], "Tamanho do campo deve ser um valor inteiro positivo.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[21], "O valor %s n�o � um n�mero v�lido para o tipo desejado: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[22], "Tipo de dados incompat�vel para a chamada de fun��o: %s");

	// Number of fields errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[23], "O n�mero de campos � diferente do n�mero de valores ");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[24], "O n�mero de valores dado n�o coincide com a defini��o da tabela %d.");

   // Default value errors.
	ASSERT2_EQUALS(Sz, errorMsgs_pt[25], "Tamanho do valor padr�o � maior que o tamanho definido para a coluna.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[26], "Uma coluna adicionada declarada como NOT NULL deve ter um valor padr�o n�o nulo.");

	// Driver errors. 
	ASSERT2_EQUALS(Sz, errorMsgs_pt[27], 
                                      "Esta inst�ncia do driver est� fechada e n�o pode ser mais utilizada. Por favor, obtenha uma nova inst�ncia.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[28], "ResultSet j� est� fechado!");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[29], "ResultSetMetaData n�o pode ser usado depois que o ResultSet estiver fechado.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[30], "O id da aplica��o de ter 4 characteres.");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[31], "O incremento deve ser maior do que 0 ou -1.");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[32], "Iterador j� foi fechado.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[33], "Prepared statement fechado. Por favor, prepare-o novamente.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[34], "Par�metro de conex�o inv�lido: %s.");

	// Table errors.
	ASSERT2_EQUALS(Sz, errorMsgs_pt[35], "Nome da tabela n�o encontrado: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[36], "Tabela j� existe: %s.");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[37],  "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de strings diferente.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[38],  "N�o � poss�vel abrir uma tabela com uma conex�o com um tipo de criptografia diferente.");

   // ROWID errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[39], "ROWID n�o pode ser mudado pelo usu�rio!");

   // Prepared Statement errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[40], "Comando SQL n�o retorna um ResultSet.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[41], "Comando SQL n�o executa uma atualiza��o no banco de dados.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[42], "Nem todos os par�metros da consulta tiveram seus valores definidos.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[43], "N�o foi definido um valor para o par�metro %d.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[44], "Invalid parameter index.");
   
   // Rename errors. 
   ASSERT2_EQUALS(Sz, errorMsgs_pt[45], "N�o � poss�vel renomear a tabela. Esta tabela j� existe: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[46], "Coluna j� existe: %s.");

	// Alias errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[47], "Nome de tabela/alias repetido: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[48], "Este alias j� est� sendo utilizado no sql: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[49], "Um alias � necess�rio para colunas com fun��o de agrega��o.");
   
	// Litebase.execute() error.
	ASSERT2_EQUALS(Sz, errorMsgs_pt[50], "Apenas CREATE TABLE e CREATE INDEX s�o permitidos no Litebase.execute()");
   
   // Order by and group by errors.
	ASSERT2_EQUALS(Sz, errorMsgs_pt[51], "Cl�usulas ORDER BY e GROUP BY devem coincidir.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[52], "SQL com cl�usula GROUP BY n�o tem suporte para colunas virtuais.");
   
   // Function errors.
	ASSERT2_EQUALS(Sz, errorMsgs_pt[53], "Todas colunas que n�os�o fun��es de agrega��o na cl�usula SELECT devem estar na cl�usula GROUP BY.");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[54], 
                      "%s n�o � uma fun��o de agrega��o. Todos as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias.");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[55], "N�o � possivel misturar colunas reais e de agrega��o no SELECT sem cl�usula GROUP BY.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[56], "N�o � poss�vel ter fun��es de agrega��o com cl�usula ORDER BY sem cl�usula GROUP BY.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[57], 
	                   "%s n�o foi listado no SELECT. Todas as colunas da cl�usula HAVING devem ser listadas no SELECT utilizando alias.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[58], "Fun��es de agrega��o SUM e AVG n�o s�o usadas com colunas do tipo DATE e DATETIME.");

   // DATE and DATETIME errors.
	ASSERT2_EQUALS(Sz, errorMsgs_pt[59], "Valor n�o � um tipo DATE v�lido: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[60], "Valor n�o � um tipo DATETIME v�lido: %s.");

   // Index error.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[61], "�ndice j� criado para a coluna %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[62], "N�o � poss�vel remover uma chave prim�ria usando drop index.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[63], "�ndice muito grande. Ele n�o pode ter mais do que 65534 n�s.");
      
   // NOT NULL errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[64], "Chave prim�ria n�o pode ter NULL.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[65], "Coluna n�o pode ser NULL: %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[66], "Um par�metro em uma where clause n�o pode ser NULL.");

   // Result set errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[67], "ResultSet em uma posi��o de registro inv�lida %d.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[68], "Valor inv�lido para casas decimais: %d. Deve ficar entre - 1 e 40.");

   // File errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[69], "N�o � poss�vel ler da tabela %s.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[70], "N�o � poss�vel carregar n� folha!");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[71], "Tabela est� corrompida: %s.");
	ASSERT2_EQUALS(Sz, errorMsgs_pt[72], "Tabela n�o foi fechada corretamente: %s."); // juliana@220_2
   ASSERT2_EQUALS(Sz, errorMsgs_pt[73], "Uma tabela fechada corretamente n�o pode ser usada no recoverTable(): %s."); // juliana@222_2
	ASSERT2_EQUALS(Sz, errorMsgs_pt[74], "N�o � poss�vel achar a posi��o de registro no �ndice na exclus�o.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[75], "O formato de tabela (%d) n�o � compat�vel com a vers�o do Litebase. Por favor, atualize suas tabelas.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[76], "O formato de tabela n�o � o anterior: %s."); // juliana@220_11
   ASSERT2_EQUALS(Sz, errorMsgs_pt[77], "Caminho inv�lido: %s."); // juliana@214_1
   ASSERT2_EQUALS(Sz, errorMsgs_pt[78], "Posi��o inv�lida no arquivo: %d.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[79], "Base de dados n�o encontrada.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[80], "Uma tabela aberta n�o pode ser recuperada ou convertida: %s."); // juliana@230_12

   // BLOB errors.
   ASSERT2_EQUALS(Sz, errorMsgs_pt[81], "O tamanho total de um BLOB n�o pode ser maior do que 10 Mb.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[82], "O multiplicador de tamanho n�o � v�lido.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[83], "Um tipo BLOB n�o pode ser parte de uma chave prim�ria.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[84], "Uma coluna do tipo BLOB n�o pode ser indexada.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[85], "Um BLOB n�o pode estar na cl�usula WHERE.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[86], "Um BLOB n�o pode ser convertido em uma string.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[87], "Tipos BLOB n�o podem estar em cl�usulas ORDER BY ou GROUP BY.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[88], "N�o � poss�vel comparar BLOBs.");
   ASSERT2_EQUALS(Sz, errorMsgs_pt[89], "S� � poss�vel inserir ou atualizar um BLOB atrav�s prepared statements usando setBlob().");

finish : ;
}

#endif
