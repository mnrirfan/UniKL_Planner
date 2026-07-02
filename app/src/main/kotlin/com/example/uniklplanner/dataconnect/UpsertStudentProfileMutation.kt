
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



public interface UpsertStudentProfileMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      DefaultConnector,
      UpsertStudentProfileMutation.Data,
      UpsertStudentProfileMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val studentId: String,
    val name: String,
    val programme: String,
    val intake: String,
    val currentSemester: Int,
    val totalCreditsRequired: Int,
    val institute: String,
    val academicAdvisor: String,
    val email: String
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val student_upsert: StudentKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "UpsertStudentProfile"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun UpsertStudentProfileMutation.ref(
  
    studentId: String,name: String,programme: String,intake: String,currentSemester: Int,totalCreditsRequired: Int,institute: String,academicAdvisor: String,email: String,

  
  
): com.google.firebase.dataconnect.MutationRef<
    UpsertStudentProfileMutation.Data,
    UpsertStudentProfileMutation.Variables
  > =
  ref(
    
      UpsertStudentProfileMutation.Variables(
        studentId=studentId,name=name,programme=programme,intake=intake,currentSemester=currentSemester,totalCreditsRequired=totalCreditsRequired,institute=institute,academicAdvisor=academicAdvisor,email=email,
  
      )
    
  )

public suspend fun UpsertStudentProfileMutation.execute(

  
    
      studentId: String,name: String,programme: String,intake: String,currentSemester: Int,totalCreditsRequired: Int,institute: String,academicAdvisor: String,email: String,

  

  ): com.google.firebase.dataconnect.MutationResult<
    UpsertStudentProfileMutation.Data,
    UpsertStudentProfileMutation.Variables
  > =
  ref(
    
      studentId=studentId,name=name,programme=programme,intake=intake,currentSemester=currentSemester,totalCreditsRequired=totalCreditsRequired,institute=institute,academicAdvisor=academicAdvisor,email=email,
  
    
  ).execute()


