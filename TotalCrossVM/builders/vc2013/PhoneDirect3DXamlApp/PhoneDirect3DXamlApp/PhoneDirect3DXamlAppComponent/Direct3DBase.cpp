#include "pch.h"
#include "Direct3DBase.h"

#include <thread>

#define HAS_TCHAR
#include "tcvm.h"

using namespace DirectX;
using namespace Microsoft::WRL;
using namespace Windows::Foundation;
using namespace Windows::UI::Core;

static Direct3DBase ^lastInstance = nullptr;


// Constructor.
Direct3DBase::Direct3DBase(PhoneDirect3DXamlAppComponent::Idummy ^_odummy)
{
   odummy = _odummy;
   lastInstance = this;
   TheDrawCommand = DRAW_COMMAND_INVALID;
}

Direct3DBase ^Direct3DBase::GetLastInstance()
{
	return lastInstance;
}

PhoneDirect3DXamlAppComponent::Idummy^ Direct3DBase::getDummy()
{
   return odummy;
}

// Initialize the Direct3D resources required to run.
void Direct3DBase::Initialize(_In_ ID3D11Device1* device)
{
	wchar_t mensagem_fim[2048];
	int saida;

	m_d3dDevice = device;
	saida = startVM("AllTests", &local_context);

	if (saida != 0) 
   {
		swprintf_s(mensagem_fim, 1000, L"Error code in starting VM: %d", saida);
		odummy->privateAlertCS(ref new Platform::String(mensagem_fim));
	}
	CreateDeviceResources();
}

// These are the resources that depend on the device.
void Direct3DBase::CreateDeviceResources()
{
   int task_n = 0;
   auto loadVSTask = DX::ReadDataAsync("VertexShaderGlobalColor.cso");
   auto loadPSTask = DX::ReadDataAsync("PixelShaderGlobalColor.cso");

   auto loadVSTask2 = DX::ReadDataAsync("VertexShaderTexture.cso");
   auto loadPSTask2 = DX::ReadDataAsync("PixelShaderTexture.cso");

   // global color vertex
   auto createVSTask = loadVSTask.then([this, task_n](Platform::Array<byte>^ fileData) 
   {
      DX::ThrowIfFailed(m_d3dDevice->CreateVertexShader(fileData->Data, fileData->Length, nullptr, &m_vertexShader));
      const D3D11_INPUT_ELEMENT_DESC vertexDesc[] =
      {
         { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      };
      DX::ThrowIfFailed(m_d3dDevice->CreateInputLayout(vertexDesc, ARRAYSIZE(vertexDesc), fileData->Data, fileData->Length, &m_inputLayout));
      loadCompleted[task_n] = true;
   });

   // global color pixel
   task_n++;
   auto createPSTask = loadPSTask.then([this, task_n](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(m_d3dDevice->CreatePixelShader(fileData->Data, fileData->Length, nullptr, &m_pixelShader));
      CD3D11_BUFFER_DESC constantBufferDesc(sizeof(ProjectionConstantBuffer), D3D11_BIND_CONSTANT_BUFFER);
      DX::ThrowIfFailed(m_d3dDevice->CreateBuffer(&constantBufferDesc, nullptr, &m_constantBuffer));
	  loadCompleted[task_n] = true;
   });

   // texture vertex
   task_n++;
   auto createVSTask2 = loadVSTask2.then([this, task_n](Platform::Array<byte>^ fileData) 
   {
      DX::ThrowIfFailed(m_d3dDevice->CreateVertexShader(fileData->Data, fileData->Length, nullptr, &m_vertexShaderT));
      const D3D11_INPUT_ELEMENT_DESC vertexDesc[] =
      {
         { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
         { "TEXCOORD", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 8, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      };
      DX::ThrowIfFailed(m_d3dDevice->CreateInputLayout(vertexDesc, ARRAYSIZE(vertexDesc), fileData->Data, fileData->Length, &m_inputLayoutT));
	  loadCompleted[task_n] = true;
   });

   // texture pixel
   task_n++;
   auto createPSTask2 = loadPSTask2.then([this, task_n](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(m_d3dDevice->CreatePixelShader(fileData->Data, fileData->Length, nullptr, &m_pixelShaderT));
	  loadCompleted[task_n] = true;
   });
}

void Direct3DBase::UpdateDevice(_In_ ID3D11Device1* device, _In_ ID3D11DeviceContext1* context, _In_ ID3D11RenderTargetView* renderTargetView)
{
	m_d3dContext = context;
	m_renderTargetView = renderTargetView;

	if (m_d3dContextDEF == nullptr)
		m_d3dDevice->CreateDeferredContext1(0, &m_d3dContextDEF);

	if (m_d3dDevice.Get() != device)
	{
		m_d3dDevice->GetDeviceRemovedReason();
		m_d3dDevice = device;
		m_d3dDevice->CreateDeferredContext1(0, &m_d3dContextDEF);
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

	// Set the rendering viewport to target the entire window.
	CD3D11_VIEWPORT viewport(0.0f,0.0f,m_renderTargetSize.Width,m_renderTargetSize.Height);
	m_d3dContextDEF->RSSetViewports(1, &viewport);
	def_status = 1;
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
   {
      // set indices
      unsigned short cubeIndices[] =
      {
         0, 1, 2, 0, 2, 3
      };
      D3D11_SUBRESOURCE_DATA indexBufferData = { 0 };
      indexBufferData.pSysMem = cubeIndices;
      CD3D11_BUFFER_DESC indexBufferDesc(sizeof(cubeIndices), D3D11_BIND_INDEX_BUFFER);
      m_d3dDevice->CreateBuffer(&indexBufferDesc, &indexBufferData, &m_indexBuffer);

      // used in setColor
      {
         VertexColor cubeColor[1];
         D3D11_SUBRESOURCE_DATA vertexBufferData = { 0 };
         vertexBufferData.pSysMem = cubeColor;
         D3D11_BUFFER_DESC bd = { 0 };
         bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
         bd.ByteWidth = sizeof(cubeColor);             // size is the VERTEX struct * 3
         bd.BindFlags = D3D11_BIND_CONSTANT_BUFFER;       // use as a vertex buffer
         bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
         m_d3dDevice->CreateBuffer(&bd, NULL, &pBufferColor);       // create the buffer
      }
      // used in fillRect and drawLine
      {
         VertexPosition cubeVertices[8];
         D3D11_SUBRESOURCE_DATA vertexBufferData = { 0 };
         vertexBufferData.pSysMem = cubeVertices;
         D3D11_BUFFER_DESC bd = { 0 };
         bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
         bd.ByteWidth = sizeof(cubeVertices);             // size is the VERTEX
         bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
         bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
         m_d3dDevice->CreateBuffer(&bd, NULL, &pBufferRect);       // create the buffer
      }
   }

   /////////// TEXTURE
   {
   // VERTEX BUFFER
   TextureVertex cubeVertices[] =
   {  // coordenadas do quadrado    coordenadas do triangulo
      { XMFLOAT2(50.0f, 50.0f), XMFLOAT2(0.0f, 0.0f) },
      { XMFLOAT2(250.0f, 50.0f), XMFLOAT2(1.0f, 0.0f) },
      { XMFLOAT2(250.0f, 250.0f), XMFLOAT2(1.0f, 1.0f) },
      { XMFLOAT2(50.0f, 250.0f), XMFLOAT2(0.0f, 1.0f) },
   };
   D3D11_BUFFER_DESC vertexBufferDesc = { 0 };
   vertexBufferDesc.ByteWidth = sizeof(TextureVertex)* ARRAYSIZE(cubeVertices);
   vertexBufferDesc.BindFlags = D3D11_BIND_VERTEX_BUFFER;
   D3D11_SUBRESOURCE_DATA vertexBufferData = { 0 };
   vertexBufferData.pSysMem = cubeVertices;
   DX::ThrowIfFailed(m_d3dDevice->CreateBuffer(&vertexBufferDesc, &vertexBufferData, &vertexBuffer));

   // INDEX BUFFER
   unsigned short cubeIndices[] = { 0, 1, 2, 0, 2, 3 };
   D3D11_BUFFER_DESC indexBufferDesc = { 0 };
   indexBufferDesc.ByteWidth = sizeof(unsigned short)* ARRAYSIZE(cubeIndices);
   indexBufferDesc.BindFlags = D3D11_BIND_INDEX_BUFFER;
   D3D11_SUBRESOURCE_DATA indexBufferData = { 0 };
   indexBufferData.pSysMem = cubeIndices;
   DX::ThrowIfFailed(m_d3dDevice->CreateBuffer(&indexBufferDesc, &indexBufferData, &indexBuffer));

   // PROJECTION BUFFER
   D3D11_BUFFER_DESC constantBufferDesc = { 0 };
   constantBufferDesc.ByteWidth = sizeof(m_constantBufferData);
   constantBufferDesc.BindFlags = D3D11_BIND_CONSTANT_BUFFER;
   DX::ThrowIfFailed(m_d3dDevice->CreateBuffer(&constantBufferDesc, nullptr, &m_constantBuffer));

   // PIXELS BUFFER
   D3D11_TEXTURE2D_DESC textureDesc = { 0 };
   textureDesc.Width = 256;
   textureDesc.Height = 256;
   textureDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
   textureDesc.MipLevels = textureDesc.ArraySize = textureDesc.SampleDesc.Count = 1;
   textureDesc.BindFlags = D3D11_BIND_SHADER_RESOURCE;
   int *pixels = new int[textureDesc.Width * textureDesc.Height]; // w * h
   for (int i = textureDesc.Width * textureDesc.Height; --i >= 0;)
      pixels[i] = 0x00FF00 | (i & 0xFF) << 24;   // ABGR
   for (int i = 0; i < (int)textureDesc.Width; i++)
      pixels[i + i * textureDesc.Height] = 0xFFFFFFFF; // linha branca diagonal
   D3D11_SUBRESOURCE_DATA textureSubresourceData = { 0 };
   textureSubresourceData.pSysMem = pixels;
   textureSubresourceData.SysMemPitch = textureDesc.Width * 4; // Specify the size of a row in bytes
   DX::ThrowIfFailed(m_d3dDevice->CreateTexture2D(&textureDesc, &textureSubresourceData, &texture));

   // SHADER VIEW
   // Once the texture is created, we must create a shader resource view of it so that shaders may use it.  
   // In general, the view description will match the texture description.
   D3D11_SHADER_RESOURCE_VIEW_DESC textureViewDesc;
   ZeroMemory(&textureViewDesc, sizeof(textureViewDesc));
   textureViewDesc.Format = textureDesc.Format;
   textureViewDesc.ViewDimension = D3D11_SRV_DIMENSION_TEXTURE2D;
   textureViewDesc.Texture2D.MipLevels = textureDesc.MipLevels;
   DX::ThrowIfFailed(m_d3dDevice->CreateShaderResourceView(texture.Get(), &textureViewDesc, &textureView));

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
   blendStateDescription.RenderTarget[0].SrcBlend = D3D11_BLEND_ONE;
   blendStateDescription.RenderTarget[0].DestBlend = D3D11_BLEND_INV_SRC_ALPHA;
   blendStateDescription.RenderTarget[0].BlendOp = D3D11_BLEND_OP_ADD;
   blendStateDescription.RenderTarget[0].SrcBlendAlpha = D3D11_BLEND_ONE;
   blendStateDescription.RenderTarget[0].DestBlendAlpha = D3D11_BLEND_ZERO;
   blendStateDescription.RenderTarget[0].BlendOpAlpha = D3D11_BLEND_OP_ADD;
   blendStateDescription.RenderTarget[0].RenderTargetWriteMask = 0x0f;
   m_d3dDevice->CreateBlendState(&blendStateDescription, &g_pBlendState);
}
}

void Direct3DBase::setColor(int color)
{
   lastRGB = color;
   aa = ((color >> 24) & 0xFF) / 255.0f;
   rr = ((color >> 16) & 0xFF) / 255.0f;
   gg = ((color >> 8) & 0xFF) / 255.0f;
   bb = (color & 0xFF) / 255.0f;

   VertexColor vcolor;
   vcolor.color = XMFLOAT4(rr, gg, bb, aa); // last is alpha

   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContextDEF->Map(pBufferColor, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, &vcolor, sizeof(VertexColor));                // copy the data
   m_d3dContextDEF->Unmap(pBufferColor, NULL);                                     // unmap the buffer

   m_d3dContextDEF->VSSetConstantBuffers(1, 1, &pBufferColor);
}

void Direct3DBase::drawLine(int x1, int y1, int x2, int y2, int color)
{
   VertexPosition cubeVertices[] = // position, color
   {
      { XMFLOAT2((float)x1, (float)y1) },
      { XMFLOAT2((float)x2, (float)y2) },
   };

   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContextDEF->Map(pBufferRect, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   m_d3dContextDEF->Unmap(pBufferRect, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   m_d3dContextDEF->IASetVertexBuffers(0, 1, &pBufferRect, &stride, &offset);
   m_d3dContextDEF->IASetIndexBuffer(m_indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   m_d3dContextDEF->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST);
   m_d3dContextDEF->IASetIndexBuffer(m_indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   if (color != lastRGB) setColor(color);
   m_d3dContextDEF->DrawIndexed(2, 0, 0);
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

   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContextDEF->Map(pBufferRect, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   m_d3dContextDEF->Unmap(pBufferRect, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   m_d3dContextDEF->IASetVertexBuffers(0, 1, &pBufferRect, &stride, &offset);
   m_d3dContextDEF->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
   m_d3dContextDEF->IASetIndexBuffer(m_indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   if (color != lastRGB) setColor(color);
   m_d3dContextDEF->DrawIndexed(6, 0, 0);
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

   if (n > lastPixelsCount)
   {
      // cache the pixels index
      unsigned short *cubeIndexes = new unsigned short[n];
      for (i = n; --i >= 0;) cubeIndexes[i] = i;
      lastPixelsCount = n;

      D3D11_SUBRESOURCE_DATA indexBufferData = { 0 };
      indexBufferData.pSysMem = cubeIndexes;
      CD3D11_BUFFER_DESC indexBufferDesc(sizeof(cubeIndexes[0]) * n, D3D11_BIND_INDEX_BUFFER);
      DX::ThrowIfFailed(m_d3dDevice->CreateBuffer(&indexBufferDesc, &indexBufferData, &pixelsIndexBuffer));
      delete cubeIndexes;

      VertexPosition *cubeVertices = new VertexPosition[n];
      D3D11_SUBRESOURCE_DATA vertexBufferData = { 0 };
      vertexBufferData.pSysMem = cubeVertices;
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexPosition)* n;             // size is the VERTEX struct * 3
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      m_d3dDevice->CreateBuffer(&bd, NULL, &pBufferPixels);       // create the buffer
      delete cubeVertices;
   }

   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContextDEF->Map(pBufferPixels, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(VertexPosition)* n);                // copy the data
   m_d3dContextDEF->Unmap(pBufferPixels, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   m_d3dContextDEF->IASetVertexBuffers(0, 1, &pBufferPixels, &stride, &offset);
   m_d3dContextDEF->IASetIndexBuffer(pixelsIndexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   m_d3dContextDEF->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST);
   if (color != lastRGB) setColor(color);
   m_d3dContextDEF->DrawIndexed(n, 0, 0);

   delete cubeVertices;
}

bool Direct3DBase::isLoadCompleted() 
{
	for (int i = 0; i < N_LOAD_TASKS; i++)
	if (!loadCompleted[i])
		return false;
	return true;
}

ID3D11CommandList *cl = nullptr;

void Direct3DBase::Present()
{
	TheDrawCommand = DRAW_COMMAND_PRESENT;
	while (TheDrawCommand == DRAW_COMMAND_PRESENT) 
		Sleep(OCCUPIED_WAIT_TIME);
}

void Direct3DBase::PreRender()
{
	static int mustClear = true;

	if (mustClear) 
   {
		const float clearColor[4] = { 0.0f, 0.0f, 1.0f, 1.0f };
		m_d3dContextDEF->ClearRenderTargetView(m_renderTargetView.Get(), clearColor);
		m_d3dContextDEF->ClearDepthStencilView(m_depthStencilView.Get(), D3D11_CLEAR_DEPTH, 1.0f, 0);
	}

	m_d3dContextDEF->OMSetDepthStencilState(depthDisabledStencilState, 1);
	m_d3dContextDEF->OMSetBlendState(g_pBlendState, 0, 0xffffffff);

	m_d3dContextDEF->OMSetRenderTargets(1, m_renderTargetView.GetAddressOf(), m_depthStencilView.Get());
	m_d3dContextDEF->UpdateSubresource(m_constantBuffer.Get(), 0, NULL, &m_constantBufferData, 0, 0);
	m_d3dContextDEF->IASetInputLayout(m_inputLayout.Get());
	m_d3dContextDEF->VSSetShader(m_vertexShader.Get(), nullptr, 0);
	m_d3dContextDEF->VSSetConstantBuffers(0, 1, m_constantBuffer.GetAddressOf());
	m_d3dContextDEF->PSSetShader(m_pixelShader.Get(), nullptr, 0);
}

bool Direct3DBase::Render()
{
   if (!isLoadCompleted()) 
   {
	   def_status = 0;
	   return false;
   }
   int ini = GetTickCount64() & 0x3FFFFFFF;

   if (!VMStarted) 
   {
	   auto lambda = [this]() 
      {
		   DrawCommandLock.lock();
		   PreRender();
		   startProgram(local_context);
	   };
	   std::thread(lambda).detach();
	   VMStarted = true;
	   Sleep(OCCUPIED_WAIT_TIME);
   }

   int fim = GetTickCount64() & 0x3FFFFFFF;
   char buf[50];
   sprintf_s(buf, "elapsed: %d ms\n", fim - ini);
   OutputDebugStringA(buf);
   return true;
}

bool Direct3DBase::RenderTest()
{
	if (!isLoadCompleted()) return false;
	int ini = GetTickCount64() & 0x3FFFFFFF;

	PreRender();

	int w = (int)m_windowBounds.Width, h = (int)m_windowBounds.Height;
	for (int i = 0; i < h / 2; i++)
		drawLine(0, i, w, i, 0xFF00FF00);
	int *xx = new int[w], *yy = new int[w];

	static int madness = 0;
	for (int i = h / 2; i <= h; i++)
	{
		for (int j = 0; j <= w; j++)
		{
			xx[j] = j;
			yy[j] = i;
		}
		madness++;
		drawPixels(xx, yy, w, ((i * 255 / h) << 24) | 0xFF0000 + madness);
	}

	fillRect(w / 2 - w / 8, h / 2 - h / 8, w / 2 + w / 8, h / 2 + h / 8, 0xFF0000FF);

	////////////// TEXTURA

	//m_d3dContext->OMSetDepthStencilState(depthDisabledStencilState, 1);
	//m_d3dContext->OMSetBlendState(g_pBlendState, 0, 0xffffffff);

	m_d3dContextDEF->PSSetSamplers(0, 1, texsampler.GetAddressOf());
	m_d3dContextDEF->UpdateSubresource(m_constantBuffer.Get(), 0, nullptr, &m_constantBufferData, 0, 0);
	m_d3dContextDEF->OMSetRenderTargets(1, m_renderTargetView.GetAddressOf(), m_depthStencilView.Get());
	m_d3dContextDEF->IASetInputLayout(m_inputLayoutT.Get());

	// Set the vertex and index buffers, and specify the way they define geometry.
	UINT stride = sizeof(TextureVertex);
	UINT offset = 0;
	m_d3dContextDEF->IASetVertexBuffers(0, 1, vertexBuffer.GetAddressOf(), &stride, &offset);
	m_d3dContextDEF->IASetIndexBuffer(indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
	m_d3dContextDEF->IASetPrimitiveTopology(D3D11_PRIMITIVE_TOPOLOGY_TRIANGLELIST);

	// Set the vertex and pixel shader stage state.
	m_d3dContextDEF->VSSetShader(m_vertexShaderT.Get(), nullptr, 0);
	m_d3dContextDEF->VSSetConstantBuffers(0, 1, m_constantBuffer.GetAddressOf());
	m_d3dContextDEF->PSSetShader(m_pixelShaderT.Get(), nullptr, 0);
	m_d3dContextDEF->PSSetShaderResources(0, 1, textureView.GetAddressOf());
	// Draw the cube.
	m_d3dContextDEF->DrawIndexed(6, 0, 0);

	int fim = GetTickCount64() & 0x3FFFFFFF;
	char buf[50];
	sprintf_s(buf, "elapsed: %d ms\n", fim - ini);
	OutputDebugStringA(buf);
	return true;
}

// DrawCommand methods
int Direct3DBase::WaitDrawCommand() 
{
	return (int)TheDrawCommand;
}

void Direct3DBase::DoDrawCommand(bool should_redo) 
{
	static ID3D11CommandList *cl;
	ID3D11DeviceContext1 *ic;

	if (should_redo) 
   {
		m_d3dContextDEF->FinishCommandList(true, &cl);
		m_d3dDevice->GetImmediateContext1(&ic);

		ic->ExecuteCommandList(cl, true);
		TheDrawCommand = DRAW_COMMAND_INVALID;
	}
	if (!should_redo) 
   {
		m_d3dDevice->GetImmediateContext1(&ic);
		ic->ExecuteCommandList(cl, true);
	}
}

void Direct3DBase::drawCommand_drawLine(int x1, int y1, int x2, int y2, int color) 
{
	drawLine(x1, y1, x2, y2, color);
}

void Direct3DBase::drawCommand_drawPixels(int *x, int *y, int count, int color) 
{
	drawPixels(x, y, count, color);
}

void Direct3DBase::drawCommand_fillRect(int x1, int y1, int x2, int y2, int color) 
{
	fillRect(x1, y1, x2, y2, color);
}

void Direct3DBase::drawCommand_setColor(int color) 
{
	while (TheDrawCommand != DRAW_COMMAND_INVALID)
		Sleep(OCCUPIED_WAIT_TIME);
	DrawCommandLock.lock();
	DrawCommand_color = color;
	TheDrawCommand = DRAW_COMMAND_SETCOLOR;
}
void Direct3DBase::drawCommandLock() 
{
	DrawCommandLock.lock();
}

void Direct3DBase::drawCommandUnlock() 
{
	DrawCommandLock.unlock();
}
