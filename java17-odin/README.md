# Java 17 on ArcaOS through Odin32 (release 17-0-17b10a2)

Runs **Temurin 17.0.17+10** (the 32-bit Windows x86 build of OpenJDK 17) on ArcaOS using Odin32's PE loader (`pec.exe` / `pe.exe`). No port of the JVM itself; Odin provides the Win32 API.
Status: works for headless programs, servers, TLS and Swing windows, with the gaps listed below.

## Requirements
* **Odin 0.9.1a3 or newer** (Odin 0.9.1a2 also runs JDK 17). Odin 0.9.0 and 0.9.1a1 will not start it.
* Temurin 17 x86-32 zip from Adoptium (`OpenJDK17U-jdk_x86-32_windows_hotspot_17.0.17_10.zip`, SHA-256 `34ecb7e53b87ecb288be113ad1c76f3e55e7dc2f2b32427605e356feac95cb74`). Adoptium's later 17.0.x updates are 64-bit only, so 17.0.17+10 is the last 32-bit build.
* TrueType fonts in `%SystemRoot%\Fonts` (SystemRoot is the Odin directory, e.g. `C:\ODINRT\Fonts`) if anything draws text (AWT/Swing, `Font`, `ImageIO` with text).
* About 1 GB of address space: `VIRTUALADDRESSLIMIT=1536` in CONFIG.SYS caps the heap at roughly 700 MB (`-Xmx640m` works, `-Xmx768m` does not). Raise it (maximum 3072) for more.

## Setup
See INSTALL.txt. In short: unzip the JDK, copy `odin.args` to `<jdk>\conf\`, put the `launcher\*.exe` in the PATH, `SET JAVA17_HOME=<jdk directory>`, `SET ODIN_DIR=<Odin directory with pec.exe>`, then `java -version`.

## What was tested (all on one CPU, client VM only)
| Area | Result |
|---|---|
| `java -version`, `javac`, `jar`, `javap`, `jdeps`, `javadoc`, `jlink` | works |
| Records, lambdas, streams, threads, parallel streams, `CompletableFuture` | works |
| Time zone, locale, dates, SHA-256, AES, CRC32, `keytool` RSA-2048 key generation | works |
| `javac` compiling the whole `java.util` package (java.base patched) in about 50 s | works |
| Netty 4.1.115 HTTP server (NIO event loops), Jetty 9.4.56 embedded server | works |
| TCP sockets, UDP, `HttpServer`, `HttpURLConnection`, `java.net.http.HttpClient`, 200 concurrent requests | works |
| TLS 1.3 handshake and data transfer on loopback | works |
| NIO files, `FileChannel` (positional, mmap, `transferTo`, locks), `Selector`, `AsynchronousFileChannel` | works (selector needs `odin.args`) |
| `java.net.NetworkInterface`, `user.home` (follows `USERPROFILE`, else `HOME`) | works |
| Swing/AWT: windows, menus, dialogs, `JOptionPane`, `JFileChooser`, Java2D demo | works |
| `AsynchronousSocketChannel` | not available (needs overlapped sockets) |
| IPv6 sockets | not available (clean error); IPv6 literals parse |
| Multi-CPU | untested (VM has one CPU) |

## Native launcher (`launcher\`)
`java.c` builds (Open Watcom, `launcher\bld.cmd`) to an LX `java.exe`. The ready-made `launcher\*.exe` are
copies of it under the names of the JDK tools; the program name selects `<jdk>\bin\<name>.exe`, which is started
through `pec.exe`. For `java` it also passes `conf\odin.args`. Arguments reach Java unchanged, so `-Dname=value`
and quoted arguments work from the command line (a `.cmd` file would split them at `=`). To get another tool,
copy `java.exe` to e.g. `javadoc.exe` or `jlink.exe`. Settings: `JAVA17_HOME` (default: the parent of the
launcher's directory if it has `bin\java.exe`, else `C:\Java17\jdk-17.0.17+10`), `ODIN_DIR` (default `C:\odinrt\b2`),
`JAVA17_NOARGS` (do not pass `odin.args`).

OS/2 command line limits: keytool has no `@argfile` support and `CN=...` style arguments are split by CMD
(not by this launcher); call its main class from a small Java program if needed.

## Native OpenJDK build
`NATIVE-PORT-FEASIBILITY.md` has notes about building OpenJDK 17 itself as OS/2 (LX) DLLs. It is not started; running the Windows build under Odin is the recommended route.
