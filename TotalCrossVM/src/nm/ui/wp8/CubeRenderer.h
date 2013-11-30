#pragma once
#include <wrl/client.h>

#include "esUtil.h"

#if (_MSC_VER >= 1800)
#include <d3d11_2.h>
#else
#include <d3d11_1.h>
#endif

#include <DirectXMath.h>
#include <memory>
#include <agile.h>



using namespace DirectX;

// This class renders a simple spinning cube.
class CubeRenderer
{
public:
	//CubeRenderer();

    void CreateResources();
    void UpdateForWindowSizeChanged();
#if WINAPI_FAMILY_PARTITION(WINAPI_PARTITION_PHONE)
	void OnOrientationChanged();
#endif

	void Render();
	
	// Method for updating time-dependent objects.
	void Update(float timeTotal, float timeDelta);

private:
	Windows::Foundation::Rect m_windowBounds;
	Windows::Graphics::Display::DisplayOrientations m_orientation;
    GLuint m_colorProgram;
    GLint a_positionColor;
    GLint a_colorColor;
    GLint u_mvpColor;
    XMMATRIX m_projectionMatrix;
    XMMATRIX m_viewMatrix;
    XMMATRIX m_modelMatrix;
};
