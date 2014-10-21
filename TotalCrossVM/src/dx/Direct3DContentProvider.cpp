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
   if (m_controller->renderer == nullptr)
      m_controller->renderer = ref new Direct3DBase(m_controller->cs);
	return S_OK;
}

void Direct3DContentProvider::Disconnect()
{
	m_host = nullptr;
   m_controller->renderer->syncTex = nullptr;
}

extern "C" {extern int appW, appH; }
HRESULT Direct3DContentProvider::PrepareResources(_In_ const LARGE_INTEGER* presentTargetTime, _Out_ BOOL* contentDirty)
{
   if (!m_controller->renderer->syncTex)
   {
      m_controller->renderer->updateDevice(m_host.Get());
      if (m_controller->renderer->minimized)
      {
         m_controller->renderer->minimized = false;
         m_controller->renderer->preRender();
         PhoneDirect3DXamlAppComponent::Direct3DBackground::GetInstance()->OnScreenChanged(-1, appW, appH);
         PhoneDirect3DXamlAppComponent::Direct3DBackground::GetInstance()->RequestNewFrame();
      }
   }
   return m_controller->PrepareResources(contentDirty);
}

HRESULT Direct3DContentProvider::GetTexture(_In_ const DrawingSurfaceSizeF* size, _Out_ IDrawingSurfaceSynchronizedTextureNative** synchronizedTexture, _Out_ DrawingSurfaceRectF* texSubRect)
{
   // Draw to the texture
   if (m_controller->renderer->updateScreenWaiting)
   {
      m_controller->renderer->syncTex->BeginDraw();
      m_controller->renderer->d3dImedContext->ExecuteCommandList(m_controller->renderer->d3dCommandList, FALSE);
      m_controller->renderer->d3dCommandList->Release();
      m_controller->renderer->updateScreenWaiting = false; // once the commandlist is used, we can continue
      m_controller->renderer->syncTex->EndDraw();
      //{static int lastm, nzero;  int mm = getFreeMemory(0); if (mm == lastm) nzero++; else { debug("--->>> last refresh: %d (%d) - #0: %d", (lastm - mm) / 1024, mm / 1024, nzero); lastm = mm; nzero = 0; }}
   }
   // Set output parameters
   texSubRect->left = texSubRect->top = 0.0f; texSubRect->right = (float)size->width; texSubRect->bottom = (float)size->height;
   m_controller->renderer->syncTex.CopyTo(synchronizedTexture);
   //{static ULONGLONG last; ULONGLONG cur = GetTickCount64(); debug("elapsed: %d", (int)(cur - last)); last = cur;}
   return S_OK;
}
