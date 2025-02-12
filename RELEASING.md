# Releasing

## Pre-release steps

- [ ] Switch or create the release branch for this feature release (e.g. `releases/5.12.x`)
- [ ] Change `version`, `platformVersion`, and `vintageVersion` in `gradle.properties` to the versions about to be released
- [ ] Change release date in Release Notes
- [ ] Change release date in `README.MD`
- [ ] Commit with message "Release ${VERSION}"
- [ ] Execute `./gradlew --no-build-cache --no-configuration-cache clean build publish closeSonatypeStagingRepository`
- [ ] Tag current commit: `git tag -s -m ${VERSION} r${VERSION}`
- [ ] Change `version`, `platformVersion`, and `vintageVersion` properties in `gradle.properties` on release branch to new development versions and commit with message "Back to snapshots for further development" or similar
- [ ] Push release branch and tag to GitHub: `git push --set-upstream --follow-tags origin HEAD`
- [ ] Trigger a [release build](https://github.com/junit-team/junit5/actions/workflows/release.yml): `gh workflow run --ref r${VERSION} -f releaseVersion=${VERSION} -f stagingRepoId=orgjunit-1234 release.yml`
  - Select the release branch
  - Enter the version to be released
  - Enter the staging repository ID from the output of above Gradle build

## Post-release steps

- [ ] Post about the new release:
    - [ ] [Mastodon](https://fosstodon.org/@junit)
    - [ ] [Bluesky](https://bsky.app/profile/junit.org)

### Preview releases (milestones and release candidates)

- [ ] Fast-forward merge the release branch to `main` and push to GitHub
- [ ] Create release notes for the next preview or feature release from the template

### Feature releases (x.y.0)

- [ ] Fast-forward merge the release branch to `main` and push to GitHub
- [ ] Update the [security policy](https://github.com/junit-team/junit5/blob/main/SECURITY.md) and commit with message "Update security policy to reflect 5.x release" or similar
- [ ] Create release notes for the next feature release from the template
- [ ] Update [JBang catalog](https://github.com/junit-team/jbang-catalog/blob/main/jbang-catalog.json)

### Patch releases (x.y.z)

- [ ] Cherry-pick the tagged commit from the release branch to `main` and resolve the conflict in `gradle.properties` by choosing the version of the `main` branch
- [ ] Include the release notes of the patch release on `main` if not already present
- [ ] Update [JBang catalog](https://github.com/junit-team/jbang-catalog/blob/main/jbang-catalog.json)
