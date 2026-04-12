import React from "react";
import renderer from "react-test-renderer";
import { BookCard } from "../src/components/BookCard";

describe("BookCard snapshot", () => {
  test("matches snapshot", () => {
    const tree = renderer
      .create(
        <BookCard
          book={{
            title: "Snapshot Book",
            authorName: "Writer Name",
            year: 2024,
            editionCount: 2,
            subjects: ["fiction", "mystery"],
            coverUrl: null
          }}
        />
      )
      .toJSON();

    expect(tree).toMatchSnapshot();
  });
});
