
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


public interface ListGroupsForCourseQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      DefaultConnector,
      ListGroupsForCourseQuery.Data,
      ListGroupsForCourseQuery.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val courseCode: String
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val scheduleGroups: List<ScheduleGroupsItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class ScheduleGroupsItem(
  
    val id: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val groupCode: String,
    val day: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val lecturer: String,
    val semesterOffered: String,
    val capacity: Int,
    val enrolled: Int
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "ListGroupsForCourse"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun ListGroupsForCourseQuery.ref(
  
    courseCode: String,

  
  
): com.google.firebase.dataconnect.QueryRef<
    ListGroupsForCourseQuery.Data,
    ListGroupsForCourseQuery.Variables
  > =
  ref(
    
      ListGroupsForCourseQuery.Variables(
        courseCode=courseCode,
  
      )
    
  )

public suspend fun ListGroupsForCourseQuery.execute(

  
    
      courseCode: String,

  

  ): com.google.firebase.dataconnect.QueryResult<
    ListGroupsForCourseQuery.Data,
    ListGroupsForCourseQuery.Variables
  > =
  ref(
    
      courseCode=courseCode,
  
    
  ).execute()


  public fun ListGroupsForCourseQuery.flow(
    
      courseCode: String,

  
    
    ): kotlinx.coroutines.flow.Flow<ListGroupsForCourseQuery.Data> =
    ref(
        
          courseCode=courseCode,
  
        
      ).subscribe()
      .flow
      ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
      ._flow_filterNotNull()
      ._flow_map { it.data }

