# Telegram Flow

[![Build Status](https://travis-ci.org/galimru/telegramflow.svg?branch=develop)](https://travis-ci.org/galimru/telegramflow)
[![Release](https://jitpack.io/v/galimru/telegramflow.svg)](https://jitpack.io/#galimru/telegramflow)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Use this library to add the state machine to telegram bots. 
The library uses a screen-based approach for interaction with users. 
You just need to describe your screens in XML-way and the flow engine will take care of the rest.

## Installation

Import the library to your project using [jitpack](https://jitpack.io/#galimru/telegramflow/1.0.0) repository 

#### Gradle

  1. Add the JitPack repository to your build file
  
```gradle
repositories {
    ...
    maven { url 'https://jitpack.io' }
}
```

  2. Add the Telegram Flow library dependency

```gradle
implementation 'com.github.galimru:telegramflow:1.0.0'
```

_Note: The JitPack supports both Gradle/Maven build tools, please refer to jitpack [documentation](https://jitpack.io/#galimru/telegramflow) if you want use Maven_

## Limitations

Currently, the library doesn't support SSL termination on callback server. 
It means you will have to use another webserver behind callback server which supports SSL configuration. 
The Telegram API requires to use HTTPS for webhook addresses.

## Links

* [TelegramBots](https://github.com/rubenlagus/TelegramBots) (The java library to create telegram bots)
* [JitPack Repository](https://jitpack.io/#galimru/telegramflow)

## License

This library is released under the terms of the Apache 2.0 license. See License for more information.