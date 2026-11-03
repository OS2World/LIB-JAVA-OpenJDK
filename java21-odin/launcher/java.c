/* java.c - native OS/2 front end for Temurin 17 running through Odin32.
 *
 * Build once, copy under the name of each JDK tool (java.exe, javac.exe, jar.exe, ...):
 * the program name selects <jdk>\bin\<name>.exe, which is started through Odin's pec.exe.
 * Because this is an LX program, arguments like -Dname=value reach Java unchanged
 * (CMD .cmd files split them at '=').
 *
 * Build with -DJVER=17 (default, env JAVA17_HOME) or -DJVER=21 (env JAVA21_HOME).
 * Environment: JAVA<JVER>_HOME  JDK directory (default: parent of the directory of this exe,
 *                           if it holds bin\java.exe, else the JDK_DEFAULT path below)
 *              ODIN_DIR     directory with pec.exe and the Odin DLLs (default C:\odinrt\b2)
 *              JAVA<JVER>_NOARGS  if set, conf\odin.args is not passed
 */
#define INCL_DOS
#include <os2.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <process.h>
#include <io.h>

#ifndef JVER
#define JVER 17
#endif
#define XSTR(x) STR(x)
#define STR(x) #x
#define HOME_VAR  "JAVA" XSTR(JVER) "_HOME"
#if JVER == 21
#define JDK_DEFAULT "C:\\Java21\\jdk-21.0.13.0.3"
#else
#define JDK_DEFAULT "C:\\Java17\\jdk-17.0.17+10"
#endif

static char *dup_(const char *s) { return strdup(s); }

static int exists(const char *p) { return access(p, 0) == 0; }

/* quote an argument for the OS/2 command line (pec.exe passes it on as is) */
static char *quote(const char *a)
{
    char *q = (char *)malloc(strlen(a) * 2 + 3), *o = q;
    int needq = (*a == 0) || strpbrk(a, " \t\"") != NULL;
    if (needq) *o++ = '"';
    for (; *a; a++) {
        if (*a == '"') *o++ = '\\';
        *o++ = *a;
    }
    if (needq) *o++ = '"';
    *o = 0;
    return q;
}

int main(int argc, char **argv)
{
    char exe[CCHMAXPATH], home[CCHMAXPATH], tool[64], path[CCHMAXPATH], odin[CCHMAXPATH];
    char blp[2048], oldblp[1024];
    char **av;
    const char *p, *e;
    int n = 0, i, rc;
    char *s, *dot, *slash;

    /* tool name from argv[0] */
    strcpy(exe, argv[0]);
    for (s = exe; *s; s++) if (*s == '/') *s = '\\';
    slash = strrchr(exe, '\\');
    strcpy(tool, slash ? slash + 1 : exe);
    dot = strrchr(tool, '.');
    if (dot) *dot = 0;
    strlwr(tool);

    /* JDK home */
    e = getenv(HOME_VAR);
    if (e && *e) {
        strcpy(home, e);
    } else {
        PPIB ppib; PTIB ptib;
        home[0] = 0;
        DosGetInfoBlocks(&ptib, &ppib);
        if (ppib && ppib->pib_pchcmd) {
            char mod[CCHMAXPATH];
            if (DosQueryModuleName(ppib->pib_hmte, sizeof(mod), mod) == 0) {
                slash = strrchr(mod, '\\');
                if (slash) { *slash = 0; slash = strrchr(mod, '\\'); }
                if (slash) { *slash = 0; strcpy(home, mod); }
            }
        }
        sprintf(path, "%s\\bin\\java.exe", home);
        if (!home[0] || !exists(path)) strcpy(home, JDK_DEFAULT);
    }
    sprintf(path, "%s\\bin\\%s.exe", home, tool);
    if (!exists(path)) {
        fprintf(stderr, "%s: %s not found (set " HOME_VAR ")\n", tool, path);
        return 2;
    }

    e = getenv("ODIN_DIR");
    strcpy(odin, e && *e ? e : "C:\\odinrt\\b2");
    sprintf(blp, "%s\\pec.exe", odin);
    if (!exists(blp)) {
        fprintf(stderr, "%s: %s not found (set ODIN_DIR)\n", tool, blp);
        return 2;
    }

    /* BEGINLIBPATH = odin dir + old value */
    oldblp[0] = 0;
    DosQueryExtLIBPATH(oldblp, BEGIN_LIBPATH);
    sprintf(blp, "%s;%s", odin, oldblp);
    DosSetExtLIBPATH(blp, BEGIN_LIBPATH);

    av = (char **)calloc(argc + 4, sizeof(char *));
    sprintf(blp, "%s\\pec.exe", odin);
    av[n++] = dup_(blp);
    av[n++] = quote(path);
    if (!stricmp(tool, "java") && !getenv("JAVA" XSTR(JVER) "_NOARGS")) {
        sprintf(blp, "%s\\conf\\odin.args", home);
        if (exists(blp)) {
            char *a = (char *)malloc(strlen(blp) + 4);
            sprintf(a, "@%s", blp);
            av[n++] = quote(a);
        }
    }
    for (i = 1; i < argc; i++) av[n++] = quote(argv[i]);
    av[n] = NULL;

    rc = spawnv(P_WAIT, av[0], (const char * const *)av);
    if (rc == -1) {
        perror(av[0]);
        return 3;
    }
    return rc;
}
