# === Configure for socket communication on client and app server side
export SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION=cloud.cave.ipc.MQTopicClientRequestHandler
export SKYCAVE_REACTOR_IMPLEMENTATION=cloud.cave.ipc.MQTopicReactor

# === Configure for server to run on localhost
export SKYCAVE_APPSERVER=ubuntuvm.local:5672

# === Inject test doubles for all delegates (Note IP endpoints are dummies)

# = Subscription service
export SKYCAVE_SUBSCRIPTION_IMPLEMENTATION=cloud.cave.service.CircuitBreakerSubscriptionService
export SKYCAVE_SUBSCRIPTIONSERVER=cavereg.baerbak.com:4567

# = Cave storage
export SKYCAVE_CAVESTORAGE_IMPLEMENTATION=cloud.cave.doubles.FakeCaveStorage
export SKYCAVE_DBSERVER=localhost:27017

# = Weather service
export SKYCAVE_WEATHER_IMPLEMENTATION=cloud.cave.service.CircuitBreakerWeatherService
export SKYCAVE_WEATHERSERVER=caveweather.baerbak.com:8182
