package com.example.alva.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.alva.processors.AsyncVisitorExecutionService;
import com.example.alva.storage.ProcessStorage;
import com.example.alva.storage.ResultStorage;
import com.example.alva.storage.VisitorProcess;
import com.example.alva.storage.VisitorResult;

@RestController
@RequestMapping("/api/v1")
public class RestAPIController {

    private static final int MAX_RECURSION = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAPIController.class);
    private final AsyncVisitorExecutionService executionService;
    private final ProcessStorage processStorage;
    private final ResultStorage resultStorage;

    @Autowired
    public RestAPIController(final AsyncVisitorExecutionService executionService, final ProcessStorage processStorage,
        final ResultStorage resultStorage) {
        this.executionService = executionService;
        this.processStorage = processStorage;
        this.resultStorage = resultStorage;
    }

    @PostMapping("/visitors")
    public ResponseEntity<VisitorProcess> addNewVisit(final HttpServletRequest request, final String url)
        throws InterruptedException {
        try {
            final VisitorProcess process = new VisitorProcess(request, new URL(url));
            final VisitorResult result = new VisitorResult(process);
            this.processStorage.save(process);
            this.resultStorage.save(result);

            this.executionService.execute(process, result, MAX_RECURSION);

            return ResponseEntity.ok(process);
        } catch (final MalformedURLException e) {
            LOGGER.error("Encountered invalid URL \"{}\". Answering with BAD_REQUEST.", url, e);
            return ResponseEntity.badRequest().build();
        } catch (final IOException e) {
            LOGGER.error("Unable to create new result. Answering with INTERNAL_SERVER_ERROR.", url, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/visitors/{id}")
    public ResponseEntity<VisitorProcess> getVisit(@PathVariable final String id) {

        return this.processStorage.get(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/visitors/{id}/result")
    public ResponseEntity<VisitorResult> getVisitResult(@PathVariable final String id) {

        return this.processStorage.get(id).<ResponseEntity<VisitorResult>>map(process -> {
            if (process.getStatus() != VisitorProcess.ProcessStatus.DONE) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            return this.resultStorage
                .get(process.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
        }).orElse(ResponseEntity.notFound().build());
    }
}
