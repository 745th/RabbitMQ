import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Conn implements Conn_itf{
    private State state;
    private final static String QUEUE_NAME = "message";
    private ConnectionFactory factory;
    private int ID;
    private boolean p;

    Conn(int t_id)
    {
        ID=t_id;
        state=State.IDLE;
    }
    @Override
    public void init_connection() {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {
                //channel.basicQos(1);
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                String message = "START " + ID;
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                state=State.WAITING;
                System.out.println(message);
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message2 = new String(delivery.getBody(), "UTF-8");
                    System.out.println("hey");
                    Work(delivery,channel,message2);
                };
                channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
            } catch (IOException | TimeoutException e) {
                System.out.println(e);
            }
    }
    @Override
    public void Work(Delivery dev, Channel channel, String message) throws IOException
    {
        String[] command = message.split(" ");
        message = command[0];
        int sID = Integer.parseInt(command[1]);
        if(sID != ID)
        {
            System.out.println("Message consume : "+message);
            switch(message)
            {
                case "START":
                    if(sID<ID)
                    {
                        message="FIRST "+ID;
                    }
                    else
                    {
                        message="PING";
                    }
                    state=State.STARTED;
                    break;
                case "FIRST":
                    state=State.STARTED;
                case "PING":
                    message="PONG "+ID;

                    break;
                case "PONG":
                    message="PING "+ID;

                    break;
            }
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            channel.basicAck(dev.getEnvelope().getDeliveryTag(), false);
        }
        channel.basicAck(dev.getEnvelope().getDeliveryTag(), false);

    }
}
