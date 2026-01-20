package edu.usip.pdfdocumentmanager.dto.records;

import org.springframework.core.io.Resource;

public record DocumentDownload(Resource resource, String fileName) {}
