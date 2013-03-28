#!/bin/sh
INSTALL_DIR="/opt/pasteque"
DIR=$(pwd)

mv $DIR $INSTALL_DIR
cp $INSTALL_DIR/pasteque /usr/bin/
chmod 755 /usr/bin/pasteque
cp $INSTALL_DIR/pasteque.desktop /usr/share/applications/
ln -s $INSTALL_DIR/pasteque-logo-128.png /usr/share/icons/pasteque.png
