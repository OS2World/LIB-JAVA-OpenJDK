/* dsprobe: the DirectSound calls the Java sound library makes to open a playback line. */
#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

typedef HRESULT (__stdcall *PFN_DSCREATE)(void *guid, void **ppDS, void *outer);
typedef struct {
    DWORD dwSize, dwFlags, dwBufferBytes, dwReserved;
    WAVEFORMATEX *lpwfxFormat;
    BYTE guid3D[16];
} DSBD;

#define VT(obj, i) ((void **)(*(void ***)(obj)))[i]

int main(int argc, char **argv)
{
    HMODULE m;
    PFN_DSCREATE create;
    void *ds = NULL, *buf = NULL;
    HRESULT hr;
    WAVEFORMATEX wf;
    DSBD d;
    HWND w;
    DWORD flags = 0x10000 | 0x8000;   /* GETCURRENTPOSITION2 | GLOBALFOCUS */

    freopen("D:\\OdinTests\\dsprobe_user.txt", "w", stdout);
    setvbuf(stdout, NULL, _IONBF, 0);
    if (getenv("DSPROBE_WIN")) {
        /* a window makes Odin set up PM (message queue) for this thread first */
        HWND hw = CreateWindowExA(0, "STATIC", "dsprobe", WS_POPUP, 0, 0, 10, 10, NULL, NULL, GetModuleHandleA(NULL), NULL);
        printf("CreateWindow -> %p err=%lu\n", hw, GetLastError());
    }
    m = LoadLibraryA("dsound.dll");
    printf("LoadLibrary dsound -> %p err=%lu\n", m, GetLastError());
    if (!m) return 1;
    create = (PFN_DSCREATE)GetProcAddress(m, "DirectSoundCreate");
    printf("DirectSoundCreate -> %p\n", create);
    if (!create) return 2;
    hr = create(NULL, &ds, NULL);
    printf("DirectSoundCreate() -> %08lx ds=%p\n", hr, ds);
    if (hr < 0) return 3;

    w = GetForegroundWindow();
    if (!w) w = GetDesktopWindow();
    hr = ((HRESULT (__stdcall *)(void *, HWND, DWORD))VT(ds, 6))(ds, w, 1 /* DSSCL_NORMAL */);
    printf("SetCooperativeLevel(hwnd=%p, NORMAL) -> %08lx\n", w, hr);

    memset(&wf, 0, sizeof(wf));
    wf.wFormatTag = WAVE_FORMAT_PCM;
    wf.nChannels = 2;
    wf.nSamplesPerSec = 44100;
    wf.wBitsPerSample = 16;
    wf.nBlockAlign = 4;
    wf.nAvgBytesPerSec = 44100 * 4;
    wf.cbSize = 0;

    memset(&d, 0, sizeof(d));
    d.dwSize = (argc > 1) ? 20 : 36;
    d.dwFlags = (argc > 2) ? 0x8000 : flags;
    d.dwBufferBytes = 2 * 44100 * 4;
    d.lpwfxFormat = &wf;
    hr = ((HRESULT (__stdcall *)(void *, void *, void **, void *))VT(ds, 3))(ds, &d, &buf, NULL);
    printf("CreateSoundBuffer(size=%lu flags=%08lx bytes=%lu) -> %08lx buf=%p\n", d.dwSize, d.dwFlags, d.dwBufferBytes, hr, buf);
    if (hr >= 0 && buf) {
        DWORD st = 0;
        void *p1, *p2; DWORD n1 = 0, n2 = 0;
        hr = ((HRESULT (__stdcall *)(void *, DWORD *))VT(buf, 9))(buf, &st);
        printf("GetStatus -> %08lx status=%08lx\n", hr, st);
        hr = ((HRESULT (__stdcall *)(void *, DWORD, DWORD, void **, DWORD *, void **, DWORD *, DWORD))VT(buf, 11))(buf, 0, 4096, &p1, &n1, &p2, &n2, 0);
        printf("Lock(0,4096) -> %08lx n1=%lu n2=%lu\n", hr, n1, n2);
        if (hr >= 0) {
            memset(p1, 0, n1);
            hr = ((HRESULT (__stdcall *)(void *, void *, DWORD, void *, DWORD))VT(buf, 19))(buf, p1, n1, p2, n2);
            printf("Unlock -> %08lx\n", hr);
        }
        hr = ((HRESULT (__stdcall *)(void *, DWORD, DWORD, DWORD))VT(buf, 12))(buf, 0, 0, 1 /* LOOPING */);
        printf("Play(LOOPING) -> %08lx\n", hr);
        Sleep(500);
        hr = ((HRESULT (__stdcall *)(void *))VT(buf, 18))(buf);
        printf("Stop -> %08lx\n", hr);
        ((ULONG (__stdcall *)(void *))VT(buf, 2))(buf);
    }
    ((ULONG (__stdcall *)(void *))VT(ds, 2))(ds);
    return 0;
}
