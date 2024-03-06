import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Conn_itf {

    void init_connection() throws IOException, TimeoutException;
    void Work(String consumer,long deliveryTag, String message) throws IOException,TimeoutException;
}
