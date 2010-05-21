package fedreg.workflow

import static groovyx.gpars.GParsPool.withPool
import static groovyx.gpars.GParsPool.executeAsync
import groovyx.gpars.actor.*

class ExecutionActor extends DynamicDispatchActor {
	
	def processService
	def taskService
	def workflowMessager
	
	void onMessage(List message) {
        def taskInstanceID = message.get(0)
		def action = message.get(1)
		
		switch(action) {
			case ExecutionAction.APPROVALREQUIRED:
				taskService.requestApproval(taskInstanceID)
				break
			case ExecutionAction.APPROVALREJECT:
				def rejection = message.get(2)
				def reasoning = message.get(3)
				taskService.reject(taskInstanceID, rejection, reasoning)
				break
			case ExecutionAction.EXECUTE:
				taskService.execute(taskInstanceID)
				break
			case ExecutionAction.FINALIZE:
				taskService.finalize(taskInstanceID, outcome, reasoning)
				break
			default:
				throw new RuntimeException("ExecutionActor default.. WTF.. TODO")
		}
    }
	
}

public enum ExecutionAction {
	APPROVALREQUIRED,
	APPROVALREQUESTED,
	APPROVALREJECT,
	EXECUTE,
	FINALIZE
}