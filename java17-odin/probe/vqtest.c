/* vqtest: check VirtualAlloc/VirtualQuery semantics the way HotSpot's
 * os::win32::find_mapping / os::pd_release_memory rely on them. */
#include <windows.h>
#include <stdio.h>

static FILE *lg;

static const char *st(DWORD s)
{
    return s == MEM_FREE ? "FREE" : s == MEM_RESERVE ? "RESERVE" : s == MEM_COMMIT ? "COMMIT" : "?";
}

/* same walk as find_mapping(); prints every region */
static void walk(const char *what, char *addr)
{
    MEMORY_BASIC_INFORMATION mi;
    char *abase, *end;
    unsigned long total = 0;
    int n = 0;

    fprintf(lg, "-- %s: find_mapping(%p)\n", what, addr);
    if (!VirtualQuery(addr, &mi, sizeof(mi))) {
        fprintf(lg, "   VirtualQuery failed err=%lu\n", GetLastError());
        return;
    }
    fprintf(lg, "   at addr: base=%p allocbase=%p size=%08lx state=%s prot=%lx type=%lx\n",
            mi.BaseAddress, mi.AllocationBase, (unsigned long)mi.RegionSize, st(mi.State),
            mi.Protect, mi.Type);
    if (mi.State == MEM_FREE)
        return;
    abase = (char *)mi.AllocationBase;
    end = abase;
    for (;;) {
        if (!VirtualQuery(end, &mi, sizeof(mi)) || mi.State == MEM_FREE ||
            (char *)mi.AllocationBase != abase)
            break;
        fprintf(lg, "   region %d: base=%p size=%08lx state=%s prot=%lx allocprot=%lx\n", n,
                mi.BaseAddress, (unsigned long)mi.RegionSize, st(mi.State), mi.Protect,
                mi.AllocationProtect);
        end += mi.RegionSize;
        n++;
        if (n > 40) {
            fprintf(lg, "   ... too many regions, stop\n");
            break;
        }
    }
    total = (unsigned long)(end - abase);
    fprintf(lg, "   mapping: base=%p size=%08lx regions=%d\n", abase, total, n);
    fflush(lg);
}

int main(void)
{
    char *p, *q;
    DWORD sz = 0x0a800000;      /* the 168 MB heap reservation HotSpot makes */

    lg = fopen("D:/OdinTests/vqtest.log", "w");
    if (!lg)
        return 2;
    p = (char *)VirtualAlloc(NULL, sz, MEM_RESERVE, PAGE_READWRITE);
    fprintf(lg, "reserve %08lx -> %p (err %lu)\n", sz, p, GetLastError());
    if (!p)
        return 3;
    walk("after reserve", p);

    q = (char *)VirtualAlloc(p, 0x10000, MEM_COMMIT, PAGE_READWRITE);
    fprintf(lg, "commit first 64K -> %p\n", q);
    walk("after commit head", p);

    q = (char *)VirtualAlloc(p + 0x400000, 0x20000, MEM_COMMIT, PAGE_READWRITE);
    fprintf(lg, "commit 128K at +4M -> %p\n", q);
    walk("after commit middle", p);

    walk("query inside middle", p + 0x401000);

    fprintf(lg, "decommit middle -> %d\n", VirtualFree(p + 0x400000, 0x20000, MEM_DECOMMIT));
    walk("after decommit", p);

    fprintf(lg, "release -> %d (err %lu)\n", VirtualFree(p, 0, MEM_RELEASE), GetLastError());
    walk("after release", p);

    /* two adjacent reservations must not merge in the walk */
    {
        char *a = (char *)VirtualAlloc(NULL, 0x100000, MEM_RESERVE, PAGE_NOACCESS);
        char *b = (char *)VirtualAlloc(a + 0x100000, 0x100000, MEM_RESERVE, PAGE_NOACCESS);
        fprintf(lg, "adjacent: a=%p b=%p\n", a, b);
        if (b) {
            walk("adjacent a", a);
            walk("adjacent b", b);
        }
    }
    fprintf(lg, "DONE\n");
    fclose(lg);
    return 0;
}
