# Changelog

## 0.1.0

### Breaking changes

* the library requires Java 21 (previously Java 11)
* `MailserverConfigurationDiscovery.discover(...)` returns a `Set` instead of a `List`, so results no longer contain duplicates
* renamed `ConfigurationMethod.RFC_61186` to `RFC_6186` and `MOZILLA_AUTOCONF` to `MOZILLA_AUTOCONFIG`; removed the never implemented `MS_AUTODISCOVER_V1`
* renamed all `MozillaAutoconf*` types to `MozillaAutoconfig*` (package `strategy.mozillaautoconfig`, `MozillaAutoconfigMailserverService`, ...) to match the official name of the mechanism
* moved `EmailAddress` (with `EmailAddress.DomainPart`) from `de.adrianlange.mcd.strategy` to `de.adrianlange.mcd`
* renamed `MailserverConfigurationDiscoveryContextBuilder.useTcpForDnsLookups` to `withTcpForDnsLookups` and `DiscoveryScope.get( Protocol )` to `DiscoveryScope.of( Protocol )`
* Mozilla Autoconfig documents are fetched over HTTPS only by default; plain HTTP can be enabled via `withInsecureHttpAllowed( true )`. Previously only HTTP URLs were queried, which silently failed for providers redirecting to HTTPS
* removed the DNS TXT `mailconf=` lookup from the Mozilla Autoconfig strategy. It was based on a 2008 proposal (Thunderbird:Autoconfiguration:DNSBasedLookup) that Thunderbird never implemented, and it never worked because it queried SRV instead of TXT records
* `DnsLookupContext.getDnsServers()` returns an empty collection instead of `null` if no server is configured

### Added

* SRV strategy: `_submissions._tcp` (RFC 8314 section 5.1), reported as SMTP with socket type SSL
* `withHttpTimeout( Duration )` sets the connect and request timeout for fetching Autoconfig documents (default 10 seconds)
* README sections on installation, all context options, scope and limitations, security and roadmap

### Changed

* a failing discovery strategy no longer fails the whole discovery: the failure is logged on WARN level and the results of the other strategies are returned
* the default executor is a virtual-thread-per-task executor instead of a new `ForkJoinPool` per context
* HTTP redirects are followed (including HTTP to HTTPS, never HTTPS to HTTP) and non-200 responses are ignored instead of being parsed
* the `emailaddress` query parameter of the Autoconfig URL is URL-encoded
* Mozilla Autoconfig parser: servers with an invalid `<port>` are skipped, unknown `<authentication>` and `<socketType>` values are ignored instead of adding `null` to the result
* invalid DNS configuration fails with `IllegalArgumentException` instead of `AssertionError`/`RuntimeException`; unparsable DNS names and unreachable documents are logged on DEBUG instead of ERROR
* Javadoc corrections (results are sets, `getHost()` is a hostname, all configured DNS servers are used, placeholder handling)

### Build and dependencies

* publish releases via the Sonatype Central Portal (`central-publishing-maven-plugin`) instead of the discontinued OSSRH staging deploy. The release process is documented in RELEASING.md
* CI runs on pull requests and pushes to main with a Java 21 and 25 matrix, and Dependabot also watches the GitHub Actions versions
* update dependencies (commons-validator 1.11.0, dnsjava 3.6.5, JUnit Jupiter 5.14.4), declare `slf4j-api` explicitly
* migrate tests from Spock/Groovy to JUnit 5 + Mockito, removing all Groovy and Spock dependencies; tests live in the same packages as the code and inject mocks via package-private constructors
* `maven.compiler.release` instead of source/target, pinned compiler plugin, Mockito loaded as a Java agent, `slf4j-simple` with DEBUG output for the library in tests

## 0.0.3

* fix XXE_DOCUMENT and URLCONNECTION_SSRF_FD bugs when looking up XML autoconf files

## 0.0.2

* add support for Mozilla Autoconf
* remove synchronous discovery to minimize complexity

## 0.0.1

* initial release with RFC 6186 support
