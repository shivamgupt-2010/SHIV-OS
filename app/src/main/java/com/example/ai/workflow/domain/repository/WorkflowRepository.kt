package com.example.ai.workflow.domain.repository

import com.example.ai.workflow.domain.model.*
import com.example.data.local.dao.WorkflowDao
import com.example.data.local.entity.WorkflowEntity
import com.example.data.local.entity.WorkflowStepEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class WorkflowRepository(
    private val workflowDao: WorkflowDao,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    suspend fun saveWorkflow(workflow: Workflow) {
        val entity = WorkflowEntity(
            id = workflow.id,
            name = workflow.name,
            description = workflow.description,
            status = workflow.status.name,
            currentStepIndex = workflow.currentStepIndex,
            progressMetadataJson = json.encodeToString(workflow.progressMetadata),
            createdAt = workflow.createdAt,
            updatedAt = workflow.updatedAt
        )
        workflowDao.insertWorkflow(entity)
        
        val stepEntities = workflow.steps.map { 
            WorkflowStepEntity(
                id = it.id,
                workflowId = it.workflowId,
                stepIndex = it.stepIndex,
                type = it.type.name,
                actionPayloadJson = json.encodeToString(it.actionPayload),
                status = it.status.name,
                resultJson = it.result,
                retryCount = it.retryCount
            )
        }
        workflowDao.insertSteps(stepEntities)
    }

    suspend fun getWorkflow(id: String): Workflow? {
        val entity = workflowDao.getWorkflowById(id) ?: return null
        val steps = workflowDao.getStepsForWorkflow(id).map { it.toDomain() }
        
        return Workflow(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            status = enumValueOf(entity.status),
            currentStepIndex = entity.currentStepIndex,
            steps = steps,
            progressMetadata = try { json.decodeFromString(entity.progressMetadataJson) } catch (e: Exception) { emptyMap() },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    suspend fun getActiveWorkflows(): List<Workflow> {
        return workflowDao.getActiveWorkflows().mapNotNull { getWorkflow(it.id) }
    }
    
    suspend fun updateStep(step: WorkflowStep) {
        workflowDao.updateStep(
            WorkflowStepEntity(
                id = step.id,
                workflowId = step.workflowId,
                stepIndex = step.stepIndex,
                type = step.type.name,
                actionPayloadJson = json.encodeToString(step.actionPayload),
                status = step.status.name,
                resultJson = step.result,
                retryCount = step.retryCount
            )
        )
    }

    private fun WorkflowStepEntity.toDomain(): WorkflowStep {
        return WorkflowStep(
            id = id,
            workflowId = workflowId,
            stepIndex = stepIndex,
            type = enumValueOf(type),
            actionPayload = try { json.decodeFromString(actionPayloadJson) } catch (e: Exception) { emptyMap() },
            status = enumValueOf(status),
            result = resultJson,
            retryCount = retryCount
        )
    }
}
