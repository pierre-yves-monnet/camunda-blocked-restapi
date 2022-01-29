package org.camunda.poc.blockerrestapi;

public class LocalApplication {

    public static void main(String[] args) {

        SimpleProcess simpleProcess = new SimpleProcess();
        System.out.println("Execute Simple process");
        // simpleProcess.executeSimpleProcessPooling();
        System.out.println("End of execute Simple process");

        simpleProcess.registerSimpleProcessCallback();

        System.out.println("Execute Simple process with Message");
        simpleProcess.executeSimpleProcessMessage();
        System.out.println("End of execute Simple process with Message");


    }

}
