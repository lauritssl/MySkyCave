package cloud.cave.ipc;

import cloud.cave.server.common.ServerConfiguration;
import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
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
 * Created by lalan on 22/09/15.
 */
public class MQReactor implements Reactor{
    private Logger logger;
    private JSONParser parser;
    private String hostName;
    private Connection con;
    private Channel channel;
    private QueueingConsumer consumer;
    private Invoker invoker;

    @Override
    public void initialize(Invoker invoker, ServerConfiguration config) {
        parser = new JSONParser();
        logger = LoggerFactory.getLogger(MQReactor.class);
        hostName = config.get(0).getHostName();
        this.invoker = invoker;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostName);

        try{
            con = factory.newConnection();
            channel = con.createChannel();


            channel.exchangeDeclare(RabbitMQConfig.RPC_EXCHANGE_NAME, "direct");
            channel.queueDeclare(RabbitMQConfig.RPC_QUEUE_NAME, false, false, false, null);
            channel.queueBind(RabbitMQConfig.RPC_QUEUE_NAME, RabbitMQConfig.RPC_EXCHANGE_NAME, RabbitMQConfig.RPC_QUEUE_NAME);
            channel.basicQos(1);

            consumer = new QueueingConsumer(channel);
            channel.basicConsume(RabbitMQConfig.RPC_QUEUE_NAME, false, consumer);

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
