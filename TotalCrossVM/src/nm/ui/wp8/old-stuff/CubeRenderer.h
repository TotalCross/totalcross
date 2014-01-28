#pragma once

#include "Direct3DBase.h"

struct ModelViewProjectionConstantBuffer
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

// This class renders a simple spinning cube.
ref class CubeRenderer sealed : public Direct3DBase
{
public:
   CubeRenderer();

   // Direct3DBase methods.
   virtual void CreateDeviceResources() override;
   virtual void CreateWindowSizeDependentResources() override;
   virtual void Render() override;
   void drawLine(int x1, int y1, int x2, int y2, int color);
   void drawPixels(int *x, int *y, int count, int color);
   void fillRect(int x1, int y1, int x2, int y2, int color);
   void setColor(int color);
   void createTexture();

   // Method for updating time-dependent objects.
   void setup();

private:
   int count;
   bool m_loadingComplete;
   int lastRGB;
   float aa,rr, gg, bb;
   ID3D11Buffer *pBufferRect, *pBufferPixels, *pBufferColor;
   int lastPixelsCount;
   VertexPosition *pixelsVertices;
   ID3D11Texture2D* pTexture;

   Microsoft::WRL::ComPtr<ID3D11InputLayout> m_inputLayout;
   Microsoft::WRL::ComPtr<ID3D11Buffer> m_vertexBuffer;
   Microsoft::WRL::ComPtr<ID3D11Buffer> m_indexBuffer, pixelsIndexBuffer, colorBuffer;
   Microsoft::WRL::ComPtr<ID3D11VertexShader> m_vertexShader;
   Microsoft::WRL::ComPtr<ID3D11PixelShader> m_pixelShader;
   Microsoft::WRL::ComPtr<ID3D11Buffer> m_constantBuffer;

   ModelViewProjectionConstantBuffer m_constantBufferData;
};