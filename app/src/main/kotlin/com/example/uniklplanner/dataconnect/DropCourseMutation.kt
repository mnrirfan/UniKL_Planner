
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



public interface DropCourseMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      DefaultConnector,
      DropCourseMutation.Data,
      DropCourseMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val enrollmentId: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val enrollment_delete: EnrollmentKey?
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "DropCourse"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun DropCourseMutation.ref(
  
    enrollmentId: java.util.UUID,

  
  
): com.google.firebase.dataconnect.MutationRef<
    DropCourseMutation.Data,
    DropCourseMutation.Variables
  > =
  ref(
    
      DropCourseMutation.Variables(
        enrollmentId=enrollmentId,
  
      )
    
  )

public suspend fun DropCourseMutation.execute(

  
    
      enrollmentId: java.util.UUID,

  

  ): com.google.firebase.dataconnect.MutationResult<
    DropCourseMutation.Data,
    DropCourseMutation.Variables
  > =
  ref(
    
      enrollmentId=enrollmentId,
  
    
  ).execute()


