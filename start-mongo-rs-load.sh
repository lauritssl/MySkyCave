cd /root/cave

# === Configure for socket communication on client and app server side
export SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION=cloud.cave.config.socket.SocketClientRequestHandler
export SKYCAVE_REACTOR_IMPLEMENTATION=cloud.cave.config.socket.SocketReactor

# === Configure for server to run on localhost
export SKYCAVE_APPSERVER=localhost:37123

# === Inject test doubles for all delegates (Note IP endpoints are dummies)

# = Subscription service
export SKYCAVE_SUBSCRIPTION_IMPLEMENTATION=cloud.cave.service.CircuitBreakerSubscriptionService
export SKYCAVE_SUBSCRIPTIONSERVER=cavereg.baerbak.com:4567

# = Cave storage
export SKYCAVE_CAVESTORAGE_IMPLEMENTATION=cloud.cave.service.MongoStorage
export SKYCAVE_DBSERVER=$RS_PORT_27017_TCP_ADDR:27017,$RS_PORT_27017_TCP_ADDR:27018,$RS_PORT_27017_TCP_ADDR:27019

# = Weather service
export SKYCAVE_WEATHER_IMPLEMENTATION=cloud.cave.service.CircuitBreakerWeatherService
export SKYCAVE_WEATHERSERVER=caveweather.baerbak.com:8182

ant load.mongo