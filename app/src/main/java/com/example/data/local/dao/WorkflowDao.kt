package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.WorkflowEntity
import com.example.data.local.entity.WorkflowStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkflowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: WorkflowEntity)

    @Update
    suspend fun updateWorkflow(workflow: WorkflowEntity)

    @Query("SELECT * FROM workflows WHERE id = :id")
    suspend fun getWorkflowById(id: String): WorkflowEntity?

    @Query("SELECT * FROM workflows WHERE status IN ('PENDING', 'RUNNING')")
    suspend fun getActiveWorkflows(): List<WorkflowEntity>

    @Query("SELECT * FROM workflows WHERE status IN ('PENDING', 'RUNNING')")
    fun getActiveWorkflowsFlow(): kotlinx.coroutines.flow.Flow<List<WorkflowEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<WorkflowStepEntity>)

    @Update
    suspend fun updateStep(step: WorkflowStepEntity)

    @Query("SELECT * FROM workflow_steps WHERE workflowId = :workflowId ORDER BY stepIndex ASC")
    suspend fun getStepsForWorkflow(workflowId: String): List<WorkflowStepEntity>
}
