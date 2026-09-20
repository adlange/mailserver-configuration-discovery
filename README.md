# Mailserver Configuration Discovery

A Java library that looks up the mailserver client configuration (IMAP, POP3, SMTP) a domain owner has published
for their domain, the same way a mail client does when a user adds an account. Give it a domain or an email
address and it returns the hosts, ports, socket types and authentication hints it found.

The library only reports what the domain itself publishes via DNS or its own web server. It does not guess, it does
not query third-party databases and it does not connect to the mailservers it finds (see [Scope and limitations](#scope-and-limitations)).

## Supported discovery methods

* **DNS SRV records** according to [RFC 6186](https://www.rfc-editor.org/rfc/rfc6186) (`_submission`, `_imap`,
  `_imaps`, `_pop3`, `_pop3s`) plus `_submissions` from
  [RFC 8314 section 5.1](https://www.rfc-editor.org/rfc/rfc8314#section-5.1).
* **Mozilla Autoconfig** according to the
  [Thunderbird Autoconfiguration](https://wiki.mozilla.org/Thunderbird:Autoconfiguration) specification: the XML
  document is fetched from `https://autoconfig.<domain>/mail/config-v1.1.xml` and from
  `https://<domain>/.well-known/autoconfig/mail/config-v1.1.xml`.

## Installation

The library requires Java 21 or newer.

```xml
<dependency>
  <groupId>de.adrianlange</groupId>
  <artifactId>mailserver-configuration-discovery</artifactId>
  <version>0.1.0</version>
</dependency>
```

```groovy
implementation( 'de.adrianlange:mailserver-configuration-discovery:0.1.0' )
```

How releases of this library are published is described in [RELEASING.md](RELEASING.md).

The library logs through the [SLF4J](https://www.slf4j.org/) API. Add a binding such as Logback or
`slf4j-simple` to your application to see the output. On DEBUG level the library tells you why a document or DNS
lookup was ignored, which is the first thing to look at when a lookup returns nothing.

## Usage

The simplest lookup needs no configuration and works with a domain or a complete email address:

```java
Set<MailserverService> servicesA = MailserverConfigurationDiscovery.discover( "dummy-domain.com" );
Set<MailserverService> servicesB = MailserverConfigurationDiscovery.discover( EmailAddress.of( "user@dummy-domain.com" ) );
```

Passing the email address instead of the domain has two effects: Mozilla Autoconfig documents can be requested for
the specific address (some providers generate per-user documents) and the placeholders in the returned configuration
(see below) are filled in for you.

`EmailAddress` and `EmailAddress.DomainPart` live in the package `de.adrianlange.mcd`. Both accept Unicode
(IDN) input and throw an `IllegalArgumentException` for invalid addresses or domains.

### Context configuration

A context configures the lookup. Build it with `MailserverConfigurationDiscoveryContextBuilder`; every option has a
sensible default, so only set what you need.

| Builder method | Default | Effect |
|---|---|---|
| `withConfigurationMethods( ConfigurationMethod... )` | all | Which discovery methods run: `RFC_6186`, `MOZILLA_AUTOCONFIG` |
| `withDiscoveryScopes( DiscoveryScope... )` | all | `SUBMISSION` (SMTP) and/or `RECEPTION` (IMAP, POP3) |
| `withDnsServer( String )` | system resolvers | Adds a DNS server; several servers are used with failover |
| `withDnsLookupTimeout( Duration )` | 10 s | Timeout per DNS query |
| `withDnsLookupRetries( int )` | 3 | Retries per DNS query |
| `withTcpForDnsLookups( boolean )` | `false` | Use TCP instead of UDP for DNS |
| `withHttpTimeout( Duration )` | 10 s | Connect and request timeout for fetching Autoconfig documents |
| `withInsecureHttpAllowed( boolean )` | `false` | Additionally fetch Autoconfig documents over plain HTTP, see [Security](#security) |
| `withExecutor( Executor )` | virtual threads | Executor running the concurrent lookups |

Use only RFC 6186 configurations:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withConfigurationMethods( ConfigurationMethod.RFC_6186 )
    .build();
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com", context );
```

Only look for submission (SMTP) configurations:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withDiscoveryScopes( MailserverConfigurationDiscoveryContext.DiscoveryScope.SUBMISSION )
    .build();
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com", context );
```

Use a specific DNS resolver:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withDnsServer( "9.9.9.9" )
    .withDnsLookupTimeout( Duration.ofSeconds( 5 ) )
    .withDnsLookupRetries( 2 )
    .build();
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com", context );
```

Allow plain HTTP for Autoconfig documents and shorten the HTTP timeout. Read the [Security](#security) section
before enabling HTTP:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withInsecureHttpAllowed( true )
    .withHttpTimeout( Duration.ofSeconds( 5 ) )
    .build();
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com", context );
```

All lookups run concurrently on an `Executor`. By default a virtual-thread-per-task executor is used, which suits
the blocking DNS and HTTP I/O and needs no shutdown. If you want to limit concurrency or reuse an existing pool,
provide your own executor:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withExecutor( Executors.newFixedThreadPool( 2 ) )
    .build();
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com", context );
```

### Discovered mailserver configurations

`discover(...)` returns a `Set<MailserverService>` without duplicates. Every entry carries the protocol, host,
port, socket type (`PLAIN`, `STARTTLS`, `SSL` or `null` if unknown) and the `ConfigurationMethod` it was found
with. Depending on that method the entry can be cast to a subtype with method-specific details:

```java
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com" );
for( MailserverService service : services ) {
  if( service instanceof SrvRecordMailserverService srv ) {
    // srv.getPriority(), srv.getWeight()
  } else if( service instanceof MozillaAutoconfigMailserverService autoconfig ) {
    // autoconfig.getUsername(), autoconfig.getPassword(), autoconfig.getAuthentications(), autoconfig.getOAuth2s()
  }
}
```

or, using the configuration method:

```java
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com" );
for( MailserverService service : services ) {
  if( service.getConfigurationMethod() == ConfigurationMethod.RFC_6186 ) {
    var srv = (SrvRecordMailserverService) service;
    // ...
  } else if( service.getConfigurationMethod() == ConfigurationMethod.MOZILLA_AUTOCONFIG ) {
    var autoconfig = (MozillaAutoconfigMailserverService) service;
    // ...
  }
}
```

**SRV records** carry a priority and a weight. Lower priority wins, also across protocols: a domain can publish
IMAP with priority 0 and POP3 with priority 10 to say "prefer IMAP". Equal priorities are balanced by weight. The
socket type is only known for the implicit-TLS labels `_submissions`, `_imaps` and `_pop3s`; for `_submission`,
`_imap` and `_pop3` it is `null` because the record does not tell whether STARTTLS is offered.

**Mozilla Autoconfig** entries may carry a username, a password, one or more authentication methods and OAuth2
issuer details. Values can contain the placeholders `%EMAILADDRESS%`, `%EMAILLOCALPART%` and `%EMAILDOMAIN%`. If
you passed an `EmailAddress` to `discover(...)`, the library replaces all of them. If you passed a domain only,
`%EMAILDOMAIN%` is replaced and the other two remain in the values for you to fill in.

## Scope and limitations

* **Publisher data only.** The library queries the DNS zone and the web server of the domain in question. It does
  not consult Mozilla's ISPDB (`autoconfig.thunderbird.net`), does not fall back to the domain of the MX host, does
  not guess hostnames like `imap.<domain>` and does not read the user's provider database of any mail client.
* **No verification.** No connection is made to the discovered mailservers. Whether a host is reachable, the
  certificate is valid or the authentication method works is up to the caller.
* **Unordered results.** The returned set has no order. Sort by SRV priority and weight yourself and decide how to
  weigh SRV against Autoconfig results; both can be present for the same domain.
* **Partial results on failure.** A failing source (unreachable DNS server, malformed document, HTTP timeout) is
  logged on WARN level and skipped; the results of the other sources are returned. `discover(...)` only throws for
  invalid input (`IllegalArgumentException`).
* **Autoconfig documents are trusted as published.** Elements the library does not understand are ignored, a server
  entry with an invalid port is skipped, and the `<domain>` elements of the document are not checked against the
  requested domain.
* **Not supported:** the DNS TXT `mailconf=` lookup described in
  [Thunderbird:Autoconfiguration:DNSBasedLookup](https://wiki.mozilla.org/Thunderbird:Autoconfiguration:DNSBasedLookup).
  It is a proposal from 2008 that Thunderbird never implemented.

## Security

Discovery results tell a mail client where to send a user's credentials. Treat them as untrusted input.

* **DNS and HTTP are not authenticated.** Without DNSSEC anyone who can tamper with DNS answers can point SRV
  records at their own server, and the Autoconfig hostnames are only as trustworthy as the TLS connection they were
  fetched over. Always validate the mailserver's TLS certificate against the discovered host name, prefer
  implicit-TLS or STARTTLS configurations, and let the user confirm the configuration before credentials are sent.
* **HTTPS only by default.** Autoconfig documents are fetched over HTTPS. Documents from hosts whose certificate is
  invalid or does not cover the requested host name are rejected. This means that domains which point
  `autoconfig.<domain>` to a hosting provider's server via CNAME are often not found, because the provider's
  certificate cannot cover the customer's subdomain. Thunderbird handles these cases with a plain-HTTP fallback.
* **`withInsecureHttpAllowed( true )`** additionally fetches the documents over plain HTTP. An attacker on the path
  can then replace the document, or the redirect target, and thereby the hosts the user will authenticate against.
  Enable it only if you accept that risk, for example for domains you control, and never for credentials you did not
  intend to expose. Redirects from HTTPS to HTTP are never followed.
* **Hardened fetching and parsing.** Only `http` and `https` URLs are fetched, redirects are limited, requests have
  timeouts, only `200` responses are parsed, and the XML parser runs with secure processing, forbids `DOCTYPE`
  declarations (no XXE) and does not expand entities.
* **Passwords in documents.** The Autoconfig format allows a `<password>` element. If a provider publishes one, the
  library returns it verbatim via `getPassword()`. Do not log discovery results without redacting it.

## Roadmap

* **Microsoft Autodiscover v1** (the POX/XML protocol), limited to the HTTP(S) endpoints. The SMTP/IMAP/POP3
  settings it returns fit the model of this library.

Deliberately not planned:

* **Mozilla ISPDB and MX-based fallback.** Both return data published by third parties, not by the domain owner. If
  you need them, query `https://autoconfig.thunderbird.net/v1.1/<domain>` yourself; the document format is the same.
* **Microsoft Autodiscover v2.** The JSON API only returns Exchange Web Services and REST endpoints for Exchange
  Online, not IMAP, POP3 or SMTP settings.
* **DNS TXT `mailconf=`**, see above.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
