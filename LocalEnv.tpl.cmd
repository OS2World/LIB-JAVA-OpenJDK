/*
 * Local environment settings for OpenJDK (Template)
 *
 * This file is used to specify local paths to external tools and libraries
 * and to perform other site-specific project environment setup.
 *
 * NOTES:
 *
 *   This file is a template! Copy it to a file named LocalEnv.cmd in
 *   the same directory and modify the copy to fit your local environment.
 *
 *   All paths in this file are specified using back slashes unless specified
 *   otherwise.
 */

/**
 * Path to JDK used for bootstrapping. Must be set.
 *
 * Note that when using a previous OpenJDK build here, you must make sure
 * that all it needs (e.g. a corresponding Odin runtime) is on LIBPATH.
 */
G.PATH_TOOL_BOOT_JDK    = 'D:\Dev\openjdk6'

/**
 * Path to GCC 4 environment script (+ possible arguments). If you use
 * the GCC 4 distribution recommended in README, the path will look like:
 *
 *   X:\path_to_gcc4\gcc4xx.cmd
 *
 * Otherwise you will have to play with GCC's gccenv.cmd on your own.
 *
 * Leave this empty if you have GCC 4 already available in your
 * environment.
 */
/*G.PATH_TOOL_GCC4_ENV    = 'D:\Dev\gcc452\gcc452.cmd'*/

/**
 * Path to Apache Ant. Leave empty if you have it already available
 * in your environment.
 */
G.PATH_TOOL_ANT         = 'D:\Dev\java\apache-ant-1.8.1'

/**
 * Path to the Odin32 SDK. Must be set. Assumes the source build and should
 * point to the root of the source tree. The locaiton of separate parts, if
 * differs from the default layout, can be individually tailored below.
 */
G.PATH_SDK_ODIN32_SRCTREE   = 'D:\Coding\odin\odin32'

G.PATH_SDK_ODIN32_HEADERS   = G.PATH_SDK_ODIN32_SRCTREE'\include'
G.PATH_SDK_ODIN32_LIBS      = G.PATH_SDK_ODIN32_SRCTREE'\out\os2.x86\release\stage\lib'
G.PATH_SDK_ODIN32_DBGLIBS   = G.PATH_SDK_ODIN32_SRCTREE'\out\os2.x86\debug\stage\lib'
G.PATH_SDK_ODIN32_BIN       = G.PATH_SDK_ODIN32_SRCTREE'\out\os2.x86\release\stage\bin'
G.PATH_SDK_ODIN32_DBGBIN    = G.PATH_SDK_ODIN32_SRCTREE'\out\os2.x86\debug\stage\bin'

/**
 * Path to the previous Java SDK where components not built from the
 * current source tree will be imported from. Normally, it is not used and
 * should be just the same as G.PATH_TOOL_BOOT_JDK.
 */
G.PATH_JDK_IMPORT       = G.PATH_TOOL_BOOT_JDK

/**
 * Log file to save all console output of the build process (only works when
 * using the SE script). Leave it empty to disable logging.
 */
G.LOG_FILE              = 'se.log' /* ScriptDir'se.log' */

/**
 * The number of make jobs to run in parallel. A value of N+1 (where N is
 * the number of CPUs on the build machine) will greatly improve the build
 * speed.
 */
G.MAKE_JOBS             = '2'

/*
 * Advanced definitions
 * ----------------------------------------------------------------------------
 *
 * Used mostly for developing and debugging.
 */

/**
 * Path to the debug version of kLIBC DLL (used with -l flag). Optional.
 */
G.PATH_TOOL_KLIBC_DEBUG     = ''

/**
 * Path to the log check version of kLIBC DLL (used with -L flag). Optional.
 */
G.PATH_TOOL_KLIBC_LOGCHK    = ''

/*
 * Additional environment settings
 * ----------------------------------------------------------------------------
 *
 * Controls various aspects of the build process.
 */

/**
 * Directory where to find JAXP/JAXWS source bundles. If this is not set (or if
 * the specified directory doesn't contain the bundles), ALLOW_DOWNLOADS below
 * must be set to 'true'.
 */
/*
call EnvSet 'ALT_DROPS_DIR', 'D:/Coding/java_src_drops'
*/

/**
 * Instructs the build process to download JAXP/JAXWS source bundles if they
 * are missing when building JAXP/JAXWS.
 */
call EnvSet 'ALLOW_DOWNLOADS', 'true'

/**
 * Only build the client hotspot JVM
 */
/*
call EnvSet 'BUILD_CLIENT_ONLY', '1'
*/

/**
 * Here you may put any additional environment variable definitions needed for
 * your local environment or for the OpenJDK make files using the form shown
 * below. These variables are passed to the environment without any
 * modifications.
 */
/*
call EnvSet 'ALT_SOME_VAR', 'some value'
*/

/*
 * Production definitions
 * ----------------------------------------------------------------------------
 *
 * Not intended for playing around with.
 */

/**
 * JDK update (marketing) number. Corresponds to the OpenJDK source bundle
 * build (release) number. Should be updated with each upstream update.
 */
call EnvSet 'JDK_UPDATE_VERSION', '25'

/**
 * Build number in format 'bNN[-sometext]'. While it might seem like this one
 * should reflect the source bundle build number, in fact it doesn't -- it
 * reflects the serial build number for the given platform instead (which allows
 * to distinguish several distributed builds of the same upstream release), as
 * well as the symbolic build name (optional).
 */
call EnvSet 'BUILD_NUMBER', 'b00-test'

/*
call EnvSet 'COMPILE_APPROACH', 'parallel'
call EnvSet 'ALT_PARALLEL_COMPILE_JOBS', '2'
call EnvSet 'HOTSPOT_BUILD_JOBS', '2'
 */
