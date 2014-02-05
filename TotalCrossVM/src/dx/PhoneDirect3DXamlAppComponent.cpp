#include "Direct3DContentProvider.h"

#include "cppwrapper.h"

static float lastX, lastY;
static float glShiftY = 0.0f;

using namespace Windows::Foundation;
using namespace Windows::UI::Core;
using namespace Microsoft::WRL;
using namespace Windows::Phone::Graphics::Interop;
using namespace Windows::Phone::Input::Interop;

namespace PhoneDirect3DXamlAppComponent
{

   Direct3DBackground::Direct3DBackground(CSwrapper ^_cs) : cs(_cs)
{
}

IDrawingSurfaceBackgroundContentProvider^ Direct3DBackground::CreateContentProvider()
{
	ComPtr<Direct3DContentProvider> provider = Make<Direct3DContentProvider>(this);
	return reinterpret_cast<IDrawingSurfaceBackgroundContentProvider^>(provider.Get());
}

bool Direct3DBackground::backKeyPress()
{
	eventQueuePush(KEYEVENT_SPECIALKEY_PRESS, SK_ESCAPE, 0, 0, 0);

	return keepRunning;
}

// IDrawingSurfaceManipulationHandler
void Direct3DBackground::SetManipulationHost(DrawingSurfaceManipulationHost^ manipulationHost)
{
	manipulationHost->PointerPressed +=
		ref new TypedEventHandler<DrawingSurfaceManipulationHost^, PointerEventArgs^>(this, &Direct3DBackground::OnPointerPressed);

	manipulationHost->PointerMoved +=
		ref new TypedEventHandler<DrawingSurfaceManipulationHost^, PointerEventArgs^>(this, &Direct3DBackground::OnPointerMoved);

	manipulationHost->PointerReleased +=
		ref new TypedEventHandler<DrawingSurfaceManipulationHost^, PointerEventArgs^>(this, &Direct3DBackground::OnPointerReleased);
	m_renderer->setManipulationComplete();
}

// Event Handlers
void Direct3DBackground::OnPointerPressed(DrawingSurfaceManipulationHost^ sender, PointerEventArgs^ args)
{
	auto pos = args->CurrentPoint->Position;
	//debug("pressed lastY %.2f lastX %.2f Y %.2f X %.2f", lastY, lastX, pos.Y, pos.X);
   eventQueuePush(PENEVENT_PEN_DOWN, 0, (int32)(lastX = pos.X), (int32)(lastY = pos.Y - glShiftY), -1);
}

void Direct3DBackground::OnPointerMoved(DrawingSurfaceManipulationHost^ sender, PointerEventArgs^ args)
{
	// Insert your code here.
	auto pos = args->CurrentPoint->Position;
	if (lastX != pos.X || lastY != pos.Y) 
   {
		//debug("moving lastY %.2f lastX %.2f Y %.2f X %.2f", lastY, lastX, pos.Y, pos.X);
      eventQueuePush(PENEVENT_PEN_DRAG, 0, (int32)(lastX = pos.X), (int32)(lastY = pos.Y - glShiftY), -1);
		isDragging = true;
	}
}

void Direct3DBackground::OnPointerReleased(DrawingSurfaceManipulationHost^ sender, PointerEventArgs^ args)
{
	// Insert your code here.
	auto pos = args->CurrentPoint->Position;
   eventQueuePush(PENEVENT_PEN_UP, 0, (int32)(lastX = pos.X), (int32)(lastY = pos.Y - glShiftY), -1);
	isDragging = false;
}

// Interface With Direct3DContentProvider
HRESULT Direct3DBackground::Connect(_In_ IDrawingSurfaceRuntimeHostNative* host, _In_ ID3D11Device1* device)
{
   m_renderer = ref new Direct3DBase(cs);
	m_renderer->Initialize(device);
	m_renderer->UpdateForWindowSizeChange(WindowBounds.Width, WindowBounds.Height);
	return S_OK;
}

void Direct3DBackground::Disconnect()
{
	m_renderer = nullptr;
}

HRESULT Direct3DBackground::PrepareResources(_In_ const LARGE_INTEGER* presentTargetTime, _Inout_ DrawingSurfaceSizeF* desiredRenderTargetSize)
{
	desiredRenderTargetSize->width = RenderResolution.Width;
	desiredRenderTargetSize->height = RenderResolution.Height;

	return S_OK;
}

HRESULT Direct3DBackground::Draw(_In_ ID3D11Device1* device, _In_ ID3D11DeviceContext1* context, _In_ ID3D11RenderTargetView* renderTargetView)
{
	if (!m_renderer->isLoadCompleted()) {
	   m_renderer->UpdateDevice(device, context, renderTargetView);
	   RequestAdditionalFrame();
	}
	else 
   {
      Platform::String^ alertMsg;

		m_renderer->UpdateDevice(device, context, renderTargetView);
		m_renderer->PreRender();
		m_renderer->Render();

		m_renderer->DoneDrawCommand();

		while (m_renderer->WaitDrawCommand() != DRAW_COMMAND_PRESENT) {
			Sleep(OCCUPIED_WAIT_TIME);
		}
      
      alertMsg = m_renderer->GetAlertMsg();
      if (alertMsg != nullptr)

         Direct3DBase::GetLastInstance()->getCSwrapper()->privateAlertCS(alertMsg);
      alertMsg = nullptr;
		RequestAdditionalFrame();
	}

	return S_OK;
}

}