# Java 17 and 21 on ArcaOS through Odin32 (release 17a2)

Runs **Temurin 17.0.17** (the 32-bit Windows x86 build of OpenJDK 17) on ArcaOS using
Odin32's PE loader (`pec.exe` / `pe.exe`). No port of the JVM itself; Odin provides the Win32 API.
Status: works for headless programs, servers, TLS and Swing windows, with the gaps listed below.

## Requirements
* **Odin 0.9.1a3 or newer** (0.9.1a2 is enough for JDK 17 only) (new kernel32 APIs, completion ports, memory-map and file fixes, `ws2_32` name
  resolution, new `POWRPROF`, `WINHTTP`, `MSWSOCK` DLLs). Odin 0.9.0 and 0.9.1a1 will not start Java 17.
* Temurin 17 x86-32 zip, from Adoptium (`OpenJDK17U-jdk_x86-32_windows_hotspot_17.0.17_10.zip`,
  SHA-256 `34ecb7e53b87ecb288be113ad1c76f3e55e7dc2f2b32427605e356feac95cb74`).
* TrueType fonts in `%SystemRoot%\Fonts` (SystemRoot is the Odin directory, e.g. `C:\ODINRT\Fonts`)
  if anything draws text (AWT/Swing, `Font`, `ImageIO` with text). The VM had none; GNU FreeSans was used.
* About 1 GB of address space: `VIRTUALADDRESSLIMIT=1536` in CONFIG.SYS caps the heap at roughly
  700 MB (`-Xmx640m` works, `-Xmx768m` does not). Raise it (maximum 3072) for more.

## Setup
1. Unzip Temurin, copy `odin.args` to `<jdk>\conf\odin.args`.
2. Edit the two paths at the top of `java17.cmd` (`ODIN_DIR` = directory with `pec.exe` and the Odin DLLs,
   `JAVA17_HOME` = the JDK directory).
3. `java17 -version`, `java17 -cp dir Class`, `java17 javac.exe -d out Hello.java`.

OS/2 command line limits: `-Dname=value` is split at `=`; put such options in a file and use `java17 @opts.txt`
(forward slashes in paths). Keytool has no `@argfile` support; call its main class from a small Java program.

## What was tested (all on one CPU, client VM only)
| Area | Result |
|---|---|
| `java -version`, `javac`, `jar`, `javap`, `jdeps`, `javadoc`, `jlink` (custom 43 MB runtime that runs) | works |
| Records, lambdas, streams, threads, parallel streams, `CompletableFuture` | works |
| Time zone, locale, dates, SHA-256, AES, CRC32, `keytool` RSA-2048 key generation | works |
| `jshell` script run; `javac` compiling the 354 files of `java.util` (patched into java.base) to 1369 classes in about 50 s | works |
| Netty 4.1.115 HTTP server (NIO event loops) and Jetty 9.4.56 embedded server, 100 and 50 requests | works |
| TCP sockets, UDP, `HttpServer`, `HttpURLConnection`, `java.net.http.HttpClient`, 200 concurrent requests | works |
| TLS 1.3 handshake and data transfer on loopback | works |
| NIO files, `FileChannel` (positional, mmap, `transferTo`, locks), `Selector`, `AsynchronousFileChannel` | works (selector needs `odin.args`) |
| Swing/AWT: windows, menus, dialogs, `JOptionPane`, `JFileChooser`, mouse and keyboard | works |
| `AsynchronousSocketChannel` | not yet (needs overlapped sockets) |
| IPv6 sockets | not available (clean error); IPv6 literals parse |
| Multi-CPU | untested (VM has one CPU) |
| `user.home` | follows `USERPROFILE`, else `HOME` (shell32 fix) |
| Exit | clean (warning fixed in kernel32 2026-11-02); one unexplained non-exiting Swing process seen once |

## Native launcher (`launcher\`)
`java.c` builds (Open Watcom, `launcher\bld.cmd`) to an LX `java.exe`; copy it under the name of any tool
(`javac.exe`, `jar.exe`, `keytool.exe`...) and the name selects `<jdk>\bin\<name>.exe`, started through `pec.exe`.
It sets BEGINLIBPATH, passes `conf\odin.args` for `java`, and keeps arguments intact, so `-Dname=value`,
quoted arguments and keytool work from the command line. Settings: `JAVA17_HOME` (default: parent of the
launcher's directory if it has `bin\java.exe`, else `C:\Java17\jdk-17.0.17+10`), `ODIN_DIR`. Verified in the VM
(`-Dx=hello`, `"a b"` argument, javac). Launchers need not live in the JDK; `JAVA17_HOME` locates it.

## Java 21 (BellSoft Liberica)

Adoptium (Temurin) stopped its 32-bit Windows builds after JDK 19, and OpenJDK removed the
32-bit Windows port in JDK 24, so 21 is the newest release Odin can run. Only BellSoft
Liberica still builds it: `bellsoft-jdk21.0.13.0.3+5-windows-i586.zip`
(SHA-1 `598b925cf01f596cb18e3b6607b7db4534218b89`). Set `JAVA17_HOME` to the unzipped JDK 21
directory; everything else is the same. Needs Odin 0.9.1a3 (iphlpapi network interface
functions, a few kernel32/advapi32 exports).

Tested on 21.0.13: `java -version`, javac (1430 classes of java.util), hello/system/network
tests, NIO (selector, file channels), HTTP server with 200 requests, TLS 1.3, Netty, Jetty,
Swing/Java2D demo, benchmark. Not working: `AsynchronousSocketChannel` (as on 17) and `jshell`
with its default remote execution engine (`jshell --execution local` works).

## Files here
`java17.cmd` launcher, `odin.args` default options, `impcheck.py` (static check of a Windows program's imports
against Odin's exports), `probe\` (small Win32 test programs built with Open Watcom), `os2delta.py` and
`os2-delta\` (what the OpenJDK 6 OS/2 port changed against jdk6-b27), test drivers in `..\..\..\OdinTests`.
