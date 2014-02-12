#!/bin/sh

execPath=$(readlink -f $(dirname $0))
chmod 700 $execPath/start.sh
chmod 700 $execPath/configure.sh
touch $execPath/uninstall.sh
command echo "java -jar $execPath/uninstall.jar" >> $execPath/uninstall.sh
chmod 700 $execPath/uninstall.sh
