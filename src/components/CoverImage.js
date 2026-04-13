import React, { useState } from "react";
import { Image } from "react-native";
import { resolveAssetUrl } from "../api/openLibrary";

export function CoverImage({ uri, style, placeholder }) {
  const [failed, setFailed] = useState(false);
  const resolvedUri = resolveAssetUrl(uri);

  if (!resolvedUri || failed) {
    return placeholder;
  }

  return <Image source={{ uri: resolvedUri }} style={style} onError={() => setFailed(true)} />;
}
