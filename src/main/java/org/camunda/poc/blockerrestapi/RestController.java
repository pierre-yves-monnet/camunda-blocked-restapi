package org.camunda.poc.blockerrestapi;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@org.springframework.web.bind.annotation.RestController

public class RestController {
    @PostMapping(value = "/api/execute/simpleprocess", produces = "application/json")
    @ResponseBody
    public Map<String, Object> callSimpleProcessPost(@RequestParam(required = false) String processName) {
        SimpleProcess simpleProcess = new SimpleProcess();
        return simpleProcess.executeSimpleProcessPooling();
    }


}