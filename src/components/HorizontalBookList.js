import React from "react";
import { FlatList, Image, Pressable, StyleSheet, Text, View } from "react-native";
import { colors, spacing } from "../theme/colors";

export function HorizontalBookList({ title, books, onPressBook }) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      <FlatList
        horizontal
        data={books}
        keyExtractor={(item) => item.id || item.workKey}
        contentContainerStyle={styles.listContent}
        showsHorizontalScrollIndicator={false}
        renderItem={({ item }) => (
          <Pressable style={styles.item} onPress={() => onPressBook(item)}>
            {item.coverUrl ? <Image source={{ uri: item.coverUrl }} style={styles.cover} /> : <View style={styles.coverPlaceholder} />}
            <Text numberOfLines={2} style={styles.itemTitle}>
              {item.title}
            </Text>
            <Text numberOfLines={1} style={styles.author}>
              {item.authorName}
            </Text>
          </Pressable>
        )}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: spacing.sm
  },
  title: {
    fontSize: 20,
    fontWeight: "800",
    color: colors.text,
    marginHorizontal: spacing.md
  },
  listContent: {
    paddingHorizontal: spacing.md,
    gap: spacing.sm
  },
  item: {
    width: 136,
    padding: spacing.sm,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    gap: spacing.xs
  },
  cover: {
    width: "100%",
    height: 182,
    borderRadius: 10,
    backgroundColor: colors.primarySoft
  },
  coverPlaceholder: {
    width: "100%",
    height: 182,
    borderRadius: 10,
    backgroundColor: colors.primarySoft
  },
  itemTitle: {
    fontSize: 13,
    color: colors.text,
    fontWeight: "700"
  },
  author: {
    fontSize: 12,
    color: colors.textMuted
  }
});
