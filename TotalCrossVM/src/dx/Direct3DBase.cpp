#include "Direct3DBase.h"
#include <thread>
#include <mutex>
#include "PhoneDirect3DXamlAppComponent.h"
#define HAS_TCHAR
#include "tcvm.h"

static float f255[256];

using namespace DirectX;
using namespace Microsoft::WRL;
using namespace Windows::Foundation;
using namespace Windows::UI::Core;

#ifdef _DEBUG
#define DEBUG_APP "TotalCrossAPI"
#endif

#define DXRELEASE(x) do {if (x) {x->Release(); x = null;}} while (0)
static Direct3DBase ^instance;

#define DxFailed(x) _DxFailed(x, /*__FILE__, */__LINE__) // file is always at Direct3DBase.cpp
inline bool _DxFailed(HRESULT hr, /*const char *file, */int line)
{
   if (FAILED(hr))
   {
      debug("DX failed: %X (line %d)", hr, /*file, */line);// Platform::Exception::ex = Platform::Exception::CreateException(hr); // Set a breakpoint on this line to catch Win32 API errors.
      return true;
   }
   return false;
}

extern "C" 
{ 
   extern int32 appW, appH, glShiftY;
   void invalidateTextures();
   void repaintActiveWindows(Context currentContext);
}

// Constructor.
Direct3DBase::Direct3DBase(PhoneDirect3DXamlAppComponent::CSwrapper ^cs)
{
   csharp = cs;
   instance = this;
   for (int i = 0; i <= 255; i++)
      f255[i] = (float)i / (float)255;
   initialize();
}

Direct3DBase ^Direct3DBase::getLastInstance()
{
	return instance;
}

void Direct3DBase::setBufAndLen(Platform::Array<byte>^ fileData, byte** buf, int* len, int completeValue)
{
   *len = fileData->Length;
   *buf = new byte[*len]; 
   memmove(*buf, fileData->Data, *len);
   loadCompleted |= completeValue;
}

// Initialize the Direct3D resources required to run.
void Direct3DBase::initialize()
{
#ifndef DEBUG_APP
	const wchar_t* wide_chars = csharp->getAppName()->Data();
	char chars[512];
	wcstombs(chars, wide_chars, 512);
	int exitCode = startVM(chars, &localContext);
#else
   int exitCode = startVM(DEBUG_APP, &localContext);
#endif

   if (exitCode != 0)
   {
      wchar_t exitMsg[64];
      swprintf_s(exitMsg, 64, L"Error code when starting VM: %d", exitCode);
      csharp->privateAlertCS(ref new Platform::String(exitMsg), eventsInitialized);
      csharp->appExit();
      return;
   }

   DX::ReadDataAsync("VertexShaderGlobalColor.cso").then([this](Platform::Array<byte>^ fileData) {setBufAndLen(fileData, &vs1buf, &vs1len, 1); });
   DX::ReadDataAsync("PixelShaderGlobalColor.cso" ).then([this](Platform::Array<byte>^ fileData) {setBufAndLen(fileData, &ps1buf, &ps1len, 2); });
   DX::ReadDataAsync("VertexShaderTexture.cso"    ).then([this](Platform::Array<byte>^ fileData) {setBufAndLen(fileData, &vs2buf, &vs2len, 4); });
   DX::ReadDataAsync("PixelShaderTexture.cso"     ).then([this](Platform::Array<byte>^ fileData) {setBufAndLen(fileData, &ps2buf, &ps2len, 8); });
   DX::ReadDataAsync("VertexShaderLocalColor.cso" ).then([this](Platform::Array<byte>^ fileData) {setBufAndLen(fileData, &vs3buf, &vs3len, 16); });
   DX::ReadDataAsync("PixelShaderLocalColor.cso"  ).then([this](Platform::Array<byte>^ fileData) {setBufAndLen(fileData, &ps3buf, &ps3len, 32); });
}

void Direct3DBase::updateScreenMatrix()
{
   XMMATRIX mat = XMMatrixOrthographicOffCenterLH(0, (float)appW, (float)appH, 0, -1.0f, 1.0f);
   XMStoreFloat4x4(&constantBufferData.projection, mat);
}

void Direct3DBase::updateDevice(IDrawingSurfaceRuntimeHostNative* host)
{
   while (!isLoadCompleted())
      Sleep(100);

   // create the D3DDevice
   UINT creationFlags = D3D11_CREATE_DEVICE_BGRA_SUPPORT;
   //creationFlags |= D3D11_CREATE_DEVICE_DEBUG;
   D3D_FEATURE_LEVEL featureLevels[] = { D3D_FEATURE_LEVEL_9_3 };
   if (DxFailed(D3D11CreateDevice(nullptr, D3D_DRIVER_TYPE_HARDWARE, nullptr, creationFlags, featureLevels, ARRAYSIZE(featureLevels), D3D11_SDK_VERSION, &d3dDevice, &m_featureLevel, &d3dImedContext)))
      return;
   d3dDevice->CreateDeferredContext(0, &d3dcontext);

   DXRELEASE(depthStencil);                 DXRELEASE(pBlendState);                     DXRELEASE(pixelShader);
   DXRELEASE(depthStencilView);             DXRELEASE(constantBuffer);
   DXRELEASE(indexBuffer);                  DXRELEASE(vertexShaderT);
   DXRELEASE(pBufferColor);                 DXRELEASE(texVertexBuffer);                 DXRELEASE(inputLayoutT);
   DXRELEASE(pBufferRect);                  DXRELEASE(renderTexView);                   DXRELEASE(pixelShaderT);
   DXRELEASE(pBufferRectLC);                DXRELEASE(renderTex);                       DXRELEASE(vertexShaderLC);
   DXRELEASE(texsampler);                   DXRELEASE(vertexShader);                    DXRELEASE(inputLayoutLC);
   DXRELEASE(depthDisabledStencilState);    DXRELEASE(inputLayout);                     DXRELEASE(pixelShaderLC);

   // Create a descriptor for the render target buffer.
   int appSize = dxGetScreenSize();
   CD3D11_TEXTURE2D_DESC renderTargetDesc(DXGI_FORMAT_B8G8R8A8_UNORM, appSize, appSize, 1, 1, D3D11_BIND_RENDER_TARGET | D3D11_BIND_SHADER_RESOURCE);
   renderTargetDesc.MiscFlags = D3D11_RESOURCE_MISC_SHARED_KEYEDMUTEX | D3D11_RESOURCE_MISC_SHARED_NTHANDLE;

   // Allocate a 2-D surface as the render target buffer.
   if (DxFailed(d3dDevice->CreateTexture2D(&renderTargetDesc, nullptr, &renderTex))) return;
   if (DxFailed(d3dDevice->CreateRenderTargetView(renderTex, nullptr, &renderTexView))) return;
   host->CreateSynchronizedTexture(renderTex, &syncTex);

   // Create a depth stencil view.
   CD3D11_TEXTURE2D_DESC depthStencilDesc(DXGI_FORMAT_D24_UNORM_S8_UINT, appSize, appSize, 1, 1, D3D11_BIND_DEPTH_STENCIL);
   if (DxFailed(d3dDevice->CreateTexture2D(&depthStencilDesc, nullptr, &depthStencil))) return;
   CD3D11_DEPTH_STENCIL_VIEW_DESC depthStencilViewDesc(D3D11_DSV_DIMENSION_TEXTURE2D);
   if (DxFailed(d3dDevice->CreateDepthStencilView(depthStencil, &depthStencilViewDesc, &depthStencilView))) return;

   updateScreenMatrix();

   unsigned short cubeIndices[] =
   {
      0, 1, 2, 0, 2, 3
   };
   D3D11_SUBRESOURCE_DATA indexBufferData = { 0 };
   indexBufferData.pSysMem = cubeIndices;
   CD3D11_BUFFER_DESC indexBufferDesc(sizeof(cubeIndices), D3D11_BIND_INDEX_BUFFER);
   if (DxFailed(d3dDevice->CreateBuffer(&indexBufferDesc, &indexBufferData, &indexBuffer))) return;

   // used in setColor for fillRect and drawLine and also textures
   {
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexColor);             // size is the VERTEX struct * 3
      bd.BindFlags = D3D11_BIND_CONSTANT_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      if (DxFailed(d3dDevice->CreateBuffer(&bd, NULL, &pBufferColor))) return;       // create the buffer
   }
   // used in fillRect and drawLine
   {
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexPosition)* 4;     // size is the VERTEX
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      if (DxFailed(d3dDevice->CreateBuffer(&bd, NULL, &pBufferRect))) return;       // create the buffer
   }
   // used in fillShadedRect
   {
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.ByteWidth = sizeof(VertexPositionColor)* 4;             // size is the VERTEX
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      if (DxFailed(d3dDevice->CreateBuffer(&bd, NULL, &pBufferRectLC))) return;       // create the buffer
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
   if (DxFailed(d3dDevice->CreateSamplerState(&samplerDesc, &texsampler))) return;

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
   if (DxFailed(d3dDevice->CreateDepthStencilState(&depthDisabledStencilDesc, &depthDisabledStencilState))) return;

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
   if (DxFailed(d3dDevice->CreateBlendState(&blendStateDescription, &pBlendState))) return;

   // texture vertices
   D3D11_BUFFER_DESC bd = { 0 };
   bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
   bd.ByteWidth = sizeof(TextureVertex)* 8;             // size is the VERTEX struct * 3
   bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
   bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
   if (DxFailed(d3dDevice->CreateBuffer(&bd, NULL, &texVertexBuffer))) return;       // create the buffer

   byte* buf;
   int len;

   buf = vs1buf; len = vs1len;
   if (DxFailed(d3dDevice->CreateVertexShader(buf, len, nullptr, &vertexShader))) return;
   const D3D11_INPUT_ELEMENT_DESC vertexDesc1[] =
   {
      { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
   };
   if (DxFailed(d3dDevice->CreateInputLayout(vertexDesc1, ARRAYSIZE(vertexDesc1), buf, len, &inputLayout))) return;

   buf = ps1buf; len = ps1len;
   if (DxFailed(d3dDevice->CreatePixelShader(buf, len, nullptr, &pixelShader))) return;
   CD3D11_BUFFER_DESC constantBufferDesc(sizeof(ProjectionConstantBuffer), D3D11_BIND_CONSTANT_BUFFER);
   if (DxFailed(d3dDevice->CreateBuffer(&constantBufferDesc, nullptr, &constantBuffer))) return;

   buf = vs2buf; len = vs2len;
   if (DxFailed(d3dDevice->CreateVertexShader(buf, len, nullptr, &vertexShaderT))) return;
   const D3D11_INPUT_ELEMENT_DESC vertexDesc2[] =
   {
      { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      { "TEXCOORD", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 8, D3D11_INPUT_PER_VERTEX_DATA, 0 },
   };
   if (DxFailed(d3dDevice->CreateInputLayout(vertexDesc2, ARRAYSIZE(vertexDesc2), buf, len, &inputLayoutT))) return;

   buf = ps2buf; len = ps2len;
   if (DxFailed(d3dDevice->CreatePixelShader(buf, len, nullptr, &pixelShaderT))) return;

   buf = vs3buf; len = vs3len;
   if (DxFailed(d3dDevice->CreateVertexShader(buf, len, nullptr, &vertexShaderLC))) return;
   const D3D11_INPUT_ELEMENT_DESC vertexDesc3[] =
   {
      { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      { "COLOR", 0, DXGI_FORMAT_R32G32B32A32_FLOAT, 0, 8, D3D11_INPUT_PER_VERTEX_DATA, 0 },
   };
   if (DxFailed(d3dDevice->CreateInputLayout(vertexDesc3, ARRAYSIZE(vertexDesc3), buf, len, &inputLayoutLC))) return;

   buf = ps3buf; len = ps3len;
   if (DxFailed(d3dDevice->CreatePixelShader(buf, len, nullptr, &pixelShaderLC))) return;

   // reset variables
   lastPixelsCount = lastLinesCount = 0;

   if (!vmStarted)
      std::thread([this]() {startProgram(localContext); }).detach(); // this will block until the application ends         
   vmStarted = true;
   preRender();
   PhoneDirect3DXamlAppComponent::Direct3DBackground::GetInstance()->RequestNewFrame(); // fixes black screen that appeared run % 2
   eventQueuePush(PENEVENT_PEN_DOWN, 0, 0, -1, -1);  // fixes black screen when resuming from wp8 if the screen was not pressed before
}

void Direct3DBase::setColor(int color, int alphaMask)
{
   if (alphaMask == 255 && (color == lastRGB || minimized)) return;
   lastRGB = color;
   aa = ((color >> 24) & 0xFF) / 255.0f;
   rr = ((color >> 16) & 0xFF) / 255.0f;
   gg = ((color >> 8) & 0xFF) / 255.0f;
   bb = (color & 0xFF) / 255.0f;
   float aam = alphaMask / 255.0f;

   VertexColor vcolor;
   vcolor.color = XMFLOAT4(rr, gg, bb, aa);
   vcolor.alphaMask = XMFLOAT4(0, 0, 0, aam);

   D3D11_MAPPED_SUBRESOURCE ms;

   d3dcontext->Map(pBufferColor, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, &vcolor, sizeof(VertexColor));                // copy the data
   d3dcontext->Unmap(pBufferColor, NULL);                                     // unmap the buffer

   d3dcontext->VSSetConstantBuffers(1, 1, &pBufferColor);
}

void Direct3DBase::fillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz)
{
   if (minimized) return;
   y += glShiftY;
   float x1 = (float)x, y1 = (float)y, x2 = x1 + w, y2 = y1 + h;
   XMFLOAT4 color1 = XMFLOAT4(f255[c2.r], f255[c2.g], f255[c2.b], f255[c2.a]);
   XMFLOAT4 color2 = XMFLOAT4(f255[c1.r], f255[c1.g], f255[c1.b], f255[c1.a]);
   VertexPositionColor vertices[] = // position, color
   {
      { XMFLOAT2(x1, y1), horiz ? color1 : color1 },
      { XMFLOAT2(x2, y1), horiz ? color2 : color1 },
      { XMFLOAT2(x2, y2), horiz ? color2 : color2 },
      { XMFLOAT2(x1, y2), horiz ? color1 : color2 },
   };

   setProgram(PROGRAM_LC);
   D3D11_MAPPED_SUBRESOURCE ms;
   d3dcontext->Map(pBufferRectLC, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, vertices, sizeof(vertices));                // copy the data
   d3dcontext->Unmap(pBufferRectLC, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPositionColor);
   UINT offset = 0;
   d3dcontext->IASetVertexBuffers(0, 1, &pBufferRectLC, &stride, &offset);
   d3dcontext->IASetIndexBuffer(indexBuffer, DXGI_FORMAT_R16_UINT, 0);
   d3dcontext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
   d3dcontext->DrawIndexed(6, 0, 0);
}

void Direct3DBase::drawLines(Context currentContext, TCObject g, int32* xx, int32* yy, int32 count, int32 tx, int32 ty, int color, bool fill)
{
   if (minimized) return;
   int i;
   VertexPosition *vertices = (VertexPosition*) xmalloc(sizeof(VertexPosition) * count), *v = vertices;// position, color
   ty += glShiftY;
   setColor(color,255);
   for (i = count; --i >= 0; v++)
      v->pos = XMFLOAT2((float)(*xx++ + tx), (float)(*yy++ + ty)); 
   setProgram(PROGRAM_GC);

   if (count > lastLinesCount)
   {
      lastLinesCount = count;
      DXRELEASE(linesIndexBuffer);
      unsigned short *indexes = (unsigned short *)xmalloc(sizeof(unsigned short) * count);
      for (i = count; --i >= 0;) indexes[i] = i;
      D3D11_SUBRESOURCE_DATA indexBufferData = { indexes, 0, 0 };
      CD3D11_BUFFER_DESC indexBufferDesc(sizeof(indexes[0]) * count, D3D11_BIND_INDEX_BUFFER);
      if (DxFailed(d3dDevice->CreateBuffer(&indexBufferDesc, &indexBufferData, &linesIndexBuffer))) return;
      xfree(indexes);

      DXRELEASE(pBufferLines);
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      bd.ByteWidth = sizeof(vertices[0]) * count;             // size is the VERTEX struct * 3
      d3dDevice->CreateBuffer(&bd, NULL, &pBufferLines);       // create the buffer
   }

   D3D11_MAPPED_SUBRESOURCE ms;
   d3dcontext->Map(pBufferLines, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, vertices, sizeof(vertices[0]) * count);                    // copy the data
   d3dcontext->Unmap(pBufferLines, NULL);                                     // unmap the buffer
   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;

   d3dcontext->IASetVertexBuffers(0, 1, &pBufferLines, &stride, &offset);
   d3dcontext->IASetIndexBuffer(linesIndexBuffer, DXGI_FORMAT_R16_UINT, 0);
   d3dcontext->IASetPrimitiveTopology(fill ? D3D10_PRIMITIVE_TOPOLOGY_TRIANGLELIST: D3D10_PRIMITIVE_TOPOLOGY_LINELIST);
   d3dcontext->DrawIndexed(count, 0, 0);

   xfree(vertices);
}

bool Direct3DBase::checkPixelsBuf(int32 n)
{
   if (n > lastPixelsCount)
   {
      int32 i;
      lastPixelsCount = n;
      DXRELEASE(pixelsIndexBuffer);
      unsigned short *indexes = (unsigned short *)xmalloc(sizeof(unsigned short) * n);
      for (i = n; --i >= 0;) indexes[i] = i;
      D3D11_SUBRESOURCE_DATA indexBufferData = { indexes, 0, 0 };
      CD3D11_BUFFER_DESC indexBufferDesc(sizeof(indexes[0]) * n, D3D11_BIND_INDEX_BUFFER);
      if (FAILED(d3dDevice->CreateBuffer(&indexBufferDesc, &indexBufferData, &pixelsIndexBuffer)))
      {
         xfree(indexes);
         return false;
      }
      xfree(indexes);

      DXRELEASE(pBufferPixels);
      D3D11_BUFFER_DESC bd = { 0 };
      bd.Usage = D3D11_USAGE_DYNAMIC;                // write access access by CPU and GPU
      bd.BindFlags = D3D11_BIND_VERTEX_BUFFER;       // use as a vertex buffer
      bd.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE;    // allow CPU to write in buffer
      bd.ByteWidth = sizeof(VertexPositionColor)* n;             // size is the VERTEX struct * 3
      if (FAILED(d3dDevice->CreateBuffer(&bd, NULL, &pBufferPixels)))       // create the buffer
         return false;
   }
   return true;
}

void Direct3DBase::drawPixelColors(int32* xx, int32* yy, PixelConv* colors, int32 count)
{
   if (minimized) return;
   int i, n = count * 2;
   VertexPositionColor *vertices = (VertexPositionColor *)xmalloc(sizeof(VertexPositionColor) * n), *v = vertices;// position, color
   for (i = count; --i >= 0;)
   {
      float x = (float) *xx++;
      float y = (float) *yy++;
      PixelConv color = *colors++;
      XMFLOAT4 cor = XMFLOAT4(f255[color.r], f255[color.g], f255[color.b], f255[color.a]);
      v->pos = XMFLOAT2(x, y + glShiftY); v->color = cor;  v++;
      v->pos = XMFLOAT2(x + 1, y + 1 + glShiftY); v->color = cor;  v++;
   }
   setProgram(PROGRAM_LC);
   if (!checkPixelsBuf(n))
   {
      xfree(vertices);
      return;
   }

   D3D11_MAPPED_SUBRESOURCE ms;
   d3dcontext->Map(pBufferPixels, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, vertices, sizeof(vertices[0]) * n);               // copy the data
   d3dcontext->Unmap(pBufferPixels, NULL);                                     // unmap the buffer
   UINT stride = sizeof(vertices[0]);
   UINT offset = 0;
   d3dcontext->IASetVertexBuffers(0, 1, &pBufferPixels, &stride, &offset);
   d3dcontext->IASetIndexBuffer(pixelsIndexBuffer, DXGI_FORMAT_R16_UINT, 0);
   d3dcontext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST); // POINTLIST results in points being drawn in a slightly different position
   d3dcontext->DrawIndexed(n, 0, 0);

   xfree(vertices);
}

void Direct3DBase::drawPixels(float* glXYA, int count, int color)
{
   if (minimized) return;
   XMFLOAT4 cor = XMFLOAT4(f255[(color >> 16) & 0xFF], f255[(color >> 8) & 0xFF], f255[color & 0xFF], 0);
   if (count == 1) // drawLine for a single pixel is faster than drawPixels
   {
      int32 x = (int32)*glXYA++, y = (int32)*glXYA++;
      int32 a = (int)(255.0f * *glXYA++);
      drawLine(x, y, x + 1, y + 1, (a << 24) | color);
      return;
   }
   int i, n = count * 2;
   VertexPositionColor *vertices = (VertexPositionColor *)xmalloc(sizeof(VertexPositionColor) * n), *v = vertices;// position, color
   for (i = count; --i >= 0;)
   {
      float x = *glXYA++;
      float y = *glXYA++;
      cor.w = *glXYA++; // alpha [0,1)
      v->pos = XMFLOAT2(x, y + glShiftY); v->color = cor;  v++;
      v->pos = XMFLOAT2(x + 1, y + 1 + glShiftY); v->color = cor;  v++;
   }
   setProgram(PROGRAM_LC);
   if (!checkPixelsBuf(n))
   {
      xfree(vertices);
      return;
   }

   D3D11_MAPPED_SUBRESOURCE ms;
   if (DxFailed(d3dcontext->Map(pBufferPixels, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms))) return;   // map the buffer
   memcpy(ms.pData, vertices, sizeof(vertices[0]) * n);               // copy the data
   d3dcontext->Unmap(pBufferPixels, NULL);                                     // unmap the buffer
   UINT stride = sizeof(vertices[0]);
   UINT offset = 0;
   d3dcontext->IASetVertexBuffers(0, 1, &pBufferPixels, &stride, &offset);
   d3dcontext->IASetIndexBuffer(pixelsIndexBuffer, DXGI_FORMAT_R16_UINT, 0);
   d3dcontext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST); // POINTLIST results in points being drawn in a slightly different position
   d3dcontext->DrawIndexed(n, 0, 0);

   xfree(vertices);
}

void Direct3DBase::drawLine(int x1, int y1, int x2, int y2, int color)
{
   if (minimized) return;
   y1 += glShiftY;
   y2 += glShiftY;
   VertexPosition vertices[] = // position, color
   {
      { XMFLOAT2((float)x1, (float)y1) },
      { XMFLOAT2((float)x2, (float)y2) },
   };

   setProgram(PROGRAM_GC);
   D3D11_MAPPED_SUBRESOURCE ms;
   d3dcontext->Map(pBufferRect, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, vertices, sizeof(vertices));                // copy the data
   d3dcontext->Unmap(pBufferRect, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   d3dcontext->IASetVertexBuffers(0, 1, &pBufferRect, &stride, &offset);
   d3dcontext->IASetIndexBuffer(indexBuffer, DXGI_FORMAT_R16_UINT, 0);
   d3dcontext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST);
   setColor(color,255);
   d3dcontext->DrawIndexed(2, 0, 0);
}

void Direct3DBase::fillRect(int x1, int y1, int x2, int y2, int color)
{
   if (minimized) return;
   y1 += glShiftY;
   y2 += glShiftY;
   VertexPosition vertices[] = // position, color
   {
      { XMFLOAT2((float)x1, (float)y1) },
      { XMFLOAT2((float)x2, (float)y1) },
      { XMFLOAT2((float)x2, (float)y2) },
      { XMFLOAT2((float)x1, (float)y2) },
   };

   setProgram(PROGRAM_GC);
   D3D11_MAPPED_SUBRESOURCE ms;
   d3dcontext->Map(pBufferRect, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, vertices, sizeof(vertices));                // copy the data
   d3dcontext->Unmap(pBufferRect, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   d3dcontext->IASetVertexBuffers(0, 1, &pBufferRect, &stride, &offset);
   d3dcontext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
   d3dcontext->IASetIndexBuffer(indexBuffer, DXGI_FORMAT_R16_UINT, 0);
   setColor(color,255);
   d3dcontext->DrawIndexed(6, 0, 0);
}

bool Direct3DBase::isLoadCompleted() 
{
   return loadCompleted == TASKS_COMPLETED;
}

void Direct3DBase::lifeCycle(bool minimizing)
{
   if (minimizing) minimized = true; // will be set to false in the ContentProvider
   postOnMinimizeOrRestore(minimizing);
   if (minimized)
      invalidateTextures();
}

void Direct3DBase::updateScreen()
{
   if (minimized) return;
   d3dcontext->FinishCommandList(FALSE, &d3dCommandList); // 0ms
   updateScreenWaiting = true;
   PhoneDirect3DXamlAppComponent::Direct3DBackground::GetInstance()->RequestNewFrame();
   while (updateScreenWaiting) Sleep(1); // 16ms
   preRender();
}

void Direct3DBase::preRender()
{
   if (minimized) return;
   // Set the rendering viewport to target the entire window.
   CD3D11_VIEWPORT viewport(0.0f, 0.0f, (float)appW, (float)appH);
   d3dcontext->RSSetViewports(1, &viewport);

   d3dcontext->ClearRenderTargetView(renderTexView, clearColor);
   d3dcontext->ClearDepthStencilView(depthStencilView, D3D11_CLEAR_DEPTH, 1.0f, 0);

   d3dcontext->OMSetDepthStencilState(depthDisabledStencilState, 1);
   d3dcontext->OMSetBlendState(pBlendState, 0, 0xffffffff);

   d3dcontext->OMSetRenderTargets(1, &renderTexView, depthStencilView);
   d3dcontext->UpdateSubresource(constantBuffer, 0, NULL, &constantBufferData, 0, 0);
   curProgram = PROGRAM_NONE;
}

void Direct3DBase::setProgram(whichProgram p)
{
   if (p == curProgram || minimized) return;
   lastRGB = 0xFAFFFFFF; // user may never set to this color
   curProgram = p;
   switch (p)
   {
      case PROGRAM_GC:
         d3dcontext->VSSetShader(vertexShader, nullptr, 0);
         d3dcontext->PSSetShader(pixelShader, nullptr, 0);
         d3dcontext->IASetInputLayout(inputLayout);
         break;
      case PROGRAM_LC:
         d3dcontext->VSSetShader(vertexShaderLC, nullptr, 0);
         d3dcontext->PSSetShader(pixelShaderLC, nullptr, 0);
         d3dcontext->IASetInputLayout(inputLayoutLC);
         break;
      case PROGRAM_TEX:
         d3dcontext->PSSetSamplers(0, 1, &texsampler);
         d3dcontext->UpdateSubresource(constantBuffer, 0, nullptr, &constantBufferData, 0, 0);
         d3dcontext->VSSetShader(vertexShaderT, nullptr, 0);
         d3dcontext->PSSetShader(pixelShaderT, nullptr, 0);
         d3dcontext->IASetInputLayout(inputLayoutT);
         d3dcontext->IASetIndexBuffer(indexBuffer, DXGI_FORMAT_R16_UINT, 0);
         d3dcontext->IASetPrimitiveTopology(D3D11_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
         break;
   }
   d3dcontext->VSSetConstantBuffers(0, 1, &constantBuffer);
}

bool Direct3DBase::loadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool onlyAlpha)
{
   if (minimized) return false;
   bool ret = true;
   int32 i;
   PixelConv* pf = (PixelConv*)pixels;
   PixelConv ptemp;
   ID3D11Texture2D *texture;
   D3D11_TEXTURE2D_DESC textureDesc = { 0 };
   textureDesc.Width = width;
   textureDesc.Height = height;
   textureDesc.Format = onlyAlpha ? DXGI_FORMAT_A8_UNORM : DXGI_FORMAT_R8G8B8A8_UNORM;
   textureDesc.MipLevels = textureDesc.ArraySize = textureDesc.SampleDesc.Count = 1;
   textureDesc.BindFlags = D3D11_BIND_SHADER_RESOURCE;

   if (!onlyAlpha)
      for (i = width*height; --i >= 0; pf++) { ptemp.pixel = pf->pixel; pf->a = ptemp.r; pf->b = ptemp.g; pf->g = ptemp.b; pf->r = ptemp.a; }
   D3D11_SUBRESOURCE_DATA textureSubresourceData = { 0 };
   textureSubresourceData.pSysMem = pixels;
   textureSubresourceData.SysMemPitch = textureDesc.Width * (onlyAlpha ? 1 : 4); // Specify the size of a row in bytes
   if (FAILED(d3dDevice->CreateTexture2D(&textureDesc, &textureSubresourceData, &texture)))
   {
      ret = false;
      throwException(currentContext, OutOfMemoryError, "Out of texture memory for image with %dx%d", width, height);
   }
   else
   {
      ID3D11ShaderResourceView* textureView;
      D3D11_SHADER_RESOURCE_VIEW_DESC textureViewDesc;
      ZeroMemory(&textureViewDesc, sizeof(textureViewDesc));
      textureViewDesc.Format = textureDesc.Format;
      textureViewDesc.ViewDimension = D3D11_SRV_DIMENSION_TEXTURE2D;
      textureViewDesc.Texture2D.MipLevels = 1;
      d3dDevice->CreateShaderResourceView(&texture[0], &textureViewDesc, &textureView);
      xmoveptr(&textureId[0], &texture);
      xmoveptr(&textureId[1], &textureView);
   }
   if (!onlyAlpha)
      for (pf = (PixelConv*)pixels, i = width*height; --i >= 0; pf++) { ptemp.pixel = pf->pixel; pf->a = ptemp.r; pf->b = ptemp.g; pf->g = ptemp.b; pf->r = ptemp.a; }
   return ret;
}

void Direct3DBase::deleteTexture(TCObject img, int32* textureId)
{
   if (!textureId) return;
   ID3D11Texture2D *texture;
   ID3D11ShaderResourceView *textureView;
   xmoveptr(&texture, &textureId[0]);
   xmoveptr(&textureView, &textureId[1]);
   if (textureView)
      textureView->Release();
   if (texture)
      texture->Release();
}

int lastTexView;
void Direct3DBase::drawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 dstW, int32 dstH, int32 imgW, int32 imgH, PixelConv* color, int32 alphaMask)
{
   bool isDrawText = color != null;
   if (minimized) return;
   ID3D11ShaderResourceView *textureView;
   xmoveptr(&textureView, &textureId[1]);
   setProgram(PROGRAM_TEX);
   setColor(!color ? 0 : 0xFF000000 | (color->r << 16) | (color->g << 8) | color->b, alphaMask);

   dstY += glShiftY;
   int32 dstY2 = isDrawText ? dstY + dstH : dstY + h;
   int32 dstX2 = isDrawText ? dstX + dstW : dstX + w;

   float left = (float)x / (float)imgW, top = (float)y / (float)imgH, right = (float)(x + w) / (float)imgW, bottom = (float)(y + h) / (float)imgH; // 0,0,1,1

   // VERTEX BUFFER
   TextureVertex vertices[] =
   {  // destination coordinates    source coordinates
      { XMFLOAT2((float)dstX,  (float)dstY),  XMFLOAT2(left, top) },
      { XMFLOAT2((float)dstX2, (float)dstY),  XMFLOAT2(right, top) },
      { XMFLOAT2((float)dstX2, (float)dstY2), XMFLOAT2(right, bottom) },
      { XMFLOAT2((float)dstX,  (float)dstY2), XMFLOAT2(left, bottom) },
   };
   D3D11_MAPPED_SUBRESOURCE ms;
   d3dcontext->Map(texVertexBuffer, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, vertices, sizeof(vertices));                // copy the data
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

// note: on wp8 this will not work because we write everything in a deferred context, and the context is erased once 
// its printed on screen. so, when it arrives here, its already empty.
void Direct3DBase::getPixels(Pixel* dstPixels, int32 srcX, int32 srcY, int32 width, int32 height, int32 pitch)
{
   if (minimized) return;
   ID3D11Texture2D *captureTexture;
   // Copy the renderTarget texture resource to my "captureTexture" resource
   D3D11_TEXTURE2D_DESC desc;
   renderTex->GetDesc(&desc);
   desc.BindFlags = desc.MiscFlags = 0;
   desc.CPUAccessFlags = D3D11_CPU_ACCESS_READ;
   desc.Usage = D3D11_USAGE_STAGING;
   desc.Width = width;
   desc.Height = height;
   HRESULT hr = d3dDevice->CreateTexture2D(&desc, 0, &captureTexture);
   if (FAILED(hr))
      return;
   D3D11_BOX sourceRegion;
   sourceRegion.left = srcX;
   sourceRegion.right = srcX+width;
   sourceRegion.top = srcY;
   sourceRegion.bottom = srcY+height;
   sourceRegion.front = 0;
   sourceRegion.back = 1;
   d3dImedContext->CopySubresourceRegion(captureTexture, 0, 0,0,0, renderTex, 0, &sourceRegion);
   // Map my "captureTexture" resource to access the pixel data
   D3D11_MAPPED_SUBRESOURCE mapped;
   hr = d3dImedContext->Map(captureTexture, 0, D3D11_MAP_READ, 0, &mapped);
   if (FAILED(hr))
      return;

   // Cast the pixel data to a byte array essentially
   struct Color { uint8 r, g, b, a; };
   const Color* psrc = reinterpret_cast<const Color*>(mapped.pData);
   int row = 0,i;
   for (; height-- > 0; srcY++, dstPixels += pitch, row++)
   {
      const Color* c = psrc + row * mapped.RowPitch/4;
      PixelConv* p = (PixelConv*)dstPixels;
      for (i = 0; i < width; i++, c++,p++)
      {
         p->a = 255;
         p->r = c->b;
         p->g = c->g;
         p->b = c->r;
      }
   }         
   d3dImedContext->Unmap(captureTexture, 0);
   captureTexture->Release();
}