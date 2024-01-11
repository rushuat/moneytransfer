# moneytransfer
## overview
A straightforward implementation of the money transfer concept in concurrency mode.\
Basic synchronization involves locking two accounts with a trick to prevent deadlock.\
The primary idea is to obtain locks on account IDs in a consistent order.\
The simple rule is that the first ID should always be smaller than the second ID.

*Notice:* This approach is not recommended for production services.\
For real systems it is important to keep all transactions in a log.\
And use a solution something similar to the two-phase commit mechanism.

## build
gradlew clean build
## run
java -jar moneytransfer-0.0.1-SNAPSHOT.jar
