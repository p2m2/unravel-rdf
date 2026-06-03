# Déboguer unravel-rdf en local dans une page HTML

Ce guide explique comment tester le bundle CDN directement dans un navigateur,
sans serveur distant et sans passer par npm.

---

## Prérequis

Avoir compilé et bundlé le projet :

```bash
sbt "fullOptJS; cdnPrepare"
# → target/cdn/unravel-rdf.min.js
```

---

## Option 1 : bundle inline (sans serveur HTTP)

La méthode la plus rapide. Le JS est injecté directement dans le HTML —
aucun problème de CORS avec `file://`.

```bash
# Générer le fichier de test
cat > target/cdn/test.html << 'EOF'
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="utf-8"><title>Debug unravel-rdf</title></head>
<body>
<h2>unravel-rdf — debug local</h2>
<pre id="out"></pre>
<script>
EOF

cat target/cdn/unravel-rdf.min.js >> target/cdn/test.html

cat >> target/cdn/test.html << 'EOF'
</script>
<script>
  const out = document.getElementById("out")
  const lib = window.UnravelRdf

  if (!lib) {
    out.textContent = "❌ window.UnravelRdf non défini"
  } else {
    out.textContent = "✅ Exports disponibles :\n" +
      JSON.stringify(Object.keys(lib), null, 2)
  }
</script>
</body>
</html>
EOF

# Ouvrir dans Firefox (permissif avec file://)
firefox target/cdn/test.html
```

---

## Option 2 : serveur HTTP local

Utile si tu veux charger des ressources externes (endpoint SPARQL, fichiers RDF)
depuis la page de test — le navigateur bloque ces requêtes depuis `file://`.

```bash
cd target/cdn
python3 -m http.server 8080
```

Crée `target/cdn/test.html` :

```html
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="utf-8"><title>Debug unravel-rdf</title></head>
<body>
<h2>unravel-rdf — debug local</h2>
<pre id="out"></pre>

<script src="./unravel-rdf.min.js"></script>
<script>
  const { SWDiscovery, SWDiscoveryConfiguration, URI } = window.UnravelRdf

  const out = document.getElementById("out")
  out.textContent = "Librairie chargée. Lancement d'une requête..."

  const config = SWDiscoveryConfiguration
    .init()
    .sparqlEndpoint("http://localhost:8890/sparql")  // adapte l'URL

  SWDiscovery(config)
    .something("s")
    .select("s")
    .commit()
    .raw()
    .then(r => {
      out.textContent = "✅ Résultats :\n" + JSON.stringify(r, null, 2)
    })
    .catch(e => {
      out.textContent = "❌ Erreur : " + e.message
    })
</script>
</body>
</html>
```

Puis ouvrir : [http://localhost:8080/test.html](http://localhost:8080/test.html)

---

## Option 3 : vérifier les exports depuis Node.js

Sans navigateur, pour valider rapidement le contenu du module CommonJS :

```bash
# Contenu du module npm (CommonJS)
node -e "const m = require('./target/npm/unravel-rdf.js'); console.log(Object.keys(m))"

# Vérifier une classe spécifique
node -e "
  const { SWDiscovery, SWDiscoveryConfiguration } = require('./target/npm/unravel-rdf.js')
  console.log(typeof SWDiscovery)
  console.log(typeof SWDiscoveryConfiguration)
"
```

> Note : ce test utilise `target/npm/unravel-rdf.js` (CommonJS),
> pas le bundle CDN. Le bundle CDN (`target/cdn/unravel-rdf.min.js`) est
> au format UMD et ne s'utilise pas directement avec `require()`.

---

## Séquence complète de rebuild + test

```bash
# Recompiler et rebundler
sbt "fullOptJS; cdnPrepare"

# Régénérer le fichier de test inline
cat > target/cdn/test.html << 'EOF'
<!DOCTYPE html><html><body><pre id="out"></pre><script>
EOF
cat target/cdn/unravel-rdf.min.js >> target/cdn/test.html
cat >> target/cdn/test.html << 'EOF'
</script><script>
  document.getElementById("out").textContent =
    window.UnravelRdf
      ? "✅ " + JSON.stringify(Object.keys(window.UnravelRdf), null, 2)
      : "❌ window.UnravelRdf non défini"
</script></body></html>
EOF

firefox target/cdn/test.html
```

---

## Erreurs fréquentes

| Erreur | Cause | Solution |
|---|---|---|
| `window.UnravelRdf` est `undefined` | `libraryExport: 'default'` dans webpack alors qu'il n'y a pas d'export default | Retirer `libraryExport` de `webpack.cdn.config.js` |
| `require is not defined` | Fichier CommonJS ouvert directement dans le browser | Utiliser `target/cdn/unravel-rdf.min.js`, pas `target/npm/unravel-rdf.js` |
| Modules non trouvés par webpack | `node_modules` absent de `target/npm/` | Relancer `sbt cdnPrepare` (fait le `npm install` automatiquement) |
| CORS bloqué sur `file://` | Requête vers un endpoint externe depuis `file://` | Utiliser le serveur HTTP local (Option 2) |
