/////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Tencent is pleased to support the open source community by making libpag available.
//
//  Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
//  except in compliance with the License. You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  unless required by applicable law or agreed to in writing, software distributed under the
//  license is distributed on an "as is" basis, without warranties or conditions of any kind,
//  either express or implied. see the license for the specific language governing permissions
//  and limitations under the license.
//
/////////////////////////////////////////////////////////////////////////////////////////////////

#include "JPAGSurface.h"
#include "JNIHelper.h"

namespace pag {
static jfieldID PAGSurface_nativeSurface;
}  // namespace pag

using namespace pag;

std::shared_ptr<PAGSurface> getPAGSurface(JNIEnv* env, jobject thiz) {
  auto pagSurface =
      reinterpret_cast<JPAGSurface*>(env->GetLongField(thiz, PAGSurface_nativeSurface));
  if (pagSurface == nullptr) {
    return nullptr;
  }
  return pagSurface->get();
}

extern "C" {

JNIEXPORT void JNICALL Java_org_libpag_PAGSurface_nativeInit(JNIEnv* env, jclass clazz) {
  PAGSurface_nativeSurface = env->GetFieldID(clazz, "nativeSurface", "J");
}

JNIEXPORT void JNICALL Java_org_libpag_PAGSurface_nativeRelease(JNIEnv* env, jobject thiz) {
  auto jPAGSurface =
      reinterpret_cast<JPAGSurface*>(env->GetLongField(thiz, PAGSurface_nativeSurface));
  if (jPAGSurface != nullptr) {
    jPAGSurface->clear();
  }
}

JNIEXPORT void JNICALL Java_org_libpag_PAGSurface_nativeFinalize(JNIEnv* env, jobject thiz) {
  auto old = reinterpret_cast<JPAGSurface*>(env->GetLongField(thiz, PAGSurface_nativeSurface));
  delete old;
  env->SetLongField(thiz, PAGSurface_nativeSurface, (jlong)thiz);
}

JNIEXPORT jint JNICALL Java_org_libpag_PAGSurface_width(JNIEnv* env, jobject thiz) {
  auto surface = getPAGSurface(env, thiz);
  if (surface == nullptr) {
    return 0;
  }
  return surface->width();
}

JNIEXPORT jint JNICALL Java_org_libpag_PAGSurface_height(JNIEnv* env, jobject thiz) {
  auto surface = getPAGSurface(env, thiz);
  if (surface == nullptr) {
    return 0;
  }
  return surface->height();
}

JNIEXPORT void JNICALL Java_org_libpag_PAGSurface_updateSize(JNIEnv* env, jobject thiz) {
  auto surface = getPAGSurface(env, thiz);
  if (surface == nullptr) {
    return;
  }
  surface->updateSize();
}

JNIEXPORT jboolean JNICALL Java_org_libpag_PAGSurface_clearAll(JNIEnv* env, jobject thiz) {
  auto surface = getPAGSurface(env, thiz);
  if (surface == nullptr) {
    return static_cast<jboolean>(false);
  }
  auto changed = static_cast<jboolean>(surface->clearAll());
  return changed;
}

JNIEXPORT void JNICALL Java_org_libpag_PAGSurface_freeCache(JNIEnv* env, jobject thiz) {
  auto surface = getPAGSurface(env, thiz);
  if (surface == nullptr) {
    return;
  }
  surface->freeCache();
}

JNIEXPORT jlong JNICALL Java_org_libpag_PAGSurface_SetupOffscreen(JNIEnv*, jclass, jint width,
                                                                  jint height) {
  auto surface = PAGSurface::MakeOffscreen(width, height);
  if (surface == nullptr) {
    LOGE("PAGSurface.SetupOffscreen(): Failed to create a offscreen PAGSurface!");
    return 0;
  }
  return reinterpret_cast<jlong>(new JPAGSurface(surface));
}

JNIEXPORT jboolean JNICALL Java_org_libpag_PAGSurface_copyPixelsTo(JNIEnv* env, jobject thiz,
                                                                   jbyteArray pixels, jint stride) {
  if (thiz == nullptr || pixels == nullptr) {
    return false;
  }
  auto surface = getPAGSurface(env, thiz);
  if (surface == nullptr) {
    return false;
  }
  jbyte* pixelBuffer = env->GetByteArrayElements(pixels, nullptr);
  if (pixelBuffer == nullptr) {
    return false;
  }
  bool success = surface->readPixels(pag::ColorType::RGBA_8888, pag::AlphaType::Premultiplied,
                                     pixelBuffer, stride);
  env->ReleaseByteArrayElements(pixels, pixelBuffer, 0);
  return success;
}
}