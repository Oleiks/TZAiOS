import React, { useState } from "react";
import { FlatList, Pressable, StyleSheet, Text, View } from "react-native";
import { getSubjectBooks } from "../api/openLibrary";
import { BookCard } from "../components/BookCard";
import { LoadingView } from "../components/LoadingView";
import { colors, spacing } from "../theme/colors";

const SUBJECTS = ["fiction", "fantasy", "science_fiction", "history", "romance", "mystery"];

export function SubjectScreen({ navigation }) {
  const [subject, setSubject] = useState("fiction");
  const [loading, setLoading] = useState(false);
  const [items, setItems] = useState([]);

  React.useEffect(() => {
    load(subject);
  }, []);

  async function load(next) {
    setSubject(next);
    setLoading(true);
    try {
      const data = await getSubjectBooks(next, 20);
      setItems(data.works);
    } finally {
      setLoading(false);
    }
  }

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={loading ? [] : items}
      keyExtractor={(item) => item.id || item.workKey}
      ItemSeparatorComponent={() => <View style={{ height: spacing.sm }} />}
      renderItem={({ item }) => <BookCard book={item} onPress={() => navigation.navigate("BookDetail", { book: item })} />}
      ListHeaderComponent={
        <View style={styles.chips}>
          {SUBJECTS.map((item) => (
            <Pressable key={item} style={[styles.chip, item === subject && styles.chipActive]} onPress={() => load(item)}>
              <Text style={[styles.chipText, item === subject && styles.chipTextActive]}>{item.replace(/_/g, " ")}</Text>
            </Pressable>
          ))}
          {loading ? <LoadingView label={`Loading ${subject} books...`} /> : null}
        </View>
      }
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: spacing.md, gap: spacing.md },
  chips: { flexDirection: "row", flexWrap: "wrap", gap: spacing.xs },
  chip: {
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 999,
    paddingHorizontal: 12,
    paddingVertical: 8
  },
  chipActive: { backgroundColor: colors.primary },
  chipText: { color: colors.text, fontWeight: "700" },
  chipTextActive: { color: "white" }
});
