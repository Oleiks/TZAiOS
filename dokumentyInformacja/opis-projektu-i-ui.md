# Dokumentacja techniczna i koncepcja projektu

## Opis projektu

Mobilna aplikacja biblioteczna na Android i iOS z backendem Spring Boot. Aplikacja korzysta wyłącznie z własnego API, a backend pobiera dane z Open Library, cache'uje je w Postgres i wystawia spójny interfejs do urządzenia mobilnego.

## Poziom projektu

Projekt jest przygotowany pod ocenę 5.0, ponieważ wykorzystuje autorski backend, aplikacja mobilna komunikuje się wyłącznie z tym backendem, zawiera co najmniej 6 funkcjonalności i ma złożony interfejs z widokiem szczegółowym oraz wielopoziomową nawigacją.

## Skład zespołu

- Miraslau Farelnik, indeks: 191573 - frontend, testy frontend, networking, UI, model danych, architektura
- Aleksander Górski, indeks: 188827 - backend, testy backend, testy frontend, networking, UI, architektura, poprawki

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
- zapisane książki i ostatnie wyszukiwania przechowywane lokalnie w `AsyncStorage`.

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
- Settings - informacje o aplikacji i czyszczenie lokalnych danych.

### Nawigacja

- dolny tab bar dla głównych obszarów aplikacji,
- stack navigation wewnątrz każdej sekcji,
- przejścia z list do widoku szczegółowego,
- przejście z książki do autora,
- zakładki w szczegółach książki: `Overview / Related`.

### Elementy interfejsu

- hero card na Home,
- poziome listy książek,
- segmented control w widoku szczegółów,
- chips dla subjectów i szybkich wyszukiwań,
- akcja save/unsave w widoku szczegółów książki,
- prosty panel ustawień z czyszczeniem lokalnych danych.

## Szkic widoków

Grafiki nie są dołączone do repozytorium, więc poniżej znajduje się tekstowy szkic widoków zgodny z aktualną implementacją.

### Home

```text
[Hero]
[Trending Now carousel]
[Fantasy Escape carousel]
[History Picks carousel]
```

### Search

```text
[Search input]
[Quick search chips]
[Recent searches]
[Results list]
```

### Subjects

```text
[Subject chips]
[Books list]
```

### Detail

```text
[Cover + title + author]
[Save button] [Author button]
[Overview | Related]
[Tab content]
```

### Author

```text
[Author name]
[Bio]
[Works list]
```

### Saved

```text
[Header]
[Saved books list]
```

### Settings

```text
[App info]
[Clear saved data]
```

## Główne funkcjonalności

- przeglądanie katalogu przez backend,
- szybkie wyszukiwanie i filtrowanie,
- przeglądanie subjectów,
- widok szczegółów książki,
- widok autora i jego dzieł,
- zapis książek,
- lokalne przechowywanie zapisanych książek i ostatnich wyszukiwań,
- działanie na Android i iOS,
- czytelne widoki szczegółowe z obrazami i zakładkami.
