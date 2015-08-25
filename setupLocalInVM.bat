echo Setting everything for InVM IPC (no server) with test doubles

REM === Configure for In memory IPC for the client; server require no config
set SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION=cloud.cave.doubles.AllTestDoubleClientRequestHandler

REM === Dummy config of the server IP endpoint
set SKYCAVE_APPSERVER=localhost:37123

REM === Inject test doubles for all delegates (Note IP endpoints are dummies)

REM = Subscription service 
set SKYCAVE_SUBSCRIPTION_IMPLEMENTATION=cloud.cave.doubles.TestStubSubscriptionService
set SKYCAVE_SUBSCRIPTIONSERVER=localhost:42042

REM = Cave storage
set SKYCAVE_CAVESTORAGE_IMPLEMENTATION=cloud.cave.doubles.FakeCaveStorage
set SKYCAVE_DBSERVER=localhost:27017

REM = Weather service
set SKYCAVE_WEATHER_IMPLEMENTATION=cloud.cave.doubles.TestStubWeatherService
set SKYCAVE_WEATHERSERVER=localhost:8182