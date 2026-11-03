# Native (LX) OpenJDK 17 for OS/2 - feasibility notes (2026-11-02)

Context: Temurin 17 (a PE build) already runs under Odin32 (see README.md). The question is whether
building JDK 17 itself as native OS/2 (LX) DLLs, the way the OpenJDK 6 port was built, is worth it.

## What the OpenJDK 6 port shows
* Architecture: HotSpot's *Windows* OS layer (`os_windows.cpp`, `os_windows_x86.cpp`) plus ~280 lines of
  `os_os2`, JDK Windows natives and Windows AWT, built as LX DLLs with GCC against the Odin32 SDK.
  Delta against pristine jdk6-b27: 433 changed + 43 added + 22 removed files of 25 455 (`os2-delta\`).

## Findings for JDK 17
* The Windows OS layer still has few compiler-specific constructs: in `os_windows.cpp` 5 `__try`/`__except`
  and 3 inline-asm uses, in `os_windows_x86.cpp` 1 `__try` and 4 asm uses. **GCC 9.2 on OS/2 has no `__try`**
  (tested), so these ~6 sites need a replacement (Odin's `___seh_handler`/OS/2 exception handler registration);
  `os::os_exception_wrapper` is the important one. This is small, not a blocker.
* Everything else in HotSpot is portable C++14; GCC 9.2 can compile it. Size of the work is therefore the build:
  ~1000 HotSpot sources, `make` + autoconf-based `configure` (needs an OS/2 platform definition), a boot JDK to build
  the Java classes (build those on another OS with a normal JDK and only compile natives on OS/2), and linking a
  very large `jvm.dll` with wlink (the 6.x port needed ILINK because WLINK produced wrong fixups with HLL data).
* JDK 17 natives need newer Win32 declarations than Odin32's NT4-era headers (many already provided by the
  `win7compat`/`iocp` additions), and the Windows AWT natives use COM/GDI+/Direct3D headers - headless first.
* Runtime dependencies on Win32 behaviour are the same as now: everything found while running Temurin under Odin
  (memory manager semantics, file mapping, completion ports...) has to be right either way.

## Comparison
| | PE under Odin (current) | native LX build |
|---|---|---|
| Effort | done; incremental Odin fixes | weeks: build system, linker, SEH, headers, packaging |
| Update to newer JDK | download new Temurin, fix Odin gaps (impcheck.py lists them first) | redo/forward-port the OS/2 delta for every release |
| Performance | JIT code identical; only PE loading overhead at startup (~1 s) | slightly faster startup |
| Odin dependency | full (Win32 API provider) | full as well (Windows layer on Odin) |
| Integration | launcher script (`java17.cmd`) | real `java.exe`, JNI libs as LX |
| Bugs fixed benefit | every Win32 app (Notepad++, PhotoFiltre, ...) | same |

## Recommendation
Keep the PE-on-Odin route as the main line: it gives modern Java now and every fix improves Odin for other
programs. Revisit a native build only if startup time, packaging or distribution requirements demand it. If it
is started, begin with: (1) a GCC 9.2 `configure`/make probe of `src/hotspot/share` to count compile failures,
(2) the SEH replacement for `os_exception_wrapper`, (3) linking an empty-ish `jvm.dll` with wlink to confirm limits.

## Useful remaining work on the current route
Overlapped Winsock for `AsynchronousSocketChannel`; a proper `user.home`; an LX `java.exe` front end so ArcaOS tools
find `java`; more CPU cores (needs an SMP VM); fix the exit-time `DuplicateHandle(GetCurrentThread())` warning;
RPM/ZIP packaging.
