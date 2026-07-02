
@file:Suppress(
  "KotlinRedundantDiagnosticSuppress",
  "LocalVariableName",
  "MayBeConstant",
  "RedundantVisibilityModifier",
  "RedundantCompanionReference",
  "RemoveEmptyClassBody",
  "SpellCheckingInspection",
  "LocalVariableName",
  "unused",
)

package com.example.uniklplanner.dataconnect



public interface DeleteTaskMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      DefaultConnector,
      DeleteTaskMutation.Data,
      DeleteTaskMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val taskId: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val studyTask_delete: StudyTaskKey?
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "DeleteTask"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun DeleteTaskMutation.ref(
  
    taskId: java.util.UUID,

  
  
): com.google.firebase.dataconnect.MutationRef<
    DeleteTaskMutation.Data,
    DeleteTaskMutation.Variables
  > =
  ref(
    
      DeleteTaskMutation.Variables(
        taskId=taskId,
  
      )
    
  )

public suspend fun DeleteTaskMutation.execute(

  
    
      taskId: java.util.UUID,

  

  ): com.google.firebase.dataconnect.MutationResult<
    DeleteTaskMutation.Data,
    DeleteTaskMutation.Variables
  > =
  ref(
    
      taskId=taskId,
  
    
  ).execute()


