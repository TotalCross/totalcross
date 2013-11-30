/*#include "Direct3DBase.h"
#include "cppwrapper.h"
#include "MainView.h"

using namespace DX;
using namespace DirectX;
using namespace Microsoft::WRL;
using namespace Windows::UI::Core;
using namespace Windows::Foundation;
using namespace Windows::Graphics::Display;

// Constructor.
// Initialize the Direct3D resources required to run.
Direct3DBase::Direct3DBase(CoreWindow^ window) 
{
   m_loadingComplete = false;
   m_indexCount = 0;
   m_window = window;

   CreateDeviceResources();
   CreateWindowSizeDependentResources();
}

// Recreate all device resources and set them back to the current state.
void Direct3DBase::HandleDeviceLost()
{
	// Reset these member variables to ensure that UpdateForWindowSizeChange recreates all resources.
	m_windowBounds.Width = 0;
	m_windowBounds.Height = 0;
	m_swapChain = nullptr;

	CreateDeviceResources();
	UpdateForWindowSizeChange();
}

// These are the resources that depend on the device.
void Direct3DBase::CreateDeviceResources()
{
	// This flag adds support for surfaces with a different color channel ordering
	// than the API default. It is required for compatibility with Direct2D.
	UINT creationFlags = D3D11_CREATE_DEVICE_BGRA_SUPPORT;

#if defined(_DEBUG)
	// If the project is in a debug build, enable debugging via SDK Layers with this flag.
	creationFlags |= D3D11_CREATE_DEVICE_DEBUG;
#endif

	// This array defines the set of DirectX hardware feature levels this app will support.
	// Note the ordering should be preserved.
	// Don't forget to declare your application's minimum required feature level in its
	// description.  All applications are assumed to support 9.3 unless otherwise stated.
	D3D_FEATURE_LEVEL featureLevels[] = 
	{
		D3D_FEATURE_LEVEL_9_3
	};

	// Create the Direct3D 11 API device object and a corresponding context.
	ComPtr<ID3D11Device> device;
	ComPtr<ID3D11DeviceContext> context;
	ThrowIfFailed(
		D3D11CreateDevice(
			nullptr, // Specify nullptr to use the default adapter.
			D3D_DRIVER_TYPE_HARDWARE,
			nullptr,
			creationFlags, // Set set debug and Direct2D compatibility flags.
			featureLevels, // List of feature levels this app can support.
			ARRAYSIZE(featureLevels),
			D3D11_SDK_VERSION, // Always set this to D3D11_SDK_VERSION.
			&device, // Returns the Direct3D device created.
			&m_featureLevel, // Returns feature level of device created.
			&context // Returns the device immediate context.
			)
		);
   
	// Get the Direct3D 11.1 API device and context interfaces.
	ThrowIfFailed(
		device.As(&m_d3dDevice)
		);
   
	ThrowIfFailed(
		context.As(&m_d3dContext)
		);

   
   auto loadVSTask = ReadDataAsync("SimpleVertexShader.cso");
   auto loadPSTask = ReadDataAsync("SimplePixelShader.cso");

   auto createVSTask = loadVSTask.then([this](Platform::Array<byte>^ fileData) {
      ThrowIfFailed(
         m_d3dDevice->CreateVertexShader(
         fileData->Data,
         fileData->Length,
         nullptr,
         &m_vertexShader
         )
         );

      const D3D11_INPUT_ELEMENT_DESC vertexDesc[] =
      {
         { "POSITION", 0, DXGI_FORMAT_R32G32B32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
         { "COLOR", 0, DXGI_FORMAT_R32G32B32_FLOAT, 0, 12, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      };

      ThrowIfFailed(
         m_d3dDevice->CreateInputLayout(
         vertexDesc,
         ARRAYSIZE(vertexDesc),
         fileData->Data,
         fileData->Length,
         &m_inputLayout
         )
         );
   }); 

   auto createPSTask = loadPSTask.then([this](Platform::Array<byte>^ fileData) {
      ThrowIfFailed(
         m_d3dDevice->CreatePixelShader(
         fileData->Data,
         fileData->Length,
         nullptr,
         &m_pixelShader
         )
         );
         
      CD3D11_BUFFER_DESC constantBufferDesc(sizeof(ModelViewProjectionConstantBuffer), D3D11_BIND_CONSTANT_BUFFER);
      ThrowIfFailed(
         m_d3dDevice->CreateBuffer(
         &constantBufferDesc,
         nullptr,
         &m_constantBuffer
         )
         );
   });
      
   auto createCubeTask = (createPSTask && createVSTask).then([this]() {
      VertexPositionColor cubeVertices[] =
      {
         { XMFLOAT3(-0.5f, -0.5f, -0.5f), XMFLOAT3(0.0f, 0.0f, 0.0f) },
         { XMFLOAT3(-0.5f, -0.5f, 0.5f), XMFLOAT3(0.0f, 0.0f, 1.0f) },
         { XMFLOAT3(-0.5f, 0.5f, -0.5f), XMFLOAT3(0.0f, 1.0f, 0.0f) },
         { XMFLOAT3(-0.5f, 0.5f, 0.5f), XMFLOAT3(0.0f, 1.0f, 1.0f) },
         { XMFLOAT3(0.5f, -0.5f, -0.5f), XMFLOAT3(1.0f, 0.0f, 0.0f) },
         { XMFLOAT3(0.5f, -0.5f, 0.5f), XMFLOAT3(1.0f, 0.0f, 1.0f) },
         { XMFLOAT3(0.5f, 0.5f, -0.5f), XMFLOAT3(1.0f, 1.0f, 0.0f) },
         { XMFLOAT3(0.5f, 0.5f, 0.5f), XMFLOAT3(1.0f, 1.0f, 1.0f) },
      };
      
      D3D11_SUBRESOURCE_DATA vertexBufferData = { 0 };
      vertexBufferData.pSysMem = cubeVertices;
      vertexBufferData.SysMemPitch = 0;
      vertexBufferData.SysMemSlicePitch = 0;
      CD3D11_BUFFER_DESC vertexBufferDesc(sizeof(cubeVertices), D3D11_BIND_VERTEX_BUFFER);
      ThrowIfFailed(
         m_d3dDevice->CreateBuffer(
         &vertexBufferDesc,
         &vertexBufferData,
         &m_vertexBuffer
         )
         );

      unsigned short cubeIndices[] =
      {
         0, 2, 1, // -x
         1, 2, 3,

         4, 5, 6, // +x
         5, 7, 6,

         0, 1, 5, // -y
         0, 5, 4,

         2, 6, 7, // +y
         2, 7, 3,

         0, 4, 6, // -z
         0, 6, 2,

         1, 3, 7, // +z
         1, 7, 5,
      };
      
      m_indexCount = ARRAYSIZE(cubeIndices);
      
      D3D11_SUBRESOURCE_DATA indexBufferData = { 0 };
      indexBufferData.pSysMem = cubeIndices;
      indexBufferData.SysMemPitch = 0;
      indexBufferData.SysMemSlicePitch = 0;
      CD3D11_BUFFER_DESC indexBufferDesc(sizeof(cubeIndices), D3D11_BIND_INDEX_BUFFER);
      ThrowIfFailed(
         m_d3dDevice->CreateBuffer(
         &indexBufferDesc,
         &indexBufferData,
         &m_indexBuffer
         )
         );
   });
   
   createCubeTask.then([this]() {
      m_loadingComplete = true;
   }); 
}

// Allocate all memory resources that depend on the window size.
void Direct3DBase::CreateWindowSizeDependentResources()
{
   m_windowBounds = TotalCross::MainView::GetLastInstance()->getBounds();

	// Calculate the necessary swap chain and render target size in pixels.
   m_renderTargetSize.Width = ConvertDipsToPixels(m_windowBounds.Width);
   m_renderTargetSize.Height = ConvertDipsToPixels(m_windowBounds.Height);

	DXGI_SWAP_CHAIN_DESC1 swapChainDesc = {0};
	swapChainDesc.Width = static_cast<UINT>(m_renderTargetSize.Width); // Match the size of the window.
	swapChainDesc.Height = static_cast<UINT>(m_renderTargetSize.Height);
	swapChainDesc.Format = DXGI_FORMAT_B8G8R8A8_UNORM; // This is the most common swap chain format.
	swapChainDesc.Stereo = false;
	swapChainDesc.SampleDesc.Count = 1; // Don't use multi-sampling.
	swapChainDesc.SampleDesc.Quality = 0;
	swapChainDesc.BufferUsage = DXGI_USAGE_RENDER_TARGET_OUTPUT;
	swapChainDesc.BufferCount = 1; // On phone, only single buffering is supported.
	swapChainDesc.Scaling = DXGI_SCALING_STRETCH; // On phone, only stretch and aspect-ratio stretch scaling are allowed.
	swapChainDesc.SwapEffect = DXGI_SWAP_EFFECT_DISCARD; // On phone, no swap effects are supported.
	swapChainDesc.Flags = 0;

	ComPtr<IDXGIDevice1> dxgiDevice;
	ThrowIfFailed(
		m_d3dDevice.As(&dxgiDevice)
		);

	ComPtr<IDXGIAdapter> dxgiAdapter;
	ThrowIfFailed(
		dxgiDevice->GetAdapter(&dxgiAdapter)
		);

	ComPtr<IDXGIFactory2> dxgiFactory;
	ThrowIfFailed(
		dxgiAdapter->GetParent(
			__uuidof(IDXGIFactory2), 
			&dxgiFactory
			)
		);

	Windows::UI::Core::CoreWindow^ window = m_window.Get();
	ThrowIfFailed(
		dxgiFactory->CreateSwapChainForCoreWindow(
			m_d3dDevice.Get(),
			reinterpret_cast<IUnknown*>(window),
			&swapChainDesc,
			nullptr, // Allow on all displays.
			&m_swapChain
			)
		);
		
	// Ensure that DXGI does not queue more than one frame at a time. This both reduces latency and
	// ensures that the application will only render after each VSync, minimizing power consumption.
	ThrowIfFailed(
		dxgiDevice->SetMaximumFrameLatency(1)
		);

	// Create a render target view of the swap chain back buffer.
	ComPtr<ID3D11Texture2D> backBuffer;
	ThrowIfFailed(
		m_swapChain->GetBuffer(
			0,
			__uuidof(ID3D11Texture2D),
			&backBuffer
			)
		);

	ThrowIfFailed(
		m_d3dDevice->CreateRenderTargetView(
			backBuffer.Get(),
			nullptr,
			&m_renderTargetView
			)
		);

	// Create a depth stencil view.
	CD3D11_TEXTURE2D_DESC depthStencilDesc(
		DXGI_FORMAT_D24_UNORM_S8_UINT,
		static_cast<UINT>(m_renderTargetSize.Width),
		static_cast<UINT>(m_renderTargetSize.Height),
		1,
		1,
		D3D11_BIND_DEPTH_STENCIL
		);

	ComPtr<ID3D11Texture2D> depthStencil;
	ThrowIfFailed(
		m_d3dDevice->CreateTexture2D(
			&depthStencilDesc,
			nullptr,
			&depthStencil
			)
		);

	CD3D11_DEPTH_STENCIL_VIEW_DESC depthStencilViewDesc(D3D11_DSV_DIMENSION_TEXTURE2D);
	ThrowIfFailed(
		m_d3dDevice->CreateDepthStencilView(
			depthStencil.Get(),
			&depthStencilViewDesc,
			&m_depthStencilView
			)
		);

	// Set the rendering viewport to target the entire window.
	CD3D11_VIEWPORT viewport(
		0.0f,
		0.0f,
		m_renderTargetSize.Width,
		m_renderTargetSize.Height
		);

	m_d3dContext->RSSetViewports(1, &viewport);

   float aspectRatio = m_windowBounds.Width / m_windowBounds.Height;
   float fovAngleY = 70.0f * XM_PI / 180.0f;
   if (aspectRatio < 1.0f)
   {
      fovAngleY /= aspectRatio;
   }

   XMStoreFloat4x4(
      &m_constantBufferData.projection,
      XMMatrixTranspose(
      XMMatrixPerspectiveFovRH(
      fovAngleY,
      aspectRatio,
      0.01f,
      100.0f
      )
      )
      ); 
}

// This method is called in the event handler for the SizeChanged event.
void Direct3DBase::UpdateForWindowSizeChange()
{
	if (m_window->Bounds.Width  != m_windowBounds.Width ||
		m_window->Bounds.Height != m_windowBounds.Height)
	{
		ID3D11RenderTargetView* nullViews[] = {nullptr};
		m_d3dContext->OMSetRenderTargets(ARRAYSIZE(nullViews), nullViews, nullptr);
		m_renderTargetView = nullptr;
		m_depthStencilView = nullptr;
		m_d3dContext->Flush();
		CreateWindowSizeDependentResources();
	}
}

void Direct3DBase::ReleaseResourcesForSuspending()
{
	// Phone applications operate in a memory-constrained environment, so when entering
	// the background it is a good idea to free memory-intensive objects that will be
	// easy to restore upon reactivation. The swapchain and backbuffer are good candidates
	// here, as they consume a large amount of memory and can be reinitialized quickly.
	m_swapChain = nullptr;
	m_renderTargetView = nullptr;
	m_depthStencilView = nullptr;
}

void Direct3DBase::Render()
{
   const float midnightBlue[] = { 0.098f, 0.098f, 0.439f, 1.000f };
   m_d3dContext->ClearRenderTargetView(
      m_renderTargetView.Get(),
      midnightBlue
      );

   m_d3dContext->ClearDepthStencilView(
      m_depthStencilView.Get(),
      D3D11_CLEAR_DEPTH,
      1.0f,
      0
      );
   
   // Only draw the cube once it is loaded (loading is asynchronous).
   if (!m_loadingComplete)
   {
      return;
   }
  
   m_d3dContext->OMSetRenderTargets(
      1,
      m_renderTargetView.GetAddressOf(),
      m_depthStencilView.Get()
      );
  
   m_d3dContext->UpdateSubresource(
      m_constantBuffer.Get(),
      0,
      NULL,
      &m_constantBufferData,
      0,
      0
      );

   UINT stride = sizeof(VertexPositionColor);
   UINT offset = 0;
   m_d3dContext->IASetVertexBuffers(
      0,
      1,
      m_vertexBuffer.GetAddressOf(),
      &stride,
      &offset
      );

   m_d3dContext->IASetIndexBuffer(
      m_indexBuffer.Get(),
      DXGI_FORMAT_R16_UINT,
      0
      );

   m_d3dContext->IASetPrimitiveTopology(D3D11_PRIMITIVE_TOPOLOGY_TRIANGLELIST);

   m_d3dContext->IASetInputLayout(m_inputLayout.Get());

   m_d3dContext->VSSetShader(
      m_vertexShader.Get(),
      nullptr,
      0
      );

   m_d3dContext->VSSetConstantBuffers(
      0,
      1,
      m_constantBuffer.GetAddressOf()
      );

   m_d3dContext->PSSetShader(
      m_pixelShader.Get(),
      nullptr,
      0
      );

   m_d3dContext->DrawIndexed(
      m_indexCount,
      0,
      0
      );
}

// Method to deliver the final image to the display.
void Direct3DBase::Present()
{
	// The first argument instructs DXGI to block until VSync, putting the application
	// to sleep until the next VSync. This ensures we don't waste any cycles rendering
	// frames that will never be displayed to the screen.
	HRESULT hr = m_swapChain->Present(1, 0);

	// Discard the contents of the render target.
	// This is a valid operation only when the existing contents will be entirely
	// overwritten. If dirty or scroll rects are used, this call should be removed.
	m_d3dContext->DiscardView(m_renderTargetView.Get());

	// Discard the contents of the depth stencil.
	m_d3dContext->DiscardView(m_depthStencilView.Get());

	// If the device was removed either by a disconnect or a driver upgrade, we 
	// must recreate all device resources.
	if (hr == DXGI_ERROR_DEVICE_REMOVED)
	{
		HandleDeviceLost();
	}
	else
	{
		ThrowIfFailed(hr);
	}
}

// Method to convert a length in device-independent pixels (DIPs) to a length in physical pixels.
float Direct3DBase::ConvertDipsToPixels(float dips)
{
	static const float dipsPerInch = 96.0f;
	return floor(dips * DisplayProperties::LogicalDpi / dipsPerInch + 0.5f); // Round to nearest integer.
}

void Direct3DBase::ReleaseDX(void)
{
   ReleaseResourcesForSuspending();
}*/