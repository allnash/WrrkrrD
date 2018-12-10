#!/bin/bash
# Copy New Tar to Host $1
export VERSION=0.1
scp -i wrrkrr-reader builds/wrrkrr-reader-$VERSION.zip "oem@$1:/home/oem/"
scp -i wrrkrr-reader reader/scanner/setup.sh "oem@$1:/home/oem/"
scp -i wrrkrr-reader reader/scanner/start.sh "oem@$1:/home/oem/"
scp -i wrrkrr-reader reader/scanner/reset.sh "oem@$1:/home/oem/"
scp -i wrrkrr-reader reader/scanner/hostname.sh "oem@$1:/home/oem/"
scp -i wrrkrr-reader reader/scanner/usbreset.c "oem@$1:/home/oem/"
