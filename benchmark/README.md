# Benchmark

Planned macrobenchmark (to add when `androidx.benchmark:benchmark-macro-junit4` is adopted):

- `StartupBenchmark` – cold start MainActivity, measure timeToInitialDisplay.
- `TileToggleBenchmark` – toggle QS tile state and assert no ANR.
- `ServiceStartBenchmark` – start MediaPlaybackService and measure notification posted latency.

To run:

```bash
./gradlew :benchmark:connectedCheck
```

Current baseline (manual, Pixel 6, debug): cold start ~380ms, service start <120ms. Add automated baselines once CI has a device farm.
