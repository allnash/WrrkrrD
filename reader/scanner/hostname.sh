#!/bin/bash
MAC="omegatrace-""$( sed "s/^.*macaddr=\([0-9A-F:]*\) .*$/\1/;s/://g" /proc/cmdline )"
echo "$MAC" > "/etc/hostname"
CURRENT_HOSTNAME=$(cat /proc/sys/kernel/hostname)
sed -i "s/127.0.1.1.*$CURRENT_HOSTNAME/127.0.1.1\t$MAC/g" /etc/hosts
hostname $MAC
sudo reboot
