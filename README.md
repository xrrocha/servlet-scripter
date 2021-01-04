### Remote JVM Scripting

This tool enables remote script execution for all JSR-233-compliant JVM scripting languages.

Special support is given to Javascript. This includes EcmaScript6-to-EcmaScript5.1 transpilation 
via [Babel](https://babeljs.io/), as [Nashorn](https://blogs.oracle.com/nashorn/) supports only 
the latter. [Typescript](https://www.typescriptlang.org/) support is also in the works.

THere's also special support for *compilable* languages where scripts and services are compiled 
to JVM bytecode for better performance.

All supported JVM languages are available including [Groovy](http://groovy-lang.org/), 
[Scala](http://www.scala-lang.org/), [Kotlin](https://kotlinlang.org/) and many others. It is 
possible to execute a script or a invoke a service written in different languages. 
