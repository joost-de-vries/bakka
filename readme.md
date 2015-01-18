#Banking with akka
A playground for exploring different concurrency choices in a banking example
##Compiling
You can access this code from the browser by running `activator ui`.  
You can also open the project using IntelliJ Community Edition + Scala Plugin.  
To open in Eclipse Scala IDE run `activator eclipse` and import.  
You can compile from the commandline using `activator compile` or `sbt compile`.  

##Code
The domain model is immutable. So it is suitable for concurrent access.
Validations and other domain logic are implemented in immutable objects and functions in the domain model and are independent of actors. Actors handle only concurrent access.

The current balance amount is determined by doing a `foldRight` on the history; i.e. similar to an append data store that replays events to get to the current state. `foldRight` performs better on a right branching `List[Transaction]`. While `foldLeft` starts with the oldest `Transaction` and as a result respects validation constraints for intermediate results. 

##Concurrency

##Todo
- add generated test cases that validate invariants during random transactions
- add http api
- add event sourcing persistence
