@echo off
javac -target 1.1 -source 1.2 Test*.java
jar cvf Test.jar Test*.class barbara.jpg pal685.png test.wav
java.exe -classpath p:\gitrepo\TotalCross\TotalCrossSDK\output\eclipse;P:\gitrepo\TotalCross\TotalCrossVM\src\tests\java;p:\gitrepo\TotalCross\TotalCrossSDK\dist\tc.jar;. tc.Deploy Test.jar -win32 /a TCvm %1 >out.txt
copy install\win32\Test.tcz P:\gitrepo\TotalCross\TotalCrossVM\builders\vc6\Debug
rem copy Test.tcz Z:\TotalCross
rem copy install\palm\Test.pdb Z:\TotalCross
