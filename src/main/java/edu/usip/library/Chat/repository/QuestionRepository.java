package edu.usip.library.Chat.repository;

import edu.usip.library.Chat.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
