
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



public interface UpdateEnrollmentStatusMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      DefaultConnector,
      UpdateEnrollmentStatusMutation.Data,
      UpdateEnrollmentStatusMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val enrollmentId: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val newStatus: String,
    val credits: com.google.firebase.dataconnect.OptionalVariable<Int?>
  ) {
    
    
      
      @kotlin.DslMarker public annotation class BuilderDsl

      @BuilderDsl
      public interface Builder {
        public var enrollmentId: java.util.UUID
        public var newStatus: String
        public var credits: Int?
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          enrollmentId: java.util.UUID,newStatus: String,
          block_: Builder.() -> Unit
        ): Variables {
          var enrollmentId= enrollmentId
            var newStatus= newStatus
            var credits: com.google.firebase.dataconnect.OptionalVariable<Int?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            

          return object : Builder {
            override var enrollmentId: java.util.UUID
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { enrollmentId = value_ }
              
            override var newStatus: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { newStatus = value_ }
              
            override var credits: Int?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { credits = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            
          }.apply(block_)
          .let {
            Variables(
              enrollmentId=enrollmentId,newStatus=newStatus,credits=credits,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val enrollment_update: EnrollmentKey?
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "UpdateEnrollmentStatus"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun UpdateEnrollmentStatusMutation.ref(
  
    enrollmentId: java.util.UUID,newStatus: String,

  
    block_: UpdateEnrollmentStatusMutation.Variables.Builder.() -> Unit = {}
  
): com.google.firebase.dataconnect.MutationRef<
    UpdateEnrollmentStatusMutation.Data,
    UpdateEnrollmentStatusMutation.Variables
  > =
  ref(
    
      UpdateEnrollmentStatusMutation.Variables.build(
        enrollmentId=enrollmentId,newStatus=newStatus,
  
    block_
      )
    
  )

public suspend fun UpdateEnrollmentStatusMutation.execute(

  
    
      enrollmentId: java.util.UUID,newStatus: String,

  
    block_: UpdateEnrollmentStatusMutation.Variables.Builder.() -> Unit = {}

  ): com.google.firebase.dataconnect.MutationResult<
    UpdateEnrollmentStatusMutation.Data,
    UpdateEnrollmentStatusMutation.Variables
  > =
  ref(
    
      enrollmentId=enrollmentId,newStatus=newStatus,
  
    block_
    
  ).execute()


