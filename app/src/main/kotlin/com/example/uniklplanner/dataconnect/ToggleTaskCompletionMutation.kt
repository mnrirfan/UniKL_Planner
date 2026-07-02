
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



public interface ToggleTaskCompletionMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      DefaultConnector,
      ToggleTaskCompletionMutation.Data,
      ToggleTaskCompletionMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val taskId: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val isCompleted: Boolean
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val studyTask_update: StudyTaskKey?
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "ToggleTaskCompletion"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun ToggleTaskCompletionMutation.ref(
  
    taskId: java.util.UUID,isCompleted: Boolean,

  
  
): com.google.firebase.dataconnect.MutationRef<
    ToggleTaskCompletionMutation.Data,
    ToggleTaskCompletionMutation.Variables
  > =
  ref(
    
      ToggleTaskCompletionMutation.Variables(
        taskId=taskId,isCompleted=isCompleted,
  
      )
    
  )

public suspend fun ToggleTaskCompletionMutation.execute(

  
    
      taskId: java.util.UUID,isCompleted: Boolean,

  

  ): com.google.firebase.dataconnect.MutationResult<
    ToggleTaskCompletionMutation.Data,
    ToggleTaskCompletionMutation.Variables
  > =
  ref(
    
      taskId=taskId,isCompleted=isCompleted,
  
    
  ).execute()


