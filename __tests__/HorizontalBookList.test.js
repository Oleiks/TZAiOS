import React from "react";
import renderer from "react-test-renderer";
import { Image } from "react-native";
import { HorizontalBookList } from "../src/components/HorizontalBookList";

describe("HorizontalBookList", () => {
  test("resolves relative cover urls before rendering", () => {
    const tree = renderer
      .create(
        <HorizontalBookList
          title="Featured"
          books={[
            {
              id: "1",
              title: "The Test Book",
              authorName: "A Writer",
              coverUrl: "/covers/id/12?size=M"
            }
          ]}
          onPressBook={jest.fn()}
        />
      )
      .root;

    expect(tree.findByType(Image).props.source.uri).toBe("http://localhost:8080/api/v1/covers/id/12?size=M");
  });
});
