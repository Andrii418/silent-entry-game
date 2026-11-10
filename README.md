# Akta 01: Zniknięcie prototypu

Pierwsza wersja okienkowej gry detektywistycznej napisanej w Javie i JavaFX. Podczas zamkniętej prezentacji znika prototyp urządzenia. Zbadaj trzy miejsca, porozmawiaj z podejrzanymi i wskaż osobę, której wersja wydarzeń nie zgadza się z dowodami.

## Uruchomienie

Wymagana Java 17 lub nowsza. Pierwsze uruchomienie wymaga pobrania bibliotek JavaFX przez Maven.

**IntelliJ IDEA:** otwórz `pom.xml` jako projekt Maven i poczekaj na załadowanie zależności. Otwórz panel **Maven** po prawej stronie, następnie **Plugins → javafx → javafx:run**. Pojawi się okno gry. Jeśli panel nie pokazuje wtyczki, kliknij ikonę ponownego załadowania projektów Maven.

**Terminal, wersja okienkowa:**

```powershell
mvn javafx:run
```

**Terminal, starsza wersja konsolowa:**

```powershell
javac -encoding UTF-8 -d build src\pl\detektyw\*.java
java '-Dfile.encoding=UTF-8' -cp build pl.detektyw.Main
```

## Plan nauki

1. **Obecna wersja:** klasy, metody, `enum`, `Set`, stan gry i podstawy JavaFX.
2. **Następny krok:** dedukcje łączące kilka śladów oraz ciekawsza zagadka.
3. **Dalszy rozwój:** rozgałęzione przesłuchania, zapis gry, testy zasad i oprawa graficzna.

Kod gry znajduje się w `src/pl/detektyw/`. `DetectiveApp` buduje okno, `Investigation` przechowuje postęp, `CaseFile` zawiera treść sprawy, a `Main` i `Game` obsługują wersję konsolową.
