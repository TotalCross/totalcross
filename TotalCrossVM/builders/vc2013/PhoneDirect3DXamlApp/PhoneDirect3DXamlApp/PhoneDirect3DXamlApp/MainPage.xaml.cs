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
using System.Diagnostics;
using Windows.UI.ViewManagement;

namespace PhoneDirect3DXamlAppInterop
{
   public class CSWrapper : CSwrapper
   {
      // Workarround vars
      Grid root;

      // RadioDevice
      private int turnedState;

      // GPS
      private Geolocator geolocator;
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
      
      // user interface
      private double fontHeight;
      public TextBox tbox;
      public int portraitSipH, landscapeSipH, currentSipH;
      public int appW, appH;
      public bool sipVisible;

      public MainPage mp { set; get; }

      public CSWrapper(Grid g)
      {
          this.root = g;
          // used to get input from keyboard
          tbox = new TextBox();
          root.Children.Add(tbox);
          tbox.Visibility = Visibility.Collapsed;
          tbox.Margin = new Thickness(0, MainPage.instance.ActualHeight * 10, 0, 0); // at bottom
          tbox.LostFocus += tbox_LostFocus;
          // get native font size
          fontHeight = tbox.FontSize;
      }

      void tbox_LostFocus(object sender, RoutedEventArgs e)
      {
         if (sipVisible) // when we click outside the keyboard area, it tries to close; so we set the focus back again.
            tbox.Focus();
      }

      public double getFontHeightCS()
      {
          return fontHeight;
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
            if (geolocator == null)
            {
               geolocator = new Geolocator();
               geolocator.DesiredAccuracy = PositionAccuracy.High;
               geolocator.DesiredAccuracyInMeters = 10;
               geolocator.MovementThreshold = 10; // The units are meters.
               geolocator.StatusChanged += geolocator_StatusChanged;
               geolocator.PositionChanged += geolocator_PositionChanged;
            }
            return true;
         }
         catch (Exception)
         {
            nativeStopGPSCS();
            return false;
         }
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

      public void setSip(bool visible)
      {
         if (!visible)
            tbox.Visibility = System.Windows.Visibility.Collapsed;
         else
         {
            tbox.Text = "A"; tbox.SelectionStart = 1;
            tbox.Visibility = System.Windows.Visibility.Visible;
            tbox.Focus();
         }
      }
      public void privateWindowSetSIP(bool visible)
      {
         sipVisible = visible;
         root.Dispatcher.BeginInvoke((Action)(() => // must run on ui thread
         {
             setSip(visible);
         }));
         currentSipH = visible ? (appH > appW ? portraitSipH : landscapeSipH) : 0;
         if ((visible && currentSipH != 0) || (!visible && currentSipH == 0))
            MainPage.instance.d3dBackground.OnScreenChanged(currentSipH, 0, 0);
      }

      public int getSipHeight()
      {
         return currentSipH;
      }
   }

    ////////////////////////////////////////////////////////////////////////////////

    public partial class MainPage : PhoneApplicationPage
    {
        public Direct3DBackground d3dBackground;
        private int specialKey;
        private CSWrapper cs;
        public static MainPage instance;

        // Constructor
        public MainPage()
        {
            instance = this;
            InitializeComponent();
            this.BackKeyPress += MainPage_BackKeyPress;
            this.MouseMove += MainPage_MouseMove;
            this.MouseLeftButtonDown += MainPage_MouseLeftButtonDown;
            this.MouseLeftButtonUp += MainPage_MouseLeftButtonUp;
            this.SizeChanged += MainPage_SizeChanged;
            // InputPane input = InputPane.GetForCurrentView(); input.Showing += input_Showing; input.Hiding += input_Hiding; DOES NOT WORK! METHODS ARE NEVER CALLED. TEST AGAIN ON FUTURE WP VERSIONS
            BeginListenForSIPChanged();
        }

        void MainPage_SizeChanged(object sender, SizeChangedEventArgs e)
        {
           if (d3dBackground == null) return;

           bool isPortrait = instance.cs.appH > instance.cs.appW;
           // if sipH isnt set for current rotation, move it to bottom again
           if ((isPortrait && instance.cs.portraitSipH == 0) || (!isPortrait && instance.cs.landscapeSipH == 0))
              instance.cs.tbox.Margin = new Thickness(0, instance.ActualHeight * 10, 0, 0); // move to top
           d3dBackground.OnScreenChanged(-1, (int)e.NewSize.Width, (int)e.NewSize.Height);
        }

        private void BeginListenForSIPChanged()
        {
           System.Windows.Data.Binding b = new System.Windows.Data.Binding("Y");
           b.Source = (((App.Current as App).RootFrame).RenderTransform as TransformGroup).Children[0] as TranslateTransform;
           SetBinding(RootFrameTransformProperty, b);
        }

        public static readonly DependencyProperty RootFrameTransformProperty = DependencyProperty.Register("RootFrameTransform",typeof(double),typeof(MainPage),new PropertyMetadata(OnRootFrameTransformChanged));
        static void OnRootFrameTransformChanged(DependencyObject source, DependencyPropertyChangedEventArgs e)
        {
           double newvalue = (double)e.NewValue;
           if (newvalue == (int)newvalue) // finished?
           {
              bool isPortrait = instance.cs.appH > instance.cs.appW;
              if ((isPortrait && instance.cs.portraitSipH == 0) || (!isPortrait && instance.cs.landscapeSipH == 0))
              {
                 if (isPortrait)
                    instance.cs.currentSipH = instance.cs.portraitSipH = -(int)newvalue;
                 else
                    instance.cs.currentSipH = instance.cs.landscapeSipH = -(int)newvalue;
                 instance.cs.tbox.Margin = new Thickness(0, 0, 0, instance.ActualHeight * 10); // move to top
                 // once we set the value, we move the sip to top to prevent the odd animation
                 instance.cs.setSip(false);
                 instance.cs.setSip(true);
              }
           }
        }

        void MainPage_KeyDown(object sender, KeyEventArgs e)
        {
            int key = e.PlatformKeyCode;
            specialKey = key < 32 ? key : 0;
        }

        public bool ignoreNext;
        void tbox_TextChanged(object sender, TextChangedEventArgs e)
        {
            if (ignoreNext) { ignoreNext = false; return; }
            if (specialKey != 0)
                d3dBackground.OnKeyPressed(specialKey);
            else
            {
                String s = cs.tbox.Text;
                int idx = cs.tbox.SelectionStart - 1;
                cs.tbox.Text = "A"; cs.tbox.SelectionStart = 1; ignoreNext = true; // reset keyboard
                if (0 <= idx && idx < s.Length)
                   d3dBackground.OnKeyPressed(s.ElementAt(idx));
            }
            if (cs.tbox.Text.Length == 0)
            {
                ignoreNext = true;
                cs.tbox.Text = "A"; // dont leave it empty, otherwise we will not receive a backspace key (and we don't know the TotalCross' edit value)
                cs.tbox.SelectionStart = 1;
            }
        }

        void MainPage_MouseLeftButtonUp(object sender, MouseButtonEventArgs e)
        {
            e.Handled = true;
            Point p = e.GetPosition(this);
            d3dBackground.OnPointerReleased((int)p.X, (int)p.Y);
        }

        void MainPage_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            e.Handled = true;
            Point p = e.GetPosition(this);
            d3dBackground.OnPointerPressed((int)p.X, (int)p.Y);
        }

        void MainPage_MouseMove(object sender, MouseEventArgs e)
        {
            Point p = e.GetPosition(this);
            d3dBackground.OnPointerMoved((int)p.X, (int)p.Y);
        }

        void MainPage_BackKeyPress(object sender, System.ComponentModel.CancelEventArgs e)
        {
            e.Cancel = d3dBackground != null && d3dBackground.backKeyPress();
        }

        public void onSuspend()
        {
            d3dBackground.lifeCycle(true);
        }

        public void onResume()
        {
            d3dBackground.lifeCycle(false);
        }

        private void DrawingSurfaceBackground_Loaded(object sender, RoutedEventArgs e)
        {
            if (d3dBackground == null)
            {
                cs = new CSWrapper(LayoutRoot);
                cs.mp = this;
                d3dBackground = new Direct3DBackground(cs);

                cs.tbox.KeyDown += MainPage_KeyDown;
                cs.tbox.TextChanged += tbox_TextChanged;

                cs.appW = (int)LayoutRoot.ActualWidth;
                cs.appH = (int)LayoutRoot.ActualHeight;
                //int scrW = (int)Application.Current.Host.Content.ActualWidth, scrH = (int)Application.Current.Host.Content.ActualHeight;
                //LayoutRoot.Margin = new Thickness(0, scrH - appH,0,0);
                d3dBackground.OnScreenChanged(-1, cs.appW, cs.appH);
                d3dBackground.WindowBounds = d3dBackground.RenderResolution = d3dBackground.NativeResolution = new Windows.Foundation.Size(cs.appW,cs.appH);
                DrawingSurface.SetContentProvider(d3dBackground.CreateContentProvider());
            }
        }
    }
}