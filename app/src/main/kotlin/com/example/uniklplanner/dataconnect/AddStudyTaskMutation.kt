
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



public interface AddStudyTaskMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      DefaultConnector,
      AddStudyTaskMutation.Data,
      AddStudyTaskMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val title: String,
    val courseName: String,
    val dueDate: String
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val studyTask_insert: StudyTaskKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "AddStudyTask"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun AddStudyTaskMutation.ref(
  
    title: String,courseName: String,dueDate: String,

  
  
): com.google.firebase.dataconnect.MutationRef<
    AddStudyTaskMutation.Data,
    AddStudyTaskMutation.Variables
  > =
  ref(
    
      AddStudyTaskMutation.Variables(
        title=title,courseName=courseName,dueDate=dueDate,
  
      )
    
  )

public suspend fun AddStudyTaskMutation.execute(

  
    
      title: String,courseName: String,dueDate: String,

  

  ): com.google.firebase.dataconnect.MutationResult<
    AddStudyTaskMutation.Data,
    AddStudyTaskMutation.Variables
  > =
  ref(
    
      title=title,courseName=courseName,dueDate=dueDate,
  
    
  ).execute()


