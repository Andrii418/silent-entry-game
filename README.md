# Akta 01: Zniknięcie prototypu

Okienkowa gra detektywistyczna napisana w Javie i JavaFX. Podczas zamkniętej prezentacji znika prototyp urządzenia. Gra zaczyna się od wyboru akt; druga sprawa jest zapowiedziana w menu, a pierwsza jest grywalna. Laboratorium, korytarz i biuro mają pełnoekranowe sceny z klikalnymi przedmiotami i postaciami.

## Podgląd gry

![Laboratorium w pierwszej sprawie](docs/screenshots/game.jpg)

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

