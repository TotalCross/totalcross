#include "CubeRenderer.h"
#include "MainView.h"

#include <thread>
#include "winrtangle.h"

using namespace TotalCross;

using namespace Windows::ApplicationModel::Core;

using namespace Platform;

using namespace Windows::ApplicationModel;
using namespace Windows::ApplicationModel::Core;
using namespace Windows::ApplicationModel::Activation;
using namespace Windows::UI::Core;
using namespace Windows::Foundation;
using namespace Windows::Graphics::Display;

static MainView ^lastInstance = nullptr;

std::thread t;

//#include "GLES2/gl2.h"

//#include "GLES2/gl2ext.h"


//XXX

// Helper class for basic timing.
ref class BasicTimer sealed
{
public:
	// Initializes internal timer values.
	BasicTimer()
	{
		if (!QueryPerformanceFrequency(&m_frequency))
		{
			throw ref new Platform::FailureException();
		}
		Reset();
	}

	// Reset the timer to initial values.
	void Reset()
	{
		Update();
		m_startTime = m_currentTime;
		m_total = 0.0f;
		m_delta = 1.0f / 60.0f;
	}

	// Update the timer's internal values.
	void Update()
	{
		if (!QueryPerformanceCounter(&m_currentTime))
		{
			throw ref new Platform::FailureException();
		}

		m_total = static_cast<float>(
			static_cast<double>(m_currentTime.QuadPart - m_startTime.QuadPart) /
			static_cast<double>(m_frequency.QuadPart)
			);

		if (m_lastTime.QuadPart == m_startTime.QuadPart)
		{
			// If the timer was just reset, report a time delta equivalent to 60Hz frame time.
			m_delta = 1.0f / 60.0f;
		}
		else
		{
			m_delta = static_cast<float>(
				static_cast<double>(m_currentTime.QuadPart - m_lastTime.QuadPart) /
				static_cast<double>(m_frequency.QuadPart)
				);
		}

		m_lastTime = m_currentTime;
	}

	// Duration in seconds between the last call to Reset() and the last call to Update().
	property float Total
	{
		float get() { return m_total; }
	}

	// Duration in seconds between the previous two calls to Update().
	property float Delta
	{
		float get() { return m_delta; }
	}

private:
	LARGE_INTEGER m_frequency;
	LARGE_INTEGER m_currentTime;
	LARGE_INTEGER m_startTime;
	LARGE_INTEGER m_lastTime;
	float m_total;
	float m_delta;
};
//XXX

// rotating cube

MainView::MainView() :
m_windowClosed(false),
m_windowVisible(true)
{
	lastInstance = this;
   //currentDirect3DBase = nullptr;
}

MainView::MainView(String ^cmdline, String ^_appPath) :
m_windowClosed(false),
m_windowVisible(true)
{
	strcpy(cmdLine, " /cmd ");
	WideCharToMultiByte(CP_ACP, 0, cmdline->Data(), cmdline->Length(), cmdLine + 7, 512 - 7, NULL, NULL);
	//WideCharToMultiByte(CP_ACP, 0, _appPath->Data(), _appPath->Length(), appPath, 1024 , NULL, NULL);
	appPath = _appPath;
	_cmdline = cmdline;

	lastInstance = this;
   //currentDirect3DBase = nullptr;
}

void MainView::Initialize(CoreApplicationView^ applicationView)
{
	applicationView->Activated +=
		ref new TypedEventHandler<CoreApplicationView^, IActivatedEventArgs^>(this, &MainView::OnActivated);

	CoreApplication::Suspending +=
		ref new EventHandler<SuspendingEventArgs^>(this, &MainView::OnSuspending);

	CoreApplication::Resuming +=
		ref new EventHandler<Platform::Object^>(this, &MainView::OnResuming);
}

void MainView::SetWindow(CoreWindow^ window)
{
   currentWindow = window;
   SetBounds();

	window->VisibilityChanged +=
		ref new TypedEventHandler<CoreWindow^, VisibilityChangedEventArgs^>(this, &MainView::OnVisibilityChanged);

	window->Closed +=
		ref new TypedEventHandler<CoreWindow^, CoreWindowEventArgs^>(this, &MainView::OnWindowClosed);

	window->PointerPressed +=
		ref new TypedEventHandler<CoreWindow^, PointerEventArgs^>(this, &MainView::OnPointerPressed);

	window->PointerMoved +=
		ref new TypedEventHandler<CoreWindow^, PointerEventArgs^>(this, &MainView::OnPointerMoved);

	window->PointerReleased +=
		ref new TypedEventHandler<CoreWindow^, PointerEventArgs^>(this, &MainView::OnPointerReleased);

#if (_MSC_VER >= 1800)
	// WinRT on Windows 8.1 can compile shaders at run time so we don't care about the DirectX feature level
	auto featureLevel = ANGLE_D3D_FEATURE_LEVEL::ANGLE_D3D_FEATURE_LEVEL_ANY;
#elif WINAPI_FAMILY_PARTITION(WINAPI_PARTITION_PHONE)
	// Windows Phone 8.0 uses D3D_FEATURE_LEVEL_9_3
	auto featureLevel = ANGLE_D3D_FEATURE_LEVEL::ANGLE_D3D_FEATURE_LEVEL_9_3;
#endif 

	HRESULT result = CreateWinrtEglWindow(WINRT_EGL_IUNKNOWN(CoreWindow::GetForCurrentThread()), featureLevel, m_eglWindow.GetAddressOf());
	if (SUCCEEDED(result))
	{
		m_esContext.hWnd = m_eglWindow;

		//title, width, and height are unused, but included for backwards compatibility
		esCreateWindow(&m_esContext, nullptr, 0, 0, ES_WINDOW_RGB | ES_WINDOW_DEPTH);

		m_cubeRenderer.CreateResources();
	}
}

void MainView::Load(Platform::String^ entryPoint)
{

}

void MainView::Run()
{
	BasicTimer^ timer = ref new BasicTimer();

   auto x = CoreWindow::GetForCurrentThread();
   t = std::thread([=]{mainLoop(); });

   m_windowClosed = false;
   while (!m_windowClosed)
   {
      if (m_windowVisible)
      {
		  timer->Update();
		  CoreWindow::GetForCurrentThread()->Dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessAllIfPresent);
		  m_cubeRenderer.Update(timer->Total, timer->Delta);
		  m_cubeRenderer.Render();
		  eglSwapBuffers(m_esContext.eglDisplay, m_esContext.eglSurface);
         //x->Dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessAllIfPresent);
         //DisplayDX();
      }
      else
      {
		  CoreWindow::GetForCurrentThread()->Dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessOneAndAllPending);
         //x->Dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessOneAndAllPending);
      }
   }
}

void MainView::mainLoop()
{
   int32 vm_err_code = startVM(cmdLine, &local_context);
   if (vm_err_code != 0)
   {
      m_windowClosed = true;
   }

   if (!m_windowClosed)
      startProgram(local_context);
}

void MainView::Uninitialize()
{
   m_windowClosed = true;
   t.join();
}

void MainView::OnVisibilityChanged(CoreWindow^ sender, VisibilityChangedEventArgs^ args)
{
	m_windowVisible = args->Visible;
}

void MainView::OnWindowClosed(CoreWindow^ sender, CoreWindowEventArgs^ args)
{
	m_windowClosed = true;
}

void MainView::OnPointerPressed(CoreWindow^ sender, PointerEventArgs^ args)
{
	// Insert your code here.
}

void MainView::OnPointerMoved(CoreWindow^ sender, PointerEventArgs^ args)
{
	// Insert your code here.
}

void MainView::OnPointerReleased(CoreWindow^ sender, PointerEventArgs^ args)
{
	// Insert your code here.
}

void MainView::OnActivated(CoreApplicationView^ applicationView, IActivatedEventArgs^ args)
{
	CoreWindow::GetForCurrentThread()->Activate();
}

void MainView::OnSuspending(Platform::Object^ sender, SuspendingEventArgs^ args)
{
   // Save app state asynchronously after requesting a deferral. Holding a deferral
   // indicates that the application is busy performing suspending operations. Be
   // aware that a deferral may not be held indefinitely. After about five seconds,
   // the app will be forced to exit.
   SuspendingDeferral^ deferral = args->SuspendingOperation->GetDeferral();
   
   //if (currentDirect3DBase)
    //  currentDirect3DBase->ReleaseResourcesForSuspending();

   //create_task([this, deferral]()
   //{
      // Insert your code here.

      deferral->Complete();
   //});
}

void MainView::OnResuming(Platform::Object^ sender, Platform::Object^ args)
{
	// Restore any data or state that was unloaded on suspend. By default, data
	// and state are persisted when resuming from suspend. Note that this event
	// does not occur if the app was previously terminated.
}

MainView ^MainView::GetLastInstance()
{
	return lastInstance;
}

String ^MainView::getAppPath()
{
	return appPath;
}

Windows::UI::Core::CoreWindow^ MainView::GetWindow()
{
   return currentWindow.Get();
}

/*Direct3DBase^ MainView::getDirect3DBase()
{
   return currentDirect3DBase;
}

void MainView::setDirect3DBase(Direct3DBase^ direct3DBase)
{
   currentDirect3DBase = direct3DBase;
}*/

void MainView::setBounds(void)
{
   bounds = currentWindow->Bounds;
}

Rect MainView::getBounds(void)
{
   return bounds;
}

LONGLONG MainView::getEglWindow()
{
	return (LONGLONG)(void*)m_eglWindow.GetAddressOf();
}