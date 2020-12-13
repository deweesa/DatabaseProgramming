public class main {
    public static void main(String[] args)
    {
        RegistrationSystem db = new RegistrationSystem(args[0], args[1]);
        db.run();
    }
}