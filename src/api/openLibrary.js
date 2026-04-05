const BASE_URL = "https://openlibrary.org";
const COVERS_URL = "https://covers.openlibrary.org/b";

function toQuery(params = {}) {
  const entries = Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== "");
  return new URLSearchParams(entries).toString();
}

async function request(path, params) {
  const query = toQuery(params);
  const url = `${BASE_URL}${path}${query ? `?${query}` : ""}`;
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Open Library request failed: ${response.status}`);
  }
  return response.json();
}

export function getCoverUrl({ coverId, isbn, size = "M" }) {
  if (coverId) return `${COVERS_URL}/id/${coverId}-${size}.jpg`;
  if (isbn) return `${COVERS_URL}/isbn/${isbn}-${size}.jpg`;
  return null;
}

export function resolveAssetUrl(url) {
  if (!url) return null;
  if (url.startsWith("http://") || url.startsWith("https://")) return url;
  if (url.startsWith("/")) return `${API_BASE_URL}${url}`;
  return `${API_BASE_URL}/${url}`;
}

export async function searchBooks(query, page = 1) {
  const data = await request("/search.json", { q: query, page, limit: 20 });
  return {
    total: data.numFound || 0,
    items: (data.docs || []).map(mapSearchDoc)
  };
}

export async function getSubjectBooks(subject, limit = 20) {
  const data = await request(`/subjects/${encodeURIComponent(subject)}.json`, { limit });
  return {
    name: data.name || subject,
    works: (data.works || []).map(mapSubjectWork)
  };
}

export async function getBookDetails(workKey) {
  return request(normalizeWorkKey(workKey));
}

export async function getBookEditions(workKey, limit = 20) {
  const normalized = normalizeWorkKey(workKey).replace(".json", "");
  const data = await request(`${normalized}/editions.json`, { limit });
  return data.entries || [];
}

export async function getSearchBySubject(subject, page = 1) {
  return searchBooks(`subject:${subject}`, page);
}

export async function getAuthor(authorKey) {
  return request(normalizeJsonPath(authorKey));
}

export async function getAuthorWorks(authorKey, limit = 20) {
  const normalized = normalizeJsonPath(authorKey).replace(".json", "");
  const data = await request(`${normalized}/works.json`, { limit });
  return data.entries || [];
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
  return "No description available.";
}

function normalizeWorkKey(workKey) {
  const path = workKey || "";
  if (path.startsWith("/works/")) return normalizeJsonPath(path);
  throw new Error("Only work keys are supported for details.");
}

function normalizeJsonPath(path) {
  if (!path) return "";
  if (path.endsWith(".json")) return path;
  return `${path}.json`;
}
