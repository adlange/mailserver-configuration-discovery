# Changelog

## 0.1.0

* **Breaking:** renamed `ConfigurationMethod.RFC_61186` to `RFC_6186` and `MOZILLA_AUTOCONF` to `MOZILLA_AUTOCONFIG`; removed the never implemented `MS_AUTODISCOVER_V1`
* **Breaking:** renamed all `MozillaAutoconf*` types to `MozillaAutoconfig*` (package `strategy.mozillaautoconfig`, `MozillaAutoconfigMailserverService`, ...) to match the official name of the mechanism
* **Breaking:** moved `EmailAddress` (with `EmailAddress.DomainPart`) from `de.adrianlange.mcd.strategy` to `de.adrianlange.mcd`
* **Breaking:** renamed `MailserverConfigurationDiscoveryContextBuilder.useTcpForDnsLookups` to `withTcpForDnsLookups` and `DiscoveryScope.get( Protocol )` to `DiscoveryScope.of( Protocol )`
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
