#!/bin/bash
# OmegaTrace Inc. ("COMPANY") CONFIDENTIAL
# Unpublished Copyright (c) 2017 OmegaTrace Inc., All Rights Reserved.
#
# NOTICE:  All information contained herein is, and remains the property of COMPANY.
# The intellectual and technical concepts contained herein are proprietary to COMPANY and may be covered by U.S. and
# Foreign Patents, patents in process, and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material is strictly forbidden unless prior written
# permission is obtained from COMPANY. Access to the source code contained herein is hereby forbidden to anyone except
# current COMPANY employees, managers or contractors who have executed Confidentiality and Non-disclosure agreements
# explicitly covering such access.
#
# The copyright notice above does not evidence any actual or intended publication or disclosure of this source code,
# which includes information that is confidential and/or proprietary, and is a trade secret, of  COMPANY.
# ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS
# SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF COMPANY IS STRICTLY PROHIBITED, AND IN VIOLATION OF APPLICABLE
# LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED INFORMATION DOES NOT
# CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL
# ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.

echo "OmegaTrace WrrKrr Reader Raspberry Pi Zero W setup"
# Updated -
# Manually Install NodeJS https://nodejs.org/dist/ for Raspberry Pi Zero W
# If this fails then try instructions from here -> https://github.com/sdesalas/node-pi-zero
sudo apt-get update
sudo apt-get dist-upgrade

# MANUALLY UPDATE SWAP SPACE
# sudo nano /etc/dphys-swapfile
# The default value in Raspbian is: 100
# update the following line
# CONF_SWAPSIZE=1024
# Reboot the system
# $ sudo reboot

# Create backup directory 04/20/2018
mkdir backup
curl -o node-v9.7.1-linux-armv6l.tar.gz https://nodejs.org/dist/v9.7.1/node-v9.7.1-linux-armv6l.tar.gz
cp node-v9.7.1-linux-armv6l.tar.gz backup
tar -xzf node-v9.7.1-linux-armv6l.tar.gz
sudo cp -r node-v9.7.1-linux-armv6l/* /usr/local/
# Cleanup
rm -rf node-v9.7.1-linux-armv6l.tar.gz node-v9.7.1-linux-armv6l
# Install other packages needed for Node and Python packet scanner.
sudo apt-get install git htop vim bluetooth bluez libbluetooth-dev libudev-dev tshark i2c-tools wget -y
# Check Node version
node -v
npm -v
sudo npm install forever -g
# Check installation
echo "hcitool version --> "
hcitool | grep ver
echo "Configure to run wireshark as non-root"
sudo dpkg-reconfigure wireshark-common
sudo usermod -a -G wireshark ${USER:-root}
newgrp wiresharkcat

# Install Oracle Java
sudo apt-get install oracle-java8-jdk -y

# commenting out for now
# Python and pip
# echo "Python version >> install python pip"
# python -V
# sudo apt-get install python-pip -y
# sudo pip install virtualenv
# sudo pip install virtualenvwrapper
# echo "source /usr/local/bin/virtualenvwrapper.sh" >> ~/.bashrc
#echo "source /usr/local/bin/virtualenvwrapper.sh" >> ~/.profile
#echo " "
#echo "To activate virtualenv wrapper please source ->"
#echo "'source /usr/local/bin/virtualenvwrapper.sh'"
#echo " "