registerSuite({
    name: "Finder",

    tests: [
        {
            name: "count()",

            run: () =>
                UnravelSession(configMetaNetX)
                    .prefix("CHEBI", "http://purl.obolibrary.org/obo/CHEBI_")
                    .something(
                        "subject",
                        subject => subject.in().out(
                            "?rel",
                            URI("CHEBI:106243")
                        ))
                    .finder
                    .count(["subject"]),

            check: result => {
                const rows =
                    Array.isArray(result)
                        ? result
                        : (result.results?.bindings ?? []);

                if (result <= 0)
                    throw new Error("count invalide");
            }
        }
    ]
});