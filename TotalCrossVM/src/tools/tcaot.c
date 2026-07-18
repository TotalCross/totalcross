// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_aot.h"
#include "tcir_frontend.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

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

typedef struct TCIRToolOptions
{
   const char *input;
   const char *output_directory;
   const char *manifest_path;
   const char *target_options;
} TCIRToolOptions;

static void usage(const char *program)
{
   fprintf(stderr,
      "usage: %s --input poc-fixtures --output <build-directory> "
      "--manifest <manifest.json> [--target-options <description>]\n",
      program);
}

static int parseOptions(int argument_count, char **arguments, TCIRToolOptions *options)
{
   int index;
   memset(options, 0, sizeof(*options));
   options->target_options = "portable-c11-host-poc";
   for (index = 1; index < argument_count; ++index)
   {
      const char *name = arguments[index];
      const char **destination = NULL;
      if (strcmp(name, "--input") == 0)
         destination = &options->input;
      else if (strcmp(name, "--output") == 0)
         destination = &options->output_directory;
      else if (strcmp(name, "--manifest") == 0)
         destination = &options->manifest_path;
      else if (strcmp(name, "--target-options") == 0)
         destination = &options->target_options;
      else
         return 0;
      if (index + 1 >= argument_count)
         return 0;
      *destination = arguments[++index];
   }
   return options->input != NULL
      && strcmp(options->input, "poc-fixtures") == 0
      && options->output_directory != NULL
      && options->manifest_path != NULL;
}

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

static int writeFile(const char *path, const char *contents, size_t size)
{
   FILE *output = fopen(path, "wb");
   int accepted;
   int close_status;
   if (output == NULL)
      return 0;
   accepted = fwrite(contents, 1U, size, output) == size;
   close_status = fclose(output);
   return accepted && close_status == 0;
}

static int makeOutputPath(char *path, size_t capacity, const char *directory, const char *name)
{
   int length = snprintf(path, capacity, "%s/%s", directory, name);
   return length >= 0 && (size_t)length < capacity;
}

int main(int argument_count, char **arguments)
{
   TCIRToolOptions tool_options;
   TCIRAotGenerateOptions generate_options;
   TCIRAotDiagnostic aot_diagnostic;
   TCIRAotOutput output;
   TCIRDiagnostic ir_diagnostic;
   TCIRModule *module;
   TCIRFunction *functions[TCIR_CONVERTER_FIXTURE_COUNT];
   const TCIRFunction *ordered_functions[TCIR_CONVERTER_FIXTURE_COUNT];
   char source_path[1024];
   char header_path[1024];
   size_t index;
   int accepted = 0;

   if (!parseOptions(argument_count, arguments, &tool_options))
   {
      usage(arguments[0]);
      return 2;
   }
   module = tcirModuleCreate(NULL, &ir_diagnostic);
   if (module == NULL)
   {
      fprintf(stderr, "tcaot: unable to create the TCIR module: %s\n", ir_diagnostic.message);
      return 1;
   }
   memset(functions, 0, sizeof(functions));
   for (index = 0U; index < sizeof(functions) / sizeof(functions[0]); ++index)
   {
      TCIRMethodParameter parameters[2];
      TCIRMethodView view;
      if (!buildFixtureView(&tcir_converter_fixtures[index], &view, parameters)
          || tcirFrontendBuildFunction(module, &view, &functions[index], &ir_diagnostic) != TCIR_FRONTEND_OK)
      {
         fprintf(stderr, "tcaot: frontend rejected %s: %s\n",
                 tcir_converter_fixtures[index].identity, ir_diagnostic.message);
         goto cleanup;
      }
      ordered_functions[index] = functions[index];
   }
   generate_options.target_options = tool_options.target_options;
   if (tcirAotGenerate(ordered_functions, TCIR_CONVERTER_FIXTURE_COUNT,
                       &generate_options, &output, &aot_diagnostic)
       != TCIR_AOT_GENERATE_READY)
   {
      fprintf(stderr, "tcaot: %s for %s: %s\n",
              tcirAotDiagnosticCodeName(aot_diagnostic.code),
              aot_diagnostic.function,
              aot_diagnostic.message);
      goto cleanup;
   }
   if (!makeOutputPath(source_path, sizeof(source_path), tool_options.output_directory,
                       "tcir_aot_generated.c")
       || !makeOutputPath(header_path, sizeof(header_path), tool_options.output_directory,
                          "tcir_aot_generated.h")
       || !writeFile(source_path, output.source, output.source_size)
       || !writeFile(header_path, output.header, output.header_size)
       || !writeFile(tool_options.manifest_path, output.manifest, output.manifest_size))
   {
      fprintf(stderr, "tcaot: unable to write generated output under %s\n", tool_options.output_directory);
      tcirAotOutputDestroy(&output);
      goto cleanup;
   }
   printf("tcaot generated %lu verified methods with input hash %s under %s.\n",
          (unsigned long)TCIR_CONVERTER_FIXTURE_COUNT,
          output.input_hash,
          tool_options.output_directory);
   tcirAotOutputDestroy(&output);
   accepted = 1;

cleanup:
   tcirModuleDestroy(module);
   return accepted ? 0 : 1;
}
