package com.example.ai.workflow.engine

import com.example.ai.tool.ToolExecutionManager
import com.example.ai.workflow.domain.model.*
import com.example.ai.workflow.domain.repository.WorkflowRepository
import com.example.core.utils.Logger
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Autonomous Workflow Engine
 * Handles execution, pausing, and resuming of multi-step intelligent workflows.
 */
class AutonomousWorkflowEngine(
    private val workflowRepository: WorkflowRepository,
    private val toolExecutionManager: ToolExecutionManager,
    private val safetyManager: SafetyManager,
    private val orchestrator: com.example.ai.orchestrator.CentralOrchestrator
) {

    suspend fun executeWorkflow(workflowId: String) = withContext(Dispatchers.IO) {
        val workflow = workflowRepository.getWorkflow(workflowId) ?: return@withContext
        
        if (workflow.status == WorkflowStatus.COMPLETED || workflow.status == WorkflowStatus.FAILED || workflow.status == WorkflowStatus.CANCELLED) {
            Logger.d("Workflow $workflowId already finished (${workflow.status}).")
            return@withContext
        }

        if (!safetyManager.canExecute()) {
            Logger.w("Safety checks failed. Pausing workflow: $workflowId")
            updateWorkflowStatus(workflow, WorkflowStatus.PAUSED)
            return@withContext
        }

        updateWorkflowStatus(workflow, WorkflowStatus.RUNNING)
        var currentWorkflow = workflow

        try {
            withTimeout(300_000) { // 5 minute max execution
                while (currentWorkflow.currentStepIndex < currentWorkflow.steps.size) {
                    if (!safetyManager.canExecute()) {
                        updateWorkflowStatus(currentWorkflow, WorkflowStatus.PAUSED)
                        break
                    }

                    val currentStep = currentWorkflow.steps[currentWorkflow.currentStepIndex]
                    
                    // Execute Step with step-level timeout
                    val executedStep = try {
                        withTimeout(60_000) { // 1 minute per step max
                            executeStep(currentStep)
                        }
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        currentStep.copy(status = WorkflowStatus.FAILED, result = "Step timed out")
                    }
                    
                    // Update Step
                    workflowRepository.updateStep(executedStep)
                    
                    if (executedStep.status == WorkflowStatus.FAILED) {
                        updateWorkflowStatus(currentWorkflow, WorkflowStatus.FAILED)
                        Logger.e("Workflow $workflowId failed at step ${executedStep.stepIndex}")
                        return@withTimeout
                    } else if (executedStep.status == WorkflowStatus.PAUSED) {
                        updateWorkflowStatus(currentWorkflow, WorkflowStatus.PAUSED)
                        Logger.d("Workflow $workflowId paused at step ${executedStep.stepIndex}")
                        return@withTimeout
                    }
                    
                    // Progress to next step
                    val nextIndex = currentWorkflow.currentStepIndex + 1
                    currentWorkflow = currentWorkflow.copy(currentStepIndex = nextIndex, updatedAt = System.currentTimeMillis())
                    workflowRepository.saveWorkflow(currentWorkflow)
                }
                
                if (currentWorkflow.currentStepIndex >= currentWorkflow.steps.size) {
                    updateWorkflowStatus(currentWorkflow, WorkflowStatus.COMPLETED)
                    Logger.d("Workflow $workflowId completed successfully.")
                }
            }
            
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Logger.e("Fatal error: Workflow $workflowId timed out entirely", e)
            updateWorkflowStatus(currentWorkflow, WorkflowStatus.FAILED)
        } catch (e: Exception) {
            Logger.e("Fatal error executing workflow $workflowId", e)
            updateWorkflowStatus(currentWorkflow, WorkflowStatus.FAILED)
        }
    }
    
    private suspend fun executeStep(step: WorkflowStep): WorkflowStep {
        Logger.d("Executing Workflow Step: ${step.type} (${step.stepIndex})")
        
        return when (step.type) {
            StepType.TOOL_CALL -> {
                val toolName = step.actionPayload["toolName"] ?: return step.copy(status = WorkflowStatus.FAILED, result = "Missing toolName")
                val toolArgsString = step.actionPayload["toolArgs"] ?: "{}"
                
                val args = try { Json.parseToJsonElement(toolArgsString).jsonObject } catch (e: Exception) { buildJsonObject {} }
                
                val result = toolExecutionManager.executeTool(toolName, args)
                if (result is com.example.ai.tool.ToolExecutionResult.Success) {
                    step.copy(status = WorkflowStatus.COMPLETED, result = result.output)
                } else {
                    step.copy(status = WorkflowStatus.FAILED, result = (result as com.example.ai.tool.ToolExecutionResult.Error).errorMessage)
                }
            }
            StepType.AI_REASONING -> {
                val prompt = step.actionPayload["prompt"] ?: "Analyze current state."
                val sessionId = "workflow_${step.workflowId}"
                val result = orchestrator.processTask(prompt, sessionId)
                
                if (result is com.example.core.utils.Result.Success) {
                    step.copy(status = WorkflowStatus.COMPLETED, result = result.data)
                } else {
                    val error = (result as? com.example.core.utils.Result.Error)?.message ?: "Reasoning failed"
                    step.copy(status = WorkflowStatus.FAILED, result = error)
                }
            }
            StepType.DELAY -> {
                val delayMs = step.actionPayload["delayMs"]?.toLongOrNull() ?: 1000L
                delay(delayMs)
                step.copy(status = WorkflowStatus.COMPLETED, result = "Delay $delayMs ms complete")
            }
            StepType.CONDITION -> {
                // Evaluate condition, potentially branching / skipping
                step.copy(status = WorkflowStatus.COMPLETED, result = "Condition evaluated to true")
            }
        }
    }

    private suspend fun updateWorkflowStatus(workflow: Workflow, status: WorkflowStatus) {
        val updated = workflow.copy(status = status, updatedAt = System.currentTimeMillis())
        workflowRepository.saveWorkflow(updated)
    }
}
