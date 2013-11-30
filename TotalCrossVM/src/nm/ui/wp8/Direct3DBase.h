#pragma once
#include "DirectXHelper.h"
/*
struct ModelViewProjectionConstantBuffer
{
   DirectX::XMFLOAT4X4 model;
   DirectX::XMFLOAT4X4 view;
   DirectX::XMFLOAT4X4 projection;
};

struct VertexPositionColor
{
   DirectX::XMFLOAT3 pos;
   DirectX::XMFLOAT3 color;
};

// Helper class that initializes DirectX APIs for 3D rendering.
ref class Direct3DBase sealed
{
public:
   Direct3DBase(Windows::UI::Core::CoreWindow^ window);

	virtual void HandleDeviceLost();
	virtual void CreateDeviceResources();
	virtual void CreateWindowSizeDependentResources();
	virtual void UpdateForWindowSizeChange();
	virtual void ReleaseResourcesForSuspending();
	virtual void Render();
	virtual void Present();
	virtual float ConvertDipsToPixels(float dips);
   virtual void ReleaseDX(void);

protected private:
	// Direct3D Objects.
	Microsoft::WRL::ComPtr<ID3D11Device1> m_d3dDevice;
	Microsoft::WRL::ComPtr<ID3D11DeviceContext1> m_d3dContext;
	Microsoft::WRL::ComPtr<IDXGISwapChain1> m_swapChain;
	Microsoft::WRL::ComPtr<ID3D11RenderTargetView> m_renderTargetView;
	Microsoft::WRL::ComPtr<ID3D11DepthStencilView> m_depthStencilView;

	// Cached renderer properties.
	D3D_FEATURE_LEVEL m_featureLevel;
	Windows::Foundation::Size m_renderTargetSize;
	Windows::Foundation::Rect m_windowBounds;
	Platform::Agile<Windows::UI::Core::CoreWindow> m_window;

   Microsoft::WRL::ComPtr<ID3D11InputLayout> m_inputLayout;
   Microsoft::WRL::ComPtr<ID3D11Buffer> m_vertexBuffer;
   Microsoft::WRL::ComPtr<ID3D11Buffer> m_indexBuffer;
   Microsoft::WRL::ComPtr<ID3D11VertexShader> m_vertexShader;
   Microsoft::WRL::ComPtr<ID3D11PixelShader> m_pixelShader;
   Microsoft::WRL::ComPtr<ID3D11Buffer> m_constantBuffer;

   uint32 m_indexCount;
   ModelViewProjectionConstantBuffer m_constantBufferData;

   bool m_loadingComplete;
};*/