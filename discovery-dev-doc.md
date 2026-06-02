# Discovery — documentation développeur

Cette note décrit les commandes utiles pour développer, construire et préparer la publication npm de `discovery`. Le projet est désormais centré sur **Scala.js** et la distribution JavaScript, avec une build optimisée pour la publication et une build debug avec source maps. [file:138][file:204]

## Pré-requis

L’environnement de développement repose sur Java 17, sbt 1.x et Node.js/npm, ce qui est cohérent avec le pipeline CI actuel. La compilation JavaScript s’appuie sur Scala.js et `scalajs-bundler`, avec des dépendances npm comme `axios`, `@comunica/query-sparql`, `n3` et `rdfxml-streaming-parser`. [file:138]

- Java 17
- sbt
- Node.js
- npm

Exemples de vérification :

```bash
java -version
sbt sbtVersion
node --version
npm --version
```

## Commandes sbt

### Tests

Pour lancer la suite de tests du projet :

```bash
sbt clean test
```

Cette commande correspond au job principal de test dans la CI. [file:138]

### Build debug

Pour générer une version debug JavaScript, lisible et avec source maps :

```bash
sbt fastOptJS
```

Dans la configuration actuelle, `fastOptJS` est défini avec optimisation désactivée, pretty print activé et source maps activées, ce qui en fait la cible adaptée au diagnostic et au débogage. [file:138]

### Build optimisée

Pour générer une version optimisée pour la distribution :

```bash
sbt fullOptJS
```

Dans la configuration actuelle, `fullOptJS` produit un module CommonJS sans source maps, adapté à une publication npm. [file:138]

### Préparer le package npm optimisé

Pour préparer le répertoire de publication npm standard :

```bash
sbt fullOptJS npmPrepareRelease
```

Cette commande génère un dossier `target/npm/` avec au minimum :
- `discovery.js`
- `package.json`
- `README.md` s’il existe à la racine du projet. [file:138]

### Préparer le package npm debug

Pour préparer le répertoire de publication npm debug :

```bash
sbt fastOptJS npmPrepareDebugRelease
```

Cette commande génère un dossier `target/npm-debug/` contenant la version debug de la bibliothèque, ainsi que la source map si elle est produite par Scala.js. [file:138]

## Artefacts générés

Les artefacts Scala.js attendus se trouvent sous `target/scala-2.13/scalajs-bundler/main/`. Dans l’état actuel observé, `fullOptJS` produit `discovery-opt.js`. [file:138]

Exemple de vérification :

```bash
find target/scala-2.13/scalajs-bundler/main -maxdepth 1 -type f | sort
```

## Commandes npm

### Publier la version optimisée

Une fois `target/npm/` préparé :

```bash
cd target/npm
npm publish --tag latest
```

Le package reste `@p2m2/discovery`, et la version optimisée est destinée au tag npm `latest`. [web:280]

### Publier la version debug

Une fois `target/npm-debug/` préparé :

```bash
cd target/npm-debug
npm publish --tag debug
```

La version debug peut ainsi être publiée dans le même registry sous le tag `debug`. [web:280]

### Installer côté utilisateur

Version standard :

```bash
npm install @p2m2/discovery
```

Version debug :

```bash
npm install @p2m2/discovery@debug
```

L’usage des dist-tags npm permet de publier plusieurs variantes d’un même package sans multiplier les noms de packages. [web:280]

## Publication vers Forge INRAE

Le registry npm ciblé est celui du projet GitLab/Forge, à l’URL de type :

```text
https://forge.inrae.fr/api/v4/projects/<CI_PROJECT_ID>/packages/npm/
```

GitLab documente cette méthode de publication via le Package Registry npm du projet. [web:254][web:266]

Exemple minimal avec authentification :

```bash
cd target/npm
echo "@p2m2:registry=https://forge.inrae.fr/api/v4/projects/<PROJECT_ID>/packages/npm/" > .npmrc
echo "//forge.inrae.fr/api/v4/projects/<PROJECT_ID>/packages/npm/:_authToken=<TOKEN>" >> .npmrc
npm publish --tag latest
```

Exemple debug :

```bash
cd target/npm-debug
echo "@p2m2:registry=https://forge.inrae.fr/api/v4/projects/<PROJECT_ID>/packages/npm/" > .npmrc
echo "//forge.inrae.fr/api/v4/projects/<PROJECT_ID>/packages/npm/:_authToken=<TOKEN>" >> .npmrc
npm publish --tag debug
```

## Workflow recommandé

### Développement local

```bash
sbt clean test
sbt fastOptJS
```

Utiliser `fastOptJS` pour le diagnostic local est pertinent car cette cible garde les source maps et un JS plus lisible. [file:138]

### Préparation d’une release optimisée

```bash
export DISCOVERY_VERSION=0.4.5
sbt clean fullOptJS npmPrepareRelease
cd target/npm
npm publish --tag latest
```

### Préparation d’une release debug

```bash
export DISCOVERY_VERSION=0.4.5-debug
sbt clean fastOptJS npmPrepareDebugRelease
cd target/npm-debug
npm publish --tag debug
```

## Débogage

Pour le debug, l’objectif est de disposer d’une version JS interprétable avec source maps afin de retrouver plus facilement les fichiers Scala et des positions de code utiles. Les source maps servent précisément à relier le code transformé au code source d’origine dans les outils de debug. [web:281][file:138]

Dans un runtime Node.js, l’exploitation des source maps peut nécessiter une option dédiée, par exemple :

```bash
node --enable-source-maps your-script.js
```

Le support exact dépend du mode d’exécution et des outils utilisés autour de la bibliothèque. [web:272][web:281]
