#include "Direct3DContentProvider.h"

using namespace PhoneDirect3DXamlAppComponent;

Direct3DContentProvider::Direct3DContentProvider(Direct3DBackground^ controller) :
	m_controller(controller)
{
	m_controller->RequestAdditionalFrame += ref new RequestAdditionalFrameHandler([=] ()
		{
			if (m_host)
			{
				m_host->RequestAdditionalFrame();
			}
		});
}

// IDrawingSurfaceContentProviderNative interface
HRESULT Direct3DContentProvider::Connect(_In_ IDrawingSurfaceRuntimeHostNative* host)
{
	m_host = host;
	return m_controller->Connect(host);
}

void Direct3DContentProvider::Disconnect()
{
	m_host = nullptr;
   m_controller->renderer->syncTex = nullptr;
}

HRESULT Direct3DContentProvider::PrepareResources(_In_ const LARGE_INTEGER* presentTargetTime, _Out_ BOOL* contentDirty)
{
   if (!m_controller->renderer->syncTex)
      m_controller->renderer->updateDevice(m_host.Get());
   return m_controller->PrepareResources(contentDirty);
}

HRESULT Direct3DContentProvider::GetTexture(_In_ const DrawingSurfaceSizeF* size, _Out_ IDrawingSurfaceSynchronizedTextureNative** synchronizedTexture, _Out_ DrawingSurfaceRectF* texSubRect)
{
   // Draw to the texture
   if (m_controller->renderer->updateScreenWaiting)
   {
      m_controller->renderer->syncTex->BeginDraw();
      m_controller->renderer->d3dImedContext->ExecuteCommandList(m_controller->renderer->d3dCommandList, FALSE);
      m_controller->renderer->updateScreenWaiting = false; // once the commandlist is used, we can continue
      m_controller->renderer->syncTex->EndDraw();
   }
   // Set output parameters
   texSubRect->left = texSubRect->top = 0.0f; texSubRect->right = (float)size->width; texSubRect->bottom = (float)size->height;
   m_controller->renderer->syncTex.CopyTo(synchronizedTexture);
   //{static ULONGLONG last; ULONGLONG cur = GetTickCount64(); debug("elapsed: %d", (int)(cur - last)); last = cur;}
   //if (m_controller->renderer->alertMsg != nullptr) { Direct3DBase::getLastInstance()->csharp->privateAlertCS(m_controller->renderer->alertMsg); m_controller->renderer->alertMsg = nullptr; } // alert stuff
   return S_OK;
}
