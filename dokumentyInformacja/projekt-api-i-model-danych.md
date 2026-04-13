# Projekt API i model danych

## Założenia

Aplikacja mobilna korzysta wyłącznie z własnego backendu. Backend wystawia endpointy dla katalogu książek, wyszukiwania, autora i okładek oraz cachuje odpowiedzi z Open Library.

Poniżej są opisane tylko endpointy używane przez aktualny klient mobilny.

## Bazowy adres API

- lokalnie: `http://localhost:8080/api/v1`
- mobilnie: `EXPO_PUBLIC_LIBRARY_API_URL`

## Endpointy

### Catalog

#### `GET /home`

- sekcje startowe aplikacji,
- bez autoryzacji.

#### `GET /search?q=...&page=1`

- wyszukiwanie książek,
- parametry: `q`, `page`.

#### `GET /subjects/{subject}`

- książki dla konkretnego subjectu,
- parametr ścieżki: `subject`.

#### `GET /books?key=/works/...`

- szczegóły książki,
- parametr: `key`.

#### `GET /authors?key=/authors/...`

- dane autora,
- parametr: `key`.

#### `GET /authors/works?key=/authors/...`

- dzieła autora,
- parametry: `key`, opcjonalnie `limit`.

#### `GET /covers/id/{coverId}?size=M`

- proxy dla okładki po `coverId`,
- parametry: `coverId`, `size` (`S`, `M`, `L`).

#### `GET /covers/isbn/{isbn}?size=M`

- proxy dla okładki po ISBN,
- parametry: `isbn`, `size` (`S`, `M`, `L`).

## Request headers

- brak dodatkowych nagłówków po stronie klienta,
- endpointy są publiczne i używane przez zwykłe zapytania `GET`.

## Response model

### `BookSummaryDto`

```json
{
  "id": "/works/OL123W",
  "workKey": "/works/OL123W",
  "title": "Example Book",
  "authorName": "Jane Doe",
  "authorKey": "/authors/OL1A",
  "firstPublishYear": 1999,
  "rating": 4.5,
  "coverUrl": "/covers/id/12?size=M",
  "subjects": ["fiction"],
  "editionCount": 4
}
```

### `HomeResponse`

```json
{
  "sections": [
    {
      "id": "fiction",
      "title": "Trending Now",
      "works": []
    }
  ]
}
```

### `SearchResponse`

```json
{
  "numFound": 1234,
  "books": []
}
```

### `SubjectResponse`

```json
{
  "name": "fiction",
  "works": []
}
```

### `AuthorDto`

```json
{
  "key": "/authors/OL1A",
  "name": "Jane Doe",
  "bio": "Short biography text.",
  "personalName": "Jane Doe",
  "photos": [1, 2]
}
```

### `AuthorWorksResponse`

```json
{
  "entries": []
}
```

### `BookDetailsDto`

```json
{
  "key": "/works/OL123W",
  "title": "Example Book",
  "description": "Long description or first sentence fallback.",
  "firstSentence": "First sentence.",
  "notes": "",
  "excerpt": "",
  "coverUrl": "/covers/id/12?size=M",
  "covers": [12],
  "authors": [],
  "works": [],
  "subjects": ["fiction"]
}
```

## Model danych aplikacji

### Mobile models

- `BookSummaryDto` - książka w listach,
- `SectionDto` - sekcja startowa,
- `HomeResponse` - strona główna,
- `SearchResponse` - wyniki wyszukiwania,
- `SubjectResponse` - subject i jego książki,
- `AuthorDto` - dane autora,
- `AuthorWorksResponse` - dzieła autora,
- `BookDetailsDto` - szczegóły książki.

### Mapowanie response → model

- `home` -> `HomeResponse`,
- `search` -> `SearchResponse.books`,
- `subjects/{subject}` -> `SubjectResponse.works`,
- `books` -> `BookDetailsDto`,
- `authors` -> `AuthorDto`,
- `authors/works` -> `AuthorWorksResponse.entries`,
- `covers/*` -> binarna odpowiedź obrazu.

## TTL cache

- `search` - 1h,
- `subject` - 6h,
- `book` - 24h,
- `author` - 168h,
- stale window - 72h.

Backend zwraca cache, jeśli dane są świeże. Jeśli są przeterminowane, ale nadal mieszczą się w stale window, backend zwraca zapisany wynik i odświeża go w tle.

## Zgodność z wymaganiami 5.0

- własny backend agregujący dane z publicznego API,
- aplikacja mobilna komunikuje się wyłącznie z backendem,
- co najmniej 6 funkcjonalności po stronie aplikacji,
- złożony interfejs z listami, detalami i wielostopniową nawigacją,
- warstwa networkingu, modele danych i UI są rozdzielone,
- przygotowane są testy jednostkowe oraz testy UI/snapshot.
