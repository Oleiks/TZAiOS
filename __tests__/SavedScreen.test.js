import React from "react";
import { render } from "@testing-library/react-native";
import { SavedScreen } from "../src/screens/SavedScreen";

jest.mock("../src/state/SavedContext", () => ({
  useSaved: () => ({
    favorites: [
      {
        id: "1",
        workKey: "/works/OL1W",
        title: "Saved Book",
        authorName: "A. Writer",
        subjects: ["fiction"],
        coverUrl: null
      }
    ]
  })
}));

describe("SavedScreen", () => {
  test("renders saved books", () => {
    const navigation = { navigate: jest.fn() };
    const { getByText } = render(<SavedScreen navigation={navigation} />);

    expect(getByText("Saved Book")).toBeTruthy();
    expect(getByText("Your reading list")).toBeTruthy();
  });
});
