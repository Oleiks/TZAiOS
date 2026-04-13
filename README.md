# Open Library Mobile

Aplikacja Expo na Android i iOS z backendem Spring Boot, który proxyzuje Open Library.

## Uruchomienie

### Wymagania

- Node.js i npm
- Java 21
- Maven
- Docker

### 1. Uruchom Postgresa

```bash
docker compose up -d postgres
```

Postgres działa na `localhost:5433` i zapisuje dane w wolumenie `postgres-data`.

### 2. Uruchom backend

```bash
mvn -f backend/library/pom.xml spring-boot:run
```

Backend udostępnia API pod adresem `http://localhost:8080/api/v1`.

### 3. Uruchom aplikację mobilną

```bash
npm install
npm run start
```

Następnie uruchom aplikację na Androidzie, iOS lub w przeglądarce z menu Expo.

Jeśli uruchamiasz na fizycznym urządzeniu, ustaw `EXPO_PUBLIC_LIBRARY_API_URL` na adres backendu dostępny z telefonu.

## Testy

```bash
npm test
mvn -f backend/library/pom.xml test
```

## Funkcje

- Strona główna
- Wyszukiwanie
- Przeglądanie subjectów
- Widok szczegółów książki
- Strona autora
- Zapisane książki
