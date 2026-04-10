import React from "react";
import { Image, Pressable, StyleSheet, Text, View } from "react-native";
import { resolveAssetUrl } from "../api/openLibrary";
import { colors, spacing } from "../theme/colors";

export function BookCard({ book, onPress, rightSlot }) {
  return (
    <Pressable style={styles.card} onPress={onPress}>
      {book.coverUrl ? <Image source={{ uri: resolveAssetUrl(book.coverUrl) }} style={styles.cover} /> : <View style={styles.placeholder} />}
      <View style={styles.body}>
        <Text numberOfLines={2} style={styles.title}>
          {book.title}
        </Text>
        <Text numberOfLines={1} style={styles.author}>
          {book.authorName}
        </Text>
        <View style={styles.metaRow}>
          {book.year ? <Text style={styles.meta}>{book.year}</Text> : null}
          {book.editionCount ? <Text style={styles.meta}>{book.editionCount} editions</Text> : null}
        </View>
        <View style={styles.tagsRow}>
          {(book.subjects || []).slice(0, 2).map((subject) => (
            <View key={subject} style={styles.tag}>
              <Text numberOfLines={1} style={styles.tagText}>
                {subject}
              </Text>
            </View>
          ))}
        </View>
      </View>
      {rightSlot ? <View style={styles.right}>{rightSlot}</View> : null}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.card,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.sm,
    flexDirection: "row",
    gap: spacing.sm,
    alignItems: "flex-start"
  },
  cover: {
    width: 72,
    height: 106,
    borderRadius: 10,
    backgroundColor: colors.primarySoft
  },
  placeholder: {
    width: 72,
    height: 106,
    borderRadius: 10,
    backgroundColor: colors.primarySoft
  },
  body: {
    flex: 1,
    gap: spacing.xs
  },
  right: {
    marginTop: 2
  },
  title: {
    fontSize: 16,
    fontWeight: "700",
    color: colors.text
  },
  author: {
    color: colors.textMuted,
    fontSize: 13
  },
  metaRow: {
    flexDirection: "row",
    gap: spacing.sm
  },
  meta: {
    color: colors.textMuted,
    fontSize: 12
  },
  tagsRow: {
    marginTop: 4,
    flexDirection: "row",
    gap: spacing.xs
  },
  tag: {
    maxWidth: 110,
    backgroundColor: colors.primarySoft,
    borderRadius: 999,
    paddingHorizontal: 8,
    paddingVertical: 3
  },
  tagText: {
    fontSize: 11,
    color: colors.primary
  }
});
