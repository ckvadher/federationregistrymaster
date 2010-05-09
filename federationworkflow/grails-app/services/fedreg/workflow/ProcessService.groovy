package fedreg.workflow

class ProcessService {
	
	def taskService
	
	def create(def definition) {
		Binding binding = new Binding()
		binding.process = { m, closure ->
			this.interpret(m, definition, closure)
		}
		
		def script = new GroovyShell().parse(definition)
		script.binding = binding
		script.run()
	}
	
	def interpret(Map m, String definition, Closure closure) {
		def processes = Process.findAllByName(m.name)
		int version = 1
		if (processes) {
			version = processes.size() + 1
		}
		
		def process = new Process(name: m.name, description: m.description, processVersion: version, creator: authenticatedUser, definition: definition, active:true)
		closure.delegate = new ProcessDelegate(process)
		closure()
		
		process.save()
		if(process.hasErrors()) {
			process.errors.each {
				log.error it
			}
		}
	}
	
	def initiate(String processName, String instanceDescription, ProcessPriority priority, Map params) {

		def process = Process.findWhere(name: processName, active: true)
		if (!process) {
			log.error "Unable to initiate an instance of process ${processName}"
			log.error "The process definition for ${processName} was not found, no such process by name or no process definition is active"
			return null
		}

		def processInstance = new ProcessInstance(process: process, description: instanceDescription, status: ProcessStatus.INPROGRESS, priority: priority ?:ProcessPriority.LOW, initiatedBy: authenticatedUser, params:params)
		process.addToInstances(processInstance)
		
		processInstance.save()
		if(processInstance.hasErrors()) {
			log.error "Unable to initiate an instance of process ${processName}"
			processInstance.errors.each {
				log.error it
			}
			return null
		}
		
		process.save()
		if(process.hasErrors()) {
			log.error "Unable to initiate an instance of process ${processName}"
			process.errors.each {
				log.error it
			}
			return null
		}

        return processInstance
	}
	
	def run(ProcessInstance processInstance) {
		
		taskService.initiate(processInstance)
		
	}
	
}