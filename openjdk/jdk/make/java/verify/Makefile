#
# Copyright (c) 1999, 2005, Oracle and/or its affiliates. All rights reserved.
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

#
# Build libverify.so
#

BUILDDIR = ../..
PRODUCT = java
LIBRARY = verify
include $(BUILDDIR)/common/Defs.gmk

ifeq ($(PLATFORM), os2)
LIBRARY_SHORT = jverify
endif

#
# Link to libjvm so JVM_ functions can be called.
#
OTHER_LDLIBS += $(JVMLIB)

ifeq ($(PLATFORM), windows)
  #
  # Don't want JAVALIB
  #
  JAVALIB = 
  EXTRA_LIBS = 
  # Files built here do not compile with warning level 3 if warnings are fatal
  COMPILER_WARNINGS_FATAL=false
endif

#
# Files to compile.
#
FILES_c = \
	check_code.c \
	check_format.c

#
# libverify.so needs these 2 header files (opcodes.h opcodes.length) 
# from the VM.
#
CPPFLAGS += -I$(SHARE_SRC)/javavm/include 

#
# Targets.
#
ifeq ($(PLATFORM), solaris)
  ifneq ($(ARCH), amd64)
    FILES_reorder += reorder-$(ARCH)
  endif
endif
include $(BUILDDIR)/common/Mapfile-vers.gmk
include $(BUILDDIR)/common/Library.gmk

#
# Add to the ambient VPATH.
#
vpath %.c   $(SHARE_SRC)/native/common

