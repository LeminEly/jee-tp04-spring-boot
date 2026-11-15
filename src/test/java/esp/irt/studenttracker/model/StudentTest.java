package esp.irt.studenttracker.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StudentTest {

    @Test
    void constructeurInitialiseLesChamps() {
        Student student = new Student("Mario", "Rossi");

        assertThat(student.getFirstName()).isEqualTo("Mario");
        assertThat(student.getLastName()).isEqualTo("Rossi");
    }

    @Test
    void settersModifientLesChamps() {
        Student student = new Student();
        student.setFirstName("Mary");
        student.setLastName("Smith");

        assertThat(student.getFirstName()).isEqualTo("Mary");
        assertThat(student.getLastName()).isEqualTo("Smith");
    }
}
