# Complaint Service

Serwis RESTowy do zarządzania reklamacjami produktów. Umożliwia dodawanie nowych reklamacji, edycję ich treści oraz pobieranie zapisanych reklamacji.

## Funkcjonalności

- Dodawanie nowych reklamacji
- Edycja treści istniejących reklamacji
- Pobieranie zapisanych reklamacji
- Automatyczne wykrywanie kraju użytkownika na podstawie adresu IP
- Automatyczne zwiększanie licznika zgłoszeń dla duplikatów reklamacji (na podstawie identyfikatora produktu i osoby zgłaszającej)

## Wymagania techniczne

- Java 17
- Maven 3.8+
- Dostęp do internetu (dla usługi geolokalizacji IP)

## Użyte technologie

- Java 17
- Spring Boot 3.4.3
- Spring Data JPA
- H2 Database (baza danych w pamięci)
- Lombok
- JUnit 5
- Mockito
- JaCoCo (pokrycie kodu testami)
- Jakarta Validation
- Jackson (obsługa JSON)

## Struktura projektu

```
src/
├── main/
│   ├── java/
│   │   └── pl/empik/complaintservice/
│   │       ├── constants/           # Stałe używane w aplikacji
│   │       ├── controller/          # Kontrolery REST
│   │       ├── exception/           # Obsługa wyjątków
│   │       ├── model/               # Modele danych i DTOs
│   │       ├── repository/          # Repozytoria JPA
│   │       └── service/             # Warstwa usług
│   └── resources/                   # Pliki konfiguracyjne
└── test/
    └── java/
        └── pl/empik/complaintservice/
            └── service/             # Testy jednostkowe
```

## Jak zbudować projekt

```bash
mvn clean install
```

## Jak uruchomić

### Za pomocą Mavena

```bash
mvn spring-boot:run
```

### Jako plik JAR

```bash
java -jar target/complaint-service-0.0.1-SNAPSHOT.jar
```

Po uruchomieniu aplikacja będzie dostępna pod adresem: http://localhost:8080

## Jak debugować

### W IntelliJ IDEA

1. Otwórz projekt w IntelliJ IDEA
2. Kliknij prawym przyciskiem myszy klasę `ComplaintServiceApplication`
3. Wybierz "Debug 'ComplaintServiceApplication'"

### Za pomocą Mavena

```bash
mvnDebug spring-boot:run
```

Następnie podłącz swoje IDE do portu 8000.

## Testy

### Uruchamianie testów jednostkowych

```bash
mvn test
```

### Generowanie raportu pokrycia kodu (JaCoCo)

```bash
mvn clean test jacoco:report
```

Raport będzie dostępny w katalogu `target/site/jacoco/index.html`

### Weryfikacja pokrycia kodu

```bash
mvn clean verify
```

Ta komenda uruchamia testy i sprawdza, czy pokrycie kodu spełnia minimalne wymagania (80% instrukcji, 70% gałęzi).

## Testowanie API

### Kolekcja Postman

W projekcie znajduje się kolekcja Postman (`Complaints-API.postman_collection.json`), którą można zaimportować do aplikacji Postman, aby testować API.

Aby zaimportować kolekcję:
1. Otwórz Postman
2. Kliknij "Import"
3. Wybierz plik `Complaints-API.postman_collection.json`

### Dostępne endpointy

- `GET /api/complaints` - Pobiera wszystkie reklamacje
- `GET /api/complaints/{id}` - Pobiera reklamację o podanym ID
- `POST /api/complaints` - Tworzy nową reklamację
- `PUT /api/complaints/{id}` - Aktualizuje treść reklamacji o podanym ID

### Przykładowe żądanie tworzenia reklamacji (POST)

```json
{
  "productId": "PROD-123",
  "content": "Produkt nie działa zgodnie z opisem",
  "reportedBy": "jan.kowalski@example.com"
}
```

### Testowanie geolokalizacji IP

Aby przetestować funkcję geolokalizacji IP podczas lokalnego rozwoju, możesz użyć nagłówka `X-Forwarded-For` w Postmanie:
1. W zakładce "Headers" dodaj nagłówek
2. Jako klucz wpisz `X-Forwarded-For`
3. Jako wartość wpisz wybrany adres IP (np. `8.8.8.8` dla Google DNS)

## Konsola H2 Database

Konsola bazy danych H2 jest dostępna pod adresem: http://localhost:8080/h2-console

Dane logowania:
- JDBC URL: `jdbc:h2:mem:complaintsdb`
- Username: `sa`
- Password: `password`

## Znane ograniczenia

- Usługa geolokalizacji używa darmowego API, które może mieć ograniczenia w liczbie zapytań
- Dane są przechowywane w pamięci (H2 Database) i są tracone po restarcie aplikacji