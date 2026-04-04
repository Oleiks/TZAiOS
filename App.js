import "react-native-gesture-handler";
import React from "react";
import { StatusBar } from "expo-status-bar";
import { NavigationContainer } from "@react-navigation/native";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { RootNavigator } from "./src/navigation/RootNavigator";
import { SavedProvider } from "./src/state/SavedContext";

export default function App() {
  return (
    <SafeAreaProvider>
      <SavedProvider>
        <NavigationContainer>
          <RootNavigator />
          <StatusBar style="dark" />
        </NavigationContainer>
      </SavedProvider>
    </SafeAreaProvider>
  );
}
