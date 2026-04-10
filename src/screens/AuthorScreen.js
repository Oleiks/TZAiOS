import React, { useEffect, useState } from "react";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import { getAuthor, getAuthorWorks } from "../api/openLibrary";
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
        const [authorResult, worksResult] = await Promise.allSettled([getAuthor(authorKey), getAuthorWorks(authorKey, 12)]);
        if (!mounted) return;
        if (authorResult.status === "fulfilled") {
          setAuthor(authorResult.value);
        }
        if (worksResult.status === "fulfilled") {
          const normalizedWorks = Array.isArray(worksResult.value)
            ? worksResult.value
            : worksResult.value?.entries || worksResult.value?.works || worksResult.value?.items || [];
          setWorks(normalizedWorks);
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

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {error ? <Text style={styles.error}>{error}</Text> : null}
      <View style={styles.hero}>
        <Text style={styles.name}>{author?.name || authorName}</Text>
        <Text style={styles.bio}>{author?.bio?.value || author?.bio || "No biography available."}</Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Works</Text>
        {works.map((work, index) => (
          <View key={work.key || work.id || `${authorKey}-${index}`} style={{ marginBottom: spacing.sm }}>
            <BookCard
              book={{
                id: work.key || work.id || `${authorKey}-${index}`,
                workKey: work.key || work.id || null,
                title: work.title || work.name || "Untitled",
                authorName: author?.name || authorName,
                authorKey,
                subjects: work.subjects || work.subject || [],
                coverUrl: work.coverUrl || work.cover_url || (work.covers?.[0] ? `/covers/id/${work.covers[0]}?size=M` : null)
              }}
              onPress={() => navigation.navigate("BookDetail", { book: {
                id: work.key || work.id || `${authorKey}-${index}`,
                workKey: work.key || work.id || null,
                title: work.title || work.name || "Untitled",
                authorName: author?.name || authorName,
                authorKey,
                subjects: work.subjects || work.subject || [],
                coverUrl: work.coverUrl || work.cover_url || (work.covers?.[0] ? `/covers/id/${work.covers[0]}?size=M` : null)
              } })}
            />
          </View>
        ))}
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
