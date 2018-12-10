#!/bin/bash
# sudo ./usbreset /dev/bus/usb/001/002
sudo pkill -f 'java.*'
sudo forever stopall
sudo rm -rf engage_reader.mv.db engage_reader.trace.db
echo "Please run ./start.sh to apply changes."