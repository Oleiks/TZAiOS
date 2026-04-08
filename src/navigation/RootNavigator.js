import React from "react";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import { Text } from "react-native";
import { colors } from "../theme/colors";
import { HomeScreen } from "../screens/HomeScreen";
import { SearchScreen } from "../screens/SearchScreen";
import { SubjectScreen } from "../screens/SubjectScreen";
import { SavedScreen } from "../screens/SavedScreen";
import { SettingsScreen } from "../screens/SettingsScreen";
import { BookDetailScreen } from "../screens/BookDetailScreen";
import { AuthorScreen } from "../screens/AuthorScreen";

const Tab = createBottomTabNavigator();
const HomeStack = createNativeStackNavigator();
const SearchStack = createNativeStackNavigator();
const SubjectStack = createNativeStackNavigator();
const SavedStack = createNativeStackNavigator();

function screenOptions(title) {
  return {
    headerStyle: { backgroundColor: colors.surface },
    headerTintColor: colors.text,
    headerTitleStyle: { fontWeight: "800" },
    title
  };
}

function HomeStackNavigator() {
  return (
    <HomeStack.Navigator>
      <HomeStack.Screen name="HomeFeed" component={HomeScreen} options={screenOptions("Home")} />
      <HomeStack.Screen name="BookDetail" component={BookDetailScreen} options={{ headerShown: false }} />
      <HomeStack.Screen name="Author" component={AuthorScreen} options={{ headerShown: false }} />
    </HomeStack.Navigator>
  );
}

function SearchStackNavigator() {
  return (
    <SearchStack.Navigator>
      <SearchStack.Screen name="SearchFeed" component={SearchScreen} options={screenOptions("Search")} />
      <SearchStack.Screen name="BookDetail" component={BookDetailScreen} options={{ headerShown: false }} />
      <SearchStack.Screen name="Author" component={AuthorScreen} options={{ headerShown: false }} />
    </SearchStack.Navigator>
  );
}

function SubjectStackNavigator() {
  return (
    <SubjectStack.Navigator>
      <SubjectStack.Screen name="SubjectFeed" component={SubjectScreen} options={screenOptions("Subjects")} />
      <SubjectStack.Screen name="BookDetail" component={BookDetailScreen} options={{ headerShown: false }} />
      <SubjectStack.Screen name="Author" component={AuthorScreen} options={{ headerShown: false }} />
    </SubjectStack.Navigator>
  );
}

function SavedStackNavigator() {
  return (
    <SavedStack.Navigator>
      <SavedStack.Screen name="SavedFeed" component={SavedScreen} options={screenOptions("Saved")} />
      <SavedStack.Screen name="BookDetail" component={BookDetailScreen} options={{ headerShown: false }} />
      <SavedStack.Screen name="Author" component={AuthorScreen} options={{ headerShown: false }} />
      <SavedStack.Screen name="Settings" component={SettingsScreen} options={screenOptions("Settings")} />
    </SavedStack.Navigator>
  );
}

export function RootNavigator() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textMuted,
        tabBarStyle: { backgroundColor: colors.surface, borderTopColor: colors.border },
        tabBarIcon: ({ color }) => <Text style={{ color }}>{iconFor(route.name)}</Text>
      })}
    >
      <Tab.Screen name="Home" component={HomeStackNavigator} />
      <Tab.Screen name="Search" component={SearchStackNavigator} />
      <Tab.Screen name="Subjects" component={SubjectStackNavigator} />
      <Tab.Screen name="Saved" component={SavedStackNavigator} />
      <Tab.Screen name="Settings" component={SettingsScreen} options={screenOptions("Settings")} />
    </Tab.Navigator>
  );
}

function iconFor(name) {
  switch (name) {
    case "Home":
      return "⌂";
    case "Search":
      return "⌕";
    case "Subjects":
      return "◫";
    case "Saved":
      return "♥";
    case "Settings":
      return "⚙";
    default:
      return "•";
  }
}
