#pragma once

#include "Direct3DBase.h"
#include <DrawingSurfaceNative.h>
#include "cswrapper.h"

namespace PhoneDirect3DXamlAppComponent
{
public delegate void RequestAdditionalFrameHandler();

[Windows::Foundation::Metadata::WebHostHidden]
public ref class Direct3DBackground sealed
{
public:
   Direct3DBackground(CSwrapper ^_cs);

	Windows::Phone::Graphics::Interop::IDrawingSurfaceContentProvider^ CreateContentProvider();

	event RequestAdditionalFrameHandler^ RequestAdditionalFrame;

	property Windows::Foundation::Size WindowBounds;
	property Windows::Foundation::Size NativeResolution;
	property Windows::Foundation::Size RenderResolution;

	// Forward the back key press event to the application; return false when the app should quit
	bool backKeyPress();
   static Direct3DBackground^ GetInstance();
   void RequestNewFrame();

	void OnPointerPressed(int x, int y);
   void OnPointerReleased(int x, int y);
   void OnPointerMoved(int x, int y);
   void OnKeyPressed(int key);
   void OnManipulation(int type, double delta);
   void OnScreenChanged(int newKeyboardH, int newWidth, int newHeight);
   void lifeCycle(bool suspending);

protected:


internal:
   HRESULT STDMETHODCALLTYPE PrepareResources(_Out_ BOOL* contentDirty);
   Direct3DBase^ renderer;
   CSwrapper ^cs;

private:
   bool updateScreenCalledOnce;
};

}