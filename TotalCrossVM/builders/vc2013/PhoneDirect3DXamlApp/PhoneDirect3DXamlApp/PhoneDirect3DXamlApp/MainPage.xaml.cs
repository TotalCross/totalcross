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
using System.IO;
using Windows.Storage;
using System.IO.IsolatedStorage;
using System.Xml.Linq;
using Microsoft.Phone.BackgroundAudio;
using System.Windows.Resources;
using Microsoft.Phone.Info;
using System.Device.Location;

namespace PhoneDirect3DXamlAppInterop
{
   class Settings
   {
      public static int landSipH, portSipH;
   }

   public class CSWrapper : CSwrapper
   {
      // Workarround vars
      Grid root;
      bool setAutoOffCalled;

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
      public int currentSipH;
      public bool sipVisible;
      private bool alertIsVisible;
      // camera support
      private CameraCaptureTask cameraCaptureTask;
      private String cameraName;
      private int cameraResult;
      public String appName;

      public CSWrapper(Grid g)
      {
          this.root = g;
          g.Background = new SolidColorBrush(Colors.Black); // fixes inverted background when using light theme

          // used to get input from keyboard
          tbox = new TextBox();
          root.Children.Add(tbox);
          tbox.Visibility = Visibility.Collapsed;
          tbox.LostFocus += tbox_LostFocus;
          // get native font size
          fontHeight = tbox.FontSize;
          ReadSettings().Wait();
          tbox.Margin = isSipSet()
            ? new Thickness(0, 0, 0, MainPage.instance.ActualHeight * 10) // to top
            : new Thickness(0, MainPage.instance.ActualHeight * 10, 0, 0);  // to bottom
      }

      public long getFreeMemory()
      {
         return Microsoft.Phone.Info.DeviceStatus.ApplicationMemoryUsageLimit - Microsoft.Phone.Info.DeviceStatus.ApplicationCurrentMemoryUsage;
      }
      public String getDeviceId()
      {
         return Microsoft.Phone.Info.DeviceStatus.DeviceManufacturer + " " + Microsoft.Phone.Info.DeviceStatus.DeviceName;
      }
      public bool isVirtualKeyboard()
      {
         return !Microsoft.Phone.Info.DeviceStatus.IsKeyboardPresent;
      }
      // http://msdn.microsoft.com/en-us/library/windows/apps/microsoft.phone.info.deviceextendedproperties%28v=vs.105%29.aspx
      public double getDpiX()
      {
         object o;
         return Microsoft.Phone.Info.DeviceExtendedProperties.TryGetValue("RawDpiX", out o) ? (double)o : 0;
      }
      public double getDpiY()
      {
         object o;
         return Microsoft.Phone.Info.DeviceExtendedProperties.TryGetValue("RawDpiY", out o) ? (double)o : 0;
      }
      public String getSerialNumber()
      {
         return BitConverter.ToString((Byte[])Microsoft.Phone.Info.DeviceExtendedProperties.GetValue("DeviceUniqueId")).Replace("-", string.Empty);
         // another possibility (longer value)       
         // BitConverter.ToString(Convert.FromBase64String((String)Microsoft.Phone.Info.UserExtendedProperties.GetValue("ANID2"))).Replace("-", string.Empty);
      }
      public int getOSVersion()
      {
         return Environment.OSVersion.Version.Major * 100 + Environment.OSVersion.Version.Minor;
         // note that Environment.OSVersion.ToString() returns "Microsoft Windows NT "+version
      }

      void cameraCaptureTask_Completed(object sender, PhotoResult e)
      {
         if (e.TaskResult == TaskResult.Cancel)
            cameraResult = 0;
         else
         if (e.TaskResult == TaskResult.OK)
         {
            String photoFullPath = e.OriginalFileName;
            int idx = photoFullPath.LastIndexOf('\\');
            String photoName = photoFullPath.Substring(idx + 1);
            using (IsolatedStorageFile myIsolatedStorage = IsolatedStorageFile.GetUserStoreForApplication())
            {
               if (myIsolatedStorage.FileExists(photoName))
                  myIsolatedStorage.DeleteFile(photoName);
               IsolatedStorageFileStream fileStream = myIsolatedStorage.CreateFile(photoName);
               e.ChosenPhoto.CopyTo(fileStream);
               fileStream.Close();
            }
            cameraName = "device/" + photoName;
            cameraResult = 1;
         }
      }
      public void cameraClick()
      {
         if (cameraCaptureTask == null)
         {
            cameraCaptureTask = new CameraCaptureTask();
            cameraCaptureTask.Completed += cameraCaptureTask_Completed;
         }
         cameraResult = -1;
         cameraName = null;
         cameraCaptureTask.Show();
      }
      public int cameraStatus()
      {
         return cameraResult;
      }
      public String cameraFilename()
      {
         return cameraName;
      }
      
      public bool isSipSet()
      {
         bool isPortrait = MainPage.isPortrait;
         return (isPortrait && Settings.portSipH != 0) || (!isPortrait && Settings.landSipH != 0);
      }

      public async Task WriteSettings()
      {
         var file = await Windows.Storage.ApplicationData.Current.LocalFolder.CreateFileAsync("AppSettings.dat", CreationCollisionOption.ReplaceExisting);
         using (var s = await file.OpenStreamForWriteAsync())
         {
            BinaryWriter bw = new BinaryWriter(s);
            bw.Write(Settings.landSipH);
            bw.Write(Settings.portSipH);
         }
      }

      public async Task ReadSettings()
      {
         try
         {
            var file = await Windows.Storage.ApplicationData.Current.LocalFolder.OpenStreamForReadAsync("AppSettings.dat");
            using (var br = new BinaryReader(file))
            {
               Settings.landSipH = br.ReadInt32();
               Settings.portSipH = br.ReadInt32();
            }
         }
         catch
         {
            // do nothing
         }
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

      public void privateAlertCS(String str, bool eventsInitialized) // Vm
      {
         alertIsVisible = true;
         if (!eventsInitialized) // needed if an error occurs at vm startup
            try
            {
               MessageBox.Show(str, "ALERT", MessageBoxButton.OK);
               alertIsVisible = false;
               return;
            }
            catch (System.UnauthorizedAccessException e) { }
         Deployment.Current.Dispatcher.BeginInvoke(() =>
         {
            MessageBox.Show(str, "ALERT", MessageBoxButton.OK);
            alertIsVisible = false;
         });
      }

      public bool isAlertVisible()
      {
         return alertIsVisible;
      }

      public void vmSetAutoOffCS(bool enable) // Vm
      {
         if (!setAutoOffCalled)
            PhoneApplicationService.Current.ApplicationIdleDetectionMode = PhoneApplicationService.Current.UserIdleDetectionMode = enable ? IdleDetectionMode.Enabled : IdleDetectionMode.Disabled;
         setAutoOffCalled = true;
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
      public void appSetFullScreen()
      {
         Deployment.Current.Dispatcher.BeginInvoke(() =>
         {
            SystemTray.IsVisible = false;
         });
      }
      public void appExit()
      {
         Application.Current.Terminate();
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
            try { nativeStopGPSCS(); } catch (Exception) { }
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

      public void nativeSoundPlayCS(String filename)
      {
         //Deployment.Current.Dispatcher.BeginInvoke(() =>
         //{
            try
            {
               if (BackgroundAudioPlayer.Instance.PlayerState == PlayState.Playing)
                  BackgroundAudioPlayer.Instance.Stop();
               BackgroundAudioPlayer.Instance.Track = new AudioTrack(new Uri(filename, UriKind.Absolute), "Title", "Artist", "Album", null/*, "Tag", EnabledPlayerControls.None*/);
               BackgroundAudioPlayer.Instance.Play();
            }
            catch (Exception e) 
            { 
               String s = e.Message;
               s = "";
            }
         //});
      }

      public void nativeStopGPSCS() // GPS
      {
         if (geolocator != null)
         {
            geolocator.PositionChanged -= geolocator_PositionChanged;
            geolocator.StatusChanged -= geolocator_StatusChanged;
            geolocator = null;
         }
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
      public double getPdop()       {return pdop == null ? 0 : (double)pdop;}

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
         bool isPortrait = MainPage.isPortrait;
         currentSipH = visible ? (isPortrait ? Settings.portSipH : Settings.landSipH) : 0;
         if ((visible && currentSipH != 0) || (!visible && currentSipH == 0))
            MainPage.instance.d3dBackground.OnScreenChanged(currentSipH, 0, 0);
      }

      public int getSipHeight()
      {
         return currentSipH;
      }

      public int getScreenSize()
      {
         return MainPage.screenSize;
      }

      public String getAppName()
      {
          return appName;
      }

      public bool showMap(String origin, String destination)
      {
         if (origin.StartsWith("@")) origin = origin.Substring(1);
         if (destination.StartsWith("@")) destination = destination.Substring(1);
         root.Dispatcher.BeginInvoke((Action)(() => // must run on ui thread
         {
            if (destination.Length == 0)
            {
               MapsTask maps = new MapsTask();
               //maps.ZoomLevel = 0.1;
               maps.SearchTerm = origin;
               maps.Show();
            }
            else
            {
               BingMapsDirectionsTask direction = new BingMapsDirectionsTask();
               direction.Start = new LabeledMapLocation(origin, null);
               direction.End = new LabeledMapLocation(destination, null);//new GeoCoordinate(-3.758814, -38.484199));
               direction.Show();
            }
         }));
         return true;
      }
   }

    ////////////////////////////////////////////////////////////////////////////////

    public partial class MainPage : PhoneApplicationPage
    {
        public Direct3DBackground d3dBackground;
        private int specialKey, scale;
        private CSWrapper cs;
        private bool manipulating;
        public static MainPage instance;
        public static bool isPortrait;
        public static int screenSize;

        // Constructor
        public MainPage()
        {
            instance = this;
            scale = System.Windows.Application.Current.Host.Content.ScaleFactor;
            screenSize = scaled(Math.Max(Application.Current.Host.Content.ActualWidth, Application.Current.Host.Content.ActualHeight));
            InitializeComponent();
            this.BackKeyPress += MainPage_BackKeyPress;
            this.MouseMove += MainPage_MouseMove;
            this.MouseLeftButtonDown += MainPage_MouseLeftButtonDown;
            this.MouseLeftButtonUp += MainPage_MouseLeftButtonUp;
            this.SizeChanged += MainPage_SizeChanged;
            this.ManipulationDelta += MainPage_ManipulationDelta;
            this.ManipulationCompleted += MainPage_ManipulationCompleted;
            checkOrientation();
            // InputPane input = InputPane.GetForCurrentView(); input.Showing += input_Showing; input.Hiding += input_Hiding; DOES NOT WORK! METHODS ARE NEVER CALLED. TEST AGAIN ON FUTURE WP VERSIONS
            BeginListenForSIPChanged();
        }

        void MainPage_ManipulationCompleted(object sender, ManipulationCompletedEventArgs e)
        {
           e.Handled = true;
           if (manipulating)
           {
              manipulating = false;
              d3dBackground.OnManipulation(2, 0);
           }
        }

        void MainPage_ManipulationDelta(object sender, ManipulationDeltaEventArgs e)
        {
           e.Handled = true;
           double scale = scaled(isPortrait ? e.DeltaManipulation.Scale.Y : e.DeltaManipulation.Scale.X);
           if (scale != 0)
           {
              if (!manipulating)
              {
                 manipulating = true;
                 d3dBackground.OnManipulation(1, 0);
              }
              d3dBackground.OnManipulation(0, scale);
           }
        }

        public void checkOrientation()
        {
           isPortrait = Orientation == PageOrientation.Portrait || Orientation == PageOrientation.PortraitUp || Orientation == PageOrientation.PortraitDown;
        }

        void MainPage_SizeChanged(object sender, SizeChangedEventArgs e)
        {
           if (d3dBackground == null) return;
           checkOrientation();
           // if sipH isnt set for current rotation, move it to bottom again
           if ((isPortrait && Settings.portSipH == 0) || (!isPortrait && Settings.landSipH == 0))
              instance.cs.tbox.Margin = new Thickness(0, scaled(instance.ActualHeight * 10), 0, 0); // move to bottom
           d3dBackground.OnScreenChanged(-1, scaled(e.NewSize.Width), scaled(e.NewSize.Height));
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
              if (!instance.cs.isSipSet())
              {
                 if (isPortrait)
                    instance.cs.currentSipH = Settings.portSipH = -(int)newvalue;
                 else
                    instance.cs.currentSipH = Settings.landSipH = -(int)newvalue;
                 instance.cs.WriteSettings();
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
            d3dBackground.OnPointerReleased(scaled(p.X), scaled(p.Y));
        }

        void MainPage_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            e.Handled = true;
            Point p = e.GetPosition(this);
            d3dBackground.OnPointerPressed(scaled(p.X), scaled(p.Y));
        }

        void MainPage_MouseMove(object sender, MouseEventArgs e)
        {
            Point p = e.GetPosition(this);
            if (!manipulating)
               d3dBackground.OnPointerMoved(scaled(p.X), scaled(p.Y));
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

        private int scaled(double k)
        {
           return (int)(k * scale / 100);
        }
        private void DrawingSurfaceBackground_Loaded(object sender, RoutedEventArgs e)
        {
           if (d3dBackground == null)
           {
              cs = new CSWrapper(LayoutRoot);
              cs.appName = ReadManifest();
              d3dBackground = new Direct3DBackground(cs);

              cs.tbox.KeyDown += MainPage_KeyDown;
              cs.tbox.TextChanged += tbox_TextChanged;

              int appW = scaled(LayoutRoot.ActualWidth);
              int appH = scaled(LayoutRoot.ActualHeight);
              d3dBackground.OnScreenChanged(-1, appW, appH);
              d3dBackground.WindowBounds = d3dBackground.RenderResolution = d3dBackground.NativeResolution = new Windows.Foundation.Size(appW,appH);
              DrawingSurface.SetContentProvider(d3dBackground.CreateContentProvider());
           }
        }

        public static string ReadManifest()
        {
           try // allow debugging
           {
              XElement xml = XElement.Load("TotalCrossManifest.xml");
              var manifestElement = (from manifest in xml.Descendants("App") select manifest).SingleOrDefault();
              if (manifestElement != null)
              {
                 return manifestElement.Attribute("Name").Value;
              }
           }
           catch (Exception) { }
           return string.Empty;
        }
    }
}