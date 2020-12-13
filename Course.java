/**
 * Created by deweesa on 4/23/19.
 */
public class Course {
    private String course_id;
    private String section;

    Course(String course_id, String section) {
        this.course_id = course_id;
        this.section = section;
    }

    public String toString() {
        return course_id;
    }

    public String getCourse_id() {
        return course_id;
    }

    public String getSection() {
        return section;
    }
}
