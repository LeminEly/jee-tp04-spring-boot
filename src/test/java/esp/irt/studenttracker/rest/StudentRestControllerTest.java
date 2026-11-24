package esp.irt.studenttracker.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@WebMvcTest(StudentRestController.class)
class StudentRestControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private StudentRestController controller;

    @Test
    void laListeEstConstruiteUneSeuleFoisParLeConteneur() {
        // @PostConstruct a été exécutée par Spring : la liste existe déjà.
        assertThat(controller.getStudents()).hasSize(3);
        // Et elle n'est pas recréée d'un appel à l'autre.
        assertThat(controller.getStudents()).isSameAs(controller.getStudents());
    }

    // Exercice 5.7 : la gestion d'erreur vit dans le @ControllerAdvice, plus dans le contrôleur.
    @Test
    void leControleurNeContientPlusDeHandlerLocal() {
        assertThat(StudentRestController.class.getDeclaredMethods())
                .noneMatch(m -> m.isAnnotationPresent(ExceptionHandler.class));
        assertThat(StudentRestExceptionHandler.class.isAnnotationPresent(ControllerAdvice.class))
                .isTrue();
    }

    @Test
    void helloRenvoieHelloWorldEnTexte() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Hello World"));
    }

    @Test
    void helloSansPrefixeApiNExistePas() throws Exception {
        mockMvc.perform(get("/hello")).andExpect(status().isNotFound());
    }

    @Test
    void studentsRenvoieUnTableauJsonDesTroisEtudiants() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(
                        content()
                                .json(
                                        """
                                        [
                                          {"firstName":"Poornima","lastName":"Patel"},
                                          {"firstName":"Mario","lastName":"Rossi"},
                                          {"firstName":"Mary","lastName":"Smith"}
                                        ]
                                        """,
                                        JsonCompareMode.STRICT));
    }

    @Test
    void studentParIndexRenvoieLePremierEtudiant() throws Exception {
        mockMvc.perform(get("/api/students/0"))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .json(
                                        "{\"firstName\":\"Poornima\",\"lastName\":\"Patel\"}",
                                        JsonCompareMode.STRICT));
    }

    // 3 est la première valeur hors bornes (taille 3), -1 la borne inférieure.
    @ParameterizedTest
    @ValueSource(ints = {999, 3, -1})
    void indexHorsBornesRenvoie404Structure(int studentId) throws Exception {
        long avant = System.currentTimeMillis();
        mockMvc.perform(get("/api/students/" + studentId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Student id not found - " + studentId))
                .andExpect(jsonPath("$.timeStamp").value(entre(avant)))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    // Le catch-all global ne doit pas transformer une URL inconnue en 400.
    @ParameterizedTest
    @ValueSource(strings = {"/hello", "/api/inconnu"})
    void urlInconnueRenvoie404Structure(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // Catch-all : un id non numérique échoue à la conversion en int.
    @Test
    void idNonNumeriqueRenvoie400Structure() throws Exception {
        long avant = System.currentTimeMillis();
        mockMvc.perform(get("/api/students/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timeStamp").value(entre(avant)))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    /** L'horodatage est celui de la requête : entre le début du test et maintenant. */
    private static Matcher<Long> entre(long avant) {
        return Matchers.both(Matchers.greaterThanOrEqualTo(avant))
                .and(Matchers.lessThanOrEqualTo(System.currentTimeMillis() + 1000));
    }

    @Test
    void studentParIndexRenvoieLeDernierEtudiant() throws Exception {
        mockMvc.perform(get("/api/students/2"))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .json(
                                        "{\"firstName\":\"Mary\",\"lastName\":\"Smith\"}",
                                        JsonCompareMode.STRICT));
    }
}
