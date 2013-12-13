#include "tcvm.h"

#define TEST_COUNT 347

// Function prototypes
void test_VM_PrimitiveTypeSizes(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_TestSetJmp(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_Stack(struct TestSuite *tc, Context currentContext);     // tcvm/objectmemorymanager_test.h
void test_DblList(struct TestSuite *tc, Context currentContext);   // tcvm/objectmemorymanager_test.h
void test_GarbageCollector(struct TestSuite *tc, Context currentContext);// tcvm/objectmemorymanager_test.h
void test_VM_LoadTestTCZ(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_BREAK(struct TestSuite *tc, Context currentContext);  // tcvm/tcvm_test.h
void test_tiF_isCardInserted_i(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h
void test_tiF_create_sii(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_isCardInserted_i
void test_tiF_createDir(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_delete(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_exists(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_getSize(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_isDir(struct TestSuite *tc, Context currentContext); // nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_listFiles(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_nativeClose(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_rename_s(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_setAttributes_i(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_setSize_i(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_setTime_bt(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiF_writeBytes_Bii(struct TestSuite *tc, Context currentContext);// nm/io/File_test.h - depends on testtiF_create_sii
void test_tiPDBF_addRecord_i(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_addRecord_ii(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_create_sssi(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_delete(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_deleteRecord(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_getRecordCount(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_inspectRecord_Bii(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_listPDBs_ii(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_nativeClose(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_readBytes_Bii(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_rename_s(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_resizeRecord_i(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_searchBytes_Bii(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_setAttributes_i(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_setRecordAttributes_ib(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_setRecordPos_i(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tiPDBF_writeBytes_Bii(struct TestSuite *tc, Context currentContext);// nm/io/PDBFile_test.h
void test_tidPC_close(struct TestSuite *tc, Context currentContext);// nm/io/device_PortConnector_test.h
void test_tidPC_create_iiiii(struct TestSuite *tc, Context currentContext);// nm/io/device_PortConnector_test.h
void test_tidPC_isOpen(struct TestSuite *tc, Context currentContext);// nm/io/device_PortConnector_test.h
void test_tidPC_readBytes_Bii(struct TestSuite *tc, Context currentContext);// nm/io/device_PortConnector_test.h
void test_tidPC_readCheck(struct TestSuite *tc, Context currentContext);// nm/io/device_PortConnector_test.h
void test_tidPC_setFlowControl_b(struct TestSuite *tc, Context currentContext);// nm/io/device_PortConnector_test.h
void test_tidPC_writeBytes_Bii(struct TestSuite *tc, Context currentContext);// nm/io/device_PortConnector_test.h
void test_jlC_forName_s(struct TestSuite *tc, Context currentContext);// nm/lang/Class_test.h
void test_jlC_newInstance(struct TestSuite *tc, Context currentContext);// nm/lang/Class_test.h - depends on testjlC_forName_s
void test_jlC_isInstance_o(struct TestSuite *tc, Context currentContext);// nm/lang/Class_test.h - depends on testjlC_newInstance
void test_jlO_getClass(struct TestSuite *tc, Context currentContext);// nm/lang/Object_test.h
void test_jlO_toStringNative(struct TestSuite *tc, Context currentContext);// nm/lang/Object_test.h
void test_jlSB_aensureCapacity_i(struct TestSuite *tc, Context currentContext);// nm/lang/StringBuffer_test.h
void test_jlSB_append_C(struct TestSuite *tc, Context currentContext);// nm/lang/StringBuffer_test.h
void test_jlSB_append_Cii(struct TestSuite *tc, Context currentContext);// nm/lang/StringBuffer_test.h
void test_jlSB_append_c(struct TestSuite *tc, Context currentContext);// nm/lang/StringBuffer_test.h
void test_jlSB_append_d(struct TestSuite *tc, Context currentContext);// nm/lang/StringBuffer_test.h
void test_jlSB_append_i(struct TestSuite *tc, Context currentContext);// nm/lang/StringBuffer_test.h
void test_jlSB_append_l(struct TestSuite *tc, Context currentContext);// nm/lang/StringBuffer_test.h
void test_jlSB_append_s(struct TestSuite *tc, Context currentContext);// nm/lang/StringBuffer_test.h
void test_jlSB_setLength_i(struct TestSuite *tc, Context currentContext);// nm/lang/StringBuffer_test.h
void test_jlS_compareTo_s(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_copyChars_CiCii(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_endsWith_s(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_equalsIgnoreCase_s(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_equals_o(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_hashCode(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_indexOf_i(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_indexOf_ii(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_indexOf_si(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_lastIndexOf_i(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_lastIndexOf_ii(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_replace_cc(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_startsWith_si(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_toLowerCase(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_toUpperCase(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_trim(struct TestSuite *tc, Context currentContext);  // nm/lang/String_test.h
void test_jlS_valueOf_c(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_valueOf_d(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlS_valueOf_i(struct TestSuite *tc, Context currentContext);// nm/lang/String_test.h
void test_jlT_start(struct TestSuite *tc, Context currentContext); // nm/lang/Thread_test.h
void test_jlT_yield(struct TestSuite *tc, Context currentContext); // nm/lang/Thread_test.h
void test_jlT_printStackTraceNative(struct TestSuite *tc, Context currentContext);// nm/lang/Throwable_test.h
void test_tnSS_accept(struct TestSuite *tc, Context currentContext);// nm/net/ServerSocket_test.h
void test_tnSS_isOpen(struct TestSuite *tc, Context currentContext);// nm/net/ServerSocket_test.h
void test_tnSS_nativeClose(struct TestSuite *tc, Context currentContext);// nm/net/ServerSocket_test.h
void test_tnSS_serversocketCreate_iiis(struct TestSuite *tc, Context currentContext);// nm/net/ServerSocket_test.h
void test_Socket(struct TestSuite *tc, Context currentContext);    // nm/net/Socket_test.h
void test_tnsSSLCTX_create_ii(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSLCTX_dispose(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSLCTX_find_s(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSLCTX_newClient_sB(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSLCTX_newServer_s(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSLCTX_objLoad_iBis(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSLCTX_objLoad_iss(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSLU_displayError_i(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSLU_getConfig_i(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSLU_version(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSL_dispose(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSL_getCertificateDN_i(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSL_getCipherId(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSL_getSessionId(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSL_handshakeStatus(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSL_read_s(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSL_renegotiate(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSL_verifyCertificate(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tnsSSL_write_Bi(struct TestSuite *tc, Context currentContext);// nm/net/ssl_SSL_test.h
void test_tpcbIPOIC_GetAllAppointments(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_GetAllContacts(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_GetAllTasks(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_NewContact(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_ViewAllAppointments(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_ViewAllContacts(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_ViewAllTasks(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_editIAppointment_sssss(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_editIContact_sssssssss(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_editITask_ssssssssssss(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_getIAppointmentString_(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_getIContactString_s(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_getITaskString_s(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_newAppointment(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_newTask(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_removeIAppointment_s(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_removeIContact_s(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tpcbIPOIC_removeITask_s(struct TestSuite *tc, Context currentContext);// nm/pim/POutlook_test.h
void test_tsC_doubleToIntBits_d(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_doubleToLongBits_d(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tufF_fontCreate_f(struct TestSuite *tc, Context currentContext);// nm/ui/font_Font_test.h
void test_tufFM_fontMetricsCreate(struct TestSuite *tc, Context currentContext);// nm/ui/font_FontMetrics_test.h - depends on testtufF_fontCreate_f
void test_tsC_getBreakPos_fsiib(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h - depends on testtufFM_fontMetricsCreate
void test_tsC_hashCode_s(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_insertAt_sic(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_intBitsToDouble_i(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_longBitsToDouble_l(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_toDouble_s(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_toInt_s(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_toLong_s(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_toLowerCase_c(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_toString_c(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_toString_di(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_toString_i(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_toString_l(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_toString_si(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_toUpperCase_c(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsC_unsigned2hex_ii(struct TestSuite *tc, Context currentContext);// nm/sys/Convert_test.h
void test_tsT_update(struct TestSuite *tc, Context currentContext);// nm/sys/Time_test.h
void test_tsV_arrayCopy_oioii(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_attachLibrary_s(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_clipboardPaste(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_debug_s(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_exec_ssib(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_exitAndReboot(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_getFile_s(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_getFreeMemory(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_getRemainingBattery(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_getStackTrace_t(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_getTimeStamp(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_interceptSpecialKeys_I(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_isKeyDown_i(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_privateAttachNativeLibrary_s(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_setAutoOff_b(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_setTime_t(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_sleep_i(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tsV_tweak_ib(struct TestSuite *tc, Context currentContext);// nm/sys/Vm_test.h
void test_tuC_updateScreen(struct TestSuite *tc, Context currentContext);// nm/ui/Control_test.h
void test_tuMW_exit_i(struct TestSuite *tc, Context currentContext);// nm/ui/MainWindow_test.h
void test_tuMW_getCommandLine(struct TestSuite *tc, Context currentContext);// nm/ui/MainWindow_test.h
void test_tuMW_setTimerInterval_i(struct TestSuite *tc, Context currentContext);// nm/ui/MainWindow_test.h
void test_tuW_pumpEvents(struct TestSuite *tc, Context currentContext);// nm/ui/Window_test.h
void test_tuW_setSIP_icb(struct TestSuite *tc, Context currentContext);// nm/ui/Window_test.h
void test_tueE_isAvailable(struct TestSuite *tc, Context currentContext);// nm/ui/event_Event_test.h
void test_tufFM_charWidth_c(struct TestSuite *tc, Context currentContext);// nm/ui/font_FontMetrics_test.h - depends on testtufFM_fontMetricsCreate
void test_tufFM_stringWidth_Cii(struct TestSuite *tc, Context currentContext);// nm/ui/font_FontMetrics_test.h
void test_tuiI_imageLoad_s(struct TestSuite *tc, Context currentContext);// nm/ui/image_Image_test.h
void test_Graphics(struct TestSuite *tc, Context currentContext);  // nm/ui/gfx_Graphics_test.h - depends on testtuiI_imageLoad_s
void test_tufF_FontTestCleanup_f(struct TestSuite *tc, Context currentContext);// nm/ui/font_Font_test.h - depends on testGraphics
void test_tuiI_imageParse_sB(struct TestSuite *tc, Context currentContext);// nm/ui/image_Image_test.h - depends on testtuiI_imageLoad_s
void test_tuiI_changeColors_ii(struct TestSuite *tc, Context currentContext);// nm/ui/image_Image_test.h - depends on testtuiI_imageParse_sB
void test_tuiI_getModifiedInstance_iiiiiii(struct TestSuite *tc, Context currentContext);// nm/ui/image_Image_test.h - depends on testtuiI_imageParse_sB
void test_tuiI_getPixelRow_Bi(struct TestSuite *tc, Context currentContext);// nm/ui/image_Image_test.h - depends on testtuiI_imageParse_sB
void test_tumMC_pause_b(struct TestSuite *tc, Context currentContext);// nm/ui/media_MediaClip_test.h
void test_tumMC_play_b(struct TestSuite *tc, Context currentContext);// nm/ui/media_MediaClip_test.h
void test_tumMC_stop(struct TestSuite *tc, Context currentContext);// nm/ui/media_MediaClip_test.h
void test_tumS_beep(struct TestSuite *tc, Context currentContext); // nm/ui/media_Sound_test.h
void test_tumS_setEnabled_b(struct TestSuite *tc, Context currentContext);// nm/ui/media_Sound_test.h
void test_tumS_tone_ii(struct TestSuite *tc, Context currentContext);// nm/ui/media_Sound_test.h
void test_ZLib(struct TestSuite *tc, Context currentContext);      // nm/util/zip_ZLib_test.h
void test_XmlTokenizer(struct TestSuite *tc, Context currentContext);// nm/xml/xml_XmlTokenizer_test.h
void test_StringObject(struct TestSuite *tc, Context currentContext);// tcvm/objectmemorymanager_test.h - depends on testDblList
void test_VM_CodeUnion(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_ADD_aru_regI_s6(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_CodeUnion
void test_VM_ADD_regD_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_ADD_regI_aru_s6(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_ADD_regI_arc_s6(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_ADD_regI_aru_s6
void test_VM_ADD_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_ADD_regI_regI_sym(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_ADD_regI_s12_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_ADD_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_AND_regI_aru_s6(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_AND_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_AND_regI_regI_s12(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_AND_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_CHECKCAST(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_CONV_regD_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_CONV_regD_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_CONV_regI_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_CONV_regI_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_CONV_regIb_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_CONV_regIc_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_CONV_regIs_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_CONV_regL_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_CONV_regL_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_DECJGEZ_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_DECJGTZ_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_DIV_regD_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_DIV_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_DIV_regI_regI_s12(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_DIV_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_INC_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_INSTANCEOF(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JEQ_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JEQ_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JEQ_regI_s6(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JEQ_regI_sym(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JEQ_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JEQ_regO_null(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JEQ_regO_regO(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JGE_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JGE_regI_arlen(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JGE_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JGE_regI_s6(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JGE_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JGT_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JGT_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JGT_regI_s6(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JGT_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JLE_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JLE_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JLE_regI_s6(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JLE_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JLT_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JLT_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JLT_regI_s6(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JLT_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JNE_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JNE_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JNE_regI_s6(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JNE_regI_sym(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JNE_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JNE_regO_null(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_JNE_regO_regO(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOD_regD_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOD_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOD_regI_regI_s12(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOD_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_arc_reg16(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_aru_reg64(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_arc_reg64(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_aru_reg64
void test_VM_MOV_aru_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_arc_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_aru_regI
void test_VM_MOV_aru_regIb(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_arc_regIb(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_aru_regIb
void test_VM_MOV_aru_regO(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_arc_regO(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_aru_regO
void test_VM_MOV_aru_reg16(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_field_reg64(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_field_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_field_regO(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_reg16_arc(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_reg16_aru(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_reg64_aru(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_reg64_arc(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_reg64_aru
void test_VM_MOV_reg64_field(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_field_reg64
void test_VM_MOV_reg64_reg64(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_reg64_static(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regD_s18(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regD_sym(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regI_aru(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regI_arc(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_regI_aru
void test_VM_MOV_regI_arlen(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regI_field(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_field_regI
void test_VM_MOV_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regI_s18(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regI_static(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regI_sym(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regIb_arc(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_reg16_aru
void test_VM_MOV_regIb_aru(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regL_s18(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regL_sym(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regO_aru(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regO_arc(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_regO_aru
void test_VM_MOV_regO_field(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_field_regO
void test_VM_MOV_regO_null(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regO_regO(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_static_regO(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_regO_static(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h - depends on testVM_MOV_static_regO
void test_VM_MOV_regO_sym(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_static_reg64(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MOV_static_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MUL_regD_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MUL_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MUL_regI_regI_s12(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_MUL_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_NEWARRAY_len(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_NEWARRAY_multi(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_NEWARRAY_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_NEWOBJ(struct TestSuite *tc, Context currentContext); // tcvm/tcvm_test.h
void test_VM_OR_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_OR_regI_regI_s12(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_OR_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SHL_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SHL_regI_regI_s12(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SHL_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SHR_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SHR_regI_regI_s12(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SHR_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SUB_regD_regD_regD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SUB_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SUB_regI_s12_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SUB_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_SWITCH(struct TestSuite *tc, Context currentContext); // tcvm/tcvm_test.h
void test_VM_TEST_regO(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_THROW(struct TestSuite *tc, Context currentContext);  // tcvm/tcvm_test.h
void test_VM_USHR_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_USHR_regI_regI_s12(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_USHR_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_XOR_regI_regI_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_XOR_regI_regI_s12(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_XOR_regL_regL_regL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z0_JUMP_s24(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z1_JUMP_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z2_RETURN_void(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z3_RETURN_reg64(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z3_RETURN_regI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z3_RETURN_regO(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z4_RETURN_null(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z4_RETURN_s24D(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z4_RETURN_s24I(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z4_RETURN_s24L(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z5_RETURN_symD(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z5_RETURN_symI(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z5_RETURN_symL(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z5_RETURN_symO(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z6_CALL_normal(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test_VM_z7_CALL_virtual(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h
void test__doubleToStr(struct TestSuite *tc, Context currentContext);// util/utils_test.h
void test__str2double(struct TestSuite *tc, Context currentContext);// util/utils_test.h
void test__str2int64(struct TestSuite *tc, Context currentContext);// util/utils_test.h
void test_VM_Cleanup(struct TestSuite *tc, Context currentContext);// tcvm/tcvm_test.h

#ifdef ENABLE_TEST_SUITE

void fillTestCaseArray(testFunc *tests)
{
   tests[0] = test_VM_PrimitiveTypeSizes;
   tests[1] = test_VM_TestSetJmp;
   tests[2] = test_Stack;
   tests[3] = test_DblList;
   tests[4] = test_GarbageCollector;
   tests[5] = test_VM_LoadTestTCZ;
   tests[6] = test_VM_BREAK;
   tests[7] = test_tiF_isCardInserted_i;
   tests[8] = test_tiF_create_sii;
   tests[9] = test_tiF_createDir;
   tests[10] = test_tiF_delete;
   tests[11] = test_tiF_exists;
   tests[12] = test_tiF_getSize;
   tests[13] = test_tiF_isDir;
   tests[14] = test_tiF_listFiles;
   tests[15] = test_tiF_nativeClose;
   tests[16] = test_tiF_rename_s;
   tests[17] = test_tiF_setAttributes_i;
   tests[18] = test_tiF_setSize_i;
   tests[19] = test_tiF_setTime_bt;
   tests[20] = test_tiF_writeBytes_Bii;
   tests[21] = test_tiPDBF_addRecord_i;
   tests[22] = test_tiPDBF_addRecord_ii;
   tests[23] = test_tiPDBF_create_sssi;
   tests[24] = test_tiPDBF_delete;
   tests[25] = test_tiPDBF_deleteRecord;
   tests[26] = test_tiPDBF_getRecordCount;
   tests[27] = test_tiPDBF_inspectRecord_Bii;
   tests[28] = test_tiPDBF_listPDBs_ii;
   tests[29] = test_tiPDBF_nativeClose;
   tests[30] = test_tiPDBF_readBytes_Bii;
   tests[31] = test_tiPDBF_rename_s;
   tests[32] = test_tiPDBF_resizeRecord_i;
   tests[33] = test_tiPDBF_searchBytes_Bii;
   tests[34] = test_tiPDBF_setAttributes_i;
   tests[35] = test_tiPDBF_setRecordAttributes_ib;
   tests[36] = test_tiPDBF_setRecordPos_i;
   tests[37] = test_tiPDBF_writeBytes_Bii;
   tests[38] = test_tidPC_close;
   tests[39] = test_tidPC_create_iiiii;
   tests[40] = test_tidPC_isOpen;
   tests[41] = test_tidPC_readBytes_Bii;
   tests[42] = test_tidPC_readCheck;
   tests[43] = test_tidPC_setFlowControl_b;
   tests[44] = test_tidPC_writeBytes_Bii;
   tests[45] = test_jlC_forName_s;
   tests[46] = test_jlC_newInstance;
   tests[47] = test_jlC_isInstance_o;
   tests[48] = test_jlO_getClass;
   tests[49] = test_jlO_toStringNative;
   tests[50] = test_jlSB_aensureCapacity_i;
   tests[51] = test_jlSB_append_C;
   tests[52] = test_jlSB_append_Cii;
   tests[53] = test_jlSB_append_c;
   tests[54] = test_jlSB_append_d;
   tests[55] = test_jlSB_append_i;
   tests[56] = test_jlSB_append_l;
   tests[57] = test_jlSB_append_s;
   tests[58] = test_jlSB_setLength_i;
   tests[59] = test_jlS_compareTo_s;
   tests[60] = test_jlS_copyChars_CiCii;
   tests[61] = test_jlS_endsWith_s;
   tests[62] = test_jlS_equalsIgnoreCase_s;
   tests[63] = test_jlS_equals_o;
   tests[64] = test_jlS_hashCode;
   tests[65] = test_jlS_indexOf_i;
   tests[66] = test_jlS_indexOf_ii;
   tests[67] = test_jlS_indexOf_si;
   tests[68] = test_jlS_lastIndexOf_i;
   tests[69] = test_jlS_lastIndexOf_ii;
   tests[70] = test_jlS_replace_cc;
   tests[71] = test_jlS_startsWith_si;
   tests[72] = test_jlS_toLowerCase;
   tests[73] = test_jlS_toUpperCase;
   tests[74] = test_jlS_trim;
   tests[75] = test_jlS_valueOf_c;
   tests[76] = test_jlS_valueOf_d;
   tests[77] = test_jlS_valueOf_i;
   tests[78] = test_jlT_start;
   tests[79] = test_jlT_yield;
   tests[80] = test_jlT_printStackTraceNative;
   tests[81] = test_tnSS_accept;
   tests[82] = test_tnSS_isOpen;
   tests[83] = test_tnSS_nativeClose;
   tests[84] = test_tnSS_serversocketCreate_iiis;
   tests[85] = test_Socket;
   tests[86] = test_tnsSSLCTX_create_ii;
   tests[87] = test_tnsSSLCTX_dispose;
   tests[88] = test_tnsSSLCTX_find_s;
   tests[89] = test_tnsSSLCTX_newClient_sB;
   tests[90] = test_tnsSSLCTX_newServer_s;
   tests[91] = test_tnsSSLCTX_objLoad_iBis;
   tests[92] = test_tnsSSLCTX_objLoad_iss;
   tests[93] = test_tnsSSLU_displayError_i;
   tests[94] = test_tnsSSLU_getConfig_i;
   tests[95] = test_tnsSSLU_version;
   tests[96] = test_tnsSSL_dispose;
   tests[97] = test_tnsSSL_getCertificateDN_i;
   tests[98] = test_tnsSSL_getCipherId;
   tests[99] = test_tnsSSL_getSessionId;
   tests[100] = test_tnsSSL_handshakeStatus;
   tests[101] = test_tnsSSL_read_s;
   tests[102] = test_tnsSSL_renegotiate;
   tests[103] = test_tnsSSL_verifyCertificate;
   tests[104] = test_tnsSSL_write_Bi;
   tests[105] = test_tpcbIPOIC_GetAllAppointments;
   tests[106] = test_tpcbIPOIC_GetAllContacts;
   tests[107] = test_tpcbIPOIC_GetAllTasks;
   tests[108] = test_tpcbIPOIC_NewContact;
   tests[109] = test_tpcbIPOIC_ViewAllAppointments;
   tests[110] = test_tpcbIPOIC_ViewAllContacts;
   tests[111] = test_tpcbIPOIC_ViewAllTasks;
   tests[112] = test_tpcbIPOIC_editIAppointment_sssss;
   tests[113] = test_tpcbIPOIC_editIContact_sssssssss;
   tests[114] = test_tpcbIPOIC_editITask_ssssssssssss;
   tests[115] = test_tpcbIPOIC_getIAppointmentString_;
   tests[116] = test_tpcbIPOIC_getIContactString_s;
   tests[117] = test_tpcbIPOIC_getITaskString_s;
   tests[118] = test_tpcbIPOIC_newAppointment;
   tests[119] = test_tpcbIPOIC_newTask;
   tests[120] = test_tpcbIPOIC_removeIAppointment_s;
   tests[121] = test_tpcbIPOIC_removeIContact_s;
   tests[122] = test_tpcbIPOIC_removeITask_s;
   tests[123] = test_tsC_doubleToIntBits_d;
   tests[124] = test_tsC_doubleToLongBits_d;
   tests[125] = test_tufF_fontCreate_f;
   tests[126] = test_tufFM_fontMetricsCreate;
   tests[127] = test_tsC_getBreakPos_fsiib;
   tests[128] = test_tsC_hashCode_s;
   tests[129] = test_tsC_insertAt_sic;
   tests[130] = test_tsC_intBitsToDouble_i;
   tests[131] = test_tsC_longBitsToDouble_l;
   tests[132] = test_tsC_toDouble_s;
   tests[133] = test_tsC_toInt_s;
   tests[134] = test_tsC_toLong_s;
   tests[135] = test_tsC_toLowerCase_c;
   tests[136] = test_tsC_toString_c;
   tests[137] = test_tsC_toString_di;
   tests[138] = test_tsC_toString_i;
   tests[139] = test_tsC_toString_l;
   tests[140] = test_tsC_toString_si;
   tests[141] = test_tsC_toUpperCase_c;
   tests[142] = test_tsC_unsigned2hex_ii;
   tests[143] = test_tsT_update;
   tests[144] = test_tsV_arrayCopy_oioii;
   tests[145] = test_tsV_attachLibrary_s;
   tests[146] = test_tsV_clipboardPaste;
   tests[147] = test_tsV_debug_s;
   tests[148] = test_tsV_exec_ssib;
   tests[149] = test_tsV_exitAndReboot;
   tests[150] = test_tsV_getFile_s;
   tests[151] = test_tsV_getFreeMemory;
   tests[152] = test_tsV_getRemainingBattery;
   tests[153] = test_tsV_getStackTrace_t;
   tests[154] = test_tsV_getTimeStamp;
   tests[155] = test_tsV_interceptSpecialKeys_I;
   tests[156] = test_tsV_isKeyDown_i;
   tests[157] = test_tsV_privateAttachNativeLibrary_s;
   tests[158] = test_tsV_setAutoOff_b;
   tests[159] = test_tsV_setTime_t;
   tests[160] = test_tsV_sleep_i;
   tests[161] = test_tsV_tweak_ib;
   tests[162] = test_tuC_updateScreen;
   tests[163] = test_tuMW_exit_i;
   tests[164] = test_tuMW_getCommandLine;
   tests[165] = test_tuMW_setTimerInterval_i;
   tests[166] = test_tuW_pumpEvents;
   tests[167] = test_tuW_setSIP_icb;
   tests[168] = test_tueE_isAvailable;
   tests[169] = test_tufFM_charWidth_c;
   tests[170] = test_tufFM_stringWidth_Cii;
   tests[171] = test_tuiI_imageLoad_s;
   tests[172] = test_Graphics;
   tests[173] = test_tufF_FontTestCleanup_f;
   tests[174] = test_tuiI_imageParse_sB;
   tests[175] = test_tuiI_changeColors_ii;
   tests[176] = test_tuiI_getModifiedInstance_iiiiiii;
   tests[177] = test_tuiI_getPixelRow_Bi;
   tests[178] = test_tumMC_pause_b;
   tests[179] = test_tumMC_play_b;
   tests[180] = test_tumMC_stop;
   tests[181] = test_tumS_beep;
   tests[182] = test_tumS_setEnabled_b;
   tests[183] = test_tumS_tone_ii;
  // tests[184] = test_ZLib;
   tests[185] = test_XmlTokenizer;
   tests[186] = test_StringObject;
   tests[187] = test_VM_CodeUnion;
   tests[188] = test_VM_ADD_aru_regI_s6;
   tests[189] = test_VM_ADD_regD_regD_regD;
   tests[190] = test_VM_ADD_regI_aru_s6;
   tests[191] = test_VM_ADD_regI_arc_s6;
   tests[192] = test_VM_ADD_regI_regI_regI;
   tests[193] = test_VM_ADD_regI_regI_sym;
   tests[194] = test_VM_ADD_regI_s12_regI;
   tests[195] = test_VM_ADD_regL_regL_regL;
   tests[196] = test_VM_AND_regI_aru_s6;
   tests[197] = test_VM_AND_regI_regI_regI;
   tests[198] = test_VM_AND_regI_regI_s12;
   tests[199] = test_VM_AND_regL_regL_regL;
   tests[200] = test_VM_CHECKCAST;
   tests[201] = test_VM_CONV_regD_regI;
   tests[202] = test_VM_CONV_regD_regL;
   tests[203] = test_VM_CONV_regI_regD;
   tests[204] = test_VM_CONV_regI_regL;
   tests[205] = test_VM_CONV_regIb_regI;
   tests[206] = test_VM_CONV_regIc_regI;
   tests[207] = test_VM_CONV_regIs_regI;
   tests[208] = test_VM_CONV_regL_regD;
   tests[209] = test_VM_CONV_regL_regI;
   tests[210] = test_VM_DECJGEZ_regI;
   tests[211] = test_VM_DECJGTZ_regI;
   tests[212] = test_VM_DIV_regD_regD_regD;
   tests[213] = test_VM_DIV_regI_regI_regI;
   tests[214] = test_VM_DIV_regI_regI_s12;
   tests[215] = test_VM_DIV_regL_regL_regL;
   tests[216] = test_VM_INC_regI;
   tests[217] = test_VM_INSTANCEOF;
   tests[218] = test_VM_JEQ_regD_regD;
   tests[219] = test_VM_JEQ_regI_regI;
   tests[220] = test_VM_JEQ_regI_s6;
   tests[221] = test_VM_JEQ_regI_sym;
   tests[222] = test_VM_JEQ_regL_regL;
   tests[223] = test_VM_JEQ_regO_null;
   tests[224] = test_VM_JEQ_regO_regO;
   tests[225] = test_VM_JGE_regD_regD;
   tests[226] = test_VM_JGE_regI_arlen;
   tests[227] = test_VM_JGE_regI_regI;
   tests[228] = test_VM_JGE_regI_s6;
   tests[229] = test_VM_JGE_regL_regL;
   tests[230] = test_VM_JGT_regD_regD;
   tests[231] = test_VM_JGT_regI_regI;
   tests[232] = test_VM_JGT_regI_s6;
   tests[233] = test_VM_JGT_regL_regL;
   tests[234] = test_VM_JLE_regD_regD;
   tests[235] = test_VM_JLE_regI_regI;
   tests[236] = test_VM_JLE_regI_s6;
   tests[237] = test_VM_JLE_regL_regL;
   tests[238] = test_VM_JLT_regD_regD;
   tests[239] = test_VM_JLT_regI_regI;
   tests[240] = test_VM_JLT_regI_s6;
   tests[241] = test_VM_JLT_regL_regL;
   tests[242] = test_VM_JNE_regD_regD;
   tests[243] = test_VM_JNE_regI_regI;
   tests[244] = test_VM_JNE_regI_s6;
   tests[245] = test_VM_JNE_regI_sym;
   tests[246] = test_VM_JNE_regL_regL;
   tests[247] = test_VM_JNE_regO_null;
   tests[248] = test_VM_JNE_regO_regO;
   tests[249] = test_VM_MOD_regD_regD_regD;
   tests[250] = test_VM_MOD_regI_regI_regI;
   tests[251] = test_VM_MOD_regI_regI_s12;
   tests[252] = test_VM_MOD_regL_regL_regL;
   tests[253] = test_VM_MOV_arc_reg16;
   tests[254] = test_VM_MOV_aru_reg64;
   tests[255] = test_VM_MOV_arc_reg64;
   tests[256] = test_VM_MOV_aru_regI;
   tests[257] = test_VM_MOV_arc_regI;
   tests[258] = test_VM_MOV_aru_regIb;
   tests[259] = test_VM_MOV_arc_regIb;
   tests[260] = test_VM_MOV_aru_regO;
   tests[261] = test_VM_MOV_arc_regO;
   tests[262] = test_VM_MOV_aru_reg16;
   tests[263] = test_VM_MOV_field_reg64;
   tests[264] = test_VM_MOV_field_regI;
   tests[265] = test_VM_MOV_field_regO;
   tests[266] = test_VM_MOV_reg16_arc;
   tests[267] = test_VM_MOV_reg16_aru;
   tests[268] = test_VM_MOV_reg64_aru;
   tests[269] = test_VM_MOV_reg64_arc;
   tests[270] = test_VM_MOV_reg64_field;
   tests[271] = test_VM_MOV_reg64_reg64;
   tests[272] = test_VM_MOV_reg64_static;
   tests[273] = test_VM_MOV_regD_s18;
   tests[274] = test_VM_MOV_regD_sym;
   tests[275] = test_VM_MOV_regI_aru;
   tests[276] = test_VM_MOV_regI_arc;
   tests[277] = test_VM_MOV_regI_arlen;
   tests[278] = test_VM_MOV_regI_field;
   tests[279] = test_VM_MOV_regI_regI;
   tests[280] = test_VM_MOV_regI_s18;
   tests[281] = test_VM_MOV_regI_static;
   tests[282] = test_VM_MOV_regI_sym;
   tests[283] = test_VM_MOV_regIb_arc;
   tests[284] = test_VM_MOV_regIb_aru;
   tests[285] = test_VM_MOV_regL_s18;
   tests[286] = test_VM_MOV_regL_sym;
   tests[287] = test_VM_MOV_regO_aru;
   tests[288] = test_VM_MOV_regO_arc;
   tests[289] = test_VM_MOV_regO_field;
   tests[290] = test_VM_MOV_regO_null;
   tests[291] = test_VM_MOV_regO_regO;
   tests[292] = test_VM_MOV_static_regO;
   tests[293] = test_VM_MOV_regO_static;
   tests[294] = test_VM_MOV_regO_sym;
   tests[295] = test_VM_MOV_static_reg64;
   tests[296] = test_VM_MOV_static_regI;
   tests[297] = test_VM_MUL_regD_regD_regD;
   tests[298] = test_VM_MUL_regI_regI_regI;
   tests[299] = test_VM_MUL_regI_regI_s12;
   tests[300] = test_VM_MUL_regL_regL_regL;
   tests[301] = test_VM_NEWARRAY_len;
   tests[302] = test_VM_NEWARRAY_multi;
   tests[303] = test_VM_NEWARRAY_regI;
   tests[304] = test_VM_NEWOBJ;
   tests[305] = test_VM_OR_regI_regI_regI;
   tests[306] = test_VM_OR_regI_regI_s12;
   tests[307] = test_VM_OR_regL_regL_regL;
   tests[308] = test_VM_SHL_regI_regI_regI;
   tests[309] = test_VM_SHL_regI_regI_s12;
   tests[310] = test_VM_SHL_regL_regL_regL;
   tests[311] = test_VM_SHR_regI_regI_regI;
   tests[312] = test_VM_SHR_regI_regI_s12;
   tests[313] = test_VM_SHR_regL_regL_regL;
   tests[314] = test_VM_SUB_regD_regD_regD;
   tests[315] = test_VM_SUB_regI_regI_regI;
   tests[316] = test_VM_SUB_regI_s12_regI;
   tests[317] = test_VM_SUB_regL_regL_regL;
   tests[318] = test_VM_SWITCH;
   tests[319] = test_VM_TEST_regO;
   tests[320] = test_VM_THROW;
   tests[321] = test_VM_USHR_regI_regI_regI;
   tests[322] = test_VM_USHR_regI_regI_s12;
   tests[323] = test_VM_USHR_regL_regL_regL;
   tests[324] = test_VM_XOR_regI_regI_regI;
   tests[325] = test_VM_XOR_regI_regI_s12;
   tests[326] = test_VM_XOR_regL_regL_regL;
   tests[327] = test_VM_z0_JUMP_s24;
   tests[328] = test_VM_z1_JUMP_regI;
   tests[329] = test_VM_z2_RETURN_void;
   tests[330] = test_VM_z3_RETURN_reg64;
   tests[331] = test_VM_z3_RETURN_regI;
   tests[332] = test_VM_z3_RETURN_regO;
   tests[333] = test_VM_z4_RETURN_null;
   tests[334] = test_VM_z4_RETURN_s24D;
   tests[335] = test_VM_z4_RETURN_s24I;
   tests[336] = test_VM_z4_RETURN_s24L;
   tests[337] = test_VM_z5_RETURN_symD;
   tests[338] = test_VM_z5_RETURN_symI;
   tests[339] = test_VM_z5_RETURN_symL;
   tests[340] = test_VM_z5_RETURN_symO;
   tests[341] = test_VM_z6_CALL_normal;
   tests[342] = test_VM_z7_CALL_virtual;
   tests[343] = test__doubleToStr;
   tests[344] = test__str2double;
   tests[345] = test__str2int64;
   tests[346] = test_VM_Cleanup;
}

void startTestSuite(Context currentContext)
{
   struct TestSuite *tc = createTestSuite();
   int i;
   int from = 0;
   int to = TEST_COUNT-1;
   int lastNumberOfFail = 0; //How many tests have failed until now?
   int testsResults[TEST_COUNT] = { 0 }; // This flag vector indicates whether a given test had failed
   testFunc tests[TEST_COUNT];

   xmemzero(tests, sizeof(tests));
   fillTestCaseArray(tests);

   for (i = from; !tc->abort && i <= to; i++)
   {
      tc->total++;
      currentContext->thrownException = null;
      if (tests[i]) tests[i](tc, currentContext);
      if (tc->failed != lastNumberOfFail)
      {
         testsResults[i] = 1;
         lastNumberOfFail = tc->failed;
      }
   }
   showTestResults(testsResults);
}

#endif

