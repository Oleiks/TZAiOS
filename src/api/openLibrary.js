import { Platform } from "react-native";

const DEFAULT_API_BASE_URL = Platform.select({
  android: "http://10.0.2.2:8080/api/v1",
  default: "http://localhost:8080/api/v1"
});

const API_BASE_URL = process.env.EXPO_PUBLIC_LIBRARY_API_URL || DEFAULT_API_BASE_URL;

function isValidCoverId(coverId) {
  if (coverId === undefined || coverId === null || coverId === "") return false;
  const numeric = Number(coverId);
  return Number.isFinite(numeric) && numeric > 0;
}

function isBadCoverUrl(url) {
  const match = String(url).match(/\/covers\/(id|olid|isbn)\/([^/?#]+)(?:[/?#]|$)/i);
  if (!match) return false;

  const [, type, value] = match;
  if (value === "-1") return true;
  if ((type === "id" || type === "isbn") && Number(value) <= 0) return true;
  return false;
}

function toQuery(params = {}) {
  const entries = Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== "");
  return new URLSearchParams(entries).toString();
}

async function request(path, params) {
  const query = toQuery(params);
  const url = `${API_BASE_URL}${path}${query ? `?${query}` : ""}`;
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Backend request failed: ${response.status}`);
  }
  return response.json();
}

export function getCoverUrl({ coverId, coverEditionKey, isbn, size = "M" }) {
  if (isValidCoverId(coverId)) return `/covers/id/${coverId}?size=${size}`;
  if (coverEditionKey) return `/covers/olid/${coverEditionKey}?size=${size}`;
  if (isbn && String(isbn).trim()) return `/covers/isbn/${isbn}?size=${size}`;
  return null;
}

export function normalizeCoverUrl(url) {
  if (!url) return null;
  const trimmed = String(url).trim();
  return isBadCoverUrl(trimmed) ? null : trimmed;
}

export function normalizeAuthorName(name, fallback = "Unknown author") {
  const trimmed = typeof name === "string" ? name.trim() : "";
  return trimmed && trimmed !== "Unknown author" ? trimmed : fallback;
}

export function resolveAssetUrl(url) {
  const normalized = normalizeCoverUrl(url);
  if (!normalized) return null;
  if (normalized.startsWith("http://") || normalized.startsWith("https://")) return normalized;
  if (normalized.startsWith("/")) return `${API_BASE_URL}${normalized}`;
  return `${API_BASE_URL}/${normalized}`;
}

export async function searchBooks(query, page = 1, limit = 40) {
  const data = await request("/search", { q: query, page, limit });
  const items = data.books || data.items || (data.docs || []).map(mapSearchDoc);
  return {
    total: data.numFound || data.total || 0,
    items
  };
}

export async function getSubjectBooks(subject, limit = 40) {
  const data = await request(`/subjects/${encodeURIComponent(subject)}`, { limit });
  return {
    name: data.name || subject,
    works: data.works || data.items || (data.docs || []).map(mapSubjectWork)
  };
}

export async function getBookDetails(workKey) {
  return request("/books", { key: workKey });
}

export async function getSearchBySubject(subject, page = 1) {
  return searchBooks(`subject:${subject}`, page);
}

export async function getAuthor(authorKey) {
  return request("/authors", { key: authorKey });
}

export async function getAuthorWorks(authorKey, limit = 20) {
  const data = await request("/authors/works", { key: authorKey, limit });
  return data.entries || data.works || data.items || [];
}

export async function hydrateMissingBookCovers(books) {
  if (!Array.isArray(books) || books.length === 0) {
    return [];
  }

  const results = await Promise.allSettled(
    books.map(async (book) => {
      if (book?.coverUrl) {
        return book;
      }

      const key = book?.workKey || book?.key || book?.id;
      if (!key) {
        return book;
      }

      const details = await getBookDetails(key);
      const coverUrl = normalizeCoverUrl(details?.coverUrl || getCoverUrl({ coverId: details?.covers?.[0] }));
      if (!coverUrl) {
        return book;
      }

      return { ...book, coverUrl };
    })
  );

  return results.map((result, index) => (result.status === "fulfilled" ? result.value : books[index]));
}

export function mapSearchDoc(doc) {
  const workKey = doc.key && doc.key.startsWith("/works/") ? doc.key : doc.cover_edition_key ? `/books/${doc.cover_edition_key}` : null;
  return {
    id: doc.key || doc.cover_edition_key || doc.edition_key?.[0],
    workKey,
    title: doc.title,
    authorName: doc.author_name?.[0] || "Unknown author",
    authorKey: doc.author_key?.[0] ? `/authors/${doc.author_key[0]}` : null,
    year: doc.first_publish_year,
    rating: doc.ratings_average,
    coverUrl: getCoverUrl({ coverId: doc.cover_i, isbn: doc.isbn?.[0] }),
    subjects: doc.subject?.slice(0, 4) || [],
    editionCount: doc.edition_count || 0
  };
}

export function mapSubjectWork(work) {
  return {
    id: work.key,
    workKey: work.key,
    title: work.title,
    authorName: work.authors?.[0]?.name || "Unknown author",
    authorKey: work.authors?.[0]?.key || null,
    year: work.first_publish_year,
    rating: work.rating,
    coverUrl: getCoverUrl({ coverId: work.cover_id }),
    subjects: work.subject?.slice(0, 4) || []
  };
}

export function extractDescription(value) {
  if (!value) return "No description available.";
  if (typeof value === "string") return value;
  if (typeof value.value === "string") return value.value;
  if (typeof value.description === "string") return value.description;
  if (typeof value.first_sentence === "string") return value.first_sentence;
  if (typeof value.notes === "string") return value.notes;
  if (typeof value.excerpt === "string") return value.excerpt;
  if (typeof value.description?.value === "string") return value.description.value;
  if (typeof value.first_sentence?.value === "string") return value.first_sentence.value;
  if (typeof value.notes?.value === "string") return value.notes.value;
  if (typeof value.excerpt?.value === "string") return value.excerpt.value;
  return "No description available.";
}

