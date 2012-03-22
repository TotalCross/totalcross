# Microsoft Developer Studio Project File - Name="Litebase" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Dynamic-Link Library" 0x0102

CFG=Litebase - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "Litebase.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "Litebase.mak" CFG="Litebase - Win32 Debug"
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
# ADD CPP /nologo /Zp4 /W3 /Zi /Od /I "..\..\..\..\TotalCross\TotalCrossVm\src\zlib" /I "..\..\..\..\TotalCross\TotalCrossVm\src\tcvm" /I "..\..\..\..\TotalCross\TotalCrossVm\src\util" /I "p:\extlibs\win32\msinttypes" /I "..\..\..\..\Litebase\LitebaseSDK\src\native\parser" /I "..\..\..\..\Litebase\LitebaseSDK\src\native" /I "..\..\..\..\TotalCross\TotalCrossVm\src\nm\io" /I "..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /FR /TC /GZ /c
# ADD BASE MTL /nologo /win32
# ADD MTL /nologo /D "_DEBUG" /win32
# ADD BASE RSC /l 0x409
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib wsock32.lib winmm.lib MSImg32.Lib /nologo /version:2.52 /subsystem:windows /dll /incremental:no /pdb:"Debug\Litebasevc.pdb" /debug /machine:IX86 /pdbtype:sept
# SUBTRACT BASE LINK32 /pdb:none
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib uuid.lib winmm.lib ws2_32.lib Rasapi32.lib /nologo /version:2.52 /subsystem:windows /dll /incremental:no /debug /machine:IX86 /out:"../../../../output/debug/LitebaseVMS/dist/lib/win32/litebase.dll" /pdbtype:sept
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
# ADD CPP /nologo /Zp4 /W3 /O2 /Ob2 /I "..\..\..\..\TotalCross\TotalCrossVm\src\tcvm" /I "..\..\..\..\TotalCross\TotalCrossVm\src\util" /I "p:\extlibs\win32\msinttypes" /I "..\..\..\..\Litebase\LitebaseSDK\src\native\parser" /I "..\..\..\..\Litebase\LitebaseSDK\src\native" /I "..\..\..\..\TotalCross\TotalCrossVm\src\nm\io" /I "..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang" /I "..\..\..\..\TotalCross\TotalCrossVm\src\zlib" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /TC /c
# ADD BASE MTL /nologo /win32
# ADD MTL /nologo /win32
# ADD BASE RSC /l 0x409
# ADD RSC /l 0x409
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib msvcrt.lib wsock32.lib winmm.lib MSImg32.Lib /nologo /version:2.52 /subsystem:windows /dll /machine:IX86 /nodefaultlib /pdbtype:sept /opt:ref /opt:icf
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
# ADD CPP /nologo /Zp4 /W3 /O2 /Ob2 /I "..\..\..\..\TotalCross\TotalCrossVm\src\tcvm" /I "..\..\..\..\TotalCross\TotalCrossVm\src\util" /I "p:\extlibs\win32\msinttypes" /I "..\..\..\..\Litebase\LitebaseSDK\src\native\parser" /I "..\..\..\..\Litebase\LitebaseSDK\src\native" /I "..\..\..\..\TotalCross\TotalCrossVm\src\nm\io" /I "..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang" /I "..\..\src\zlib" /D "ENABLE_DEMO" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /Fr /TC /c
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

SOURCE=..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.c
DEP_CPP_TC_TE=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_TC_TE=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# End Group
# Begin Group "Parser"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseLex.c
DEP_CPP_LITEB=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_LITEB=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseMessage.c
DEP_CPP_LITEBA=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_LITEBA=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\LitebaseParser.c
DEP_CPP_LITEBAS=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_LITEBAS=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClause.c
DEP_CPP_SQLBO=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_SQLBO=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLBooleanClauseTree.c
DEP_CPP_SQLBOO=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_SQLBOO=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLColumnListClause.c
DEP_CPP_SQLCO=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_SQLCO=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLDeleteStatement.c
DEP_CPP_SQLDE=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_SQLDE=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLInsertStatement.c
DEP_CPP_SQLIN=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_SQLIN=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLSelectStatement.c
DEP_CPP_SQLSE=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_SQLSE=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\parser\SQLUpdateStatement.c
DEP_CPP_SQLUP=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_SQLUP=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# End Group
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Index.c
DEP_CPP_INDEX=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_INDEX=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Key.c
DEP_CPP_KEY_C=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_KEY_C=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\lbFile.c
DEP_CPP_LBFIL=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\io\palm\File_c.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\io\posix\File_c.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\io\win\File_c.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_LBFIL=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Litebase.c
DEP_CPP_LITEBASE=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_LITEBASE=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\LitebaseGlobals.c
DEP_CPP_LITEBASEG=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_LITEBASEG=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\MarkBits.c
DEP_CPP_MARKB=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_MARKB=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\MemoryFile.c
DEP_CPP_MEMOR=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_MEMOR=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\NativeMethods.c
DEP_CPP_NATIV=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\NativeMethods.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_NATIV=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Node.c
DEP_CPP_NODE_=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_NODE_=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\NormalFile.c
DEP_CPP_NORMA=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_NORMA=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\PlainDB.c
DEP_CPP_PLAIN=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_PLAIN=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\PreparedStatement.c
DEP_CPP_PREPA=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_PREPA=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\ResultSet.c
DEP_CPP_RESUL=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_RESUL=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\SQLValue.c
DEP_CPP_SQLVA=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_SQLVA=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\Table.c
DEP_CPP_TABLE=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_TABLE=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\TCVMLib.c
DEP_CPP_TCVML=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_TCVML=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\..\..\Litebase\LitebaseSDK\src\native\UtilsLB.c
DEP_CPP_UTILS=\
	"..\..\..\..\..\extlibs\win32\msinttypes\stdint.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\event.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\demo.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\globals.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\settings.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\init\startup.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\io\file.h"\
	"..\..\..\..\TotalCross\TotalCrossVm\src\nm\lang\StringBuffer.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\graphicsprimitives.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\debug.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\util\errormsg.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\mem.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\utils.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zconf.h"\
	"..\..\..\..\totalcross\totalcrossvm\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
	"..\..\src\native\lbFile.h"\
	"..\..\src\native\Litebase.h"\
	"..\..\src\native\LitebaseGlobals.h"\
	"..\..\src\native\LitebaseTypes.h"\
	"..\..\src\native\Macros.h"\
	"..\..\src\native\MarkBits.h"\
	"..\..\src\native\MemoryFile.h"\
	"..\..\src\native\Node.h"\
	"..\..\src\native\NormalFile.h"\
	"..\..\src\native\parser\LitebaseLex.h"\
	"..\..\src\native\parser\LitebaseMessage.h"\
	"..\..\src\native\parser\LitebaseParser.h"\
	"..\..\src\native\parser\SQLBooleanClause.h"\
	"..\..\src\native\parser\SQLBooleanClauseTree.h"\
	"..\..\src\native\parser\SQLColumnListClause.h"\
	"..\..\src\native\parser\SQLDeleteStatement.h"\
	"..\..\src\native\parser\SQLInsertStatement.h"\
	"..\..\src\native\parser\SQLSelectStatement.h"\
	"..\..\src\native\parser\SQLUpdateStatement.h"\
	"..\..\src\native\PlainDB.h"\
	"..\..\src\native\PreparedStatement.h"\
	"..\..\src\native\ResultSet.h"\
	"..\..\src\native\SQLValue.h"\
	"..\..\src\native\Table.h"\
	"..\..\src\native\TCVMLib.h"\
	"..\..\src\native\UtilsLB.h"\
	
NODEP_CPP_UTILS=\
	"..\..\..\..\totalcross\totalcrossvm\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\..\TotalCross\TotalCrossVM\src\tcvm\palm_posix.h"\
	
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
# End Group
# End Target
# End Project
