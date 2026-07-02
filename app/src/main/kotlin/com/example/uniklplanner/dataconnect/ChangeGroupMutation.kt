
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



public interface ChangeGroupMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      DefaultConnector,
      ChangeGroupMutation.Data,
      ChangeGroupMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val enrollmentId: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val newGroupId: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val enrollment_update: EnrollmentKey?
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "ChangeGroup"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun ChangeGroupMutation.ref(
  
    enrollmentId: java.util.UUID,newGroupId: java.util.UUID,

  
  
): com.google.firebase.dataconnect.MutationRef<
    ChangeGroupMutation.Data,
    ChangeGroupMutation.Variables
  > =
  ref(
    
      ChangeGroupMutation.Variables(
        enrollmentId=enrollmentId,newGroupId=newGroupId,
  
      )
    
  )

public suspend fun ChangeGroupMutation.execute(

  
    
      enrollmentId: java.util.UUID,newGroupId: java.util.UUID,

  

  ): com.google.firebase.dataconnect.MutationResult<
    ChangeGroupMutation.Data,
    ChangeGroupMutation.Variables
  > =
  ref(
    
      enrollmentId=enrollmentId,newGroupId=newGroupId,
  
    
  ).execute()


