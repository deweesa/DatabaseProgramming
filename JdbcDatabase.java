import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by deweesa on 4/23/19.
 */
public class JdbcDatabase {
    private String userId;
    private String password;
    private final String preConnectionString = "jdbc:mysql://mysql.cs.wwu.edu:3306/";
    private final String postConnectionString = "?useSSL=false";

    JdbcDatabase(String userId, String password)
    {
        this.userId = userId;
        this.password = password;
    }

    public void run()
    {
        try {
            Connection connection = getConnection();

            Statement query = connection.createStatement();
            ResultSet result = query.executeQuery("SELECT * FROM student;");
            List<Student> students = new ArrayList<>();

            while(result.next()) {
                
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getConnectionString(), userId, password);
    }

    private String getConnectionString() {
        return preConnectionString+userId+postConnectionString;
    }
}
