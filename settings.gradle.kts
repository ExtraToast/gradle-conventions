rootProject.name = "gradle-conventions"

include(
    "aggregate",
    "plugins:kotlin",
    "plugins:detekt",
    "plugins:ktlint",
    "plugins:spring",
    "plugins:testing",
    "plugins:test-logging",
    "plugins:jooq-codegen",
)
