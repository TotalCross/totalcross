#pragma once

#include <wrl/module.h>
#include <Windows.Phone.Graphics.Interop.h>
#include <DrawingSurfaceNative.h>

#include "PhoneDirect3DXamlAppComponent.h"

class Direct3DContentProvider : public Microsoft::WRL::RuntimeClass<
		Microsoft::WRL::RuntimeClassFlags<Microsoft::WRL::WinRtClassicComMix>,
		ABI::Windows::Phone::Graphics::Interop::IDrawingSurfaceContentProvider,
		IDrawingSurfaceContentProviderNative>
{
public:
	Direct3DContentProvider(PhoneDirect3DXamlAppComponent::Direct3DBackground^ controller);

	// IDrawingSurfaceContentProviderNative
	HRESULT STDMETHODCALLTYPE Connect(_In_ IDrawingSurfaceRuntimeHostNative* host);
	void STDMETHODCALLTYPE Disconnect();

   HRESULT STDMETHODCALLTYPE PrepareResources(_In_ const LARGE_INTEGER* presentTargetTime, _Out_ BOOL* contentDirty);
   HRESULT STDMETHODCALLTYPE GetTexture(_In_ const DrawingSurfaceSizeF* size, _Out_ IDrawingSurfaceSynchronizedTextureNative** synchronizedTexture, _Out_ DrawingSurfaceRectF* textureSubRectangle);
	//HRESULT STDMETHODCALLTYPE Draw(_In_ ID3D11Device1* device, _In_ ID3D11DeviceContext1* context, _In_ ID3D11RenderTargetView* renderTargetView);

private:
	PhoneDirect3DXamlAppComponent::Direct3DBackground^ m_controller;
	Microsoft::WRL::ComPtr<IDrawingSurfaceRuntimeHostNative> m_host;
   Microsoft::WRL::ComPtr<IDrawingSurfaceSynchronizedTextureNative> m_synchronizedTexture,syncTex1,syncTex2;
};