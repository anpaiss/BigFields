# BigFields

Karoo extension: native data fields drawn in digits as large as the cell allows, for riders who read the screen without glasses.

Put two BigFields fields on a data page and each number fills half the screen. BigFields scales the number to the cell instead of using the Karoo's standard text size, keeps it steady when the digit count changes (9.8 → 10.2 km/h), and draws label and unit small so the number gets the room.

## Big Pages

A Big Page field fills a whole data page with up to four data in rows of equal height, chosen in the app. The Karoo 3 editor decides field heights on its own, so this is the way to get two equal halves. On a Karoo 3, a Big Page with two rows draws digits 180 px tall; native fields on a two-field page draw them 130 px tall.

## Fields

32 fields, each reading one native Karoo value: speed (current, 3 s, average, max), heart rate (current, average), power (current, 3 s, average, normalized), cadence, distance, ride time, elapsed time, time of day, grade, altitude, ascent, distance / time / arrival to destination, climb left, next turn, distance and climb to the top, lap time / distance / speed / power, temperature, battery, calories.

Units follow the Karoo user profile (metric or imperial).

## Styles

Chosen in the app, applied to every field at once:

| Style | Look |
|---|---|
| Karoo dark | white on black, like the native dark theme |
| Karoo light | black on white |
| Vivid | black on orange; heart rate and power fields take the zone colour |
| Outline | white on near-black, zone colour on the border |
| Pastel | black on light tints, zone tint for heart rate and power |
| Ink | orange on black, zone colour on the number |

Every pairing meets WCAG AA contrast (4.5:1), checked by a unit test. The label with the unit can be hidden to give the whole cell to the number.

## Font

Numbers are drawn in Barlow Condensed Bold by default: condensed digits are narrower, so they grow taller in the same row. The Karoo's own font is one switch away. Barlow is licensed under the SIL Open Font License 1.1 (`app/src/main/assets/OFL-BarlowCondensed.txt`).

## Build

```
$env:JAVA_HOME="$env:USERPROFILE\.gradle\jdks\jetbrains_s_r_o_-21-amd64-windows.2"
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Status

First test on a Karoo 3 done with sample values; not yet used on a ride. Fields from other extensions are a planned addition.
