import java.io.IOException;
import java.sql.Time;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class main {
    public static void main(String[] args) throws IOException, TimeoutException {


        long id = new Date().getTime();

        System.out.println("Start Client ID :"+id);
        Conn_itf Connexion = new Conn(id);
        Connexion.init_connection();
    }
}
