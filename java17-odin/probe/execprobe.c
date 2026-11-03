/* execprobe: DosExecPgm the program named on the command line and report the loader's failure information. */
#define INCL_DOS
#include <os2.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char **argv)
{
    char obj[260], args[300];
    RESULTCODES res;
    ULONG rc;
    FILE *f;
    int k;

    f = fopen("D:\\OdinTests\\execprobe.txt", "w");
    if (!f) return 1;
    if (argc < 2) { fprintf(f, "usage\n"); fclose(f); return 2; }
    memset(obj, 0, sizeof(obj));
    memset(args, 0, sizeof(args));
    strcpy(args, argv[1]);
    args[strlen(argv[1]) + 1] = 0;
    rc = DosExecPgm((PCHAR)obj, sizeof(obj), EXEC_SYNC, (PSZ)args, NULL, &res, (PSZ)argv[1]);
    fprintf(f, "DosExecPgm(%s) -> rc=%lu term=%lu result=%lu failname='", argv[1], rc,
            (unsigned long)res.codeTerminate, (unsigned long)res.codeResult);
    for (k = 0; k < 100 && obj[k]; k++) fputc((obj[k] >= 32 && obj[k] < 127) ? obj[k] : '?', f);
    fprintf(f, "'\n");
    fclose(f);
    return 0;
}
