package cloud.cave.ipc;

import cloud.cave.client.PlayerProxy;
import cloud.cave.domain.Player;
import cloud.cave.domain.Region;
import cloud.cave.server.common.ServerConfiguration;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Created by Soren and Laurits on 22/09/15.
 */
public class MQTopicClientRequestHandler implements ClientRequestHandler{
    private String hostName;
    private String replyQueueName;
    private QueueingConsumer consumer;
    private Connection con;
    private Channel channel;
    private Region region;
    private boolean regionCalled;

    @Override
    public JSONObject sendRequestAndBlockUntilReply(JSONObject requestJson) throws CaveIPCException {

        JSONObject response = null;
        String corrID = UUID.randomUUID().toString();

        //Get the player instance and ask for region if it has not been called
        Player p = PlayerProxy.getInstance();
        if(p != null && regionCalled == false){
            regionCalled = true;
            region = p.getRegion();
        }

        BasicProperties props = new BasicProperties
                .Builder()
                .replyTo(replyQueueName)
                .contentType("application/json")
                .correlationId(corrID)
                .build();

        try {

            if(region != null){
                channel.basicPublish(RabbitMQConfig.RPC_EXCHANGE_NAME, "cave." + region, props, requestJson.toString().getBytes());
            } else {
                channel.basicPublish(RabbitMQConfig.RPC_EXCHANGE_NAME, "cave.login", props, requestJson.toString().getBytes());
            }

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
        region = null;

        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(hostName);
            con = factory.newConnection();
            channel = con.createChannel();

            replyQueueName = channel.queueDeclare().getQueue();

            channel.queueBind(replyQueueName, RabbitMQConfig.RPC_EXCHANGE_NAME, replyQueueName);

            consumer = new QueueingConsumer(channel);
            channel.basicConsume(replyQueueName, true, consumer);



        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
