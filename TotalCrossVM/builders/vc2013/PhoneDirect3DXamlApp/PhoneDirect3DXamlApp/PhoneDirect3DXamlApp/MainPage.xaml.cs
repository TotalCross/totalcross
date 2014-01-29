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

namespace PhoneDirect3DXamlAppInterop
{
    public class dummy : Idummy
    {
        public dummy()
        {

        }
        public MainPage mp { set; get; }
        public void callDraw()
        {
            //mp.Background.Transform.Inverse = true;
            //mp.UpdateLayout();
        }

        public void privateAlertCS(String str) // debug_c.h
        {
           MessageBox.Show(str, "ALERT", MessageBoxButton.OK);
        }

        public void vmSetAutoOffCS(bool enable) // VM_c.h
        {
           if (enable)
              PhoneApplicationService.Current.ApplicationIdleDetectionMode = PhoneApplicationService.Current.UserIdleDetectionMode
                                                                            = IdleDetectionMode.Enabled;
           else
              PhoneApplicationService.Current.ApplicationIdleDetectionMode = PhoneApplicationService.Current.UserIdleDetectionMode
                                                                            = IdleDetectionMode.Disabled;
        }

        public void dialNumberCS(String number) // Dial.c
        {
           PhoneCallTask phoneCallTask = new PhoneCallTask();

           phoneCallTask.PhoneNumber = number;
           phoneCallTask.Show();
        }

        public void smsSendCS(String message, String destination)
        {
           SmsComposeTask smsComposeTask = new SmsComposeTask();

           smsComposeTask.To = destination;
           smsComposeTask.Body = message;
           smsComposeTask.Show();
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

                // Hook-up native component to DrawingSurfaceBackgroundGrid
                DrawingSurfaceBackground.SetBackgroundContentProvider(m_d3dBackground.CreateContentProvider());
                DrawingSurfaceBackground.SetBackgroundManipulationHandler(m_d3dBackground);
            }
        }
    }
}