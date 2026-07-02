
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


public interface ListAllCoursesQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      DefaultConnector,
      ListAllCoursesQuery.Data,
      Unit
    >
{
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val courses: List<CoursesItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class CoursesItem(
  
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
      
    
    
  }
  

  public companion object {
    public val operationName: String = "ListAllCourses"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Unit> =
      kotlinx.serialization.serializer()
  }
}

public fun ListAllCoursesQuery.ref(
  
): com.google.firebase.dataconnect.QueryRef<
    ListAllCoursesQuery.Data,
    Unit
  > =
  ref(
    
      Unit
    
  )

public suspend fun ListAllCoursesQuery.execute(

  

  ): com.google.firebase.dataconnect.QueryResult<
    ListAllCoursesQuery.Data,
    Unit
  > =
  ref(
    
  ).execute()


  public fun ListAllCoursesQuery.flow(
    
    ): kotlinx.coroutines.flow.Flow<ListAllCoursesQuery.Data> =
    ref(
        
      ).subscribe()
      .flow
      ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
      ._flow_filterNotNull()
      ._flow_map { it.data }

