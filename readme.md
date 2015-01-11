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

A lot has changed when it comes to handling concurrency. It used to be that we used ACID transactions for changing the datastore. And when we had to update several stores in one action we'd use two phase commit transactions.
Having ACID guarantees and 2PC is nice from the standpoint of the developer; we don't need to think about managing inconsistent outcomes when an exception occurs; that is all taken care of for us.
Though when you [dig deeper into the real world behaviour of datastores](http://www.xaprb.com/blog/2014/12/08/eventual-consistency-simpler-than-mvcc/) the promises of ACID become more nuanced and involved. Similarly for 2PC the status codes of the [JTA XAException] (http://docs.oracle.com/javaee/5/api/javax/transaction/xa/XAException.html) shows that life can be complicated; there's `XA_HEURHAZ` which means "The transaction branch *may* have been heuristically completed."
But the real driver for de emphasising ACID  properties has been that global internet companies needed to scale endlessly. It seems that myspace was one the first companies that ran into a brick wall in spite of using state of the art Oracle RDBMS persistence. Scaling endlessly turned out to amount to scaling horizontally using a shared nothing architecture. 

##Todo
- handle concurrent access to transfers(!)  
