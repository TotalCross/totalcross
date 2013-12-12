#include <wrl/client.h>

#include "esUtil.h"

#if (_MSC_VER >= 1800)
#include <d3d11_2.h>
#else
#include <d3d11_1.h>
#endif

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

//#include "GLES2/gl2.h"
//#include "GLES2/gl2ext.h"

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
}

MainView::MainView(String ^cmdline, String ^_appPath) :
m_windowClosed(false),
m_windowVisible(true)
{
	strcpy(cmdLine, "UIControls /cmd ");
	WideCharToMultiByte(CP_ACP, 0, cmdline->Data(), cmdline->Length(), cmdLine + strlen(cmdLine), 512 - strlen(cmdLine), NULL, NULL);
	//WideCharToMultiByte(CP_ACP, 0, _appPath->Data(), _appPath->Length(), appPath, 1024 , NULL, NULL);
	appPath = _appPath;
	_cmdline = cmdline;

	lastInstance = this;
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
   m_inputBuffer = ref new Windows::Phone::UI::Core::KeyboardInputBuffer();

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
	window->KeyDown +=
		ref new TypedEventHandler<CoreWindow^, KeyEventArgs^>(this, &MainView::OnKeyDown);

//#if (_MSC_VER >= 1800)
//	// WinRT on Windows 8.1 can compile shaders at run time so we don't care about the DirectX feature level
//	auto featureLevel = ANGLE_D3D_FEATURE_LEVEL::ANGLE_D3D_FEATURE_LEVEL_ANY;
//#elif WINAPI_FAMILY_PARTITION(WINAPI_PARTITION_PHONE)
//	// Windows Phone 8.0 uses D3D_FEATURE_LEVEL_9_3
//	auto featureLevel = ANGLE_D3D_FEATURE_LEVEL::ANGLE_D3D_FEATURE_LEVEL_9_3;
//#endif 
//
//	HRESULT result = CreateWinrtEglWindow(WINRT_EGL_IUNKNOWN(CoreWindow::GetForCurrentThread()), featureLevel, m_eglWindow.GetAddressOf());
//	if (SUCCEEDED(result))
//	{
//		m_esContext.hWnd = m_eglWindow;
//
//		//title, width, and height are unused, but included for backwards compatibility
//		esCreateWindow(&m_esContext, nullptr, 0, 0, ES_WINDOW_RGB | ES_WINDOW_DEPTH);
//
//		m_cubeRenderer.CreateResources();
//	}
}

void MainView::Load(Platform::String^ entryPoint)
{

}

void MainView::Run()
{
	BasicTimer^ timer = ref new BasicTimer();
	int32 vm_err_code = startVM(cmdLine, &local_context);
	if (vm_err_code != 0)
	{
		m_windowClosed = true;
	}


	//currentWindow.Get()->IsKeyboardInputEnabled = true;
	set_dispatcher();

   if (!m_windowClosed)
      startProgram(local_context);
   /*t = std::thread([=]{mainLoop(); });

   m_windowClosed = false;
   while (!m_windowClosed)
   {
      if (m_windowVisible)
      {
		  //callLastDrawText();
		  //timer->Update();
		  CoreWindow::GetForCurrentThread()->Dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessAllIfPresent);
		  //updateScreenANGLE();
		  //m_cubeRenderer.Update(timer->Total, timer->Delta);
		  //m_cubeRenderer.Render();
		  //eglSwapBuffers(m_esContext.eglDisplay, m_esContext.eglSurface);
         //x->Dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessAllIfPresent);
         //DisplayDX();
      }
      else
      {
		  CoreWindow::GetForCurrentThread()->Dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessOneAndAllPending);
         //x->Dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessOneAndAllPending);
      }
   }*/
}

void MainView::mainLoop()
{
   if (!m_windowClosed)
      startProgram(local_context);
}

void MainView::Uninitialize()
{
   m_windowClosed = true;
}

void MainView::OnKeyDown(CoreWindow ^sender, KeyEventArgs ^args)
{
	char buffer[1024];
	int i, l;

	auto s = args->VirtualKey.ToString();
	auto cp = s->Data();
	l = s->Length();

	debug("tecla pressionada len \"%d\"", l);

	for (i = 0; i < l; i++) {
		buffer[i] = (char)cp[i];
	}
	buffer[i] = '\0';
	debug("buff %s", buffer);

	debug("lalala %d", (int)args->VirtualKey);

	switch (args->VirtualKey) {
#define macro_key(key) case Windows::System::VirtualKey::##key##: debug("pressionado " #key ", seu hexadecimal: %x", (int)Windows::System::VirtualKey::##key); break
		macro_key(Accept);
		macro_key(Add);
		macro_key(Back);
		macro_key(Cancel);
		macro_key(CapitalLock);
		macro_key(Clear);
		macro_key(Control);
		macro_key(Convert);
		macro_key(Decimal);
		macro_key(Delete);
		macro_key(Divide);
		macro_key(Down);
		macro_key(End);
		macro_key(Enter);
		macro_key(Escape);
		macro_key(Execute);
		macro_key(F1);
		macro_key(F2);
		macro_key(F3);
		macro_key(F4);
		macro_key(F5);
		macro_key(F6);
		macro_key(F7);
		macro_key(F8);
		macro_key(F9);
		macro_key(F10);
		macro_key(F11);
		macro_key(F12);
		macro_key(F13);
		macro_key(F14);
		macro_key(F15);
		macro_key(F16);
		macro_key(F17);
		macro_key(F18);
		macro_key(F19);
		macro_key(F20);
		macro_key(F21);
		macro_key(F22);
		macro_key(F23);
		macro_key(F24);
		macro_key(Final);
		macro_key(Hangul);
		macro_key(Hanja);
		macro_key(Help);
		macro_key(Home);
		macro_key(Insert);
		macro_key(Junja);
		//macro_key(Kana);
		//macro_key(Kanji);
		macro_key(Left);
		macro_key(LeftButton);
		macro_key(LeftControl);
		macro_key(LeftMenu);
		macro_key(LeftShift);
		macro_key(LeftWindows);
		macro_key(Menu);
		macro_key(MiddleButton);
		macro_key(ModeChange);
		macro_key(Multiply);
		macro_key(NonConvert);
		macro_key(None);
		macro_key(Number0);
		macro_key(Number1);
		macro_key(Number2);
		macro_key(Number3);
		macro_key(Number4);
		macro_key(Number5);
		macro_key(Number6);
		macro_key(Number7);
		macro_key(Number8);
		macro_key(Number9);
		macro_key(NumberKeyLock);
		macro_key(NumberPad0);
		macro_key(NumberPad1);
		macro_key(NumberPad2);
		macro_key(NumberPad3);
		macro_key(NumberPad4);
		macro_key(NumberPad5);
		macro_key(NumberPad6);
		macro_key(NumberPad7);
		macro_key(NumberPad8);
		macro_key(NumberPad9);
		macro_key(PageUp);
		macro_key(PageDown);
		macro_key(Pause);
		macro_key(Print);
		macro_key(Right);
		macro_key(RightButton);
		macro_key(RightControl);
		macro_key(RightMenu);
		macro_key(RightShift);
		macro_key(RightWindows);
		macro_key(Scroll);
		macro_key(Select);
		macro_key(Separator);
		macro_key(Shift);
		macro_key(Sleep);
		macro_key(Snapshot);
		macro_key(Space);
		macro_key(Subtract);
		macro_key(Tab);
		macro_key(Up);
		macro_key(XButton1);
		macro_key(XButton2);
		//Windows::System::VirtualKey::
#undef macro_key
	default:
		debug("pressionado não reconhecido, seu hexa %x", (int) args->VirtualKey);
	}
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
	auto pos = args->CurrentPoint->Position;
	debug("pressed lastY %.2f lastX %.2f Y %.2f X %.2f", lastY, lastX, pos.Y, pos.X);
	postEvent(mainContext, PENEVENT_PEN_DOWN, 0, lastX = pos.X, lastY = pos.Y, -1);
}

void MainView::OnPointerMoved(CoreWindow^ sender, PointerEventArgs^ args)
{
	// Insert your code here.
	auto pos = args->CurrentPoint->Position;
	if (lastX != pos.X || lastY != pos.Y) {
		debug("lastY %.2f lastX %.2f Y %.2f X %.2f", lastY, lastX, pos.Y, pos.X);
		postEvent(mainContext, PENEVENT_PEN_DRAG, 0, lastX = pos.X, lastY = pos.Y, -1);
		isDragging = true;
	}
}

void MainView::OnPointerReleased(CoreWindow^ sender, PointerEventArgs^ args)
{
	auto pos = args->CurrentPoint->Position;
	postEvent(mainContext, PENEVENT_PEN_UP, 0, lastX = pos.X, lastY =  pos.Y, -1);
	isDragging = false;
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

void MainView::setKeyboard(bool state)
{
	currentWindow.Get()->IsKeyboardInputEnabled = state;
//	throw(1);
}