#include "Direct3DBase.h"

#include <thread>

#define HAS_TCHAR
#include "tcvm.h"

using namespace DirectX;
using namespace Microsoft::WRL;
using namespace Windows::Foundation;
using namespace Windows::UI::Core;

static Direct3DBase ^lastInstance;

// Constructor.
Direct3DBase::Direct3DBase(PhoneDirect3DXamlAppComponent::CSwrapper ^_cs)
{
   cs = _cs;
   lastInstance = this;
}

Direct3DBase ^Direct3DBase::GetLastInstance()
{
	return lastInstance;
}

PhoneDirect3DXamlAppComponent::CSwrapper^ Direct3DBase::getCSwrapper()
{
   return cs;
}

// Initialize the Direct3D resources required to run.
void Direct3DBase::Initialize(_In_ ID3D11Device1* device)
{
	wchar_t mensagem_fim[2048];
	int saida;

	m_d3dDevice = device;
	saida = startVM("UIControls", &local_context);

	if (saida != 0) 
   {
		swprintf_s(mensagem_fim, 1000, L"Error code in starting VM: %d", saida);
		cs->privateAlertCS(ref new Platform::String(mensagem_fim));
	}
	CreateDeviceResources();
}

// These are the resources that depend on the device.
void Direct3DBase::CreateDeviceResources()
{
   auto loadVSTask1 = DX::ReadDataAsync("VertexShaderGlobalColor.cso");
   auto loadPSTask1 = DX::ReadDataAsync("PixelShaderGlobalColor.cso");

   auto loadVSTask2 = DX::ReadDataAsync("VertexShaderTexture.cso");
   auto loadPSTask2 = DX::ReadDataAsync("PixelShaderTexture.cso");

   auto loadVSTask3 = DX::ReadDataAsync("VertexShaderLocalColor.cso");
   auto loadPSTask3 = DX::ReadDataAsync("PixelShaderLocalColor.cso");

   // global color vertex
   auto createVSTask1 = loadVSTask1.then([this](Platform::Array<byte>^ fileData) 
   {
      DX::ThrowIfFailed(m_d3dDevice->CreateVertexShader(fileData->Data, fileData->Length, nullptr, &m_vertexShader));
      const D3D11_INPUT_ELEMENT_DESC vertexDesc[] =
      {
         { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      };
      DX::ThrowIfFailed(m_d3dDevice->CreateInputLayout(vertexDesc, ARRAYSIZE(vertexDesc), fileData->Data, fileData->Length, &m_inputLayout));
      loadCompleted |= 1;
   });

   // global color pixel
   auto createPSTask1 = loadPSTask1.then([this](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(m_d3dDevice->CreatePixelShader(fileData->Data, fileData->Length, nullptr, &m_pixelShader));
      CD3D11_BUFFER_DESC constantBufferDesc(sizeof(ProjectionConstantBuffer), D3D11_BIND_CONSTANT_BUFFER);
      DX::ThrowIfFailed(m_d3dDevice->CreateBuffer(&constantBufferDesc, nullptr, &m_constantBuffer));
      loadCompleted |= 2;
   });

   // texture vertex
   auto createVSTask2 = loadVSTask2.then([this](Platform::Array<byte>^ fileData) 
   {
      DX::ThrowIfFailed(m_d3dDevice->CreateVertexShader(fileData->Data, fileData->Length, nullptr, &m_vertexShaderT));
      const D3D11_INPUT_ELEMENT_DESC vertexDesc[] =
      {
         { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
         { "TEXCOORD", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 8, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      };
      DX::ThrowIfFailed(m_d3dDevice->CreateInputLayout(vertexDesc, ARRAYSIZE(vertexDesc), fileData->Data, fileData->Length, &m_inputLayoutT));
      loadCompleted |= 4;
   });

   // texture pixel
   auto createPSTask2 = loadPSTask2.then([this](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(m_d3dDevice->CreatePixelShader(fileData->Data, fileData->Length, nullptr, &m_pixelShaderT));
      loadCompleted |= 8;
   });

   // local color vertex
   auto createVSTask3 = loadVSTask3.then([this](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(m_d3dDevice->CreateVertexShader(fileData->Data, fileData->Length, nullptr, &m_vertexShaderLC));
      const D3D11_INPUT_ELEMENT_DESC vertexDesc[] =
      {
         { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
         { "COLOR", 0,    DXGI_FORMAT_R32G32B32A32_FLOAT, 0, 8, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      };
      DX::ThrowIfFailed(m_d3dDevice->CreateInputLayout(vertexDesc, ARRAYSIZE(vertexDesc), fileData->Data, fileData->Length, &m_inputLayoutLC));
      loadCompleted |= 16;
   });

   // local color pixel
   auto createPSTask3 = loadPSTask3.then([this](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(m_d3dDevice->CreatePixelShader(fileData->Data, fileData->Length, nullptr, &m_pixelShaderLC));
      loadCompleted |= 32;
   });

}

void Direct3DBase::UpdateDevice(_In_ ID3D11Device1* device, _In_ ID3D11DeviceContext1* context, _In_ ID3D11RenderTargetView* renderTargetView)
{
	m_d3dContext = context;
	m_renderTargetView = renderTargetView;

	if (m_d3dDevice.Get() != device)
	{
		m_d3dDevice->GetDeviceRemovedReason();
      eventsInitialized = false;
		m_d3dDevice = device;
		CreateDeviceResources();
		// Force call to CreateWindowSizeDependentResources.
		m_renderTargetSize.Width  = -1;
		m_renderTargetSize.Height = -1;
	}

	ComPtr<ID3D11Resource> renderTargetViewResource;
	m_renderTargetView->GetResource(&renderTargetViewResource);

	ComPtr<ID3D11Texture2D> backBuffer;
	DX::ThrowIfFailed(renderTargetViewResource.As(&backBuffer));

	// Cache the rendertarget dimensions in our helper class for convenient use.
   D3D11_TEXTURE2D_DESC backBufferDesc;
   backBuffer->GetDesc(&backBufferDesc);

   if (m_renderTargetSize.Width  != static_cast<float>(backBufferDesc.Width) || m_renderTargetSize.Height != static_cast<float>(backBufferDesc.Height))
   {
      m_renderTargetSize.Width  = static_cast<float>(backBufferDesc.Width);
      m_renderTargetSize.Height = static_cast<float>(backBufferDesc.Height);
      CreateWindowSizeDependentResources();
   }
}

// Allocate all memory resources that depend on the window size.
void Direct3DBase::CreateWindowSizeDependentResources()
{
	// Create a depth stencil view.
	CD3D11_TEXTURE2D_DESC depthStencilDesc(DXGI_FORMAT_D24_UNORM_S8_UINT,static_cast<UINT>(m_renderTargetSize.Width),static_cast<UINT>(m_renderTargetSize.Height),1,1,D3D11_BIND_DEPTH_STENCIL);
	ComPtr<ID3D11Texture2D> depthStencil;
	DX::ThrowIfFailed(m_d3dDevice->CreateTexture2D(&depthStencilDesc,nullptr,&depthStencil));
	CD3D11_DEPTH_STENCIL_VIEW_DESC depthStencilViewDesc(D3D11_DSV_DIMENSION_TEXTURE2D);
	DX::ThrowIfFailed(m_d3dDevice->CreateDepthStencilView(depthStencil.Get(),&depthStencilViewDesc,&m_depthStencilView));

   XMStoreFloat4x4(&m_constantBufferData.projection, XMMatrixOrthographicOffCenterLH(0, m_windowBounds.Width, m_windowBounds.Height, 0, -1.0f, 1.0f));
   setup();
}

void Direct3DBase::UpdateForWindowSizeChange(float width, float height)
{
	m_windowBounds.Width  = width;
	m_windowBounds.Height = height;
}

void Direct3DBase::setup()
{
   unsigned short cubeIndices[] =
   {
      0, 1, 2, 0, 2, 3
   };
   D3D11_SUBRESOURCE_DATA indexBufferData = { 0 };
   indexBufferData.pSysMem = cubeIndices;
   CD3D11_BUFFER_DESC indexBufferDesc(sizeof(cubeIndices), D3D11_BIND_INDEX_BUFFER);
   m_d3dDevice->CreateBuffer(&indexBufferDesc, &indexBufferData, &m_indexBuffer);

   // used in setColor for fillRect and drawLine and also textures
   {
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexColor);             // size is the VERTEX struct * 3
      bd.BindFlags = D3D11_BIND_CONSTANT_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      m_d3dDevice->CreateBuffer(&bd, NULL, &pBufferColor);       // create the buffer
   }
   // used in fillRect and drawLine
   {
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexPosition) * 4;     // size is the VERTEX
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      m_d3dDevice->CreateBuffer(&bd, NULL, &pBufferRect);       // create the buffer
   }
   // used in fillShadedRect
   {
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexPositionColor) * 4;             // size is the VERTEX
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      m_d3dDevice->CreateBuffer(&bd, NULL, &pBufferRectLC);       // create the buffer
   }

   /////////// TEXTURE
   // Once the texture view is created, create a sampler.  This defines how the color
   // for a particular texture coordinate is determined using the relevant texture data.
   D3D11_SAMPLER_DESC samplerDesc;
   ZeroMemory(&samplerDesc, sizeof(samplerDesc));
   samplerDesc.Filter = D3D11_FILTER_MIN_MAG_MIP_LINEAR;
   samplerDesc.AddressU = samplerDesc.AddressV = samplerDesc.AddressW = D3D11_TEXTURE_ADDRESS_WRAP;
   samplerDesc.MaxLOD = D3D11_FLOAT32_MAX;
   samplerDesc.ComparisonFunc = D3D11_COMPARISON_NEVER;
   DX::ThrowIfFailed(m_d3dDevice->CreateSamplerState(&samplerDesc, &texsampler));

   D3D11_DEPTH_STENCIL_DESC depthDisabledStencilDesc;
   depthDisabledStencilDesc.DepthEnable = false;
   depthDisabledStencilDesc.DepthWriteMask = D3D11_DEPTH_WRITE_MASK_ALL;
   depthDisabledStencilDesc.DepthFunc = D3D11_COMPARISON_LESS;
   depthDisabledStencilDesc.StencilEnable = true;
   depthDisabledStencilDesc.StencilReadMask = 0xFF;
   depthDisabledStencilDesc.StencilWriteMask = 0xFF;
   depthDisabledStencilDesc.FrontFace.StencilFailOp = D3D11_STENCIL_OP_KEEP;
   depthDisabledStencilDesc.FrontFace.StencilDepthFailOp = D3D11_STENCIL_OP_INCR;
   depthDisabledStencilDesc.FrontFace.StencilPassOp = D3D11_STENCIL_OP_KEEP;
   depthDisabledStencilDesc.FrontFace.StencilFunc = D3D11_COMPARISON_ALWAYS;
   depthDisabledStencilDesc.BackFace.StencilFailOp = D3D11_STENCIL_OP_KEEP;
   depthDisabledStencilDesc.BackFace.StencilDepthFailOp = D3D11_STENCIL_OP_DECR;
   depthDisabledStencilDesc.BackFace.StencilPassOp = D3D11_STENCIL_OP_KEEP;
   depthDisabledStencilDesc.BackFace.StencilFunc = D3D11_COMPARISON_ALWAYS;
   // Create the state using the device.
   m_d3dDevice->CreateDepthStencilState(&depthDisabledStencilDesc, &depthDisabledStencilState);

   // setup alpha blending
   D3D11_BLEND_DESC blendStateDescription = { 0 };
   blendStateDescription.RenderTarget[0].BlendEnable = TRUE;
   blendStateDescription.RenderTarget[0].SrcBlend = D3D11_BLEND_SRC_ALPHA;
   blendStateDescription.RenderTarget[0].DestBlend = D3D11_BLEND_INV_SRC_ALPHA;
   blendStateDescription.RenderTarget[0].BlendOp = D3D11_BLEND_OP_ADD;
   blendStateDescription.RenderTarget[0].SrcBlendAlpha = D3D11_BLEND_ONE;
   blendStateDescription.RenderTarget[0].DestBlendAlpha = D3D11_BLEND_ZERO;
   blendStateDescription.RenderTarget[0].BlendOpAlpha = D3D11_BLEND_OP_ADD;
   blendStateDescription.RenderTarget[0].RenderTargetWriteMask = 0x0f;
   m_d3dDevice->CreateBlendState(&blendStateDescription, &g_pBlendState);

   // setup clipping
   D3D11_RASTERIZER_DESC1 rasterizerState = { D3D11_FILL_SOLID };
   rasterizerState.CullMode = D3D11_CULL_FRONT;
   rasterizerState.FrontCounterClockwise = true;
   rasterizerState.DepthClipEnable = true;
   m_d3dDevice->CreateRasterizerState1(&rasterizerState, &pRasterStateDisableClipping);
   rasterizerState.ScissorEnable = true;
   m_d3dDevice->CreateRasterizerState1(&rasterizerState, &pRasterStateEnableClipping);

   // texture vertices
   D3D11_BUFFER_DESC bd = { 0 };
   bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
   bd.ByteWidth = sizeof(TextureVertex) * 8;             // size is the VERTEX struct * 3
   bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
   bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
   m_d3dDevice->CreateBuffer(&bd, NULL, &texVertexBuffer);       // create the buffer
}

#define f255(x) ((float)x/255.0f)
void Direct3DBase::fillShadedRect(int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz)
{
   //y += glShiftY;
   float x1 = (float)x, y1 = (float)y, x2 = x1 + w, y2 = y1 + h;
   XMFLOAT4 color1 = XMFLOAT4(f255(c2.r), f255(c2.g), f255(c2.b), f255(c2.a));
   XMFLOAT4 color2 = XMFLOAT4(f255(c1.r), f255(c1.g), f255(c1.b), f255(c1.a));
   VertexPositionColor cubeVertices[] = // position, color
   {
      { XMFLOAT2(x1, y1), horiz ? color1 : color1 },
      { XMFLOAT2(x2, y1), horiz ? color2 : color1 },
      { XMFLOAT2(x2, y2), horiz ? color2 : color2 },
      { XMFLOAT2(x1, y2), horiz ? color1 : color2 },
   };

   setProgram(PROGRAM_LC);
   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContext->Map(pBufferRectLC, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   m_d3dContext->Unmap(pBufferRectLC, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPositionColor);
   UINT offset = 0;
   m_d3dContext->IASetVertexBuffers(0, 1, &pBufferRectLC, &stride, &offset);
   m_d3dContext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
   m_d3dContext->IASetIndexBuffer(m_indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   m_d3dContext->DrawIndexed(6, 0, 0);
}

void Direct3DBase::setColor(int color)
{
   if (color == lastRGB) return;
   lastRGB = color;
   aa = ((color >> 24) & 0xFF) / 255.0f;
   rr = ((color >> 16) & 0xFF) / 255.0f;
   gg = ((color >> 8) & 0xFF) / 255.0f;
   bb = (color & 0xFF) / 255.0f;

   VertexColor vcolor;
   vcolor.color = XMFLOAT4(rr, gg, bb, aa);

   D3D11_MAPPED_SUBRESOURCE ms;

   m_d3dContext->Map(pBufferColor, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, &vcolor, sizeof(VertexColor));                // copy the data
   m_d3dContext->Unmap(pBufferColor, NULL);                                     // unmap the buffer

   m_d3dContext->VSSetConstantBuffers(1, 1, &pBufferColor);
}

void Direct3DBase::drawLine(int x1, int y1, int x2, int y2, int color)
{
   VertexPosition cubeVertices[] = // position, color
   {
      { XMFLOAT2((float)x1, (float)y1) },
      { XMFLOAT2((float)x2, (float)y2) },
   };

   setProgram(PROGRAM_GC);
   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContext->Map(pBufferRect, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   m_d3dContext->Unmap(pBufferRect, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   m_d3dContext->IASetVertexBuffers(0, 1, &pBufferRect, &stride, &offset);
   m_d3dContext->IASetIndexBuffer(m_indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   m_d3dContext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST);
   setColor(color);
   m_d3dContext->DrawIndexed(2, 0, 0);
}

void Direct3DBase::fillRect(int x1, int y1, int x2, int y2, int color)
{
   VertexPosition cubeVertices[] = // position, color
   {
      { XMFLOAT2((float)x1, (float)y1) },
      { XMFLOAT2((float)x2, (float)y1) },
      { XMFLOAT2((float)x2, (float)y2) },
      { XMFLOAT2((float)x1, (float)y2) },
   };

   setProgram(PROGRAM_GC);
   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContext->Map(pBufferRect, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   m_d3dContext->Unmap(pBufferRect, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   m_d3dContext->IASetVertexBuffers(0, 1, &pBufferRect, &stride, &offset);
   m_d3dContext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
   m_d3dContext->IASetIndexBuffer(m_indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   setColor(color);
   m_d3dContext->DrawIndexed(6, 0, 0);
}

void Direct3DBase::drawPixels(int *x, int *y, int count, int color)
{
   int i;
   int n = count * 2;
   VertexPosition *cubeVertices = new VertexPosition[n];// position, color
   XMFLOAT3 xcolor = XMFLOAT3(rr, gg, bb);
   for (i = 0; i < n;)
   {
      cubeVertices[i].pos = XMFLOAT2((float)*x, (float)*y);
      i++;
      cubeVertices[i].pos = XMFLOAT2((float)(*x + 1), (float)*y);
      i++;
      x++; y++;
   }
   setProgram(PROGRAM_GC);

   if (n > lastPixelsCount)
   {
      // cache the pixels index
      unsigned short *cubeIndexes = new unsigned short[n];
      for (i = n; --i >= 0;) cubeIndexes[i] = i;
      lastPixelsCount = n;

      D3D11_SUBRESOURCE_DATA indexBufferData = { cubeIndexes,0,0 };
      CD3D11_BUFFER_DESC indexBufferDesc(sizeof(cubeIndexes[0]) * n, D3D11_BIND_INDEX_BUFFER);
      DX::ThrowIfFailed(m_d3dDevice->CreateBuffer(&indexBufferDesc, &indexBufferData, &pixelsIndexBuffer));
      delete cubeIndexes;

      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexPosition)* n;             // size is the VERTEX struct * 3
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      m_d3dDevice->CreateBuffer(&bd, NULL, &pBufferPixels);       // create the buffer
   }

   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContext->Map(pBufferPixels, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(VertexPosition)* n);                // copy the data
   m_d3dContext->Unmap(pBufferPixels, NULL);                                     // unmap the buffer
   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   m_d3dContext->IASetVertexBuffers(0, 1, &pBufferPixels, &stride, &offset);
   m_d3dContext->IASetIndexBuffer(pixelsIndexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   m_d3dContext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST);
   setColor(color);
   m_d3dContext->DrawIndexed(n, 0, 0);

   delete cubeVertices;
}

bool Direct3DBase::isLoadCompleted() 
{
   return loadCompleted == TASKS_COMPLETED && eventsInitialized;
}

void Direct3DBase::updateScreen()
{
   for (updateScreenRequested = true; updateScreenRequested;) 
      Sleep(0);
}

void Direct3DBase::setProgram(whichProgram p)
{
   if (p == curProgram) return;
   lastRGB = 0xFAFFFFFF; // user may never set to this color
   curProgram = p;
   clipRect.right = -1;
   m_d3dContext->RSSetState(pRasterStateDisableClipping);
   clipSet = false;
   switch (p)
   {
      case PROGRAM_GC:
         m_d3dContext->VSSetShader(m_vertexShader.Get(), nullptr, 0);
         m_d3dContext->PSSetShader(m_pixelShader.Get(), nullptr, 0);
         m_d3dContext->IASetInputLayout(m_inputLayout.Get());
         break;
      case PROGRAM_LC:
         m_d3dContext->VSSetShader(m_vertexShaderLC.Get(), nullptr, 0);
         m_d3dContext->PSSetShader(m_pixelShaderLC.Get(), nullptr, 0);
         m_d3dContext->IASetInputLayout(m_inputLayoutLC.Get());
         break;
      case PROGRAM_TEX:
         m_d3dContext->PSSetSamplers(0, 1, texsampler.GetAddressOf());
         m_d3dContext->UpdateSubresource(m_constantBuffer.Get(), 0, nullptr, &m_constantBufferData, 0, 0);
         m_d3dContext->VSSetShader(m_vertexShaderT.Get(), nullptr, 0);
         m_d3dContext->PSSetShader(m_pixelShaderT.Get(), nullptr, 0);
         m_d3dContext->IASetInputLayout(m_inputLayoutT.Get());
         m_d3dContext->IASetIndexBuffer(m_indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
         m_d3dContext->IASetPrimitiveTopology(D3D11_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
         break;
   }
   m_d3dContext->VSSetConstantBuffers(0, 1, m_constantBuffer.GetAddressOf());
}

void Direct3DBase::PreRender()
{
   const float clearColor[4] = { 0.0f, 0.0f, 0.0f, 0.0f };
   // Set the rendering viewport to target the entire window.
   CD3D11_VIEWPORT viewport(0.0f, 0.0f, m_renderTargetSize.Width, m_renderTargetSize.Height);
   m_d3dContext->RSSetViewports(1, &viewport);

   m_d3dContext->ClearRenderTargetView(m_renderTargetView.Get(), clearColor);
   m_d3dContext->ClearDepthStencilView(m_depthStencilView.Get(), D3D11_CLEAR_DEPTH, 1.0f, 0);

   m_d3dContext->OMSetDepthStencilState(depthDisabledStencilState, 1);
   m_d3dContext->OMSetBlendState(g_pBlendState, 0, 0xffffffff);

   m_d3dContext->OMSetRenderTargets(1, m_renderTargetView.GetAddressOf(), m_depthStencilView.Get());
   m_d3dContext->UpdateSubresource(m_constantBuffer.Get(), 0, NULL, &m_constantBufferData, 0, 0);
   curProgram = PROGRAM_NONE;
   m_d3dContext->RSSetState(pRasterStateDisableClipping);
   clipSet = false;
}

void Direct3DBase::startVMIfNeeded()
{
   if (!VMStarted && isLoadCompleted())
   {
	   auto lambda = [this]() 
      {
		   PreRender();
		   startProgram(local_context);
	   };
	   std::thread(lambda).detach();
	   VMStarted = true;
	   Sleep(1);
   }
}

void Direct3DBase::loadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool updateList)
{
   int32 i;
   PixelConv* pf = (PixelConv*)pixels;
   PixelConv* pt = (PixelConv*)xmalloc(width*height * 4), *pt0 = pt;
   ID3D11Texture2D *texture;
   D3D11_TEXTURE2D_DESC textureDesc = { 0 };
   textureDesc.Width = width;
   textureDesc.Height = height;
   textureDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
   textureDesc.MipLevels = textureDesc.ArraySize = textureDesc.SampleDesc.Count = 1;
   textureDesc.BindFlags = D3D11_BIND_SHADER_RESOURCE;

   for (i = width*height; --i >= 0; pt++, pf++) { pt->a = pf->r; pt->b = pf->g; pt->g = pf->b; pt->r = pf->a; }
   D3D11_SUBRESOURCE_DATA textureSubresourceData = { 0 };
   textureSubresourceData.pSysMem = pt0;
   textureSubresourceData.SysMemPitch = textureDesc.Width * 4; // Specify the size of a row in bytes
   if (FAILED(m_d3dDevice->CreateTexture2D(&textureDesc, &textureSubresourceData, &texture)))
      throwException(currentContext, OutOfMemoryError, "Out of texture memory for image with %dx%d", width, height);
   else
   {
      ID3D11ShaderResourceView* textureView;
      D3D11_SHADER_RESOURCE_VIEW_DESC textureViewDesc;
      ZeroMemory(&textureViewDesc, sizeof(textureViewDesc));
      textureViewDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
      textureViewDesc.ViewDimension = D3D11_SRV_DIMENSION_TEXTURE2D;
      textureViewDesc.Texture2D.MipLevels = 1;
      m_d3dDevice->CreateShaderResourceView(&texture[0], &textureViewDesc, &textureView);
      xmoveptr(&textureId[0], &texture);
      xmoveptr(&textureId[1], &textureView);
   }
   xfree(pt0);
}
void Direct3DBase::deleteTexture(TCObject img, int32* textureId, bool updateList)
{
}
void Direct3DBase::drawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 imgW, int32 imgH, PixelConv *color, int32* clip)
{
   bool doClip = clip != null;
   ID3D11Texture2D *texture;
   ID3D11ShaderResourceView *textureView;

   xmoveptr(&texture, &textureId[0]);
   xmoveptr(&textureView, &textureId[1]);
   setProgram(PROGRAM_TEX);
   setColor(!color ? 0 : 0xFF000000 | (color->r << 16) | (color->g << 8) | color->b);

   if (!doClip && clipSet)
      m_d3dContext->RSSetState(pRasterStateDisableClipping);
   else
   if (doClip)
   {
      if (!clipSet)
         m_d3dContext->RSSetState(pRasterStateEnableClipping);
      if (clip[0] != clipRect.left || clip[1] != clipRect.top || clip[2] != clipRect.right || clip[3] != clipRect.bottom)
      {
         clipRect.left = clip[0];
         clipRect.top = clip[1];
         clipRect.right = clip[2];
         clipRect.bottom = clip[3];
         m_d3dContext->RSSetScissorRects(1, &clipRect);
      }
   }
   clipSet = doClip;

   //dstY += glShiftY;
   int32 dstY2 = dstY + h;
   int32 dstX2 = dstX + w;

   float left = (float)x / (float)imgW, top = (float)y / (float)imgH, right = (float)(x + w) / (float)imgW, bottom = (float)(y + h) / (float)imgH; // 0,0,1,1

   // VERTEX BUFFER
   TextureVertex cubeVertices[] =
   {  // destination coordinates    source coordinates
      { XMFLOAT2((float)dstX,  (float)dstY),  XMFLOAT2(left, top) },
      { XMFLOAT2((float)dstX2, (float)dstY),  XMFLOAT2(right, top) },
      { XMFLOAT2((float)dstX2, (float)dstY2), XMFLOAT2(right, bottom) },
      { XMFLOAT2((float)dstX,  (float)dstY2), XMFLOAT2(left, bottom) },
   };
   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContext->Map(texVertexBuffer, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   m_d3dContext->Unmap(texVertexBuffer, NULL);                                     // unmap the buffer

   // Set the vertex and index buffers, and specify the way they define geometry.
   UINT stride = sizeof(TextureVertex);
   UINT offset = 0;
   m_d3dContext->IASetVertexBuffers(0, 1, &texVertexBuffer, &stride, &offset);

   // Set the vertex and pixel shader stage state.
   m_d3dContext->PSSetShaderResources(0, 1, &textureView);
   // Draw the cube.
   m_d3dContext->DrawIndexed(6, 0, 0);
}
