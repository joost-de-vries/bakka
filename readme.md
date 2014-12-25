**Compiling
You can access this code from the browser by running `activator ui` .
You can also open the project using IntelliJ. 
To open in Eclipse Scala IDE run `activator eclipse`.

**Code
The domain model is completely immutable. So it is ready for concurrent access. 
The current balance amount is determined by doing a `foldLeft` on the history; i.e. similar to an append data store.