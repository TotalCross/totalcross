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
   m_synchronizedTexture = nullptr;
}

HRESULT Direct3DContentProvider::PrepareResources(_In_ const LARGE_INTEGER* presentTargetTime, _Out_ BOOL* contentDirty)
{
   return m_controller->PrepareResources(presentTargetTime, contentDirty);
}

HRESULT Direct3DContentProvider::GetTexture(_In_ const DrawingSurfaceSizeF* size, _Out_ IDrawingSurfaceSynchronizedTextureNative** synchronizedTexture, _Out_ DrawingSurfaceRectF* textureSubRectangle)
{
   HRESULT hr = S_OK;
   if (!m_synchronizedTexture)
   {
      m_controller->renderer->updateDevice();
      hr = m_host->CreateSynchronizedTexture(m_controller->GetTexture(), &m_synchronizedTexture);
   }
   // Set output parameters.
   textureSubRectangle->left = textureSubRectangle->top = 0.0f;
   textureSubRectangle->right = static_cast<FLOAT>(size->width);
   textureSubRectangle->bottom = static_cast<FLOAT>(size->height);
   m_synchronizedTexture.CopyTo(synchronizedTexture);

   // Draw to the texture.
   if (SUCCEEDED(hr))
   {
      static int count;
      debug("updating screen texture %d", count++);
      hr = m_synchronizedTexture->BeginDraw();
      if (SUCCEEDED(hr))
         hr = m_controller->updateScreenTexture();
      m_synchronizedTexture->EndDraw();
   }
   return hr;
}
