#define HAS_TCHAR

#include <wrl/client.h>

#if (_MSC_VER >= 1800)
#include <d3d11_2.h>
#else
#include <d3d11_1.h>
#endif

#include "openglWrapper.h"
#include "datastructures.h"
#include "tcvm.h"
#include "MainView.h"
#include "precompiled_texture.h"
#include "precompiled_lrp.h"
#include "precompiled_points.h"
#include "precompiled_shade.h"

#define TOGGLE_BUFFER

#define MAKE_RGBA(rgb, a) MAKE_RGBA_123_321( (rgb), (a & 0xFF))
//                                   1st byte               2nd byte                      3rd byte
#define MAKE_RGBA_123_123(bgr, a) (((bgr & 0xFF) <<  0) | ((bgr & 0xFF00) >> 8) <<  8 | ((bgr & 0xFF0000) >> 16) << 16 | (a) << 24)
#define MAKE_RGBA_123_132(bgr, a) (((bgr & 0xFF) <<  0) | ((bgr & 0xFF00) >> 8) << 16 | ((bgr & 0xFF0000) >> 16) <<  8 | (a) << 24)
#define MAKE_RGBA_123_213(bgr, a) (((bgr & 0xFF) <<  8) | ((bgr & 0xFF00) >> 8) <<  0 | ((bgr & 0xFF0000) >> 16) << 16 | (a) << 24)
#define MAKE_RGBA_123_231(bgr, a) (((bgr & 0xFF) << 16) | ((bgr & 0xFF00) >> 8) <<  0 | ((bgr & 0xFF0000) >> 16) <<  8 | (a) << 24)
#define MAKE_RGBA_123_312(bgr, a) (((bgr & 0xFF) <<  8) | ((bgr & 0xFF00) >> 8) << 16 | ((bgr & 0xFF0000) >> 16) <<  0 | (a) << 24)
#define MAKE_RGBA_123_321(bgr, a) (((bgr & 0xFF) << 16) | ((bgr & 0xFF00) >> 8) <<  8 | ((bgr & 0xFF0000) >> 16) <<  0 | (a) << 24)

using namespace Windows::UI::Core;
using namespace TotalCross;

TC_API void throwException(Context currentContext, Throwable t, CharP message, ...);

#define GL_CHECK_ERROR checkGlError(__FILE__,__LINE__);
#define stringfy(x) #x

#pragma region StaticVariables

//static EGLNativeWindowType *window, *lastWindow;
//
//static ESContext m_esContext;
//static Microsoft::WRL::ComPtr<IWinrtEglWindow> m_eglWindow;
static TCGint lastProg = -1;

// http://www.songho.ca/opengl/gl_projectionmatrix.html
//////////// texture
static char TEXTURE_VERTEX_CODE[] = stringfy(
attribute vec4 vertexPoint;
attribute vec2 aTextureCoord;
uniform mat4 projectionMatrix;
varying vec2 vTextureCoord;
void main()
{
    gl_Position = vertexPoint * projectionMatrix;
    vTextureCoord = aTextureCoord;
});
	

static char TEXTURE_FRAGMENT_CODE[] = stringfy(
precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;
void main()
{
	gl_FragColor = texture2D(sTexture, vTextureCoord);
});

static TCGuint textureProgram;
static TCGuint texturePoint;
static TCGuint textureCoord, textureS;

//////////// points (text)

static char POINTS_VERTEX_CODE[] = stringfy(
attribute vec4 a_Position; uniform vec4 a_Color; varying vec4 v_Color; attribute float alpha;
uniform mat4 projectionMatrix; 
void main()
{
	gl_PointSize = 1.0; v_Color = vec4(a_Color.x,a_Color.y,a_Color.z,alpha); gl_Position = a_Position * projectionMatrix;
});

static char POINTS_FRAGMENT_CODE[] = stringfy(
precision mediump float;
varying vec4 v_Color;
void main()
{
	gl_FragColor = v_Color;
});

static TCGuint pointsProgram;
static TCGuint pointsPosition;
static TCGuint pointsColor;
static TCGuint pointsAlpha;

///////////// line, rect, point

static char LRP_VERTEX_CODE[] = stringfy(
attribute vec4 a_Position;
uniform mat4 projectionMatrix;
void main()
{
	gl_PointSize = 1.0; gl_Position = a_Position*projectionMatrix;
});

static char LRP_FRAGMENT_CODE[] = stringfy(
precision mediump float;
uniform vec4 a_Color;
void main()
{
	gl_FragColor = a_Color;
});

static TCGuint lrpProgram;
static TCGuint lrpPosition;
static TCGuint lrpColor;
static TCGubyte rectOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

///////////// shaded rect

static char SHADE_VERTEX_CODE[] = stringfy(
attribute vec4 a_Position; attribute vec4 a_Color; varying vec4 v_Color;
uniform mat4 projectionMatrix;
void main()
{
	gl_PointSize = 1.0; v_Color = a_Color; gl_Position = a_Position*projectionMatrix;
});

static char SHADE_FRAGMENT_CODE[] = stringfy(
precision mediump float;
varying vec4 v_Color;
void main()
{
	gl_FragColor = v_Color;
});

static TCGuint shadeProgram;
static TCGuint shadePosition;
static TCGuint shadeColor;

//static EGLDisplay _display;
//static EGLSurface _surface;
//static EGLContext _context;

static TCGfloat texcoords[16], lrcoords[8], shcolors[24], shcoords[8];
static int32 *pixcoords, *pixcolors, *pixEnd;

static int pixLastRGB = -1;
static bool surfaceWillChange;

//extern int32 lastW, lastH, ascrHRes, ascrVRes;
//extern int32 *shiftScreenColorP;

static int32 desiredglShiftY;

#pragma endregion

#pragma region NonStaticVars

bool setShiftYonNextUpdateScreen;

VoidPs* imgTextures;
int32 realAppH, appW, appH, glShiftY;
TCGfloat ftransp[16], f255[256];
int32 flen;
TCGfloat* glcoords;//[flen*2]; x,y
TCGfloat* glcolors;//[flen];   alpha

#pragma endregion



int32 abs32(int32 a)
{
	return a < 0 ? -a : a;
}

#pragma region static functions

static bool checkGlError(const char* op, int line)
{
#ifndef USE_DX
	TCGint error;

	//debug("%s (%d)",op,line);

	if (!op)
		return glGetError() != 0;
	else
	for (error = glGetError(); error; error = glGetError())
	{
		char* msg = "???";
		switch (error)
		{
		case GL_INVALID_ENUM: msg = "INVALID ENUM"; break;
		case GL_INVALID_VALUE: msg = "INVALID VALUE"; break;
		case GL_INVALID_OPERATION: msg = "INVALID OPERATION"; break;
		case GL_OUT_OF_MEMORY: msg = "OUT OF MEMORY"; break;
		}
		debug("glError %s at %s (%d)\n", msg, op, line);
		return true;
	}
#endif
	return false;
}

static TCGuint createProgram_angle(unsigned char* precompiledCode, size_t precompiledCodeSize)
{
#ifndef USE_DX
	TCGint ret = GL_TRUE;
	TCGuint vshader, fshader;
	TCGuint p = glCreateProgram();

	//vshader = loadShader(GL_VERTEX_SHADER, vertexCode);
	//fshader = loadShader(GL_FRAGMENT_SHADER, fragmentCode);
	//glAttachShader(p, vshader); GL_CHECK_ERROR
	//glAttachShader(p, fshader); GL_CHECK_ERROR
	//glLinkProgram(p); GL_CHECK_ERROR
	////glValidateProgram(p);
	//glGetProgramiv(p, GL_LINK_STATUS, &ret); GL_CHECK_ERROR
	//if (ret == GL_FALSE)
	//{
	//	TCGchar *buffer;
	//	glGetProgramiv(p, GL_INFO_LOG_LENGTH, &ret); GL_CHECK_ERROR
	//	buffer = (TCGchar*)xmalloc(sizeof(TCGchar)* ret);
	//	glGetProgramInfoLog(p, ret, &ret, buffer); GL_CHECK_ERROR
	//		debug("Link error: %s", buffer);
	//	xfree(buffer);
	//}

	glProgramBinaryOES(p, GL_PROGRAM_BINARY_ANGLE, precompiledCode, precompiledCodeSize);
	glGetProgramiv(p, GL_LINK_STATUS, &ret); GL_CHECK_ERROR
	if (ret == GL_FALSE)
	{
		TCGchar *buffer;
		glGetProgramiv(p, GL_INFO_LOG_LENGTH, &ret); GL_CHECK_ERROR
			buffer = (TCGchar*)xmalloc(sizeof(TCGchar)* ret);
		glGetProgramInfoLog(p, ret, &ret, buffer); GL_CHECK_ERROR
			debug("Link error: %s", buffer);
		xfree(buffer);
	}

	return p;
#endif
   return 0;
}

static void setCurrentProgram(TCGint prog)
{
#ifndef USE_DX
	if (prog != lastProg)
	{
		glUseProgram(lastProg = prog); GL_CHECK_ERROR
	}
#endif
}

static void initPoints()
{
#ifndef USE_DX
	pointsProgram = createProgram_angle(precompiled_points, sizeof(precompiled_points));
	setCurrentProgram(lrpProgram);
	pointsColor = glGetUniformLocation(pointsProgram, "a_Color"); GL_CHECK_ERROR
		pointsAlpha = glGetAttribLocation(pointsProgram, "alpha"); GL_CHECK_ERROR
		pointsPosition = glGetAttribLocation(pointsProgram, "a_Position"); GL_CHECK_ERROR // get handle to vertex shader's vPosition member
		glEnableVertexAttribArray(pointsAlpha); GL_CHECK_ERROR // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
		glEnableVertexAttribArray(pointsPosition); GL_CHECK_ERROR // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
#endif
}

static void clearPixels()
{
#ifndef USE_DX
	pixcoords = (int32*)glcoords;
	pixcolors = (int32*)glcolors;
#endif
}


static void destroyEGL()
{
#ifndef USE_DX
	eglMakeCurrent(_display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
	eglDestroyContext(_display, _context);
	eglDestroySurface(_display, _surface);
	eglTerminate(_display);

	_display = EGL_NO_DISPLAY;
	_surface = EGL_NO_SURFACE;
	_context = EGL_NO_CONTEXT;
#endif
}

static void add2pipe(int32 x, int32 y, int32 w, int32 h, int32 rgb, int32 a)
{
#ifndef USE_DX
	bool isPixel = (x & IS_PIXEL) != 0;
	if ((pixcoords + (isPixel ? 2 : 4)) > pixEnd)
		flushPixels(7);
	*pixcoords++ = x;
	*pixcoords++ = y;
	if (!isPixel)
	{
		*pixcoords++ = w;
		*pixcoords++ = h;
	}
	PixelConv pc;
	pc.pixel = rgb;
	pc.a = a;
	*pixcolors++ = pc.pixel;
#endif
}

static void initShade()
{
#ifndef USE_DX
	shadeProgram = createProgram_angle(precompiled_shade, sizeof(precompiled_shade));
	setCurrentProgram(shadeProgram);
	shadeColor = glGetAttribLocation(shadeProgram, "a_Color"); GL_CHECK_ERROR
		shadePosition = glGetAttribLocation(shadeProgram, "a_Position"); GL_CHECK_ERROR // get handle to vertex shader's vPosition member
		glEnableVertexAttribArray(shadeColor); GL_CHECK_ERROR // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
		glEnableVertexAttribArray(shadePosition); GL_CHECK_ERROR // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
		shcolors[3] = shcolors[7] = shcolors[11] = shcolors[15] = shcolors[19] = shcolors[23] = 1; // note: last 2 colors are not used by opengl
#endif
}

static void setProjectionMatrix(TCGfloat w, TCGfloat h)
{
#ifndef USE_DX
	mat4 mat =
	{
		2.0 / w, 0.0, 0.0, -1.0,
		0.0, -2.0 / h, 0.0, 1.0,
		0.0, 0.0, -1.0, 0.0,
		0.0, 0.0, 0.0, 1.0
	};
	setCurrentProgram(textureProgram); glUniformMatrix4fv(glGetUniformLocation(textureProgram, "projectionMatrix"), 1, 0, mat); GL_CHECK_ERROR
		setCurrentProgram(lrpProgram);     glUniformMatrix4fv(glGetUniformLocation(lrpProgram, "projectionMatrix"), 1, 0, mat); GL_CHECK_ERROR
		setCurrentProgram(pointsProgram);  glUniformMatrix4fv(glGetUniformLocation(pointsProgram, "projectionMatrix"), 1, 0, mat); GL_CHECK_ERROR
		setCurrentProgram(shadeProgram);   glUniformMatrix4fv(glGetUniformLocation(shadeProgram, "projectionMatrix"), 1, 0, mat); GL_CHECK_ERROR
#ifdef darwin
		int fw, fh;
	glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_WIDTH, &fw);
	glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_HEIGHT, &fh);
	glViewport(0, 0, fw, fh); GL_CHECK_ERROR
#else
		glViewport(0, 0, w, h); GL_CHECK_ERROR
#endif
#endif
}

static void initTexture()
{
#ifndef USE_DX
	//textureProgram = esLoadProgram(TEXTURE_VERTEX_CODE, TEXTURE_FRAGMENT_CODE);
	//textureProgram = createProgram_angle(gProgram, sizeof(gProgram));
	textureProgram = createProgram_angle(precompiled_texture, sizeof(precompiled_texture));
	setCurrentProgram(textureProgram);
	textureS = glGetUniformLocation(textureProgram, "sTexture"); GL_CHECK_ERROR
		texturePoint = glGetAttribLocation(textureProgram, "vertexPoint"); GL_CHECK_ERROR
		textureCoord = glGetAttribLocation(textureProgram, "aTextureCoord"); GL_CHECK_ERROR

		glEnableVertexAttribArray(textureCoord); GL_CHECK_ERROR
		glEnableVertexAttribArray(texturePoint); GL_CHECK_ERROR
#endif
}

static void initLineRectPoint()
{
#ifndef USE_DX
	lrpProgram = createProgram_angle(precompiled_lrp, sizeof(precompiled_lrp));
	setCurrentProgram(lrpProgram);
	lrpColor = glGetUniformLocation(lrpProgram, "a_Color"); GL_CHECK_ERROR
		lrpPosition = glGetAttribLocation(lrpProgram, "a_Position"); GL_CHECK_ERROR
		glEnableVertexAttribArray(lrpPosition); GL_CHECK_ERROR
#endif
}


#pragma endregion

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
#ifndef ANDROID
	screen->extension = deviceCtx;
#endif
	screen->pitch = screen->screenW * screen->bpp / 8;
	screen->pixels = (uint8*)1;
	return screen->pixels != null;
}

void flushPixels(int q)
{
#ifndef USE_DX
	if (pixcolors != (int32*)glcolors)
	{
		int32 n = pixcolors - (int32*)glcolors, i;
		PixelConv pc;
		TCGfloat* coords = lrcoords;
		setCurrentProgram(lrpProgram);
		pixcoords = (int32*)glcoords;
		pixcolors = (int32*)glcolors;
		glVertexAttribPointer(lrpPosition, 2, GL_FLOAT, GL_FALSE, 0, coords); GL_CHECK_ERROR
		int32 lastRGBA = ~*pixcolors;
		int32 x, y, w, h, x2, y2;
		int32 ty = glShiftY;
		for (i = 0; i < n; i++)
		{
			// color
			int32 rgba = *pixcolors++;
			if (lastRGBA != rgba) // prevent color change = performance x2 in galaxy tab2
			{
				pc.pixel = lastRGBA = rgba;
				glUniform4f(lrpColor, f255[pc.r], f255[pc.g], f255[pc.b], f255[pc.a]); GL_CHECK_ERROR
			}
			// coord
			x = *pixcoords++;
			y = *pixcoords++;
			y += ty;
			if (x & IS_DIAGONAL)
			{
				x2 = *pixcoords++;
				y2 = *pixcoords++ + ty;
				coords[0] = x & ~IS_DIAGONAL;
				coords[1] = y;
				coords[2] = x2;
				coords[3] = y2;
				glDrawArrays(GL_LINES, 0, 2); GL_CHECK_ERROR
			}
			else
			{
				if (x & IS_PIXEL)
				{
					x &= ~IS_PIXEL;
					w = h = 1;
				}
				else
				{
					w = *pixcoords++;
					h = *pixcoords++;
				}
				coords[0] = coords[2] = x;
				coords[1] = coords[7] = y;
				coords[3] = coords[5] = y + h;
				coords[4] = coords[6] = x + w;
				glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, rectOrder); GL_CHECK_ERROR
			}
		}
		clearPixels();
	}
#endif
}


/*static TCGuint createProgram(char* vertexCode, char* fragmentCode)
{
	TCGint ret = GL_TRUE;
	TCGuint vshader, fshader;
	TCGuint p = glCreateProgram();

	vshader = loadShader(GL_VERTEX_SHADER, vertexCode);
	fshader = loadShader(GL_FRAGMENT_SHADER, fragmentCode);
	glAttachShader(p, vshader); GL_CHECK_ERROR
	glAttachShader(p, fshader); GL_CHECK_ERROR
	glLinkProgram(p); GL_CHECK_ERROR
	//glValidateProgram(p);
	glGetProgramiv(p, GL_LINK_STATUS, &ret); GL_CHECK_ERROR
	if (ret == GL_FALSE)
	{
		TCGchar *buffer;
		glGetProgramiv(p, GL_INFO_LOG_LENGTH, &ret); GL_CHECK_ERROR
		buffer = (TCGchar*)xmalloc(sizeof(TCGchar)* ret);
		glGetProgramInfoLog(p, ret, &ret, buffer); GL_CHECK_ERROR
			debug("Link error: %s", buffer);
		xfree(buffer);
	}

	return p;
}*/

bool checkGLfloatBuffer(Context c, int32 n)
{
	if (n > flen)
	{
		xfree(glcoords);
		xfree(glcolors);
		flen = n * 3 / 2;
		int len = flen * 2;
		glcoords = (TCGfloat*)xmalloc(sizeof(TCGfloat)*len); pixcoords = (int32*)glcoords;
		glcolors = (TCGfloat*)xmalloc(sizeof(TCGfloat)*flen); pixcolors = (int32*)glcolors;
		pixEnd = pixcoords + len;
		if (!glcoords || !glcolors)
		{
			throwException(c, OutOfMemoryError, "Cannot allocate buffer for drawPixels");
			xfree(glcoords);
			xfree(glcolors);
			flen = 0;
			return false;
		}
	}
	return true;
}

#ifdef USE_DX

#define SIZE_BUFFER (INT16_MAX)
#define MAX_SHOULD_BUFF_LINE 100
typedef struct _dxDrawCache
{
   Pixel lastARGB;
   int32 coords_x[SIZE_BUFFER];
   int32 coords_y[SIZE_BUFFER];
   int32 size;
   int32 time;
} dxDrawCache;

dxDrawCache dxDrawPixelCache = { 0 };
dxDrawCache dxDrawPixelsCache = { 0 };
dxDrawCache dxDrawLineCache = { 0 };

#endif

void graphicsUpdateScreen(Context currentContext, ScreenSurface screen)
{
#ifndef USE_DX
   if (surfaceWillChange) { clearPixels(); return; }
   if (pixcolors != (int32*)glcolors) flushPixels(11);
#if defined ANDROID || defined WP8
   eglSwapBuffers(_display, _surface); // requires API LEVEL 9 (2.3 and up)
#else
   graphicsUpdateScreenIOS();
#endif
#if !defined(WP8)
   // erase buffer with keyboard's background color
   PixelConv gray;
   gray.pixel = shiftScreenColorP ? *shiftScreenColorP : 0xFFFFFF;
   glClearColor(f255[gray.r], f255[gray.g], f255[gray.b], 1); GL_CHECK_ERROR
      glClear(GL_COLOR_BUFFER_BIT); GL_CHECK_ERROR
#endif
#else
#ifdef TOGGLE_BUFFER
   bool must_do_3 = false;
   if (dxDrawPixelCache.size > 0)
   {
      int ini = getTimeStamp();
      dxDrawPixels(dxDrawPixelCache.coords_x, dxDrawPixelCache.coords_y, dxDrawPixelCache.size, dxDrawPixelCache.lastARGB);
      dxDrawPixelCache.size = 0;
      dxDrawPixelCache.time += getTimeStamp() - ini;
      debug("drawPixel: %d", dxDrawPixelCache.time);

      if (dxDrawPixelCache.lastARGB == dxDrawLineCache.lastARGB)
         must_do_3 = true;
      dxDrawPixelCache.time = 0;
   }
   if (must_do_3)
   {
      if (dxDrawLineCache.size > 0)
      {
         int ini = getTimeStamp();
         dxDrawPixels(dxDrawLineCache.coords_x, dxDrawLineCache.coords_y, dxDrawLineCache.size, dxDrawLineCache.lastARGB);
         dxDrawLineCache.size = 0;
         dxDrawLineCache.time += getTimeStamp() - ini;
         debug("drawLine: %d", dxDrawLineCache.time);
         dxDrawLineCache.time = 0;
      }
   }
   if (dxDrawPixelsCache.size > 0)
   {
      int ini = getTimeStamp();
      if (dxDrawPixelsCache.size > 33000)
         ini = ini;
      dxDrawPixels(dxDrawPixelsCache.coords_x, dxDrawPixelsCache.coords_y, dxDrawPixelsCache.size, dxDrawPixelsCache.lastARGB);
      dxDrawPixelsCache.size = 0;
      dxDrawPixelsCache.time += getTimeStamp() - ini;
      debug("drawPixels: %d", dxDrawPixelsCache.time);
      dxDrawPixelsCache.time = 0;
   }
   if (!must_do_3)
   {
      if (dxDrawLineCache.size > 0)
      {
         int ini = getTimeStamp();
         dxDrawPixels(dxDrawLineCache.coords_x, dxDrawLineCache.coords_y, dxDrawLineCache.size, dxDrawLineCache.lastARGB);
         dxDrawLineCache.size = 0;
         dxDrawLineCache.time += getTimeStamp() - ini;
         debug("drawLine: %d", dxDrawLineCache.time);
         dxDrawLineCache.time = 0;
      }
   }
#endif
   debug("drawPixel: %d", dxDrawPixelCache.time);
   dxDrawPixelCache.time = 0;
   debug("drawPixels: %d", dxDrawPixelsCache.time);
   dxDrawPixelsCache.time = 0;
   debug("drawLine: %d", dxDrawLineCache.time);
   dxDrawLineCache.time = 0;

   dxUpdateScreen();
#endif
}

#ifndef USE_DX
bool setupGL(int width, int height)
{
	int i;
	pixLastRGB = -1;
	appW = width;
	appH = height;

	initTexture();
	initLineRectPoint();
	initPoints();
	initShade();
	setProjectionMatrix(appW, appH);

	glPixelStorei(GL_PACK_ALIGNMENT, 1); GL_CHECK_ERROR
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1); GL_CHECK_ERROR

		glDisable(GL_CULL_FACE); GL_CHECK_ERROR
		glDisable(GL_DEPTH_TEST); GL_CHECK_ERROR
		glEnable(GL_BLEND); GL_CHECK_ERROR // enable color alpha channel
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); GL_CHECK_ERROR

	for (i = 0; i < 14; i++)
		ftransp[i + 1] = (TCGfloat)(i << 4) / (TCGfloat)255; // make it lighter. since ftransp[0] is never used, shift it to [1]
	ftransp[15] = 1;
	for (i = 0; i <= 255; i++)
		f255[i] = (TCGfloat)i / (TCGfloat)255;
	clearPixels();
	return checkTCGfloatBuffer(mainContext, 10000);
}
#endif

//void applyChanges(Context currentContext, Object obj, bool updateList)
//{
//	int32 frameCount = Image_frameCount(obj);
//	Object pixelsObj = frameCount == 1 ? Image_pixels(obj) : Image_pixelsOfAllFrames(obj);
//	if (pixelsObj)
//	{
//		Pixel *pixels = (Pixel*)ARRAYOBJ_START(pixelsObj);
//		int32 width = (Image_frameCount(obj) > 1) ? Image_widthOfAllFrames(obj) : Image_width(obj);
//		int32 height = Image_height(obj);
//		glLoadTexture(currentContext, obj, Image_textureId(obj), pixels, width, height, updateList);
//	}
//	Image_changed(obj) = false;
//}

void glDeleteTexture(TCObject img, int32* textureId, bool updateList)
{
#ifndef USE_DX
	glDeleteTextures(1, (TCGuint*)textureId); GL_CHECK_ERROR
		*textureId = 0;
	if (updateList)
		imgTextures = VoidPsRemove(imgTextures, img, null);
#endif
}

void glLoadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool updateList)
{
#ifndef USE_DX
	int32 i;
	PixelConv* pf = (PixelConv*)pixels;
	PixelConv* pt = (PixelConv*)xmalloc(width*height * 4), *pt0 = pt;
	bool textureAlreadyCreated = *textureId != 0;
	bool err;
	if (!pt)
	{
		throwException(currentContext, OutOfMemoryError, "Out of bitmap memory for image with %dx%d", width, height);
		return;
	}

	if (!textureAlreadyCreated)
	{
		glGenTextures(1, (TCGuint*)textureId); err = GL_CHECK_ERROR
		if (err)
		{
			throwException(currentContext, OutOfMemoryError, "Cannot bind texture for image with %dx%d", width, height);
			return;
		}
	}
	// OpenGL ES provides support for non-power-of-two textures, provided that the s and t wrap modes are both GL_CLAMP_TO_EDGE.
	glBindTexture(GL_TEXTURE_2D, *textureId); GL_CHECK_ERROR
	if (!textureAlreadyCreated)
	{
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); GL_CHECK_ERROR
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); GL_CHECK_ERROR
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE); GL_CHECK_ERROR
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE); GL_CHECK_ERROR
	}
	// must invert the pixels from ARGB to RGBA
	for (i = width*height; --i >= 0; pt++, pf++) { pt->a = pf->r; pt->b = pf->g; pt->g = pf->b; pt->r = pf->a; }
	if (textureAlreadyCreated)
	{
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pt0); GL_CHECK_ERROR
			glBindTexture(GL_TEXTURE_2D, 0); GL_CHECK_ERROR
	}
	else
	{
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pt0); err = GL_CHECK_ERROR
		if (err)
			throwException(currentContext, OutOfMemoryError, "Out of texture memory for image with %dx%d", width, height);
		else
		{
			if (updateList)
				imgTextures = VoidPsAdd(imgTextures, img, null);
			glBindTexture(GL_TEXTURE_2D, 0); GL_CHECK_ERROR
		}
	}
	xfree(pt0);
#endif
}

void glDrawThickLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb, int32 a)
{
#ifndef USE_DX
	add2pipe(x1 | IS_DIAGONAL, y1, x2, y2, rgb, a);
#endif
}

int32 glGetPixel(int32 x, int32 y)
{
#ifndef USE_DX
	glpixel gp;
	if (pixcolors != (int32*)glcolors) flushPixels(8);
	glReadPixels(x, appH - y - 1, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, &gp); GL_CHECK_ERROR
		return (((int32)gp.r) << 16) | (((int32)gp.g) << 8) | (int32)gp.b;
#else
   return 0;
#endif
}

void glGetPixels(Pixel* dstPixels, int32 srcX, int32 srcY, int32 width, int32 height, int32 pitch)
{
#ifndef USE_DX
#define GL_BGRA 0x80E1 // BGRA is 20x faster than RGBA on devices that supports it
	PixelConv* p;
	glpixel gp;
	int32 i;
	TCGint ext_format, ext_type;
	if (pixcolors != (int32*)glcolors) flushPixels(9);
	glGetIntegerv(GL_IMPLEMENTATION_COLOR_READ_FORMAT, &ext_format);
	glGetIntegerv(GL_IMPLEMENTATION_COLOR_READ_TYPE, &ext_type);
	if (ext_format == GL_BGRA && ext_type == GL_UNSIGNED_BYTE)
	for (; height-- > 0; srcY++, dstPixels += pitch)
	{
		glReadPixels(srcX, appH - srcY - 1, width, 1, GL_BGRA, GL_UNSIGNED_BYTE, dstPixels); GL_CHECK_ERROR
			p = (PixelConv*)dstPixels;
		for (i = 0; i < width; i++, p++)
		{
			gp.pixel = p->pixel;
			p->a = 255;//gp.a; - with this, the transition effect causes a fade-out when finished in UIGadgets
			p->r = gp.b;
			p->g = gp.g;
			p->b = gp.r;
		}
	}
	else
	for (; height-- > 0; srcY++, dstPixels += pitch)
	{
		glReadPixels(srcX, appH - srcY - 1, width, 1, GL_RGBA, GL_UNSIGNED_BYTE, dstPixels); GL_CHECK_ERROR
			p = (PixelConv*)dstPixels;
		for (i = 0; i < width; i++, p++)
		{
			gp.pixel = p->pixel;
			p->a = 255;//gp.a; - with this, the transition effect causes a fade-out when finished in UIGadgets
			p->r = gp.r;
			p->g = gp.g;
			p->b = gp.b;
		}
	}
#endif
}

void flushAll()
{
#ifndef USE_DX
	flushPixels(10);
	glFlush(); GL_CHECK_ERROR
#endif
}


void privateScreenChange(int32 w, int32 h)
{
#ifdef darwin
	graphicsIOSdoRotate();
#else
	appW = w;
	appH = h;
#endif

#ifndef USE_DX
	clearPixels();
	setProjectionMatrix(w, h);
#endif
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
#if defined ANDROID || defined WP8
	if (!isScreenChange)
	{
		destroyEGL();
		xfree(screen->extension);
		xfree(glcoords);
		xfree(glcolors);
	}
#else
	if (isScreenChange)
		screen->extension = NULL;
	else
	{
		if (screen->extension)
			free(screen->extension);
		deviceCtx = screen->extension = NULL;
		xfree(glcoords);
		xfree(glcolors);
	}
#endif
}

#ifndef USE_DX
bool initGLES(ScreenSurface screen)
{
	int32 i;
	const EGLint attribs[] =
	{
		EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
		EGL_BLUE_SIZE, 8,
		EGL_GREEN_SIZE, 8,
		EGL_RED_SIZE, 8,
		EGL_ALPHA_SIZE, 8,
		EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
		EGL_NONE
	};
	EGLint context_attribs[] = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE };
	EGLDisplay display;
	EGLConfig config;
	EGLint numConfigs;
	EGLint format;
	EGLSurface surface;
	EGLContext context;
	EGLint width;
	EGLint height;

#if 0 && (_MSC_VER >= 1800)
	// WinRT on Windows 8.1 can compile shaders at run time so we don't care about the DirectX feature level
	auto featureLevel = ANGLE_D3D_FEATURE_LEVEL::ANGLE_D3D_FEATURE_LEVEL_ANY;
#elif WINAPI_FAMILY_PARTITION(WINAPI_PARTITION_PHONE)
	// Windows Phone 8.0 uses D3D_FEATURE_LEVEL_9_3
	auto featureLevel = ANGLE_D3D_FEATURE_LEVEL::ANGLE_D3D_FEATURE_LEVEL_9_3;
#endif 

	HRESULT result = CreateWinrtEglWindow(WINRT_EGL_IUNKNOWN(CoreWindow::GetForCurrentThread()), featureLevel, m_eglWindow.GetAddressOf());
	if (SUCCEEDED(result))
	{
		//m_esContext.hWnd = m_eglWindow;

		//title, width, and height are unused, but included for backwards compatibility
		//esCreateWindow(&m_esContext, nullptr, 0, 0, ES_WINDOW_RGB);

		// m_cubeRenderer.CreateResources();
	}

	//display = m_esContext.eglDisplay;
	if ((display = eglGetDisplay(m_eglWindow)) == EGL_NO_DISPLAY)    { debug("eglGetDisplay() returned error %d", eglGetError()); return false; }
	if (!eglInitialize(display, 0, 0))                                       { debug("eglInitialize() returned error %d", eglGetError()); return false; }
	if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs))         { debug("eglChooseConfig() returned error %d", eglGetError()); destroyEGL(); return false; }
	//if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) { debug("eglGetConfigAttrib() returned error %d", eglGetError()); destroyEGL(); return false; }



	//EGLNativeWindow_setBuffersGeometry(window, 0, 0, format);

	//surface = m_esContext.eglSurface;
	//context = m_esContext.eglContext;
	//--window = &m_esContext.hWnd;
	if (!(surface = eglCreateWindowSurface(display, config, m_eglWindow, NULL)))     { debug("eglCreateWindowSurface() returned error %d", eglGetError()); destroyEGL(); return false; }
	if (!(context = eglCreateContext(display, config, EGL_NO_CONTEXT, context_attribs))) { debug("eglCreateContext() returned error %d", eglGetError()); destroyEGL(); return false; }
	if (!eglMakeCurrent(display, surface, surface, context))                 { debug("eglMakeCurrent() returned error %d", eglGetError()); destroyEGL(); return false; }
	//if (!eglQuerySurface(display, surface, EGL_WIDTH, &width) || !eglQuerySurface(display, surface, EGL_HEIGHT, &height)) { debug("eglQuerySurface() returned error %d", eglGetError()); destroyEGL(); return false; }

	_display = display;
	_surface = surface;
	_context = context;
	return setupGL(screen->screenW, screen->screenH);
}
#endif

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
	screen->bpp = 32;
	screen->screenX = screen->screenY = 0;
	screen->hRes = ascrHRes;
	screen->vRes = ascrVRes;
#ifndef USE_DX
	auto bounds = CoreWindow::GetForCurrentThread()->Bounds;
	screen->screenW = bounds.Width; //XXX lastW must be initialized with the screen bounds
	screen->screenH = bounds.Height;
   

   return initGLES(screen);
#endif

   screen->screenW = 480; //XXX lastW must be initialized with the screen bounds
   screen->screenH = 800;

   dxSetup();
   return checkGLfloatBuffer(mainContext, 10000);
}

void glDrawPixels(int32 n, int32 rgb)
{
#ifndef USE_DX
   setCurrentProgram(pointsProgram);
   if (pixLastRGB != rgb)
   {
      PixelConv pc;
      pc.pixel = pixLastRGB = rgb;
      glUniform4f(pointsColor, f255[pc.r], f255[pc.g], f255[pc.b], 0); GL_CHECK_ERROR
   }
   glVertexAttribPointer(pointsAlpha, 1, GL_FLOAT, GL_FALSE, 0, glcolors); GL_CHECK_ERROR
      glVertexAttribPointer(pointsPosition, 2, GL_FLOAT, GL_FALSE, 0, glcoords); GL_CHECK_ERROR
      glDrawArrays(GL_POINTS, 0, n); GL_CHECK_ERROR
#else
   Pixel colour = MAKE_RGBA(rgb, 0xFF);
   int ini = getTimeStamp();

#ifdef TOGGLE_BUFFER
   if (colour != dxDrawPixelsCache.lastARGB)
   {
      if (dxDrawPixelsCache.size > 0) {
         dxDrawPixels(dxDrawPixelsCache.coords_x, dxDrawPixelsCache.coords_y, dxDrawPixelsCache.size, dxDrawPixelsCache.lastARGB);
         dxDrawPixelsCache.size = 0;
      }
      dxDrawPixelsCache.lastARGB = colour;
   }

   if ((dxDrawPixelsCache.size + n) >= SIZE_BUFFER)
   {
      dxDrawPixels(dxDrawPixelsCache.coords_x, dxDrawPixelsCache.coords_y, dxDrawPixelsCache.size, dxDrawPixelsCache.lastARGB);
      dxDrawPixelsCache.size = 0;
   }
   
   for (int i = 0; i < n; i++)
   {
      dxDrawPixelsCache.coords_x[dxDrawPixelsCache.size] = glcoords[pointsPosition + (i * 2)];
      dxDrawPixelsCache.coords_y[dxDrawPixelsCache.size] = glcoords[pointsPosition + (i * 2 + 1)];
      dxDrawPixelsCache.size++;
   }
#else
   int* x;
   int* y;

   x = (int*) xmalloc(n * sizeof(int));
   y = (int*) xmalloc(n * sizeof(int));

   if (x != null && y != null)
   {
      for (int i = 0; i < n; i++)
      {
         x[i] = glcoords[pointsPosition + (i * 2)];
         y[i] = glcoords[pointsPosition + (i * 2 + 1)];
      }

      dxDrawPixels(x, y, n, colour);
      xfree(x);
      xfree(y);
   }
#endif

   int end = getTimeStamp();
   dxDrawPixelsCache.time += end - ini;
#endif
}

void glDrawPixel(int32 x, int32 y, int32 rgb, int32 a)
{
#ifndef USE_DX
   add2pipe(x | IS_PIXEL, y, 1, 1, rgb, a);
#else
   int ini = getTimeStamp();
   Pixel colour = MAKE_RGBA(rgb, a);

#ifdef TOGGLE_BUFFER
   if (colour != dxDrawPixelCache.lastARGB)
   {
      if (dxDrawPixelCache.size > 0) {
         dxDrawPixels(dxDrawPixelCache.coords_x, dxDrawPixelCache.coords_y, dxDrawPixelCache.size, dxDrawPixelCache.lastARGB);
         dxDrawPixelCache.size = 0;
      }
      dxDrawPixelCache.lastARGB = colour;
   }
   dxDrawPixelCache.coords_x[dxDrawPixelCache.size] = x;
   dxDrawPixelCache.coords_y[dxDrawPixelCache.size] = y;
   dxDrawPixelCache.size++;

   if (dxDrawPixelCache.size >= SIZE_BUFFER)
   {
      dxDrawPixels(dxDrawPixelCache.coords_x, dxDrawPixelCache.coords_y, dxDrawPixelCache.size, dxDrawPixelCache.lastARGB);
      dxDrawPixelCache.size = 0;
   }
#else
   dxDrawPixels(&x, &y, 1, colour);
#endif
   int end = getTimeStamp();
   dxDrawPixelCache.time += end - ini;
#endif
}

void glDrawLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb, int32 a)
{
#ifndef USE_DX
   // The Samsung Galaxy Tab 2 (4.0.4) has a bug in opengl for drawing horizontal/vertical lines: it draws at wrong coordinates, and incomplete sometimes. so we use fillrect, which always work
   if (x1 == x2)
      add2pipe(min32(x1, x2), min32(y1, y2), 1, abs32(y2 - y1), rgb, a);
   else
   if (y1 == y2)
      add2pipe(min32(x1, x2), min32(y1, y2), abs32(x2 - x1), 1, rgb, a);
   else
      add2pipe(x1 | IS_DIAGONAL, y1, x2, y2, rgb, a);
#else
   int ini = getTimeStamp();
   Pixel colour = MAKE_RGBA(rgb, a);

#ifdef TOGGLE_BUFFER
   if ((x1 == x2 && abs32(y2 - y1) < MAX_SHOULD_BUFF_LINE) || (y1 == y2 && abs32(x2 - x1) < MAX_SHOULD_BUFF_LINE))
   {
      if (colour != dxDrawLineCache.lastARGB)
      {
         if (dxDrawLineCache.size > 0) {
            dxDrawPixels(dxDrawLineCache.coords_x, dxDrawLineCache.coords_y, dxDrawLineCache.size, dxDrawLineCache.lastARGB);
            dxDrawLineCache.size = 0;
         }
         dxDrawLineCache.lastARGB = colour;
      }

      if (x1 == x2)
      {
         if ((dxDrawLineCache.size + (y2 - y1)) >= SIZE_BUFFER)
         {
            dxDrawPixels(dxDrawLineCache.coords_x, dxDrawLineCache.coords_y, dxDrawLineCache.size, dxDrawLineCache.lastARGB);
            dxDrawLineCache.size = 0;
         }

         for (int i = y1; i <= y2; i++)
         {
            dxDrawLineCache.coords_x[dxDrawLineCache.size] = x1;
            dxDrawLineCache.coords_y[dxDrawLineCache.size] = i;
            dxDrawLineCache.size++;
         }
      }
      else
      {
         if ((dxDrawLineCache.size + (x2 - x1)) >= SIZE_BUFFER)
         {
            dxDrawPixels(dxDrawLineCache.coords_x, dxDrawLineCache.coords_y, dxDrawLineCache.size, dxDrawLineCache.lastARGB);
            dxDrawLineCache.size = 0;
         }

         for (int i = x1; i <= x2; i++)
         {
            dxDrawLineCache.coords_x[dxDrawLineCache.size] = i;
            dxDrawLineCache.coords_y[dxDrawLineCache.size] = y1;
            dxDrawLineCache.size++;
         }
      }
   }
   else
#endif
   {
      dxDrawLine(x1, y1, x2, y2, colour);
   }

   int end = getTimeStamp();
   dxDrawLineCache.size += end - ini;
#endif
}

void glFillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz)
{
#ifndef USE_DX
	if (pixcolors != (int32*)glcolors) flushPixels(4);
	setCurrentProgram(shadeProgram);
	glVertexAttribPointer(shadeColor, 4, GL_FLOAT, GL_FALSE, 0, shcolors); GL_CHECK_ERROR
		glVertexAttribPointer(shadePosition, 2, GL_FLOAT, GL_FALSE, 0, shcoords); GL_CHECK_ERROR

		y += glShiftY;

	shcoords[0] = shcoords[2] = x;
	shcoords[1] = shcoords[7] = y;
	shcoords[3] = shcoords[5] = y + h;
	shcoords[4] = shcoords[6] = x + w;

	if (!horiz)
	{
		shcolors[0] = shcolors[12] = f255[c2.r]; // upper left + upper right
		shcolors[1] = shcolors[13] = f255[c2.g];
		shcolors[2] = shcolors[14] = f255[c2.b];

		shcolors[4] = shcolors[8] = f255[c1.r]; // lower left + lower right
		shcolors[5] = shcolors[9] = f255[c1.g];
		shcolors[6] = shcolors[10] = f255[c1.b];
	}
	else
	{
		shcolors[0] = shcolors[4] = f255[c2.r];  // upper left + lower left
		shcolors[1] = shcolors[5] = f255[c2.g];
		shcolors[2] = shcolors[6] = f255[c2.b];

		shcolors[8] = shcolors[12] = f255[c1.r]; // lower right + upper right
		shcolors[9] = shcolors[13] = f255[c1.g];
		shcolors[10] = shcolors[14] = f255[c1.b];
	}

	glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, rectOrder); GL_CHECK_ERROR
#endif
}

void setTimerInterval(int32 t);
void setShiftYgl()
{
#if defined ANDROID
	if (setShiftYonNextUpdateScreen && needsPaint != null)
	{
		setShiftYonNextUpdateScreen = false;
		glShiftY = desiredglShiftY - desiredScreenShiftY;     // set both at once
		screen.shiftY = desiredScreenShiftY;
		*needsPaint = true; // now that the shifts has been set, schedule another window update to paint at the given location
		setTimerInterval(1);
	}
	if (glShiftY < 0) // guich: occurs sometimes when the keyboard is closed and the desired shift y is 0. it was resulting in a negative value.
		glShiftY = 0;
#elif defined (WP8)
	if (setShiftYonNextUpdateScreen) {
		int32 componentPos;
		int siph = 100;//XXX MainView::GetLastInstance()->GetSIPHeight();
		componentPos = -(desiredglShiftY - desiredScreenShiftY);     // set both at once
		setShiftYonNextUpdateScreen = false;

		if (componentPos <= 100)//XXX MainView::GetLastInstance()->GetSIPHeight())
		glShiftY = 0;
		else
			glShiftY = -(componentPos - 100);//XXX MainView::GetLastInstance()->GetSIPHeight());
	}
#else
	glShiftY = -desiredScreenShiftY;
#endif
}


void glFillRect(int32 x, int32 y, int32 w, int32 h, int32 rgb, int32 a)
{
#ifndef USE_DX
   add2pipe(x, y, w, h, rgb, a);
#else
   Pixel colour = MAKE_RGBA(rgb, a);
   int ini = getTimeStamp();
   dxFillRect(x, y, x + w, y + h, colour);
   int end = getTimeStamp();

   //if (end - ini > 10)
   //debug(">>> glFillRect %d;     %03d,%03d    %03d,%03d #%08X", end - ini, x, y, x + w, y + h, colour);
#endif
}

void glSetLineWidth(int32 w)
{
#ifndef USE_DX
	setCurrentProgram(lrpProgram);
	glLineWidth(w); GL_CHECK_ERROR
#endif
}

void glDrawTexture(int32 textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 imgW, int32 imgH)
{
#ifndef USE_DX
	TCGfloat* coords = texcoords;
	/*   TCGfloat degrees = 45;
	TCGfloat radians = degreesToRadians(degrees);
	TCGfloat co = cosf(radians), si = sinf(radians);*/
	if (pixcolors != (int32*)glcolors) flushPixels(6);
	setCurrentProgram(textureProgram);
	glBindTexture(GL_TEXTURE_2D, textureId); GL_CHECK_ERROR

		dstY += glShiftY;

	// destination coordinates
	coords[0] = coords[6] = dstX;
	coords[1] = coords[3] = (dstY + h);
	coords[2] = coords[4] = (dstX + w);
	coords[5] = coords[7] = dstY;
	/* TODO trying to do rotation too
	http://db-in.com/blog/2011/04/cameras-on-opengl-es-2-x/
	http://en.wikipedia.org/wiki/Transformation_matrix#Rotation_2
	http://androidbook.com/item/4254

	mat4 tempX,tempY,temp,temp2;
	matrixRotateX(rot, tempX); matrixRotateY(rot, tempY);
	matrixMultiply(tempX,tempY,temp);
	matrixMultiply(coords,temp,temp2);
	xmemmove(coords,temp2,sizeof(mat4));*/
	//matrixRotateZ(rotY, temp); matrixMultiply(coords,temp,temp2); xmemmove(coords,temp2,sizeof(mat4));

	glVertexAttribPointer(texturePoint, 2, GL_FLOAT, false, 0, coords); GL_CHECK_ERROR

		// source coordinates
		TCGfloat left = (float)x / (float)imgW, top = (float)y / (float)imgH, right = (float)(x + w) / (float)imgW, bottom = (float)(y + h) / (float)imgH; // 0,0,1,1
	coords[8] = coords[14] = left;
	coords[9] = coords[11] = bottom;
	coords[10] = coords[12] = right;
	coords[13] = coords[15] = top;
	glVertexAttribPointer(textureCoord, 2, GL_FLOAT, false, 0, &coords[8]); GL_CHECK_ERROR

		glDrawArrays(GL_TRIANGLE_FAN, 0, 4); GL_CHECK_ERROR
		glBindTexture(GL_TEXTURE_2D, 0); GL_CHECK_ERROR
#endif
}

/*void updateScreenANGLE()
{
	graphicsUpdateScreen(mainContext, &screen);
}

void makeCurrentANGLE()
{
	if (!eglMakeCurrent(_display, _surface, _surface, _context))                 
	{ 
		debug("eglMakeCurrent() returned error %d", eglGetError()); 
		destroyEGL(); 
	}
}*/
