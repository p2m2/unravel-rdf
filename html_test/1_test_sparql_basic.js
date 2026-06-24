registerSuite({
    name: "SPARQL – requêtes de base",
    tests: [
        {
            name: "out() – subject liés à CHEBI:106243",
            run: () =>
                UnravelSession(configMetaNetX)
                    .prefix("CHEBI", "http://purl.obolibrary.org/obo/CHEBI_")
                    .something(
                        "subject",
                        subject => subject.out(
                            "?rel",
                            URI("CHEBI:106243")
                        )
                    )
                    .select("subject","rel")
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
        },
        {
            name: "out() – subject liés à CHEBI:106243 par la relation https://rdf.metanetx.org/schema/chemXref",
            run: () =>
                UnravelSession(configMetaNetX)
                    .prefix("CHEBI", "http://purl.obolibrary.org/obo/CHEBI_")
                    .prefix("mtx","https://rdf.metanetx.org/chem/")
                    .something(
                        "subject",
                        subject => subject.out(
                            "https://rdf.metanetx.org/schema/chemXref",
                            "?object"
                        )
                    )
                    .select("subject","object")
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
        },
        {
            name: "out() – (VARIANTE) subject liés à CHEBI:106243 par la relation https://rdf.metanetx.org/schema/chemXref",
            run: () =>
                UnravelSession(configMetaNetX)
                    .prefix("CHEBI", "http://purl.obolibrary.org/obo/CHEBI_")
                    .prefix("mtx","https://rdf.metanetx.org/chem/")
                    .something(
                        "subject",
                        subject => subject.out(
                            URI("https://rdf.metanetx.org/schema/chemXref"),
                            Var("object")
                        )
                    )
                    .select("subject","object")
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
        },
        {
            name: "in() – object liés à https://rdf.metanetx.org/chem/MNXM586757",
            run: () =>
                UnravelSession(configMetaNetX)
                    .prefix("CHEBI", "http://purl.obolibrary.org/obo/CHEBI_")
                    .prefix("mtx", "https://rdf.metanetx.org/chem/")
                    .something(
                        "object",
                        object => object.in(
                            "?rel",
                            URI("mtx:MNXM586757")
                        )
                    )
                    .select("object", "rel")
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
        },
        {
            name: "in() – VARIANTE object liés à https://rdf.metanetx.org/chem/MNXM586757",
            run: () =>
                UnravelSession(configMetaNetX)
                    .prefix("CHEBI", "http://purl.obolibrary.org/obo/CHEBI_")
                    .prefix("mtx","https://rdf.metanetx.org/chem/")
                    .something(
                        "object",
                        object => object.in(
                            Var("rel"),
                            "mtx:MNXM586757"
                        )
                    )
                    .select("object","rel")
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
        },
        {
            name: "traverse() – object liés à https://rdf.metanetx.org/chem/MNXM586757",
            run: () =>
                UnravelSession(configMetaNetX)
                    .prefix("CHEBI", "http://purl.obolibrary.org/obo/CHEBI_")
                    .prefix("mtx", "https://rdf.metanetx.org/chem/")
                    .something(
                        "node",
                        node => node.traverse(
                            "?rel",
                            "mtx:MNXM586757"
                        )
                    )
                    .select("node", "rel")
                    .limit(30)
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
        },

    ]
});