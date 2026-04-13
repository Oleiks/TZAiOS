import { extractDescription, getCoverUrl, mapSearchDoc, normalizeAuthorName, normalizeCoverUrl, resolveAssetUrl } from "../src/api/openLibrary";

describe("openLibrary helpers", () => {
  test("builds a cover url from cover id", () => {
    expect(getCoverUrl({ coverId: 42 })).toBe("/covers/id/42?size=M");
  });

  test("drops invalid cover ids and urls", () => {
    expect(getCoverUrl({ coverId: -1 })).toBeNull();
    expect(normalizeCoverUrl("/covers/id/-1?size=M")).toBeNull();
    expect(resolveAssetUrl("/covers/id/-1?size=M")).toBeNull();
  });

  test("keeps a real author name when fallback is unknown", () => {
    expect(normalizeAuthorName("Unknown author", "Jane Doe")).toBe("Jane Doe");
  });

  test("maps search docs into app books", () => {
    const book = mapSearchDoc({
      key: "/works/OL123W",
      title: "Example Book",
      author_name: ["Jane Doe"],
      author_key: ["OL1A"],
      first_publish_year: 1999,
      cover_i: 12,
      subject: ["fiction", "adventure"],
      edition_count: 4
    });

    expect(book).toEqual({
      id: "/works/OL123W",
      workKey: "/works/OL123W",
      title: "Example Book",
      authorName: "Jane Doe",
      authorKey: "/authors/OL1A",
      year: 1999,
      rating: undefined,
      coverUrl: "/covers/id/12?size=M",
      subjects: ["fiction", "adventure"],
      editionCount: 4
    });
  });

  test("extracts descriptions from strings and objects", () => {
    expect(extractDescription("Hello")).toBe("Hello");
    expect(extractDescription({ value: "World" })).toBe("World");
    expect(extractDescription(null)).toBe("No description available.");
  });
});
