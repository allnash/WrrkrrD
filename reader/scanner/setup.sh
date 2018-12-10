#!/bin/bash
export VERSION=0.1
echo "Attempting to terminate all WrrKrr task instances."
sudo pkill -f 'java.*'
sudo forever stopall
# Move the node modules outside so update time can be minimized.
echo "Move node modules outside for caching"
mv -v /home/pi/wrrkrr-reader-${VERSION}/scanner/node_modules /home/pi
echo "Remove directories and database."
echo "We are removing database so we can ignore conflicts."
# TODO: THIS NEEDS TO BE HANDLED BETTER IN THE FUTURE.
sudo rm -rf engage_reader.mv.db engage_reader.trace.db
sudo rm -rf /home/pi/wrrkrr-reader-${VERSION}
sudo rm -rf /home/pi/scanner
echo "Unzip WrrKrr Zip image"
unzip -q wrrkrr-reader-${VERSION}.zip
echo "Move node modules back into scanner directory to speed up installation of packages."
mv -v /home/pi/node_modules /home/pi/wrrkrr-reader-${VERSION}/scanner
echo "Run NPM package install inside scanner directory."
cd /home/pi/wrrkrr-reader-${VERSION}/scanner
npm install --silent
echo "Install NPM package 'forever' globally."
sudo npm install --silent -g forever
echo "Copy new rc.local to the OS."
sudo cp -v /home/pi/wrrkrr-reader-${VERSION}/scanner/rc.local /etc/rc.local
echo "Compile USB reset."
cc usbreset.c -o usbreset
chmod 755 usbreset

# TODO: Commenting out for now.
# echo "Deleting Virtual Env WrrKrr."
# source `which virtualenvwrapper.sh`
# rmvirtualenv wrrkrr
# echo "Run pip install for python package 'lookaroundyou' using local code."
# echo "Creating Virtual Env WrrKrr."
# mkvirtualenv wrrkrr
# echo " "
# pip install -e /home/pi/wrrkrr-reader-${VERSION}/lookaroundyou/
#

echo " Printing capabilities of wifi"
iw phy phy1 info
echo " "
echo " "
echo "Please restart the system."
