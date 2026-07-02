
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



public interface EnrollCourseMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      DefaultConnector,
      EnrollCourseMutation.Data,
      EnrollCourseMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val courseCode: String,
    val groupId: com.google.firebase.dataconnect.OptionalVariable<@kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID?>
  ) {
    
    
      
      @kotlin.DslMarker public annotation class BuilderDsl

      @BuilderDsl
      public interface Builder {
        public var courseCode: String
        public var groupId: java.util.UUID?
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          courseCode: String,
          block_: Builder.() -> Unit
        ): Variables {
          var courseCode= courseCode
            var groupId: com.google.firebase.dataconnect.OptionalVariable<java.util.UUID?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            

          return object : Builder {
            override var courseCode: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { courseCode = value_ }
              
            override var groupId: java.util.UUID?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { groupId = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            
          }.apply(block_)
          .let {
            Variables(
              courseCode=courseCode,groupId=groupId,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val enrollment_insert: EnrollmentKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "EnrollCourse"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun EnrollCourseMutation.ref(
  
    courseCode: String,

  
    block_: EnrollCourseMutation.Variables.Builder.() -> Unit = {}
  
): com.google.firebase.dataconnect.MutationRef<
    EnrollCourseMutation.Data,
    EnrollCourseMutation.Variables
  > =
  ref(
    
      EnrollCourseMutation.Variables.build(
        courseCode=courseCode,
  
    block_
      )
    
  )

public suspend fun EnrollCourseMutation.execute(

  
    
      courseCode: String,

  
    block_: EnrollCourseMutation.Variables.Builder.() -> Unit = {}

  ): com.google.firebase.dataconnect.MutationResult<
    EnrollCourseMutation.Data,
    EnrollCourseMutation.Variables
  > =
  ref(
    
      courseCode=courseCode,
  
    block_
    
  ).execute()


