# Microsoft Developer Studio Project File - Name="Litebase" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Dynamic-Link Library" 0x0102

CFG=Litebase - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE
!MESSAGE NMAKE /f "litebase.mak".
!MESSAGE
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE
!MESSAGE NMAKE /f "litebase.mak" CFG="Litebase - Win32 Debug"
!MESSAGE
!MESSAGE Possible choices for configuration are:
!MESSAGE
!MESSAGE "Litebase - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "Litebase - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "Litebase - Win32 Demo" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "Litebase - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "../../../../temp/vc6/Litebase/Debug"
# PROP Intermediate_Dir "../../../../temp/vc6/Litebase/Debug"
# PROP Ignore_Export_Lib 1
# PROP Target_Dir ""
# ADD BASE CPP /nologo /Zp4 /W3 /Gm /GX /Zi /Od /I "..\..\src\native\vm" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /FR"$(OutDir)/" /YX /TC /GZ /c
# ADD CPP /nologo /Zp4 /W3 /Zi /Od /I "..\..\src\zlib" /I "..\..\src\tcvm" /I "..\..\src\util" /I "p:\extlibs\win32\msinttypes" /I "..\..\..\..\Litebase\LitebaseSDK\src\native\parser" /I "..\..\..\..\Litebase\LitebaseSDK\src\native" /I "..\..\src\nm\io" /I "..\..\src\nm\lang" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /FR /TC /GZ /c
# SUBTRACT CPP /Gf /YX
# ADD BASE MTL /nologo /win32
# ADD MTL /nologo /D "_DEBUG" /win32
# ADD BASE RSC /l 0x409
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib wsock32.lib winmm.lib MSImg32.Lib /nologo /version:2.28 /subsystem:windows /dll /incremental:no /pdb:"Debug\Litebasevc.pdb" /debug /machine:IX86 /pdbtype:sept
# SUBTRACT BASE LINK32 /pdb:none
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib uuid.lib winmm.lib ws2_32.lib Rasapi32.lib /nologo /version:1.14 /subsystem:windows /dll /incremental:no /debug /machine:IX86 /out:"../../../../output/debug/LitebaseVMS/dist/lib/win32/litebase.dll" /pdbtype:sept
# SUBTRACT LINK32 /pdb:none

!ELSEIF  "$(CFG)" == "Litebase - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "../../../../temp/vc6/Litebase/Release"
# PROP Intermediate_Dir "../../../../temp/vc6/Litebase/Release"
# PROP Ignore_Export_Lib 1
# PROP Target_Dir ""
# ADD BASE CPP /nologo /Zp4 /W3 /GX /I "..\..\src\native\vm" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /YX /TC /c
# ADD CPP /nologo /Zp4 /W3 /O2 /Ob2 /I "..\..\src\tcvm" /I "..\..\src\util" /I "p:\extlibs\win32\msinttypes" /I "..\..\..\..\Litebase\LitebaseSDK\src\native\parser" /I "..\..\..\..\Litebase\LitebaseSDK\src\native" /I "..\..\src\nm\io" /I "..\..\src\nm\lang" /I "..\..\src\zlib" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /TC /c
# SUBTRACT CPP /Fr /YX
# ADD BASE MTL /nologo /win32
# ADD MTL /nologo /win32
# ADD BASE RSC /l 0x409
# ADD RSC /l 0x409
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib msvcrt.lib wsock32.lib winmm.lib MSImg32.Lib /nologo /version:2.28 /subsystem:windows /dll /machine:IX86 /nodefaultlib /pdbtype:sept /opt:ref /opt:icf
# ADD LINK32 kernel32.lib libc.lib /nologo /version:1.0 /subsystem:windows /dll /machine:IX86 /nodefaultlib /out:"../../../../output/release/LitebaseVMS/dist/lib/win32/litebase.dll" /pdbtype:sept /opt:ref /opt:icf

!ELSEIF  "$(CFG)" == "Litebase - Win32 Demo"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Litebase___Win32_Demo"
# PROP BASE Intermediate_Dir "Litebase___Win32_Demo"
# PROP BASE Ignore_Export_Lib 1
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "../../../../temp/vc6/Litebase/Demo"
# PROP Intermediate_Dir "../../../../temp/vc6/Litebase/Demo"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /Zp4 /W3 /O2 /Ob2 /I "..\..\src\tcvm" /I "..\..\src\util" /I "p:\extlibs\win32\msinttypes" /I "..\..\..\..\Litebase\LitebaseSDK\src\native\parser" /I "..\..\..\..\Litebase\LitebaseSDK\src\native" /I "..\..\src\nm\io" /I "..\..\src\nm\lang" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /TC /c
# SUBTRACT BASE CPP /Fr /YX
# ADD CPP /nologo /Zp4 /W3 /O2 /Ob2 /I "..\..\src\tcvm" /I "..\..\src\util" /I "p:\extlibs\win32\msinttypes" /I "..\..\..\..\Litebase\LitebaseSDK\src\native\parser" /I "..\..\..\..\Litebase\LitebaseSDK\src\native" /I "..\..\src\nm\io" /I "..\..\src\nm\lang" /I "..\..\src\zlib" /D "ENABLE_DEMO" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /Fr /TC /c
# SUBTRACT CPP /YX
# ADD BASE MTL /nologo /win32
# ADD MTL /nologo /win32
# ADD BASE RSC /l 0x409
# ADD RSC /l 0x409
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib shell32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib wsock32.lib winmm.lib MSImg32.Lib /nologo /version:1.0 /subsystem:windows /dll /machine:IX86 /out:"P:\TotalCrossVM\builders\vc6\Release\litebase.dll" /pdbtype:sept /opt:ref /opt:icf
# SUBTRACT BASE LINK32 /nodefaultlib
# ADD LINK32 kernel32.lib shell32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib wsock32.lib winmm.lib MSImg32.Lib /nologo /version:1.0 /subsystem:windows /dll /machine:IX86 /out:"../../../../output/release/LitebaseSDK/dist/lib/win32/litebase.dll" /pdbtype:sept /opt:ref /opt:icf
# SUBTRACT LINK32 /nodefaultlib

!ENDIF

# Begin Target

# Name "Litebase - Win32 Debug"
# Name "Litebase - Win32 Release"
# Name "Litebase - Win32 Demo"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;def;odl;idl;hpj;bat;asm;asmx"
# Begin Group "tests"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\..\TotalCrossVM\src\tests\tc_testsuite.c
DEP_CPP_TC_TE=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\event\event.h"\
	"..\..\src\event\specialkeys.h"\
	"..\..\src\init\demo.h"\
	"..\..\src\init\globals.h"\
	"..\..\src\init\noras_ids\noras.inc"\
	"..\..\src\init\settings.h"\
	"..\..\src\init\startup.h"\
	"..\..\src\nm\instancefields.h"\
	"..\..\src\nm\ui\android\gfx_ex.h"\
	"..\..\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\src\nm\ui\linux\gfx_ex.h"\
	"..\..\src\nm\ui\palm\gfx_ex.h"\
	"..\..\src\nm\ui\PalmFont.h"\
	"..\..\src\nm\ui\win\gfx_ex.h"\
	"..\..\src\tcvm\context.h"\
	"..\..\src\tcvm\objectmemorymanager.h"\
	"..\..\src\tcvm\opcodes.h"\
	"..\..\src\tcvm\tcclass.h"\
	"..\..\src\tcvm\tcexception.h"\
	"..\..\src\tcvm\tcfield.h"\
	"..\..\src\tcvm\tcmethod.h"\
	"..\..\src\tcvm\tcthread.h"\
	"..\..\src\tcvm\tcvm.h"\
	"..\..\src\tests\tc_testsuite.h"\

NODEP_CPP_TC_TE=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\
	"..\..\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\src\tcvm\config.h"\
	"..\..\src\tcvm\palm_posix.h"\

# End Source File
# End Group
# Begin Group "Parser"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.c
DEP_CPP_LITEB=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_LITEB=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.c
DEP_CPP_LITEBA=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_LITEBA=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.c
DEP_CPP_LITEBAS=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_LITEBAS=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.tab.c
DEP_CPP_LITEBASE=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_LITEBASE=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.c
DEP_CPP_SQLBO=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_SQLBO=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.c
DEP_CPP_SQLBOO=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_SQLBOO=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.c
DEP_CPP_SQLCO=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_SQLCO=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.c
DEP_CPP_SQLDE=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_SQLDE=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.c
DEP_CPP_SQLIN=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_SQLIN=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.c
DEP_CPP_SQLSE=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_SQLSE=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.c
DEP_CPP_SQLUP=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Constants.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Index.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Key.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Litebase.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseGlobals.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\LitebaseTypes.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Macros.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MarkBits.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\MemoryFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Node.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\NormalFile.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PlainDB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\PreparedStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\ResultSet.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\SQLValue.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Table.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\TCVMLib.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\UtilsLB.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\Value.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.h"\

NODEP_CPP_SQLUP=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# End Group
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Index.c
DEP_CPP_INDEX=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_INDEX=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Key.c
DEP_CPP_KEY_C=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_KEY_C=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.c
DEP_CPP_LITEBASE_=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_LITEBASE_=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.c
DEP_CPP_LITEBASEG=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_LITEBASEG=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.c
DEP_CPP_MARKB=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_MARKB=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.c
DEP_CPP_MEMOR=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_MEMOR=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\NativeMethods.c
DEP_CPP_NATIV=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NativeMethods.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_NATIV=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Node.c
DEP_CPP_NODE_=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_NODE_=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.c
DEP_CPP_NORMA=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_NORMA=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.c
DEP_CPP_PLAIN=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_PLAIN=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.c
DEP_CPP_PREPA=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_PREPA=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.c
DEP_CPP_RESUL=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_RESUL=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.c
DEP_CPP_SQLVA=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_SQLVA=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Table.c
DEP_CPP_TABLE=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_TABLE=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.c
DEP_CPP_TCVML=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_TCVML=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.c
DEP_CPP_UTILS=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_UTILS=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Value.c
DEP_CPP_VALUE=\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseLex.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseMessage.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\LitebaseParser.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLColumnListClause.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLDeleteStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLInsertStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLSelectStatement.h"\
	"..\..\..\..\..\LitebaseSDK_200\src\native\parser\SQLUpdateStatement.h"\
	"..\..\..\..\..\totalcrossvm\src\event\event.h"\
	"..\..\..\..\..\totalcrossvm\src\event\specialkeys.h"\
	"..\..\..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\..\..\totalcrossvm\src\init\globals.h"\
	"..\..\..\..\..\totalcrossvm\src\init\noras_ids\noras.inc"\
	"..\..\..\..\..\totalcrossvm\src\init\settings.h"\
	"..\..\..\..\..\totalcrossvm\src\init\startup.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\instancefields.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\palmfont.h"\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\context.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\opcodes.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcclass.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcexception.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcfield.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcmethod.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcthread.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\tcvm.h"\
	"..\..\..\..\..\totalcrossvm\src\tests\tc_testsuite.h"\
	"..\..\..\..\..\totalcrossvm\src\util\datastructures.h"\
	"..\..\..\..\..\totalcrossvm\src\util\debug.h"\
	"..\..\..\..\..\totalcrossvm\src\util\dlmalloc.h"\
	"..\..\..\..\..\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\..\totalcrossvm\src\util\jchar.h"\
	"..\..\..\..\..\totalcrossvm\src\util\mem.h"\
	"..\..\..\..\..\totalcrossvm\src\util\nativelib.h"\
	"..\..\..\..\..\totalcrossvm\src\util\tcz.h"\
	"..\..\..\..\..\totalcrossvm\src\util\utils.h"\
	"..\..\..\..\..\totalcrossvm\src\util\xtypes.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\..\totalcrossvm\src\zlib\zlib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h"\
	"..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h"\

NODEP_CPP_VALUE=\
	"..\..\..\..\..\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\config.h"\
	"..\..\..\..\..\totalcrossvm\src\tcvm\palm_posix.h"\

# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl;inc;xsd"
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Constants.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Index.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Key.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseTypes.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Macros.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\NativeMethods.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Node.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Table.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.h
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Value.h
# End Source File
# End Group
# End Target
# End Project
