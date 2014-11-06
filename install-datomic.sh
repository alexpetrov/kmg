#!/bin/bash
export DATOMIC_VERSION=0.9.5052

echo Downloading version ${DATOMIC_VERSION}

mkdir ../temp
mkdir ../datomic

curl --progress-bar --location\
 --user-agent 'tauhoDB (info@tauho.db)'\
 --url "https://my.datomic.com/downloads/free/${DATOMIC_VERSION}"\
 --output ../temp/datomic.zip


# unzip datomic
unzip -u ../temp/datomic.zip -d ../temp

#move unzipped files into own folder and remove temp folder
cp -r ../temp/datomic-free-${DATOMIC_VERSION}/* ../datomic
rm -r ../temp

cd ../datomic
./bin/maven-install
