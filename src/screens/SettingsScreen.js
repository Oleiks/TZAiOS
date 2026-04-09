import React from "react";
import { Alert, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { useSaved } from "../state/SavedContext";
import { colors, spacing } from "../theme/colors";

export function SettingsScreen() {
  const { clearAll } = useSaved();

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.card}>
        <Text style={styles.title}>App settings</Text>
        <Text style={styles.body}>A simple, offline-friendly book browser built on Open Library.</Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.section}>Data</Text>
        <Pressable
          style={styles.button}
          onPress={() => {
            Alert.alert("Clear local data", "Remove favorites and recent searches?", [
              { text: "Cancel", style: "cancel" },
              { text: "Clear", style: "destructive", onPress: clearAll }
            ]);
          }}
        >
          <Text style={styles.buttonText}>Clear saved data</Text>
        </Pressable>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: spacing.md, gap: spacing.md },
  card: { backgroundColor: colors.surface, borderRadius: 20, borderWidth: 1, borderColor: colors.border, padding: spacing.lg, gap: spacing.sm },
  title: { fontSize: 24, fontWeight: "900", color: colors.text },
  body: { color: colors.textMuted, lineHeight: 20 },
  section: { fontSize: 18, fontWeight: "800", color: colors.text },
  button: { backgroundColor: colors.danger, borderRadius: 14, paddingVertical: 12, alignItems: "center" },
  buttonText: { color: "white", fontWeight: "800" }
});
