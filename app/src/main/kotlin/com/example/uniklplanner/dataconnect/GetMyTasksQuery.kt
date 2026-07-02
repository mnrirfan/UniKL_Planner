
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


public interface GetMyTasksQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      DefaultConnector,
      GetMyTasksQuery.Data,
      Unit
    >
{
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val studyTasks: List<StudyTasksItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class StudyTasksItem(
  
    val id: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val title: String,
    val courseName: String,
    val dueDate: String,
    val isCompleted: Boolean
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetMyTasks"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Unit> =
      kotlinx.serialization.serializer()
  }
}

public fun GetMyTasksQuery.ref(
  
): com.google.firebase.dataconnect.QueryRef<
    GetMyTasksQuery.Data,
    Unit
  > =
  ref(
    
      Unit
    
  )

public suspend fun GetMyTasksQuery.execute(

  

  ): com.google.firebase.dataconnect.QueryResult<
    GetMyTasksQuery.Data,
    Unit
  > =
  ref(
    
  ).execute()


  public fun GetMyTasksQuery.flow(
    
    ): kotlinx.coroutines.flow.Flow<GetMyTasksQuery.Data> =
    ref(
        
      ).subscribe()
      .flow
      ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
      ._flow_filterNotNull()
      ._flow_map { it.data }

