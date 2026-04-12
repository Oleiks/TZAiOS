import React, { useMemo, useState } from "react";
import { FlatList, Pressable, StyleSheet, Text, TextInput, View } from "react-native";
import { searchBooks } from "../api/openLibrary";
import { BookCard } from "../components/BookCard";
import { LoadingView } from "../components/LoadingView";
import { useSaved } from "../state/SavedContext";
import { colors, spacing } from "../theme/colors";

export function SearchScreen({ navigation }) {
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const { recentSearches, addRecentSearch } = useSaved();

  const hints = useMemo(() => ["Dune", "Octavia Butler", "poetry", "history"], []);

  async function runSearch(term = query) {
    if (!term.trim()) return;
    setLoading(true);
    setSearched(true);
    try {
      const data = await searchBooks(term.trim(), 1);
      setResults(data.items);
      addRecentSearch(term.trim());
    } finally {
      setLoading(false);
    }
  }

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      keyboardShouldPersistTaps="handled"
      data={searched ? results : []}
      keyExtractor={(item) => item.id || item.workKey}
      ItemSeparatorComponent={() => <View style={{ height: spacing.sm }} />}
      renderItem={({ item }) => <BookCard book={item} onPress={() => navigation.navigate("BookDetail", { book: item })} />}
      ListHeaderComponent={
        <View style={{ gap: spacing.lg }}>
          <View style={styles.searchBox}>
            <Text style={styles.label}>Search books, authors, or subjects</Text>
            <TextInput
              testID="search-input"
              value={query}
              onChangeText={setQuery}
              placeholder="Try 'Moby Dick' or 'science fiction'"
              placeholderTextColor={colors.textMuted}
              style={styles.input}
              returnKeyType="search"
              onSubmitEditing={() => runSearch()}
            />
            <Pressable testID="search-button" style={styles.button} onPress={() => runSearch()}>
              <Text style={styles.buttonText}>Search</Text>
            </Pressable>
          </View>

          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Quick searches</Text>
            <View style={styles.chips}>
              {hints.map((hint) => (
                <Pressable
                  key={hint}
                  style={styles.chip}
                  onPress={() => {
                    setQuery(hint);
                    runSearch(hint);
                  }}
                >
                  <Text style={styles.chipText}>{hint}</Text>
                </Pressable>
              ))}
            </View>
          </View>

          {recentSearches.length ? (
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Recent searches</Text>
              <View style={styles.chips}>
                {recentSearches.map((item) => (
                  <Pressable
                    key={item}
                    style={styles.chip}
                    onPress={() => {
                      setQuery(item);
                      runSearch(item);
                    }}
                  >
                    <Text style={styles.chipText}>{item}</Text>
                  </Pressable>
                ))}
              </View>
            </View>
          ) : null}

          {loading ? <LoadingView label="Searching the catalog..." /> : null}
        </View>
      }
      ListEmptyComponent={!loading && searched ? <Text style={styles.empty}>No results found.</Text> : null}
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: spacing.md, gap: spacing.lg },
  searchBox: {
    borderRadius: 20,
    padding: spacing.lg,
    backgroundColor: colors.surface,
    borderWidth: 1,
    borderColor: colors.border,
    gap: spacing.sm
  },
  label: { color: colors.text, fontWeight: "800", fontSize: 16 },
  input: {
    backgroundColor: colors.card,
    borderColor: colors.border,
    borderWidth: 1,
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 12,
    color: colors.text
  },
  button: {
    backgroundColor: colors.primary,
    borderRadius: 14,
    alignItems: "center",
    paddingVertical: 12
  },
  buttonText: { color: "white", fontWeight: "800" },
  section: { gap: spacing.sm },
  sectionTitle: { fontSize: 18, fontWeight: "800", color: colors.text },
  chips: { flexDirection: "row", flexWrap: "wrap", gap: spacing.xs },
  chip: {
    backgroundColor: colors.primarySoft,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 999
  },
  chipText: { color: colors.primary, fontWeight: "700" },
  empty: { color: colors.textMuted, textAlign: "center", paddingVertical: spacing.md }
});
