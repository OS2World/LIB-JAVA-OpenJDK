/* crtprobe: calls the JDK's own ucrtbase.dll the way the Java launcher's manifest parser does. */
#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>

typedef int (__cdecl *PFN_OPEN)(const char *, int, ...);
typedef __int64 (__cdecl *PFN_LSEEK)(int, __int64, int);
typedef int (__cdecl *PFN_READ)(int, void *, unsigned);
typedef int (__cdecl *PFN_CLOSE)(int);

int main(int argc, char **argv)
{
    HMODULE m;
    PFN_OPEN fopen_;
    PFN_LSEEK fseek_;
    PFN_READ fread_;
    PFN_CLOSE fclose_;
    int fd, n, i;
    __int64 len, pos;
    unsigned char buf[64];

    if (argc < 3) { printf("usage: crtprobe ucrtbase.dll file\n"); return 1; }
    m = LoadLibraryA(argv[1]);
    printf("LoadLibrary(%s) -> %p err=%lu\n", argv[1], m, GetLastError());
    if (!m) return 2;
    fopen_ = (PFN_OPEN)GetProcAddress(m, "_open");
    fseek_ = (PFN_LSEEK)GetProcAddress(m, "_lseeki64");
    fread_ = (PFN_READ)GetProcAddress(m, "_read");
    fclose_ = (PFN_CLOSE)GetProcAddress(m, "_close");
    printf("_open=%p _lseeki64=%p _read=%p _close=%p\n", fopen_, fseek_, fread_, fclose_);
    if (!fopen_ || !fseek_ || !fread_) return 3;

    fd = fopen_(argv[2], 0x0000 | 0x8000 /* O_RDONLY | O_BINARY */);
    printf("_open -> %d err=%lu\n", fd, GetLastError());
    if (fd < 0) return 4;
    len = fseek_(fd, 0, 2 /* SEEK_END */);
    printf("_lseeki64(0, SEEK_END) -> %ld (hi %ld)\n", (long)len, (long)(len >> 32));
    pos = fseek_(fd, len - 22, 0 /* SEEK_SET */);
    printf("_lseeki64(len-22, SEEK_SET) -> %ld\n", (long)pos);
    memset(buf, 0, sizeof(buf));
    n = fread_(fd, buf, 22);
    printf("_read(22) -> %d  sig=%02x %02x %02x %02x\n", n, buf[0], buf[1], buf[2], buf[3]);
    pos = fseek_(fd, -22, 2 /* SEEK_END, negative offset as the launcher does */);
    printf("_lseeki64(-22, SEEK_END) -> %ld (hi %ld)\n", (long)pos, (long)(pos >> 32));
    memset(buf, 0, sizeof(buf));
    n = fread_(fd, buf, 22);
    printf("_read(22) -> %d  sig=%02x %02x %02x %02x\n", n, buf[0], buf[1], buf[2], buf[3]);
    pos = fseek_(fd, 0, 0);
    n = fread_(fd, buf, 4);
    printf("_read at 0 -> %d sig=%02x %02x %02x %02x\n", n, buf[0], buf[1], buf[2], buf[3]);
    fclose_(fd);
    return 0;
}
