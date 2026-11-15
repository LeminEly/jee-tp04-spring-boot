package esp.irt.studenttracker.rest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StudentErrorResponseTest {

    @Test
    void constructeurCompletInitialiseLesTroisChamps() {
        StudentErrorResponse error = new StudentErrorResponse(404, "introuvable", 1717000000000L);

        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getMessage()).isEqualTo("introuvable");
        assertThat(error.getTimeStamp()).isEqualTo(1717000000000L);
    }

    @Test
    void settersModifientLesTroisChamps() {
        StudentErrorResponse error = new StudentErrorResponse();
        error.setStatus(400);
        error.setMessage("requête invalide");
        error.setTimeStamp(42L);

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getMessage()).isEqualTo("requête invalide");
        assertThat(error.getTimeStamp()).isEqualTo(42L);
    }
}
