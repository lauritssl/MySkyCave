cd /root/cave

# === Configure for socket communication on client and app server side
export SKYCAVE_CLIENTREQUESTHANDLER_IMPLEMENTATION=cloud.cave.ipc.MQTopicClientRequestHandler
export SKYCAVE_REACTOR_IMPLEMENTATION=cloud.cave.ipc.MQTopicReactor

# === Configure the toppic for the RabbitMQ setup - MUST be set as first agument
export SKYCAVE_MQ_TOPIC=$1

# === Configure for server to run on localhost
export SKYCAVE_APPSERVER=$MQ0_PORT_5672_TCP_ADDR:5672

# === Inject test doubles for all delegates (Note IP endpoints are dummies)

# = Subscription service
export SKYCAVE_SUBSCRIPTION_IMPLEMENTATION=cloud.cave.service.CircuitBreakerSubscriptionService
export SKYCAVE_SUBSCRIPTIONSERVER=cavereg.baerbak.com:4567

# = Cave storage
export SKYCAVE_CAVESTORAGE_IMPLEMENTATION=cloud.cave.service.MongoStorage
export SKYCAVE_DBSERVER=$DB0_PORT_27017_TCP_ADDR:27017

# = Weather service
export SKYCAVE_WEATHER_IMPLEMENTATION=cloud.cave.service.CircuitBreakerWeatherService
export SKYCAVE_WEATHERSERVER=caveweather.baerbak.com:8182

ant daemon