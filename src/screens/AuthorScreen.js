import React, { useEffect, useState } from "react";
import { Image, ScrollView, StyleSheet, Text, View } from "react-native";
import { getAuthor, getAuthorWorks, getCoverUrl, hydrateMissingBookCovers, normalizeAuthorName, normalizeCoverUrl, resolveAssetUrl } from "../api/openLibrary";
import { BookCard } from "../components/BookCard";
import { LoadingView } from "../components/LoadingView";
import { colors, spacing } from "../theme/colors";

export function AuthorScreen({ navigation, route }) {
  const { authorKey, authorName } = route.params;
  const [author, setAuthor] = useState(null);
  const [works, setWorks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let mounted = true;

    async function load() {
      setLoading(true);
      setError("");
      try {
        const [authorResult, worksResult] = await Promise.allSettled([getAuthor(authorKey), getAuthorWorks(authorKey, 20)]);
        if (!mounted) return;
        if (authorResult.status === "fulfilled") {
          setAuthor(authorResult.value);
        }
        if (worksResult.status === "fulfilled") {
          const resolvedAuthorName = authorResult.status === "fulfilled" ? normalizeAuthorName(authorResult.value?.name, authorName) : authorName;
          const normalizedWorks = Array.isArray(worksResult.value)
            ? worksResult.value
            : worksResult.value?.entries || worksResult.value?.works || worksResult.value?.items || [];
          const enrichedWorks = normalizedWorks.slice(0, 20).map((work, index) => ({
            ...work,
            id: work.id || work.key || `${authorKey}-${index}`,
            workKey: work.workKey || work.key || work.id || null,
            title: work.title || work.name || "Untitled",
            subjects: work.subjects || work.subject || [],
            authorName: normalizeAuthorName(work.authorName, resolvedAuthorName),
            authorKey: work.authorKey || authorKey,
            coverUrl: normalizeCoverUrl(work.coverUrl || work.cover_url || getCoverUrl({ coverId: work.covers?.[0] }))
          }));
          const hydratedWorks = typeof hydrateMissingBookCovers === "function" ? await hydrateMissingBookCovers(enrichedWorks) : enrichedWorks;
          const coverUris = hydratedWorks.map((work) => work.coverUrl).filter(Boolean).map((url) => resolveAssetUrl(url));
          await Promise.allSettled(coverUris.map((uri) => Image.prefetch(uri)));
          setWorks(hydratedWorks);
        }
        if (authorResult.status === "rejected" && worksResult.status === "rejected") {
          throw authorResult.reason || worksResult.reason;
        }
      } catch (err) {
        if (mounted) setError(err.message || "Could not load author profile.");
      } finally {
        if (mounted) setLoading(false);
      }
    }

    load();
    return () => {
      mounted = false;
    };
  }, [authorKey]);

  if (loading) return <LoadingView label="Loading author profile..." />;
  const resolvedAuthorName = normalizeAuthorName(author?.name, authorName);

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <View style={styles.hero}>
        <Text style={styles.name}>{resolvedAuthorName}</Text>
        <Text style={styles.bio}>{author?.bio?.value || author?.bio || "No biography available."}</Text>
      </View>

      <View style={styles.section}>
            <Text style={styles.sectionTitle}>Works</Text>
        {works.map((work, index) => {
          const book = {
            id: work.key || work.id || `${authorKey}-${index}`,
            workKey: work.key || work.id || null,
            title: work.title || work.name || "Untitled",
            authorName: normalizeAuthorName(work.authorName, resolvedAuthorName),
            authorKey: work.authorKey || authorKey,
            subjects: work.subjects || work.subject || [],
            coverUrl: normalizeCoverUrl(work.coverUrl || work.cover_url || getCoverUrl({ coverId: work.covers?.[0] }))
          };

          return (
            <View key={book.id} style={{ marginBottom: spacing.sm }}>
              <BookCard book={book} onPress={() => navigation.navigate("BookDetail", { book })} />
            </View>
          );
        })}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: spacing.md, gap: spacing.lg },
  hero: { backgroundColor: colors.surface, borderRadius: 20, borderWidth: 1, borderColor: colors.border, padding: spacing.lg, gap: spacing.sm },
  name: { fontSize: 28, fontWeight: "900", color: colors.text },
  bio: { color: colors.textMuted, lineHeight: 21 },
  section: { gap: spacing.sm },
  sectionTitle: { fontSize: 18, fontWeight: "800", color: colors.text },
  error: { color: colors.danger, fontWeight: "700" }
});
