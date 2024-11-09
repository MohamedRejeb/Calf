# Calf - Compose Adaptive Look & Feel

Calf is a library that allows you to easily create adaptive UIs and access platform specific APIs from your Compose Multiplatform apps.

[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.7.0-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
[![MohamedRejeb](https://raw.githubusercontent.com/MohamedRejeb/MohamedRejeb/main/badges/mohamedrejeb.svg)](https://github.com/MohamedRejeb)
[![Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![BuildPassing](https://shields.io/badge/build-passing-brightgreen)](https://github.com/MohamedRejeb/ksoup/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-ui%22)

![Calf thumbnail](docs/images/thumbnail.png)

Calf stands for **C**ompose **A**daptive **L**ook & **F**eel

## Artifacts

| Artifact              | Description                               | Platforms                            | Version                                                                                                                                                                                           |
|-----------------------|-------------------------------------------|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **calf-ui**           | Adaptive UI components                    | Android, iOS, Desktop, Web(Js, Wasm) | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-ui%22)                   |
| **calf-file-picker**  | Native File Picker wrapper                | Android, iOS, Desktop, Web(Js, Wasm) | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-file-picker)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-file-picker%22) |
| **calf-webview**      | WebView component                         | Android, iOS, Desktop                | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-webview)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-webview%22)         |
| **calf-permissions**  | API that allows you to handle permissions | Android, iOS                         | [![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-file-picker)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-file-picker%22) |                                                                                                                                                                        
| **calf-geo**          | API that allows you to access geolocation | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |
| **calf-navigation**   | Native navigation wrapper                 | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |
| **calf-map**          | Native Maps wrapper                       | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |
| **calf-media**        | Video/Audio player                        | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |
| **calf-notification** | Notification manager                      | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |
| **calf-sf-symbols**   | Apple SF Symbols icons                    | Coming soon... 🚧 🚧                 | Coming soon... 🚧 🚧                                                                                                                                                                              |

> The main focus for now is Android and iOS, but more Desktop components are coming that allows you to create adaptive UIs for Desktop as well (Windows, macOS, Linux)

## Web Demo

You can try the web demo [here](https://calf-library.netlify.app/)

## Compatibility

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.calf/calf-ui)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.calf%22%20AND%20a:%calf-ui%22)

| Kotlin version | Compose version | Calf version |
|----------------|-----------------|--------------|
| 2.0.21         | 1.7.0           | 0.6.0        |
| 2.0.10         | 1.6.11          | 0.5.5        |
| 1.9.22         | 1.6.0           | 0.4.1        |
| 1.9.21         | 1.5.11          | 0.3.1        |
| 1.9.20         | 1.5.10          | 0.2.0        |
| 1.9.0          | 1.5.0           | 0.1.1        |

## Documentation

For more information, check the documentation:

- [Getting Started](https://mohamedrejeb.github.io/Calf/installation/)
- [Adaptive UI](https://mohamedrejeb.github.io/Calf/ui/)
- [WebView](https://mohamedrejeb.github.io/Calf/webview/)
- [File Picker](https://mohamedrejeb.github.io/Calf/filepicker/)
- [Permissions](https://mohamedrejeb.github.io/Calf/permissions/)

## Contribution
If you've found an error in this sample, please file an issue. <br>
Feel free to help out by sending a pull request :heart:.

[Code of Conduct](https://github.com/MohamedRejeb/Calf/blob/main/CODE_OF_CONDUCT.md)

## Find this library useful? :heart:
Support it by joining __[stargazers](https://github.com/MohamedRejeb/Calf/stargazers)__ for this repository. :star: <br>
Also, __[follow me](https://github.com/MohamedRejeb)__ on GitHub for more libraries! 🤩

You can always <a href="https://www.buymeacoffee.com/MohamedRejeb"><img src="https://img.buymeacoffee.com/button-api/?text=Buy me a coffee&emoji=&slug=MohamedRejeb&button_colour=FFDD00&font_colour=000000&font_family=Cookie&outline_colour=000000&coffee_colour=ffffff"></a>

# License
```
Copyright 2023 Mohamed Rejeb

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
