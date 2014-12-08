#pragma once

#include "DirectXHelper.h"
#include "cswrapper.h"
#include <DrawingSurfaceNative.h>

#define HAS_TCHAR
#include "tcvm.h"

#define TASKS_COMPLETED ((1 << 6)-1) // 6 tasks

struct ProjectionConstantBuffer
{
   DirectX::XMFLOAT4X4 projection;
};

struct VertexPosition
{
   DirectX::XMFLOAT2 pos;
};

struct VertexColor
{
   DirectX::XMFLOAT4 color;
   DirectX::XMFLOAT4 alphaMask;
};

struct VertexPositionColor
{
   DirectX::XMFLOAT2 pos;
   DirectX::XMFLOAT4 color;
};

struct TextureVertex
{
   DirectX::XMFLOAT2 pos;  // position
   DirectX::XMFLOAT2 tex;  // texture coordinate
};

enum whichProgram
{
   PROGRAM_NONE,
   PROGRAM_GC,  // global color
   PROGRAM_TEX, // texture
   PROGRAM_LC,  // local color
};

// Helper class that initializes DirectX APIs
ref class Direct3DBase 
{
internal:
   Direct3DBase(PhoneDirect3DXamlAppComponent::CSwrapper ^_cs);
   static Direct3DBase ^getLastInstance();

	void updateScreen();
   void updateDevice(IDrawingSurfaceRuntimeHostNative* host);
   void updateScreenMatrix();
   void setProgram(whichProgram p);
   void deleteTexture(TCObject img, int32* textureId);
   void loadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool onlyAlpha);
   void drawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 dstW, int32 dstH, int32 imgW, int32 imgH, PixelConv* color, int32 alphaMask);
   void drawLines(Context currentContext, TCObject g, int32* x, int32* y, int32 n, int32 tx, int32 ty, int color, bool fill);
   void drawPixelColors(int32* x, int32* y, PixelConv* colors, int32 count);
   void drawPixels(float *glXYA, int count, int color);
   void drawLine(int x1, int y1, int x2, int y2, int color);
   void fillRect(int x1, int y1, int x2, int y2, int color);
   void fillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz);
   void setColor(int color, int alphaMask);
   void createTexture();
   void getPixels(Pixel* dstPixels, int32 srcX, int32 srcY, int32 width, int32 height, int32 pitch);
   bool isLoadCompleted();
   void lifeCycle(bool suspending);
   bool checkPixelsBuf(int32 n);

   ID3D11DeviceContext *d3dcontext, *d3dImedContext;
   ID3D11CommandList *d3dCommandList;
   PhoneDirect3DXamlAppComponent::CSwrapper ^csharp;
   bool updateScreenWaiting;
   int sipHeight;
   bool minimized;
   Microsoft::WRL::ComPtr<IDrawingSurfaceSynchronizedTextureNative> syncTex;
   void preRender(); // resets the screen and set it ready to render

private:
   void initialize();
   void setBufAndLen(Platform::Array<byte>^ fileData, byte** buf, int* len, int completeValue);
   int loadCompleted;
   whichProgram curProgram;
   int lastRGB;
   float aa, rr, gg, bb;
   int lastPixelsCount,lastLinesCount;
   float clearColor[4]; // all 0
	Context localContext;
	bool vmStarted;
   byte *vs1buf, *ps1buf, *vs2buf, *ps2buf, *vs3buf, *ps3buf;
   int vs1len, ps1len, vs2len, ps2len, vs3len, ps3len;

   // screen textures
   ID3D11Texture2D *renderTex;
   ID3D11RenderTargetView *renderTexView;

   D3D_FEATURE_LEVEL m_featureLevel;
   ID3D11Buffer *pBufferRect, *pBufferPixels, *pBufferLines, *pBufferColor, *texVertexBuffer, *pBufferRectLC;
	ID3D11DepthStencilView* depthStencilView;
   ID3D11Texture2D *depthStencil;
   ID3D11SamplerState *texsampler;
   ID3D11DepthStencilState *depthDisabledStencilState;
   ID3D11BlendState *pBlendState;
   ID3D11InputLayout *inputLayout, *inputLayoutT, *inputLayoutLC;
   ID3D11Buffer *indexBuffer, *pixelsIndexBuffer, *linesIndexBuffer;
   ID3D11VertexShader *vertexShader, *vertexShaderT, *vertexShaderLC;
   ID3D11PixelShader *pixelShader, *pixelShaderT, *pixelShaderLC;
   ID3D11Buffer *constantBuffer;
   ProjectionConstantBuffer constantBufferData;
	ID3D11Device* d3dDevice;
};
