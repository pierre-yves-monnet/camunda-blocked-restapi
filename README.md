# Introduction
The goal of this project is to demonstrate how you can implement a REST API or a class which waits for the end of a process instance.

With Camunda, when via a REST API, you start a process instance, then the execution is done by the thread.
So, you will get an answer from your REST API when the process instance is finished, or the process instance arrived in a human task, or a catch message, a timer, or a service task with an external client (this is what Camunda calls a Transaction Boundary).
So, this is nice if the use case is to automate a list of tasks and then wait for the result of the execution to send back the final status.

If you want to execute the flow in a multithreading way, you can check the "Asynchronous before/after" checkbox. You explicitly declare a Transaction Boundary. Thread execution stops here, and the next part will be taken in charge by the Job Executor.
This is very helpful if you have multiple branches or multi-instance tasks, and you want to execute this part in a multithreading environment.
Come back to our use case: you get the answer not at the end of your process but at the boundary event.
How can you wait until the end of the execution then?

# Pooling:
You can then decide to sleep x secondes and ask Camunda engine if the process instance is finished or not.
This is simple, but due to the pooling, you generate a lot of traffic to the Camunda Engine.
Have a look to Simpleprocess.executeSimpleProcessMessage()

# Call me back:
You can use the External Client pattern. in your process, when you want (maybe at the end of the process, but maybe before), send a message (on  a topic name "simple-process-message-end" for example)
Then, your software subscribes to this topic. When the process instance reaches the event, the message is sent, your software receives it, and then you can wake up your receiver.
To do that,
1/ your software must subscribe to the topic. See SimpleProcess.registerSimpleProcessCallback()
2/ then, when a new process instance is created, the thread must wait()
On a callback, all threads are notified, and you need to just detect if this is your process id or not.
(see SimpleProcess.executeSimpleProcessMessage())

# Limitation
The second method does not work if you have multiple java machines. In that circumstance, the different java machines will subscribe to the topic (which is perfectly fine), but you can't ensure Camunda will call back the machine that creates the process instance.
Then, if you have three machines, "Blue/Red / Orange", Blue creates a process instance and wait() for the callback. But the message can be sent to the machine RED. BLUE will never receive the callback.

# How to test it
Deploy processes SimpleMessage under src/main/ressources
use the Spring Application under org.camunda.poc.blockerrestapi.Application, and send the appropriate REST API
==> Note: this Spring application is not finished at this moment
or execute the application in org.camunda.poc.blockerrestapi.LocalApplication
==> At this moment, the registration is correct, but no callback is received—investigation in progress.
