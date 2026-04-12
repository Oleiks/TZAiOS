# Dokumentacja techniczna i koncepcja projektu

## Opis projektu

Mobilna aplikacja biblioteczna na Android i iOS z backendem Spring Boot. Aplikacja korzysta wyłącznie z własnego API, a backend pobiera dane z Open Library, cache'uje je w Postgres i wystawia spójny interfejs do urządzenia mobilnego.

## Poziom projektu

Projekt jest przygotowany pod ocenę 5.0, ponieważ wykorzystuje autorski backend, aplikacja mobilna komunikuje się wyłącznie z tym backendem, zawiera co najmniej 6 funkcjonalności i ma złożony interfejs z widokiem szczegółowym oraz wielopoziomową nawigacją.

## Skład zespołu

- do uzupełnienia.

## Zakres

- przeglądanie polecanych książek na stronie głównej,
- wyszukiwanie książek i autorów,
- przeglądanie subjectów/kategorii,
- widok szczegółów książki,
- widok autora i jego dzieł,
- lista zapisanych książek.

## Źródło danych

- Open Library jako zewnętrzny katalog,
- własny backend Spring Boot jako jedyny punkt kontaktu dla aplikacji,
- cache z TTL oraz stale-while-revalidate w Postgres,
- zapisane książki przechowywane po stronie backendu.

## Odpowiedzialność backendu

- pobieranie danych z Open Library,
- cache'owanie odpowiedzi,
- proxy dla obrazów okładek,
- udostępnianie jednego API dla klienta mobilnego.

## Projekt UI

### Główne ekrany

- Home - sekcje z wybranymi książkami,
- Search - wyszukiwanie i szybkie skróty,
- Subjects - przeglądanie tematów,
- Detail - szczegóły książki z zakładkami,
- Author - profil autora i jego dzieła,
- Saved - zapisane książki,
- Settings - informacje o aplikacji i ustawienia.

### Nawigacja

- dolny tab bar dla głównych obszarów aplikacji,
- stack navigation wewnątrz każdej sekcji,
- przejścia z list do widoku szczegółowego,
- przejście z książki do autora,
- zakładki w szczegółach książki: `Overview / Editions / Related`.

### Elementy interfejsu

- hero card na Home,
- poziome karuzele książek,
- segmented control w widoku szczegółów,
- chips dla subjectów i szybkich wyszukiwań,
- akcje save/unsave na karcie książki,
- prosty panel ustawień w Settings.

## Szkic widoków

### Home

![Home screen](./img/home.png)

```text
[Hero]
[Trending Now carousel]
[Fantasy Escape carousel]
[History Picks carousel]
```

### Search

![Search screen](./img/search.png)

```text
[Search input]
[Quick search chips]
[Recent searches]
[Results list]
```

### Subjects

![Subjects screen](./img/subjects.png)

```text
[Subject chips]
[Books list]
```

### Detail

![Detail screen](./img/detail.png)

```text
[Cover + title + author]
[Save button] [Author button]
[Overview | Editions | Related]
[Tab content]
```

### Author

![Author screen](./img/author.png)

```text
[Author name]
[Bio]
[Works list]
```

### Saved

![Saved screen](./img/saved.png)

```text
[Header]
[Saved books list]
```

### Settings

![Settings screen](./img/settings.png)

```text
[App info]
[Cache / refresh info]
[Utility actions]
```

## Główne funkcjonalności

- przeglądanie katalogu przez backend,
- szybkie wyszukiwanie i filtrowanie,
- zapis książek,
- działanie na Android i iOS,
- czytelne widoki szczegółowe z obrazami i zakładkami.
