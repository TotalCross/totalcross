using System;
using System.Net;
using System.Linq;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Shapes;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Media.Animation;
using System.Threading.Tasks;
using System.Collections.Generic;
using Microsoft.Phone.Shell;
using Microsoft.Phone.Tasks;
using Microsoft.Phone.Storage;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Net.NetworkInformation;
using PhoneDirect3DXamlAppComponent;
using Windows.Devices.Geolocation;
using Windows.Networking.Proximity;

namespace PhoneDirect3DXamlAppInterop
{
   public class CSWrapper : CSwrapper
   {
      // Workarround vars
      Grid root;
      TextBlock tx;
      private Thickness awayMargin; // any new  programaticallycreated controller that should not be placed on screen should do "newController.margin = awayMargin;"

      // RadioDevice
      private int turnedState;

      // GPS
      private Geolocator geolocator;
      private bool tracking;
      private PositionStatus status = PositionStatus.NotInitialized;
      private double latitude; // GPS_latitude()               
      private double longitude; // GPS_longitude()                  
      private double? direction; // GPS_direction()                  
      private double? velocity; // GPS_velocity()                   
      // GPS_satellites()  -- não tem essa info, não precisa colocar 0, já vez 0 do objeto               
      int year;
      int month;
      int day;
      int hour;
      int minute;
      int second;
      int milliSecond;
      private String messageReceived = ""; // GPS_messageReceived()          
      private double? pdop; // GPS_pdop() 
      private String lowSignalReason = ""; // GPS_lowSignalReason()

      // File
      private bool cardIsInserted;
      private int semaphore = 1;

      public double getFontHeightCS()
      {
          if (tx == null)
          {
              tx = new TextBlock();
              tx.Text = "@";

              tx.Margin = awayMargin;
              root.Children.Add(tx);
          }

          return tx.ActualHeight;
      }

      public CSWrapper(Grid g)
      {
          this.root = g;
          this.awayMargin = new Thickness(500, 0, 0, 0);
      }
      public MainPage mp { set; get; }
      public void callDraw()
      {
      }

      public void privateAlertCS(String str) // Vm
      {
         Deployment.Current.Dispatcher.BeginInvoke(() =>
         {
            MessageBox.Show(str, "ALERT", MessageBoxButton.OK);
         });
      }

      public void vmSetAutoOffCS(bool enable) // Vm
      {
         PhoneApplicationService.Current.ApplicationIdleDetectionMode = PhoneApplicationService.Current.UserIdleDetectionMode = enable ? IdleDetectionMode.Enabled : IdleDetectionMode.Disabled;
      }

      public void dialNumberCS(String number) // Dial
      {
         PhoneCallTask phoneCallTask = new PhoneCallTask();
         phoneCallTask.PhoneNumber = number;
         phoneCallTask.Show();
      }

      public void smsSendCS(String message, String destination) // SMS
      {
         SmsComposeTask smsComposeTask = new SmsComposeTask();
         smsComposeTask.To = destination;
         smsComposeTask.Body = message;
         smsComposeTask.Show();
      }

      public int getTurnedState() // RadioDevice
      {
         return turnedState;
      }

      public async void rdGetStateCS(int type) // RadioDevice
      {
         switch (type)
         {
            case 0: // RadioDevice.WIFI = 0
               turnedState = DeviceNetworkInformation.IsWiFiEnabled? 1 : 0;
               break;
            case 1: // RadioDevice.PHONE = 1
               turnedState = DeviceNetworkInformation.IsCellularDataEnabled? 1 : 0;
               break;
            default: // RadioDevice.BLUETOOTH = 2
               {
                  PeerFinder.Start();                 
                  try
                  {
                     turnedState = 1;
                     await PeerFinder.FindAllPeersAsync();                    
                  }
                  catch (Exception)
                  {
                     turnedState = 0;
                  }
                  break;
               }
         }
      }

      public bool nativeStartGPSCS() // GPS
      {
         try
         {
            if (!tracking)
            {
               (geolocator = new Geolocator()).DesiredAccuracy = PositionAccuracy.High;
               geolocator.DesiredAccuracyInMeters = 10;
               geolocator.MovementThreshold = 10; // The units are meters.

               geolocator.StatusChanged += geolocator_StatusChanged;
               geolocator.PositionChanged += geolocator_PositionChanged;
               tracking = true;
            }
         }
         catch (Exception)
         {
            nativeStopGPSCS();
            return false;
         }

         return true;
      }

      public int nativeUpdateLocationCS() // GPS
      {
         int flags = 0;

         if (status != PositionStatus.Ready)            
         {
            latitude = longitude = 4.9E-324;
            direction = velocity = pdop = 4.9E-324;
            lowSignalReason = messageReceived;
         }

         if (latitude != 4.9E-324)
            flags |= 1;
         if (longitude != 4.9E-324)
            flags |= 2;
         if (direction != null && direction != 4.9E-324)
            flags |= 4;
         if (velocity != null && velocity != 4.9E-324)
            flags |= 8;
         // 16 = satellites
         if (pdop != null && pdop != 4.9E-324)
            flags |= 32;

         return flags;
      }

      public void nativeStopGPSCS() // GPS
      {
         geolocator.PositionChanged -= geolocator_PositionChanged;
         geolocator.StatusChanged -= geolocator_StatusChanged;
         geolocator = null;
         tracking = false;
      }

      public double getLatitude()   {return latitude;}
      public double getLongitude()  {return longitude;}
      public double getDirection()  {return (double)direction;}      
      public double getVelocity()   {return (double)velocity;}
      public int getYear()          {return year;}
      public int getMonth()         {return month;}
      public int getDay()           {return day;}
      public int getHour()          {return hour;}
      public int getMinute()        {return minute;}
      public int getSecond()        {return second;}
      public int getMilliSecond()   {return milliSecond;}
      public double getPdop()       {return (double)pdop;}

      public String getMessageReceived()  {return messageReceived;}
      public String getLowSignalReason() {return lowSignalReason;}

      private void geolocator_StatusChanged(Geolocator sender, StatusChangedEventArgs args)
      {
         switch (status = args.Status)
         {
            case PositionStatus.Disabled:
               messageReceived = "location is disabled in phone settings"; // the application does not have the right capability or the location master switch is off
               break;
            case PositionStatus.Initializing:
               messageReceived = "initializing"; // the geolocator started the tracking operation
               break;
            case PositionStatus.NoData:
               messageReceived = "no data"; // the location service was not able to acquire the location
               break;
            case PositionStatus.Ready:
               messageReceived = "ready"; // the location service is generating geopositions as specified by the tracking parameters
               break;
            case PositionStatus.NotAvailable:
               messageReceived = "not available"; // not used in WindowsPhone, Windows desktop uses this value to signal that there is no hardware capable to acquire location information
               break;
            case PositionStatus.NotInitialized:
               messageReceived = ""; // the initial state of the geolocator, once the tracking operation is stopped by the user the geolocator moves back to this state
               break;
         }
      }

      private void geolocator_PositionChanged(Geolocator sender, PositionChangedEventArgs args)
      {
         latitude = args.Position.Coordinate.Latitude;
         longitude = args.Position.Coordinate.Longitude;
         direction = args.Position.Coordinate.Heading;
         velocity = args.Position.Coordinate.Speed;
         // não tem número de satélites
         DateTimeOffset lastFix = args.Position.Coordinate.Timestamp;
         year = lastFix.Year;
         month = lastFix.Month;
         day = lastFix.Day;
         hour = lastFix.Hour;
         minute = lastFix.Minute;
         second = lastFix.Second;
         milliSecond = lastFix.Millisecond;
         pdop = args.Position.Coordinate.SatelliteData.PositionDilutionOfPrecision;
      }

      private async Task sdCardTask()
      {
         semaphore = 0;
         ExternalStorageDevice sdCard = (await ExternalStorage.GetExternalStorageDevicesAsync()).FirstOrDefault();
         if (sdCard != null && sdCard.RootFolder != null)
            cardIsInserted = true;
         semaphore = 1;
      }

      public bool fileIsCardInsertedCS()
      {
         if (!cardIsInserted)
            sdCardTask();
         while (semaphore == 0) { }
         return cardIsInserted;
      }
    }
    public partial class MainPage : PhoneApplicationPage
    {
        private Direct3DBackground m_d3dBackground;

        // Constructor
        public MainPage()
        {
            InitializeComponent();
            this.LostFocus += MainPage_LostFocus;
            this.BackKeyPress += MainPage_BackKeyPress;
        }

        void MainPage_BackKeyPress(object sender, System.ComponentModel.CancelEventArgs e)
        {
            e.Cancel = m_d3dBackground != null && m_d3dBackground.backKeyPress();
        }

        void MainPage_LostFocus(object sender, RoutedEventArgs e)
        {
            MessageBox.Show("lalala");
        }

        private void DrawingSurfaceBackground_Loaded(object sender, RoutedEventArgs e)
        {
            if (m_d3dBackground == null)
            {
                CSWrapper cs = new CSWrapper(LayoutRoot);
                cs.mp = this;
                cs.getFontHeightCS();
                m_d3dBackground = new Direct3DBackground(cs);

                float w = (float)Application.Current.Host.Content.ActualWidth;
                float h = (float)Application.Current.Host.Content.ActualHeight;
                float k = (float)Application.Current.Host.Content.ScaleFactor;
                // Set window bounds in dips
                m_d3dBackground.WindowBounds = new Windows.Foundation.Size(w,h);
                // Set native resolution in pixels
                m_d3dBackground.NativeResolution = new Windows.Foundation.Size((float)Math.Floor(w * k / 100.0f + 0.5f), (float)Math.Floor(h * k / 100.0f + 0.5f));
                // Set render resolution to the full native resolution
                m_d3dBackground.RenderResolution = m_d3dBackground.NativeResolution;
                // Hook-up native component to DrawingSurfaceBackgroundGrid
                DrawingSurfaceBackground.SetBackgroundContentProvider(m_d3dBackground.CreateContentProvider());
                DrawingSurfaceBackground.SetBackgroundManipulationHandler(m_d3dBackground);
            }
        }
    }
}