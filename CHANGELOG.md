# Changelog

## 0.1.0

* **Breaking:** renamed `ConfigurationMethod.RFC_61186` to `RFC_6186` and `MOZILLA_AUTOCONF` to `MOZILLA_AUTOCONFIG`; removed the never implemented `MS_AUTODISCOVER_V1`
* **Breaking:** renamed all `MozillaAutoconf*` types to `MozillaAutoconfig*` (package `strategy.mozillaautoconfig`, `MozillaAutoconfigMailserverService`, ...) to match the official name of the mechanism
* **Breaking:** moved `EmailAddress` (with `EmailAddress.DomainPart`) from `de.adrianlange.mcd.strategy` to `de.adrianlange.mcd`
* **Breaking:** renamed `MailserverConfigurationDiscoveryContextBuilder.useTcpForDnsLookups` to `withTcpForDnsLookups` and `DiscoveryScope.get( Protocol )` to `DiscoveryScope.of( Protocol )`
* **Breaking:** removed the DNS TXT `mailconf=` lookup from the Mozilla Autoconfig strategy. It was based on a 2008 proposal (Thunderbird:Autoconfiguration:DNSBasedLookup) that Thunderbird never implemented, and it never worked because it queried SRV instead of TXT records
* SRV strategy: added `_submissions._tcp` (RFC 8314 section 5.1), reported as SMTP with socket type SSL
* Mozilla Autoconfig parser: servers with an invalid `<port>` are skipped, unknown `<authentication>` and `<socketType>` values are ignored instead of adding `null` to the result
* a failing discovery strategy no longer fails the whole discovery: the failure is logged on WARN level and the results of the other strategies are returned
* the default executor is a virtual-thread-per-task executor instead of a new `ForkJoinPool` per context; it suits the blocking DNS/HTTP I/O and needs no shutdown
* `DnsLookupContext.getDnsServers()` returns an empty collection instead of `null` if no server is configured; invalid DNS configuration fails with `IllegalArgumentException` instead of `AssertionError`/`RuntimeException`, and unparsable DNS names are logged on DEBUG instead of ERROR
* build: `maven.compiler.release` instead of source/target (fixes the javac system-modules warning), pinned compiler plugin, Mockito loaded as a Java agent (removes the self-attach warning)
* Mozilla Autoconfig documents are fetched over HTTPS only by default; plain HTTP can be enabled via `MailserverConfigurationDiscoveryContextBuilder.withInsecureHttpAllowed( true )`. Previously only HTTP URLs were queried, which silently failed for providers redirecting to HTTPS.
* HTTP redirects are followed now (including HTTP to HTTPS), HTTP requests have a configurable timeout (`withHttpTimeout`, default 10 seconds), and non-200 responses are ignored instead of being parsed
* the `emailaddress` query parameter of the Autoconf URL is URL-encoded now
* `MailserverConfigurationDiscovery` returns sets instead of lists now, s.th. result set won't contain duplicates
* the library requires Java 21 now (previously Java 11)
* update dependencies (commons-validator 1.11.0, dnsjava 3.6.5, JUnit Jupiter 5.14.4)
* migrate tests from Spock/Groovy to JUnit 5 + Mockito, removing all Groovy and Spock dependencies

## 0.0.3

* fix XXE_DOCUMENT and URLCONNECTION_SSRF_FD bugs when looking up XML autoconf files

## 0.0.2

* add support for Mozilla Autoconf
* remove synchronous discovery to minimize complexity

## 0.0.1

* initial release with RFC 6186 support
