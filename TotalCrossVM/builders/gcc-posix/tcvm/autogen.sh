#! /bin/sh

echo "Regenerate the autoconf/automake files";

libtoolize --force --automake

rm -f config.cache
rm -f config.log

aclocal
autoconf
autoheader
automake --add-missing
