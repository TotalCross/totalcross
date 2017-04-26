#!/usr/bin/perl

my $progname = $0;
$progname =~ s/.*\/([^\/]*)/\1/;

#
# On the iPhone, any software crash implies the creation of a crashdump on the device in the folder
# "/Library/Logs/CrashReporter/". This tool generates a stacktrace with symbols using the iphone binaries
# especially the TC libtcvm.dylib that must be located in the folder "iphonebindir". Fix the below path if needed.
# Without args, the tool retrieves the latest crash from the device.
# mailto:frank@superwaba.com.br
#

my $iphonebindir = "$ENV{HOME}/workspace/TotalCrossVM/builders/gcc-posix/tcvm/iphone";
my $litebasebindir = "$ENV{HOME}/workspace/LitebaseSDK_tc/builders/gcc/iphone";

my $crashFile = "__crash.log";
my $pattern = "Crashed";
my $nextarg = 0;

if ($#ARGV >= 0)
{
    if ($ARGV[0] eq "-help")
    {
        print "$progname [-highlighted|-crashed|-help] [crashdump]\n";
        exit;
    }
    if ($ARGV[0] eq "-highlighted")
    {
        $pattern = "Highlighted";
        $nextarg++;
    }
    if ($ARGV[0] eq "-crashed")
    {
        $pattern = "Crashed";
        $nextarg++;
    }
}

if ($#ARGV < $nextarg)
{
   system("scp root\@i:/Library/Logs/CrashReporter/LatestCrash.plist $crashFile") == 0 or die "cannot copy crash log";
}
else
{
   $crashFile = $ARGV[$nextarg];
}

sub cvthex
{
   my ($dec) = @_;
   my $hex = sprintf("%x", $dec);
   return $hex;
}

sub lookupfunc
{
   my ($path, $addr) = @_;
   my $last_func = "";
   open (OBJDUMP, "arm-apple-darwin-otool -tv $path 2>&1 |");
   while ($line = <OBJDUMP>)
   {      
       chomp $line;
      if ($line =~ /^(\w*)\:.*/)
      {
	  $last_func = $1;
      }
      if ($line =~ /0*${addr}\s*/)
      {
	  return "$last_func $line";
      }
   }
   close (OBJDUMP);
   return "";
}

sub getfunc
{
   my ($info, $addr) = @_;
   $info =~ /\d*\s*(\S*)/;
   my $lib = $1;
   my $path = "";
   if ($lib eq "libtcvm.dylib")
   {
       $path = $iphonebindir."/.libs/libtcvm.dylib";
   }
   else
   {
	   if ($lib eq "libLitebase.dylib")
	   {
	       $path = $litebasebindir."/.libs/libLitebase.dylib";
	   }
	   else
	   {
           $path = "/usr/local/share/iphone-filesystem/usr/lib/".$lib;
       }
   }
   return lookupfunc($path, $addr);
}

my $on = 0;

open (CRASH, "<$crashFile");

while ($line = <CRASH>)
{
    chop $line;
    my $emit_whole_line = 1;

    if ($line =~ m/$pattern\:/)
    {
        $on = 1;
    }
    else
    {
	if (length($line) == 0)
	{
	    $on = 0;
	}
    }

    if ($on && $line =~ m/dylib/) 
    {
       if ($line =~ m/(.*)\+ (\d*)(.*)/)
       {
           my $addr = cvthex($2);
           print "$1${addr}$3 ".getfunc($1, $addr)."\n";	   
	   $emit_whole_line = 0;
       }
    }

    if ($emit_whole_line)
    {
       print "$line\n";
    }
}

close(CRASH);
