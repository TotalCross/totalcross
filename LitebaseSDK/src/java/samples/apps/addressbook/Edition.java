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

package samples.apps.addressbook;

import litebase.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.*;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.*;
import totalcross.ui.media.Sound;
import totalcross.util.*;

class Edition extends Container
{
   /**
    * The edit for entering the person name.
    */
   private Edit edName;
   
   /**
    * The edit for entering the person address.
    */
   private Edit edAddress;
   
   /**
    * The edit for entering the person phone.
    */
   Edit edPhone;
   
   /**
    * The edit for entering the person birthday.
    */
   Edit edBirth;
   
   /**
    * The edit for entering the person salary.
    */
   Edit edSalary;
   
   /**
    * The radio button for male gender.
    */
   private Radio rdMale;
   
   /**
    * The radio button for female gender.
    */
   private Radio rdFemale;
   
   /**
    * The group for the gender radio boxes.
    */
   private RadioGroupController rgGender;
   
   /**
    * The check box for the civil status.
    */
   private Check chMarried;
   
   /**
    * The push button group for editing a person information.
    */
   private PushButtonGroup pbgAction;
   
   /**
    * The OK button, which executes the selected operation.
    */
   private Button btnOk;
   
   /**
    * Next button.
    */
   private Button btnNext;
   
   /**
    * Previous button.
    */
   private Button btnPrev;
   
   /**
    * The status label which shows that last time when the table was updated.
    */
   private Label lbStatus;
   
   /**
    * The image controler.
    */
   private ImageControl imgCtrl;
   
   /**
    * The address book object.
    */
   AddressBook book;
   
   /** 
    * The rowid of the selected row.
    */
   int rowId;
   
   /**
    * The photo rowid.
    */
   private int photoIdx;
   
   /**
    * The connection with Litebase;
    */
   private LitebaseConnection driver;
   
   /**
    * A prepared statement for deleting from the table.
    */
   private PreparedStatement psDelete;
   
   /**
    * A prepared statement for inserting into the table.
    */
   private PreparedStatement psInsert;
   
   /**
    * A prepared statement for updating from the table.
    */
   private PreparedStatement psUpdate;
   
   /**
    * A prepared statement for selecting a row from the table.
    */
   private PreparedStatement psSelect;
   
   /**
    * A result set with the pictures.
    */
   private ResultSet photoResultSet;
   
   /**
    * A temporary date object to save memory.
    */
   Date tempDate = new Date();
   
   /**
    * A temporary time object to save memory.
    */
   Time tempTime = new Time();
   
   /**
    * A temporary buffer to save memory.
    */
   StringBuffer buffer = new StringBuffer(100);

   /**
    * Initializes the user interface.
    */
   public void initUI()
   {
      Label labelAux = new Label("Birthday");
      Edit editAux = edBirth = new Edit("99/99/9999") ;
      int xx = labelAux.getPreferredWidth() + 5; // Computes the max width so that all can stay at the same position.
      
      // name
      add(new Label("Name"),LEFT + 1, AFTER + 2);
      add(edName = new Edit(""), xx, SAME - 1);
      edName.setMaxLength(30);

      // address
      add(new Label("Address"),LEFT + 1,AFTER + 1);
      add(edAddress = new Edit(""), xx, SAME - 1);
      edAddress.setMaxLength(50);

      // phone
      add(new Label("Phone"),LEFT + 1,AFTER + 1);
      add(edPhone = new Edit("+99 99 9999-9999"), xx, SAME - 1);
      edPhone.setMaxLength(20);

      // birthday
      add(labelAux, LEFT + 1, AFTER + 1);
      add(editAux = edBirth = new Edit("99/99/9999"), xx, SAME - 1);
      editAux.setMode(Edit.DATE);
      editAux.setMaxLength(10);

      // salary
      add(new Label("Salary"), LEFT, AFTER + 1);
      add(edSalary = new Edit("9999999.99"), xx, SAME - 1);
      edSalary.setMode(Edit.CURRENCY);
      
      // gender
      add(new Label("Gender"), LEFT, AFTER + 1);
      add(rdMale = new Radio("Male", rgGender = new RadioGroupController()), xx, SAME - 1);
      add(rdFemale = new Radio("Female", rgGender), AFTER + 2, SAME);
      rdMale.setChecked(true);

      // Civil status.
      add(chMarried = new Check("Married"), xx, AFTER + 1);

      // action
      add(pbgAction = new PushButtonGroup(new String[]{"Insert", "Update", "Delete", "Clear"}, false, -1, 0, 4, 0, false, PushButtonGroup.NORMAL), 
                                                                                                                      LEFT, AFTER + 2);
      add(btnOk = new Button(" Ok "), RIGHT, SAME);

      // status
      (labelAux = lbStatus = new Label("", CENTER)).setInvert(true);
      labelAux.setForeColor(Color.brighter(getForeColor()));
      add(labelAux, LEFT, BOTTOM);
      
      // picture
      add(new Spacer(" "), CENTER, BEFORE - 10, PREFERRED, PREFERRED, labelAux);
      add(btnPrev = new Button("     <<     "), BEFORE, SAME);
      add(btnNext = new Button("     >>     "), AFTER, SAME);
      add(imgCtrl = new ImageControl(), LEFT, AFTER + 10, FILL, FIT, btnOk);
      imgCtrl.setEventsEnabled(true);
     
      driver = book.driver;
      psDelete = driver.prepareStatement("delete bookentry where rowid = ?");
      psInsert = driver.prepareStatement("insert into bookentry values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
      psUpdate = driver.prepareStatement("update bookentry set name = ?, address = ?, phone = ?, birthday = ?, salary = ?, married = ?, gender = ?,"
                                                                                              + "lastUpdated = ?, photoidx = ? where rowid = ?");
      psSelect = driver.prepareStatement("select * from bookentry where rowid = ?");
      photoResultSet = driver.executeQuery("select rowid, photo from photodb");  
      
      setPicture(0);
   }

   /**
    * Verifies if all the fields are ok.
    * 
    * @return <code>true</code> if all the field values are valid; <code>false</code>, otherwise.
    */
   private boolean verifyFields()
   {  
      StringBuffer sb = buffer;
      
      sb.setLength(0);
      if (edName.getText().length() == 0)
         sb.append("name |");   
      if (edAddress.getText().length() == 0)
         sb.append("address |");   
      if (edPhone.getText().length() == 0)
         sb.append("phone |");
      try
      {
         if (Convert.toDouble(edSalary.getText()) < 0)
            sb.append("salary |");
      } catch (InvalidNumberException exception)
      {
         sb.append("salary |");
      }
      try
      {
          tempDate.set(edBirth.getText(), Settings.dateFormat);
      } catch (InvalidDateException exception)
      {
         sb.append("birthday |");
      }

      if (sb.length() > 0) // Any fields with problems?
      {
         sb.setLength(sb.length() -1); // Removes the last |.
         new MessageBox("Attention","You must fill/correct | the following fields: |" + sb).popupNonBlocking();
         return false;
      }
      return true;
   }

   /**
    * Called to process posted events.
    *
    * @param event The posted event.
    */
   public void onEvent(Event event)
   {
      if (event.type == ControlEvent.PRESSED)
      {   
         if (event.target == btnOk)
         {
            switch (pbgAction.getSelectedIndex())
            {
               case 0: // insert
                  if (verifyFields())
                     doInsertUpdate(true);
                  break;
               case 1: // update
                  if (verifyFields()) // Only updates if the row exists.
                     if (rowId > 0)
                        doInsertUpdate(false);
                     else
                        Sound.beep();
                  break;
               case 2: // delete
                  if (rowId > 0) // Only deletes if the row exists.
                     doDelete();
                  else
                     Sound.beep();
                  break;
               case 3: // clear
                  clear();
                  break;
               default:
                  Sound.beep();
            }
         }
         else if (event.target == btnPrev) // Button prev: goes to the previous picture.
            move(-1);
         else if (event.target == btnNext) // Button next: goes to the next picture.
            move(1);
      }
   }
   
   /**
    * Executes the selected operation in the database.
    * 
    * @param isInsert Indicates if the operation is an insert or update.
    */
   private void doInsertUpdate(boolean isInsert)
   {
      String name = edName.getText(),
             addr = edAddress.getText(),
             phone = edPhone.getText();
      int birth = -1,
          married = chMarried.isChecked()? 1 : 0,
          gender = rdMale.isChecked()? 1 : 0,
          rows = -1;
      double salary = 0;
      long lastUpdated = new Time().getTimeLong();
      PreparedStatement psAux;
      
      try
      {
         birth = tempDate.set(edBirth.getText(), Settings.dateFormat);
         salary = Convert.toDouble(edSalary.getText());
      }
      catch (Exception exception)
      {
         MessageBox.showException(exception, true);
      }
      
      if (isInsert)
         psAux = psInsert;
      else
         (psAux = psUpdate).setInt(9, rowId);
         
      psAux.setString(0, name);
      psAux.setString(1, addr);
      psAux.setString(2, phone);
      psAux.setInt(3, birth);
      psAux.setFloat(4, salary);
      psAux.setShort(5, (short)married);
      psAux.setShort(6, (short)gender);
      psAux.setLong(7, lastUpdated);
      psAux.setShort(8, (short)photoIdx);
      rows = psAux.executeUpdate();
      if (rows == 1)
      {
         book.invalidateRS();
         clear();
      }
      else 
         Sound.beep();
   }

   /**
    * Deletes a row from the table.
    */
   private void doDelete()
   {
      psDelete.setInt(0, rowId);
      if (psDelete.executeUpdate() == 1)
      {
         book.invalidateRS();
         clear();
      }
      else 
         Sound.beep();
   }

   /**
    * Clears all the fields.
    */
   public void clear()
   {
      edName.setText("");
      edAddress.setText("");
      edPhone.setText("");
      edBirth.setText("");
      edSalary.setText("");
      chMarried.setChecked(false);
      rgGender.setSelectedItem(rdMale);
      pbgAction.setSelectedIndex(-1);
      lbStatus.setText("");
      rowId = -1;
      setPicture(0);
   }

   /**
    * Shows the selected row for edition.
    */
   public void show()
   {
      psSelect.setInt(0, rowId);
      ResultSet resultSet = psSelect.executeQuery();
      resultSet.next();
      edName.setText(resultSet.getString(1));
      edAddress.setText(resultSet.getString(2));
      edPhone.setText(resultSet.getString(3));

      int date = resultSet.getInt(4);
      try
      {
         tempDate.set(date % 100, (date /= 100) % 100, date / 100);
         edBirth.setText("" + tempDate);
      } 
      catch (InvalidDateException exception) {}

      edSalary.setText(Convert.toString(resultSet.getFloat(5), 2));

      chMarried.setChecked(resultSet.getString(6).charAt(0) == '1');

      String gender = resultSet.getString(7);
      rgGender.setSelectedItem(gender.charAt(0) == '1'? rdMale : rdFemale); // Activates the proper one.

      long dateTime = resultSet.getLong(8);
      int time = (int)(dateTime % 1000000);
      date = (int)(dateTime / 1000000);
      
      try
      {
         tempDate.set(date % 100, (date /= 100) % 100, date / 100);
         Time timeAux = tempTime;
         timeAux.second = time % 100;
         timeAux.minute = (time /= 100) % 100;
         timeAux.hour = time / 100;
         StringBuffer sb = buffer;
         sb.setLength(0);
         lbStatus.setText(sb.append("Last updated: ").append(tempDate).append(' ').append(timeAux).toString());
      } catch (InvalidDateException exception) {}

      pbgAction.setSelectedIndex(1);
      setPicture(resultSet.getShort(9));
   }
   
   /** 
    * Moves the current record by a step and load a person image.
    * 
    * @param step The sted 
    */
   private void move(int step)
   {
      int length = 3;
      // Selects the current index.
      
      photoIdx = (photoIdx + step) % length;
      if (photoIdx < 0) 
         photoIdx += length;
      
      setPicture(photoIdx); // Selects the person image.
   }
   
   /**
    * Sets the right picture in its control.
    * @param photoIdx The picture index.
    */
   private void setPicture(int newPhotoIdx)
   {
      photoResultSet.absolute(photoIdx = newPhotoIdx);         
      try
      {
         imgCtrl.setImage(new Image(photoResultSet.getBlob(2)));
      }
      catch (ImageException exception)
      {
         MessageBox.showException(exception, true);
      }
   }
}
