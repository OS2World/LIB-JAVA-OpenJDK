#
# Copyright (c) 2007, 2010, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the LICENSE file that accompanied this code.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

# Makefile for langtools: wrapper around Ant build.xml file

#
# On Solaris, the standard 'make' utility will not work with these makefiles.
#    This little rule is only understood by Solaris make, and is harmless
#    when seen by the GNU make tool. If using Solaris make, this causes the
#    make command to fail.
#
SUN_MAKE_TEST:sh = @echo "ERROR: PLEASE USE GNU VERSION OF MAKE"; exit 33

#
# Minimal platform defs
# Need FullPath because we can't rely on gnumake abspath, until we use v3.81
#

SYSTEM_UNAME := $(shell uname)

# Where is unwanted output to be delivered?
# On Windows, MKS uses the special file "NUL", cygwin uses the customary unix file.
ifeq ($(SYSTEM_UNAME), Windows_NT)
DEV_NULL = NUL
else
DEV_NULL = /dev/null 
endif

ifneq (,$(findstring CYGWIN,$(SYSTEM_UNAME)))
USING_CYGWIN = true
endif

ifdef USING_CYGWIN
define FullPath
$(shell cygpath -a -s -m $1 2> $(DEV_NULL))
endef
else
define FullPath
$(shell cd $1 2> $(DEV_NULL) && pwd)
endef
endif

#
# Makefile args
#

ifdef QUIET
  ANT_OPTIONS += -quiet
endif

ifdef VERBOSE
  ANT_OPTIONS += -verbose -debug
endif

ifdef JDK_VERSION
  ANT_OPTIONS += -Djdk.version=$(JDK_VERSION)
endif

ifdef FULL_VERSION
  ANT_OPTIONS += -Dfull.version='$(FULL_VERSION)' # will contain spaces
endif

ifdef MILESTONE
ifneq ($(MILESTONE),fcs)
  ANT_OPTIONS += -Dmilestone=$(MILESTONE)
else
  ANT_OPTIONS += -Drelease=$(JDK_VERSION)
endif
endif

ifdef BUILD_NUMBER
  ANT_OPTIONS += -Dbuild.number=$(BUILD_NUMBER)
else
  ifdef JDK_BUILD_NUMBER
    ANT_OPTIONS += -Dbuild.number=$(JDK_BUILD_NUMBER)
  endif
endif

ifeq ($(VARIANT), DBG)
  ANT_OPTIONS += -Djavac.debug=true
else
  ifeq ($(VARIANT), OPT)
    ANT_OPTIONS += -Djavac.debug=false
  endif
endif

ifeq ($(DEBUG_CLASSFILES), true)
  ANT_OPTIONS += -Djavac.debug=true
  ANT_OPTIONS += -Ddebug.classfiles=true
endif

# Note: jdk/make/common/Defs.gmk uses LANGUAGE_VERSION (-source NN)
# and the somewhat misnamed CLASS_VERSION (-target NN)
ifdef TARGET_CLASS_VERSION
  ANT_OPTIONS += -Djavac.target=$(TARGET_CLASS_VERSION)
else
  ifdef JAVAC_TARGET_ARG
    ANT_OPTIONS += -Djavac.target=$(JAVAC_TARGET_ARG)
  endif
endif

ifdef SOURCE_LANGUAGE_VERSION
  ANT_OPTIONS += -Djavac.source=$(SOURCE_LANGUAGE_VERSION)
else
  ifdef JAVAC_SOURCE_ARG
    ANT_OPTIONS += -Djavac.source=$(JAVAC_SOURCE_ARG)
  endif
endif

ifdef ALT_BOOTDIR
  ANT_OPTIONS += -Dboot.java.home=$(ALT_BOOTDIR)
  ANT_JAVA_HOME = JAVA_HOME=$(ALT_BOOTDIR)
endif

ifdef ALT_OUTPUTDIR
  OUTPUTDIR = $(ALT_OUTPUTDIR)
  ANT_OPTIONS += -Dbuild.dir=$(ALT_OUTPUTDIR)/build
  ANT_OPTIONS += -Ddist.dir=$(ALT_OUTPUTDIR)/dist
else
  OUTPUTDIR = ..
endif
#ABS_OUTPUTDIR = $(abspath $(OUTPUTDIR))
ABS_OUTPUTDIR = $(call FullPath,$(OUTPUTDIR))

ANT_TMPDIR = $(ABS_OUTPUTDIR)/build/ant-tmp
ANT_OPTS = ANT_OPTS=-Djava.io.tmpdir='$(ANT_TMPDIR)'

ifdef FINDBUGS_HOME
  ANT_OPTIONS += -Dfindbugs.home=$(FINDBUGS_HOME)
endif

ifeq ($(PLATFORM), os2)
  # system classpath isn't necessary for the build but it's usually long (due to
  # ant jars) and passing it to javac started through cmd.exe (as in case of
  # fork=yes) causes cmd.exe to crash with SYS0008, so disable this
  ANT_OPTIONS += -Dbuild.sysclasspath=ignore
endif

ifdef ANT_HOME
  ifeq ($(PLATFORM), windows)
    ANT = $(subst \,/,$(ANT_HOME))/bin/ant.bat
  else
  ifeq ($(PLATFORM), os2)
    ANT = $(subst \,/,$(ANT_HOME))/bin/ant.cmd
  else
    ANT = $(ANT_HOME)/bin/ant
  endif
  endif
  ifneq ($(shell test -x $(ANT); echo $$?), 0)
    $(error $(ANT) not found -- please update ANT_HOME)
  endif
else
  ANT = ant
  ifneq ($(shell which $(ANT) > /dev/null; echo $$?), 0)
    $(error 'ant' not found -- please set ANT_HOME or put 'ant' on your PATH)
  endif
endif

# Default target and expected 'do everything' target
all: build

# Standard make clobber target
clobber: clean

# All ant targets of interest
ANT_TARGETS = build clean sanity post-sanity diagnostics # for now

# Create diagnostics log (careful, ant 1.8.0 -diagnostics always does an exit 1)
# ant 1.8.1 -version does exit 666 here, ignore this
$(OUTPUTDIR)/build/ant-diagnostics.log:
	@mkdir -p $(OUTPUTDIR)/build $(ANT_TMPDIR)
	@$(RM) $@
	$(ANT_JAVA_HOME) $(ANT_OPTS) $(ANT) -diagnostics > $@
	-$(ANT_JAVA_HOME) $(ANT_OPTS) $(ANT) -version >> $@

# Create a make target for each
$(ANT_TARGETS): $(OUTPUTDIR)/build/ant-diagnostics.log
	@ mkdir -p $(OUTPUTDIR)/build $(ANT_TMPDIR)
	$(ANT_JAVA_HOME) $(ANT_OPTS) $(ANT) $(ANT_OPTIONS) $@

#-------------------------------------------------------------------
#
# Targets for Oracle's internal JPRT build system

CD = cd
ZIP = zip

JPRT_ARCHIVE_BUNDLE=$(ABS_OUTPUTDIR)/$(JPRT_BUILD_FLAVOR)-bundle.zip

jprt_build_product jprt_build_debug jprt_build_fastdebug: all
	( $(CD) $(OUTPUTDIR) && \
	  $(ZIP) -q -r $(JPRT_ARCHIVE_BUNDLE) build dist )

#-------------------------------------------------------------------

# Declare these phony (not filenames)
.PHONY: $(ANT_TARGETS) all clobber \
	jprt_build_product jprt_build_debug jprt_build_fastdebug
