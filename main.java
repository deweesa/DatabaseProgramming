public class main {
    public static void main(String[] args)
    {
        String username = args[0];
        String password = args[1];

        JdbcDatabase db = new JdbcDatabase(username, password);
        db.run();
    }
}