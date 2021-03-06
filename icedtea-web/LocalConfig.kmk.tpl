## @file
# Local Project Configuration File (Template)
#
# The local project configuration file is used to specify local paths to
# external tools and libraries and also to optioanlly override the global
# project configuration options.
#
# NOTES:
#
#   This file is a template! Copy it to a file named LocalConfig.kmk in 
#   the same directory and modify the copy to fit your local environment.
#
#   All paths in this file are specified using forward slashes unless specified
#   otherwise.
#

#
# Base directory where all build output will go. The directory will be created
# if does not exist. The default is "out" in the root of the source tree.
#
PATH_OUT_BASE := $(PATH_ROOT)/../$(PACKAGE_NAME)-build

#
# Path to bootstrap version of OpenJDK. Must be set.
#
PATH_BOOTSTRAP_JDK := $(PATH_ROOT)/../openjdk/build-product-release/j2sdk-image
