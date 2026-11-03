/* mapwrite: WriteFile() from a memory-mapped view (what java.nio transferTo does) */
#include <windows.h>
#include <stdio.h>
#include <string.h>

static FILE *lg;

static void say(const char *s, unsigned long a)
{
    fprintf(lg, "%s %08lx\n", s, a);
    fflush(lg);
}

int main(void)
{
    HANDLE hSrc, hMap, hDst;
    unsigned char *view;
    DWORD n, i;
    unsigned char buf[8192];

    lg = fopen("D:/OdinTests/mapwrite.log", "w");
    if (!lg)
        return 2;

    /* source file with known contents */
    hSrc = CreateFileA("D:\\OdinTests\\jt\\mw_src.dat", GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE,
                       NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, NULL);
    for (i = 0; i < 8192; i++)
        buf[i] = (unsigned char)(i * 7);
    WriteFile(hSrc, buf, 8192, &n, NULL);
    say("source written bytes", n);

    hMap = CreateFileMappingA(hSrc, NULL, PAGE_READONLY, 0, 0, NULL);
    say("CreateFileMapping", (unsigned long)hMap);
    view = (unsigned char *)MapViewOfFile(hMap, FILE_MAP_READ, 0, 0, 8192);
    say("MapViewOfFile", (unsigned long)view);
    if (!view)
        return 3;
    say("first view byte (expect 00)", view[0]);
    say("byte 100 (expect c4)", view[100]);

    /* second, independent mapping of the same file (the first one stays alive) */
    {
        HANDLE hSrc2 = CreateFileA("D:\\OdinTests\\jt\\mw_src.dat", GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE,
                                   NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
        HANDLE hMap2 = CreateFileMappingA(hSrc2, NULL, PAGE_READONLY, 0, 0, NULL);
        unsigned char *view2 = (unsigned char *)MapViewOfFile(hMap2, FILE_MAP_READ, 0, 0, 8192);
        say("2nd mapping handle", (unsigned long)hMap2);
        say("2nd view addr", (unsigned long)view2);
        if (view2) {
            say("2nd view byte 100 (expect bc)", view2[100]);
            view = view2;
        }
    }
    hDst = CreateFileA("D:\\OdinTests\\jt\\mw_dst.dat", GENERIC_WRITE, FILE_SHARE_READ, NULL, CREATE_ALWAYS,
                       FILE_ATTRIBUTE_NORMAL, NULL);
    say("dst handle", (unsigned long)hDst);
    say("about to WriteFile from view", 0);
    n = 0;
    say("WriteFile ok", WriteFile(hDst, view, 8192, &n, NULL));
    say("bytes written", n);
    CloseHandle(hDst);
    UnmapViewOfFile(view);
    CloseHandle(hMap);
    CloseHandle(hSrc);
    say("DONE", 0);
    fclose(lg);
    return 0;
}
