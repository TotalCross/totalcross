#ifdef __cplusplus

#pragma once

#define HAS_TCHAR

#include <agile.h>
#include "tcvm.h"
#include "esUtil.h"
#include "winrtangle.h"
#include "openglWrapper.h"

namespace TotalCross
{

	ref class MainView sealed : public Windows::ApplicationModel::Core::IFrameworkView
	{
	public:

		MainView();
		MainView(Platform::String ^cmdline, Platform::String ^vmPath, Platform::String ^appPath);

		void setKeyboard(int kb); // enum TCSIP
		static MainView ^GetLastInstance();
		// IFrameworkView Methods.
		virtual void Initialize(Windows::ApplicationModel::Core::CoreApplicationView^ applicationView);
		virtual void SetWindow(Windows::UI::Core::CoreWindow^ window);
		virtual void Load(Platform::String^ entryPoint);
      virtual void Run(void);
      virtual void Uninitialize(void);
      Platform::String ^getAppPath(void);
	  Platform::String ^getVmPath(void);
      Windows::UI::Core::CoreWindow^ GetWindow(void);

	  int GetSIPHeight(void);

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
	  void OnKeyDown(Windows::UI::Core::CoreWindow^ sender, Windows::UI::Core::KeyEventArgs^ args);
	  void OnKeyUp(Windows::UI::Core::CoreWindow^ sender, Windows::UI::Core::KeyEventArgs^ args);
	  void OnPointerWheel(Windows::UI::Core::CoreWindow^ sender, Windows::UI::Core::PointerEventArgs^ args);
	  void OnCharacterReceived(Windows::UI::Core::CoreWindow^ sender, Windows::UI::Core::CharacterReceivedEventArgs^ args);
	  void OnInputEnabled(Windows::UI::Core::CoreWindow^ sender, Windows::UI::Core::InputEnabledEventArgs^ args);

	  void OnTextChange(Windows::Phone::UI::Core::KeyboardInputBuffer ^sender, Windows::Phone::UI::Core::CoreTextChangedEventArgs ^args);
	  void OnBackPressed(Platform::Object ^sender, Windows::Phone::UI::Input::BackPressedEventArgs ^args);
	  void OnHidingSIP(Windows::UI::ViewManagement::InputPane ^sender, Windows::UI::ViewManagement::InputPaneVisibilityEventArgs ^args);
	  void OnShowingSIP(Windows::UI::ViewManagement::InputPane ^sender, Windows::UI::ViewManagement::InputPaneVisibilityEventArgs ^args);
	  void OnSizeChanged(Windows::UI::Core::CoreWindow ^sender, Windows::UI::Core::WindowSizeChangedEventArgs ^args);

	private:
      ESContext m_esContext;
      //Microsoft::WRL::ComPtr<IWinrtEglWindow> m_eglWindow;
	  Windows::Phone::UI::Core::KeyboardInputBuffer^ m_inputBuffer;


      bool m_windowClosed;
      bool m_windowVisible;

	  float lastX, lastY;


      Context local_context;
      char cmdLine[512];
	  Platform::String ^vmPath;
      Platform::String ^appPath;
      Platform::String ^_cmdline;
      Platform::Agile<Windows::UI::Core::CoreWindow> currentWindow;
      Windows::Foundation::Rect bounds;

      void MainView::mainLoop();
	};
}

#endif