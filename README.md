# Mailserver Configuration Discovery

A Java library for looking up published mailserver configurations for clients for a given domain.

## Supported discovery methods:

* SRV records ([RFC 6186](https://www.rfc-editor.org/rfc/rfc6186))
* [Mozilla Autoconfig](https://wiki.mozilla.org/Thunderbird:Autoconfiguration)

Possibly supported soon:

* Microsoft Office Autodiscover v1 (only for http(s) URIs)
* Apple Mail Profile Autoconfig

Microsoft Office Autodiscover v2 is not expected to be supported in the future.

## Usage

The easiest way to determine mailserver configurations can be done without configuration based on the domain or the whole email address.

```java
Set<MailserverService> servicesA = MailserverConfigurationDiscovery.discover( "dummy-domain.com" );
Set<MailserverService> servicesB = MailserverConfigurationDiscovery.discover( EmailAddress.of( "user@dummy-domain.com" ) );
```

### Context Configuration

With the help of a context, the query can be configured in many ways. To build a context the `MailserverConfigurationDiscoveryContextBuilder` is used.

Use only RFC 6186 configurations:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withConfigurationMethods( ConfigurationMethod.RFC_6186 )
    .build();
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com", context );
```

To only look for submission protocol configurations:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withDiscoveryScopes( MailserverConfigurationDiscoveryContext.DiscoveryScope.SUBMISSION )
    .build();
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com", context );
```

A custom DNS resolver can also be configured:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withDnsServer( "9.9.9.9" )
    .withDnsLookupTimeout( Duration.ofSeconds( 50 ) )
    .withDnsLookupRetries( 5 )
    .build();
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com", context );
```

Mozilla Autoconfig documents are fetched over HTTPS only by default. A document fetched over plain HTTP could be tampered with on the wire and point clients to an attacker-controlled mailserver. If you need to support providers that publish their configuration over HTTP only, plain HTTP URLs can be queried in addition to HTTPS ones. The HTTP timeout (connect and overall request timeout) can be adjusted as well:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withInsecureHttpAllowed( true )
    .withHttpTimeout( Duration.ofSeconds( 5 ) )
    .build();
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com", context );
```

The discovery is run as concurrent task. If you want to use a custom Executor, you can overwrite the default one:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withExecutor( new ForkJoinPool( 1 ) )
    .build();
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com", context );
```

### Discovered Mailserver Configurations

Depending on the method used to discover the configurations, they can be cast into their corresponding types.

```java
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com" );
for( MailserverService service : services ) {
  if( service instanceof SrvRecordMailserverService srvRecordMailserverService ) {
    // ...
  } else if( service instanceof MozillaAutoconfigMailserverService mozillaAutoconfigMailserverService ) {
    // ...
  }
}
```

or

```java
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com" );
for( MailserverService service : services ) {
  if( service.getConfigurationMethod() == ConfigurationMethod.RFC_6186 ) {
    var srvRecordMailserverService = (SrvRecordMailserverService) service;
    // ...
  } else if( service.getConfigurationMethod() == ConfigurationMethod.MOZILLA_AUTOCONFIG ) {
    var mozillaAutoconfigMailserverService = (MozillaAutoconfigMailserverService) service;
    // ...
  }
}
```

## Changelog

See [CHANGELOG.md](CHANGELOG.md).
