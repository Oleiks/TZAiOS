import React, { useEffect, useState } from "react";
import { RefreshControl, ScrollView, StyleSheet, Text, View } from "react-native";
import { getSubjectBooks } from "../api/openLibrary";
import { HorizontalBookList } from "../components/HorizontalBookList";
import { LoadingView } from "../components/LoadingView";
import { colors, spacing } from "../theme/colors";

const FEATURED_SUBJECTS = [
  { key: "fiction", title: "Trending Now" },
  { key: "fantasy", title: "Fantasy Escape" },
  { key: "history", title: "History Picks" }
];

export function HomeScreen({ navigation }) {
  const [sections, setSections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    load();
  }, []);

  async function load() {
    setLoading(true);
    setError("");
    try {
      const data = await Promise.all(FEATURED_SUBJECTS.map((subject) => getSubjectBooks(subject.key, 16)));
      const merged = FEATURED_SUBJECTS.map((meta, index) => ({
        title: meta.title,
        key: meta.key,
        books: data[index].works
      }));
      setSections(merged);
    } catch (err) {
      setError(err.message || "Could not load featured books.");
    } finally {
      setLoading(false);
    }
  }

  async function refresh() {
    setRefreshing(true);
    await load();
    setRefreshing(false);
  }

  const onPressBook = (book) => {
    navigation.navigate("BookDetail", { book });
  };

  if (loading) return <LoadingView label="Gathering featured shelves..." />;

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.content}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={refresh} tintColor={colors.primary} />}
    >
      <View style={styles.heroCard}>
        <Text style={styles.heroEyebrow}>Open Library Explorer</Text>
        <Text style={styles.heroTitle}>Discover your next favorite read.</Text>
        <Text style={styles.heroBody}>Browse live books by subject, open detailed pages, and save titles for later.</Text>
      </View>

      {error ? <Text style={styles.error}>{error}</Text> : null}

      {sections.map((section) => (
        <HorizontalBookList key={section.key} title={section.title} books={section.books} onPressBook={onPressBook} />
      ))}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background
  },
  content: {
    paddingVertical: spacing.md,
    gap: spacing.lg
  },
  heroCard: {
    marginHorizontal: spacing.md,
    padding: spacing.lg,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface,
    gap: spacing.xs
  },
  heroEyebrow: {
    color: colors.accent,
    textTransform: "uppercase",
    fontWeight: "800",
    letterSpacing: 0.5,
    fontSize: 11
  },
  heroTitle: {
    color: colors.text,
    fontSize: 26,
    lineHeight: 30,
    fontWeight: "900"
  },
  heroBody: {
    color: colors.textMuted,
    fontSize: 15,
    lineHeight: 21
  },
  error: {
    color: colors.danger,
    marginHorizontal: spacing.md
  }
});
