/* dartprobe: opens the MMPM/2 AMP/MIXER device the way Odin's dsound (dart.cpp) does.
   OS/2 native program; MDM.DLL is loaded dynamically so that load problems are reported. */
#define INCL_DOS
#define INCL_WIN
#define INCL_OS2MM
#define INCL_MCIOS2
#include <os2.h>
#include <os2me.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef ULONG (APIENTRY *PFN_MCI)(USHORT, USHORT, ULONG, PVOID, USHORT);
typedef ULONG (APIENTRY *PFN_ERR)(ULONG, PSZ, USHORT);

static PFN_ERR pErr;

static void err(const char *what, ULONG rc)
{
    char buf[200];
    buf[0] = 0;
    if (pErr) pErr(rc, (PSZ)buf, sizeof(buf));
    printf("%s -> rc=%lu (0x%lx) '%s'\n", what, rc, rc, buf);
}

int main(void)
{
    MCI_AMP_OPEN_PARMS open;
    MCI_MIXSETUP_PARMS mix;
    MCI_BUFFER_PARMS bp;
    MCI_MIX_BUFFER bufs[4];
    HMODULE hmod;
    char fail[260];
    PFN_MCI pMci;
    ULONG rc;
    PFN_ERR pe = NULL;

    freopen("D:\\OdinTests\\dartprobe.txt", "w", stdout);
    setvbuf(stdout, NULL, _IONBF, 0);
    printf("start\n");

    if (getenv("DART_PM")) {
        HAB hab = WinInitialize(0);
        HMQ hmq = WinCreateMsgQueue(hab, 0);
        printf("WinInitialize hab=%lu hmq=%lu\n", (unsigned long)hab, (unsigned long)hmq);
    }
#ifdef STATIC_MDM
    /* linked against mmpm2.lib: MDM.DLL is loaded by the OS/2 loader before main() */
    (void)hmod; (void)fail; (void)pe;
    pMci = (PFN_MCI)mciSendCommand;
    pErr = (PFN_ERR)mciGetErrorString;
    printf("static link: mciSendCommand at %p\n", (void *)pMci);
#else
    rc = DosLoadModule((PSZ)fail, sizeof(fail), (PSZ)"MDM", &hmod);
    printf("DosLoadModule(MDM) -> %lu (%s)\n", rc, fail);
    if (rc) return 1;
    rc = DosQueryProcAddr(hmod, 0, (PSZ)"mciSendCommand", (PFN *)&pMci);
    printf("DosQueryProcAddr(mciSendCommand) -> %lu\n", rc);
    if (rc) return 2;
    if (DosQueryProcAddr(hmod, 0, (PSZ)"mciGetErrorString", (PFN *)&pe) == 0) pErr = pe;
#endif

    memset(&open, 0, sizeof(open));
    open.usDeviceID = 0;
    open.pszDeviceType = (PSZ)MAKEULONG(MCI_DEVTYPE_AUDIO_AMPMIX, 0);
    rc = pMci(0, MCI_OPEN, MCI_WAIT | MCI_OPEN_TYPE_ID | MCI_OPEN_SHAREABLE, &open, 0);
    err("MCI_OPEN (AMPMIX, shareable)", rc);
    if (rc) return 3;
    printf("device id %u\n", open.usDeviceID);

    memset(&mix, 0, sizeof(mix));
    mix.ulBitsPerSample = 16;
    mix.ulFormatTag = MCI_WAVE_FORMAT_PCM;
    mix.ulSamplesPerSec = 22500;
    mix.ulChannels = 2;
    mix.ulFormatMode = MCI_PLAY;
    mix.ulDeviceType = MCI_DEVTYPE_WAVEFORM_AUDIO;
    mix.pmixEvent = NULL;
    rc = pMci(open.usDeviceID, MCI_MIXSETUP, MCI_WAIT | MCI_MIXSETUP_INIT, &mix, 0);
    err("MCI_MIXSETUP (22500 Hz 16 bit stereo, NULL event)", rc);

    memset(&bp, 0, sizeof(bp));
    mix.ulBufferSize = 4096;
    bp.ulNumBuffers = 4;
    bp.ulBufferSize = 4096;
    bp.pBufList = bufs;
    rc = pMci(open.usDeviceID, MCI_BUFFER, MCI_WAIT | MCI_ALLOCATE_MEMORY, &bp, 0);
    err("MCI_BUFFER allocate", rc);
    if (!rc) {
        rc = pMci(open.usDeviceID, MCI_BUFFER, MCI_WAIT | MCI_DEALLOCATE_MEMORY, &bp, 0);
        err("MCI_BUFFER deallocate", rc);
    }
    rc = pMci(open.usDeviceID, MCI_CLOSE, MCI_WAIT, NULL, 0);
    err("MCI_CLOSE", rc);
    return 0;
}
