sbt fastOptJS npmPrepareDebugRelease

cd ~/workspace/P2M2/discovery
npm link

cd ts/tests
npm link @p2m2/discovery

npx tsx github.issues.test.ts

