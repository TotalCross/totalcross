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



package litebase;

/**
 * Checks if the record that will be written to the table will be comply to the primary key rule.
 */
class CheckPK extends Monkey
{
   /**
    * The name of the table being checked.
    */
   String tableName;

   /**
    * Climbs on a value.
    *
    * @param value Ignored. If the value is climbed, there is a primary key violation.
    */
   void onValue(Value value)
   {
      throw new PrimaryKeyViolationException(LitebaseMessage.getMessage(LitebaseMessage.ERR_STATEMENT_CREATE_DUPLICATED_PK) + tableName);
   }
}
