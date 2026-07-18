// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_aot.h"
#include "tcir_frontend.h"

#include <stdio.h>
#include <string.h>

#define REQUIRE(condition) \
   do \
   { \
      if (!(condition)) \
      { \
         fprintf(stderr, "requirement failed at %s:%d: %s\n", __FILE__, __LINE__, #condition); \
         return 0; \
      } \
   } while (0)

typedef struct TCIRConverterFixture
{
   const char *identity;
   const unsigned int *code;
   const int *lines;
   size_t code_count;
   unsigned int i32_count;
   unsigned int ref_count;
   unsigned int v64_count;
   unsigned int parameter_count;
   const TCIRMethodParameter *parameters;
   TCIRType return_type;
   const TCIRType *v64_home_types;
} TCIRConverterFixture;

#include "fixtures/tcir_converter_fixtures.h"

static int buildFixtureView(
   const TCIRConverterFixture *fixture,
   TCIRMethodView *view,
   TCIRMethodParameter *parameters)
{
   static const int constants[] = { 0 };
   (void)parameters;
   memset(view, 0, sizeof(*view));
   view->identity = fixture->identity;
   view->code = fixture->code;
   view->code_slot_count = fixture->code_count;
   view->i32_home_count = fixture->i32_count;
   view->ref_home_count = fixture->ref_count;
   view->v64_home_count = fixture->v64_count;
   view->v64_home_types = fixture->v64_home_types;
   view->parameters = fixture->parameters;
   view->parameter_count = fixture->parameter_count;
   view->return_type = fixture->return_type;
   view->i32_constants = constants;
   view->i32_constant_count = sizeof(constants) / sizeof(constants[0]);
   view->source_lines = fixture->lines;
   view->resolve_call_shape = tcirResolveConverterFixtureCall;
   view->resolve_call_shape_user_data = (void *)fixture;
   return 1;
}

static int buildFixtureFunctions(
   TCIRModule *module,
   TCIRFunction **functions,
   TCIRDiagnostic *diagnostic)
{
   size_t index;
   for (index = 0U; index < TCIR_CONVERTER_FIXTURE_COUNT; ++index)
   {
      TCIRMethodParameter parameters[2];
      TCIRMethodView view;
      if (!buildFixtureView(&tcir_converter_fixtures[index], &view, parameters)
          || tcirFrontendBuildFunction(module, &view, &functions[index], diagnostic) != TCIR_FRONTEND_OK)
         return 0;
   }
   return 1;
}

static TCIRFunction *buildUnsupportedNullCheck(TCIRModule *module, TCIRDiagnostic *diagnostic)
{
   static const unsigned int code[] = { 0x0000007aU, 0x00000088U };
   TCIRMethodParameter parameter;
   TCIRMethodView view;
   TCIRFunction *function = NULL;
   memset(&parameter, 0, sizeof(parameter));
   parameter.type = TCIR_TYPE_REF;
   parameter.home_bank = TCIR_HOME_REF;
   memset(&view, 0, sizeof(view));
   view.identity = "fixtures.TCIRPoc.checkedRef:(Ljava/lang/Object;)V";
   view.code = code;
   view.code_slot_count = sizeof(code) / sizeof(code[0]);
   view.ref_home_count = 1U;
   view.parameters = &parameter;
   view.parameter_count = 1U;
   view.return_type = TCIR_TYPE_VOID;
   return tcirFrontendBuildFunction(module, &view, &function, diagnostic) == TCIR_FRONTEND_OK
      ? function : NULL;
}

static TCCompiledStatus fakeEntry(TCCompiledFrame *frame, TCCompiledResult *result)
{
   (void)frame;
   (void)result;
   return TC_COMPILED_RETURNED;
}

static int testDeterministicGeneration(void)
{
   TCIRDiagnostic ir_diagnostic;
   TCIRAotDiagnostic aot_diagnostic;
   TCIRAotGenerateOptions options;
   TCIRAotOutput first;
   TCIRAotOutput second;
   TCIRModule *module = tcirModuleCreate(NULL, &ir_diagnostic);
   TCIRFunction *functions[TCIR_CONVERTER_FIXTURE_COUNT];
   const TCIRFunction *forward[TCIR_CONVERTER_FIXTURE_COUNT];
   const TCIRFunction *reverse[TCIR_CONVERTER_FIXTURE_COUNT];
   const char *abs_position;
   const char *add_position;
   size_t index;

   REQUIRE(module != NULL);
   REQUIRE(buildFixtureFunctions(module, functions, &ir_diagnostic));
   for (index = 0U; index < TCIR_CONVERTER_FIXTURE_COUNT; ++index)
      forward[index] = functions[index];
   for (index = 0U; index < TCIR_CONVERTER_FIXTURE_COUNT; ++index)
      reverse[index] = functions[TCIR_CONVERTER_FIXTURE_COUNT - index - 1U];
   options.target_options = "host-c11-test";
   REQUIRE(tcirAotGenerate(forward, TCIR_CONVERTER_FIXTURE_COUNT, &options, &first, &aot_diagnostic)
           == TCIR_AOT_GENERATE_READY);
   REQUIRE(tcirAotGenerate(reverse, TCIR_CONVERTER_FIXTURE_COUNT, &options, &second, &aot_diagnostic)
           == TCIR_AOT_GENERATE_READY);
   REQUIRE(first.source_size == second.source_size);
   REQUIRE(first.header_size == second.header_size);
   REQUIRE(first.manifest_size == second.manifest_size);
   REQUIRE(memcmp(first.source, second.source, first.source_size) == 0);
   REQUIRE(memcmp(first.header, second.header, first.header_size) == 0);
   REQUIRE(memcmp(first.manifest, second.manifest, first.manifest_size) == 0);
   REQUIRE(strcmp(first.input_hash, second.input_hash) == 0);
   REQUIRE(strstr(first.manifest, "\"generator\":\"tcir-portable-c\"") != NULL);
   REQUIRE(strstr(first.manifest, "\"ir_version\":1") != NULL);
   REQUIRE(strstr(first.manifest, "\"runtime_abi_version\":4") != NULL);
   REQUIRE(strstr(first.manifest, "\"rejected_methods\":[]") != NULL);
   abs_position = strstr(first.manifest, "fixtures.TCIRPoc.abs:(I)I");
   add_position = strstr(first.manifest, "fixtures.TCIRPoc.add:(II)I");
   REQUIRE(abs_position != NULL && add_position != NULL && abs_position < add_position);
   tcirAotOutputDestroy(&second);
   tcirAotOutputDestroy(&first);
   tcirModuleDestroy(module);
   return 1;
}

static int testChangedInputChangesIdentity(void)
{
   static const unsigned int changed_add_code[] = { 0x01000037U, 0x00000085U };
   TCIRConverterFixture changed_fixture = tcir_converter_fixtures[0];
   TCIRMethodParameter parameters[2];
   TCIRMethodView view;
   TCIRDiagnostic ir_diagnostic;
   TCIRAotDiagnostic aot_diagnostic;
   TCIRAotOutput original;
   TCIRAotOutput changed;
   TCIRModule *module = tcirModuleCreate(NULL, &ir_diagnostic);
   TCIRFunction *functions[TCIR_CONVERTER_FIXTURE_COUNT];
   TCIRFunction *changed_function;
   const TCIRFunction *original_input[1];
   const TCIRFunction *changed_input[1];

   REQUIRE(module != NULL);
   REQUIRE(buildFixtureFunctions(module, functions, &ir_diagnostic));
   changed_fixture.code = changed_add_code;
   REQUIRE(buildFixtureView(&changed_fixture, &view, parameters));
   REQUIRE(tcirFrontendBuildFunction(module, &view, &changed_function, &ir_diagnostic) == TCIR_FRONTEND_OK);
   original_input[0] = functions[0];
   changed_input[0] = changed_function;
   REQUIRE(tcirAotGenerate(original_input, 1U, NULL, &original, &aot_diagnostic)
           == TCIR_AOT_GENERATE_READY);
   REQUIRE(tcirAotGenerate(changed_input, 1U, NULL, &changed, &aot_diagnostic)
           == TCIR_AOT_GENERATE_READY);
   REQUIRE(strcmp(original.input_hash, changed.input_hash) != 0);
   REQUIRE(strcmp(original.source, changed.source) != 0);
   tcirAotOutputDestroy(&changed);
   tcirAotOutputDestroy(&original);
   tcirModuleDestroy(module);
   return 1;
}

static int testUnsupportedRejectedBeforeOutput(void)
{
   TCIRDiagnostic ir_diagnostic;
   TCIRAotDiagnostic aot_diagnostic;
   TCIRAotOutput output;
   TCIRModule *module = tcirModuleCreate(NULL, &ir_diagnostic);
   TCIRFunction *function;
   const TCIRFunction *input[1];

   REQUIRE(module != NULL);
   function = buildUnsupportedNullCheck(module, &ir_diagnostic);
   REQUIRE(function != NULL && tcirVerifyFunction(function, &ir_diagnostic));
   input[0] = function;
   memset(&output, 0xa5, sizeof(output));
   REQUIRE(tcirAotGenerate(input, 1U, NULL, &output, &aot_diagnostic)
           == TCIR_AOT_GENERATE_INELIGIBLE);
   REQUIRE(aot_diagnostic.code == TCIR_AOT_DIAGNOSTIC_INELIGIBLE_OPERATION);
   REQUIRE(output.source == NULL && output.header == NULL && output.manifest == NULL);
   tcirModuleDestroy(module);
   return 1;
}

static int testRegistrationRequiresExactIdentity(void)
{
   const TCIRAotRegistryEntry entries[] = {
      { "fixtures.TCIRPoc", "add", "(II)I", "0123456789abcdef", fakeEntry }
   };
   REQUIRE(tcirAotRegistryFind(entries, 1U, "fixtures.TCIRPoc", "add", "(II)I",
                               "0123456789abcdef") == &entries[0]);
   REQUIRE(tcirAotRegistryFind(entries, 1U, "fixtures.TCIRPoc", "abs", "(II)I",
                               "0123456789abcdef") == NULL);
   REQUIRE(tcirAotRegistryFind(entries, 1U, "fixtures.TCIRPoc", "add", "(I)I",
                               "0123456789abcdef") == NULL);
   REQUIRE(tcirAotRegistryFind(entries, 1U, "fixtures.TCIRPoc", "add", "(II)I",
                               "fedcba9876543210") == NULL);
   return 1;
}

int main(void)
{
   if (!testDeterministicGeneration()
       || !testChangedInputChangesIdentity()
       || !testUnsupportedRejectedBeforeOutput()
       || !testRegistrationRequiresExactIdentity())
      return 1;
   printf("TCIR portable-C generator tests passed: deterministic output, identity invalidation, "
          "pre-emission rejection, and exact registration.\n");
   return 0;
}
