#!/bin/bash
echo "remove db"
rm -rf ~/engage_reader.*.db
cp  -fv conf/application.conf.development conf/application.conf
echo "+ APP READY FOR DEVELOPMENT+"
