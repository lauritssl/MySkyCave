package cloud.cave.ipc;

import cloud.cave.config.Config;
import cloud.cave.server.common.ServerConfiguration;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

/**
 * Created by Soren and Laurits on 22/09/15.
 */
public class MQTopicReactor implements Reactor{
    private Logger logger;
    private JSONParser parser;
    private String hostName;
    private Connection con;
    private Channel channel;
    private QueueingConsumer consumer;
    private Invoker invoker;

    /**
     * Will initialize the reactor to listen to the following queues on the RabbitMQ server
     *
     * cave.login - the login is not region specific, thus all regions handle these requests
     * cave.(REGION) - looks for the region in the queue SKYCAVE_(REGION) OBS. can also be set to login for the login server
     */
    @Override
    public void initialize(Invoker invoker, ServerConfiguration config) {
        parser = new JSONParser();
        logger = LoggerFactory.getLogger(MQTopicReactor.class);
        hostName = config.get(0).getHostName();
        this.invoker = invoker;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);

        try{
            con = factory.newConnection();
            channel = con.createChannel();


            channel.exchangeDeclare(RabbitMQConfig.RPC_EXCHANGE_NAME, "topic");

            //Getting topic from environment variable, needs to be set in the start script!
            String topic = System.getenv("SKYCAVE_MQ_TOPIC");
            String queue = "SKYCAVE_" + topic;

            channel.queueDeclare(queue, false, false, false, null);
            channel.queueBind(queue, RabbitMQConfig.RPC_EXCHANGE_NAME, "cave." + topic);

            channel.basicQos(1);

            consumer = new QueueingConsumer(channel);
            channel.basicConsume(queue, false, consumer);

            System.out.println("*** RabbitMQ connection established ***");
            logger.info("*** RabbitMQ connection established ***");


        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void run() {

        while(true){
            System.out.println("--> Accepting...");

            JSONObject response = null;
            JSONObject reply = null;

            QueueingConsumer.Delivery delivery = null;
            try {
                delivery = consumer.nextDelivery();


                BasicProperties prop = delivery.getProperties();
                BasicProperties replayProbs = new BasicProperties
                                                .Builder()
                                                .correlationId(prop.getCorrelationId())
                                                .contentType("application/json")
                                                .build();

                try {
                    response = (JSONObject) parser.parse(new String(delivery.getBody(), "UTF-8"));
                    System.out.println("--> AcceptED!");
                    LocalDateTime dateTime = LocalDateTime.now(); //Timestamp updated after reply
                    System.out.println("["+ dateTime.toString() +"] --> Received " + response.toString()); //Timestamp added to output

                    reply = invoker.handleRequest(response);

                }catch (ParseException e) {
                    String errorMsg = "JSON Parse error on input = " + response.toString();
                    logger.error(errorMsg, e);
                    reply = Marshaling.createInvalidReplyWithExplantion(
                            StatusCode.SERVER_FAILURE, errorMsg);
                    System.out.println("--< !!! replied: "+reply);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }catch (Exception e){
                    String errorMsg = "Unknown exception on input = " + response.toString();
                    logger.error(errorMsg, e);
                    reply = Marshaling.createInvalidReplyWithExplantion(
                            StatusCode.SERVER_FAILURE, errorMsg);
                }finally {
                    LocalDateTime dateTime = LocalDateTime.now(); //Timestamp updated after reply
                    System.out.println("[" + dateTime.toString() + "] --< replied: " + reply); //Timestamp added to output

                    channel.basicPublish(RabbitMQConfig.RPC_EXCHANGE_NAME, prop.getReplyTo(), replayProbs, reply.toString().getBytes("UTF-8"));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }

            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }

    }
}
