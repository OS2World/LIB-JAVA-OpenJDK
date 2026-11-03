@echo off
set WATCOM=C:\WATCOM
set PATH=C:\WATCOM\BINP;%PATH%
set INCLUDE=C:\WATCOM\H;C:\WATCOM\H\OS2
set LIB=C:\WATCOM\LIB386;C:\WATCOM\LIB386\OS2
D:
cd \Projects\Other\openjdk17-odin\launcher
wcl386 -bt=os2 -l=os2v2 -zq -fe=java.exe java.c > build.log 2>&1
copy java.exe javac.exe > nul
copy java.exe jar.exe > nul
copy java.exe keytool.exe > nul
