@echo off
rem ---------------------------------------------------------------------------
rem java17.cmd - run Temurin 17 (32 bit Windows build of OpenJDK 17) on ArcaOS
rem              through Odin32's PE loader.
rem
rem usage:  java17 [tool.exe] [arguments...]
rem         java17 -version
rem         java17 -cp mydir MyClass
rem         java17 javac.exe -d out Hello.java       (any tool of the JDK's bin directory)
rem
rem Notes
rem  * The OS/2 command processor splits arguments at '=', so options such as
rem    -Dname=value cannot be passed on the command line. Put them into an
rem    argument file and use  java17 @myopts.txt MyClass  (one option per line,
rem    use forward slashes in paths).
rem  * java.nio selectors: Java 17's default Windows selector needs an NT
rem    driver that does not exist here; odin.args selects the classic one.
rem  * Fonts: Java needs TrueType fonts in %SystemRoot%\Fonts (SystemRoot is the
rem    Odin directory, e.g. C:\ODINRT\Fonts) for any text drawing.
rem  * pec.exe is Odin's console launcher; use pe.exe instead for GUI-only programs.
rem ---------------------------------------------------------------------------
rem --- adjust these two lines for your installation ---------------------------
if "%ODIN_DIR%"=="" set ODIN_DIR=C:\odinrt\b2
if "%JAVA17_HOME%"=="" set JAVA17_HOME=C:\Java17\jdk-17.0.17+10
rem -----------------------------------------------------------------------------
set BEGINLIBPATH=%ODIN_DIR%;%BEGINLIBPATH%
set EXE=java.exe
echo %1 | C:\OS2\FIND.EXE /I ".exe" > nul
if not errorlevel 1 goto tool
%ODIN_DIR%\pec.exe %JAVA17_HOME%\bin\java.exe @%JAVA17_HOME%\conf\odin.args %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end
:tool
set EXE=%1
shift
%ODIN_DIR%\pec.exe %JAVA17_HOME%\bin\%EXE% %1 %2 %3 %4 %5 %6 %7 %8 %9
:end
