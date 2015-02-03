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
import totalcross.pim.addressbook.*;
import totalcross.pim.datebook.*;
import totalcross.pim.todobook.*;
import totalcross.pim.*;
import totalcross.util.*;
/**
 * provides constants for the POOM wrapper layer, this means fieldnames, templates etc
 * @author Fabian Kroeher
 *
 */
public class Constant
{
   public static final String datasetSep = "|~~|";
   public static final String d1Sep = "|1~";
   public static final String d2Sep = "|2~";
   public static final String d3Sep = "|3~";
   private static Vector vAppointmentFields;
   private static Vector vContactFields;
   private static Vector vTaskFields;
   private static Vector vAddressFieldTemplates = new Vector();
   private static Vector vToDoFieldTemplates = new Vector();
   private static Vector vDateFieldTemplates = new Vector();
   static
   {
      initIAppointmentFields();
      initIContactFields();
      initITaskFields();
      initAddressFieldTemplates();
      initToDoFieldTemplates();
      initDateFieldTemplates();
   }
   /**
    * @return the size of the dateFieldTemplates
    */
   public static int dateFieldTemplates()
   {
      return vDateFieldTemplates.size();
   }
   /**
    * clones the dateFieldTemplate at the given position and returns it
    * @param position of the dateFieldTemplate in the dateFieldTemplates Vector
    * @return a new Object of class DateField which has been cloned from the DateField at given position
    */
   public static DateField dateFieldTemplate(int position)
   {
      return (DateField)((DateField)vDateFieldTemplates.items[position]).clone();
   }
   /**
    * @return a new Vector with cloned copies of the dateFieldTemplates in it
    */
   public static Vector getDateFieldTemplates()
   {
      Vector retVal = new Vector();
      int n = vDateFieldTemplates.size();
      for (int i = 0; i < n; i++)
         retVal.addElement(((DateField)vDateFieldTemplates.items[i]).clone());
      return retVal;
   }
   /**
    * @return the size of the toDoFieldTemplates Vector
    */
   public static int toDoFieldTemplates()
   {
      return vToDoFieldTemplates.size();
   }
   /**
    * clones the toDoFieldTemplate at the given position and returns it
    * @param position of the toDoFieldTemplate in the toDoFieldTemplates Vector
    * @return a new Object of class ToDoField which has been cloned from the ToDoField at given position
    */
   public static ToDoField toDoFieldTemplate(int position)
   {
      return (ToDoField)((ToDoField)vToDoFieldTemplates.items[position]).clone();
   }
   /**
    * @return a new Vector with cloned copies of the toDoFieldTemplates in it
    */
   public static Vector getToDoFieldTemplates()
   {
      Vector retVal = new Vector();
      int n = vToDoFieldTemplates.size();
      for (int i = 0; i < n; i++)
         retVal.addElement(((ToDoField)vToDoFieldTemplates.items[i]).clone());
      return retVal;
   }
   /**
    * @return the size of the addressFieldTemplate Vector
    */
   public static int addressFieldTemplates()
   {
      return vAddressFieldTemplates.size();
   }
   /**
    * clones the addressFieldTemplate at the given position and returns it
    * @param position of the addressFieldTemplate in the addressFieldTemplates Vector
    * @return a new Object of class AddressField which has been cloned from the AddressField at given position
    */
   public static AddressField addressFieldTemplate(int position)
   {
      return (AddressField)((AddressField)vAddressFieldTemplates.items[position]).clone();
   }
   /**
    * @return a new Vector with cloned copies of the addressFieldTemplates in it
    */
   public static Vector getAddressFieldTemplates()
   {
      Vector retVal = new Vector();
      int n = vAddressFieldTemplates.size();
      for (int i = 0; i < n; i++)
         retVal.addElement(((AddressField)vAddressFieldTemplates.items[i]).clone());
      return retVal;
   }
   /**
    * @param position
    * @return the name of the IAppointment field at the given position, "undefined" if the position is too high
    */
   public static String iAppointmentFields(int position)
   {
      try
      {
         return (String)vAppointmentFields.items[position];
      }
      catch(ArrayIndexOutOfBoundsException aioobe)
      {
         return "undefined" + position;
      }
   }
   /**
    * @return the size of the iAppointmentFields Vector
    */
   public static int iAppointmentFields()
   {
      return vAppointmentFields.size();
   }
   /**
    * @param position
    * @return the name of the IContact field at the given position, "undefined" of the position is too high
    */
   public static String iContactFields(int position)
   {
      try
      {
         return (String)vContactFields.items[position];
      }
      catch(ArrayIndexOutOfBoundsException aioobe)
      {
         return "undefined" + position;
      }
   }
   /**
    * @return the size of the iContactField Vector
    */
   public static int iContactFields()
   {
      return vContactFields.size();
   }
   /**
    * @param position
    * @return the name of the ITask field at the given position, "undefined" if the number is too high
    */
   public static String iTaskFields(int position)
   {
      try
      {
         return (String)vTaskFields.items[position];
      }
      catch(ArrayIndexOutOfBoundsException aioobe)
      {
         return "undefined" + position;
      }
   }
   /**
    * @return the size of the iTaskFields Vector
    */
   public static int iTaskFields()
   {
      return vTaskFields.size();
   }
   /**
    * this method initialises the iTaskFields Vector; it contains all the field names of an ITask;
    * the order of the field names determines how the fields are read out of the String that comes
    * from CeIoBuiltIn.dll; e.g. the first field name is (String)id - this means that the first value in the
    * String from CeIoBuiltIn.dll will be stored as the id of the ITask.
    * So DO NOT MESS WITH THIS unless you are sure of what you are doing
    */
   private static void initITaskFields()
   {
      // this determines how the fields of ITask are read out of the native String
      String []fields =
      {
         "(String)id",
         "(String)subject", // mapped to SUMMARY
         "(String)categories", // mapped to CATEGORIES
         "(IDate)startDate", // mapped to DTSTART
         "(IDate)dueDate", // mappped to DUE
         "(String)importance", // mapped to PRIORITY
         "(String)completed", // mapped to COMPLETED - BOOLEAN
         "(IRecurrence)recurrence", // mapped to RRULE
         "(String)sensitivity", // mapped to CLASSIFICATION
         "(String)teamTask", // mapped to X-TEAMTASK
         "(String)reminderSet", // mapped to X-REMINDERSET
         "(String)reminderOptions", // mapped to X-REMINDER
         "(IDate)reminderTime", // mapped to X-REMINDER
         "(String)note", // mapped to DESCRIPTION
      };
      vTaskFields = new Vector(fields);
   }
   /**
    * this method initialises the todoFieldTemplates; this vector determines how the pimAL fields will be
    * mapped to and from the IAppointment fields
    */
   private static void initToDoFieldTemplates()
   {
      // mapping of the vCal SUMMARY property
      String[] subject =
      {
         "(String)subject"
      };
      String[] subjectOptions = {};
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.SUMMARY, subjectOptions, subject));
      // mapping of the vCal CATEGORIES property
      String[] categories =
      {
         "(String)categories"
      };
      String[] categoriesOptions = {};
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.CATEGORIES, categoriesOptions, categories));
      // mapping of the vCal DTSTART property
      String[] startDate =
      {
         "(IDate)startDate"
      };
      String[] startDateOptions = {};
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.DTSTART, startDateOptions, startDate));
      // mapping of the vCal DUE property
      String[] dueDate =
      {
         "(IDate)dueDate"
      };
      String[] dueDateOptions = {};
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.DUE, dueDateOptions, dueDate));
      // mapping of the vCal PRIORITY property
      String[] importance =
      {
         "(String)importance"
      };
      String[] importanceOptions = {};
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.PRIORITY, importanceOptions, importance));
      // mapping of the vCal COMPLETED property
      String[] completed =
      {
         "(String)completed"
      };
      String[] completedOptions = {};
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.COMPLETED, completedOptions, completed));
      // mapping of the vCal RRULE property
      String[] recurrence =
      {
         "(IRecurrence)recurrence"
      };
      String[] recurrenceOptions = {};
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.RRULE, recurrenceOptions, recurrence));
      // mapping of the vCal DESCRIPTION property
      String[] description =
      {
         "(String)note"
      };
      String[] descriptionOptions = {};
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.DESCRIPTION, descriptionOptions, description));
      // mapping of the X-REMINDER property
      String[] reminder =
      {
         "(String)reminderOptions", "(IDate)reminderTime"
      };
      String[] reminderOptions =
      {
         "type=X-REMINDER"
      };
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.X, reminderOptions, reminder));
      // mapping of the X-TEAMTASK property
      String[] teamTask =
      {
         "(String)teamTask"
      };
      String[] teamTaskOptions =
      {
         "type=X-TEAMTASK"
      };
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.X, teamTaskOptions, teamTask));

//    GUICH: not parametized on PocketPC - is it on Palm?

      // mapping of the X-COMPLETE property
      String[] complete =
      {
         "(String)complete"
      };
      String[] completeOptions =
      {
         "type=X-COMPLETE"
      };
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.X, completeOptions, complete));
      // mapping of the vCal CLASSIFICATION property
      String[] sensitivity =
      {
         "(String)sensitivity"
      };
      String[] sensitivityOptions = {};
      vToDoFieldTemplates.addElement(new ToDoField(VCalField.CLASSIFICATION, sensitivityOptions, sensitivity));
   }
   /**
    * this method initialises the iContactFields Vector; it contains all the field names of an IContact;
    * the order of the field names determines how the fields are read out of the String that comes
    * from CeIoBuiltIn.dll; e.g. the first field name is (String)id - this means that the first value in the
    * String from CeIoBuiltIn.dll will be stored as the id of the IContact.
    * So DO NOT MESS WITH THIS unless you are sure of what you are doing
    */
   private static void initIContactFields()
   {
      // this determines how the fields of IContact are read out of the native String
      // the comments after each field indicate to which vCard-Property the field is mapped
      String []fields =
      {
         "(String)id",
         "(String)title", // mapped to N
         "(String)firstName", // mapped N
         "(String)middleName", // mapped to N
         "(String)lastName", // mapped to N
         "(String)suffix", // mapped to N
         "(String)jobTitle", // mapped to TITLE
         "(String)department", // mapped to ORG
         "(String)company", // mapped to ORG
         "(String)workTel1", // mapped to TEL
         "(String)workTel2", // mapped to TEL
         "(String)homeTel1", // mapped to TEL
         "(String)homeTel2", // mapped to TEL
         "(String)mobileTel", // mapped to TEL
         "(String)pager", // mapped to TEL
         "(String)carTel", // mapped to TEL
         "(String)workFax", // mapped to TEL
         "(String)homeFax", // mapped to TEL
         "(String)assistantTel", // mapped to X-ASSISTANTTEL
         "(String)radioTel", // mapped to TEL
         "(String)email1", // mapped to EMAIL
         "(String)email2", // mapped to EMAIL
         "(String)email3", // mapped to EMAIL
         "(String)webPage", // mapped to URL
         "(String)workStreet", // mapped to ADR
         "(String)workCity", // mapped to ADR
         "(String)workState", // mapped to ADR
         "(String)workZip", // mapped to ADR
         "(String)workCountry", // mapped to ADR
         "(String)officeLoc", // mapped to X-OFFICE_LOC
         "(String)homeStreet", // mapped to ADR
         "(String)homeCity", // mapped to ADR
         "(String)homeState", // mapped to ADR
         "(String)homeZip", // mapped to ADR
         "(String)homeCountry", // mapped to ADR
         "(String)otherStreet", // mapped to ADR
         "(String)otherCity", // mapped to ADR
         "(String)otherState", // mapped to ADR
         "(String)otherZip", // mapped to ADR
         "(String)otherCountry", // mapped to ADR
         "(String)categories", // mapped to CATEGORIES
         "(String)assistant", // mapped to X-ASSISTANT
         "(IDate)birthday", // mapped to BDAY
         "(IDate)anniversary", // mapped to X-ANNIVERSARY
         "(String)spouse", // mapped to X-SPOUSE
         "(String)children", // mapped to X-CHILDREN
         "(String)note", // mapped to NOTE
      };
      vContactFields = new Vector(fields);
   }
   /**
    * this method initialises the addressFieldTemplates; this vector determines how the pimAL fields will be
    * mapped to and from the IAppointment fields
    */
   private static void initAddressFieldTemplates()
   {
      // mapping of the vCard N property
      // note that the title in Outlook means the Name prefix like Mr. or Mrs.
      String[] name =
      {
         "(String)lastName", "(String)firstName", "(String)middleName",
         "(String)title", "(String)suffix"
      };
      String[] nameOptions = {};
      vAddressFieldTemplates.addElement(new AddressField(AddressField.N, nameOptions, name));
      // mapping of the vCard TITLE property
      // note that the vCard title means jobtitle in Outlook
      String[] title =
      {
         "(String)jobTitle"
      };
      String[] titleOptions = {};
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TITLE, titleOptions, title));
      // mapping of the vCard ORG property
      String[] org =
      {
         "(String)company", "(String)department"
      };
      String[] orgOptions = {};
      vAddressFieldTemplates.addElement(new AddressField(AddressField.ORG, orgOptions, org));
      // mapping the X-OFFICE_LOCATION
      String[] officeloc =
      {
         "(String)officeLoc"
      };
      String[] officelocOptions =
      {
         "type=X-OFFICE_LOCATION"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.X, officelocOptions, officeloc));
      // mapping of the vCards TEL property
      // note that the first tel of each type (homeTel, workTel ...) gets a PREF
      String[] worktel1 =
      {
         "(String)workTel1"
      };
      String[] worktel1Options =
      {
         "type=PREF", "type=WORK", "type=VOICE"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TEL, worktel1Options, worktel1));
      String[] worktel2 =
      {
         "(String)workTel2"
      };
      String[] worktel2Options =
      {
         "type=WORK", "type=VOICE"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TEL, worktel2Options, worktel2));
      String[] workfax =
      {
         "(String)workFax"
      };
      String[] workfaxOptions =
      {
         "type=PREF", "type=WORK", "type=FAX"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TEL, workfaxOptions, workfax));
      String[] hometel1 =
      {
         "(String)homeTel1"
      };
      String[] hometel1Options =
      {
         "type=PREF", "type=HOME", "type=VOICE"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TEL, hometel1Options, hometel1));
      String[] hometel2 =
      {
         "(String)homeTel2"
      };
      String[] hometel2Options =
      {
         "type=HOME", "type=VOICE"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TEL, hometel2Options, hometel2));
      String[] homefax =
      {
         "(String)homeFax"
      };
      String[] homefaxOptions =
      {
         "type=PREF", "type=HOME", "type=FAX"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TEL, homefaxOptions, homefax));
      String[] mobiletel =
      {
         "(String)mobileTel"
      };
      String[] mobiletelOptions =
      {
         "type=PREF", "type=VOICE", "type=MSG", "type=CELL"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TEL, mobiletelOptions, mobiletel));
      String[] pager =
      {
         "(String)pager"
      };
      String[] pagerOptions =
      {
         "type=PREF", "type=PAGER"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TEL, pagerOptions, pager));
      String[] cartel =
      {
         "(String)carTel"
      };
      String[] cartelOptions =
      {
         "type=PREF", "type=CELL", "type=VOICE"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TEL, cartelOptions, cartel));
      String[] radiotel =
      {
         "(String)radioTel"
      };
      String[] radiotelOptions =
      {
         "type=PREF", "type=CELL"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.TEL, radiotelOptions, radiotel));
      // mapping of the vCard EMAIL property
      // note that first email is PREF
      String[] email1 =
      {
         "(String)email1"
      };
      String[] email1Options =
      {
         "type=PREF"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.EMAIL, email1Options, email1));
      String[] email2 =
      {
         "(String)email2"
      };
      String[] email2Options = {};
      vAddressFieldTemplates.addElement(new AddressField(AddressField.EMAIL, email2Options, email2));
      String[] email3 =
      {
         "(String)email3"
      };
      String[] email3Options = {};
      vAddressFieldTemplates.addElement(new AddressField(AddressField.EMAIL, email3Options, email3));
      // mapping of tthe vCard URL property
      String[] webpage =
      {
         "(String)webPage"
      };
      String[] webpageOptions = {};
      vAddressFieldTemplates.addElement(new AddressField(AddressField.URL, webpageOptions, webpage));
      // mapping of the vCards ADR property
      // note that HOME and WORK ADR is pref, not the OTHER ADR
      String[] workadr =
      {
         "", "", "(String)workStreet", "(String)workCity", "(String)workState",
         "(String)workZip", "(String)workCountry"
      };
      String[] workadrOptions =
      {
         "type=PREF", "type=INTL", "type=POSTAL", "type=PARCEL",
         "type=WORK"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.ADR, workadrOptions, workadr));
      String[] homeadr =
      {
         "", "", "(String)homeStreet", "(String)homeCity", "(String)homeState",
         "(String)homeZip", "(String)homeCountry"
      };
      String[] homeadrOptions =
      {
         "type=PREF", "type=INTL", "type=POSTAL", "type=PARCEL",
         "type=HOME"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.ADR, homeadrOptions, homeadr));
      String[] otheradr =
      {
         "", "", "(String)otherStreet", "(String)otherCity",
         "(String)otherState", "(String)otherZip", "(String)otherCountry"
      };
      String[] otheradrOptions =
      {
         "type=INTL", "type=POSTAL", "type=PARCEL"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.ADR, otheradrOptions, otheradr));
      // mapping of the vCard CATEGORIES property
      String[] categories =
      {
         "(String)categories"
      };
      String[] categoriesOptions = {};
      vAddressFieldTemplates.addElement(new AddressField(AddressField.CATEGORIES, categoriesOptions, categories));
      // mapping of the vCard BDAY property
      String[] birthday =
      {
         "(String)birthday"
      };
      String[] birthdayOptions = {};
      vAddressFieldTemplates.addElement(new AddressField(AddressField.BDAY, birthdayOptions, birthday));
      // mapping of the vCal NOTE property
      String[] description =
      {
         "(String)note"
      };
      String[] descriptionOptions = {};
      /*vDateFieldTemplates*/vAddressFieldTemplates.addElement(new AddressField(AddressField.NOTE, descriptionOptions, description));
      // mapping of X-ANNIVERSARY property
      String[] anniversary =
      {
         "(String)anniversary"
      };
      String[] anniversaryOptions =
      {
         "type=X-ANNIVERSARY"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.X, anniversaryOptions, anniversary));
      // mapping of X-ASSISTANT and X-ASSISTANTTEL
      String[] assistant =
      {
         "(String)assistant"
      };
      String[] assistantOptions =
      {
         "type=X-ASSISTANT"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.X, assistantOptions, assistant));
      String[] assistanttel =
      {
         "(String)assistantTel"
      };
      String[] assistanttelOptions =
      {
         "type=X-ASSISTANTTEL"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.X, assistanttelOptions, assistanttel));
      // mapping of X-SPOUSE and X-CHILDREN
      String[] spouse =
      {
         "(String)spouse"
      };
      String[] spouseOptions =
      {
         "type=X-SPOUSE"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.X, spouseOptions, spouse));
      String[] children =
      {
         "(String)children"
      };
      String[] childrenOptions =
      {
         "type=X-CHILDREN"
      };
      vAddressFieldTemplates.addElement(new AddressField(AddressField.X, childrenOptions, children));
   }
   /**
    * this method initialises the iAppointmentFields Vector; it contains all the field names of an IAppointment;
    * the order of the field names determines how the fields are read out of the String that comes
    * from CeIoBuiltIn.dll; e.g. the first field name is (String)id - this means that the first value in the
    * String from CeIoBuiltIn.dll will be stored as the id of the IAppointment.
    * So DO NOT MESS WITH THIS unless you are sure of what you are doing
    */
   private static void initIAppointmentFields()
   {
      // this determines how the fields of IAppointment are read out of the native String
      String []fields =
      {
         "(String)id",
         "(String)subject", // mapped to SUMMARY
         "(String)location", // mapped to LOCATION
         "(String)categories", // mapped to CATEGORIES
         "(String)reminderSoundFile", // mapped to X-REMINDER
         "(String)note", // mapped to DESCRIPTION
         "(IDate)startDate", // mapped to DTSTART
         "(IDate)endDate", // mapped to DTEND
         "(String)duration", // mapped to X-DURATION
         "(String)meetingStatus", // mapped to X-MEETINGSTATUS
         "(String)sensitivity", // mapped to CLASS
         "(String)busyStatus", // mapped to X-BUSYSTATUS
         "(String)reminderOptions", // mapped to X-REMINDER
         "(String)reminderMinutesBeforeStart", // mapped to X-REMINDER
         "(String)allDayEvent", // mapped to X-ALLDAYEVENT
         "(IRecurrence)recurrence", // mapped to RRULE
         "(String)reminderSet", // mapped X-REMINDER
         "(IRecipients)recipients", // mapped to ATTENDEE
      };
      vAppointmentFields = new Vector(fields);
   }
   /**
    * this method initialises the dateFieldTemplates; this vector determines how the pimAL fields will be
    * mapped to and from the IAppointment fields
    */
   private static void initDateFieldTemplates()
   {
      // mapping of the vCal SUMMARY property
      String[] subject =
      {
         "(String)subject"
      };
      String[] subjectOptions = {};
      vDateFieldTemplates.addElement(new DateField(VCalField.SUMMARY, subjectOptions, subject));
      // mapping of the vCal CATEGORIES property
      String[] categories =
      {
         "(String)categories"
      };
      String[] categoriesOptions = {};
      vDateFieldTemplates.addElement(new DateField(VCalField.CATEGORIES, categoriesOptions, categories));
      // mapping of the vCal CLASS property
      String[] sensitivity =
      {
         "(String)sensitivity"
      };
      String[] sensitivityOptions = {};
      vDateFieldTemplates.addElement(new DateField(VCalField.CLASSIFICATION, sensitivityOptions, sensitivity));
      // mapping of the vCal DTSTART property
      String[] startDate =
      {
         "(IDate)startDate"
      };
      String[] startDateOptions = {};
      vDateFieldTemplates.addElement(new DateField(VCalField.DTSTART, startDateOptions, startDate));
      // mapping of the vCal DTEND property
      String[] endDate =
      {
         "(IDate)endDate"
      };
      String[] endDateOptions = {};
      vDateFieldTemplates.addElement(new DateField(VCalField.DTEND, endDateOptions, endDate));
      // mapping of the vCal RRULE property
      String[] recurrence =
      {
         "(IRecurrence)recurrence"
      };
      String[] recurrenceOptions = {};
      vDateFieldTemplates.addElement(new DateField(VCalField.RRULE, recurrenceOptions, recurrence));
      // mapping of the vCal ATTENDEE property
      String[] recipients =
      {
         "(IRecipients)recipients"
      };
      String[] recipientsOptions = {};
      vDateFieldTemplates.addElement(new DateField(VCalField.ATTENDEE, recipientsOptions, recipients));
      // mapping of the vCal LOCATION property
      String[] location =
      {
         "(String)location"
      };
      String[] locationOptions = {};
      vDateFieldTemplates.addElement(new DateField(VCalField.LOCATION, locationOptions, location));
      // mapping of the vCal DESCRIPTION property
      String[] description =
      {
         "(String)note"
      };
      String[] descriptionOptions = {};
      vDateFieldTemplates.addElement(new DateField(VCalField.DESCRIPTION, descriptionOptions, description));
      // mapping of X-DURATION property
      String[] duration =
      {
         "(String)duration"
      };
      String[] durationOptions =
      {
         "type=X-DURATION"
      };
      vDateFieldTemplates.addElement(new DateField(VCalField.X, durationOptions, duration));
      // mapping of X-BUSYSTATUS property
      String[] busyStatus =
      {
         "(String)busyStatus"
      };
      String[] busyStatusOptions =
      {
         "X-BUSYSTATUS"
      };
      vDateFieldTemplates.addElement(new DateField(VCalField.X, busyStatusOptions, busyStatus));
      // mapping of X-MEETINGSTATUS property
      String[] meetingStatus =
      {
         "(String)meetingStatus"
      };
      String[] meetingStatusOptions =
      {
         "X-MEETINGSTATUS"
      };
      vDateFieldTemplates.addElement(new DateField(VCalField.X, meetingStatusOptions, meetingStatus));
      // mapping of the vCal X-ALLDAYEVENT property
      String[] allDayEvent =
      {
         "(String)allDayEvent"
      };
      String[] allDayEventOptions =
      {
         "X-ALLDAYEVENT"
      };
      vDateFieldTemplates.addElement(new DateField(VCalField.X, allDayEventOptions, allDayEvent));
      // mapping of the X-REMINDER property
      String[] reminder =
      {
         "(String)reminderOptions", "(String)reminderMinutesBeforeStart",
         "(String)reminderSet", "(String)reminderSoundFile"
      };
      String[] reminderOptions =
      {
         "type=X-REMINDER"
      };
      vDateFieldTemplates.addElement(new DateField(VCalField.X, reminderOptions, reminder));
   }
   /**
    * returns a range of Task fields
    */
   public static String[] iTaskFieldRange(int start, int end)
   {
      int n = end-start+1;
      String []ret = new String[n];
      totalcross.sys.Vm.arrayCopy(vTaskFields.items,start,ret,0,n);
      return ret;
   }
   /**
    * returns a range of Appointment fields
    */
   public static String[] iAppointmentFieldRange(int start, int end)
   {
      int n = end-start+1;
      String []ret = new String[n];
      totalcross.sys.Vm.arrayCopy(vAppointmentFields.items,start,ret,0,n);
      return ret;
   }
   /**
    * returns a range of Contact fields
    */
   public static String[] iContactFieldRange(int start, int end)
   {
      int n = end-start+1;
      String []ret = new String[n];
      totalcross.sys.Vm.arrayCopy(vContactFields.items,start,ret,0,n);
      return ret;
   }
}
