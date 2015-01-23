/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2003 Fabian Kroeher                                            *
 *  Copyright (C) 2003-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.pim.ce.builtin;
/**
 * represents the eVC++ interface IPOutlookItemCollection of the Pocket Outlook Object Model
 * TODO this class is obsolete; put the methods in the I*s classes and kick it
 * @author Fabian Kroeher
 *
 */
public class IPOutlookItemCollection
{
   /**
    * creates new IContact on the device with all fields empty and returns ID
    * @return a new IContact object with only the id field set
    * @throws RuntimeException If it is not possible to create the contact
    */
   public static IContact createIContact()
   {
      String result = NewContact();
      if (result.equals("0"))
         throw new RuntimeException("Could not create the task.");
      StringExt nativeString = new StringExt(result);
      return new IContact(nativeString);
   }
   public static String NewContact()
   {
      return "";
   }
   native public static String NewContact4D();

   /**
    * gets all the IContacts from the device and returns them in an IContacts object
    * @return an IContacts object containing all the IContacts of the device
    */
   public static IContacts getIContacts()
   {
      return new IContacts(new StringExt(GetAllContacts()));
   }
   public static String GetAllContacts()
   {
      return "";
   }
   native public static String GetAllContacts4D();

   /**
    * gets only the ids of all the IContacts on the device
    * @return an IContacts object containing all the IContacts of the device with only the id field set
    */
   public static IContacts viewIContacts()
   {
      return new IContacts(new StringExt(ViewAllContacts()));
   }
   public static String ViewAllContacts()
   {
      return "";
   }
   native public static String ViewAllContacts4D();

   /**
    * gets the native IContact String from the device with all the IContacts matching the oid
    * normally, this method is only used by the IContact object when it updates itself with the full data from the device
    * @param oid the object id
    * @return the IContact as a String separated by the defined separators D1_SEP, D2_SEP ...
    */
   public static String getIContactString(String oid)
   {
      return "";
   }
   native public static String getIContactString4D(String oid);

   /**
    * removes all the IContacts matching to the oid from the device
    * normally, this method is only used by the IContact object when it removes itself
    * @param oid the object id
    */
   public static void removeIContact(String oid)
   {
   }
   native public static void removeIContact4D(String oid);

   /**
    * edits all the IContacts matching to the oid on the device
    * @param oid the object id
    * @return the number of edited IContacts
    */
   public static int editIContact(String oid, String title, String firstName, String middleName, String lastName, String suffix, String jobTitle, String department, String company, String workTel, String workTel2, String homeTel, String homeTel2, String mobileTel, String pager, String carTel, String workFax, String homeFax, String assistantTel, String radioTel, String email, String email2, String email3, String webPage, String workStreet, String workCity, String workState, String workZip, String workCountry, String officeLoc, String homeStreet, String homeCity, String homeState, String homeZip, String homeCountry, String otherStreet, String otherCity, String otherState, String otherZip, String otherCountry, String categories, String assistant, String birthdayAsDateHelperGenericDateString, String anniversaryAsDateHelperGenericDateString, String spouse, String children, String note)
   {
      return 1;
   }
   native public static int editIContact4D(String oid, String title, String firstName, String middleName, String lastName, String suffix, String jobTitle, String department, String company, String workTel, String workTel2, String homeTel, String homeTel2, String mobileTel, String pager, String carTel, String workFax, String homeFax, String assistantTel, String radioTel, String email, String email2, String email3, String webPage, String workStreet, String workCity, String workState, String workZip, String workCountry, String officeLoc, String homeStreet, String homeCity, String homeState, String homeZip, String homeCountry, String otherStreet, String otherCity, String otherState, String otherZip, String otherCountry, String categories, String assistant, String birthdayAsDateHelperGenericDateString, String anniversaryAsDateHelperGenericDateString, String spouse, String children, String note);

   /**
    * creates new IAppointment on the device with name NULL and returns ID
    * @return the newly created IAppointment (it is recommend to set its fields next and then to save() it again on the device
    * @throws RuntimeException If it is not possible to create the appointment
    */
   public static IAppointment createIAppointment()
   {
      String result = newAppointment();
      StringExt nativeString = new StringExt(result);
      if (result.equals("0"))
         throw new RuntimeException("Could not create the task.");
      return new IAppointment(nativeString);
   }
   public static String newAppointment()
   {
      return "";
   }
   native public static String newAppointment4D();

   /**
    * gets all the Appointments from the device and returns them in an IAppointments Object
    * @return an IAppointments Object containig all the IAppointments of the device
    */
   public static IAppointments getIAppointments()
   {
      return new IAppointments(new StringExt(GetAllAppointments()));
   }
   public static String GetAllAppointments()
   {
      return "";
   }
   native public static String GetAllAppointments4D();

   /**
    * gets only the ids of all the IAppointments on the device
    * @return an IAppointments object containing all the IAppointments of the device with only the id field set
    */
   public static IAppointments viewIAppointments()
   {
      return new IAppointments(new StringExt(ViewAllAppointments()));
   }
   public static String ViewAllAppointments()
   {
      return "";
   }
   native public static String ViewAllAppointments4D();

   /**
    * gets the native IAppointment String from the device with all the IAppointments matching the oid
    * normally, this method is only used by the IAppointment object when it updates itself with the full data from the device
    * @param oid the object id
    * @return the IAppointment as a String separated by the defined separators D1_SEP, D2_SEP ...
    */
   public static String getIAppointmentString(String oid)
   {
      return "";
   }
   native public static String getIAppointmentString4D(String oid);

   /**
    * removes all the IAppointments matching to the oid from the device
    * normally, this method is only used by the IAppointment object when it removes itself
    * @param oid the object id
    */
   public static void removeIAppointment(String oid)
   {
   }
   native public static void removeIAppointment4D(String oid);

   /**
    * edits all the IAppointments matching to the oid on the device
    * @param oid the object id
    * @return the number of edited IAppointments
    */
   public static int editIAppointment(String oid, String subject, String location, String categories, String reminderSoundFile, String note, String startDate, String endDate, String duration, String meetingStatus, String sensitivity, String busyStatus, String reminderOptions, String reminderMinutesBeforeStart, String allDayEvent, String isRecurring, String reminderIsSet, String recurrenceType, String occurrences, String interval, String dayOfWeek, String dayOfMonth, String weekOfMonth, String monthOfYear, String patternStartDate, String patternEndDate, String startTime, String endTime, String noEndDate, String recipients)
   {
      return 1;
   }
   native public static int editIAppointment4D(String oid, String subject, String location, String categories, String reminderSoundFile, String note, String startDate, String endDate, String duration, String meetingStatus, String sensitivity, String busyStatus, String reminderOptions, String reminderMinutesBeforeStart, String allDayEvent, String isRecurring, String reminderIsSet, String recurrenceType, String occurrences, String interval, String dayOfWeek, String dayOfMonth, String weekOfMonth, String monthOfYear, String patternStartDate, String patternEndDate, String startTime, String endTime, String noEndDate, String recipients);

   /**
    * gets only the ids of all the ITasks on the device
    * @return an ITasks object containing all the ITasks of the device with only the id field set
    */
   public static ITasks viewITasks()
   {
      return new ITasks(new StringExt(ViewAllTasks()));
   }
   public static String ViewAllTasks()
   {
      return "";
   }
   native public static String ViewAllTasks4D();

   /**
    * gets ale the ITasks from the device and returns them in an ITasks object
    * @return an ITasks object containing all the ITasks of the device
    */
   public static ITasks getITasks()
   {
      return new ITasks(new StringExt(GetAllTasks()));
   }
   public static String GetAllTasks()
   {
      return "";
   }
   native public static String GetAllTasks4D();

   /**
    * creates new ITask on the device with all fields empty and returns ID
    * @return a new ITask object with only the id field set
    * @throws RuntimeException If it is not possible to create the task
    */
   public static ITask createITask()
   {
      String result = newTask();
      if (result.equals("0"))
         throw new RuntimeException("Could not create the task.");
      StringExt nativeString = new StringExt(result);
      return new ITask(nativeString);
   }

   public static String newTask()
   {
      return "184555400";
   }
   native public static String newTask4D();

   /**
    * gets the native ITask String from the device with the ITask matching the oid
    * normally, this method is only used by the ITask object when it updates itself with the full data from the device
    * @param oid the object id
    * @return the ITask as a String separated by the defined separators D1_SEP, D2_SEP ...
    */
   public static String getITaskString(String oid)
   {
      return "";
   }
   native public static String getITaskString4D(String oid);

   /**
    * removes all the ITasks matching to the oid from the device
    * normally, this method is only used by the ITask object when it removes itself
    * @param oid the object id
    */
   public static void removeITask(String oid)
   {
   }
   native public static void removeITask4D(String oid);

   /**
    * @return the number of edited tasks
    */
   public static int editITask(String restriction, String subject, String categories, String startDate, String dueDate, String importance, String complete, String isRecurring, String duration, String recurrenceType, String occurrences, String interval, String dayOfWeek, String dayOfMonth, String weekOfMonth, String monthOfYear, String patternStartDate, String patternEndDate, String startTime, String endTime, String noEndDate, String sensitivity, String teamTask, String reminderIsSet, String reminderOptions, String reminderTime, String note)
   {
      return 1;
   }
   native public static int editITask4D(String restriction, String subject, String categories, String startDate, String dueDate, String importance, String complete, String isRecurring, String duration, String recurrenceType, String occurrences, String interval, String dayOfWeek, String dayOfMonth, String weekOfMonth, String monthOfYear, String patternStartDate, String patternEndDate, String startTime, String endTime, String noEndDate, String sensitivity, String teamTask, String reminderIsSet, String reminderOptions, String reminderTime, String note);
}
