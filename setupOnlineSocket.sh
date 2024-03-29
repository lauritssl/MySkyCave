echo Setting everything for socket based connection on LocalHost with test doubles

# === Configure for socket communication on client and app server side
export SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION=cloud.cave.config.socket.SocketClientRequestHandler
export SKYCAVE_REACTOR_IMPLEMENTATION=cloud.cave.config.socket.SocketReactor

# === Configure for server to run on localhost
export SKYCAVE_APPSERVER=46.101.43.174:37123

# === Inject test doubles for all delegates (Note IP endpoints are dummies)

# = Subscription service 
export SKYCAVE_SUBSCRIPTION_IMPLEMENTATION=cloud.cave.service.StandardSubscriptionService
export SKYCAVE_SUBSCRIPTIONSERVER=cavereg.baerbak.com:4567

# = Cave storage
export SKYCAVE_CAVESTORAGE_IMPLEMENTATION=cloud.cave.doubles.FakeCaveStorage
export SKYCAVE_DBSERVER=46.101.43.174:27017

# = Weather service
export SKYCAVE_WEATHER_IMPLEMENTATION=cloud.cave.service.StandardWeatherService
export SKYCAVE_WEATHERSERVER=caveweather.baerbak.com:8182
