#! /usr/bin/perl -w

eval 'exec /usr/bin/perl -S $0 ${1+"$@"}'
    if 0; #$running_under_some_shell

use Getopt::Std;
%options=();
getopts("hvd:",\%options);

my $help = "run with: -d <path to package source directory>";

defined $options{h} and die "$help\n";
! defined $options{d} and die "$help\n";

#my $root = '/home/fdie/workspace/TotalCrossSDK/src/tc/samples/';
my $root = $options{d};
my $verbose = $options{v};

print "process root directory: $root\n" if $verbose;

use strict;
use File::Find ();
use File::Basename ();

# for the convenience of &wanted calls, including -eval statements:
use vars qw/*filename/;
*filename = *File::Find::name;

sub generate_subbuild
{
	my ($dir, $name) = @_;
	my $build = $dir."/subbuild.xml";
	
	if (-e $build)
	{
		print "build file $build already exists\n";
		return;
	}
	 
    print("generate $build for sample ".$name."\n");
    
    my $relpath = $dir;
    $relpath =~ s/$root//;
    
    open(SB, ">$build") or die "Can't open $build: $!";

	print SB "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n";
	print SB "\n";
	print SB "<project name=\"".$name."\" basedir=\".\">\n";
	print SB "\n";
	print SB "<property name=\"app.srcdir\" value=\"".$relpath."\" />\n";
	print SB "<import file=\"\${sdk.root}/build.xml\" />\n";
	print SB "\n";
	print SB "<target name=\"sub-build\">\n";
	
	print SB "<mkdir dir=\"\${output}/samples/\${ant.project.name}\" />\n";
	print SB "<jar\n";
	print SB "\tbasedir=\"\${output}/classes\"\n";
	print SB "\tdestfile=\"\${output}/samples/\${ant.project.name}/\${ant.project.name}.jar\"\n";
	print SB "\tincludes=\"\${samples.includes}/\${app.srcdir}/**\"\n";
	print SB "/>\n";
	
	print SB "<antcall target=\"deploy-app\">\n";
	print SB "\t<param name=\"app.name\" value=\"\${ant.project.name}\"/>\n";
	print SB "</antcall>\n";

	print SB "</target>\n";
	print SB "\n";
	print SB "</project>\n";

    close(SB);
}

sub wanted
{
    ! -f $filename && return;
    open(FH, $filename) or die "Can't open $filename: $!";
    while (<FH>)
    {
        if (/(\w*)\s*extends\s*MainWindow\s/ or /(\w*)\s*extends\s*GameEngine\s/ or /(\w*)\s*extends\s*Conduit\s/)
        {
            my $name = $1;
        	generate_subbuild(File::Basename::dirname($filename), $name);
        }
    }
    close (FH);
}

# Traverse desired filesystems
File::Find::find({wanted => \&wanted}, $root);
exit;
