@echo off
rem ---------------------------------------------------------------------------
rem java21.cmd - run Liberica 21 (32 bit Windows build of OpenJDK 21) on ArcaOS
rem              through Odin32's PE loader.
rem
rem usage:  java21 [tool.exe] [arguments...]
rem         java21 -version
rem         java21 -cp mydir MyClass
rem         java21 javac.exe -d out Hello.java       (any tool of the JDK's bin directory)
rem
rem Notes
rem  * The OS/2 command processor splits arguments at '=', so options such as
rem    -Dname=value cannot be passed on the command line. Put them into an
rem    argument file and use  java21 @myopts.txt MyClass  (one option per line,
rem    use forward slashes in paths).
rem  * java.nio selectors: Java 17's default Windows selector needs an NT
rem    driver that does not exist here; odin.args selects the classic one.
rem  * Fonts: Java needs TrueType fonts in %SystemRoot%\Fonts (SystemRoot is the
rem    Odin directory, e.g. C:\ODINRT\Fonts) for any text drawing.
rem  * pec.exe is Odin's console launcher; use pe.exe instead for GUI-only programs.
rem ---------------------------------------------------------------------------
rem --- adjust these two lines for your installation ---------------------------
if "%ODIN_DIR%"=="" set ODIN_DIR=C:\odinrt\b2
if "%JAVA21_HOME%"=="" set JAVA21_HOME=C:\Java21\jdk-21.0.13.0.3
rem -----------------------------------------------------------------------------
set BEGINLIBPATH=%ODIN_DIR%;%BEGINLIBPATH%
set EXE=java.exe
echo %1 | C:\OS2\FIND.EXE /I ".exe" > nul
if not errorlevel 1 goto tool
%ODIN_DIR%\pec.exe %JAVA21_HOME%\bin\java.exe @%JAVA21_HOME%\conf\odin.args %1 %2 %3 %4 %5 %6 %7 %8 %9
goto end
:tool
set EXE=%1
shift
%ODIN_DIR%\pec.exe %JAVA21_HOME%\bin\%EXE% %1 %2 %3 %4 %5 %6 %7 %8 %9
:end
