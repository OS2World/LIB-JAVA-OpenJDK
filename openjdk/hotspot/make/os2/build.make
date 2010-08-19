#
# Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

# Note: this makefile is invoked both from build.cmd and from the J2SE
# control workspace in exactly the same manner; the required
# environment variables (Variant, WorkSpace, BootStrapDir, BuildUser, HOTSPOT_BUILD_VERSION)
# are passed in as command line arguments.

# SA components are built if BUILD_SA_SA=1 is specified.
# See notes in README. This produces files:
#  1. sa-jdi.jar       - This is built before building jvm.dll
#  2. sawindbg[_g].dll - Native library for SA - This is built after jvm.dll
#                      - Also, .lib, .map.
#
# Please refer to ./makefiles/sa.make

# If we haven't set an ARCH yet use x86
ifndef ARCH
ARCH=x86
endif

# At this point we should be certain that ARCH has a definition
# now determine the BUILDARCH
#

# the default BUILDARCH
BUILDARCH=i486

ARCH_TEXT=

ifndef CC_INTERP
FORCE_TIERED=1
endif

ifeq ($(BUILDARCH),i486)
Platform_arch=x86
Platform_arch_model=x86_32
endif

variantDir = os2_$(BUILDARCH)_$(Variant)

realVariant=$(Variant)
VARIANT_TEXT=Core
ifeq ($(Variant),compiler1)
VARIANT_TEXT=Client
else
  ifeq ($(Variant),compiler2)
    ifdef FORCE_TIERED
VARIANT_TEXT=Server
realVariant=tiered
    else
VARIANT_TEXT=Server
    endif
  else
    ifeq ($(Variant),tiered)
VARIANT_TEXT=Tiered
    else
      ifeq ($(Variant),kernel)
VARIANT_TEXT=Kernel
      endif
    endif
  endif
endif

#########################################################################
# Parameters for VERSIONINFO resource for jvm[_g].dll.
# These can be overridden via the nmake.exe command line.
# They are overridden by RE during the control builds.
#

include $(WorkSpace)/make/hotspot_version

# Define HOTSPOT_VM_DISTRO based on settings in make/openjdk_distro.
ifndef HOTSPOT_VM_DISTRO
include $(WorkSpace)/make/openjdk_distro
endif

# Following the Web Start / Plugin model here....
# We can have update versions like "01a", but Windows requires
# we use only integers in the file version field.  So:
# JDK_UPDATE_VER = JDK_UPDATE_VERSION * 10 + EXCEPTION_VERSION
#
JDK_UPDATE_VER=0
JDK_BUILD_NUMBER=0

HS_FILEDESC=$(HOTSPOT_VM_DISTRO) $(ARCH_TEXT) $(VARIANT_TEXT) VM

# JDK ProductVersion:
# 1.5.0_<wx>-b<yz> will have DLL version 5.0.wx*10.yz
# Thus, 1.5.0_10-b04  will be 5.0.100.4
#       1.6.0-b01     will be 6.0.0.1
#       1.6.0_01a-b02 will be 6.0.11.2
#
# JDK_* variables are defined in make/hotspot_version or on command line
#
JDK_VER=$(JDK_MINOR_VER),$(JDK_MICRO_VER),$(JDK_UPDATE_VER),$(JDK_BUILD_NUMBER)
JDK_DOTVER=$(JDK_MINOR_VER).$(JDK_MICRO_VER).$(JDK_UPDATE_VER).$(JDK_BUILD_NUMBER)
ifeq ($(JRE_RELEASE_VERSION),)
JRE_RELEASE_VER=$(JDK_MAJOR_VER).$(JDK_MINOR_VER).$(JDK_MICRO_VER)
else
JRE_RELEASE_VER=$(JRE_RELEASE_VERSION)
endif
ifeq ($(JDK_MKTG_VERSION),)
JDK_MKTG_VERSION=$(JDK_MINOR_VER).$(JDK_MICRO_VER)
endif

# Hotspot Express VM FileVersion:
# 10.0-b<yz> will have DLL version 10.0.0.yz (need 4 numbers).
#
# HS_* variables are defined in make/hotspot_version
#
HS_VER=$(HS_MAJOR_VER),$(HS_MINOR_VER),0,$(HS_BUILD_NUMBER)
HS_DOTVER=$(HS_MAJOR_VER).$(HS_MINOR_VER).0.$(HS_BUILD_NUMBER)

ifeq ($(HOTSPOT_RELEASE_VERSION),)
HOTSPOT_RELEASE_VERSION=$(HS_MAJOR_VER).$(HS_MINOR_VER)-b$(HS_BUILD_NUMBER)
endif

ifeq ($(HOTSPOT_BUILD_VERSION),)
HS_BUILD_VER=$(HOTSPOT_RELEASE_VERSION)
else
HS_BUILD_VER=$(HOTSPOT_RELEASE_VERSION)-$(HOTSPOT_BUILD_VERSION)
endif

# End VERSIONINFO parameters

checkSA::

ifneq ($(BUILD_OS2_SA),1)
checkSA::
	@echo     Not building SA:  BUILD_OS2_SA != 1
endif

#########################################################################

# With the jvm_g.dll now being named jvm.dll, we can't build both and place
#   the dll's in the same directory, so we only build one at a time,
#   re-directing the output to different output directories (done by user
#   of this makefile).
#
defaultTarget: product

# The product or release build is an optimized build, and is the default

# note that since all the build targets depend on local.make that BUILDARCH
# and Platform_arch and Platform_arch_model will get set in local.make
# and there is no need to pass them thru here on the command line
#
product release optimized: checks $(variantDir) $(variantDir)/local.make sanity
	cd $(variantDir); \
	$(MAKE) -f $(WorkSpace)/make/os2/makefiles/top.make BUILD_FLAVOR=product ARCH=$(ARCH)

# The debug or jvmg (all the same thing) is an optional build
debug jvmg: checks $(variantDir) $(variantDir)/local.make sanity
	cd $(variantDir); \
	$(MAKE) -f $(WorkSpace)/make/os2/makefiles/top.make BUILD_FLAVOR=debug ARCH=$(ARCH)
fastdebug: checks $(variantDir) $(variantDir)/local.make sanity
	cd $(variantDir); \
	$(MAKE) -f $(WorkSpace)/make/os2/makefiles/top.make BUILD_FLAVOR=fastdebug ARCH=$(ARCH)

develop: checks $(variantDir) $(variantDir)/local.make sanity
	cd $(variantDir); \
	$(MAKE) -f $(WorkSpace)/make/os2/makefiles/top.make BUILD_FLAVOR=product DEVELOP=1 ARCH=$(ARCH)

sanity:
	@ echo; \
	cd $(variantDir); \
	$(MAKE) -f $(WorkSpace)/make/os2/makefiles/sanity.make; \
	cd ..; \
	echo;

clean: checkVariant
	- rm -r -f $(variantDir)

$(variantDir):
	mkdir -p $(variantDir)

$(variantDir)/local.make: checks
	@ echo \# Generated file					>  $@
	@ echo Variant=$(realVariant)				>> $@
	@ echo WorkSpace=$(WorkSpace)				>> $@
	@ echo BootStrapDir=$(BootStrapDir)			>> $@
	@ $(if $(USERNAME),echo BuildUser=$(USERNAME) >> $@)
	@ echo HS_VER=$(HS_VER)					>> $@
	@ echo HS_DOTVER=$(HS_DOTVER)				>> $@
	@ echo HS_COMPANY=$(COMPANY_NAME)			>> $@
	@ echo HS_FILEDESC=$(HS_FILEDESC)			>> $@
	@ echo HOTSPOT_VM_DISTRO=$(HOTSPOT_VM_DISTRO)		>> $@
	@ echo HS_COPYRIGHT=$(HOTSPOT_VM_COPYRIGHT)		>> $@
	@ echo HS_NAME=$(PRODUCT_NAME) $(JDK_MKTG_VERSION)	>> $@
	@ echo HS_BUILD_VER=$(HS_BUILD_VER)			>> $@
	@ echo BUILD_OS2_SA=$(BUILD_OS2_SA)    			>> $@
	@ echo SA_BUILD_VERSION=$(HS_BUILD_VER)                 >> $@
	@ echo SA_INCLUDE=$(SA_INCLUDE)      			>> $@
	@ echo SA_LIB=$(SA_LIB)         			>> $@
	@ echo JDK_VER=$(JDK_VER)				>> $@
	@ echo JDK_DOTVER=$(JDK_DOTVER)				>> $@
	@ echo JRE_RELEASE_VER=$(JRE_RELEASE_VER)		>> $@
	@ echo BUILDARCH=$(BUILDARCH)         			>> $@
	@ echo Platform_arch=$(Platform_arch)        		>> $@
	@ echo Platform_arch_model=$(Platform_arch_model)	>> $@

checks: checkVariant checkWorkSpace checkSA

checkVariant:
	@ $(if $(filter tiered compiler2 compiler1 kernel core,$(Variant)),,\
		echo ERROR: Need to specify "Variant=[tiered|compiler2|compiler1|kernel|core]" && false)

checkWorkSpace:
	@ $(if $(WorkSpace),,echo ERROR: Need to specify "WorkSpace=..." && false)

checkBuildID:
	@ $(if $(BuildID),,echo ERROR: Need to specify "BuildID=..." && false)
