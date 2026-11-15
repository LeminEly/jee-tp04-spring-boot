package esp.irt.studenttracker.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

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
}
