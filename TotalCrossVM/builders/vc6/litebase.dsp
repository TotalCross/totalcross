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
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 1
# PROP Target_Dir ""
# ADD BASE CPP /nologo /Zp4 /W3 /Gm /GX /Zi /Od /I "..\..\src\native\vm" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /FR"$(OutDir)/" /YX /TC /GZ /c
# ADD CPP /nologo /Zp4 /W3 /Zi /Od /I "P:\TotalCrossVM\src\zlib" /I "P:\TotalCrossVM\src\tcvm" /I "P:\TotalCrossVM\src\util" /I "P:\LitebaseSDK_200\src\native\parser" /I "P:\LitebaseSDK_200\src\native" /I "P:\TotalCrossVM\src\nm\io" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /FR /TC /GZ /c
# SUBTRACT CPP /Gf /YX
# ADD BASE MTL /nologo /win32
# ADD MTL /nologo /D "_DEBUG" /win32
# ADD BASE RSC /l 0x409
# ADD RSC /l 0x409 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib wsock32.lib winmm.lib MSImg32.Lib /nologo /version:2.27 /subsystem:windows /dll /incremental:no /pdb:"Debug\Litebasevc.pdb" /debug /machine:IX86 /pdbtype:sept
# SUBTRACT BASE LINK32 /pdb:none
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib uuid.lib winmm.lib ws2_32.lib Rasapi32.lib /nologo /version:1.14 /subsystem:windows /dll /incremental:no /debug /machine:IX86 /out:"P:\TotalCrossVM\builders\vc6\Debug\litebase.dll" /pdbtype:sept
# SUBTRACT LINK32 /pdb:none

!ELSEIF  "$(CFG)" == "Litebase - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 1
# PROP Target_Dir ""
# ADD BASE CPP /nologo /Zp4 /W3 /GX /I "..\..\src\native\vm" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /YX /TC /c
# ADD CPP /nologo /Zp4 /W3 /O2 /Ob2 /I "P:\TotalCrossVM\src\tcvm" /I "P:\TotalCrossVM\src\util" /I "P:\LitebaseSDK_200\src\native\parser" /I "P:\LitebaseSDK_200\src\native" /I "P:\TotalCrossVM\src\nm\io" /I "P:\TotalCrossVM\src\zlib" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /TC /c
# SUBTRACT CPP /Fr /YX
# ADD BASE MTL /nologo /win32
# ADD MTL /nologo /win32
# ADD BASE RSC /l 0x409
# ADD RSC /l 0x409
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib msvcrt.lib wsock32.lib winmm.lib MSImg32.Lib /nologo /version:2.27 /subsystem:windows /dll /machine:IX86 /nodefaultlib /pdbtype:sept /opt:ref /opt:icf
# ADD LINK32 kernel32.lib libc.lib /nologo /version:1.0 /subsystem:windows /dll /machine:IX86 /nodefaultlib /out:"P:\TotalCrossVM\builders\vc6\Release\litebase.dll" /pdbtype:sept /opt:ref /opt:icf

!ELSEIF  "$(CFG)" == "Litebase - Win32 Demo"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Litebase___Win32_Demo"
# PROP BASE Intermediate_Dir "Litebase___Win32_Demo"
# PROP BASE Ignore_Export_Lib 1
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Demo"
# PROP Intermediate_Dir "Demo"
# PROP Ignore_Export_Lib 1
# PROP Target_Dir ""
# ADD BASE CPP /nologo /Zp4 /W3 /O2 /Ob2 /I "P:\TotalCrossVM\src\tcvm" /I "P:\TotalCrossVM\src\util" /I "P:\LitebaseSDK_200\src\native\parser" /I "P:\LitebaseSDK_200\src\native" /I "P:\TotalCrossVM\src\nm\io" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /TC /c
# SUBTRACT BASE CPP /Fr /YX
# ADD CPP /nologo /Zp4 /W3 /O2 /Ob2 /I "P:\TotalCrossVM\src\tcvm" /I "P:\TotalCrossVM\src\util" /I "P:\LitebaseSDK_200\src\native\parser" /I "P:\LitebaseSDK_200\src\native" /I "P:\TotalCrossVM\src\nm\io" /I "P:\TotalCrossVM\src\zlib" /D "ENABLE_DEMO" /D "LB_EXPORTS" /D "LITTLE_ENDIAN" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /Fr /TC /c
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
# ADD LINK32 kernel32.lib shell32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib wsock32.lib winmm.lib MSImg32.Lib /nologo /version:1.0 /subsystem:windows /dll /machine:IX86 /out:"P:\TotalCrossVM\builders\vc6\Demo\litebase.dll" /pdbtype:sept /opt:ref /opt:icf
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
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	
NODEP_CPP_TC_TE=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# End Group
# Begin Group "Parser"

# PROP Default_Filter ""
# Begin Source File

SOURCE=..\..\src\native\parser\LitebaseLex.c
DEP_CPP_LITEB=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_LITEB=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	

!IF  "$(CFG)" == "Litebase - Win32 Debug"

# ADD CPP /I "P:\TotalCrossVM\src\palmdb"

!ELSEIF  "$(CFG)" == "Litebase - Win32 Release"

!ELSEIF  "$(CFG)" == "Litebase - Win32 Demo"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\LitebaseMessage.c
DEP_CPP_LITEBA=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_LITEBA=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\LitebaseParser.c
DEP_CPP_LITEBAS=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_LITEBAS=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\LitebaseParser.tab.c
DEP_CPP_LITEBASE=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_LITEBASE=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLBooleanClause.c
DEP_CPP_SQLBO=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_SQLBO=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLBooleanClauseTree.c
DEP_CPP_SQLBOO=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_SQLBOO=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLColumnListClause.c
DEP_CPP_SQLCO=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_SQLCO=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLDeleteStatement.c
DEP_CPP_SQLDE=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_SQLDE=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLInsertStatement.c
DEP_CPP_SQLIN=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_SQLIN=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLSelectStatement.c
DEP_CPP_SQLSE=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_SQLSE=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLUpdateStatement.c
DEP_CPP_SQLUP=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_SQLUP=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# End Group
# Begin Source File

SOURCE=..\..\src\native\Index.c
DEP_CPP_INDEX=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_INDEX=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\Key.c
DEP_CPP_KEY_C=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_KEY_C=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\Litebase.c
DEP_CPP_LITEBASE_=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_LITEBASE_=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\LitebaseGlobals.c
DEP_CPP_LITEBASEG=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_LITEBASEG=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\LitebaseParser.y
# End Source File
# Begin Source File

SOURCE=..\..\src\native\MarkBits.c
DEP_CPP_MARKB=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_MARKB=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\MemoryFile.c
DEP_CPP_MEMOR=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_MEMOR=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\NativeMethods.c
DEP_CPP_NATIV=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_NATIV=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\Node.c
DEP_CPP_NODE_=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_NODE_=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\NormalFile.c
DEP_CPP_NORMA=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_NORMA=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\PlainDB.c
DEP_CPP_PLAIN=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_PLAIN=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\PreparedStatement.c
DEP_CPP_PREPA=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_PREPA=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\ResultSet.c
DEP_CPP_RESUL=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_RESUL=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\SQLValue.c
DEP_CPP_SQLVA=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_SQLVA=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\Table.c
DEP_CPP_TABLE=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_TABLE=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\TCVMLib.c
DEP_CPP_TCVML=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_TCVML=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\UtilsLB.c
DEP_CPP_UTILS=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_UTILS=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# Begin Source File

SOURCE=..\..\src\native\Value.c
DEP_CPP_VALUE=\
	"..\..\..\TotalCrossVM\src\event\event.h"\
	"..\..\..\TotalCrossVM\src\event\specialkeys.h"\
	"..\..\..\totalcrossvm\src\init\demo.h"\
	"..\..\..\TotalCrossVM\src\init\globals.h"\
	"..\..\..\TotalCrossVM\src\init\noras_ids\noras.inc"\
	"..\..\..\TotalCrossVM\src\init\settings.h"\
	"..\..\..\TotalCrossVM\src\init\startup.h"\
	"..\..\..\TotalCrossVM\src\nm\instancefields.h"\
	"..\..\..\TotalCrossVM\src\nm\io\File.h"\
	"..\..\..\TotalCrossVM\src\nm\io\palm\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\posix\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\io\win\File_c.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\android\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\darwin\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\GraphicsPrimitives.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\linux\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\palm\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\PalmFont.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\symbian\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\nm\ui\win\gfx_ex.h"\
	"..\..\..\TotalCrossVM\src\tcvm\context.h"\
	"..\..\..\TotalCrossVM\src\tcvm\objectmemorymanager.h"\
	"..\..\..\TotalCrossVM\src\tcvm\opcodes.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcclass.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcexception.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcfield.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcmethod.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcthread.h"\
	"..\..\..\TotalCrossVM\src\tcvm\tcvm.h"\
	"..\..\..\TotalCrossVM\src\tests\tc_testsuite.h"\
	"..\..\..\TotalCrossVM\src\util\datastructures.h"\
	"..\..\..\TotalCrossVM\src\util\debug.h"\
	"..\..\..\TotalCrossVM\src\util\dlmalloc.h"\
	"..\..\..\TotalCrossVM\src\util\errormsg.h"\
	"..\..\..\TotalCrossVM\src\util\jchar.h"\
	"..\..\..\TotalCrossVM\src\util\mem.h"\
	"..\..\..\TotalCrossVM\src\util\nativelib.h"\
	"..\..\..\TotalCrossVM\src\util\tcz.h"\
	"..\..\..\TotalCrossVM\src\util\utils.h"\
	"..\..\..\TotalCrossVM\src\util\xtypes.h"\
	"..\..\..\TotalCrossVM\src\zlib\zconf.h"\
	"..\..\..\TotalCrossVM\src\zlib\zlib.h"\
	"..\..\src\native\Constants.h"\
	"..\..\src\native\Index.h"\
	"..\..\src\native\Key.h"\
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
	"..\..\src\native\Value.h"\
	
NODEP_CPP_VALUE=\
	"..\..\..\TotalCrossVM\src\tcvm\config.h"\
	"..\..\..\TotalCrossVM\src\tcvm\palm_posix.h"\
	
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl;inc;xsd"
# Begin Source File

SOURCE=..\..\src\native\Constants.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\Litebase.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\Litebase_types.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\LitebaseGlobals.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\LitebaseLex.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\LitebaseMessage.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\LitebaseParser.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\LitebaseParser.tab.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\Macros.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\MarkBits.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\MemoryFile.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\NativeMethods.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\NormalFile.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\PreparedStatement.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\ResultSet.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\RowIterator.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLBooleanClause.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLBooleanClauseTree.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLColumnListClause.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLDeleteStatement.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLElement.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLInsertStatement.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLSelectClause.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLSelectStatement.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\parser\SQLUpdateStatement.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\SQLValue.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\Table.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\TCVMLib.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\UtilsLB.h
# End Source File
# Begin Source File

SOURCE=..\..\src\native\Value.h
# End Source File
# End Group
# End Target
# End Project
