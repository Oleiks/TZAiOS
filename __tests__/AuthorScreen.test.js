import React from "react";
import { render, waitFor } from "@testing-library/react-native";
import { AuthorScreen } from "../src/screens/AuthorScreen";
import { Image } from "react-native";

jest.mock("../src/api/openLibrary", () => ({
  getAuthor: jest.fn(async () => ({
    key: "/authors/OL1A",
    name: "Jane Doe",
    bio: { value: "Author bio." }
  })),
  getAuthorWorks: jest.fn(async () => [
    { key: "/works/1", title: "Work 1", authorName: "Unknown author", subjects: ["fiction"], coverUrl: "/covers/id/33?size=M" },
    { key: "/works/2", title: "Work 2", authorName: "Unknown author", subjects: ["fiction"], coverUrl: "/covers/id/22?size=M" }
  ]),
  getCoverUrl: jest.fn(({ coverId }) => (coverId ? `/covers/id/${coverId}?size=M` : null)),
  resolveAssetUrl: jest.fn((url) => url.startsWith("http") ? url : `http://localhost:8080/api/v1${url}`),
  normalizeAuthorName: jest.fn((name, fallback) => (name && name !== "Unknown author" ? name : fallback)),
  normalizeCoverUrl: jest.fn((url) => url && !String(url).includes("/covers/id/-1") ? url : null)
}));

jest.mock("../src/components/BookCard", () => ({
  BookCard: ({ book }) => {
    const { Text } = require("react-native");
    return <Text>{`${book.title}:${book.coverUrl || "no-cover"}:${book.authorName || "no-author"}`}</Text>;
  }
}));

jest.mock("../src/components/LoadingView", () => ({
  LoadingView: () => {
    const { Text } = require("react-native");
    return <Text>loading</Text>;
  }
}));

Image.prefetch = jest.fn(async () => true);

describe("AuthorScreen", () => {
  test("loads at most ten author works with covers", async () => {
    const navigation = { navigate: jest.fn() };
    const route = { params: { authorKey: "/authors/OL1A", authorName: "Jane Doe" } };

    const { getByText } = render(<AuthorScreen navigation={navigation} route={route} />);

    await waitFor(() => expect(getByText("Work 1:/covers/id/33?size=M:Jane Doe")).toBeTruthy());
    expect(getByText("Work 2:/covers/id/22?size=M:Jane Doe")).toBeTruthy();

    const { getAuthorWorks } = require("../src/api/openLibrary");
    expect(getAuthorWorks).toHaveBeenCalledWith("/authors/OL1A", 10);
    expect(Image.prefetch).toHaveBeenCalledWith("http://localhost:8080/api/v1/covers/id/33?size=M");
    expect(Image.prefetch).toHaveBeenCalledWith("http://localhost:8080/api/v1/covers/id/22?size=M");
  });
});
