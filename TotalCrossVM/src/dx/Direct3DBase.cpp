#include "Direct3DBase.h"
#include <thread>
#include <mutex>
#include "PhoneDirect3DXamlAppComponent.h"
#define HAS_TCHAR
#include "tcvm.h"

using namespace DirectX;
using namespace Microsoft::WRL;
using namespace Windows::Foundation;
using namespace Windows::UI::Core;

static Direct3DBase ^instance;

std::mutex listMutex;

struct D3DCommands
{
   D3DCommand head;
   D3DCommand tail;
   Heap heap;
} cmdFill, cmdDraw;

// lists
void listInit(D3DCommands* c)
{
   c->head = c->tail = NULL;
   c->heap = heapCreateB(false);
}
void listAdd(D3DCommands* s, D3DCommand p)
{
   p->next = NULL;
   if (NULL == s->head && NULL == s->tail)
      s->head = s->tail = p;
   else
   {
      s->tail->next = p;
      s->tail = p;
   }
}

bool listIsEmpty(D3DCommands* s)
{
   bool b = NULL == s->head && NULL == s->tail;
   return b;
}

void listSetEmpty(D3DCommands* s)
{
   //debug("heap.alloc: %d (%d)", s->heap->numAlloc, s->heap->totalAlloc);
   heapDestroyB(s->heap,false);
   s->heap = heapCreateB(false);
   s->head = s->tail = NULL;
}

int allocated;

D3DCommand Direct3DBase::newCommand()
{
   return newXH(D3DCommand,cmdFill.heap);
}

void Direct3DBase::fillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz)
{
   std::lock_guard<std::mutex> lock(listMutex);
   D3DCommand cmd = newCommand();
   cmd->cmd = D3DCMD_FILLSHADEDRECT;
   cmd->a = x;
   cmd->b = y;
   cmd->c = w;
   cmd->d = h;
   cmd->c1 = c1;
   cmd->c2 = c2;
   cmd->flags.horiz = horiz;
   cmd->flags.hasClip = true;
   cmd->clip[0] = Graphics_clipX1(g);
   cmd->clip[1] = Graphics_clipY1(g);
   cmd->clip[2] = Graphics_clipX2(g);
   cmd->clip[3] = Graphics_clipY2(g);
   listAdd(&cmdFill, cmd);
}
void Direct3DBase::drawLine(int x1, int y1, int x2, int y2, int color)
{
   std::lock_guard<std::mutex> lock(listMutex);
   D3DCommand cmd = newCommand();
   cmd->cmd = D3DCMD_DRAWLINE;
   cmd->a = x1;
   cmd->b = y1;
   cmd->c = x2;
   cmd->d = y2;
   cmd->c1.pixel = color;
   listAdd(&cmdFill, cmd);
}
void Direct3DBase::fillRect(int x1, int y1, int x2, int y2, int color)
{
   std::lock_guard<std::mutex> lock(listMutex);
   D3DCommand cmd = newCommand();
   cmd->cmd = D3DCMD_FILLRECT;
   cmd->a = x1;
   cmd->b = y1;
   cmd->c = x2;
   cmd->d = y2;
   cmd->c1.pixel = color;
   listAdd(&cmdFill, cmd);
}
void Direct3DBase::drawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 imgW, int32 imgH, PixelConv *color, int32* clip)
{
   std::lock_guard<std::mutex> lock(listMutex);
   D3DCommand cmd = newCommand();
   cmd->cmd = D3DCMD_DRAWTEXTURE;
   cmd->textureId[0] = textureId[0];
   cmd->textureId[1] = textureId[1];
   cmd->a = x;
   cmd->b = y;
   cmd->c = w;
   cmd->d = h;
   cmd->e = dstX;
   cmd->f = dstY;
   cmd->g = imgW;
   cmd->h = imgH;
   cmd->flags.hasColor = color != null;
   if (cmd->flags.hasColor) cmd->c1.pixel = color->pixel;
   cmd->flags.hasClip = clip != null;
   if (cmd->flags.hasClip) { cmd->clip[0] = clip[0]; cmd->clip[1] = clip[1]; cmd->clip[2] = clip[2]; cmd->clip[3] = clip[3]; }
   listAdd(&cmdFill, cmd);
}

void Direct3DBase::drawPixels(int *x, int *y, int count, int color)
{
   std::lock_guard<std::mutex> lock(listMutex);
   int32 *nx, *ny;
   nx = (int32*)heapAlloc(cmdFill.heap, count * sizeof(int32));
   ny = (int32*)heapAlloc(cmdFill.heap, count * sizeof(int32));
   D3DCommand cmd = newCommand();
   cmd->cmd = D3DCMD_DRAWPIXELS;
   xmemmove(nx, x, count * sizeof(int32));
   xmemmove(ny, y, count * sizeof(int32));
   cmd->a = (int)nx;
   cmd->b = (int)ny;
   cmd->c = count;
   cmd->c1.pixel = color;
   listAdd(&cmdFill, cmd);
}

void Direct3DBase::swapLists()
{
   std::lock_guard<std::mutex> lock(listMutex);
   D3DCommands oldDraw = cmdDraw;
   // 1. draw what has been filled
   cmdDraw = cmdFill;
   // 2. clear fill
   cmdFill = oldDraw;
   listSetEmpty(&cmdFill);
}

int Direct3DBase::runCommands()
{
   std::lock_guard<std::mutex> lock(listMutex);
   int n = 0;
   if (!listIsEmpty(&cmdDraw))
   {
      preRender();
      for (D3DCommand c = cmdDraw.head; c != NULL; c = c->next)
      {
         n++;
         switch (c->cmd)
         {
            case D3DCMD_FILLSHADEDRECT:
               fillShadedRectImpl(c->a, c->b, c->c, c->d, c->c1, c->c2, c->flags.horiz?true:false, c->clip);
               break;
            case D3DCMD_FILLRECT:
               fillRectImpl(c->a, c->b, c->c, c->d, c->c1.pixel);
               break;
            case D3DCMD_DRAWLINE:
               drawLineImpl(c->a, c->b, c->c, c->d, c->c1.pixel);
               break;
            case D3DCMD_DRAWTEXTURE:
               drawTextureImpl(c->textureId, c->a, c->b, c->c, c->d, c->e, c->f, c->g, c->h, c->flags.hasColor ? &c->c1 : null, c->flags.hasClip ? c->clip : null);
               break;
            case D3DCMD_DRAWPIXELS:
               int *x = (int*)c->a;
               int *y = (int*)c->b;
               drawPixelsImpl(x, y, c->c, c->c1.pixel);
               break;
         }
      }
   }
   return n;
}

// Constructor.
Direct3DBase::Direct3DBase(PhoneDirect3DXamlAppComponent::CSwrapper ^cs)
{
   csharp = cs;
   instance = this;
   listInit(&cmdFill);
}

Direct3DBase ^Direct3DBase::getLastInstance()
{
	return instance;
}

// Initialize the Direct3D resources required to run.
void Direct3DBase::initialize(_In_ ID3D11Device1* device)
{
	wchar_t mensagem_fim[2048];
	int saida;

	d3dDevice = device;
	saida = startVM("UIControls", &local_context);

	if (saida != 0) 
   {
		swprintf_s(mensagem_fim, 1000, L"Error code in starting VM: %d", saida);
		csharp->privateAlertCS(ref new Platform::String(mensagem_fim));
	}
	createDeviceResources();
}

// These are the resources that depend on the device.
void Direct3DBase::createDeviceResources()
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
      DX::ThrowIfFailed(d3dDevice->CreateVertexShader(fileData->Data, fileData->Length, nullptr, &vertexShader));
      const D3D11_INPUT_ELEMENT_DESC vertexDesc[] =
      {
         { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      };
      DX::ThrowIfFailed(d3dDevice->CreateInputLayout(vertexDesc, ARRAYSIZE(vertexDesc), fileData->Data, fileData->Length, &inputLayout));
      loadCompleted |= 1;
   });

   // global color pixel
   auto createPSTask1 = loadPSTask1.then([this](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(d3dDevice->CreatePixelShader(fileData->Data, fileData->Length, nullptr, &pixelShader));
      CD3D11_BUFFER_DESC constantBufferDesc(sizeof(ProjectionConstantBuffer), D3D11_BIND_CONSTANT_BUFFER);
      DX::ThrowIfFailed(d3dDevice->CreateBuffer(&constantBufferDesc, nullptr, &constantBuffer));
      loadCompleted |= 2;
   });

   // texture vertex
   auto createVSTask2 = loadVSTask2.then([this](Platform::Array<byte>^ fileData) 
   {
      DX::ThrowIfFailed(d3dDevice->CreateVertexShader(fileData->Data, fileData->Length, nullptr, &vertexShaderT));
      const D3D11_INPUT_ELEMENT_DESC vertexDesc[] =
      {
         { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
         { "TEXCOORD", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 8, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      };
      DX::ThrowIfFailed(d3dDevice->CreateInputLayout(vertexDesc, ARRAYSIZE(vertexDesc), fileData->Data, fileData->Length, &inputLayoutT));
      loadCompleted |= 4;
   });

   // texture pixel
   auto createPSTask2 = loadPSTask2.then([this](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(d3dDevice->CreatePixelShader(fileData->Data, fileData->Length, nullptr, &pixelShaderT));
      loadCompleted |= 8;
   });

   // local color vertex
   auto createVSTask3 = loadVSTask3.then([this](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(d3dDevice->CreateVertexShader(fileData->Data, fileData->Length, nullptr, &vertexShaderLC));
      const D3D11_INPUT_ELEMENT_DESC vertexDesc[] =
      {
         { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
         { "COLOR", 0,    DXGI_FORMAT_R32G32B32A32_FLOAT, 0, 8, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      };
      DX::ThrowIfFailed(d3dDevice->CreateInputLayout(vertexDesc, ARRAYSIZE(vertexDesc), fileData->Data, fileData->Length, &inputLayoutLC));
      loadCompleted |= 16;
   });

   // local color pixel
   auto createPSTask3 = loadPSTask3.then([this](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(d3dDevice->CreatePixelShader(fileData->Data, fileData->Length, nullptr, &pixelShaderLC));
      loadCompleted |= 32;
   });

}

void Direct3DBase::updateDevice(_In_ ID3D11Device1* device, _In_ ID3D11DeviceContext1 *ic, _In_ ID3D11RenderTargetView* renderTargetView)
{
	this->renderTargetView = renderTargetView;
   d3dcontext = ic;

	if (d3dDevice.Get() != device)
	{
		d3dDevice->GetDeviceRemovedReason();
      eventsInitialized = false;
		d3dDevice = device;
		createDeviceResources();
		// Force call to CreateWindowSizeDependentResources.
		renderTargetSize.Width  = -1;
		renderTargetSize.Height = -1;
	}

	ComPtr<ID3D11Resource> renderTargetViewResource;
	renderTargetView->GetResource(&renderTargetViewResource);

	ComPtr<ID3D11Texture2D> backBuffer;
	DX::ThrowIfFailed(renderTargetViewResource.As(&backBuffer));

	// Cache the rendertarget dimensions in our helper class for convenient use.
   D3D11_TEXTURE2D_DESC backBufferDesc;
   backBuffer->GetDesc(&backBufferDesc);

   if (renderTargetSize.Width  != static_cast<float>(backBufferDesc.Width) || renderTargetSize.Height != static_cast<float>(backBufferDesc.Height))
   {
      renderTargetSize.Width  = static_cast<float>(backBufferDesc.Width);
      renderTargetSize.Height = static_cast<float>(backBufferDesc.Height);
      createWindowSizeDependentResources();
   }
}

// Allocate all memory resources that depend on the window size.
void Direct3DBase::createWindowSizeDependentResources()
{
	// Create a depth stencil view.
	CD3D11_TEXTURE2D_DESC depthStencilDesc(DXGI_FORMAT_D24_UNORM_S8_UINT,static_cast<UINT>(renderTargetSize.Width),static_cast<UINT>(renderTargetSize.Height),1,1,D3D11_BIND_DEPTH_STENCIL);
	ComPtr<ID3D11Texture2D> depthStencil;
	DX::ThrowIfFailed(d3dDevice->CreateTexture2D(&depthStencilDesc,nullptr,&depthStencil));
	CD3D11_DEPTH_STENCIL_VIEW_DESC depthStencilViewDesc(D3D11_DSV_DIMENSION_TEXTURE2D);
	DX::ThrowIfFailed(d3dDevice->CreateDepthStencilView(depthStencil.Get(),&depthStencilViewDesc,&depthStencilView));

   XMStoreFloat4x4(&constantBufferData.projection, XMMatrixOrthographicOffCenterLH(0, windowBounds.Width, windowBounds.Height, 0, -1.0f, 1.0f));
   setup();
}

void Direct3DBase::updateForWindowSizeChange(float width, float height)
{
	windowBounds.Width  = width;
	windowBounds.Height = height;
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
   d3dDevice->CreateBuffer(&indexBufferDesc, &indexBufferData, &indexBuffer);

   // used in setColor for fillRect and drawLine and also textures
   {
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexColor);             // size is the VERTEX struct * 3
      bd.BindFlags = D3D11_BIND_CONSTANT_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      d3dDevice->CreateBuffer(&bd, NULL, &pBufferColor);       // create the buffer
   }
   // used in fillRect and drawLine
   {
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexPosition) * 4;     // size is the VERTEX
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      d3dDevice->CreateBuffer(&bd, NULL, &pBufferRect);       // create the buffer
   }
   // used in fillShadedRect
   {
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexPositionColor) * 4;             // size is the VERTEX
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      d3dDevice->CreateBuffer(&bd, NULL, &pBufferRectLC);       // create the buffer
   }

   /////////// TEXTURE
   // Once the texture view is created, create a sampler.  This defines how the color
   // for a particular texture coordinate is determined using the relevant texture data.
   D3D11_SAMPLER_DESC samplerDesc;
   ZeroMemory(&samplerDesc, sizeof(samplerDesc));
   samplerDesc.Filter = D3D11_FILTER_MIN_MAG_MIP_LINEAR;
   samplerDesc.AddressU = samplerDesc.AddressV = samplerDesc.AddressW = D3D11_TEXTURE_ADDRESS_CLAMP; // Feature level 9_3, the display device supports the use of 2-D textures with dimensions that are not powers of two under two conditions. First, only one MIP-map level for each texture can be created, and second, no wrap sampler modes for textures are allowed (that is, the AddressU, AddressV, and AddressW members of D3D11_SAMPLER_DESC cannot be set to D3D11_TEXTURE_ADDRESS_WRAP).
   samplerDesc.MaxLOD = D3D11_FLOAT32_MAX;
   samplerDesc.ComparisonFunc = D3D11_COMPARISON_NEVER;
   DX::ThrowIfFailed(d3dDevice->CreateSamplerState(&samplerDesc, &texsampler));

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
   d3dDevice->CreateDepthStencilState(&depthDisabledStencilDesc, &depthDisabledStencilState);

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
   d3dDevice->CreateBlendState(&blendStateDescription, &pBlendState);

   // setup clipping
   D3D11_RASTERIZER_DESC1 rasterizerState = { D3D11_FILL_SOLID };
   rasterizerState.CullMode = D3D11_CULL_FRONT;
   rasterizerState.FrontCounterClockwise = true;
   rasterizerState.DepthClipEnable = true;
   d3dDevice->CreateRasterizerState1(&rasterizerState, &pRasterStateDisableClipping);
   rasterizerState.ScissorEnable = true;
   d3dDevice->CreateRasterizerState1(&rasterizerState, &pRasterStateEnableClipping);

   // texture vertices
   D3D11_BUFFER_DESC bd = { 0 };
   bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
   bd.ByteWidth = sizeof(TextureVertex) * 8;             // size is the VERTEX struct * 3
   bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
   bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
   d3dDevice->CreateBuffer(&bd, NULL, &texVertexBuffer);       // create the buffer
}

#define f255(x) ((float)x/255.0f)
void Direct3DBase::fillShadedRectImpl(int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz, int32* clip)
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
   setClip(clip);
   D3D11_MAPPED_SUBRESOURCE ms;
   d3dcontext->Map(pBufferRectLC, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   d3dcontext->Unmap(pBufferRectLC, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPositionColor);
   UINT offset = 0;
   d3dcontext->IASetVertexBuffers(0, 1, &pBufferRectLC, &stride, &offset);
   d3dcontext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
   d3dcontext->IASetIndexBuffer(indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   d3dcontext->DrawIndexed(6, 0, 0);
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

   d3dcontext->Map(pBufferColor, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, &vcolor, sizeof(VertexColor));                // copy the data
   d3dcontext->Unmap(pBufferColor, NULL);                                     // unmap the buffer

   d3dcontext->VSSetConstantBuffers(1, 1, &pBufferColor);
}

void Direct3DBase::drawLineImpl(int x1, int y1, int x2, int y2, int color)
{
   VertexPosition cubeVertices[] = // position, color
   {
      { XMFLOAT2((float)x1, (float)y1) },
      { XMFLOAT2((float)x2, (float)y2) },
   };

   setProgram(PROGRAM_GC);
   D3D11_MAPPED_SUBRESOURCE ms;
   d3dcontext->Map(pBufferRect, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   d3dcontext->Unmap(pBufferRect, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   d3dcontext->IASetVertexBuffers(0, 1, &pBufferRect, &stride, &offset);
   d3dcontext->IASetIndexBuffer(indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   d3dcontext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST);
   setColor(color);
   d3dcontext->DrawIndexed(2, 0, 0);
}

void Direct3DBase::fillRectImpl(int x1, int y1, int x2, int y2, int color)
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
   d3dcontext->Map(pBufferRect, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   d3dcontext->Unmap(pBufferRect, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   d3dcontext->IASetVertexBuffers(0, 1, &pBufferRect, &stride, &offset);
   d3dcontext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
   d3dcontext->IASetIndexBuffer(indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   setColor(color);
   d3dcontext->DrawIndexed(6, 0, 0);
}

void Direct3DBase::drawPixelsImpl(int *x, int *y, int count, int color)
{
   int i;
   int n = count * 2;
   VertexPosition *cubeVertices = new VertexPosition[n];// position, color
   XMFLOAT3 xcolor = XMFLOAT3(rr, gg, bb);
   for (i = 0; i < n;)
   {
      cubeVertices[i].pos = XMFLOAT2((float)*x, (float)*y);
      i++;
      cubeVertices[i].pos = XMFLOAT2((float)(*x)+1, (float)*y);
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
      DX::ThrowIfFailed(d3dDevice->CreateBuffer(&indexBufferDesc, &indexBufferData, &pixelsIndexBuffer));
      delete cubeIndexes;

      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexPosition)* n;             // size is the VERTEX struct * 3
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      d3dDevice->CreateBuffer(&bd, NULL, &pBufferPixels);       // create the buffer
   }

   D3D11_MAPPED_SUBRESOURCE ms;
   d3dcontext->Map(pBufferPixels, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(VertexPosition)* n);                // copy the data
   d3dcontext->Unmap(pBufferPixels, NULL);                                     // unmap the buffer
   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   d3dcontext->IASetVertexBuffers(0, 1, &pBufferPixels, &stride, &offset);
   d3dcontext->IASetIndexBuffer(pixelsIndexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   d3dcontext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST);
   setColor(color);
   d3dcontext->DrawIndexed(n, 0, 0);

   delete cubeVertices;
}

bool Direct3DBase::isLoadCompleted() 
{
   return loadCompleted == TASKS_COMPLETED && eventsInitialized;
}

void Direct3DBase::updateScreen()
{
   swapLists();
   updateScreenWaiting = true;
   PhoneDirect3DXamlAppComponent::Direct3DBackground::GetInstance()->RequestNewFrame();
   while (updateScreenWaiting) Sleep(0);
}

void Direct3DBase::preRender()
{
   const float clearColor[4] = { 0.0f, 0.0f, 0.0f, 0.0f };
   // Set the rendering viewport to target the entire window.
   CD3D11_VIEWPORT viewport(0.0f, 0.0f, renderTargetSize.Width, renderTargetSize.Height);
   d3dcontext->RSSetViewports(1, &viewport);

   d3dcontext->ClearRenderTargetView(renderTargetView.Get(), clearColor);
   d3dcontext->ClearDepthStencilView(depthStencilView.Get(), D3D11_CLEAR_DEPTH, 1.0f, 0);

   d3dcontext->OMSetDepthStencilState(depthDisabledStencilState, 1);
   d3dcontext->OMSetBlendState(pBlendState, 0, 0xffffffff);

   d3dcontext->OMSetRenderTargets(1, renderTargetView.GetAddressOf(), depthStencilView.Get());
   d3dcontext->UpdateSubresource(constantBuffer.Get(), 0, NULL, &constantBufferData, 0, 0);
   curProgram = PROGRAM_NONE;
   d3dcontext->RSSetState(pRasterStateDisableClipping);
   clipSet = false;
}

void Direct3DBase::setProgram(whichProgram p)
{
   if (p == curProgram) return;
   lastRGB = 0xFAFFFFFF; // user may never set to this color
   curProgram = p;
   clipRect.right = -1;
   d3dcontext->RSSetState(pRasterStateDisableClipping);
   clipSet = false;
   switch (p)
   {
      case PROGRAM_GC:
         d3dcontext->VSSetShader(vertexShader.Get(), nullptr, 0);
         d3dcontext->PSSetShader(pixelShader.Get(), nullptr, 0);
         d3dcontext->IASetInputLayout(inputLayout.Get());
         break;
      case PROGRAM_LC:
         d3dcontext->VSSetShader(vertexShaderLC.Get(), nullptr, 0);
         d3dcontext->PSSetShader(pixelShaderLC.Get(), nullptr, 0);
         d3dcontext->IASetInputLayout(inputLayoutLC.Get());
         break;
      case PROGRAM_TEX:
         d3dcontext->PSSetSamplers(0, 1, texsampler.GetAddressOf());
         d3dcontext->UpdateSubresource(constantBuffer.Get(), 0, nullptr, &constantBufferData, 0, 0);
         d3dcontext->VSSetShader(vertexShaderT.Get(), nullptr, 0);
         d3dcontext->PSSetShader(pixelShaderT.Get(), nullptr, 0);
         d3dcontext->IASetInputLayout(inputLayoutT.Get());
         d3dcontext->IASetIndexBuffer(indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
         d3dcontext->IASetPrimitiveTopology(D3D11_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
         break;
   }
   d3dcontext->VSSetConstantBuffers(0, 1, constantBuffer.GetAddressOf());
}

bool Direct3DBase::startProgramIfNeeded()
{
   if (!VMStarted && isLoadCompleted())
   {
	   VMStarted = true;
	   auto lambda = [this]()  // program start
      {
		   startProgram(local_context); // this will block until the application ends
	   };
	   std::thread(lambda).detach();
   }
   return VMStarted;
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
   if (FAILED(d3dDevice->CreateTexture2D(&textureDesc, &textureSubresourceData, &texture)))
      throwException(currentContext, OutOfMemoryError, "Out of texture memory for image with %dx%d", width, height);
   else
   {
      ID3D11ShaderResourceView* textureView;
      D3D11_SHADER_RESOURCE_VIEW_DESC textureViewDesc;
      ZeroMemory(&textureViewDesc, sizeof(textureViewDesc));
      textureViewDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
      textureViewDesc.ViewDimension = D3D11_SRV_DIMENSION_TEXTURE2D;
      textureViewDesc.Texture2D.MipLevels = 1;
      d3dDevice->CreateShaderResourceView(&texture[0], &textureViewDesc, &textureView);
      xmoveptr(&textureId[0], &texture);
      xmoveptr(&textureId[1], &textureView);
   }
   xfree(pt0);
}

void Direct3DBase::deleteTexture(TCObject img, int32* textureId, bool updateList)
{
}

void Direct3DBase::setClip(int32* clip)
{
   bool doClip = clip != null;
   if (!doClip && clipSet)
      d3dcontext->RSSetState(pRasterStateDisableClipping);
   else
   if (doClip)
   {
      if (!clipSet)
         d3dcontext->RSSetState(pRasterStateEnableClipping);
      if (clip[0] != clipRect.left || clip[1] != clipRect.top || clip[2] != clipRect.right || clip[3] != clipRect.bottom)
      {
         clipRect.left = clip[0];
         clipRect.top = clip[1];
         clipRect.right = clip[2];
         clipRect.bottom = clip[3];
         d3dcontext->RSSetScissorRects(1, &clipRect);
      }
   }
   clipSet = doClip;
}

void Direct3DBase::drawTextureImpl(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 imgW, int32 imgH, PixelConv *color, int32* clip)
{
   ID3D11Texture2D *texture;
   ID3D11ShaderResourceView *textureView;

   xmoveptr(&texture, &textureId[0]);
   xmoveptr(&textureView, &textureId[1]);
   setProgram(PROGRAM_TEX);
   setColor(!color ? 0 : 0xFF000000 | (color->r << 16) | (color->g << 8) | color->b);

   setClip(clip);

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
   d3dcontext->Map(texVertexBuffer, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   d3dcontext->Unmap(texVertexBuffer, NULL);                                     // unmap the buffer

   // Set the vertex and index buffers, and specify the way they define geometry.
   UINT stride = sizeof(TextureVertex);
   UINT offset = 0;
   d3dcontext->IASetVertexBuffers(0, 1, &texVertexBuffer, &stride, &offset);

   // Set the vertex and pixel shader stage state.
   d3dcontext->PSSetShaderResources(0, 1, &textureView);
   // Draw the cube.
   d3dcontext->DrawIndexed(6, 0, 0);
}
