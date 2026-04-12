import React from "react";
import { fireEvent, render, waitFor } from "@testing-library/react-native";
import { SearchScreen } from "../src/screens/SearchScreen";
import { act } from "react-test-renderer";

jest.mock("../src/api/openLibrary", () => ({
  searchBooks: jest.fn(async () => ({
    items: [
      {
        id: "1",
        workKey: "/works/OL1W",
        title: "The Test Book",
        authorName: "A. Writer",
        authorKey: "/authors/OL1A",
        coverUrl: null,
        subjects: ["fiction"],
        year: 2020,
        editionCount: 1
      }
    ]
  }))
}));

jest.mock("../src/state/SavedContext", () => ({
  useSaved: () => ({
    recentSearches: ["Dune"],
    addRecentSearch: jest.fn()
  })
}));

// access the mocked module
const { searchBooks } = require("../src/api/openLibrary");

describe("SearchScreen", () => {
  test("searches and shows results", async () => {
    const navigation = { navigate: jest.fn() };
    const { getByText, getByTestId } = render(<SearchScreen navigation={navigation} />);
    const input = getByTestId("search-input");

    await act(async () => {
      fireEvent.changeText(input, "Test");
    });

    await act(async () => {
      fireEvent.press(getByTestId("search-button"));
    });

    await waitFor(() => expect(searchBooks).toHaveBeenCalledWith("Test", 1));
    await waitFor(() => expect(getByText("The Test Book")).toBeTruthy());
  });
});
