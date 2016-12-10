#!/bin/bash

cd
mvn -f javaWorkspace/SparkS/pom.xml package
cp javaWorkspace/SparkS/target/SparkServer-jar-with-dependencies.jar PNC\ Proj/ExcelLoader/SparkServer-jar-with-dependencies.jar 
cd
cp ExcelLoader/out/production/ExcelLoader/ExcelLoader.air PNC\ Proj/ExcelLoader/ExcelLoader.air
cd PNC\ Proj/
zip -r ExcelLoader.zip ExcelLoader/* -x "*.DS_Store" -x "__MACOSX"
cp ExcelLoader.zip ExcelLoader.pdf
cd
echo "Packagin Done"
if [ "$1" = "-r" ]
then
	java -jar PNC\ Proj/ExcelLoader/SparkServer-jar-with-dependencies.jar
fi