## WrrKrr Software VERSION
export JAVA_HOME=`/usr/libexec/java_home -v 1.8.0_172`
export VERSION="0.1"
## WrrKrr
echo "+ WRRKRR::DEVICE - REMOVE OLD .ZIP AND FOLDER FOR UNIVERSAL SYSTEMS+"
rm -rf builds/wrrkrr-reader-$VERSION*
cd reader
echo "+ WRRKRR::SCANNER DEVICE - REMOVE NODE MODULES UNIVERSAL SYSTEMS+"
## WrrKrr Scanner software
rm -rf scanner/node_modules
rm -rf scanner/logs/*.csv
cp scanner/scan.properties.production scanner/scan.properties
## WrrKrr Reader software
echo "+ WRRKRR::READER DEVICE - BUILD CURRENT .ZIP FOR UNIVERSAL SYSTEMS+"
cp conf/application.conf.production conf/application.conf
cp conf/logback.xml.production conf/logback.xml
./sbt universal:packageBin
sh prepare_to_develop.sh
cp target/universal/wrrkrr-reader-$VERSION.zip ../builds
cd ..
pwd
echo "+ WRRKRR::READER DEVICE - COMPLETED +"
echo "+ WRRKRR::DEVICE - COMPLETED +"
