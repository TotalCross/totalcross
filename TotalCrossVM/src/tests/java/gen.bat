@echo off
javac GenTestCalls.java -source 1.2 -target 1.1
cd P:\TotalCrossVM\src
java -classpath p:\TotalCrossSDK\output\eclipse;P:\TotalCrossVM\src\tests\java GenTestCalls .
pause
copy tc_tests.c tests
del tc_tests.c