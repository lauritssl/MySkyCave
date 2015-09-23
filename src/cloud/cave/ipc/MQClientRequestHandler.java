package cloud.cave.ipc;

import cloud.cave.server.common.ServerConfiguration;
import com.rabbitmq.client.*;
import com.rabbitmq.client.AMQP.BasicProperties;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by Soren and Laurits on 22/09/15.
 */
public class MQClientRequestHandler implements ClientRequestHandler{
    private String hostName;
    private String replyQueueName;
    private QueueingConsumer consumer;
    private Connection con;
    private Channel channel;

    @Override
    public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson) throws CaveIPCException {

        JSONObject response = null;
        String corrID = UUID.randomUUID().toString();

        BasicProperties props = new BasicProperties
                .Builder()
                .replyTo(replyQueueName)
                .contentType("application/json")
                .correlationId(corrID)
                .build();

        try {

            channel.basicPublish("", RabbitMQConfig.RPC_QUEUE_NAME, props, requestJson.toString().getBytes());
            while (true){
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                if(delivery.getProperties().getCorrelationId().equals(corrID)){

                    JSONParser parser = new JSONParser();
                    response = (JSONObject) parser.parse(new String(delivery.getBody(), "UTF-8"));
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public void initialize(ServerConfiguration config) {
        hostName = config.get(0).getHostName();

        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostName);
            con = factory.newConnection();
            channel = con.createChannel();

            replyQueueName = channel.queueDeclare().getQueue();
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(replyQueueName, true, consumer);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
