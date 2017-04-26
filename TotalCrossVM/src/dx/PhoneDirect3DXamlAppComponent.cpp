#include "Direct3DContentProvider.h"

#include "cppwrapper.h"
#include "../../event/specialkeys.h"

using namespace Windows::Foundation;
using namespace Windows::UI::Core;
using namespace Microsoft::WRL;
using namespace Windows::Phone::Graphics::Interop;
using namespace Windows::Phone::Input::Interop;

namespace PhoneDirect3DXamlAppComponent
{
static Direct3DBackground^ instance;

Direct3DBackground::Direct3DBackground(CSwrapper ^_cs) : cs(_cs)
{
   instance = this;
}

Direct3DBackground^ Direct3DBackground::GetInstance()
{
   return instance;
}
IDrawingSurfaceContentProvider^ Direct3DBackground::CreateContentProvider()
{
	ComPtr<Direct3DContentProvider> provider = Make<Direct3DContentProvider>(this);
	return reinterpret_cast<IDrawingSurfaceContentProvider^>(provider.Get());
}

bool Direct3DBackground::backKeyPress()
{
	eventQueuePush(KEYEVENT_SPECIALKEY_PRESS, SK_ESCAPE, 0, 0, 0);

	return keepRunning;
}

// Event Handlers
extern "C" 
{
   extern int32 setShiftYonNextUpdateScreen,appW,appH,glShiftY; 
}
void Direct3DBackground::OnPointerPressed(int x, int y)
{
   eventQueuePush(PENEVENT_PEN_DOWN, 0, x, y - glShiftY, -1);
}

void Direct3DBackground::OnPointerMoved(int x, int y)
{
   if (!renderer->updateScreenWaiting) // boosts performance during drag 
      eventQueuePush(PENEVENT_PEN_DRAG, 0, x, y - glShiftY, -1);
   isDragging = true;
}

void Direct3DBackground::OnPointerReleased(int x, int y)
{
   eventQueuePush(PENEVENT_PEN_UP, 0, x, y - glShiftY, -1);
	isDragging = false;
}

void Direct3DBackground::OnKeyPressed(int key)
{
   eventQueuePush(key < 32 ? KEYEVENT_SPECIALKEY_PRESS : KEYEVENT_KEY_PRESS, key < 32 ? keyDevice2Portable(key) : key, 0, 0, -1);
}

void Direct3DBackground::OnManipulation(int type, double delta)
{
   int* pd = (int*)&delta;
   if (!renderer->updateScreenWaiting) // boosts performance during drag 
      eventQueuePush(MULTITOUCHEVENT_SCALE, type, pd[1], pd[0], 0);
}

void Direct3DBackground::OnScreenChanged(int newKeyboardH, int newWidth, int newHeight)
{
   if (newKeyboardH >= 0)
   {
      renderer->sipHeight = newKeyboardH;
      setShiftYonNextUpdateScreen = true;
      if (newKeyboardH == 0)
         eventQueuePush(CONTROLEVENT_SIP_CLOSED, 0, 0, 0, -1);
   }
   else
   {
      appW = newWidth;
      appH = newHeight;
      if (renderer != nullptr)
         eventQueuePush(KEYEVENT_SPECIALKEY_PRESS, SK_SCREEN_CHANGE, 0, 0, -1);
   }
}

void Direct3DBackground::lifeCycle(bool suspending)
{
   updateScreenCalledOnce = false;
   renderer->lifeCycle(suspending);
}

HRESULT Direct3DBackground::PrepareResources(_Out_ BOOL* contentDirty)
{
   // update screen only if necessary
   updateScreenCalledOnce |= renderer->updateScreenWaiting;
   *contentDirty = !updateScreenCalledOnce || renderer->updateScreenWaiting;
	return S_OK;
}

void Direct3DBackground::RequestNewFrame()
{
   Direct3DBackground::RequestAdditionalFrame();
}
}