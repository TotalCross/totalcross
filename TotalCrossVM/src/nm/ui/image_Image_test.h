// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



TCObject pngImage,jpegImage;

TESTCASE(tuiI_imageLoad_s) // totalcross/ui/image/Image native private void imageLoad(String path);
{
   TNMParams p;
   TCObject imgObj, pathObj, obj[2], intArray;

   // load a jpg
   imgObj = createObject(currentContext, "totalcross.ui.image.Image");   
   setObjectLock(imgObj, UNLOCKED);

   intArray = FIELD_OBJ(imgObj, OBJ_CLASS(imgObj), 5) = createIntArray(currentContext, 1);
   ASSERT1_EQUALS(NotNull, intArray);

   ASSERT1_EQUALS(NotNull, imgObj);
   pathObj = createStringObjectFromCharP(currentContext, "barbara.jpg", 11);
   setObjectLock(pathObj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, pathObj);
   p.currentContext = currentContext;
   p.obj = obj;
   p.obj[0] = imgObj;
   p.obj[1] = pathObj;
   tuiI_imageLoad_s(&p);
   ASSERT1_EQUALS(NotNull, Image_pixels(imgObj));
   ASSERT2_EQUALS(I32, Image_width(imgObj), 240);
   ASSERT2_EQUALS(I32, Image_height(imgObj), 240);
   
   jpegImage = imgObj;

   // load a png
   imgObj = createObject(currentContext, "totalcross.ui.image.Image");
   setObjectLock(imgObj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, imgObj);
   intArray = FIELD_OBJ(imgObj, OBJ_CLASS(imgObj), 5) = createIntArray(currentContext, 1);
   ASSERT1_EQUALS(NotNull, intArray);
   pathObj = createStringObjectFromCharP(currentContext, "pal685.png", 10);
   setObjectLock(pathObj, UNLOCKED);
   ASSERT1_EQUALS(NotNull, pathObj);
   p.obj = obj;
   p.obj[0] = imgObj;
   p.obj[1] = pathObj;
   tuiI_imageLoad_s(&p);
   ASSERT1_EQUALS(NotNull, Image_pixels(imgObj));
   ASSERT2_EQUALS(I32, Image_width(imgObj), 240);
   ASSERT2_EQUALS(I32, Image_height(imgObj), 240);

   pngImage = imgObj; 
   finish: ;
}
TESTCASE(tuiI_imageParse_sB) // totalcross/ui/image/Image native private void imageParse(totalcross.io.Stream in, byte []buf);  #DEPENDS(tuiI_imageLoad_s)
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tuiI_changeColors_ii) // totalcross/ui/image/Image native public void changeColors(int from, int to); #DEPENDS(tuiI_imageParse_sB)
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tuiI_getPixelRow_Bi) // totalcross/ui/image/Image native protected void getPixelRow(byte []fillIn, int y); #DEPENDS(tuiI_imageParse_sB)
{
   TEST_SKIP;
   finish: ;
}
TESTCASE(tuiI_getModifiedInstance_iiiiiii) // totalcross/ui/image/Image native private void getModifiedInstance(totalcross.ui.image.Image4D newImg, int angle, int percScale, int color, int brightness, int contrast, int type); #DEPENDS(tuiI_imageParse_sB)
{
   TEST_SKIP;
   finish: ;
}
