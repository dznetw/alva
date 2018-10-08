package com.example.alva.ui;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.alva.storage.VisitorProcess;

@Controller
public class NewProcessController {

    private final RestAPIController apiController;

    @Autowired
    public NewProcessController(final RestAPIController apiController) {this.apiController = apiController;}

    @GetMapping("/new")
    public ResponseEntity<VisitorProcess> startNewVisitor(HttpServletRequest request, @RequestParam final String url)
        throws InterruptedException {
        return this.apiController.addNewVisit(request, url);
    }
}
