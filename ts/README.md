sbt fastOptJS npmPrepareDebugRelease

cd ~/workspace/P2M2/discovery
npm link

cd ts/tests
npm link @p2m2/discovery

npm install --save-dev jest ts-jest @types/jest
npx jest github.issues.test.ts


