/*********************************************************************************
 *  TotalCross Virtual Machine, version 1                                        *
 *  Copyright (C) 2007-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *********************************************************************************/


/**
 * This file is a helper tool that will be deployed with the root setuid enabled to perform operations requiring root permissions as
 * described in :
 *     http://developer.apple.com/DOCUMENTATION/Security/Conceptual/Security_Overview/Security_Services/chapter_4_section_5.html
 *   & http://developer.apple.com/DOCUMENTATION/Security/Conceptual/Security_Overview/Concepts/chapter_3_section_9.html
 */

#if HAVE_CONFIG_H
#include "config.h"
#endif

#import <Foundation/Foundation.h>
#import <Foundation/NSThread.h>
#include <stdio.h>
#include <dlfcn.h>
#include <libxml/tree.h>
#include <libxml/parser.h>
#include <libxml/xpath.h>
#include <libxml/xpathInternals.h>
#include <libxml/xmlsave.h>

#include "xtypes.h"

#ifndef DEBUG
#define VERBOSE(x)
#else
#define VERBOSE(x) _debug x

void _debug(const char *format, ...)
{
   FILE *lout = fopen("/tmp/tcpriv.out", "a+");
   if (lout)
   {
	  va_list va;
	  va_start(va, format);
	  vfprintf(lout, format, va);
	  va_end(va);
	  if (format[strlen(format)-1] != '\n')
	     fprintf(lout, "\n");
      fclose(lout);
   }
}
#endif

enum
{
   STATUS_OK,
   STATUS_UNKNOWN_ERROR,
};

int setGPRS(TCHARP apn, TCHARP username, TCHARP password)
{
   static bool libxml2_initialized;
   
   VERBOSE(("setGPRS(%s,%s,%s)", apn, username, password));
    
   if (!libxml2_initialized)
   {
	  /* Init libxml */
	  xmlInitParser();
	  LIBXML_TEST_VERSION
	  libxml2_initialized = true;
   }

   const char *prefs_file = "/Library/Preferences/SystemConfiguration/preferences.plist";
    
   /* Load XML document */
   xmlDocPtr doc = xmlParseFile(prefs_file);
   if (doc == NULL) 
   {
	  VERBOSE(("Error: unable to parse file '%s'", "file"));
	  return STATUS_UNKNOWN_ERROR;
   }
    
   xmlXPathContextPtr xpathCtx; 
   xmlXPathObjectPtr xpathObj; 
    
   xmlChar *xpathExpr = BAD_CAST "//dict[key='DeviceName' and string='ip1']/parent::*//dict[key='apn']/child::*";
   VERBOSE(("xpath='%s'", xpathExpr));
	
   /* Create xpath evaluation context */
   xpathCtx = xmlXPathNewContext(doc);
   if (xpathCtx == NULL) 
   {
      VERBOSE(("Error: unable to create new XPath context"));
      xmlFreeDoc(doc); 
	  return STATUS_UNKNOWN_ERROR;
   }

   /* Evaluate xpath expression */
   xpathObj = xmlXPathEvalExpression(xpathExpr, xpathCtx);
   if (xpathObj == NULL)
   {
      VERBOSE(("Error: unable to evaluate xpath expression '%s'", xpathExpr));
      xmlXPathFreeContext(xpathCtx); 
      xmlFreeDoc(doc); 
      return STATUS_UNKNOWN_ERROR;
   }

   xmlNodeSetPtr nodes = xpathObj->nodesetval;

   int changes = 0; // basic check
   
   if (!xmlXPathNodeSetIsEmpty(nodes) && nodes->nodeNr > 0)
   {
      int i;
      for (i = 0; i < nodes->nodeNr; i++)
      {
         xmlNodePtr cur = nodes->nodeTab[i];
		 VERBOSE(("node: %d name=%s, content=%s", i, cur->name, xmlNodeGetContent(cur)));
		 if (xmlStrEqual(cur->name, BAD_CAST "key"))
		 {
            if (xmlStrEqual(xmlNodeGetContent(cur), BAD_CAST "apn"))
			{
			   if (++i < nodes->nodeNr)
			   {
			      cur = nodes->nodeTab[i];
			      xmlNodeSetContent(cur, BAD_CAST apn);
			      changes++;
			   }
			} 
			else if (xmlStrEqual(xmlNodeGetContent(cur), BAD_CAST "password"))
			{
			   if (++i < nodes->nodeNr)
			   {
			      cur = nodes->nodeTab[i];
			      xmlNodeSetContent(cur, BAD_CAST password);
			      changes++;
			   }
			} 
			else if (xmlStrEqual(xmlNodeGetContent(cur), BAD_CAST "username"))
			{
			   if (++i < nodes->nodeNr)
			   {
				  cur = nodes->nodeTab[i];
				  xmlNodeSetContent(cur, BAD_CAST username);
			      changes++;
			   }
			}
		 } 
      }
   }

   if (changes != 3)
   {    
      VERBOSE(("Error: corrupted preferences.plist file, won't commit"));
      return STATUS_UNKNOWN_ERROR;
   }
      
   VERBOSE(("commit changes to '%s'", prefs_file)); 
   xmlSaveCtxtPtr savectx = xmlSaveToFilename(prefs_file, NULL, 0);  // requires root setuid
   //xmlSaveCtxtPtr savectx = xmlSaveToFilename("/tmp/preferences.plist", NULL, 0);
   xmlSaveDoc(savectx, doc);
   xmlSaveFlush(savectx);
   xmlSaveClose(savectx);

   /* Cleanup */
   xmlXPathFreeObject(xpathObj);
   xmlXPathFreeContext(xpathCtx); 
   xmlFreeDoc(doc); 

   return STATUS_OK;
}

int main(int argc, char *argv[])
{
   int status = -1;
   if (--argc >= 1)
   {
      argc--;
      argv++;
      VERBOSE(("command is '%s'", *argv));
      if (strcmp(*argv, "SET_GPRS") == 0)
      {
         argv++;
         if (argc == 3)
         {
			status = setGPRS(argv[0], argv[1], argv[2]);
         }
         else
            VERBOSE(("missing args"));
      }
      else
         VERBOSE(("unknown command"));
   }
   else
      VERBOSE(("no command"));
   VERBOSE(("exit status = %d", status));
   
   printf("%d\n", status);
   return status;
}
