/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package samples.sys.photodb;

import litebase.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.dialog.*;
import totalcross.ui.event.*;
import totalcross.ui.image.*;

/** 
 * This is a simple program showing how to populate a database with some photos and then retrieve them. 
 */
public class PhotoDB extends MainWindow
{
   /**
    * The connection with Litebase.
    */
   LitebaseConnection conn = LitebaseConnection.getInstance(Settings.applicationId);
   
   /**
    * Next button.
    */
   Button btnNext;
   
   /**
    * Previous button.
    */
   Button btnPrev;
   
   /**
    * The label with the person name.
    */
   Label labName;
   
   /**
    * The image controler.
    */
   ImageControl imgCtrl;
   
   /**
    * The names of the people.
    */
   String[] names = {"Guilherme", "Fábio", "Juliana"};
   
   /**
    * The current person index.
    */
   int current;
   
   /**
    * The select prepared statement.
    */
   PreparedStatement psSelect;

   static
   {
      Settings.useNewFont = true;
   }
   
   /**
    * The constructor.
    */
   public PhotoDB()
   {
      super("Photo DataBase", TAB_ONLY_BORDER);
   }
   
   /**
    * Initializes the user interface and creates the table if necessary.
    */
   public void initUI()
   {
      // database
      if (!conn.exists("photodb"))
      {
         add(new Label("Please wait,creating \n photo database..."), CENTER, CENTER);
         repaintNow();
         conn.execute("create table photodb(name char(20), photo blob(16384))");
         conn.setRowInc("photodb", 3);
         
         PreparedStatement ps = conn.prepareStatement("insert into photodb values (?,?)");
         String[] images = {"guilherme.jpg", "fabio.jpg", "juliana.jpg"};
         int i = -1;
         while (++i < 3)
         {
            ps.setString(0, names[i]);
            ps.setBlob(1, Vm.getFile(images[i]));
            ps.executeUpdate();
         }
         conn.setRowInc("photodb", -1);
         removeAll();
      }
      psSelect = conn.prepareStatement("select photo from photodb where name = ?");

      // user interface
      Spacer spacer = new Spacer(" ");
      add(spacer, CENTER, BOTTOM);
      add(btnPrev = new Button("     <<     "), BEFORE, BOTTOM);
      add(btnNext = new Button("     >>     "), AFTER, BOTTOM, spacer);
      add(labName = new Label("", CENTER), LEFT, BEFORE);
      add(imgCtrl = new ImageControl(), LEFT, TOP, FILL, FIT);
      imgCtrl.setEventsEnabled(true);
      move(0);
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
         if (event.target == btnPrev) // Button prev: goes to the previous picture.
            move(-1);
         else if (event.target == btnNext) // Button next: goes to the next picture.
            move(1);
      }
   }

   /** 
    * Moves the current record by a step and load a person image.
    * 
    * @param step The sted 
    */
   private void move(int step)
   {
      // Selects the current index.
      current = (current + step) % names.length;
      if (current < 0) 
         current += names.length;
      labName.setText(names[current]);
      
      // Selects the person image.
      try
      {
         psSelect.setString(0, names[current]);
         ResultSet rs = psSelect.executeQuery();
         rs.first();
         imgCtrl.setImage(new Image(rs.getBlob(1)));
         rs.close();
      }
      catch (ImageException exception)
      {
         MessageBox.showException(exception, true);
      }
   }
}
