@echo off
set BEGINLIBPATH=C:\odinrt\b2
set ODIN_TRACE=D:\OdinTests\mw_trace.txt
del D:\OdinTests\mw_trace.txt
C:\odinrt\b2\pec.exe D:\Projects\Other\openjdk17-odin\probe\mapwrite.exe > D:\OdinTests\mapwrite.out 2>&1
