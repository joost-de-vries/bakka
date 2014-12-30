*
**Compiling
You can access this code from the browser by running `activator ui` .
You can also open the project using IntelliJ. 
To open in Eclipse Scala IDE run `activator eclipse`.
You can compile from the commandline using `activator compile` or `sbt compile`

**Code
The domain model is immutable. So it is suitable for concurrent access.
Validations and other domain logic are implemented independent of concurrency management (i.e. actors).
The current balance amount is determined by doing a `foldLeft` on the history; i.e. similar to an append data store.