# GambaSlots Fabric-Mod

Eine lokale Erz-Slotmaschine für **Minecraft Java 1.21.11** und Fabric. Das Menü orientiert sich an den beigefügten Bildern: dunkles Violett, ein 9×3-Raster im Stil eines Minecraft-Truhenmenüs und drei hervorgehobene Felder als Gewinnlinie.

## Funktionen

- Das Menü öffnet und schließt sich mit **J**. Die Taste lässt sich unter **Optionen → Steuerung → Tastenbelegung → GambaSlots** ändern.
- Die Walzen zeigen Minecraft-Erze und drehen zufällig **2 bis 4 Sekunden**.
- Nur drei gleiche Symbole auf der goldenen Linie in der mittleren Reihe zahlen aus.
- Der Einsatz lässt sich über das Eingabefeld oder die Knöpfe `-10`, `-1`, `+1`, `+10` setzen.
- Null, negative, ungültige und nicht bezahlbare Einsätze werden abgelehnt; die Drehung startet dann nicht.
- Virtuelle Münzen und Gewinn-Multiplikatoren:

| Drei gleiche Symbole | Auszahlung | Gewicht je Walze |
| --- | ---: | ---: |
| Kohleerz | ×2 | 65 |
| Eisenerz | ×3 | 15 |
| Golderz | ×5 | 10 |
| Diamanterz | ×8 | 6 |
| Smaragderz | ×15 | 3 |
| Antiker Schrott | ×30 | 1 |

Die Auszahlung ist brutto. Bei Einsatz 10 und drei Diamanterzen werden 80 Münzen gutgeschrieben; der Nettogewinn ist 70 Münzen.

## Wichtig: lokales Guthaben

Diese Version ist eine **clientseitige Demo-Mod**. Das virtuelle Guthaben wird lokal in `.minecraft/config/gambaslots.properties` gespeichert. Sie verbindet sich nicht mit einer Serverwirtschaft, verändert keine echten Minecraft-Items und kann keinen Kontostand auf einem Multiplayer-Server ändern. Für eine gemeinsame Serverwirtschaft müsste zusätzlich eine serverseitige Fabric-Mod samt Netzwerkprotokoll installiert werden.

## Installieren

1. Installiere **Fabric Loader für Minecraft 1.21.11** und starte das Spiel einmal mit dem Fabric-Profil.
2. Lade **Fabric API für Minecraft 1.21.11** herunter.
3. Lege `GambaSlots-Fabric-1.0.0.jar` und die passende Fabric-API-JAR in `%appdata%\.minecraft\mods`.
4. Starte Minecraft mit dem Fabric-Profil. Betritt eine Welt und drücke **J**.

Java 21 wird benötigt. Bei alternativen Launchern kommt die Mod-JAR in den jeweiligen Instanzordner unter `mods`.

## Projekt lokal bauen

Voraussetzung: JDK 21.

Im Projektordner `GambaSlots`:

```powershell
.\gradlew.bat build
```

Die fertige Mod liegt anschließend in `build/libs/`. Loom lädt Gradle, Minecraft, Fabric Loader und Fabric API beim ersten Build automatisch herunter.

## GitHub

Das Projekt kann als normales GitHub-Repository hochgeladen werden. GitHub Actions baut die Mod bei jedem Push und Pull Request und stellt die JAR als Workflow-Artefakt zum Herunterladen bereit.
