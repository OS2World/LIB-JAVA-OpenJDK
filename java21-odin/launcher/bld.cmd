@echo off
rem Builds the launcher with Open Watcom (WATCOM must point to its directory, e.g. C:\WATCOM).
set PATH=%WATCOM%\BINP;%PATH%
set INCLUDE=%WATCOM%\H;%WATCOM%\H\OS2
set LIB=%WATCOM%\LIB386;%WATCOM%\LIB386\OS2
wcl386 -bt=os2 -l=os2v2 -zq -dJVER=21 -fe=java.exe java.c
for %%n in (javac jar keytool jshell) do copy java.exe %%n.exe
