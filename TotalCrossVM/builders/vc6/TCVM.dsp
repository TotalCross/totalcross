# Microsoft Developer Studio Project File - Name="TCVM" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Dynamic-Link Library" 0x0102

CFG=TCVM - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE
!MESSAGE NMAKE /f "TCVM.mak".
!MESSAGE
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE
!MESSAGE NMAKE /f "TCVM.mak" CFG="TCVM - Win32 Debug"
!MESSAGE
!MESSAGE Possible choices for configuration are:
!MESSAGE
!MESSAGE "TCVM - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "TCVM - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "TCVM - Win32 Demo" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "TCVM - Win32 Noras" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "TCVM - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "output\Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "../../../../temp/vc6/TCVM/Release"
# PROP Intermediate_Dir "../../../../temp/vc6/TCVM/Release"
# PROP Ignore_Export_Lib 1
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "TCVM_EXPORTS" /Yu"stdafx.h" /FD /c
# ADD CPP /nologo /Zp4 /W3 /Ot /Oa /Oi /Oy /Ob2 /I "..\..\src\tcvm" /I "..\..\src\util" /I "..\..\src\zlib" /I "p:\extlibs\win32" /I "p:\extlibs\win32\msinttypes" /D "TOTALCROSS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "TC_EXPORTS" /D "_WIN32_DCOM" /FD /c
# SUBTRACT CPP /Fr /YX /Yc /Yu
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /win32
# SUBTRACT MTL /mktyplib203
# ADD BASE RSC /l 0x416 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /machine:I386
# ADD LINK32 libc.lib Rasapi32.lib kernel32.lib user32.lib gdi32.lib advapi32.lib winmm.lib ws2_32.lib Tapi32.lib ole32.lib oleaut32.lib /nologo /version:2.0 /dll /pdb:none /machine:I386 /nodefaultlib /out:"../../../../output/release/TotalCrossVMS/dist/vm/win32/TCVM.dll"

!ELSEIF  "$(CFG)" == "TCVM - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "../../../../temp/vc6/TCVM/Debug"
# PROP Intermediate_Dir "../../../../temp/vc6/TCVM/Debug"
# PROP Ignore_Export_Lib 1
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "TCVM_EXPORTS" /Yu"stdafx.h" /FD /GZ /c
# ADD CPP /nologo /Zp4 /MTd /W3 /GX /Zi /Od /I "..\..\src\tcvm" /I "..\..\src\util" /I "..\..\src\zlib" /I "p:\extlibs\win32" /I "p:\extlibs\win32\msinttypes" /D "DISABLE_RAS" /D "TOTALCROSS" /D "ENABLE_TRACE" /D "LITTLE_ENDIAN" /D "TC_EXPORTS" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_WIN32_DCOM" /Fr /FD /GZ /c
# SUBTRACT CPP /YX
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /win32
# SUBTRACT MTL /mktyplib203
# ADD BASE RSC /l 0x416 /d "_DEBUG"
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /debug /machine:I386 /pdbtype:sept
# ADD LINK32 libc.lib Rasapi32.lib kernel32.lib user32.lib gdi32.lib advapi32.lib winmm.lib ws2_32.lib Tapi32.lib ole32.lib oleaut32.lib /nologo /version:2.0 /dll /incremental:no /map /debug /machine:I386 /nodefaultlib /out:"../../../../output/debug/TotalCrossVMS_NORAS/dist/vm/win32/TCVM.dll" /pdbtype:sept
# SUBTRACT LINK32 /pdb:none /force

!ELSEIF  "$(CFG)" == "TCVM - Win32 Demo"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "TCVM___Win32_Demo"
# PROP BASE Intermediate_Dir "TCVM___Win32_Demo"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "../../../../temp/vc6/TCVM/Demo"
# PROP Intermediate_Dir "../../../../temp/vc6/TCVM/Demo"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /Zp4 /W3 /O2 /Ob2 /I "..\..\src\tcvm" /I "..\..\src\util" /I "..\..\src\zlib" /D "TOTALCROSS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "TC_EXPORTS" /FD /c
# SUBTRACT BASE CPP /Fr /YX /Yc /Yu
# ADD CPP /nologo /Zp4 /W3 /Ot /Oa /Oi /Oy /Ob2 /I "..\..\src\tcvm" /I "..\..\src\util" /I "..\..\src\zlib" /I "p:\extlibs\win32" /I "p:\extlibs\win32\msinttypes" /D "ENABLE_DEMO" /D "TOTALCROSS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "TC_EXPORTS" /D "_WIN32_DCOM" /FD /c
# SUBTRACT CPP /Fr /YX /Yc /Yu
# ADD BASE MTL /nologo /D "NDEBUG" /win32
# SUBTRACT BASE MTL /mktyplib203
# ADD MTL /nologo /D "NDEBUG" /win32
# SUBTRACT MTL /mktyplib203
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 libc.lib Rasapi32.lib kernel32.lib user32.lib gdi32.lib advapi32.lib winmm.lib ws2_32.lib Tapi32.lib /nologo /version:2.0 /dll /pdb:none /machine:I386 /nodefaultlib /out:"P:\TotalCrossVM\builders\vc6\Release\TCVM.dll"
# ADD LINK32 libc.lib Rasapi32.lib kernel32.lib user32.lib gdi32.lib advapi32.lib winmm.lib ws2_32.lib Tapi32.lib ole32.lib oleaut32.lib /nologo /version:2.0 /dll /pdb:none /machine:I386 /nodefaultlib /out:"../../../../output/release/TotalCrossSDK/dist/vm/win32/TCVM.dll"

!ELSEIF  "$(CFG)" == "TCVM - Win32 Noras"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "TCVM___Win32_Noras"
# PROP BASE Intermediate_Dir "TCVM___Win32_Noras"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "../../../../temp/vc6/TCVM/Noras"
# PROP Intermediate_Dir "../../../../temp/vc6/TCVM/Noras"
# PROP Ignore_Export_Lib 1
# PROP Target_Dir ""
# ADD BASE CPP /nologo /Zp4 /W3 /O2 /Ob2 /I "..\..\src\tcvm" /I "..\..\src\util" /I "..\..\src\zlib" /D "DISABLE_RAS" /D "TOTALCROSS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "TC_EXPORTS" /FD /c
# SUBTRACT BASE CPP /Fr /YX /Yc /Yu
# ADD CPP /nologo /Zp4 /W3 /Ot /Oa /Oi /Oy /Ob2 /I "..\..\src\tcvm" /I "..\..\src\util" /I "..\..\src\zlib" /I "p:\extlibs\win32" /I "p:\extlibs\win32\msinttypes" /D "DISABLE_RAS" /D "TOTALCROSS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "TC_EXPORTS" /D "_WIN32_DCOM" /FD /c
# SUBTRACT CPP /Ox /Ow /Og /Fr /YX /Yc /Yu
# ADD BASE MTL /nologo /D "NDEBUG" /win32
# SUBTRACT BASE MTL /mktyplib203
# ADD MTL /nologo /D "NDEBUG" /win32
# SUBTRACT MTL /mktyplib203
# ADD BASE RSC /l 0x409 /d "NDEBUG"
# ADD RSC /l 0x409 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 libc.lib Rasapi32.lib kernel32.lib user32.lib gdi32.lib advapi32.lib winmm.lib ws2_32.lib Tapi32.lib /nologo /version:2.0 /dll /pdb:none /machine:I386 /nodefaultlib /out:"P:\TotalCrossVM\builders\vc6\Release\TCVM.dll"
# ADD LINK32 libc.lib Rasapi32.lib kernel32.lib user32.lib gdi32.lib advapi32.lib winmm.lib ws2_32.lib Tapi32.lib ole32.lib oleaut32.lib /nologo /version:2.0 /dll /pdb:none /machine:I386 /nodefaultlib /out:"../../../../output/release/TotalCrossVMS_NORAS/dist/vm/win32/TCVM.dll"

!ENDIF

# Begin Target

# Name "TCVM - Win32 Release"
# Name "TCVM - Win32 Debug"
# Name "TCVM - Win32 Demo"
# Name "TCVM - Win32 Noras"
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Group "Test cases"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\lang\Class_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\Control_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\sys\Convert_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\io\device_PortConnector_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\io\device_Scanner_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\event_Event_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\io\File_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\font_Font_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\font_FontMetrics_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\gfx_Graphics_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\image_Image_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\MainWindow_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\media_Sound_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\lang\Object_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\io\PDBFile_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\net\ServerSocket_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\net\Socket_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\net\ssl_SSL_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\lang\String_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\lang\StringBuffer_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\lang\Throwable_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\sys\Time_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\sys\Vm_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\Window_test.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\util\zip_ZLib_test.h
# End Source File
# End Group
# Begin Source File

SOURCE=..\..\src\tcvm\context.h
# End Source File
# Begin Source File

SOURCE=..\..\src\util\datastructures.h
# End Source File
# Begin Source File

SOURCE=..\..\src\util\debug.h
# End Source File
# Begin Source File

SOURCE=..\..\src\init\demo.h
# End Source File
# Begin Source File

SOURCE=..\..\src\util\dlmalloc.h
# End Source File
# Begin Source File

SOURCE=..\..\src\event\event.h
# End Source File
# Begin Source File

SOURCE=..\..\src\init\globals.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\instancefields.h
# End Source File
# Begin Source File

SOURCE=..\..\src\util\jchar.h
# End Source File
# Begin Source File

SOURCE=..\..\src\util\mem.h
# End Source File
# Begin Source File

SOURCE=..\..\src\util\nativelib.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\NativeMethods.h
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\objectmemorymanager.h
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\opcodes.h
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\PalmFont.h
# End Source File
# Begin Source File

SOURCE=..\..\src\init\settings.h
# End Source File
# Begin Source File

SOURCE=..\..\src\event\specialkeys.h
# End Source File
# Begin Source File

SOURCE=..\..\src\init\startup.h
# End Source File
# Begin Source File

SOURCE=..\..\src\tests\tc_testsuite.h
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcclass.h
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcexception.h
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcfield.h
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcmethod.h
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcthread.h
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcvm.h
# End Source File
# Begin Source File

SOURCE=..\..\src\util\tcz.h
# End Source File
# Begin Source File

SOURCE=..\..\src\util\utils.h
# End Source File
# Begin Source File

SOURCE=..\..\src\util\xtypes.h
# End Source File
# End Group
# Begin Group "Native methods"

# PROP Default_Filter ""
# Begin Group "io"

# PROP Default_Filter ""
# Begin Group "device"

# PROP Default_Filter ""
# Begin Group "bluetooth"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\io\device\bluetooth\DiscoveryAgent.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\io\device\bluetooth\SerialPortClient.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\io\device\bluetooth\SerialPortServer.c
# End Source File
# End Group
# Begin Group "gps"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\io\device\gps\GPS.c
# End Source File
# End Group
# End Group
# Begin Source File

SOURCE=..\..\src\nm\io\device_PortConnector.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\io\File.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\io\PDBFile.c
# End Source File
# End Group
# Begin Group "lang"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\lang\Class.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\lang\Object.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\lang\String.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\lang\StringBuffer.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\lang\Thread.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\lang\Throwable.c
# End Source File
# End Group
# Begin Group "net"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\net\ConnectionManager.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\net\ServerSocket.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\net\Socket.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\net\ssl_SSL.c
# ADD CPP /I "..\..\src\axtls"
# End Source File
# End Group
# Begin Group "pim"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\pim\POutlook.c
# End Source File
# End Group
# Begin Group "sys"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\sys\CharacterConverter.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\sys\Convert.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\sys\Registry.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\sys\Time.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\sys\Vm.c
# End Source File
# End Group
# Begin Group "ui"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\ui\Control.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\event_Event.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\font_Font.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\font_FontMetrics.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\gfx_Graphics.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\image_Image.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\MainWindow.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\media_Camera.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\media_MediaClip.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\media_Sound.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\ui\Window.c
# End Source File
# End Group
# Begin Group "util"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\util\BigInteger.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\util\zip\CompressedStream.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\util\concurrent_Lock.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\guid.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\util\zip_ZLib.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\util\zip\ZipEntry.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\util\zip\ZipFile.c
# End Source File
# End Group
# Begin Group "xml"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\xml\xml_XmlTokenizer.c
# End Source File
# End Group
# Begin Group "crypto"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\crypto\AESCipher.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\crypto\MD5Digest.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\crypto\PKCS1Signature.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\crypto\RSACipher.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\crypto\SHA1Digest.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\crypto\SHA256Digest.c
# End Source File
# End Group
# Begin Group "phone"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\nm\phone\CellInfo.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\phone\Dial.c
# End Source File
# Begin Source File

SOURCE=..\..\src\nm\phone\SMS.c
# End Source File
# End Group
# End Group
# Begin Group "ZLib"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\zlib\adler32.c
# End Source File
# Begin Source File

SOURCE=..\..\src\zlib\compress.c
# End Source File
# Begin Source File

SOURCE=..\..\src\zlib\crc32.c
# End Source File
# Begin Source File

SOURCE=..\..\src\zlib\deflate.c
# End Source File
# Begin Source File

SOURCE=..\..\src\zlib\infback.c
# End Source File
# Begin Source File

SOURCE=..\..\src\zlib\inffast.c
# End Source File
# Begin Source File

SOURCE=..\..\src\zlib\inflate.c
# End Source File
# Begin Source File

SOURCE=..\..\src\zlib\inftrees.c
# End Source File
# Begin Source File

SOURCE=..\..\src\zlib\trees.c
# End Source File
# Begin Source File

SOURCE=..\..\src\zlib\uncompr.c
# End Source File
# Begin Source File

SOURCE=..\..\src\zlib\zutil.c
# End Source File
# End Group
# Begin Group "Tests"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\tests\tc_tests.c
# End Source File
# Begin Source File

SOURCE=..\..\src\tests\tc_testsuite.c
# End Source File
# End Group
# Begin Group "Util files"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\util\datastructures.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\debug.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\dlmalloc.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\errormsg.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\jchar.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\mem.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\nativelib.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\tcz.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\utils.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\winsockLib.c
# End Source File
# Begin Source File

SOURCE=..\..\src\util\xtypes.c
# End Source File
# End Group
# Begin Group "VM Files"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\tcvm\context.c
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\objectmemorymanager.c
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcclass.c
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcexception.c
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcfield.c
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcmethod.c
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcthread.c
# End Source File
# Begin Source File

SOURCE=..\..\src\tcvm\tcvm.c
# End Source File
# End Group
# Begin Group "Event"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\event\Event.c
# End Source File
# Begin Source File

SOURCE=..\..\src\event\specialkeys.c
# End Source File
# End Group
# Begin Group "jpeg"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\jpeg\jcomapi.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdapimin.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdapistd.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdatasrc.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdcoefct.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdcolor.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jddctmgr.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdhuff.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdinput.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdmainct.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdmarker.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdmaster.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdphuff.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdpostct.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jdsample.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jerror.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jidctfst.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jidctred.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jmemmgr.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jmemnobs.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\JpegLoader.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jquant1.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jquant2.c
# End Source File
# Begin Source File

SOURCE=..\..\src\jpeg\jutils.c
# End Source File
# End Group
# Begin Group "png"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\png\png.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\pngerror.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\pngget.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\PngLoader.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\pngmem.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\pngpread.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\pngread.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\pngrio.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\pngrtran.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\pngrutil.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\pngset.c
# End Source File
# Begin Source File

SOURCE=..\..\src\png\pngtrans.c
# End Source File
# End Group
# Begin Group "init"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\init\demo.c
# End Source File
# Begin Source File

SOURCE=..\..\src\init\globals.c
# End Source File
# Begin Source File

SOURCE=..\..\src\init\settings.c
# End Source File
# Begin Source File

SOURCE=..\..\src\init\startup.c
# End Source File
# End Group
# Begin Group "PalmDB"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\palmdb\palmdb.c
# End Source File
# End Group
# Begin Group "axtls"

# PROP Default_Filter "*.c"
# Begin Source File

SOURCE=..\..\src\axtls\aes.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\asn1.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\axssl_config.h
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\bigint.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\bigint.h
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\bigint_impl.h
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\cert.h
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\crypto.h
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\crypto_misc.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\hmac.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\loader.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\md2.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\md5.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\openssl.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\os_port.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\os_port.h
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\p12.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\private_key.h
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\rc4.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\rsa.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\sha1.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\sha256.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\ssl.h
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\tls1.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\tls1.h
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\tls1_clnt.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\tls1_svr.c
# End Source File
# Begin Source File

SOURCE=..\..\src\axtls\version.h
# End Source File
# End Group
# Begin Group "ras"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\ras\ras_Utils.c
# End Source File
# End Group
# Begin Group "minizip"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\minizip\ioapi.c
# End Source File
# Begin Source File

SOURCE=..\..\src\minizip\unzip.c
# End Source File
# Begin Source File

SOURCE=..\..\src\minizip\zip.c
# End Source File
# End Group
# End Target
# End Project
