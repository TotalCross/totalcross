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
// SQLiteOpenMode.java
// Since: Dec 8, 2009
//
// $URL$ 
// $Author$
//--------------------------------------
package totalcross.db.sqlite;

import totalcross.util.Enum;

/**
 * Database file open modes of SQLite.
 * 
 * See also http://sqlite.org/c3ref/open.html
 * 
 * @author leo
 * 
 */
public class SQLiteOpenMode extends Enum
{
    public static final SQLiteOpenMode READONLY       = new SQLiteOpenMode(0x00000001,"READONLY"); /* Ok for int SQLITE3_open_v2() */
    public static final SQLiteOpenMode READWRITE      = new SQLiteOpenMode(0x00000002,"READWRITE"); /* Ok for int SQLITE3_open_v2() */
    public static final SQLiteOpenMode CREATE         = new SQLiteOpenMode(0x00000004,"CREATE"); /* Ok for int SQLITE3_open_v2() */
    public static final SQLiteOpenMode DELETEONCLOSE  = new SQLiteOpenMode(0x00000008,"DELETEONCLOSE"); /* VFS only */
    public static final SQLiteOpenMode EXCLUSIVE      = new SQLiteOpenMode(0x00000010,"EXCLUSIVE"); /* VFS only */
    public static final SQLiteOpenMode OPEN_URI       = new SQLiteOpenMode(0x00000040,"OPEN_URI"); /* Ok for sqlite3_open_v2() */
    public static final SQLiteOpenMode OPEN_MEMORY    = new SQLiteOpenMode(0x00000080,"OPEN_MEMORY"); /* Ok for sqlite3_open_v2() */
    public static final SQLiteOpenMode MAIN_DB        = new SQLiteOpenMode(0x00000100,"MAIN_DB"); /* VFS only */
    public static final SQLiteOpenMode TEMP_DB        = new SQLiteOpenMode(0x00000200,"TEMP_DB"); /* VFS only */
    public static final SQLiteOpenMode TRANSIENT_DB   = new SQLiteOpenMode(0x00000400,"TRANSIENT_DB"); /* VFS only */
    public static final SQLiteOpenMode MAIN_JOURNAL   = new SQLiteOpenMode(0x00000800,"MAIN_JOURNAL"); /* VFS only */
    public static final SQLiteOpenMode TEMP_JOURNAL   = new SQLiteOpenMode(0x00001000,"TEMP_JOURNAL"); /* VFS only */
    public static final SQLiteOpenMode SUBJOURNAL     = new SQLiteOpenMode(0x00002000,"SUBJOURNAL"); /* VFS only */
    public static final SQLiteOpenMode MASTER_JOURNAL = new SQLiteOpenMode(0x00004000,"MASTER_JOURNAL"); /* VFS only */
    public static final SQLiteOpenMode NOMUTEX        = new SQLiteOpenMode(0x00008000,"NOMUTEX"); /* Ok for int SQLITE3_open_v2() */
    public static final SQLiteOpenMode FULLMUTEX      = new SQLiteOpenMode(0x00010000,"FULLMUTEX"); /* Ok for int SQLITE3_open_v2() */
    public static final SQLiteOpenMode SHAREDCACHE    = new SQLiteOpenMode(0x00020000,"SHAREDCACHE"); /* Ok for int SQLITE3_open_v2() */
    public static final SQLiteOpenMode PRIVATECACHE   = new SQLiteOpenMode(0x00040000,"PRIVATECACHE"); /* Ok for sqlite3_open_v2() */

    private SQLiteOpenMode(int flag, String name) 
    {
       super(flag, name);
    }
}
