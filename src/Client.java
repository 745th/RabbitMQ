public class Client implements Client_itf{

    private Conn Connexion;
    Client(int id)
    {
        Connexion = new Conn(id);
    }
    private Conn connect;
    @Override
    public void StartConnection() {

    }
}
