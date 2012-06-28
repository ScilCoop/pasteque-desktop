#!/bin/sh
INSTALL_DIR="/opt/pos-tech"
DIR=$(pwd)

mv $DIR $INSTALL_DIR
cp $INSTALL_DIR/pos-tech /usr/bin/
chmod 755 /usr/bin/pos-tech
cp $INSTALL_DIR/pos-tech.desktop /usr/share/applications/
ln -s $INSTALL_DIR/pos-tech-logo-128.png /usr/share/icons/pos-tech.png
