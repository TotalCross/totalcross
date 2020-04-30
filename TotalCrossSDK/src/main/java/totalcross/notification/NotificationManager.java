// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.notification;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

/**
 * Class to notify the user of events that happen. This is how you tell the user that something has
 * happened in the background.
 *
 * @author Fábio Sobral
 * @since TotalCross 4.2.0
 */
public class NotificationManager {

  private static NotificationManager instance;

  private NotificationManager() {}

  public static NotificationManager getInstance() {
    if (instance == null) {
      instance = new NotificationManager();
    }
    return instance;
  }

  /**
   * Post a notification to be shown in the status bar.
   *
   * @param notification
   */
  @ReplacedByNativeOnDeploy
  public void notify(Notification notification) {
    SystemTray tray = SystemTray.getSystemTray();

    //If the icon is a file
    Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
    //Alternative (if the icon is on the classpath):
    //Image image = Toolkit.getToolkit().createImage(getClass().getResource("icon.png"));

    TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
    //Let the system resize the image if needed
    trayIcon.setImageAutoSize(true);
    //Set tooltip text for the tray icon
    trayIcon.setToolTip("System tray icon demo");
    try {
      tray.add(trayIcon);
    } catch (AWTException e) {
      e.printStackTrace();
    }

    trayIcon.displayMessage(notification.title(), notification.text(), MessageType.INFO);
  }
}
