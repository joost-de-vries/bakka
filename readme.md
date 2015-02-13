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

You can test the http api as follows:  
`curl -X POST http://localhost:8080/account/123/deposit/25` will deposit 25 on account 123  
`curl -X POST http://localhost:8080/account/123/transfer/25/to/234` will transfer 25 from account 123 to account 234  

##Concurrency

##Todo
- add generated test cases that validate invariants during random transactions
- add test for http api
- add test for event sourcing persistence
