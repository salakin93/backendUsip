package edu.usip.pdfdocumentmanager.repository;

import edu.usip.pdfdocumentmanager.model.ChatPdfQuestionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChatPdfQuestionLogRepository
        extends JpaRepository<ChatPdfQuestionLog, Long>, JpaSpecificationExecutor<ChatPdfQuestionLog> {
}
