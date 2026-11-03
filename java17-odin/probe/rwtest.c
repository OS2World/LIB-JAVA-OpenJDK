/* rwtest: compare positional ReadFile (OVERLAPPED offset on a synchronous handle,
 * as libjimage does) against SetFilePointer+ReadFile */
#include <windows.h>
#include <stdio.h>
#include <string.h>

static void hex(FILE *f, const unsigned char *b, int n)
{
    int i;
    for (i = 0; i < n; i++)
        fprintf(f, "%02x", b[i]);
}

int main(int argc, char **argv)
{
    FILE *lg = fopen("D:/OdinTests/rwtest.log", "w");
    HANDLE h;
    DWORD  offs[] = {0, 8, 4096, 1000003, 70000000};
    int    i;
    unsigned char a[16], b[16];
    DWORD  n;
    LARGE_INTEGER pos;

    if (!lg || argc < 2)
        return 2;
    h = CreateFileA(argv[1], GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    fprintf(lg, "open %s -> %p\n", argv[1], h);
    if (h == INVALID_HANDLE_VALUE)
        return 3;
    for (i = 0; i < 5; i++) {
        OVERLAPPED ov;
        memset(&ov, 0, sizeof(ov));
        ov.Offset = offs[i];
        memset(a, 0xEE, 16);
        n = 0;
        fprintf(lg, "off %lu: overlapped ReadFile ok=%d ", offs[i], ReadFile(h, a, 16, &n, &ov));
        fprintf(lg, "n=%lu data=", n);
        hex(lg, a, 16);

        pos.QuadPart = offs[i];
        SetFilePointerEx(h, pos, NULL, FILE_BEGIN);
        memset(b, 0xEE, 16);
        n = 0;
        ReadFile(h, b, 16, &n, NULL);
        fprintf(lg, "  | seek+read n=%lu data=", n);
        hex(lg, b, 16);
        fprintf(lg, "  %s\n", memcmp(a, b, 16) ? "MISMATCH" : "same");
    }
    fprintf(lg, "DONE\n");
    fclose(lg);
    return 0;
}
