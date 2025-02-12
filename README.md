# <img src="https://junit.org/junit5/assets/img/junit5-logo.png" align="right" width="100">JUnit 5

This repository is the home of _JUnit 5_.

## Sponsors

[![Support JUnit](https://img.shields.io/badge/%F0%9F%92%9A-Support%20JUnit-brightgreen.svg)](https://junit.org/sponsoring)

* **Gold Sponsors:** [JetBrains](https://jb.gg/junit-logo)
* **Silver Sponsors:** [Micromata](https://www.micromata.de), [Quo Card](https://quo-digital.jp)
* **Bronze Sponsors:** [Premium Minds](https://www.premium-minds.com), [Testmo](https://www.testmo.com), [codefortynine](https://codefortynine.com), [Info Support](https://www.infosupport.com), [Stiltsoft](https://stiltsoft.com), [Code Intelligence](https://www.code-intelligence.com), [Route4Me](https://route4me.com/), [Testiny](https://www.testiny.io/)

## Latest Releases

- General Availability (GA): [JUnit 5.11.4](https://github.com/junit-team/junit5/releases/tag/r5.11.4) (December 16, 2024)
- Preview (Milestone/Release Candidate): [JUnit 5.12.0-RC2](https://github.com/junit-team/junit5/releases/tag/r5.12.0-RC2) (February 12, 2025)

## Documentation

- [User Guide]
- [Javadoc]
- [Release Notes]
- [Samples]

## Contributing

Contributions to JUnit 5 are both welcomed and appreciated. For specific guidelines
regarding contributions, please see [CONTRIBUTING.md] in the root directory of the
project. Those willing to use milestone or SNAPSHOT releases are encouraged
to file feature requests and bug reports using the project's
[issue tracker](https://github.com/junit-team/junit5/issues). Issues marked with an
<a href="https://github.com/junit-team/junit5/issues?q=is%3Aissue+is%3Aopen+label%3Aup-for-grabs">`up-for-grabs`</a>
label are specifically targeted for community contributions.

## Getting Help

Ask JUnit 5 related questions on [StackOverflow] or chat with the community on [Gitter].

## Continuous Integration Builds

[![CI Status](https://github.com/junit-team/junit5/workflows/CI/badge.svg)](https://github.com/junit-team/junit5/actions) [![Cross-Version Status](https://github.com/junit-team/junit5/workflows/Cross-Version/badge.svg)](https://github.com/junit-team/junit5/actions)

Official CI build server for JUnit 5. Used to perform quick checks on submitted pull
requests and for build matrices including the latest released OpenJDK and early access
builds of the next OpenJDK.

## Code Coverage

Code coverage using [JaCoCo] for the latest build is available on [Codecov].

A code coverage report can also be generated locally via the [Gradle Wrapper] by
executing `./gradlew clean jacocoRootReport`. The results will be available
in `build/reports/jacoco/jacocoRootReport/html/index.html`.

## Develocity

[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://ge.junit.org/scans)

JUnit 5 utilizes [Develocity](https://gradle.com/) for [Build Scans](https://scans.gradle.com/),
[Build Cache](https://docs.gradle.org/current/userguide/build_cache.html), and
[Predictive Test Selection](https://docs.gradle.com/enterprise/predictive-test-selection/).

The latest Build Scans are available on [ge.junit.org](https://ge.junit.org/). Currently,
only core team members can publish Build Scans on that server.
You can, however, publish a Build Scan to [scans.gradle.com](https://scans.gradle.com/) by
using the `--scan` parameter explicitly.

The remote Build Cache is enabled by default for everyone so that local builds can reuse
task outputs from previous CI builds.

## Building from Source

You need [JDK 21] to build JUnit 5. [Gradle toolchains] are used to detect and
potentially download additional JDKs for compilation and test execution.

All modules can be _built_ and _tested_ with the [Gradle Wrapper] using the following command.

`./gradlew build`

## Installing in Local Maven Repository

All modules can be _installed_ with the [Gradle Wrapper] in a local Maven repository for
consumption in other projects via the following command.

`./gradlew publishToMavenLocal`

## Dependency Metadata

[![JUnit Jupiter version](https://img.shields.io/maven-central/v/org.junit.jupiter/junit-jupiter/5..svg?color=25a162&label=Jupiter)](https://central.sonatype.com/search?namespace=org.junit.jupiter)
[![JUnit Vintage version](https://img.shields.io/maven-central/v/org.junit.vintage/junit-vintage-engine/5..svg?color=25a162&label=Vintage)](https://central.sonatype.com/search?namespace=org.junit.vintage)
[![JUnit Platform version](https://img.shields.io/maven-central/v/org.junit.platform/junit-platform-commons/1..svg?color=25a162&label=Platform)](https://central.sonatype.com/search?namespace=org.junit.platform)

Consult the [Dependency Metadata] section of the [User Guide] for a list of all artifacts
of the JUnit Platform, JUnit Jupiter, and JUnit Vintage.

See also <https://repo1.maven.org/maven2/org/junit/> for releases and
<https://oss.sonatype.org/content/repositories/snapshots/org/junit/> for snapshots.


[Codecov]: https://codecov.io/gh/junit-team/junit5
[CONTRIBUTING.md]: https://github.com/junit-team/junit5/blob/HEAD/CONTRIBUTING.md
[Dependency Metadata]: https://junit.org/junit5/docs/current/user-guide/#dependency-metadata
[Gitter]: https://gitter.im/junit-team/junit5
[Gradle toolchains]: https://docs.gradle.org/current/userguide/toolchains.html
[Gradle Wrapper]: https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:using_wrapper
[JaCoCo]: https://www.eclemma.org/jacoco/
[Javadoc]: https://junit.org/junit5/docs/current/api/
[JDK 21]: https://javaalmanac.io/jdk/21/
[Release Notes]: https://junit.org/junit5/docs/current/release-notes/
[Samples]: https://github.com/junit-team/junit5-samples
[StackOverflow]: https://stackoverflow.com/questions/tagged/junit5
[User Guide]: https://junit.org/junit5/docs/current/user-guide/
