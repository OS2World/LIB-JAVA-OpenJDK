# Java 21 on ArcaOS through Odin32 (release 21-0-13b5a2)

Runs **BellSoft Liberica JDK 21.0.13.0.3+5** (the 32-bit Windows x86 build of OpenJDK 21) on ArcaOS using Odin32's PE loader (`pec.exe` / `pe.exe`). No port of the JVM itself; Odin provides the Win32 API.
Status: works for headless programs, servers, TLS and Swing windows, with the gaps listed below.

## Requirements
* **Odin 0.9.1a4 or newer** (it adds the `iphlpapi` network interface functions and a few kernel32/advapi32 exports that JDK 21 needs). Earlier Odin versions will not start it.
* BellSoft Liberica JDK 21 for Windows x86 (32-bit): `bellsoft-jdk21.0.13.0.3+5-windows-i586.zip` from <https://github.com/bell-sw/Liberica/releases/tag/21.0.13.0.3%2B5> (SHA-1 `598b925cf01f596cb18e3b6607b7db4534218b89`). Adoptium (Temurin) has no 32-bit Windows build of 21 and OpenJDK removed the 32-bit Windows port in JDK 24, so JDK 21 is the newest release Odin can run.
* TrueType fonts in `%SystemRoot%\Fonts` (SystemRoot is the Odin directory, e.g. `C:\ODINRT\Fonts`) if anything draws text (AWT/Swing, `Font`, `ImageIO` with text).
* About 1 GB of address space: `VIRTUALADDRESSLIMIT=1536` in CONFIG.SYS caps the heap at roughly 700 MB (`-Xmx640m` works, `-Xmx768m` does not). Raise it (maximum 3072) for more.

## Setup
See INSTALL.txt. In short: unzip the JDK, copy `odin.args` to `<jdk>\conf\`, put the `launcher\*.exe` in the PATH, `SET JAVA21_HOME=<jdk directory>`, `SET ODIN_DIR=<Odin directory with pec.exe>`, then `java -version`.

Known differences from the Java 17 kit: `jshell` only works with `--execution local` (its default remote execution engine closes at once).

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
| `java -jar app.jar` (Swing game jar, Stendhal client starter), log files opened for append | works (needs Odin 0.9.1a4) |
| Sound (`javax.sound`) | not working on the test VM: MMPM/2 does not initialise there, so Odin's DirectSound cannot open an output line (the program keeps running without sound) |
| `AsynchronousSocketChannel` | not available (needs overlapped sockets) |
| IPv6 sockets | not available (clean error); IPv6 literals parse |
| Multi-CPU | untested (VM has one CPU) |

## Native launcher (`launcher\`)
`java.c` builds (Open Watcom, `launcher\bld.cmd`) to an LX `java.exe`. The ready-made `launcher\*.exe` are
copies of it under the names of the JDK tools; the program name selects `<jdk>\bin\<name>.exe`, which is started
through `pec.exe`. For `java` it also passes `conf\odin.args`. Arguments reach Java unchanged, so `-Dname=value`
and quoted arguments work from the command line (a `.cmd` file would split them at `=`). To get another tool,
copy `java.exe` to e.g. `javadoc.exe` or `jlink.exe`. Settings: `JAVA21_HOME` (default: the parent of the
launcher's directory if it has `bin\java.exe`, else `C:\Java21\jdk-21.0.13.0.3`), `ODIN_DIR` (default `C:\odinrt\b2`),
`JAVA21_NOARGS` (do not pass `odin.args`).

OS/2 command line limits: keytool has no `@argfile` support and `CN=...` style arguments are split by CMD
(not by this launcher); call its main class from a small Java program if needed.
