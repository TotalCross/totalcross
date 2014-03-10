using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;
using Microsoft.Phone.Shell;
using Microsoft.Phone.Tasks;
using Microsoft.Phone.Controls;
using PhoneDirect3DXamlAppComponent;
using Windows.Networking.Proximity;
using Microsoft.Phone.Net.NetworkInformation;
using Windows.Devices.Geolocation;

namespace PhoneDirect3DXamlAppInterop
{
   public class dummy : Idummy
   {
       // RadioDevice
      private int turnedState;

      // GPS
      private Geolocator geolocator = null;
      private bool tracking = false;
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

      public dummy()
      {

      }
      public MainPage mp { set; get; }
      public void callDraw()
      {
         //mp.Background.Transform.Inverse = true;
         //mp.UpdateLayout();
      }

      public void privateAlertCS(String str) // Vm
      {
         MessageBox.Show(str, "ALERT", MessageBoxButton.OK);
      }

      public void vmSetAutoOffCS(bool enable) // Vm
      {
         if (enable)
            PhoneApplicationService.Current.ApplicationIdleDetectionMode = PhoneApplicationService.Current.UserIdleDetectionMode
                                                                           = IdleDetectionMode.Enabled;
         else
            PhoneApplicationService.Current.ApplicationIdleDetectionMode = PhoneApplicationService.Current.UserIdleDetectionMode
                                                                           = IdleDetectionMode.Disabled;
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
         // Não tem 16 pois não tem número de satélites.
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

      public double getLatitude()
      {
         return latitude;
      }

      public double getLongitude()
      {
         return longitude;
      }

      public double getDirection()
      {
         return (double)direction;
      }
      
      public double getVelocity()
      {
         return (double)velocity;
      }

      public int getYear()
      {
         return year;
      }

      public int getMonth()
      {
         return month;
      }

      public int getDay()
      {
         return day;
      }

      public int getHour()
      {
         return hour;
      }

      public int getMinute()
      {
         return minute;
      }

      public int getSecond()
      {
         return second;
      }

      public int getMilliSecond()
      {
         return milliSecond;
      }

      public String getMessageReceived()
      {
         return messageReceived;
      }

      public double getPdop()
      {
         return (double)pdop;
      }

      public String getLowSignalReason()
      {
         return lowSignalReason;
      }

      private void geolocator_StatusChanged(Geolocator sender, StatusChangedEventArgs args)
      {
         switch (status = args.Status)
         {
            case PositionStatus.Disabled:
               // the application does not have the right capability or the location master switch is off
               messageReceived = "location is disabled in phone settings";
               break;
            case PositionStatus.Initializing:
               // the geolocator started the tracking operation
               messageReceived = "initializing";
               break;
            case PositionStatus.NoData:
               // the location service was not able to acquire the location
               messageReceived = "no data";
               break;
            case PositionStatus.Ready:
               // the location service is generating geopositions as specified by the tracking parameters
               messageReceived = "ready";
               break;
            case PositionStatus.NotAvailable:
               // not used in WindowsPhone, Windows desktop uses this value to signal that there is no hardware capable to acquire location information
               messageReceived = "not available";
               break;
            case PositionStatus.NotInitialized:
               // the initial state of the geolocator, once the tracking operation is stopped by the user the geolocator moves back to this state
               messageReceived = "";
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

    }
    public partial class MainPage : PhoneApplicationPage
    {
        private Direct3DBackground m_d3dBackground = null;

        // Constructor
        public MainPage()
        {
            InitializeComponent();
        }

        private void DrawingSurfaceBackground_Loaded(object sender, RoutedEventArgs e)
        {
            if (m_d3dBackground == null)
            {
                dummy odummy = new dummy();
                odummy.mp = this;
                m_d3dBackground = new Direct3DBackground(odummy);

                // Set window bounds in dips
                m_d3dBackground.WindowBounds = new Windows.Foundation.Size(
                    (float)Application.Current.Host.Content.ActualWidth,
                    (float)Application.Current.Host.Content.ActualHeight
                    );

                // Set native resolution in pixels
                m_d3dBackground.NativeResolution = new Windows.Foundation.Size(
                    (float)Math.Floor(Application.Current.Host.Content.ActualWidth * Application.Current.Host.Content.ScaleFactor / 100.0f + 0.5f),
                    (float)Math.Floor(Application.Current.Host.Content.ActualHeight * Application.Current.Host.Content.ScaleFactor / 100.0f + 0.5f)
                    );

                // Set render resolution to the full native resolution
                m_d3dBackground.RenderResolution = m_d3dBackground.NativeResolution;

                // Hook-up native component to DrawingSurface
                DrawingSurfaceBackground.SetBackgroundContentProvider(m_d3dBackground.CreateContentProvider());
                DrawingSurfaceBackground.SetBackgroundManipulationHandler(m_d3dBackground);
            }
        }
    }
}