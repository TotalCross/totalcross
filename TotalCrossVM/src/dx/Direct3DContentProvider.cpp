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
   return m_controller->PrepareResources(presentTargetTime, contentDirty);
}

HRESULT Direct3DContentProvider::GetTexture(_In_ const DrawingSurfaceSizeF* size, _Out_ IDrawingSurfaceSynchronizedTextureNative** synchronizedTexture, _Out_ DrawingSurfaceRectF* texSubRect)
{
   if (!m_controller->renderer->syncTex)
      m_controller->renderer->updateDevice(m_host.Get());
   Microsoft::WRL::ComPtr<IDrawingSurfaceSynchronizedTextureNative> syncTex = m_controller->renderer->syncTex;
   // Set output parameters.
   texSubRect->left = texSubRect->top = 0.0f; texSubRect->right = (float)size->width; texSubRect->bottom = (float)size->height;
   syncTex.CopyTo(synchronizedTexture);
   // Draw to the texture.
   static int count; debug("updating screen texture %d", count++);
   syncTex->BeginDraw();
   m_controller->updateScreenTexture();
   syncTex->EndDraw();
   return S_OK;
}
