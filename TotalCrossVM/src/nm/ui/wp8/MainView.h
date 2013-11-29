#ifdef __cplusplus

#pragma once

#define HAS_TCHAR

#include <agile.h>
#include "Direct3DBase.h"
#include "tcvm.h"

namespace TotalCross
{

	ref class MainView sealed : public Windows::ApplicationModel::Core::IFrameworkView
	{
	public:
		MainView();
		MainView(Platform::String ^cmdline, Platform::String ^appPath);

		static MainView ^GetLastInstance();
		// IFrameworkView Methods.
		virtual void Initialize(Windows::ApplicationModel::Core::CoreApplicationView^ applicationView);
		virtual void SetWindow(Windows::UI::Core::CoreWindow^ window);
		virtual void Load(Platform::String^ entryPoint);
      virtual void Run(void);
      virtual void Uninitialize(void);
      Platform::String ^getAppPath(void);
      Windows::UI::Core::CoreWindow^ GetWindow(void);
      Direct3DBase^ getDirect3DBase(void);
      void setDirect3DBase(Direct3DBase^ direct3DBase);
      void setBounds(void);
      Windows::Foundation::Rect getBounds(void);

	protected:
		// Event Handlers.
		void OnActivated(Windows::ApplicationModel::Core::CoreApplicationView^ applicationView, Windows::ApplicationModel::Activation::IActivatedEventArgs^ args);
		void OnSuspending(Platform::Object^ sender, Windows::ApplicationModel::SuspendingEventArgs^ args);
		void OnResuming(Platform::Object^ sender, Platform::Object^ args);
		void OnWindowClosed(Windows::UI::Core::CoreWindow^ sender, Windows::UI::Core::CoreWindowEventArgs^ args);
		void OnVisibilityChanged(Windows::UI::Core::CoreWindow^ sender, Windows::UI::Core::VisibilityChangedEventArgs^ args);
		void OnPointerPressed(Windows::UI::Core::CoreWindow^ sender, Windows::UI::Core::PointerEventArgs^ args);
		void OnPointerMoved(Windows::UI::Core::CoreWindow^ sender, Windows::UI::Core::PointerEventArgs^ args);
		void OnPointerReleased(Windows::UI::Core::CoreWindow^ sender, Windows::UI::Core::PointerEventArgs^ args);

	private:
		bool m_windowClosed;
		bool m_windowVisible;

		Context local_context;
		char cmdLine[512];
		//char appPath[1024];
		Platform::String ^appPath;
		Platform::String ^_cmdline;
      Platform::Agile<Windows::UI::Core::CoreWindow> currentWindow;
      Windows::Foundation::Rect bounds;
      Direct3DBase^ currentDirect3DBase;

      void MainView::mainLoop();
	};
}

#endif