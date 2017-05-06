# Java JSTP implementation

[JSTP documentation](https://github.com/metarhia/JSTP)

[Check for the latest JSTP version](https://bintray.com/metarhia/maven/jstp)

## Installation
Gradle:

Add this to your build.gradle (check for the latest version):
```
dependencies {
  compile 'com.metarhia.jstp:jstp:0.7.0'
}
```

Maven:
```
<dependency>
  <groupId>com.metarhia.jstp</groupId>
  <artifactId>jstp</artifactId>
  <version>0.7.0</version>
  <type>pom</type>
</dependency>
```

## Parser usage

There are 2 parsers available:
1) To native java objects with `JSNativeParser`

Native parser gives you java Objects directly, which is more convenient.

Few simple examples
```java
Map<String, Number> a = (Map<String, Number>) new JSNativeParser("{a: 3}").parse();
a.get("a"); // returns 3.0

List<Number> arr = (List<Number>) new JSNativeParser("[1, 2, 3]").parse();
arr.get(1); // returns 2.0
```

To serialize objects you can use `JSNativeSerializer`
```java
List<Number> arr = (List<Number>) new JSNativeParser("[1, 2, 3]").parse();
JSNativeSerializer.stringify(arr); // returns "[1,2,3]"
```
If it doesn't know how to serialize input it'll call `toString()` on it and add to the result
(TODO: with recent PR it'll replace it with `undefined`)

2) To simple js mirrored hierarchy in java with `JSParser` (hierarchy has `JSValue` as superclass)

In current JSTP SDK implementation, you can use:
* JSObject
* JSArray
* JSNumber
* JSString
* JSBool
* JSNull
* JSUndefined

To parse values, you can use JSParser directly:

```java
try {
  JSParser parser = new JSParser();
  JSValue value = parser.parse("ANY JS VALUE");
  JSObject obj = parser.parseJSObject("YOUR OBJECT VALUE");
  JSArray array = parser.parseJSArray("YOUR ARRAY VALUE");
} catch (JSParsingException e) {
  // error handling goes here
}
```

You also can use it like that:
```java
try {
  JSValue value = JSTP.parse("YOUR OBJECT VALUE");
} catch (JSParsingException e) {
  //...
}
```

Get field 'a' of object `{a: 3}`;
```java
try {
  JSObject obj = JSTP.parse("{a : 3}");
  obj.get("a"); // returns 3
} catch (JSParsingException e) {
  // error handling goes here
}
```

Get second element of array `[1, 2, 3]`;
```java
try {
  JSArray arr = (JSArray) new JSParser("[1, 2, 3]").parse();
  arr.get(1); // returns 2
} catch (JSParsingException e) {
  // error handling goes here
}
```

To convert values from js java hierarchy to js use `.toString()` method or `JSTP.stringify`.
They can be parsed in js with a simple eval statement or [js parser](https://github.com/metarhia/JSTP)
```java
JSValue value;
//...
String serializedValue = JSTP.stringify(value);
```

## JSTPConnection

### Establish connection

To establish JSTP connection, you need to provide transport.
As of now the only available transport is TCP. Optionally you can
define restoration policy (there are 2 basic ones in SDK:
`DropRestorationPolicy` - which will create new connection every
time transport is restored and `SessionRestorationPolicy` - which
will resend cached packets and try to restore session.
`SessionRestorationPolicy` is used by default). For example:

```java
String host = "metarhia.com";
int port = 80;
boolean usesSSL = true;

AbstractSocket transport = new TCPTransport(host, port, usesSSL);
JSTPConnection connection = new JSTPConnection(transport);
```

You can change used transport by calling `useTransport()` method.
This will close previous transport if available and set provided one
as current transport. If transport has already been connected at that
time you will have to send `handshake` manually. Otherwise appropriate
method of restoration policy will be called when transport reports
that it's connected.

To react to connection events, you can use `JSTPConnectionListener`:

```java
connection.addSocketListener(new JSTPConnectionListener() {
  @Override
  public void onConnected(boolean restored) {
    // ...
  }

  @Override
  public void onPacketRejected(JSObject packet) {
    // ...
  }

  @Override
  public void onConnectionError(int errorCode) {
    // ...
  }

  @Override
  public void onConnectionClosed() {
    // ...
  }
});
```

You can define applicationName and/or session Id when connecting,
or connect without them (you must at least once call `connect`
with application name before that):
```java
connection.connect();
// ...
connection.connect("applicationName");
// ...
connection.connect("applicationName", "sessionId");
```

### JSTP packet types

#### Handshake

Usually you don't have to send handshake packets manually. You may need
them if If you need to implement your own restoration policy or change
transport on active connection. You can send `handshake` packet as follows:

```java
// anonymous handshake message
connection.handshake("applicationName", new ManualHandler() {
  @Override
  public void invoke(JSValue packet) {
    // ...
  }
});

// handshake with attempt to restore session
connection.handshake("applicationName", "sessionId", new ManualHandler() {
  @Override
  public void invoke(JSValue packet) {
    // ...
  }
});

// handshake message with authorization
connection.handshake("applicationName", "username", "password", new ManualHandler() {
  @Override
  public void invoke(JSValue packet) {
    // ...
  }
});

```

#### Call

To send `call` message:

```java
JSArray args = new JSArray();
// ...
connection.call("interfaceName", "methodName", args, new ManualHandler() {
  @Override
  public void invoke(final JSValue value) {
    // ...
  }
);
```

To handle incoming `call` packets, you have to `setCallHandler()` for that call.
There can only be one call handler for each call.

#### Callback

While sending callback you should specify callback type (`JSCallback.OK` or
`JSCallback.ERROR`) and arbitrary arguments.

```java
connection.setCallHandler("interfaceName", "methodName", new CallHandler() {
  @Override
  public void handleCallback(JSArray data) {
    JSArray args = new JSArray();
    // ...
    callback(connection, JSCallback.OK, args);
  }
});
```

You also can send `callback` packets like this:

```java
connection.callback(JSCallback.OK, args);

// define custom packet index
Long customIndex;
// ...
connection.callback(JSCallback.OK, args, customIndex);

```

#### Inspect

Incoming inspect packets are handled by JSTPConnection itself. To make
methods visible through inspect message you just need to define method
names with appropriate interfaces.

```java
connection.setClientMethodNames("interfaceName1", "methodName1", "methodName2");
connection.setClientMethodNames("interfaceName2", "methodName1", "methodName2");
// ...
```

To send `inspect` packet:
```java
connection.inspect("interfaceName", new ManualHandler() {
  @Override
  public void invoke(JSValue packet) {
    // ...
  }
});
```

#### Event

To handle incoming events, you add event handlers with `addEventHandler()`.
There can be multiple event handlers for each event.

```java
connection.addEventHandler("interfaceName", "methodName", new ManualHandler() {
  @Override
  public void invoke(JSValue packet) {
    // ...
  }
});
```

Sending `event` packet:
```java
JSArray args = new JSArray();
// ...
connection.event("interfaceName", "methodName", args);
```
