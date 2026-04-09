import React from "react";
import { FlatList, Pressable, StyleSheet, Text, View } from "react-native";
import { BookCard } from "../components/BookCard";
import { useSaved } from "../state/SavedContext";
import { colors, spacing } from "../theme/colors";

export function SavedScreen({ navigation }) {
  const { favorites } = useSaved();

  return (
    <View style={styles.container}>
      <FlatList
        data={favorites}
        keyExtractor={(item) => item.workKey || item.id}
        contentContainerStyle={styles.content}
        ListHeaderComponent={
          <View style={styles.header}>
            <Text style={styles.title}>Your reading list</Text>
            <Text style={styles.body}>Saved books are stored locally on your device.</Text>
          </View>
        }
        ListEmptyComponent={
          <View style={styles.empty}>
            <Text style={styles.emptyTitle}>Nothing saved yet</Text>
            <Text style={styles.body}>Tap Save on any book to build your list.</Text>
          </View>
        }
        ItemSeparatorComponent={() => <View style={{ height: spacing.sm }} />}
        renderItem={({ item }) => <BookCard book={item} onPress={() => navigation.navigate("BookDetail", { book: item })} />}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: spacing.md, gap: spacing.md },
  header: { gap: spacing.xs },
  title: { fontSize: 24, fontWeight: "900", color: colors.text },
  body: { color: colors.textMuted },
  empty: { alignItems: "center", paddingVertical: spacing.xl, gap: spacing.xs },
  emptyTitle: { fontSize: 18, fontWeight: "800", color: colors.text }
});
