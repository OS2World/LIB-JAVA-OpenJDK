/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#ifndef _JAVASOFT_JNI_MD_H_
#define _JAVASOFT_JNI_MD_H_

#if defined(SOLARIS) || defined(LINUX)
  #define JNIEXPORT
  #define JNIIMPORT
  #define JNICALL

  typedef int jint;

#ifdef _LP64
  typedef long jlong;
#else
  typedef long long jlong;
#endif

typedef signed char jbyte;

#else
  #define JNIEXPORT __declspec(dllexport)
  #define JNIIMPORT __declspec(dllimport)

#ifdef __OS2__
  #define JNICALL _System
#else
  #define JNICALL __stdcall
#endif

#ifdef TARGET_COMPILER_gcc
  typedef int jint;
  typedef __int64_t jlong;
  typedef char jbyte;
#else
  typedef int jint;
  typedef __int64 jlong;
  typedef signed char jbyte;
#endif
#endif

#endif /* !_JAVASOFT_JNI_MD_H_ */
