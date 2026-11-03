/* dupclose: the handle juggling java.nio's FileChannel.map/transferTo performs */
#include <windows.h>
#include <stdio.h>

static FILE *lg;

#define STEP(txt, expr)                                                       \
    do {                                                                      \
        DWORD r_; SetLastError(0);                                            \
        r_ = (DWORD)(expr);                                                   \
        fprintf(lg, "%-46s ret=%08lx err=%lu\n", txt, r_, GetLastError());    \
        fflush(lg);                                                           \
    } while (0)

int main(void)
{
    HANDLE h, d, m, d2;
    unsigned char *v;
    DWORD n;
    unsigned char b[64];

    lg = fopen("D:/OdinTests/dupclose.log", "w");
    if (!lg)
        return 2;
    h = CreateFileA("D:\\OdinTests\\jt\\mw_src.dat", GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, NULL,
                    OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    fprintf(lg, "file handle %p\n", h);
    STEP("DuplicateHandle(h)", DuplicateHandle(GetCurrentProcess(), h, GetCurrentProcess(), &d, 0, FALSE, DUPLICATE_SAME_ACCESS));
    fprintf(lg, "dup handle %p\n", d);
    m = CreateFileMappingA(d, NULL, PAGE_READONLY, 0, 0, NULL);
    fprintf(lg, "mapping %p\n", m);
    STEP("CloseHandle(dup)  [java closes the dup]", CloseHandle(d));
    v = (unsigned char *)MapViewOfFile(m, FILE_MAP_READ, 0, 0, 8192);
    fprintf(lg, "view %p\n", v);
    STEP("CloseHandle(mapping)", CloseHandle(m));
    fprintf(lg, "view byte 100 = %02x\n", v ? v[100] : 0xEE);
    STEP("ReadFile(h) after mapping closed", ReadFile(h, b, 16, &n, NULL));
    STEP("UnmapViewOfFile", UnmapViewOfFile(v));
    STEP("ReadFile(h) after unmap", ReadFile(h, b, 16, &n, NULL));
    STEP("GetFileSize(h)", GetFileSize(h, NULL));
    STEP("CloseHandle(h)  [channel close]", CloseHandle(h));
    fprintf(lg, "DONE\n");
    fclose(lg);
    return 0;
}
