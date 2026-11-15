package esp.irt.studenttracker.rest;

import esp.irt.studenttracker.model.Student;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StudentRestController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World";
    }

    // Première version, volontairement imparfaite (exercice 5.2) : la liste est
    // reconstruite à chaque appel. Corrigé à l'exercice 5.3 par @PostConstruct.
    @GetMapping("/students")
    public List<Student> getStudents() {
        List<Student> theStudents = new ArrayList<>();
        theStudents.add(new Student("Poornima", "Patel"));
        theStudents.add(new Student("Mario", "Rossi"));
        theStudents.add(new Student("Mary", "Smith"));
        return theStudents;
    }
}
