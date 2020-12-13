/**
 * Created by deweesa on 5/12/19.
 */
public class TranscriptItems implements Comparable<TranscriptItems> {
    private String course_id;
    private String title;
    private String semester;
    private int year;
    private String grade;
    private String credits;

    TranscriptItems(String course_id, String title, String semester,
                    int year, String grade, String credits) {
        this.course_id = course_id;
        this.title = title;
        this.semester = semester;
        this.year = year;
        this.grade = grade;
        this.credits = credits;
    }

    public String getCourse_id() {
        return course_id;
    }

    public String getTitle() {
        return title;
    }

    public String getSemester() {
        return semester;
    }

    public int getYear() {
        return year;
    }

    public String getGrade() {
        return grade;
    }

    public String getCredits() {
        return credits;
    }

    public int compareTo(TranscriptItems transcriptItems) {
        int thisSemMod = 0;
        int otherSemMod = 0;

        if(this.semester.equalsIgnoreCase("summer")) thisSemMod = 1;
        if(this.semester.equalsIgnoreCase("fall")) thisSemMod = 2;
        if(this.semester.equalsIgnoreCase("spring")) thisSemMod = 3;

        if(transcriptItems.semester.equalsIgnoreCase("summer")) otherSemMod = 1;
        if(transcriptItems.semester.equalsIgnoreCase("fall")) otherSemMod = 2;
        if(transcriptItems.semester.equalsIgnoreCase("spring")) otherSemMod = 3;

        return (10*this.year+thisSemMod) - (10*transcriptItems.year+otherSemMod);
    }
}
