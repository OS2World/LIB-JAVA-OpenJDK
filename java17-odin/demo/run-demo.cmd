@echo off
rem Java2D / Swing demo.  usage: run-demo [tab 0-4] [seconds until it closes itself]
rem Needs JAVA17_HOME and ODIN_DIR set (see ..\INSTALL.txt) and java.exe/javac.exe of this kit in PATH.
if "%JAVA17_HOME%"=="" echo Set JAVA17_HOME first (see INSTALL.txt)& goto end
if not exist Demo2D.class javac -d . Demo2D.java
java -cp . Demo2D %1 %2
:end
