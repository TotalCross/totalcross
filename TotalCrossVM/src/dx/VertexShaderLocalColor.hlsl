#pragma pack_matrix( row_major )

cbuffer ProjectionConstantBuffer : register(b0)
{
   matrix projection;
};

struct VertexShaderInput
{
   float2 pos : POSITION;
   float4 color : COLOR0;
};

struct PixelShaderInput
{
   float4 pos : SV_POSITION;
   float4 color : COLOR0;
};

PixelShaderInput main(VertexShaderInput input)
{
   PixelShaderInput output;
   float4 pos = float4(input.pos, 0.0f, 1.0f);
   output.pos = mul(pos, projection);
   output.color = input.color;
   return output;
}
