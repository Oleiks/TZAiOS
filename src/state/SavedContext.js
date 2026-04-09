import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";

const FAVORITES_KEY = "saved_books_v1";
const RECENT_KEY = "recent_searches_v1";

const SavedContext = createContext(null);

export function SavedProvider({ children }) {
  const [favorites, setFavorites] = useState([]);
  const [recentSearches, setRecentSearches] = useState([]);
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    let mounted = true;

    async function hydrate() {
      try {
        const [favRaw, recentRaw] = await Promise.all([
          AsyncStorage.getItem(FAVORITES_KEY),
          AsyncStorage.getItem(RECENT_KEY)
        ]);

        if (!mounted) return;
        setFavorites(favRaw ? JSON.parse(favRaw) : []);
        setRecentSearches(recentRaw ? JSON.parse(recentRaw) : []);
      } finally {
        if (mounted) setLoaded(true);
      }
    }

    hydrate();
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    if (!loaded) return;
    AsyncStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites)).catch(() => null);
  }, [favorites, loaded]);

  useEffect(() => {
    if (!loaded) return;
    AsyncStorage.setItem(RECENT_KEY, JSON.stringify(recentSearches)).catch(() => null);
  }, [recentSearches, loaded]);

  const value = useMemo(() => {
    const favoriteIds = new Set(favorites.map((book) => book.workKey || book.id));

    function isFavorite(book) {
      return favoriteIds.has(book?.workKey || book?.id);
    }

    function toggleFavorite(book) {
      if (!book) return;
      const key = book.workKey || book.id;
      setFavorites((prev) => {
        const exists = prev.some((item) => (item.workKey || item.id) === key);
        if (exists) return prev.filter((item) => (item.workKey || item.id) !== key);
        return [book, ...prev].slice(0, 100);
      });
    }

    function addRecentSearch(query) {
      if (!query?.trim()) return;
      setRecentSearches((prev) => {
        const normalized = query.trim();
        return [normalized, ...prev.filter((item) => item.toLowerCase() !== normalized.toLowerCase())].slice(0, 10);
      });
    }

    function clearAll() {
      setFavorites([]);
      setRecentSearches([]);
    }

    return {
      loaded,
      favorites,
      recentSearches,
      isFavorite,
      toggleFavorite,
      addRecentSearch,
      clearAll
    };
  }, [favorites, recentSearches, loaded]);

  return <SavedContext.Provider value={value}>{children}</SavedContext.Provider>;
}

export function useSaved() {
  const context = useContext(SavedContext);
  if (!context) {
    throw new Error("useSaved must be used inside SavedProvider");
  }
  return context;
}
