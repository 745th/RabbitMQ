import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

import java.io.IOException;

public interface Conn_itf {

    void init_connection();
    void Work(Delivery dev, Channel channel, String message) throws IOException;
}
