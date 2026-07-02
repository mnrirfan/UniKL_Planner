
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


import kotlinx.coroutines.flow.filterNotNull as _flow_filterNotNull
import kotlinx.coroutines.flow.map as _flow_map


public interface GetStudentProfileQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      DefaultConnector,
      GetStudentProfileQuery.Data,
      Unit
    >
{
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val student: Student?
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class Student(
  
    val uid: String,
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
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetStudentProfile"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Unit> =
      kotlinx.serialization.serializer()
  }
}

public fun GetStudentProfileQuery.ref(
  
): com.google.firebase.dataconnect.QueryRef<
    GetStudentProfileQuery.Data,
    Unit
  > =
  ref(
    
      Unit
    
  )

public suspend fun GetStudentProfileQuery.execute(

  

  ): com.google.firebase.dataconnect.QueryResult<
    GetStudentProfileQuery.Data,
    Unit
  > =
  ref(
    
  ).execute()


  public fun GetStudentProfileQuery.flow(
    
    ): kotlinx.coroutines.flow.Flow<GetStudentProfileQuery.Data> =
    ref(
        
      ).subscribe()
      .flow
      ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
      ._flow_filterNotNull()
      ._flow_map { it.data }

