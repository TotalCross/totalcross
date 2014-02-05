#pragma pack_matrix( row_major )

cbuffer ProjectionConstantBuffer : register(b0)
{
    matrix projection;
};

struct VertexShaderInput
{
    float2 pos : POSITION;
    float2 tex : TEXCOORD0;
};

struct PixelShaderInput
{
    float4 pos : SV_POSITION;
    float2 tex : TEXCOORD0;
};

PixelShaderInput main(VertexShaderInput input)
{
    PixelShaderInput output;
    float4 pos = float4(input.pos, 0.0f, 1.0f);
    output.pos = mul(pos, projection);
    output.tex = input.tex;
    return output;
}
