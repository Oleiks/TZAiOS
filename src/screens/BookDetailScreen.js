import React, { useEffect, useState } from "react";
import { Image, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { extractDescription, getAuthorWorks, getBookDetails, getCoverUrl, normalizeAuthorName, normalizeCoverUrl, resolveAssetUrl } from "../api/openLibrary";
import { BookCard } from "../components/BookCard";
import { LoadingView } from "../components/LoadingView";
import { SegmentedTabs } from "../components/SegmentedTabs";
import { useSaved } from "../state/SavedContext";
import { colors, spacing } from "../theme/colors";

export function BookDetailScreen({ navigation, route }) {
  const { book } = route.params;
  const [tab, setTab] = useState("overview");
  const [details, setDetails] = useState(null);
  const [relatedWorks, setRelatedWorks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const { isFavorite, toggleFavorite } = useSaved();
  const authorKey = book.authorKey || details?.authors?.[0]?.author?.key || details?.authors?.[0]?.key || null;
  const authorName = normalizeAuthorName(book.authorName, details?.authors?.[0]?.author?.name || details?.authors?.[0]?.name || "Unknown author");

  useEffect(() => {
    let mounted = true;

    async function load() {
      setLoading(true);
      setError("");
      try {
        const shouldLoadRelated = book.loadRelated !== false;
        const workKey = book.workKey || book.id;
        const detailData = workKey ? await getBookDetails(workKey) : null;
        const detailAuthorKey = detailData?.authors?.[0]?.author?.key || detailData?.authors?.[0]?.key || null;
        const detailAuthorName = detailData?.authors?.[0]?.author?.name || detailData?.authors?.[0]?.name || null;
        const relatedAuthorKey = shouldLoadRelated ? (book.authorKey || detailAuthorKey) : null;
        const worksResult = shouldLoadRelated && relatedAuthorKey ? await getAuthorWorks(relatedAuthorKey, 6) : [];
        const enrichedRelatedWorks = worksResult.map((work, index) => ({
          ...work,
          authorKey: work.authorKey || relatedAuthorKey,
          authorName: normalizeAuthorName(work.authorName, detailAuthorName || authorName),
          coverUrl: normalizeCoverUrl(work.coverUrl || work.cover_url || getCoverUrl({ coverId: work.covers?.[0] })) || null,
          id: work.id || work.key || `${relatedAuthorKey || "related"}-${index}`,
          workKey: work.workKey || work.key || work.id || null
        }));

        const coverUris = [detailData?.coverUrl, ...enrichedRelatedWorks.map((work) => work.coverUrl)]
          .map((url) => normalizeCoverUrl(url))
          .filter(Boolean)
          .map((url) => resolveAssetUrl(url));
        await Promise.allSettled(coverUris.map((uri) => Image.prefetch(uri)));

        if (!mounted) return;
        setDetails(detailData);
        setRelatedWorks(enrichedRelatedWorks);
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

  const cover = resolveAssetUrl(normalizeCoverUrl(details?.coverUrl || book.coverUrl || getCoverUrl({ coverId: details?.covers?.[0] })));
  const title = details?.title || book.title;
  const description = extractDescription(details?.description || details?.first_sentence || details?.notes || details?.excerpt);
  const favorite = isFavorite(book);

  if (loading) return <LoadingView label="Opening the book..." />;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <View style={styles.hero}>
        {cover ? <Image source={{ uri: cover }} style={styles.cover} /> : <View style={styles.coverPlaceholder} />}
        <View style={styles.heroBody}>
          <Text style={styles.title}>{title}</Text>
          <Text style={styles.author}>{authorName}</Text>
          <View style={styles.actions}>
            <Pressable style={[styles.actionButton, favorite && styles.actionButtonActive]} onPress={() => toggleFavorite(book)}>
              <Text style={[styles.actionText, favorite && styles.actionTextActive]}>{favorite ? "Saved" : "Save"}</Text>
            </Pressable>
            {authorKey ? (
              <Pressable style={styles.actionButton} onPress={() => navigation.navigate("Author", { authorKey, authorName })}>
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

      {tab === "related" ? (
        <View style={styles.panel}>
            <Text style={styles.panelTitle}>More by this author</Text>
          {relatedWorks.length ? (
            <View style={styles.relatedList}>
              {relatedWorks.slice(0, 6).map((work, index) => {
                const relatedBook = {
                  ...work,
                  authorName: normalizeAuthorName(work.authorName, authorName),
                  authorKey: work.authorKey || authorKey
                };

                return (
                  <BookCard
                    key={relatedBook.key || relatedBook.id || `${authorKey}-${index}`}
                    book={relatedBook}
                    onPress={() => navigation.navigate("BookDetail", { book: relatedBook })}
                  />
                );
              })}
            </View>
          ) : (
            <Text style={styles.body}>No related works available.</Text>
          )}
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
  relatedList: { gap: spacing.sm },
  tags: { flexDirection: "row", flexWrap: "wrap", gap: spacing.xs },
  tag: { backgroundColor: colors.primarySoft, borderRadius: 999, paddingHorizontal: 10, paddingVertical: 6 },
  tagText: { color: colors.primary, fontWeight: "700", fontSize: 12 },
  error: { color: colors.danger, fontWeight: "700" }
});
