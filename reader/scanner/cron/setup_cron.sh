#!/bin/bash

#write out current crontab
crontab -l > wificron
#echo new cron into cron file
echo "* * * * * *   sudo /home/pi/omegatrace-rpi/cron/wifi.sh >> /home/pi/wifi_reconnect.log" >> wificron
#install new cron file
crontab wificron
rm wificron