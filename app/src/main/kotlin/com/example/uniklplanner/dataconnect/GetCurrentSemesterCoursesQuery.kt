
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


public interface GetCurrentSemesterCoursesQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      DefaultConnector,
      GetCurrentSemesterCoursesQuery.Data,
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
    val course: Course,
    val group: Group?
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class Course(
  
    val code: String,
    val name: String,
    val credits: Int,
    val category: String
  ) {
    
    
  }
      
        @kotlinx.serialization.Serializable
  public data class Group(
  
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
    public val operationName: String = "GetCurrentSemesterCourses"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Unit> =
      kotlinx.serialization.serializer()
  }
}

public fun GetCurrentSemesterCoursesQuery.ref(
  
): com.google.firebase.dataconnect.QueryRef<
    GetCurrentSemesterCoursesQuery.Data,
    Unit
  > =
  ref(
    
      Unit
    
  )

public suspend fun GetCurrentSemesterCoursesQuery.execute(

  

  ): com.google.firebase.dataconnect.QueryResult<
    GetCurrentSemesterCoursesQuery.Data,
    Unit
  > =
  ref(
    
  ).execute()


  public fun GetCurrentSemesterCoursesQuery.flow(
    
    ): kotlinx.coroutines.flow.Flow<GetCurrentSemesterCoursesQuery.Data> =
    ref(
        
      ).subscribe()
      .flow
      ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
      ._flow_filterNotNull()
      ._flow_map { it.data }

