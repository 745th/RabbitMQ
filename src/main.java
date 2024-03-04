public class main {
    public static void main(String[] args) {

        int id=0;
        System.out.println("Start Client ID :"+id);
        Conn_itf Connexion = new Conn(id);
        Connexion.init_connection();
    }
}
