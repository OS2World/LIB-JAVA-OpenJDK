/* jarprobe: replays what the Java launcher's manifest parser does with a jar file
   (ucrtbase _open/_lseeki64/_read -> CreateFile, GetFileType, SetFilePointerEx, ReadFile). */
#include <windows.h>
#include <stdio.h>
#include <string.h>

int main(int argc, char **argv)
{
    HANDLE h;
    LARGE_INTEGER pos, newpos, zero;
    DWORD n, err, type;
    unsigned char buf[64];
    BOOL ok;

    if (argc < 2) { printf("usage: jarprobe file\n"); return 1; }
    h = CreateFileA(argv[1], GENERIC_READ, FILE_SHARE_READ | FILE_SHARE_WRITE, NULL, OPEN_EXISTING,
                    FILE_ATTRIBUTE_NORMAL, NULL);
    printf("CreateFile -> %p err=%lu\n", h, GetLastError());
    if (h == INVALID_HANDLE_VALUE) return 2;
    type = GetFileType(h);
    printf("GetFileType -> %lu\n", type);

    zero.QuadPart = 0;
    SetLastError(0);
    ok = SetFilePointerEx(h, zero, &newpos, FILE_END);
    printf("SetFilePointerEx(0, FILE_END) -> %d newpos=%ld err=%lu\n", ok, (long)newpos.LowPart, GetLastError());

    pos.QuadPart = (LONGLONG)newpos.LowPart - 22;
    SetLastError(0);
    ok = SetFilePointerEx(h, pos, &newpos, FILE_BEGIN);
    printf("SetFilePointerEx(size-22, FILE_BEGIN) -> %d newpos=%ld err=%lu\n", ok, (long)newpos.LowPart, GetLastError());

    n = 0;
    memset(buf, 0, sizeof(buf));
    SetLastError(0);
    ok = ReadFile(h, buf, 22, &n, NULL);
    printf("ReadFile(22) -> %d n=%lu err=%lu  sig=%02x %02x %02x %02x\n", ok, n, GetLastError(),
           buf[0], buf[1], buf[2], buf[3]);

    pos.QuadPart = -22;
    SetLastError(0);
    ok = SetFilePointerEx(h, pos, &newpos, FILE_END);
    printf("SetFilePointerEx(-22, FILE_END) -> %d newpos=%ld err=%lu\n", ok, (long)newpos.LowPart, GetLastError());

    /* the same with the old API */
    SetLastError(0);
    n = SetFilePointer(h, -22, NULL, FILE_END);
    printf("SetFilePointer(-22, FILE_END) -> %lu err=%lu\n", n, GetLastError());
    memset(buf, 0, sizeof(buf));
    ok = ReadFile(h, buf, 22, &n, NULL);
    printf("ReadFile(22) -> %d n=%lu  sig=%02x %02x %02x %02x\n", ok, n, buf[0], buf[1], buf[2], buf[3]);

    /* from the start */
    SetFilePointer(h, 0, NULL, FILE_BEGIN);
    ok = ReadFile(h, buf, 4, &n, NULL);
    printf("ReadFile at 0 -> %d n=%lu  sig=%02x %02x %02x %02x\n", ok, n, buf[0], buf[1], buf[2], buf[3]);
    CloseHandle(h);
    return 0;
}
