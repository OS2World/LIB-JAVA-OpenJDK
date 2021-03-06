/* OS_OS2.cc

   Copyright (C) 2009, 2010  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

#define INCL_DOS
#define INCL_PM
#define INCL_ERRORS
#include <os2.h>

#include <stdlib.h>
#include <string.h>

#include "OS_OS2.h"
#include "OS_OS2_WinOS2.h"

const char *icedtea_web_data_dir()
{
    const char *home = getenv("ICEDTEA_WEB_DATA");
    if (!home)
    {
#if !defined(ICEDTEA_WEB_DATA_DIR)
        // deduce the path from this DLL's name
        static char buf[CCHMAXPATH] = {0};
        if (buf[0] == 0)
        {
            BOOL ok = FALSE;
            HMODULE hmod;
            ULONG objNum, offset;
            APIRET rc;
            rc = DosQueryModFromEIP(&hmod, &objNum, sizeof(buf), buf, &offset,
                                    (ULONG)&icedtea_web_data_dir);
            if (rc == NO_ERROR)
            {
                rc = DosQueryModuleName(hmod, sizeof(buf), buf);
                if (rc == NO_ERROR)
                {
                    // truncate the extension to get the data dir
                    char *end = strrchr(buf, '.');
                    if (end)
                    {
                        *end = '\0';
                        ok = TRUE;
                    }
                }
            }
            if (!ok)
                buf[0] = '\0';
        }
        home = buf;
#else
        home = ICEDTEA_WEB_DATA_DIR;
#endif
    }
    return home;
}

const char *icedtea_web_jre_dir()
{
    const char *jre = getenv("ICEDTEA_WEB_JRE");
    if (!jre)
    {
#if !defined(ICEDTEA_WEB_JRE_DIR)
        // deduce the path from JAVA.EXE found in PATH
        static char buf[CCHMAXPATH] = {0};
        if (buf[0] == 0)
        {
            BOOL ok = FALSE;
            APIRET rc;
            rc = DosSearchPath(SEARCH_IGNORENETERRS | SEARCH_ENVIRONMENT |
                               SEARCH_CUR_DIRECTORY,
                               "PATH", "JAVA.EXE", buf, sizeof(buf));
            if (rc == NO_ERROR)
            {
                char *end = strrchr(buf, '\\');
                if (end)
                {
                    *end = '\0';
                    // truncate \bin if present (otherwise this path is considered invalid)
                    end = strrchr(buf, '\\');
                    if (end && stricmp(end + 1, "bin") == 0)
                    {
                        *end = '\0';
                        ok = TRUE;
                    }
                }
            }
            if (!ok)
                buf[0] = '\0';
        }
        jre = buf;
#else
        jre = ICEDTEA_WEB_JRE_DIR;
#endif
    }
    return jre;
}

bool init_os()
{
    return init_os_winos2();
}

void done_os()
{
    done_os_winos2();
}
