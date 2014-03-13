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

typedef enum
{
   D3DCMD_NONE,
   D3DCMD_FILLSHADEDRECT,
   D3DCMD_FILLRECT,
   D3DCMD_DRAWLINE,
   D3DCMD_DRAWTEXTURE,
   D3DCMD_DRAWPIXELS,
} D3DCMD;

typedef struct
{
   int horiz : 1;
   int hasColor : 1;
   int hasClip : 1;
} CmdFlags;

typedef struct TD3DCommand TD3DCommand;
typedef TD3DCommand* D3DCommand;

struct TD3DCommand
{
   D3DCMD cmd;
   union
   {
      struct
      {
         int32 a, b, c, d, e, f, g, h;
      };
      struct
      {
         float *coords, *colors, fcolor, xy[2];
      };
   };
   PixelConv c1, c2;
   int32 clip[4];
   int32 textureId[2];
   CmdFlags flags;
   struct TD3DCommand* next;
};

// Helper class that initializes DirectX APIs
ref class Direct3DBase 
{
internal:
   Direct3DBase(PhoneDirect3DXamlAppComponent::CSwrapper ^_cs);
   static Direct3DBase ^getLastInstance();

	void initialize(bool resuming);
   void updateDevice(IDrawingSurfaceRuntimeHostNative* host);
	void preRender(); // resets the screen and set it ready to render
	void updateScreen();

   void setProgram(whichProgram p);
   void deleteTexture(TCObject img, int32* textureId, bool updateList);
   void loadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool updateList);
   void drawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 imgW, int32 imgH, PixelConv *color, int32* clip);
   void drawLine(int x1, int y1, int x2, int y2, int color);
   void fillRect(int x1, int y1, int x2, int y2, int color);
   void fillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz);
   void drawPixels(float *glcoords, float *glcolors, int count, int color);
   void setClip(int32* clip);
   void setColor(int color);
   void createTexture();
   bool isLoadCompleted();
   void lifeCycle(bool suspending);

   ID3D11DeviceContext *d3dcontext, *d3dImedContext;
   ID3D11CommandList *d3dCommandList;
   PhoneDirect3DXamlAppComponent::CSwrapper ^csharp;
   Platform::String^ alertMsg;
   bool updateScreenWaiting;
   int rotatedTo;
   int sipHeight;
   bool updateWS;
   bool minimized;
   Microsoft::WRL::ComPtr<IDrawingSurfaceSynchronizedTextureNative> syncTex;

private:
   int loadCompleted;
   whichProgram curProgram;
   int lastRGB;
   float aa, rr, gg, bb;
   int lastPixelsCount;
   bool clipSet;
   D3D11_RECT clipRect;
   float clearColor[4]; // all 0
	Context localContext;
	bool vmStarted;

   // screen textures
   ID3D11Texture2D *renderTex;
   ID3D11RenderTargetView *renderTexView;

   D3D_FEATURE_LEVEL m_featureLevel;
   ID3D11Buffer *pBufferRect, *pBufferPixels, *pBufferColor, *texVertexBuffer, *pBufferRectLC;
	ID3D11DepthStencilView* depthStencilView;
   ID3D11Texture2D *depthStencil;
   ID3D11SamplerState *texsampler;
   ID3D11DepthStencilState *depthDisabledStencilState;
   ID3D11BlendState *pBlendState;
   ID3D11InputLayout *inputLayout, *inputLayoutT, *inputLayoutLC;
   ID3D11Buffer *indexBuffer, *pixelsIndexBuffer;
   ID3D11VertexShader *vertexShader, *vertexShaderT, *vertexShaderLC;
   ID3D11PixelShader *pixelShader, *pixelShaderT, *pixelShaderLC;
   ID3D11Buffer *constantBuffer;
   ProjectionConstantBuffer constantBufferData;
   ID3D11RasterizerState *pRasterStateEnableClipping, *pRasterStateDisableClipping;
	ID3D11Device* d3dDevice;
};
