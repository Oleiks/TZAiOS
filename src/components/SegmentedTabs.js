import React from "react";
import { Pressable, StyleSheet, Text, View } from "react-native";
import { colors, spacing } from "../theme/colors";

export function SegmentedTabs({ options, value, onChange }) {
  return (
    <View style={styles.container}>
      {options.map((option) => {
        const active = option.value === value;
        return (
          <Pressable key={option.value} style={[styles.tab, active && styles.activeTab]} onPress={() => onChange(option.value)}>
            <Text style={[styles.tabText, active && styles.activeText]}>{option.label}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: "row",
    gap: spacing.xs,
    padding: spacing.xs,
    borderRadius: 999,
    backgroundColor: colors.primarySoft
  },
  tab: {
    flex: 1,
    borderRadius: 999,
    paddingVertical: 8,
    alignItems: "center"
  },
  activeTab: {
    backgroundColor: colors.primary
  },
  tabText: {
    fontSize: 13,
    fontWeight: "700",
    color: colors.primary
  },
  activeText: {
    color: "#ffffff"
  }
});
