package tc.samples.app.btchat;

import totalcross.io.Connector;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.io.StreamConnectionNotifier;
import totalcross.io.device.RadioDevice;
import totalcross.io.device.bluetooth.DeviceClass;
import totalcross.io.device.bluetooth.DiscoveryAgent;
import totalcross.io.device.bluetooth.DiscoveryListener;
import totalcross.io.device.bluetooth.LocalDevice;
import totalcross.io.device.bluetooth.RemoteDevice;
import totalcross.io.device.bluetooth.ServiceRecord;
import totalcross.io.device.bluetooth.UUID;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.Grid;
import totalcross.ui.Label;
import totalcross.ui.MainWindow;
import totalcross.ui.Radio;
import totalcross.ui.RadioGroupController;
import totalcross.ui.Window;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.PressListener;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

public class FirstScreen extends Container
{
   Radio rdClient;
   Radio rdServer;
   RadioGroupController rdCtrl;
   Button btOk;
   Button btExit;

   final String serviceUUID = "700B9668C89611DEB60B10F655D89593";
   final String RFCOMM = "0000110100001000800000805F9B34FB";

   public void initUI()
   {
      rdCtrl = new RadioGroupController();
      rdClient = new Radio("Client", rdCtrl);
      rdServer = new Radio("Server", rdCtrl);

      Label lblMiddle = new Label("   ");
      add(lblMiddle, CENTER, CENTER);

      add(rdClient, BEFORE, SAME, lblMiddle);
      add(rdServer, AFTER, SAME, lblMiddle);

      Label lblRadioCaption = new Label("Act as: ");
      lblRadioCaption.align = CENTER;
      add(lblRadioCaption, LEFT, BEFORE, FILL, PREFERRED, lblMiddle);

      btOk = new Button("  Ok  ");
      btExit = new Button(" Exit ");
      add(btOk, BEFORE, BOTTOM - 4, lblMiddle);
      add(btExit, AFTER, BOTTOM - 4, lblMiddle);
   }

   public void onEvent(Event event)
   {
      switch (event.type)
      {
         case ControlEvent.PRESSED:
         {
            if (event.target == btOk)
            {
               Stream connection = null;
               if (rdCtrl.getSelectedIndex() == 0)
               {
                  RadioDevice.setState(RadioDevice.BLUETOOTH, RadioDevice.RADIO_STATE_ENABLED);
                  DiscoveryAgent agent = null;
                  Vector devices = null;
                  
                  try
                  {
                     agent = LocalDevice.getLocalDevice().getDiscoveryAgent();
                     // testing listener
                     final Window deviceList = new Window();
                     final Grid grid = new Grid(new String[] { "Name", "Address", "Major" }, new int[] { -45, -45, -10 }, new int[] { CENTER, CENTER,
                           CENTER }, false);
                     final Button windowExit = new Button("Close application");
                     deviceList.add(windowExit, LEFT, BOTTOM, FILL, PREFERRED + 2);
                     deviceList.add(grid, LEFT, TOP, FILL, FIT);
                     windowExit.setEnabled(false);
                     windowExit.addPressListener(new PressListener()
                     {
                        public void controlPressed(ControlEvent e)
                        {
                           if (e.type == ControlEvent.PRESSED)
                              deviceList.unpop();
                        }
                     });

                     final Hashtable devicesHash = new Hashtable(10);
                     agent.startInquiry(DiscoveryAgent.GIAC, new DiscoveryListener()
                     {
                        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod)
                        {
                           try
                           {
                              grid.add(new String[] { btDevice.getFriendlyName(), btDevice.getBluetoothAddress(), "" + cod.getMajorDeviceClass() });
                           }
                           catch (IOException e)
                           {
                              e.printStackTrace();
                           }
                           Vm.debug(Convert.toString(cod.getServiceClasses(), 2) + " " + Convert.toString(cod.getMajorDeviceClass(), 2) + " "
                                 + Convert.toString(cod.getMinorDeviceClass(), 2));
                           grid.repaintNow();
                           devicesHash.put(btDevice.getBluetoothAddress(), btDevice);
                        }

                        public void inquiryCompleted(int discType)
                        {
                           windowExit.setEnabled(true);
                           deviceList.repaintNow();
                        }

                        public void serviceSearchCompleted(int transID, int respCode)
                        {
                        }

                        public void servicesDiscovered(int transID, ServiceRecord[] servRecord)
                        {
                        }

                     });

                     // block until the search is finished.
                     deviceList.popup();

                     devices = devicesHash.getValues();
                     if (devices.isEmpty())
                     {
                        new MessageBox("Error 1", "No device found.").popup();
                        return;
                     }
                  }
                  catch (Exception e)
                  {
                     MessageBox.showException(e, true);
                     return;
                  }

                  Vm.alert("time to look for services");
                  
                  UUID[] uuidSet = {new UUID(0x1101)};
                  
                  final Window deviceList = new Window();
                  final Button windowExit = new Button("Close application");
                  deviceList.add(windowExit, LEFT, BOTTOM, FILL, PREFERRED + 2);
                  windowExit.setEnabled(false);
                  windowExit.addPressListener(new PressListener()
                  {
                     public void controlPressed(ControlEvent e)
                     {
                        if (e.type == ControlEvent.PRESSED)
                           deviceList.unpop();
                     }
                  });                  
                  final StringBuffer connectionURL = new StringBuffer(128);
                  
                  
                  try
                  {
                     Vm.debug(((RemoteDevice) devices.items[0]).getFriendlyName());
                     agent.searchServices(null, uuidSet, (RemoteDevice) devices.items[0], new DiscoveryListener()
                     {
                        public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod)
                        {
                        }

                        public void inquiryCompleted(int discType)
                        {
                        }

                        public void serviceSearchCompleted(int transID, int respCode)
                        {
                           Vm.debug("respCode: " + respCode);
                           windowExit.setEnabled(true);
                           deviceList.repaintNow();
                        }

                        public void servicesDiscovered(int transID, ServiceRecord[] servRecord)
                        {
                           connectionURL.append(servRecord[0].getConnectionURL());
                           for (int i = 0 ; i < servRecord.length ; i++)
                           {
                              Vm.debug("service: " + servRecord[i].getConnectionURL());
                              Vm.debug("record data: " + servRecord[i].toString());
                           }
                        }
                     });
                  }
                  catch (IOException e1)
                  {
                     MessageBox.showException(e1, true);
                  }


                  deviceList.popup();
                  
                  if (connectionURL != null)
                  try
                  {
                     String url = connectionURL.toString();
                     Vm.alert(url);
                     connection = (Stream) Connector.open(url);
                  }
                  catch (Exception e)
                  {
                     MessageBox.showException(e, true);
                     return;
                  }
                  else
                     new MessageBox("Error 2", "Service not found.").popup();
               }
               else if (rdCtrl.getSelectedIndex() == 1)
               {
                  RadioDevice.setState(RadioDevice.BLUETOOTH, RadioDevice.BLUETOOTH_STATE_DISCOVERABLE);
                  try
                  {
                     StreamConnectionNotifier server = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + serviceUUID);
                     while (connection == null)
                        connection = server.accept();
                     LocalDevice.getLocalDevice().getRecord(server);
                  }
                  catch (Exception e)
                  {
                     MessageBox.showException(e, true);
                  }
               }
               else
               {
                  // must choose one
               }
               if (connection != null)
                  MainWindow.getMainWindow().swap(new ChatScreen(connection));
            }
            else if (event.target == btExit)
               MainWindow.exit(0);
         }
      }
   }
}
