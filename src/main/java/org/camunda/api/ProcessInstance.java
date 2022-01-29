package org.camunda.api;

import java.util.HashMap;
import java.util.Map;

public class ProcessInstance {


    private final APIClient login;

    protected ProcessInstance(APIClient login) {
        this.login = login;

    }

    public Map<String, Object> startInstance(String processName, Map<String, Object> variables, String businessKey) throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        if (variables != null)
            parameters.put("variables", variables);
        if (businessKey != null)
            parameters.put("businessKey", businessKey);
        return (Map) login.doPost("/process-definition/key/"+processName+"/start", parameters);
    }


    public Map<String, Object> getProcessInstance(String id) throws Exception {

        return (Map) login.doGet("/process-instance/"+id);
    }
}
