registerSuite({
    name: "DBPedia request",
    tests: [
        {
            name: "isA() – DBPedia Albums",
            run: () =>
                UnravelSession(configDbpedia)
                    .something(
                        "album",
                        album => album.isA("http://dbpedia.org/ontology/Album")
                        )
                    .select("album")
                    .limit(3)
                    .commit()
                    .raw(),

            check: result => {

                if (!result)
                    throw new Error("Résultat vide");

                const rows =
                    Array.isArray(result)
                        ? result
                        : (result.results?.bindings ?? []);

                if (rows.length === 0)
                    throw new Error("Aucun résultat");
            }
        }
    ]
});