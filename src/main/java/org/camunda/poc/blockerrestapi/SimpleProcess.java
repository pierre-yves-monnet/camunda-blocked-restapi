package org.camunda.poc.blockerrestapi;

import org.camunda.api.APIClient;
import org.camunda.bpm.client.ExternalTaskClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

public class SimpleProcess {
    public static final String CAMUNDA_SERVER = "http://localhost:8080";
    public static final String TOPIC_MESSAGE = "simple-process-message-end";
    private final Logger logger = Logger.getLogger(SimpleProcess.class.getName());


    public Map<String, Object> executeSimpleProcessPooling() {
        APIClient login = new APIClient(CAMUNDA_SERVER);
        try {
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> processInstance = login.getProcessInstance().startInstance("simpleProcess", variables, null);
            String pid = (String) processInstance.get("id");
            // now, wait the end of the process instance
            boolean ended;
            do {
                try {
                    Map<String,Object> processStatus = login.getProcessInstance().getProcessInstance(pid);
                    ended = processStatus == null || (boolean) processStatus.get("ended");
                    if (!ended) {
                        logger.info("Pid " + pid + " does not finish, wait for it...");
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    ended = true;
                }
            } while (!ended);
            logger.info("Pid " + pid + " is finished");


        } catch (Exception e) {
            logger.severe("Error " + e);
        }
        return new HashMap<>();
    }


    /**
     * This hashset contains all PID which reach the final message
     */
    private final static HashSet<String> registerAllPid = new HashSet<>();

    /**
     * This method starts a process instance, and then wait the callback. Process must contain a Throw Message, and the software
     * register itself on the topic.
     * @return result of the execution
     */
    public Map<String, Object> executeSimpleProcessMessage() {
        APIClient login = new APIClient(CAMUNDA_SERVER);
        try {
            Map<String, Object> variables = new HashMap<>();
            Map<String, Object> processInstance = login.getProcessInstance().startInstance("simpleProcessMessage", variables, null);
            String pid = (String) processInstance.get("id");

            // now, wait the call back
            // Every callback, everybody will be notified, so we have to loop until we get the answer we look for
            // maybe we want to be smart and implement a total timeout, to not wait for ever? Timeout is set to 5 mn
            long beginTimeMarker = System.currentTimeMillis();
            while ( ! registerAllPid.contains(pid) && System.currentTimeMillis() - beginTimeMarker <5*60*1000 ) {
                try { wait(100000); } catch(Exception e) {}
            }
            if (! registerAllPid.contains(pid)) {
                logger.severe("Pid " + pid + " was never finished.");
            }
            else {
                logger.info("Pid " + pid + " is finished with ");
                //purge the marker
                synchronized (registerAllPid) {
                    registerAllPid.remove(pid);
                }
            }


        } catch (Exception e) {
            logger.severe("Error " + e);
        }
        return new HashMap<>();
    }


    /**
     * Register on the message, to receive the topic
     */
    public void registerSimpleProcessCallback() {
        APIClient apiClient = new APIClient(CAMUNDA_SERVER);

        ExternalTaskClient client = ExternalTaskClient.create()
                .workerId("CallbackSimpleProcess")
                .baseUrl(apiClient.getBaseUrl())
                .asyncResponseTimeout(10000) // long polling timeout
                .build();

        // subscribe to an external task topic as specified in the process
        client.subscribe(TOPIC_MESSAGE)
                .lockDuration(1000) // the default lock duration is 20 seconds, but you can override this
                .handler((externalTask, externalTaskService) -> {
                            logger.info("Call Back for PID=" + externalTask.getProcessInstanceId());
                            synchronized (registerAllPid) {
                                registerAllPid.add(externalTask.getProcessInstanceId());
                            }
                            // Ok, now wake up all threads, and the one who recognize himself will do the job
                                notifyAll();

                        }
                );
    }
}
