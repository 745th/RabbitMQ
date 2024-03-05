import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Conn implements Conn_itf{
    private State state;
    private final static String QUEUE_NAME = "message";
    private ConnectionFactory factory;
    private long ID;
    private boolean p;

    private DefaultConsumer Handler;

    Conn(long t_id)
    {
        ID=t_id;
        state=State.IDLE;
    }
    @Override
    public void init_connection()  throws IOException, TimeoutException{
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(5);
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        String message = "START " + ID;
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        state=State.WAITING;
        System.out.println(message);
        /*DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message2 = new String(delivery.getBody(), "UTF-8");
            System.out.println("hey");
            Work(delivery,channel,message2);
        };*/
        Handler=new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException
            {
                String message2 = new String(body, "UTF-8");
                System.out.println("New message !");
                Work(envelope.getDeliveryTag(),channel,message2);
            }
        };
        channel.basicConsume(QUEUE_NAME, false, Handler);
     }
    @Override
    public void Work(long deliveryTag, Channel channel, String message) throws IOException
    {
        String[] command = message.split(" ");
        message = command[0];
        long sID = Long.parseLong(command[1]);
        if(sID != ID)
        {
            System.out.println("Message consume : "+message);
            switch(message)
            {
                case "START":
                    if(state != State.STARTED)
                    {
                        if(sID<ID)
                        {
                            message="FIRST "+ID;
                        }
                        else
                        {
                            message="PING "+ID;
                        }
                        state=State.STARTED;
                        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                    }
                    break;
                case "FIRST":
                    state=State.STARTED;
                case "PING":
                    message="PONG "+ID;
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                    break;
                case "PONG":
                    message="PING "+ID;
                    channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                    break;
            }

            channel.basicAck(deliveryTag, false);
        }
        else
        {
            System.out.println("Message ignored : "+message);
            channel.basicReject(deliveryTag, true);
        }
    }
}
