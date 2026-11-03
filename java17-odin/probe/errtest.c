/* errtest: GetLastError() after failing file-system calls on a missing path.
 * java.nio maps these codes to NoSuchFileException etc. - error 0 breaks it. */
#include <windows.h>
#include <stdio.h>
#include <string.h>

static FILE *lg;

#define RES(name, expr, okcond)                                   \
    do {                                                          \
        DWORD r_; DWORD e_;                                       \
        SetLastError(0);                                          \
        r_ = (DWORD)(expr);                                       \
        e_ = GetLastError();                                      \
        fprintf(lg, "%-44s ret=%08lx err=%lu %s\n", name, r_, e_, (okcond) ? "" : "<-- CHECK"); \
    } while (0)

int main(void)
{
    WIN32_FILE_ATTRIBUTE_DATA fad;
    WIN32_FIND_DATAW fd;
    HANDLE h;
    static const wchar_t *missing  = L"D:\\OdinTests\\jt\\no_such_dir\\no_such_file";
    static const wchar_t *missing1 = L"D:\\OdinTests\\jt\\no_such_file";
    static const wchar_t *existing = L"D:\\OdinTests\\jt";
    static const wchar_t *tmpdir   = L"D:\\OdinTests\\jt\\errdir";
    DWORD attr;

    lg = fopen("D:/OdinTests/errtest.log", "w");
    if (!lg)
        return 2;
    RemoveDirectoryW(tmpdir);

    attr = GetFileAttributesW(missing);
    fprintf(lg, "%-44s ret=%08lx err=%lu (want ffffffff, 3)\n", "GetFileAttributesW(missing parent)", attr, GetLastError());
    SetLastError(0);
    attr = GetFileAttributesW(missing1);
    fprintf(lg, "%-44s ret=%08lx err=%lu (want ffffffff, 2)\n", "GetFileAttributesW(missing file)", attr, GetLastError());

    SetLastError(0);
    RES("GetFileAttributesExW(missing parent)", GetFileAttributesExW(missing, GetFileExInfoStandard, &fad), GetLastError() == 3);
    SetLastError(0);
    RES("GetFileAttributesExW(missing file)", GetFileAttributesExW(missing1, GetFileExInfoStandard, &fad), GetLastError() == 2);
    SetLastError(0);
    RES("GetFileAttributesExW(existing dir)", GetFileAttributesExW(existing, GetFileExInfoStandard, &fad), 1);

    SetLastError(0);
    h = FindFirstFileW(missing, &fd);
    fprintf(lg, "%-44s ret=%p err=%lu (want invalid, 3)\n", "FindFirstFileW(missing parent)", h, GetLastError());
    SetLastError(0);
    h = FindFirstFileW(missing1, &fd);
    fprintf(lg, "%-44s ret=%p err=%lu (want invalid, 2)\n", "FindFirstFileW(missing file)", h, GetLastError());

    SetLastError(0);
    h = CreateFileW(missing, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    fprintf(lg, "%-44s ret=%p err=%lu (want invalid, 3)\n", "CreateFileW(open missing parent)", h, GetLastError());
    SetLastError(0);
    h = CreateFileW(missing1, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    fprintf(lg, "%-44s ret=%p err=%lu (want invalid, 2)\n", "CreateFileW(open missing file)", h, GetLastError());
    SetLastError(0);
    h = CreateFileW(existing, FILE_READ_ATTRIBUTES, FILE_SHARE_READ | FILE_SHARE_WRITE, NULL, OPEN_EXISTING,
                    FILE_FLAG_BACKUP_SEMANTICS, NULL);
    fprintf(lg, "%-44s ret=%p err=%lu (want valid)\n", "CreateFileW(dir, BACKUP_SEMANTICS)", h, GetLastError());
    if (h != INVALID_HANDLE_VALUE)
        CloseHandle(h);

    SetLastError(0);
    RES("CreateDirectoryW(missing parent)", CreateDirectoryW(missing, NULL), GetLastError() == 3);
    SetLastError(0);
    RES("CreateDirectoryW(new)", CreateDirectoryW(tmpdir, NULL), 1);
    SetLastError(0);
    RES("CreateDirectoryW(already exists)", CreateDirectoryW(tmpdir, NULL), GetLastError() == 183);
    SetLastError(0);
    RES("RemoveDirectoryW(missing)", RemoveDirectoryW(missing1), GetLastError() == 2);
    SetLastError(0);
    RES("RemoveDirectoryW(existing)", RemoveDirectoryW(tmpdir), 1);
    SetLastError(0);
    RES("DeleteFileW(missing)", DeleteFileW(missing1), GetLastError() == 2);
    SetLastError(0);
    RES("MoveFileExW(missing source)", MoveFileExW(missing1, L"D:\\OdinTests\\jt\\x2", 0), GetLastError() == 2);
    SetLastError(0);
    RES("CopyFileW(missing source)", CopyFileW(missing1, L"D:\\OdinTests\\jt\\x3", FALSE), GetLastError() == 2);
    fprintf(lg, "DONE\n");
    fclose(lg);
    return 0;
}
