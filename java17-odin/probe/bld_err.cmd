@echo off
set WATCOM=C:\WATCOM
set PATH=C:\WATCOM\BINP;%PATH%
set INCLUDE=C:\WATCOM\H;C:\WATCOM\H\NT
set LIB=C:\WATCOM\LIB386;C:\WATCOM\LIB386\NT
D:
cd \Projects\Other\openjdk17-odin\probe
wcl386 -bt=nt -l=nt -zq -fe=errtest.exe errtest.c > builderr.log 2>&1
set BEGINLIBPATH=C:\odinrt\b2
C:\odinrt\b2\pec.exe D:\Projects\Other\openjdk17-odin\probe\errtest.exe > D:\OdinTests\errtest.out 2>&1
