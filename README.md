# Akta 01: Zniknięcie prototypu

Okienkowa gra detektywistyczna napisana w Javie i JavaFX. Podczas zamkniętej prezentacji znika prototyp urządzenia. Gra zaczyna się od wyboru akt; druga sprawa jest zapowiedziana w menu, a pierwsza jest grywalna. Laboratorium, korytarz i biuro mają pełnoekranowe sceny z klikalnymi przedmiotami i postaciami.

## Podgląd gry

![Laboratorium w pierwszej sprawie](docs/screenshots/game.jpg)

## Rozgrywka

Przeglądaj laboratorium, korytarz i biuro, klikając przedmioty, postacie i drzwi na ilustracjach. Znalezienie identyfikatora prowadzi prosto do zbliżenia czytnika, a podniesienie bezpiecznika do otwartej szafki serwisowej. Odczytujesz dziennik zamka, wkładasz bezpiecznik do pustego gniazda i widzisz, jak kamera wraca do pracy. Dopiero po przywróceniu zasilania możesz odczytać archiwum kamery. Naprawa nie odzyskuje nagrania z chwili wejścia. Godziny na urządzeniach są tekstem gry, więc da się je wyraźnie przeczytać. Na ilustrowanej osi czasu zestawiasz odczyty z przedmiotami i wybierasz wniosek, który naprawdę wynika z dowodów. Błędny wniosek daje konkretną wskazówkę.

Podczas przesłuchań wybierasz pytania w pasie dialogowym, widząc rozmówcę i pokój. Na tablicy dowodów porównujesz też alibi. Cel widoczny u góry sceny oraz dwupoziomowe podpowiedzi prowadzą przez sprawę. Finał wymaga ułożenia całej teorii: kto ukradł prototyp, jak wszedł do laboratorium i dlaczego to zrobił. Jeśli teoria nie pasuje do dowodów, gra wskazuje konkretną sprzeczność.

Kod interfejsu znajduje się w `src/pl/detektyw/DetectiveGame.java`, a zasady śledztwa i jego postęp w `Investigation.java`. Starsza wersja konsolowa korzysta z tych samych zasad.

## Uruchomienie

Wymagana Java 17 lub nowsza. Pierwsze uruchomienie wymaga pobrania bibliotek JavaFX przez Maven.

**IntelliJ IDEA:** otwórz `pom.xml` jako projekt Maven i poczekaj na załadowanie zależności. Otwórz panel **Maven** po prawej stronie, następnie **Plugins → javafx → javafx:run**. Pojawi się okno gry. Jeśli panel nie pokazuje wtyczki, kliknij ikonę ponownego załadowania projektów Maven.

**Terminal, wersja okienkowa:**

```powershell
mvn javafx:run
```

**Terminal, starsza wersja konsolowa:**

```powershell
mvn compile
java '-Dfile.encoding=UTF-8' -cp target/classes pl.detektyw.Main
```

