# ratpack-moneytransfer
Simple money transfer rest service with ratpack and rest-assured

Main goal was safety: thread safety, no negative money amount, no neggative money transfers and no decimal rounding errors. All those is tested with integration tests.

## Build

`./gradlew build`

## Run

`./gradlew run`

## Run tests

`./gradlew test`
