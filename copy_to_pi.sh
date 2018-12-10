#!/bin/bash
# Copy New Tar to Host $1
export VERSION=0.1
scp -i wrrkrr-reader builds/wrrkrr-reader-$VERSION.zip "pi@$1:/home/pi/"
scp -i wrrkrr-reader reader/scanner/setup.sh "pi@$1:/home/pi/"
scp -i wrrkrr-reader reader/scanner/start.sh "pi@$1:/home/pi/"
scp -i wrrkrr-reader reader/scanner/reset.sh "pi@$1:/home/pi/"
scp -i wrrkrr-reader reader/scanner/hostname.sh "pi@$1:/home/pi/"
scp -i wrrkrr-reader reader/scanner/usbreset.c "pi@$1:/home/pi/"
