# Copyright (C) 2026 Amalgam Solucoes em TI Ltda
#
# SPDX-License-Identifier: LGPL-2.1-only

if(NOT DEFINED TCIR_AOT_TOOL OR NOT EXISTS "${TCIR_AOT_TOOL}")
  message(FATAL_ERROR "TCIR_AOT_TOOL must name the built tcaot executable")
endif()
if(NOT DEFINED TCIR_AOT_TEST_DIR OR NOT IS_ABSOLUTE "${TCIR_AOT_TEST_DIR}")
  message(FATAL_ERROR "TCIR_AOT_TEST_DIR must be an absolute build-directory path")
endif()

set(TCIR_AOT_FIRST_DIR "${TCIR_AOT_TEST_DIR}/first")
set(TCIR_AOT_SECOND_DIR "${TCIR_AOT_TEST_DIR}/second")
file(REMOVE_RECURSE "${TCIR_AOT_FIRST_DIR}" "${TCIR_AOT_SECOND_DIR}")
file(MAKE_DIRECTORY "${TCIR_AOT_FIRST_DIR}" "${TCIR_AOT_SECOND_DIR}")

foreach(TCIR_AOT_OUTPUT_DIR IN ITEMS "${TCIR_AOT_FIRST_DIR}" "${TCIR_AOT_SECOND_DIR}")
  execute_process(
    COMMAND "${TCIR_AOT_TOOL}"
      --input poc-fixtures
      --output "${TCIR_AOT_OUTPUT_DIR}"
      --manifest "${TCIR_AOT_OUTPUT_DIR}/manifest.json"
      --target-options determinism-test
    RESULT_VARIABLE TCIR_AOT_GENERATE_RESULT
    OUTPUT_QUIET
    ERROR_VARIABLE TCIR_AOT_GENERATE_ERROR
  )
  if(NOT TCIR_AOT_GENERATE_RESULT EQUAL 0)
    message(FATAL_ERROR "tcaot clean generation failed: ${TCIR_AOT_GENERATE_ERROR}")
  endif()
endforeach()

foreach(TCIR_AOT_FILE IN ITEMS tcir_aot_generated.c tcir_aot_generated.h manifest.json)
  execute_process(
    COMMAND "${CMAKE_COMMAND}" -E compare_files
      "${TCIR_AOT_FIRST_DIR}/${TCIR_AOT_FILE}"
      "${TCIR_AOT_SECOND_DIR}/${TCIR_AOT_FILE}"
    RESULT_VARIABLE TCIR_AOT_COMPARE_RESULT
  )
  if(NOT TCIR_AOT_COMPARE_RESULT EQUAL 0)
    message(FATAL_ERROR "portable-C output is not deterministic: ${TCIR_AOT_FILE}")
  endif()
endforeach()

message(STATUS "TCIR AOT clean generations are byte-for-byte identical")
