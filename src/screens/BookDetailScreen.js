import React, { useEffect, useState } from "react";
import { Image, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { extractDescription, getAuthor, getBookDetails, getBookEditions, getCoverUrl, resolveAssetUrl } from "../api/openLibrary";
import { LoadingView } from "../components/LoadingView";
import { SegmentedTabs } from "../components/SegmentedTabs";
import { useSaved } from "../state/SavedContext";
import { colors, spacing } from "../theme/colors";

export function BookDetailScreen({ navigation, route }) {
  const { book } = route.params;
  const [tab, setTab] = useState("overview");
  const [details, setDetails] = useState(null);
  const [editions, setEditions] = useState([]);
  const [author, setAuthor] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const { isFavorite, toggleFavorite } = useSaved();

  useEffect(() => {
    let mounted = true;

    async function load() {
      setLoading(true);
      setError("");
      try {
        const workKey = book.workKey || book.id;
        const detailData = workKey && workKey.startsWith("/works/") ? await getBookDetails(workKey) : null;
        const canonicalWorkKey = detailData?.key || workKey;
        const authorKey = book.authorKey || detailData?.authors?.[0]?.author?.key;
        const [editionsResult, authorResult] = await Promise.allSettled([
          canonicalWorkKey && canonicalWorkKey.startsWith("/works/") ? getBookEditions(canonicalWorkKey, 12) : Promise.resolve([]),
          authorKey ? getAuthor(authorKey) : Promise.resolve(null)
        ]);

        if (!mounted) return;
        setDetails(detailData);
        setEditions(editionsResult.status === "fulfilled" ? editionsResult.value : []);
        setAuthor(authorResult.status === "fulfilled" ? authorResult.value : null);
      } catch (err) {
        if (mounted) setError(err.message || "Could not load this book.");
      } finally {
        if (mounted) setLoading(false);
      }
    }

    load();
    return () => {
      mounted = false;
    };
  }, [book]);

  const cover = resolveAssetUrl(book.coverUrl) || getCoverUrl({ coverId: details?.covers?.[0] });
  const title = details?.title || book.title;
  const description = extractDescription(details?.description);
  const favorite = isFavorite(book);

  if (loading) return <LoadingView label="Opening the book..." />;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <View style={styles.hero}>
        {cover ? <Image source={{ uri: cover }} style={styles.cover} /> : <View style={styles.coverPlaceholder} />}
        <View style={styles.heroBody}>
          <Text style={styles.title}>{title}</Text>
          <Text style={styles.author}>{book.authorName}</Text>
          <View style={styles.actions}>
            <Pressable style={[styles.actionButton, favorite && styles.actionButtonActive]} onPress={() => toggleFavorite(book)}>
              <Text style={[styles.actionText, favorite && styles.actionTextActive]}>{favorite ? "Saved" : "Save"}</Text>
            </Pressable>
            {book.authorKey ? (
              <Pressable style={styles.actionButton} onPress={() => navigation.navigate("Author", { authorKey: book.authorKey, authorName: book.authorName })}>
                <Text style={styles.actionText}>Author</Text>
              </Pressable>
            ) : null}
          </View>
        </View>
      </View>

      <SegmentedTabs
        value={tab}
        onChange={setTab}
        options={[
          { label: "Overview", value: "overview" },
          { label: "Editions", value: "editions" },
          { label: "Related", value: "related" }
        ]}
      />

      {tab === "overview" ? (
        <View style={styles.panel}>
          <Text style={styles.panelTitle}>About</Text>
          <Text style={styles.body}>{description}</Text>
          {book.subjects?.length ? (
            <View style={styles.tags}>
              {book.subjects.map((subject) => (
                <View key={subject} style={styles.tag}>
                  <Text style={styles.tagText}>{subject}</Text>
                </View>
              ))}
            </View>
          ) : null}
        </View>
      ) : null}

      {tab === "editions" ? (
        <View style={styles.panel}>
          <Text style={styles.panelTitle}>Editions</Text>
          {editions.length ? editions.map((edition) => <Text key={edition.key} style={styles.listItem}>{edition.title}</Text>) : <Text style={styles.body}>No editions found.</Text>}
        </View>
      ) : null}

      {tab === "related" ? (
        <View style={styles.panel}>
          <Text style={styles.panelTitle}>More by this author</Text>
          {author?.works?.length ? author.works.slice(0, 6).map((work) => <Text key={work.key} style={styles.listItem}>{work.title}</Text>) : <Text style={styles.body}>No related works available.</Text>}
        </View>
      ) : null}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: spacing.md, gap: spacing.md },
  hero: {
    backgroundColor: colors.surface,
    borderRadius: 22,
    borderWidth: 1,
    borderColor: colors.border,
    padding: spacing.md,
    flexDirection: "row",
    gap: spacing.md
  },
  cover: { width: 120, height: 180, borderRadius: 14, backgroundColor: colors.primarySoft },
  coverPlaceholder: { width: 120, height: 180, borderRadius: 14, backgroundColor: colors.primarySoft },
  heroBody: { flex: 1, gap: spacing.sm },
  title: { fontSize: 24, fontWeight: "900", color: colors.text },
  author: { fontSize: 15, color: colors.textMuted },
  actions: { flexDirection: "row", gap: spacing.sm, flexWrap: "wrap" },
  actionButton: { borderRadius: 999, borderWidth: 1, borderColor: colors.border, paddingHorizontal: 14, paddingVertical: 10, backgroundColor: colors.card },
  actionButtonActive: { backgroundColor: colors.primary, borderColor: colors.primary },
  actionText: { fontWeight: "800", color: colors.text },
  actionTextActive: { color: "white" },
  panel: { backgroundColor: colors.surface, borderRadius: 20, borderWidth: 1, borderColor: colors.border, padding: spacing.md, gap: spacing.sm },
  panelTitle: { fontSize: 18, fontWeight: "800", color: colors.text },
  body: { color: colors.textMuted, lineHeight: 21 },
  tags: { flexDirection: "row", flexWrap: "wrap", gap: spacing.xs },
  tag: { backgroundColor: colors.primarySoft, borderRadius: 999, paddingHorizontal: 10, paddingVertical: 6 },
  tagText: { color: colors.primary, fontWeight: "700", fontSize: 12 },
  listItem: { color: colors.text, paddingVertical: 6, borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: colors.border },
  error: { color: colors.danger, fontWeight: "700" }
});
