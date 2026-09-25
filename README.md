# BigFields

Karoo extension: native data fields drawn in digits as large as the cell allows, for riders who read the screen without glasses.

Put two BigFields fields on a data page and each number fills half the screen. BigFields scales the number to the cell instead of using the Karoo's standard text size, keeps it steady when the digit count changes (9.8 → 10.2 km/h), and draws label and unit small so the number gets the room.

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

## Build

```
$env:JAVA_HOME="$env:USERPROFILE\.gradle\jdks\jetbrains_s_r_o_-21-amd64-windows.2"
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Status

Built and unit-tested, not yet run on a Karoo. Fields from other extensions are a planned addition.
