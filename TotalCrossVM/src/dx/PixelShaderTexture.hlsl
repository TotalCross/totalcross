Texture2D simpleTexture : register(t0);
SamplerState simpleSampler : register(s0);

struct PixelShaderInput
{
    float4 pos : SV_POSITION;
    float4 color : COLOR0;
    float2 tex : TEXCOORD0;
};

float4 main(PixelShaderInput input) : SV_TARGET
{
   float4 t = simpleTexture.Sample(simpleSampler, input.tex);
   if (input.color.a == 1.0) t.rgb = input.color.rgb;
   return t; 
}
