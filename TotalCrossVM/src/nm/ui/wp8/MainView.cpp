#include "MainView.h"

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
	strcpy(cmdLine, " /cmd ");
	WideCharToMultiByte(CP_ACP, 0, cmdline->Data(), cmdline->Length(), cmdLine + 7, 512 - 7, NULL, NULL);
	//WideCharToMultiByte(CP_ACP, 0, _appPath->Data(), _appPath->Length(), appPath, 1024 , NULL, NULL);
	appPath = _appPath;
	_cmdline = cmdline;

	lastInstance = this;
}

void MainView::Initialize(CoreApplicationView^ applicationView)
{
	int32 vm_err_code;
	applicationView->Activated +=
		ref new TypedEventHandler<CoreApplicationView^, IActivatedEventArgs^>(this, &MainView::OnActivated);

	CoreApplication::Suspending +=
		ref new EventHandler<SuspendingEventArgs^>(this, &MainView::OnSuspending);

	CoreApplication::Resuming +=
		ref new EventHandler<Platform::Object^>(this, &MainView::OnResuming);

	vm_err_code = startVM(cmdLine, &local_context);
	if (vm_err_code != 0) {
		abort();
	}
	
}

void MainView::SetWindow(CoreWindow^ window)
{
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

}

void MainView::Load(Platform::String^ entryPoint)
{
}

void MainView::Run()
{
	startProgram(local_context);

	while (!m_windowClosed)
	{
		if (m_windowVisible)
		{
			CoreWindow::GetForCurrentThread()->Dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessAllIfPresent);
		}
		else
		{
			CoreWindow::GetForCurrentThread()->Dispatcher->ProcessEvents(CoreProcessEventsOption::ProcessOneAndAllPending);
		}
	}
}

void MainView::Uninitialize()
{
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

	deferral->Complete();
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