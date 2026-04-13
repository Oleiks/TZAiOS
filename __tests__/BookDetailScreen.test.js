import React from "react";
import { fireEvent, render, waitFor } from "@testing-library/react-native";
import { Image } from "react-native";
import { BookDetailScreen } from "../src/screens/BookDetailScreen";

jest.mock("../src/api/openLibrary", () => ({
  extractDescription: jest.fn((value) => {
    if (typeof value === "string") return value;
    if (value && typeof value.value === "string") return value.value;
    if (value && value.description) return value.description.value || value.description;
    if (value && value.first_sentence) return value.first_sentence.value || value.first_sentence;
    if (value && value.notes) return value.notes.value || value.notes;
    return "No description available.";
  }),
  getAuthor: jest.fn(async () => null),
  getAuthorWorks: jest.fn(async () => [
    { id: "1", workKey: "/works/1", title: "Related 1", authorName: "Unknown author", authorKey: "/authors/OL21594A", coverUrl: "/covers/id/201?size=M", subjects: [] },
    { id: "2", workKey: "/works/2", title: "Related 2", authorName: "Unknown author", authorKey: "/authors/OL21594A", coverUrl: "/covers/id/202?size=M", subjects: [] },
    { id: "3", workKey: "/works/3", title: "Related 3", authorName: "Unknown author", authorKey: "/authors/OL21594A", coverUrl: "/covers/id/203?size=M", subjects: [] },
    { id: "4", workKey: "/works/4", title: "Related 4", authorName: "Unknown author", authorKey: "/authors/OL21594A", coverUrl: "/covers/id/204?size=M", subjects: [] },
    { id: "5", workKey: "/works/5", title: "Related 5", authorName: "Unknown author", authorKey: "/authors/OL21594A", coverUrl: "/covers/id/205?size=M", subjects: [] }
  ]),
  getBookDetails: jest.fn(async (key) => {
    if (key === "/books/OL123M") {
      return {
        key: "/books/OL123M",
        title: "Edition Title",
        works: [{ key: "/works/OL123W" }],
        covers: [123],
        coverUrl: "/covers/id/123?size=M"
      };
    }

    return {
      key: "/works/OL123W",
      title: "Fetched Title",
      description: { value: "Fetched description" },
      authors: [{ author: { key: "/authors/OL21594A" } }],
      covers: [123],
      coverUrl: "/covers/id/123?size=M"
    };
  }),
  getCoverUrl: jest.fn(() => "http://localhost:8080/api/v1/covers/id/123?size=M"),
  resolveAssetUrl: jest.fn((url) => url && url.startsWith("http") ? url : `http://localhost:8080/api/v1${url}`),
  normalizeAuthorName: jest.fn((name, fallback) => (name && name !== "Unknown author" ? name : fallback)),
  normalizeCoverUrl: jest.fn((url) => url && !String(url).includes("/covers/id/-1") ? url : null)
}));

jest.mock("../src/components/BookCard", () => ({
  BookCard: ({ book }) => {
    const { Text } = require("react-native");
    return <Text>{`${book.title}:${book.coverUrl || "no-cover"}:${book.authorName || "no-author"}`}</Text>;
  }
}));

jest.mock("../src/state/SavedContext", () => ({
  useSaved: () => ({
    isFavorite: () => false,
    toggleFavorite: jest.fn()
  })
}));

jest.mock("../src/components/CoverImage", () => ({
  CoverImage: ({ uri }) => {
    const { Text } = require("react-native");
    return <Text>{uri}</Text>;
  }
}));

Image.prefetch = jest.fn(async () => true);

describe("BookDetailScreen", () => {
  test("opens a book detail view", async () => {
    const navigation = { navigate: jest.fn() };
    const route = {
      params: {
        book: {
          workKey: "/works/OL123W",
          title: "Fallback Title",
          authorName: "Writer Name",
          authorKey: "/authors/OL21594A",
          coverUrl: null,
          subjects: ["fiction"]
        }
      }
    };

    const { getByText } = render(<BookDetailScreen navigation={navigation} route={route} />);

    await waitFor(() => expect(getByText("Fetched Title")).toBeTruthy());
    expect(require("../src/api/openLibrary").getBookDetails).toHaveBeenCalledWith("/works/OL123W");
  });

  test("shows the author link from fetched details when missing on the book", async () => {
    const navigation = { navigate: jest.fn() };
    const route = {
      params: {
        book: {
          workKey: "/works/OL123W",
          title: "Fallback Title",
          authorName: "Writer Name",
          coverUrl: null,
          subjects: ["fiction"]
        }
      }
    };

    const { getByText } = render(<BookDetailScreen navigation={navigation} route={route} />);

    await waitFor(() => expect(getByText("Author")).toBeTruthy());
    fireEvent.press(getByText("Author"));
    expect(navigation.navigate).toHaveBeenCalledWith("Author", { authorKey: "/authors/OL21594A", authorName: "Writer Name" });
  });

  test("shows first sentence when description is missing", async () => {
    const api = require("../src/api/openLibrary");
    api.getBookDetails.mockResolvedValueOnce({
      key: "/works/OL999W",
      title: "Alt Fetched Title",
      first_sentence: { value: "First sentence description" },
      authors: [{ author: { key: "/authors/OL21594A" } }],
      covers: [123]
    });

    const navigation = { navigate: jest.fn() };
    const route = {
      params: {
        book: {
          workKey: "/works/OL999W",
          title: "Fallback Title",
          authorName: "Writer Name",
          authorKey: "/authors/OL21594A",
          coverUrl: null,
          subjects: ["fiction"]
        }
      }
    };

    const { getByText } = render(<BookDetailScreen navigation={navigation} route={route} />);

    await waitFor(() => expect(getByText("Alt Fetched Title")).toBeTruthy());
    expect(getByText("First sentence description")).toBeTruthy();
  });

  test("does not fetch related works for direct author-origin book link", async () => {
    const api = require("../src/api/openLibrary");
    api.getAuthorWorks.mockClear();
    api.getBookDetails.mockResolvedValueOnce({
      key: "/works/OL777W",
      title: "Direct Book",
      description: { value: "Direct description" },
      covers: [777],
      coverUrl: "/covers/id/777?size=M"
    });

    const navigation = { navigate: jest.fn() };
    const route = {
      params: {
        book: {
          workKey: "/works/OL777W",
          title: "Direct Book",
          authorName: "Writer Name",
          coverUrl: null,
          loadRelated: false
        }
      }
    };

    const { getByText } = render(<BookDetailScreen navigation={navigation} route={route} />);

    await waitFor(() => expect(getByText("Direct Book")).toBeTruthy());
    expect(api.getAuthorWorks).not.toHaveBeenCalled();
  });

  test("renders related works as cover cards", async () => {
    const navigation = { navigate: jest.fn() };
    const route = {
      params: {
        book: {
          workKey: "/works/OL123W",
          title: "Fallback Title",
          authorName: "Writer Name",
          authorKey: "/authors/OL21594A",
          coverUrl: null,
          subjects: ["fiction"]
        }
      }
    };

    const { getByText } = render(<BookDetailScreen navigation={navigation} route={route} />);

    await waitFor(() => expect(getByText("Fetched Title")).toBeTruthy());
    fireEvent.press(getByText("Related"));
    await waitFor(() => expect(getByText("Related 1:/covers/id/201?size=M:Writer Name")).toBeTruthy());
    expect(getByText("Related 2:/covers/id/202?size=M:Writer Name")).toBeTruthy();
  });
});
