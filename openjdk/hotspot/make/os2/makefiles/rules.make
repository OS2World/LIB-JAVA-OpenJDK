#
# Copyright 2003-2009 Sun Microsystems, Inc.  All Rights Reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
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
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 USA or visit www.sun.com if you need additional information or
# have any questions.
#
#

# These are the commands used externally to compile and run.

ifdef BootStrapDir
RUN_JAVA=$(BootStrapDir)/bin/java
RUN_JAVAP=$(BootStrapDir)/bin/javap
RUN_JAVAH=$(BootStrapDir)/bin/javah
RUN_JAR=$(BootStrapDir)/bin/jar
COMPILE_JAVAC=$(BootStrapDir)/bin/javac $(BOOTSTRAP_JAVAC_FLAGS)
COMPILE_RMIC=$(BootStrapDir)/bin/rmic
BOOT_JAVA_HOME=$(BootStrapDir)
else
RUN_JAVA=java
RUN_JAVAP=javap
RUN_JAVAH=javah
RUN_JAR=jar
COMPILE_JAVAC=javac $(BOOTSTRAP_JAVAC_FLAGS)
COMPILE_RMIC=rmic
BOOT_JAVA_HOME=
endif

# Settings for javac
BOOT_SOURCE_LANGUAGE_VERSION=5
BOOT_TARGET_CLASS_VERSION=5
JAVAC_FLAGS=-g -encoding ascii
BOOTSTRAP_JAVAC_FLAGS=$(JAVAC_FLAGS) -source $(BOOT_SOURCE_LANGUAGE_VERSION) -target $(BOOT_TARGET_CLASS_VERSION)

