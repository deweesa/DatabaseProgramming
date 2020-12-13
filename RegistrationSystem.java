import java.sql.*;
import java.util.*;

/**
 * Created by deweesa on 4/23/19.
 */
public class RegistrationSystem {
    private String userId;
    private String password;
    private final String preConnectionString = "jdbc:mysql://mysql.cs.wwu.edu:3306/";
    private final String postConnectionString = "?useSSL=false";
    private Scanner reader = new Scanner(System.in);

    RegistrationSystem(String userId, String password)
    {
        this.userId = userId;
        this.password = password;
    }

    public void run()
    {
        System.out.print("Please enter your student ID: ");
        String studentId = reader.nextLine();

        if(!checkStudent(studentId))
        {
            System.out.println("Invalid ID");
            return;
        }

        System.out.println("Valid ID");

        int status = 1;

        while(status != 5)
        {
            System.out.println("What would you like to do?");
            System.out.println("1. Get Transcript");
            System.out.println("2. Check Degree Requirements");
            System.out.println("3. Add Course");
            System.out.println("4. Remove Course");
            System.out.println("5. Exit");

            try {
                status = reader.nextInt();
            } catch (Exception e) {
                status = 0;
                reader.next();
            }

            if(status == 1)
            {
                getTranscript(studentId);
            }
            else if(status == 2)
            {
                checkReq(studentId);
            }
            else if(status == 3) {
                addCourse(studentId);
            }
            else if(status == 4)
            {
                remCourse(studentId);
            }
        }

    }

    private boolean checkStudent(String id)
    {
        try {
            Connection connection = getConnection();
            String sql = "SELECT ID FROM student WHERE ID = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, id);
            ResultSet result = statement.executeQuery();

            return result.next();
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void getTranscript(String sid)
    {
        try {
            Connection connection = getConnection();

            String sql = "Select C.course_id, C.title, T.semester, T.year, T.grade, C.credits "
                       + "From course as C, takes as T "
                       + "where T.ID = ?"
                       + "and T.course_id = C.course_id;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, sid);
            ResultSet result = statement.executeQuery();
            result.beforeFirst();
            ArrayList<TranscriptItems> classes = new ArrayList<>();

            while(result.next()) {
                String cid = result.getString("course_id" );
                String title = result.getString("title" );
                String sem = result.getString("semester" );
                int year = result.getInt("year" );
                String grade = result.getString("grade" );
                String credits = result.getString("credits" );

                classes.add(new TranscriptItems(cid, title, sem, year, grade, credits));
            }

            TranscriptItems[] sortedClasses = new TranscriptItems[classes.size()];
            for(int i = 0; i < classes.size(); i++){
                sortedClasses[i] = classes.get(i);
            }

            Arrays.sort(sortedClasses);

            Object[][] table = new String[classes.size()+1][];
            table[0] = new String[] {"Course ID", "Title", "Semester", "Year", "Grade", "Credits"};
            for(int i = 0; i < classes.size(); i++) {
                TranscriptItems curClass = sortedClasses[i];
                table[i+1] = new String[] {curClass.getCourse_id(), curClass.getTitle(), curClass.getSemester(), curClass.getYear()+"", curClass.getGrade(), curClass.getCredits()};
            }

            String upperBreak = "";
            String lowerBreak = "";

            for(int i = 0; i < 95; i++) {
                upperBreak += "_";
                lowerBreak += "-";
            }

            System.out.println(upperBreak);
            for(Object[] row : table) {
                System.out.format("|%-10s|%-50s|%-9s|%-5s|%-6s|%-8s|\n", row);
            }
            System.out.println(lowerBreak);

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private void checkReq(String sid)
    {
        String major_prefix = getMajorPre(sid);
        try {
            Connection connection = getConnection();
            String sql = "Select course.course_id, course.title "
                       + "from course "
                       + "where course.course_id not in (select takes.course_id "
                                                       +"from takes "
                                                       +"where takes.ID = ?)"
                       + "and course.course_id like '"+ major_prefix +"%';";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, sid);
            ResultSet result = statement.executeQuery();
            result.beforeFirst();
            boolean complete = true;

            ArrayList<String[]> classes = new ArrayList<>();
            classes.add(new String[] {"Course ID", "Title"});
            while(result.next())
            {
                complete = false;
                classes.add(new String[] {result.getString("course_id") ,result.getString("title")});
            }

            if(complete) {
                System.out.println("Requirements met");
                return;
            }

            String upperBreak = "";
            String lowerBreak = "";

            for(int i = 0; i < 63; i++) {
                upperBreak += "_";
                lowerBreak += "-";
            }
            System.out.println(upperBreak);
            for(String[] row : classes) {
                System.out.format("|%-10s|%-50s|\n", row);
            }
            System.out.println(lowerBreak);



        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private void addCourse(String sid)
    {
        String[] semesters = new String[] {"fall", "spring", "summer"};
        System.out.println("Please enter the semester and year you would like to add the course to.");
        System.out.print("Semester: ");
        String semester = reader.next();
        while(!Arrays.asList(semesters).contains(semester.toLowerCase()))
        {
            System.out.println("Invalid semester, please type a valid semester");
            System.out.print("Semester: ");
            semester = reader.next();
        }

        semester = Character.toUpperCase(semester.charAt(0)) + semester.substring(1);

        int year;
        try {
            System.out.print("Year: ");
            year = reader.nextInt();
        } catch (Exception e) {
            year = 0;
        }
        while(year < 1970) {
            reader.next();
            System.out.println("Invalid year, please type a valid year");
            System.out.print("Year: ");
            try {
                year = reader.nextInt();
            } catch (Exception e) {
                year = 0;
            }
        }

        try {
            Connection connection = getConnection();
            String sql = "select distinct section.course_id, section.sec_id\n" +
                    "from (section left join prereq on section.course_id = prereq.course_id) \n" +
                    "\t inner join takes on (prereq.prereq_id = takes.course_id or prereq.prereq_id is null)\n" +
                    "where takes.ID = ?\n" +
                    "  and section.semester = ?\n" +
                    "  and section.year = ?\n" +
                    "  and section.course_id not in (select course_id \n" +
                    "\t\t\t\t\t\t\t\tfrom takes \n" +
                    "\t\t\t\t\t\t\t\twhere ID = ?)\n" +
                    "  and grade not like 'F%'\n" +
                    "  and grade is not null;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, sid);
            statement.setString(2, semester);
            statement.setString(3, Integer.toString(year));
            statement.setString(4, sid);

            ResultSet result = statement.executeQuery();
            result.beforeFirst();
            boolean none_avail = true;
            LinkedList<Course> options = new LinkedList<>();

            while(result.next()) {
                none_avail = false;
                String course_id = result.getString("course_id");
                String section = result.getString("sec_id");

                options.add(new Course(course_id, section));
            }
            if(none_avail) {
                System.out.println("No classes available");
                return;
            }

            Object[][] table = new String[options.size()+1][];
            table[0] = new String[] {"Option", "Course ID", "Section"};
            for(int i = 0; i < options.size(); i++) {
                table[i+1] = new String[] {(i+1)+"", options.get(i).getCourse_id(), options.get(i).getSection() };
            }

            System.out.println("______________________________");
            for(Object[] row : table) {
                System.out.format("|%-8s|%-9s|%-9s|\n", row);
            }
            System.out.println("------------------------------");

            int response;
            try {
                System.out.print("Option: ");
                response = reader.nextInt();
            } catch (Exception e) {
                response = 0;
            }
            while(response < 1 || response > options.size()+1) {
                reader.next();
                System.out.println("Invalid option, please type a valid option");
                System.out.print("Option: ");
                try {
                    response = reader.nextInt();
                } catch (Exception e) {
                    response = 0;
                }
            }

            Course newCourse = options.get(response-1);
            System.out.println("Adding course " + newCourse.getCourse_id());

            sql = "insert into takes values (?, ?, ?, ?, ?, null);";
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, sid);
            statement.setString(2, newCourse.getCourse_id());
            statement.setString(3, newCourse.getSection());
            statement.setString(4, semester);
            statement.setString(5, Integer.toString(year));
            statement.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private void remCourse(String sid)
    {
        try {
            Connection connection = getConnection();
            String sql = "select * from takes where ID = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, sid);
            ResultSet results = statement.executeQuery();
            results.beforeFirst();

            LinkedList<Course> enrolled = new LinkedList<>();
            boolean no_classes = true;

            while(results.next()) {
                no_classes = false;
                String course_id = results.getString("course_id");
                String sec_id = results.getString("sec_id");
                enrolled.add(new Course(course_id, sec_id));
            }
            if(no_classes) {
                System.out.println("You are not currently enrolled in any classes");
                return;
            }

            Object[][] table = new String[enrolled.size()+1][];
            table[0] = new String[] {"Option", "Course ID", "Section"};
            for(int i = 0; i < enrolled.size(); i++) {
                table[i+1] = new String[] {(i+1)+"", enrolled.get(i).getCourse_id(), enrolled.get(i).getSection() };
            }

            System.out.println("______________________________");
            for(Object[] row : table) {
                System.out.format("|%-8s|%-9s|%-9s|\n", row);
            }
            System.out.println("------------------------------");

            int response;
            try {
                System.out.print("Option: ");
                response = reader.nextInt();
            } catch (Exception e) {
                response = 0;
            }
            while(response < 1 || response > enrolled.size()+1) {
                reader.next();
                System.out.println("Invalid option, please type a valid option");
                System.out.print("Option: ");
                try {
                    response = reader.nextInt();
                } catch (Exception e) {
                    response = 0;
                }
            }

            sql = "delete from takes where ID = ? and course_id = ? and sec_id = ?;";
            statement = connection.prepareStatement(sql);

            Course removedCourse = enrolled.get((response-1));
            statement.setString(1, sid);
            statement.setString(2, removedCourse.getCourse_id());
            statement.setString(3, removedCourse.getSection());

            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMajorPre(String sid)
    {
        String major = "";
        try {
            Connection connection = getConnection();
            String sql = "select dept_name from student where ID = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, sid);
            ResultSet result = statement.executeQuery();

            result.first();
            major = result.getString("dept_name");
        } catch(SQLException e) {
            e.printStackTrace();
        }

        if(major.equals("Biology")) return "BIO";
        else if(major.equals("Comp. Sci.")) return "CS";
        else if(major.equals("Elec. Eng")) return "EE";
        else if(major.equals("Finance")) return "FIN";
        else if(major.equals("History")) return "HIS";
        else if(major.equals("Music")) return "MU";
        else if(major.equals("Physics")) return "PHY";
        else return major;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getConnectionString(), userId, password);
    }

    private String getConnectionString() {
        return preConnectionString+userId+postConnectionString;
    }
}
