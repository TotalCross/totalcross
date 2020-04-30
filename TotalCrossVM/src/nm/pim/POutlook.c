// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



#include "tcvm.h"

//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_NewContact(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String NewContact();
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_GetAllContacts(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String GetAllContacts();
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_ViewAllContacts(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String ViewAllContacts();
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_getIContactString_s(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String getIContactString(String oid);
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_removeIContact_s(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static void removeIContact(String oid);
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_editIContact_sssssssss(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static int editIContact(String oid, String title, String firstName, String middleName, String lastName, String suffix, String jobTitle, String department, String company, String workTel, String workTel2, String homeTel, String homeTel2, String mobileTel, String pager, String carTel, String workFax, String homeFax, String assistantTel, String radioTel, String email, String email2, String email3, String webPage, String workStreet, String workCity, String workState, String workZip, String workCountry, String officeLoc, String homeStreet, String homeCity, String homeState, String homeZip, String homeCountry, String otherStreet, String otherCity, String otherState, String otherZip, String otherCountry, String categories, String assistant, String birthdayAsDateHelperGenericDateString, String anniversaryAsDateHelperGenericDateString, String spouse, String children, String note);
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_newAppointment(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String newAppointment();
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_GetAllAppointments(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String GetAllAppointments();
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_ViewAllAppointments(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String ViewAllAppointments();
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_getIAppointmentString_(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String getIAppointmentString(String oid);
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_removeIAppointment_s(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static void removeIAppointment(String oid);
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_editIAppointment_sssss(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static int editIAppointment(String oid, String subject, String location, String categories, String reminderSoundFile, String note, String startDate, String endDate, String duration, String meetingStatus, String sensitivity, String busyStatus, String reminderOptions, String reminderMinutesBeforeStart, String allDayEvent, String isRecurring, String reminderIsSet, String recurrenceType, String occurrences, String interval, String dayOfWeek, String dayOfMonth, String weekOfMonth, String monthOfYear, String patternStartDate, String patternEndDate, String startTime, String endTime, String noEndDate, String recipients);
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_ViewAllTasks(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String ViewAllTasks();
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_GetAllTasks(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String GetAllTasks();
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_newTask(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String newTask();
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_getITaskString_s(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static String getITaskString(String oid);
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_removeITask_s(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static void removeITask(String oid);
{
   p = 0;
}
//////////////////////////////////////////////////////////////////////////
TC_API void tpcbIPOIC_editITask_ssssssssssss(NMParams p) // totalcross/pim/ce/builtin/IPOutlookItemCollection native public static int editITask(String restriction, String subject, String categories, String startDate, String dueDate, String importance, String complete, String isRecurring, String duration, String recurrenceType, String occurrences, String interval, String dayOfWeek, String dayOfMonth, String weekOfMonth, String monthOfYear, String patternStartDate, String patternEndDate, String startTime, String endTime, String noEndDate, String sensitivity, String teamTask, String reminderIsSet, String reminderOptions, String reminderTime, String note);
{
   p = 0;
}

#ifdef ENABLE_TEST_SUITE
#include "POutlook_test.h"
#endif
