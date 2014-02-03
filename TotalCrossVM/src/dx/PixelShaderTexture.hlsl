Texture2D simpleTexture : register(t0);
SamplerState simpleSampler : register(s0);

struct PixelShaderInput
{
    float4 pos : SV_POSITION;
    float2 tex : TEXCOORD0;
};

float4 main(PixelShaderInput input) : SV_TARGET
{
    float4 t = simpleTexture.Sample(simpleSampler, input.tex);
    //if (v_Color.a == 1.0)
    //t.r = 1.0f; t.g = 0.0f; t.b = 0.0f;
    return t;
}
