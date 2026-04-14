import React, { useState } from "react";
import { Image } from "react-native";
import * as openLibrary from "../api/openLibrary";

export function CoverImage({ uri, style, placeholder }) {
  const [failed, setFailed] = useState(false);
  const resolvedUri = typeof openLibrary.resolveAssetUrl === "function" ? openLibrary.resolveAssetUrl(uri) : uri;

  if (!resolvedUri || failed) {
    return placeholder;
  }

  return <Image source={{ uri: resolvedUri }} style={style} onError={() => setFailed(true)} />;
}
