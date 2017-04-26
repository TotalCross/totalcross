#pragma pack_matrix( row_major )

cbuffer ProjectionConstantBuffer : register(b0)
{
    matrix projection;
};

cbuffer ColorConstantBuffer : register(b1) // 16 bytes minimum or it will crash
{
   float4 color : COLOR0;
   float4 alphaMask : COLOR1;
};

struct VertexShaderInput
{
    float2 pos : POSITION;
    float2 tex : TEXCOORD0;
};

struct PixelShaderInput
{
    float4 pos : SV_POSITION;
    float4 color : COLOR0;
    float2 tex : TEXCOORD0;
    float4 alphaMask : COLOR1;
};

PixelShaderInput main(VertexShaderInput input)
{
    PixelShaderInput output;
    float4 pos = float4(input.pos, 0.0f, 1.0f);
    output.pos = mul(pos, projection);
    output.tex = input.tex;
    output.color = color;
    output.alphaMask = alphaMask;
    return output;
}
