#
# Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

DEFAULTACTIONS=clean post_update create

default:: $(SUBDIRS)

ifndef DIR
DIR=.
endif

MAKE_JOBS=
ifdef HOTSPOT_BUILD_JOBS
MAKE_JOBS=-j$(HOTSPOT_BUILD_JOBS)
endif

ifdef SUBDIRS
$(SUBDIRS): FORCE
	@if [ ! -d $@ ]; then mkdir -p $@; fi; \
	if [ ! -r $@/local.make ]; then echo \# Empty > $@/local.make; fi; \
	echo $(MAKE) $(ACTION) in $(DIR)/$@; \
	cd $@; $(MAKE) $(MAKE_JOBS) -f $(WorkSpace)/make/os2/makefiles/$@.make $(ACTION) DIR=$(DIR)/$@ BUILD_FLAVOR=$(BUILD_FLAVOR)
endif

# Creates the needed directory
create::
ifneq ($(DIR),.)
	@echo mkdir $(DIR)
endif

# Epilog to update for generating derived files
post_update::

# Removes scrap files
clean:: FORCE
	-@rm -f *.OLD *.publish

# Remove all scrap files and all generated files
pure:: clean
	-@rm -f *.OLD *.publish

$(DEFAULTACTIONS) $(ACTIONS)::
ifdef SUBDIRS
	@$(MAKE) $(MAKE_JOBS) ACTION=$@ DIR=$(DIR)
endif

FORCE:
