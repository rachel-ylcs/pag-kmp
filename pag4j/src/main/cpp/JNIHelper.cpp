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

#include "JNIHelper.h"
#include <cassert>
#include <string>
#include "JPAGLayerHandle.h"

extern "C" jint JNI_OnLoad(JavaVM* vm, void*) {
  if (vm) {
    //no-op to avoid libpag compile -Wall -Werror.
  }
  LOGI("PAG JNI_OnLoad Version: %s", pag::PAG::SDKVersion().c_str());
  return JNI_VERSION_1_4;
}

extern "C" void JNI_OnUnload(JavaVM*, void*) {

}

namespace pag {
jobject MakeRectFObject(JNIEnv* env, float x, float y, float width, float height) {
  jclass RectFClass = env->FindClass("org/libpag/PAGRect");
  if (RectFClass == nullptr) {
    env->ExceptionClear();
    LOGE("Could not run JNIHelper.MakeRectFObject(), RectFClass is not found!");
    return nullptr;
  }
  auto RectFConstructID = env->GetMethodID(RectFClass, "<init>", "(FFFF)V");
  return env->NewObject(RectFClass, RectFConstructID, x, y, x + width, y + height);
}

jobject ToPAGLayerJavaObject(JNIEnv* env, std::shared_ptr<pag::PAGLayer> pagLayer) {
  if (env == nullptr || pagLayer == nullptr) {
    return nullptr;
  }
  jobject layerObject = nullptr;
  switch (pagLayer->layerType()) {
    case pag::LayerType::PreCompose: {
      if (std::static_pointer_cast<pag::PAGComposition>(pagLayer)->isPAGFile()) {
        jclass PAGLayer_Class = env->FindClass("org/libpag/PAGFile");
        auto PAGLayer_Constructor = env->GetMethodID(PAGLayer_Class, "<init>", "(J)V");
        layerObject = env->NewObject(PAGLayer_Class, PAGLayer_Constructor,
                                     reinterpret_cast<jlong>(new JPAGLayerHandle(pagLayer)));
      }

      break;
    }
    default: {
        // todo:: add more type of PAGLayer
    }
  }
  return layerObject;
}

std::shared_ptr<pag::PAGLayer> ToPAGLayerNativeObject(JNIEnv* env, jobject jLayer) {
  if (env == nullptr || jLayer == nullptr) {
    return nullptr;
  }

  jclass PAGLayer_Class = env->FindClass("org/libpag/PAGLayer");
  if (PAGLayer_Class == nullptr) {
    env->ExceptionClear();
    LOGE("Could not run JNIHelper.ToPAGLayerNativeObject(), PAGLayer_Class is not found!");
    return nullptr;
  }
  auto PAGLayer_nativeContext = env->GetFieldID(PAGLayer_Class, "nativeContext", "J");

  auto nativeContext =
      reinterpret_cast<JPAGLayerHandle*>(env->GetLongField(jLayer, PAGLayer_nativeContext));
  if (nativeContext == nullptr) {
    return nullptr;
  }

  return nativeContext->get();
}

std::shared_ptr<pag::PAGComposition> ToPAGCompositionNativeObject(JNIEnv* env,
                                                                  jobject jComposition) {
  if (env == nullptr || jComposition == nullptr) {
    return nullptr;
  }

  jclass PAGComposition_Class = env->FindClass("org/libpag/PAGComposition");
  auto PAGComposition_nativeContext = env->GetFieldID(PAGComposition_Class, "nativeContext", "J");

  auto nativeContext = reinterpret_cast<JPAGLayerHandle*>(
      env->GetLongField(jComposition, PAGComposition_nativeContext));
  if (nativeContext == nullptr) {
    return nullptr;
  }

  return std::static_pointer_cast<pag::PAGComposition>(nativeContext->get());
}
}  // namespace pag
