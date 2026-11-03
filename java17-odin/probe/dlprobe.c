/* dlprobe: DosLoadModule each module named on the command line and log the result. */
#define INCL_DOS
#include <os2.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char **argv)
{
    int i;
    FILE *f = fopen("D:\OdinTests\dlprobe.txt", "w");
    if (!f) return 1;
    for (i = 1; i < argc; i++) {
        char fail[260];
        HMODULE h = 0;
        ULONG rc;
        int k;
        memset(fail, 0, sizeof(fail));
        rc = DosLoadModule((PSZ)fail, sizeof(fail), (PSZ)argv[i], &h);
        fprintf(f, "%s -> rc=%lu hmod=%lu failname='", argv[i], rc, (unsigned long)h);
        for (k = 0; k < 40 && fail[k]; k++) fputc((fail[k] >= 32 && fail[k] < 127) ? fail[k] : '?', f);
        fprintf(f, "'\n");
        fflush(f);
    }
    fclose(f);
    return 0;
}
