
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


public interface ListAllGroupsQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      DefaultConnector,
      ListAllGroupsQuery.Data,
      Unit
    >
{
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val scheduleGroups: List<ScheduleGroupsItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class ScheduleGroupsItem(
  
    val groupCode: String,
    val day: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val lecturer: String,
    val course: Course
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class Course(
  
    val code: String
  ) {
    
    
  }
      
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "ListAllGroups"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Unit> =
      kotlinx.serialization.serializer()
  }
}

public fun ListAllGroupsQuery.ref(
  
): com.google.firebase.dataconnect.QueryRef<
    ListAllGroupsQuery.Data,
    Unit
  > =
  ref(
    
      Unit
    
  )

public suspend fun ListAllGroupsQuery.execute(

  

  ): com.google.firebase.dataconnect.QueryResult<
    ListAllGroupsQuery.Data,
    Unit
  > =
  ref(
    
  ).execute()


  public fun ListAllGroupsQuery.flow(
    
    ): kotlinx.coroutines.flow.Flow<ListAllGroupsQuery.Data> =
    ref(
        
      ).subscribe()
      .flow
      ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
      ._flow_filterNotNull()
      ._flow_map { it.data }

