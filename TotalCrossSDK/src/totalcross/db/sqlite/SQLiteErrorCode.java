/*--------------------------------------------------------------------------
 *  Copyright 2009 Taro L. Saito
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// sqlite-jdbc Project
//
// SQLiteErrorCode.java
// Since: Apr 21, 2009
//
// $URL$ 
// $Author$
//--------------------------------------
package totalcross.db.sqlite;

import totalcross.util.Enum;


/**
 * SQLite3 error code
 * 
 * @author leo
 * @see <a href="http://www.sqlite.org/c3ref/c_abort.html">http://www.sqlite.org/c3ref/c_abort.html</a>
 * 
 */
public class SQLiteErrorCode extends Enum
{
   public static final SQLiteErrorCode UNKNOWN_ERROR     = new SQLiteErrorCode(-1, "unknown error");
   public static final SQLiteErrorCode SQLITE_OK         = new SQLiteErrorCode(0, "Successful result");
   public static final SQLiteErrorCode SQLITE_ERROR      = new SQLiteErrorCode(1, "SQL error or missing database");
   public static final SQLiteErrorCode SQLITE_INTERNAL   = new SQLiteErrorCode(2, "Internal logic error in SQLite");
   public static final SQLiteErrorCode SQLITE_PERM       = new SQLiteErrorCode(3, " Access permission denied");
   public static final SQLiteErrorCode SQLITE_ABORT      = new SQLiteErrorCode(4, " Callback routine requested an abort");
   public static final SQLiteErrorCode SQLITE_BUSY       = new SQLiteErrorCode(5, " The database file is locked");
   public static final SQLiteErrorCode SQLITE_LOCKED     = new SQLiteErrorCode(6, " A table in the database is locked");
   public static final SQLiteErrorCode SQLITE_NOMEM      = new SQLiteErrorCode(7, " A malloc = new SQLiteErrorCode() failed");
   public static final SQLiteErrorCode SQLITE_READONLY   = new SQLiteErrorCode(8, " Attempt to write a readonly database");
   public static final SQLiteErrorCode SQLITE_INTERRUPT  = new SQLiteErrorCode(9, " Operation terminated by sqlite3_interrupt = new SQLiteErrorCode()");
   public static final SQLiteErrorCode SQLITE_IOERR      = new SQLiteErrorCode(10, " Some kind of disk I/O error occurred");
   public static final SQLiteErrorCode SQLITE_CORRUPT    = new SQLiteErrorCode(11, " The database disk image is malformed");
   public static final SQLiteErrorCode SQLITE_NOTFOUND   = new SQLiteErrorCode(12, " NOT USED. Table or record not found");
   public static final SQLiteErrorCode SQLITE_FULL       = new SQLiteErrorCode(13, " Insertion failed because database is full");
   public static final SQLiteErrorCode SQLITE_CANTOPEN   = new SQLiteErrorCode(14, " Unable to open the database file");
   public static final SQLiteErrorCode SQLITE_PROTOCOL   = new SQLiteErrorCode(15, " NOT USED. Database lock protocol error");
   public static final SQLiteErrorCode SQLITE_EMPTY      = new SQLiteErrorCode(16, " Database is empty");
   public static final SQLiteErrorCode SQLITE_SCHEMA     = new SQLiteErrorCode(17, " The database schema changed");
   public static final SQLiteErrorCode SQLITE_TOOBIG     = new SQLiteErrorCode(18, " String or BLOB exceeds size limit");
   public static final SQLiteErrorCode SQLITE_CONSTRAINT = new SQLiteErrorCode(19, " Abort due to constraint violation");
   public static final SQLiteErrorCode SQLITE_MISMATCH   = new SQLiteErrorCode(20, " Data type mismatch");
   public static final SQLiteErrorCode SQLITE_MISUSE     = new SQLiteErrorCode(21, " Library used incorrectly");
   public static final SQLiteErrorCode SQLITE_NOLFS      = new SQLiteErrorCode(22, " Uses OS features not supported on host");
   public static final SQLiteErrorCode SQLITE_AUTH       = new SQLiteErrorCode(23, " Authorization denied");
   public static final SQLiteErrorCode SQLITE_FORMAT     = new SQLiteErrorCode(24, " Auxiliary database format error");
   public static final SQLiteErrorCode SQLITE_RANGE      = new SQLiteErrorCode(25, " 2nd parameter to sqlite3_bind out of range");
   public static final SQLiteErrorCode SQLITE_NOTADB     = new SQLiteErrorCode(26, " File opened that is not a database file");
   public static final SQLiteErrorCode SQLITE_ROW        = new SQLiteErrorCode(100, " sqlite3_step = new SQLiteErrorCode() has another row ready");
   public static final SQLiteErrorCode SQLITE_DONE       = new SQLiteErrorCode(101, " sqlite3_step = new SQLiteErrorCode() has finished executing");

    /**
     * Constructor that applies error code and message.
     * @param code Error code.
     * @param message Message for the error.
     */
    private SQLiteErrorCode(int code, String message)
    {
       super(code, message);
    }

    /**
     * @param errorCode Error code.
     * @return Error message.
     */
    public static SQLiteErrorCode getErrorCode(int errorCode)
    {
       return (SQLiteErrorCode)get(SQLiteErrorCode.class, errorCode, UNKNOWN_ERROR);
    }

    /**
     * @see java.lang.Enum#toString()
     */
    //@Override
    public String toString()
    {
        return "["+value+"] "+ name;
    }
}
