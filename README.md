# Mailserver Configuration Discovery

A Java library for looking up published mailserver configurations for clients for a given domain.

## Supported discovery methods:

* SRV records ([RFC 6186](https://www.rfc-editor.org/rfc/rfc6186))

Possibly supported soon:

* Mozilla Autoconf
* Microsoft Office Autodiscover v1 (only for http(s) URIs)

Microsoft Office Autodiscover v2 is not expected to be supported in the future.

## Usage

The easiest way to determine mailserver configurations can be done without configuration based on the domain or the whole email address.

```java
List<MailserverService> servicesA = MailserverConfigurationDiscovery.discover( "dummy-domain.com" );
List<MailserverService> servicesB = MailserverConfigurationDiscovery.discover( EmailAddress.of( "user@dummy-domain.com" ) );
```

### Context Configuration

With the help of a context, the query can be configured in many ways. To build a context the `MailserverConfigurationDiscoveryContextBuilder` is used.

Use only RFC 6186 configurations:

```java
var context = new MailserverConfigurationDiscoveryContextBuilder()
    .withConfigurationMethods( ConfigurationMethod.RFC_61186 )
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
  }
}
```

or

```java
var services = MailserverConfigurationDiscovery.discover( "dummy-domain.com" );
for( MailserverService service : services ) {
  if( service.getConfigurationMethod() == ConfigurationMethod.RFC_61186 ) {
    var srvRecordMailserverService = (SrvRecordMailserverService) service;
    // ...
  }
}
```

## Changelog

### 0.0.2

### 0.0.1

* initial release with RFC 6186 support
