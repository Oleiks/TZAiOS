# Projekt API i model danych

## Założenia

Aplikacja mobilna korzysta wyłącznie z własnego backendu. Backend wystawia endpointy dla katalogu książek, wyszukiwania, autora i okładek oraz cachuje odpowiedzi z Open Library.

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

#### `GET /books/editions?key=/works/...`

- wydania książki,
- parametr: `key`.

#### `GET /authors?key=/authors/...`

- dane autora,
- parametr: `key`.

#### `GET /authors/works?key=/authors/...`

- dzieła autora,
- parametr: `key`.

#### `GET /covers/id/{coverId}?size=M`

- proxy dla okładki po `coverId`,
- parametry: `coverId`, `size` (`S`, `M`, `L`).

#### `GET /covers/isbn/{isbn}?size=M`

- proxy dla okładki po ISBN,
- parametry: `isbn`, `size` (`S`, `M`, `L`).

## Request headers

- `Content-Type: application/json`
- brak dodatkowych nagłówków poza standardowymi dla zapytań publicznych.

## Response model

### `BookSummaryDto`

```json
{
  "id": "/works/OL123W",
  "workKey": "/works/OL123W",
  "title": "Example Book",
  "authorName": "Jane Doe",
  "authorKey": "/authors/OL1A",
  "year": 1999,
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
      "key": "fiction",
      "title": "Trending Now",
      "books": []
    }
  ]
}
```

### `SubjectResponse`

```json
{
  "name": "fiction",
  "works": []
}
```

## Model danych aplikacji

### Mobile models

- `BookSummaryDto` - książka w listach,
- `SectionDto` - sekcja startowa,
- `HomeResponse` - strona główna,
- `SubjectResponse` - subject i jego książki,
- `Author` response - dane autora,
- `Book details` response - szczegóły książki.

### Mapowanie response → model

- `search` -> lista `BookSummaryDto`,
- `subjects/{subject}` -> lista `BookSummaryDto`,
- `books` -> JSON książki,
- `authors` -> JSON autora,
- `covers/*` -> odpowiedź binarna obrazu.

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
