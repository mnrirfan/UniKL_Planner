
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


public interface GetMyEnrollmentsQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      DefaultConnector,
      GetMyEnrollmentsQuery.Data,
      Unit
    >
{
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val enrollments: List<EnrollmentsItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class EnrollmentsItem(
  
    val id: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val status: String,
    val creditGained: Int?,
    val semesterTaken: String?,
    val grade: String?,
    val course: Course,
    val group: Group?
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class Course(
  
    val code: String,
    val name: String,
    val credits: Int,
    val category: String,
    val year: Int,
    val semester: Int,
    val prerequisite: String?,
    val isOptional: Boolean
  ) {
    
    
  }
      
        @kotlinx.serialization.Serializable
  public data class Group(
  
    val id: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val groupCode: String,
    val day: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val lecturer: String
  ) {
    
    
  }
      
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetMyEnrollments"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Unit> =
      kotlinx.serialization.serializer()
  }
}

public fun GetMyEnrollmentsQuery.ref(
  
): com.google.firebase.dataconnect.QueryRef<
    GetMyEnrollmentsQuery.Data,
    Unit
  > =
  ref(
    
      Unit
    
  )

public suspend fun GetMyEnrollmentsQuery.execute(

  

  ): com.google.firebase.dataconnect.QueryResult<
    GetMyEnrollmentsQuery.Data,
    Unit
  > =
  ref(
    
  ).execute()


  public fun GetMyEnrollmentsQuery.flow(
    
    ): kotlinx.coroutines.flow.Flow<GetMyEnrollmentsQuery.Data> =
    ref(
        
      ).subscribe()
      .flow
      ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
      ._flow_filterNotNull()
      ._flow_map { it.data }

