#include "pch.h"
#include "CubeRenderer.h"

using namespace DirectX;
using namespace Microsoft::WRL;
using namespace Windows::Foundation;
using namespace Windows::UI::Core;

CubeRenderer::CubeRenderer() :
m_loadingComplete(false)
{
}

void CubeRenderer::CreateDeviceResources()
{
   Direct3DBase::CreateDeviceResources();

   auto loadVSTask = DX::ReadDataAsync("VertexShaderGlobalColor.cso");
   auto loadPSTask = DX::ReadDataAsync("PixelShaderGlobalColor.cso");

   auto createVSTask = loadVSTask.then([this](Platform::Array<byte>^ fileData) {
      DX::ThrowIfFailed(m_d3dDevice->CreateVertexShader(fileData->Data, fileData->Length, nullptr, &m_vertexShader));

      const D3D11_INPUT_ELEMENT_DESC vertexDesc[] =
      {
         { "POSITION", 0, DXGI_FORMAT_R32G32_FLOAT, 0, 0, D3D11_INPUT_PER_VERTEX_DATA, 0 },
      };

      DX::ThrowIfFailed(m_d3dDevice->CreateInputLayout(vertexDesc, ARRAYSIZE(vertexDesc), fileData->Data, fileData->Length, &m_inputLayout));
   });

   auto createPSTask = loadPSTask.then([this](Platform::Array<byte>^ fileData)
   {
      DX::ThrowIfFailed(m_d3dDevice->CreatePixelShader(fileData->Data, fileData->Length, nullptr, &m_pixelShader));

      CD3D11_BUFFER_DESC constantBufferDesc(sizeof(ModelViewProjectionConstantBuffer), D3D11_BIND_CONSTANT_BUFFER);
      DX::ThrowIfFailed(m_d3dDevice->CreateBuffer(&constantBufferDesc, nullptr, &m_constantBuffer));
   });

   createPSTask.then([this]()
   {
      m_loadingComplete = true;
   });
}

void CubeRenderer::CreateWindowSizeDependentResources()
{
   Direct3DBase::CreateWindowSizeDependentResources();
   XMStoreFloat4x4(&m_constantBufferData.projection, XMMatrixOrthographicOffCenterLH(0, m_windowBounds.Width, m_windowBounds.Height, 0, -1.0f, 1.0f));
}

void CubeRenderer::setup()
{
   count = 0;
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

   createTexture();

   const float white[] = { 1.0f, 1.0f, 1.0f, 1.000f };
   m_d3dContext->ClearRenderTargetView(m_renderTargetView.Get(), white);
   m_d3dContext->ClearDepthStencilView(m_depthStencilView.Get(), D3D11_CLEAR_DEPTH, 1.0f, 0);
   if (!m_loadingComplete) // Only draw the cube once it is loaded (loading is asynchronous).
      return;
   m_d3dContext->OMSetRenderTargets(1, m_renderTargetView.GetAddressOf(), m_depthStencilView.Get());
   m_d3dContext->UpdateSubresource(m_constantBuffer.Get(), 0, NULL, &m_constantBufferData, 0, 0);
   m_d3dContext->IASetInputLayout(m_inputLayout.Get());
   m_d3dContext->VSSetShader(m_vertexShader.Get(), nullptr, 0);
   m_d3dContext->VSSetConstantBuffers(0, 1, m_constantBuffer.GetAddressOf());
   m_d3dContext->PSSetShader(m_pixelShader.Get(), nullptr, 0);
}

void CubeRenderer::setColor(int color)
{
   lastRGB = color;
   aa = ((color >> 24) & 0xFF) / 255.0f;
   rr = ((color >> 16) & 0xFF) / 255.0f;
   gg = ((color >> 8) & 0xFF) / 255.0f;
   bb = (color & 0xFF) / 255.0f;

   VertexColor vcolor;
   vcolor.color = XMFLOAT4(rr, gg, bb, aa); // last is alpha

   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContext->Map(pBufferColor, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, &vcolor, sizeof(VertexColor));                // copy the data
   m_d3dContext->Unmap(pBufferColor, NULL);                                     // unmap the buffer

   m_d3dContext->VSSetConstantBuffers(1, 1, &pBufferColor);
}

void CubeRenderer::createTexture()
{
   D3D11_TEXTURE2D_DESC sTexDesc;
   sTexDesc.Width = 100;
   sTexDesc.Height = 100;
   sTexDesc.MipLevels = 1;
   sTexDesc.ArraySize = 1;
   sTexDesc.Format = DXGI_FORMAT_R8G8B8A8_UNORM;
   sTexDesc.SampleDesc.Count = 1;
   sTexDesc.SampleDesc.Quality = 0;
   sTexDesc.Usage = D3D11_USAGE_IMMUTABLE;
   sTexDesc.BindFlags = D3D11_BIND_SHADER_RESOURCE;
   sTexDesc.CPUAccessFlags = 0;
   sTexDesc.MiscFlags = 0;

   int *pixels = new int[sTexDesc.Width * sTexDesc.Height];
   for (int i = sTexDesc.Width * sTexDesc.Height; --i >= 0;)
      pixels[i] = 0xFFFF0000;

   D3D11_SUBRESOURCE_DATA sSubData;
   sSubData.pSysMem = pixels;
   sSubData.SysMemPitch = (UINT)(4 * sTexDesc.Width);
   sSubData.SysMemSlicePitch = 0;

   DX::ThrowIfFailed(m_d3dDevice->CreateTexture2D(&sTexDesc, &sSubData, &pTexture));
   delete pixels;
}

void CubeRenderer::drawLine(int x1, int y1, int x2, int y2, int color)
{
   VertexPosition cubeVertices[] = // position, color
   {
      { XMFLOAT2((float)x1, (float)y1) },
      { XMFLOAT2((float)x2, (float)y2) },
   };

   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContext->Map(pBufferRect, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   m_d3dContext->Unmap(pBufferRect, NULL);                                     // unmap the buffer


   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   m_d3dContext->IASetVertexBuffers(0, 1, &pBufferRect, &stride, &offset);
   m_d3dContext->IASetIndexBuffer(m_indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   m_d3dContext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST);
   m_d3dContext->IASetIndexBuffer(m_indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   if (color != lastRGB) setColor(color);
   m_d3dContext->DrawIndexed(2, 0, 0);
}
void CubeRenderer::fillRect(int x1, int y1, int x2, int y2, int color)
{
   VertexPosition cubeVertices[] = // position, color
   {
      { XMFLOAT2((float)x1, (float)y1) },
      { XMFLOAT2((float)x2, (float)y1) },
      { XMFLOAT2((float)x2, (float)y2) },
      { XMFLOAT2((float)x1, (float)y2) },
   };

   D3D11_MAPPED_SUBRESOURCE ms;
   m_d3dContext->Map(pBufferRect, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(cubeVertices));                // copy the data
   m_d3dContext->Unmap(pBufferRect, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   m_d3dContext->IASetVertexBuffers(0, 1, &pBufferRect, &stride, &offset);
   m_d3dContext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_TRIANGLELIST);
   m_d3dContext->IASetIndexBuffer(m_indexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   if (color != lastRGB) setColor(color);
   m_d3dContext->DrawIndexed(6, 0, 0);
}
void CubeRenderer::drawPixels(int *x, int *y, int count, int color)
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
   m_d3dContext->Map(pBufferPixels, NULL, D3D11_MAP_WRITE_DISCARD, NULL, &ms);   // map the buffer
   memcpy(ms.pData, cubeVertices, sizeof(VertexPosition)* n);                // copy the data
   m_d3dContext->Unmap(pBufferPixels, NULL);                                     // unmap the buffer

   UINT stride = sizeof(VertexPosition);
   UINT offset = 0;
   m_d3dContext->IASetVertexBuffers(0, 1, &pBufferPixels, &stride, &offset);
   m_d3dContext->IASetIndexBuffer(pixelsIndexBuffer.Get(), DXGI_FORMAT_R16_UINT, 0);
   m_d3dContext->IASetPrimitiveTopology(D3D10_PRIMITIVE_TOPOLOGY_LINELIST);
   if (color != lastRGB) setColor(color);
   m_d3dContext->DrawIndexed(n, 0, 0);
   delete cubeVertices;
}

void CubeRenderer::Render()
{
   const float white[] = { 1.0f, 1.0f, 1.0f, 1.000f };

   //////////////////////////////////////////////////////////////////////////////////////////
   int ini = GetTickCount64() & 0x3FFFFFFF;

   //if (count++ < 100)
   {
      //m_d3dContext->ClearRenderTargetView(m_renderTargetView.Get(), white);
      m_d3dContext->ClearDepthStencilView(m_depthStencilView.Get(), D3D11_CLEAR_DEPTH, 1.0f, 0);
      if (!m_loadingComplete) // Only draw the cube once it is loaded (loading is asynchronous).
         return;
      m_d3dContext->OMSetRenderTargets(1, m_renderTargetView.GetAddressOf(), m_depthStencilView.Get());
      m_d3dContext->UpdateSubresource(m_constantBuffer.Get(), 0, NULL, &m_constantBufferData, 0, 0);
      m_d3dContext->IASetInputLayout(m_inputLayout.Get());
      m_d3dContext->VSSetShader(m_vertexShader.Get(), nullptr, 0);
      m_d3dContext->VSSetConstantBuffers(0, 1, m_constantBuffer.GetAddressOf());
      m_d3dContext->PSSetShader(m_pixelShader.Get(), nullptr, 0);
   }

#if 0
   if (count++ < 200) {
      int w = (int)m_windowBounds.Width, h = (int)m_windowBounds.Height;
      for (int i = 0; i < h / 2; i++)
         drawLine(0, i, w, i, 0xFF00FF00);
      int *xx = new int[w], *yy = new int[w];
      for (int i = h / 2; i <= h; i++)
      {
         for (int j = 0; j <= w; j++)
         {
            xx[j] = j;
            yy[j] = i;
         }
         drawPixels(xx, yy, w, ((i * 255 / h) << 24) | 0xFF0000);
      }

      fillRect(w / 2 - w / 8, h / 2 - h / 8, w / 2 + w / 8, h / 2 + h / 8, 0xFF0000FF);
   }
#endif


   int fim = GetTickCount64() & 0x3FFFFFFF;
   char buf[50];
   sprintf_s(buf, "elapsed: %d ms\n", fim - ini);
   OutputDebugStringA(buf);


   /*   drawLine(10, 10, 200, 10, 0xFF0000);
   drawLine(200, 10, 200, 200, 0x00FFFF);
   drawLine(200, 200, 10, 200, 0x00FF00);
   drawLine(10, 200, 10, 10, 0x0000FF);*/
}