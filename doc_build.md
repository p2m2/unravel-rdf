# Tâches SBT

Ce document décrit les principales commandes SBT utilisées dans le projet et leur rôle.

## Démarrer SBT

```bash
sbt
```

Ouvre la console interactive SBT.  
Utile pour enchaîner plusieurs commandes sans relancer SBT à chaque fois.

## Compiler le projet

```bash
sbt compile
```

Compile le code source du projet.  
Cela permet de détecter rapidement les erreurs de compilation.

## Lancer les tests

```bash
sbt test
```

Exécute tous les tests du projet.  
Utile pour vérifier que les modifications n’ont pas cassé le comportement attendu.

## Lancer un test précis

```bash
sbt testOnly nom.du.Test
```

Exécute uniquement une classe ou un fichier de test donné.  
Pratique pour itérer rapidement sur une zone de code précise.

## Exécuter une tâche en mode surveillé

```bash
sbt ~test
```

Relance automatiquement les tests à chaque modification de fichier.  
Très utile pendant le développement pour obtenir un retour continu.

## Générer le package JAR

```bash
sbt package
```

Construit le JAR du projet.  
À utiliser quand on veut produire un artefact exécutable ou distribuable.

## Publier l’artefact

```bash
sbt publish
```

Publie l’artefact dans le dépôt configuré.  
À utiliser pour diffuser une version du projet.

## Nettoyer le projet

```bash
sbt clean
```

Supprime les fichiers générés dans `target/`.  
Utile quand il faut repartir d’un build propre.

## Exécuter une tâche personnalisée

```bash
sbt generateUnravelVersionFile
sbt npmPrepareRelease
sbt npmPrepareDebugRelease
sbt cdnPrepare
sbt cdnDebugPrepare
```

### Description
- `generateUnravelVersionFile` : génère un fichier Scala contenant la version utilisée au build.
- `npmPrepareRelease` : prépare un dossier npm optimisé pour publication.
- `npmPrepareDebugRelease` : prépare un dossier npm avec version debug.
- `cdnPrepare` : construit le bundle CDN optimisé.
- `cdnDebugPrepare` : construit le bundle CDN en mode debug.

## Exemple de flux complet

```bash
sbt clean compile test
sbt fullOptJS
sbt npmPrepareRelease
sbt cdnPrepare
```

### Ce que fait ce flux
1. Nettoie le build.
2. Compile le projet.
3. Exécute les tests.
4. Génère le bundle Scala.js optimisé.
5. Prépare le package npm de release.
6. Construit le bundle CDN final.