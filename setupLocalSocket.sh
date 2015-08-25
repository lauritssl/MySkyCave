echo Setting everything for socket based connection on LocalHost with test doubles

# === Configure for socket communication on client and app server side
export SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION=cloud.cave.config.socket.SocketClientRequestHandler
export SKYCAVE_REACTOR_IMPLEMENTATION=cloud.cave.config.socket.SocketReactor

# === Configure for server to run on localhost
export SKYCAVE_APPSERVER=localhost:37123

# === Inject test doubles for all delegates (Note IP endpoints are dummies)

# = Subscription service 
export SKYCAVE_SUBSCRIPTION_IMPLEMENTATION=cloud.cave.doubles.TestStubSubscriptionService
export SKYCAVE_SUBSCRIPTIONSERVER=localhost:42042

# = Cave storage
export SKYCAVE_CAVESTORAGE_IMPLEMENTATION=cloud.cave.doubles.FakeCaveStorage
export SKYCAVE_DBSERVER=localhost:27017

# = Weather service
export SKYCAVE_WEATHER_IMPLEMENTATION=cloud.cave.doubles.TestStubWeatherService
export SKYCAVE_WEATHERSERVER=localhost:8182
