import React from "react";
import renderer from "react-test-renderer";
import { SegmentedTabs } from "../src/components/SegmentedTabs";

describe("SegmentedTabs snapshot", () => {
  test("matches snapshot", () => {
    const tree = renderer
      .create(
        <SegmentedTabs
          value="overview"
          onChange={() => null}
          options={[
            { label: "Overview", value: "overview" },
            { label: "Editions", value: "editions" }
          ]}
        />
      )
      .toJSON();

    expect(tree).toMatchSnapshot();
  });
});
