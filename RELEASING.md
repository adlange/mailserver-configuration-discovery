# Releasing

Releases are published to Maven Central through the [Sonatype Central Portal](https://central.sonatype.com) by the
GitHub workflow [`release.yml`](.github/workflows/release.yml), which runs on every `v*` tag.

## Prerequisites (one-time)

* **Central Portal account and namespace.** Sign in on https://central.sonatype.com (OSSRH accounts were migrated).
  Account menu → *View Namespaces* must list `de.adrianlange` as *Verified*. If not: *Add Namespace*, then verify it
  with the DNS TXT record the portal shows for `adrianlange.de`.
* **Portal user token.** Account menu → *View Account* → *Generate User Token* (this invalidates a previous token).
  The generated username/password pair goes into the GitHub Actions secrets `CENTRAL_TOKEN_USERNAME` and
  `CENTRAL_TOKEN_PASSWORD` — and into your local `~/.m2/settings.xml` for dry runs, see below.
* **GPG key.** The portal verifies signatures against a public keyserver. The public key of the release key must be
  available on `keyserver.ubuntu.com`, `keys.openpgp.org` or `pgp.mit.edu`:

  ```
  gpg --list-secret-keys --keyid-format long        # key id and expiry
  gpg --keyserver hkps://keyserver.ubuntu.com --send-keys <KEYID>
  ```

  If sending fails (corporate proxies often block gpg's network access), export with
  `gpg --armor --export <KEYID>` and paste the key into the *Submit Key* form on https://keyserver.ubuntu.com.
  The private key (`gpg --armor --export-secret-keys <KEYID>`) goes into the secret `GPG_PRIVATE_KEY`, its
  passphrase into `GPG_PASSPHRASE`.

## Publishing a release

1. Set the release version in `pom.xml` (remove `-SNAPSHOT`).
2. Make sure `CHANGELOG.md` has a `## <version>` section — the workflow extracts it as release notes and fails if it
   is missing.
3. Commit, then tag and push:

   ```
   git commit -am "Release <version>"
   git tag v<version>
   git push && git push origin v<version>
   ```

4. Watch the *Release* workflow. It refuses to run if the tag does not match the pom version or the version is a
   snapshot. The deploy waits until the portal reports the deployment as *published*.
5. Check the result: the artifact page
   https://central.sonatype.com/artifact/de.adrianlange/mailserver-configuration-discovery appears immediately,
   sync to `repo1.maven.org` and the search index follows with some delay. The GitHub release with the CHANGELOG
   section and the jars is created by the workflow.
6. Set the next `<version>-SNAPSHOT` in `pom.xml`, start a new `## Unreleased` section in `CHANGELOG.md`, commit.

## Dry run without publishing

A deployment can be validated on the portal without releasing anything.

The project version must be a release version for this: for a `-SNAPSHOT` version the plugin does not create a
portal deployment but uploads to the separate snapshot repository, which fails with `403 Forbidden` unless snapshot
publishing is enabled for the namespace. Set the release version locally first (no need to commit yet):

```
./mvnw versions:set -DnewVersion=<version> -DgenerateBackupPoms=false
```

Put the portal token into `~/.m2/settings.xml`:

```xml
<server>
  <id>central</id>
  <username><!-- token username --></username>
  <password><!-- token password --></password>
</server>
```

Then:

```
./mvnw -Prelease deploy -Dgpg.keyname=<KEYID> -Dcentral.autoPublish=false -Dcentral.waitUntil=validated
```

`-Dgpg.keyname` matters when your local keyring holds more than one key (for example a commit-signing key): without
it gpg signs with its default key, the portal cannot find that key on the keyservers and the deployment fails
validation. In CI the release key is the only key, so the workflow does not need it.

On https://central.sonatype.com under account menu → *View Deployments* the deployment runs through
*Pending* → *Validating* → *Validated*. Open it, check that all components (jar, sources, javadoc, pom) and their
signatures were accepted, then *Drop* it. Nothing is published.

## When a release fails

* A deployment that is *not yet published* can be dropped in the portal (*View Deployments* → *Drop*).
* Delete the tag (`git tag -d v<version>`, `git push --delete origin v<version>`) and the GitHub release if it was
  already created, fix the problem and start over.
* A **published** version is final and cannot be removed or replaced — publish a fixed patch version instead.
