registerSuite({
    name: "Config",

    tests: [
        {
            name: "http://id.nlm.nih.gov/mesh/D012140",

            run: () =>
                (UnravelSession(UnravelConfig
                    .init()
                    .urlFile("http://id.nlm.nih.gov/mesh/D012140.ttl"))
                ).something(
                        "subject",
                        subject => subject.out(
                            "?rel","?obj"
                        ))
                    .finder
                    .count(["obj"]),

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