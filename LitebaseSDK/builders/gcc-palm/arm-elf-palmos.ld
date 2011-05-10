/* Script for -z combreloc: combine and sort reloc sections */
OUTPUT_FORMAT ("elf32-littlearm", "elf32-bigarm",
               "elf32-littlearm")
OUTPUT_ARCH ("arm")

/*SEARCH_DIR("/usr/arm-palmos/lib");*/

/* This is a pathetically simple linker script that is only of use for
   building Palm OS 5 armlets, namely stand-alone code that has no global
   data or other complications.  */

SECTIONS
{
    .text :
    {
        *(.text)
        *(.rodata)
        *(.glue_7t) *(.glue_7)
    }
    .rodata.str1.4 :
    {
        *(.rodata)
    }
    /* see http://prc-tools.sourceforge.net/doc/prc-tools_3.html#SEC22 for details 
       WARNING: some tools like "peal" may discard this section causing troubles with undefined symbols!
    */
    .disposn : 
	 { 
        *(.disposn)
	 }
    .bss :
    {
        *(.bss)
        *(COMMON)
	     end = ALIGN(0x8);
	 }
}
