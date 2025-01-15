@echo off
set profile=prod
set ipAddress=78.47.117.155
set username=root

echo start build %profile% without run test ...
call mvn -P%profile% -Dmaven.test.skip.exec=true clean install
echo start transfer portalApp to server %ipAddress%
pscp -i "D:\keys\tara\private_key_new.ppk" rojo-web/target/rojo-web.jar %username%@%ipAddress%:/tmp