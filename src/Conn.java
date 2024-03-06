import com.rabbitmq.client.*;

import java.io.IOException;
import java.io.PrintStream;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Conn implements Conn_itf{
    private State state;
    private String QUEUE_PUBLISH;
    private String QUEUE_LISTENING;
    private ConnectionFactory factory;
    Channel channelPublish;
    Channel channelListen;
    private long ID;
    private boolean p;

    private DefaultConsumer Handler;

    Conn(long t_id)
    {
        ID=t_id;
        state=State.IDLE;
        QUEUE_LISTENING = "B";
        QUEUE_PUBLISH= "A";

    }
    @Override
    public void init_connection()  throws IOException, TimeoutException{
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        channelPublish = connection.createChannel();
        channelListen = connection.createChannel();
        channelPublish.queueDeclare(QUEUE_PUBLISH, false, false, false, null);
        channelListen.queueDeclare(QUEUE_LISTENING, false, false, false, null);
        QUEUE_LISTENING="A";
        String message = "START " + ID;
        channelListen.basicQos(1);
        System.out.println(message);
        /*DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message2 = new String(delivery.getBody(), "UTF-8");
            System.out.println("hey");
            Work(delivery,channel,message2);
        };*/
        Handler=new DefaultConsumer(channelListen) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException
            {
                String message2 = new String(body, "UTF-8");
                System.out.println("New message !");
                try {
                    System.out.println("CT :"+consumerTag);
                    Work(consumerTag,envelope.getDeliveryTag(), message2);
                }
                catch(TimeoutException e)
                {
                    System.out.println(e);
                }
            }
        };
        channelListen.basicConsume(QUEUE_LISTENING, false, Handler);
        channelPublish.basicPublish("", QUEUE_PUBLISH,null, message.getBytes());
        channelPublish.basicPublish("", QUEUE_PUBLISH,null, message.getBytes());
     }
    @Override
    public void Work(String consumer,long deliveryTag, String message) throws IOException, TimeoutException
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
                            //it send to A and receive from B
                            message="FIRST "+ID;
                            QUEUE_PUBLISH="A";
                            QUEUE_LISTENING="B";
                            channelListen.basicCancel(consumer);
                            channelListen.basicConsume(QUEUE_LISTENING, false, Handler);
                        }
                        else
                        {
                            //it send to B and receive from A
                            message="SECOND "+ID;
                            QUEUE_PUBLISH="B";
                            QUEUE_LISTENING="A";
                            channelListen.basicCancel(consumer);
                            channelListen.basicConsume(QUEUE_LISTENING, false, Handler);
                        }
                        state=State.WAITING;
                        channelPublish.basicPublish("", "A", null, message.getBytes());
                        channelPublish.basicPublish("", "B", null, message.getBytes());
                    }
                    break;
                case "SECOND":
                    state=State.STARTED;
                    QUEUE_LISTENING="B";
                    channelListen.basicCancel(consumer);
                    channelListen.basicConsume(QUEUE_LISTENING, false, Handler);
                    QUEUE_PUBLISH="A";
                case "FIRST":
                    state=State.STARTED;
                    QUEUE_LISTENING="A";
                    channelListen.basicCancel(consumer);
                    channelListen.basicConsume(QUEUE_LISTENING, false, Handler);
                    QUEUE_PUBLISH="B";

                case "PING":
                    message="PONG "+ID;
                    channelPublish.basicPublish("", QUEUE_PUBLISH, null, message.getBytes());
                    break;
                case "PONG":
                    message="PING "+ID;
                    channelPublish.basicPublish("", QUEUE_PUBLISH, null, message.getBytes());
                    break;
            }

            channelListen.basicAck(deliveryTag, false);
        }
        else
        {
            switch(state)
            {
                case State.IDLE:
                    if(!(QUEUE_LISTENING.equals("B"))) {
                        //only if it's alone in the channel A
                        //accept the first start
                        channelListen.basicCancel(consumer);
                        QUEUE_LISTENING="B";
                        channelListen.basicConsume(QUEUE_LISTENING, false, Handler);
                        state=State.WAITING;
                        channelListen.basicAck(deliveryTag, false);
                    }
                    break;
                case State.STARTED:
                    System.out.println("Message ignored : " + message);
                    channelListen.basicReject(deliveryTag, true);
                    break;

                default:
                    channelListen.basicAck(deliveryTag, false);
                    break;
            }


        }
    }
}
