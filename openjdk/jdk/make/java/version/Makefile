#
# Copyright (c) 1999, 2007, Oracle and/or its affiliates. All rights reserved.
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
# Makefile to generate sun/misc/Version.java
#

BUILDDIR = ../..
include $(BUILDDIR)/common/Defs.gmk

all build: $(GENSRCDIR)/sun/misc/Version.java

$(GENSRCDIR)/sun/misc/Version.java: \
		$(SHARE_SRC)/classes/sun/misc/Version-template.java
	$(prep-target)
	$(RM) $@.temp
	$(SED) -e 's/@@launcher_name@@/$(LAUNCHER_NAME)/g' \
	    -e 's/@@java_version@@/$(RELEASE)/g' \
	    -e 's/@@java_runtime_version@@/$(FULL_VERSION)/g' \
	    -e 's/@@java_runtime_name@@/$(RUNTIME_NAME)/g' \
	$< > $@.temp
	@$(MV) $@.temp $@

clean clobber:
	$(RM) $(GENSRCDIR)/sun/misc/Version.java
