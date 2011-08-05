/*********************************************************************************
 *  TotalCross Software Development Kit - Litebase                               *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

/**
 * Defines all global variables used by Litebase.
 */

#include "LitebaseGlobals.h"

// Empty structures.
IntVector emptyIntVector; // Empty IntVector.
Hashtable emptyHashtable; // Empty hash table.

// Globas for driver creation.
Hashtable htCreatedDrivers; // The hash table for the created connections with Litebase.
Heap hashTablesHeap;        // The heap to allocate the reserved words and memory usage hash tables.

// Globals for the parser.
Hashtable reserved;                 // Table containing the reserved words.
Hashtable memoryUsage;              // Indicates how much memory a select sql command uses in its temporary .db.
uint8 is[256];                      // An array to help the selection of the kind of the token.
int8 function_x_datatype[10][7] = { // Matrix of data types which applies to the SQL functions.
      {FUNCTION_DT_UPPER, FUNCTION_DT_LOWER, FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },  
      {FUNCTION_DT_ABS  , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },     
      {FUNCTION_DT_ABS  , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },                   
      {FUNCTION_DT_ABS  , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },                  
      {FUNCTION_DT_ABS  , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },                  
      {FUNCTION_DT_ABS  , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },                  
      {FUNCTION_DT_UPPER, FUNCTION_DT_LOWER, FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },  
      {FUNCTION_DT_NONE , FUNCTION_DT_NONE , FUNCTION_DT_NONE, FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  },         
      {FUNCTION_DT_YEAR , FUNCTION_DT_MONTH, FUNCTION_DT_DAY,  FUNCTION_DT_NONE, FUNCTION_DT_NONE,   FUNCTION_DT_NONE,   FUNCTION_DT_NONE  }, 
      {FUNCTION_DT_YEAR , FUNCTION_DT_MONTH, FUNCTION_DT_DAY,  FUNCTION_DT_HOUR, FUNCTION_DT_MINUTE, FUNCTION_DT_SECOND, FUNCTION_DT_MILLIS}}; 

// An array with the names of the SQL data functions.     
CharP names[10] = {"year", "month", "day", "hour", "minute", "second", "millis", "abs", "upper", "lower"};

// Used to count bits in an index bitmap.
uint8 bitsInNibble[16] = {0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4};

YYSTYPE yylval; // Variable where a token is stored.
JChar questionMark[2] = {(JChar)'?', (JChar)'\0'}; // A jchar string representing "?".

uint8 yytranslate[] = // YYTRANSLATE[YYLEX] -- Bison symbol number corresponding to YYLEX.
{
   0,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,   
   2,  2,  2, 76, 77, 78,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2, 
   2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,     
   2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,     
   2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,     
   2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,     
   2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  1,  2,  3,     
   4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,    
  41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75
};

uint8 yyr1[] = // YYR1[YYN] -- Symbol number of symbol that rule YYN derives.
{
   0,  79,  80,  80,  80,  80,  80,  80,  80,  80,  80,  81,  82,  82,  83,  83,  84,  84,  85,  85,  85,  85,  85,  85,  85,  85,  85,  85,  86,  
  86,  87,  87,  88,  88,  89,  89,  89,  89,  90,  90,  91,  91,  92,  92,  93,  93,  94,  94,  95,  95,  95,  95,  95,  95,  95,  95,  96,  96,    
  96,  97,  97,  98,  98,  99,  99, 100, 101, 101, 101, 101, 102, 103, 103, 104, 104, 105, 106, 106, 107, 107, 107, 108, 108, 109, 109, 110, 111,   
 111, 112, 112, 113, 113, 114, 114, 115, 115, 116, 117, 117, 118, 118, 119, 120, 120, 120, 121, 121, 121, 121, 121, 122, 122, 122, 122, 122, 122,   
 122, 122, 123, 123, 123, 123, 123, 124, 124, 124, 124, 124, 124, 125, 125, 125, 125, 125, 125, 125, 125, 125, 125, 126, 126, 126, 126, 126
};

uint8 yyr2[] = // YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.
{
   0, 2, 7, 8, 3, 5, 8, 4, 5, 6, 8, 1, 0, 1, 1, 1, 1, 3, 5, 5, 5, 5, 5, 9, 5, 5, 7, 9, 0, 1, 0, 2, 0, 1, 0, 2, 2, 2, 0, 2, 0, 6, 0, 3, 1, 3, 1, 3, 1,
   1, 1, 1, 3, 3, 3, 3, 4, 6, 3, 0, 1, 0, 1, 1, 3, 3, 1, 1, 1, 1, 0, 1, 1, 1, 3, 2, 0, 2, 1, 1, 1, 1, 3, 1, 3, 3, 0, 1, 0, 2, 0, 4, 1, 3, 0, 2, 1, 0,     
   3, 1, 3, 2, 0, 1, 1, 3, 4, 1, 3, 3, 2, 3, 3, 4, 3, 4, 3, 4, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4
};

int16 yydefgoto[] = // YYDEFGOTO[NTERM-NUM]. 
{
   -1,   8, 119,  56,  27,  95,  96, 257, 183, 285, 231, 263, 142,  67, 289,  28, 214,  60,  92,  13, 124, 125, 224,  17,  48,  49,  50,  87,  51, 
  148, 120, 121, 171,  98, 218, 281, 292, 293, 254, 295, 296, 306, 149, 150, 151, 209, 152,  54
};
 
int16 yypact[] = // YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing STATE-NUM.
{
   163,  -38,    7,    3,   70,   61, -217,  104,  110,  104,  117,  104, -217,  104,   21,  104,  104,   83, -217,  124, -217,   19,   55,   53,  
   124, -217, -217,   84,  135, -217,   76,  146,   87,   90,   97,  101,  102,  103,  107,  108,  109,  112,  114,  115,  116,  121,  123, -217,   
   164,  186, -217,  183, -217, -217, -217, -217,  138,  143,  147,  204, -217,  104,  206,  136,  104,  208,  210,  142,  213,  215,  215,  145,   
   215,  215,  215,  215,  215,  215,  215,  215,  215,  215,  215,  215,  104,  209,  218, -217,  215,  179,  180, -217,  159,  155,  304,  225,  
  -217,  111, -217, -217, -217,   11,  156, -217,  173,  174,  194,  198,  200,  205,  211,  217,  219,  220,  222,  224,  229,  230,  231,  221,
     9, -217, -217, -217,   22, -217,  245,  181, -217,  282,  210,  233,  234,  226,  226,  226,  226,  226,  226,  226,  235,   26,  237, -217,  
  -217, -217,  166,  111, -217,   85, -217,  236, -217, -217,   28, -217, -217, -217, -217, -217, -217, -217, -217, -217, -217, -217, -217, -217,  
  -217, -217, -217,  124,  104,  248,  215, -217,   38,  210, -217,   12,  290,  311,  273,  291,  291,  291,  291,  291,  291,  291,  317,  277,  
  -217, -217,  249,  111, -217,   20,  111,  111, -217, -217, -217, -217, -217, -217,  119,   33,  278,  264, -217, -217, -217, -217,   13, -217,  
  -217,  306,  270, -217, -217, -217, -217, -217, -217,   14, -217,  330,  257, -217,   73,  280,  280,  280,  280,  280,  280,  280,  260,  265,
    23, -217, -217, -217,  285, -217, -217, -217,   41, -217,   45, -217,  209,  320, -217, -217, -217,  267,  293, -217, -217, -217,  288, -217,  
  -217, -217, -217, -217, -217, -217,  293,  343, -217, -217, -217, -217, -217, -217, -217, -217, -217,   92,  209,  280, -217,  226, -217,  226,  
  -217,   17,  209, -217, -217,  111,   89,  335, -217, -217,  291,  291,  347, -217, -217,   85, -217, -217, -217,  209,  280,  280, -217, -217,  
  -217, -217
};

int16 yypgoto[] = // YYPGOTO[NTERM-NUM]. 
{
   -217, -217,  132,  -15, -217, -217,  212, -217, -132,   81, -174, -216, -217, -217, -217,  -58, -217, -217, -217, -217, -217,  182, -217, -217, 
   -217, -217,  269, -217, -206,  -17, -217,  185, -217,   -2, -217, -217, -217, -217, -217, -217,   48, -217, -125,  -66,  149, -217,  -16, -217
};

int16 yycheck[] = // Check table to see if the lexer state is correct.
{
   17,  17, 134, 135, 136, 137, 138, 139,  66,  24, 184, 185, 186, 187, 188, 189, 232, 233, 234, 235, 236, 237, 147,  14,   3,  14,  14,  14,  14,     
    3,  68,  14,   4,   5,   6,  16,  14,   4,  18,   6,  37,  18,   4,   5,   6,   4, 252,   6,  41,   4,   5,   6,  69,  70,  35,  72,  73,  74,
   75,  76,  77,  78,  79,  80,  81,  82,  83, 283,  85,  85, 195,  88, 130, 198, 199,  68, 282,   4,   5,  59, 146,  62,  59,  74, 290,  57,   3,    
   61,  77,  77,  77,  77, 308, 309,  77,  57,  74,  77,  15,  78,  77, 307,  57,  18,  21,  44,  14,   3,  25,  20,   0,  41,  29,  58,   3,   4,     
    5,   6, 120, 177,   3,  32, 124,  40, 298, 299,  15,   3, 194,  76,  57,  39,  49,  50,  51,  52,  53,  54,  68,   7,  29,   9,  58,  11,  59,    
   13,  63,  15,  16,  14,  67,  40,  76, 285,  71, 287, 171, 174,  75,  13,  49,  78,  51,  76,  53,  54,  76,  56, 293,   3,   4,   5,   6,  76,    
   63,  56,  57,  76,  76,  76,  17,  15,  71,  76,  76,  76,  75,  76,  76,  26,  76,  76,  76,  61,  31,  29,  64,  76,  35,  76,  14,  37,  19,    
   65,  61,  42,  40,   3,  61,   3,  74,   3,   3,   3,  72,  49,   3,  51,   3,  53,  54,   3,  56,  78,  15,  46,  46,  64,  69,  63,  21,  76,    
   76,  70,  25, 252, 252,  71,  29,  14,  19,  75,  76,   7,   8,   9,  10,  11,  12,  40,  77,  77,   3,   4,   5,   6,  11,  76,  49,  50,  51,    
   52,  53,  54,  15, 282, 282,   3,   4,   5,   6,  77,  63, 290, 290,  77,  67,  77,  29,  15,  71,  45,  77,  47,  75,   3,  38,  61,  77,  40,
  307, 307,  56,  29,  77,   5,  77,  77,  49,  77,  51,  77,  53,  54,  40,  56,  77,  77,  77,  76,  76,  76,  63,  49,  77,  51,   5,  53,  54, 
   46,  71,  30,   5,  46,  75,  47,  22,  63,  24,  23,  60,  27,  28,   3,  77,  71,  56,  77,  34,  75,  36,  76,  57,  23,  77,  57,   3,  43,    
   55,  14,   3, 270,  48, 141,  85, 307, 174, 172, 209,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1,  66,  -1,  -1,  -1,  -1,  -1,  -1,    
   73
};

uint8 yystos[] = // YYSTOS[STATE-NUM] -- The (internal number of the) accessing symbol of state STATE-NUM.
{
    0,  17,  26,  31,  35,  42,  64,  70,  80,  68,  41,  68,  37,  98,  41,  68,  44, 102,   3,  81,   0,  81,   3,  81,  81,   3,  78,  83,  94,  
   81,  81,   3,  15,  21,  25,  29,  40,  49,  50,  51,  52,  53,  54,  63,  67,  71,  75,  78, 103, 104, 105, 107, 108, 125, 126,   3,  82,  16,    
   35,  62,  96,  58,  76,  82,  58,  14,  76,  92,  13,  76,  76,  76,  76,  76,  76,  76,  76,  76,  76,  76,  76,  76,  76,  76,  37,  14,  19,   
  106,  65,  61,  61,   3,  97,  81,   3,  84,  85,  74, 112,  81,   3,  94,  72,   3, 108, 108,  78, 108, 108, 108, 108, 108, 108, 108, 108, 108, 
  108, 108, 108,  81, 109, 110, 105,   3,  99, 100, 108,  46,  46,  69,  76,  22,  24,  27,  28,  34,  36,  43,  48,  66,  73,  14,  91,   4,   5,     
    6,  56,  76, 108, 121, 122, 123, 125,  77,  76,  77,  77,  77,  77,  77,  77,  77,  77,  77,  77,  77,  77,  77,  77,  77,  19, 111,  14, 112,    
   14, 112,  11,  76,   3,  94,  76,  76,  61,  87,  87,  87,  87,  87,  87,  87,  76,  61,  85,  77,  56,  76, 122, 121,  18,  59,   7,   8,   9,    
   10,  11,  12,  45,  47,  56, 124,   4,   5,   6,  57,  95,  82, 110,  38, 113, 100,   4,   5,   6,  57, 101,  94,  77,   5,   5,  46,  30,  89,    
   89,  89,  89,  89,  89,  89,   5,  46, 121,  77, 121, 121,  56,  57,   4,   6,  47, 123,  14,  77,  23,  60, 117,  77,   3,  86,  77,   4,   5,    
   57,  56,  90,  90,  90,  90,  90,  90,  90,  77,  76,  77,  57,   4,   6,   4,   5,   6,  57, 107, 114,  23,  77,  55,  88,  57,  88,   3,  93,
   14,  39, 115, 116, 107, 118, 119,  90,  87,  87,  14,  77, 107, 121,  20,  32, 120,  14,  89,  89,   3, 119,  90,  90
};


// YYDEFACT[STATE-NAME] -- Default rule to reduce with in state STATE-NUM when YYTABLE doesn't specify something else to do. Zero means the default 
// is an error. 
uint8 yydefact[] = 
{
   0,   0,   0,  61,   0,   0,  70,   0,   0,   0,   0,   0,  62,   0,   0,   0,   0,   0,  11,  12,   1,   0,   0,   0,  12,  46,  14,   0,  15,   
   4,  42,  81,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  71,   0,  72,  73,  76,  78,  79,  80,  13,   0,   0,  
   0,  59,   7,   0,   0,  88,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0, 
  75,   0,   0,   0,  60,   0,   0,   0,  40,  16,   0,   8,   5,  47,   0,   0,  82,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,  
   0,   0,   0,  86,  88,  83,  74,  77,  88,  63,   0,   0,  58,   0,   0,   0,   0,  30,  30,  30,  30,  30,  30,  30,   0,   0,   0, 120, 119,   
 121,   0,   0, 118,  89, 107,   0, 122,  43,   0, 129, 142, 139, 134, 135, 131, 140, 138, 141, 136, 133, 137, 143, 130, 132,  87,  12,   0,  90,     
   0,   9,   0,   0,  56,   0,   0,   0,   0,  34,  34,  34,  34,  34,  34,  34,   0,   0,  17,   2,   0,   0, 110,   0,   0,   0, 127, 128, 125,   
 126, 123, 124,   0,   0,   0,   0,  48,  49,  51,  50,   0,  85,  84,   0,  97,  64,  66,  67,  69,  68,  65,   0,   3,  28,   0,  31,   0,  38,    
  38,  38,  38,  38,  38,  38,   0,   0,   0, 105, 109, 108,   0, 112, 114, 116,   0, 111,   0,   6,   0,   0,  10,  57,  29,   0,  32,  35,  36,    
  37,   0,  24,  25,  22,  21,  19,  20,  18,  32,   0, 106, 113, 115, 117,  52,  53,  55,  54,  92,  94,   0,  38,  33,  30,  39,  30,  44,   0,
   0,  96,  91,   0, 102,  98,  99,  26,  34,  34,   0,  41,  93,  95, 103, 104, 101,   0,  38,  38,  45, 100,  23,  27
};

// YYTABLE[YYPACT[STATE-NUM]]. What to do in state STATE-NUM. If positive, shifts that token. If negative, reduces the rule which number is the 
// opposite. If zero, does what YYDEFACT says. If YYTABLE_NINF, syntax error.  
uint16 yytable[] =
{
    52,  53, 184, 185, 186, 187, 188, 189, 101,  63, 232, 233, 234, 235, 236, 237, 264, 265, 266, 267, 268, 269, 197, 172,  25,  65,  65, 250,  65,
    94,   9, 300, 210, 211, 212,  57, 174, 246, 198, 247,  12, 198, 220, 221, 222, 274, 280, 275,  10, 276, 277, 278, 104, 105,  58, 107, 108, 109,   
   110, 111, 112, 113, 114, 115, 116, 117, 118, 297,  52,  53, 240, 126, 179, 242, 243,  11, 294, 259, 260, 199, 196,  59, 199,  97, 302, 213,  31,   
   191, 153, 226, 251, 255, 312, 313, 301, 223,  97, 241,  32,  26, 272, 294, 279, 198,  33,  16, 290,  18,  34, 304,  20,  14,  35,  61,  31, 143,   
   144, 145, 173, 225,  22, 305, 175,  36, 308, 309,  32,  55, 196,  62, 261, 291,  37,  38,  39,  40,  41,  42,  15,  19,  35,  21,  64,  23, 199,
    24,  43,  29,  30,  65,  44,  36,  66, 298,  45, 299, 215, 126,  46,  68,  37,  47,  39,  69,  41,  42,  70, 146, 303,  31, 143, 144, 145,  71,
    43, 244, 245,  72,  73,  74,   1,  32,  45,  75,  76,  77,  46, 147,  78,   2,  79,  80,  81,  93,   3,  35,  99,  82,   4,  83,  85,  84,  86,    
    88,  89,   5,  36,  91,  90,  94,  97, 100,  31,  25, 102,  37, 103,  39,  31,  41,  42, 123, 194, 106,  32, 127, 128,   6, 129,  43,  33, 130,   
   154,   7,  34,  52,  53,  45,  35, 141, 170,  46, 195, 200, 201, 202, 203, 204, 205,  36, 155, 156,  31, 143, 144, 145, 176, 177,  37,  38,  39,  
    40,  41,  42,  32,  52,  53,  31, 143, 144, 145, 157,  43,  52,  53, 158,  44, 159,  35,  32,  45, 206, 160, 207,  46, 178, 217, 182, 161,  36,
    52,  53, 208,  35, 162, 227, 163, 164,  37, 165,  39, 166,  41,  42,  36, 194, 167, 168, 169, 180, 181, 190,  43,  37, 193,  39, 228,  41,  42,   
   229,  45, 230, 238, 239,  46, 248, 131,  43, 132, 252, 253, 133, 134, 256, 258,  45, 262, 270, 135,  46, 136, 271, 273, 282, 283, 286, 288, 137,   
   284, 307, 310, 287, 138, 192, 122, 311, 219, 216, 249,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0, 139,   0,   0,   0,   0,   0,   0,
   140
};

// Java methods called by Litebase.                                                                   
Method newFile;           // new File(String name, int mode, int slot)                 
Method loggerLog;         // Logger.log(int level, String message, boolean prependInfo)
Method loggerLogInfo;     // Logger.logInfo(StringBuffer message) // juliana@230_30
Method addOutputHandler;  // Logger.addOutputHandler()                                 
Method getLogger;         // Logger.getLogger()                                        
                                                                                       
// Classes used.                                                                       
Class litebaseConnectionClass; // LitebaseConnection                                   
Class loggerClass;             // Logger                                               
Class fileClass;               // File                                                 
Class throwableClass;          // Throwable
Class vectorClass;             // Vector

// Mutexes used.
DECLARE_MUTEX(parser); // Mutex for the parser.
DECLARE_MUTEX(log);    // Mutex for logging.

// rnovais@568_10 @570_1 juliana@226_5
// Aggregate functions table.
int8 aggregateFunctionsTypes[FUNCTION_AGG_SUM + 1] = {INT_TYPE, UNDEFINED_TYPE, UNDEFINED_TYPE, DOUBLE_TYPE, DOUBLE_TYPE};

// Data Type functions table.
int8 dataTypeFunctionsTypes[FUNCTION_DT_LOWER + 1] = {SHORT_TYPE, SHORT_TYPE, SHORT_TYPE, SHORT_TYPE, SHORT_TYPE, SHORT_TYPE, SHORT_TYPE, 
                                                                                                      UNDEFINED_TYPE, CHARS_TYPE, CHARS_TYPE};
// Number of days in a month. 
uint8 monthDays[12] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

// Each type size in the .db file.
uint8 typeSizes[11] = {4, 2, 4, 8, 4, 8, 4, -1, 4, 8, 4}; // rnovais@567_2: added more sizes.

CharP errorMsgs_en[TOTAL_ERRORS]; // English error messages.
CharP errorMsgs_pt[TOTAL_ERRORS]; // Portuguese error messages.

// juliana@220_4: added a crc32 code for every record. Please update your tables.
int32 crcTable[CRC32_SIZE]; // The crc32 table used to calculate a crc32 for a record.

// TotalCross functions used by Litebase.
CharP2JCharPFunc TC_CharP2JCharP;
CharP2JCharPBufFunc TC_CharP2JCharPBuf;
CharPToLowerFunc TC_CharPToLower;
JCharP2CharPFunc TC_JCharP2CharP;
JCharP2CharPBufFunc TC_JCharP2CharPBuf;
JCharPEqualsJCharPFunc TC_JCharPEqualsJCharP;
JCharPEqualsIgnoreCaseJCharPFunc TC_JCharPEqualsIgnoreCaseJCharP;
JCharPHashCodeFunc TC_JCharPHashCode;
JCharPIndexOfJCharFunc TC_JCharPIndexOfJChar;
JCharPLenFunc TC_JCharPLen;
JCharToLowerFunc TC_JCharToLower;
JCharToUpperFunc TC_JCharToUpper;
alertFunc TC_alert;
appendCharPFunc TC_appendCharP; // juliana@230_30
appendJCharPFunc TC_appendJCharP; // juliana@230_30
areClassesCompatibleFunc TC_areClassesCompatible;
createArrayObjectFunc TC_createArrayObject;
createObjectFunc TC_createObject;
createObjectWithoutCallingDefaultConstructorFunc TC_createObjectWithoutCallingDefaultConstructor;
createStringObjectFromCharPFunc TC_createStringObjectFromCharP;
createStringObjectWithLenFunc TC_createStringObjectWithLen;
debugFunc TC_debug;
double2strFunc TC_double2str;
executeMethodFunc TC_executeMethod;
getApplicationIdFunc TC_getApplicationId;
getAppPathFunc TC_getAppPath;
getDataPathFunc TC_getDataPath;
getDateTimeFunc TC_getDateTime;
getErrorMessageFunc TC_getErrorMessage;
getMethodFunc TC_getMethod;
getProcAddressFunc TC_getProcAddress;
getSettingsPtrFunc TC_getSettingsPtr;
getTimeStampFunc TC_getTimeStamp;
hashCodeFunc TC_hashCode;
hashCodeFmtFunc TC_hashCodeFmt;
heapAllocFunc TC_heapAlloc;
heapDestroyPrivateFunc TC_heapDestroyPrivate;
hstrdupFunc TC_hstrdup;
htFreeFunc TC_htFree;
htFreeContextFunc TC_htFreeContext; 
htGet32Func TC_htGet32;
htGet32InvFunc TC_htGet32Inv;
htGetPtrFunc TC_htGetPtr;
htNewFunc TC_htNew;
htPut32Func TC_htPut32;
htPut32IfNewFunc TC_htPut32IfNew;
htPutPtrFunc TC_htPutPtr;
htRemoveFunc TC_htRemove;
int2CRIDFunc TC_int2CRID;
int2strFunc TC_int2str;
listFilesFunc TC_listFiles;
loadClassFunc TC_loadClass;
long2strFunc TC_long2str;
privateHeapCreateFunc TC_privateHeapCreate;
privateHeapSetJumpFunc TC_privateHeapSetJump;
privateXfreeFunc TC_privateXfree;
privateXmallocFunc TC_privateXmalloc;
privateXreallocFunc TC_privateXrealloc;
setObjectLockFunc TC_setObjectLock;
str2doubleFunc TC_str2double;
str2intFunc TC_str2int;
str2longFunc TC_str2long;
throwExceptionNamedFunc TC_throwExceptionNamed;
throwNullArgumentExceptionFunc TC_throwNullArgumentException;
toLowerFunc TC_toLower;
traceFunc TC_trace;
validatePathFunc TC_validatePath; // juliana@214_1
#ifdef PALMOS
getLastVolumeFunc TC_getLastVolume;
#endif
#ifdef ENABLE_MEMORY_TEST
getCountToReturnNullFunc TC_getCountToReturnNull;
setCountToReturnNullFunc TC_setCountToReturnNull;
#endif
