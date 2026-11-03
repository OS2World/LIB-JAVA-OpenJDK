/* dllprobe: LoadLibrary every DLL named on the command line (or in the
 * list file given with @file), logging before and after each load so a hang
 * or crash shows exactly which DLL was responsible. Console program.
 *
 * usage: dllprobe.exe <log> <dll> [dll...]      or   dllprobe.exe <log> @listfile
 */
#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <io.h>

static FILE *lg;

static void say(const char *fmt, const char *a, unsigned long b)
{
    fprintf(lg, fmt, a, b);
    fflush(lg);
    _commit(_fileno(lg));
}

static void probe(const char *dll)
{
    HMODULE h;

    say("LOAD   %s%s\n", dll, 0);
    SetLastError(0);
    h = LoadLibraryA(dll);
    if (h)
        say("OK     %s (module %08lx)\n", dll, (unsigned long)h);
    else
        say("FAIL   %s (error %lu)\n", dll, GetLastError());
}

int main(int argc, char **argv)
{
    int i;

    if (argc < 3)
        return 2;
    lg = fopen(argv[1], "w");
    if (!lg)
        return 3;
    if (argv[2][0] == '@') {
        FILE *f = fopen(argv[2] + 1, "r");
        char line[512];

        while (f && fgets(line, sizeof(line), f)) {
            line[strcspn(line, "\r\n")] = 0;
            if (line[0])
                probe(line);
        }
    } else {
        for (i = 2; i < argc; i++)
            probe(argv[i]);
    }
    say("DONE%s%s\n", "", 0);
    fclose(lg);
    return 0;
}
