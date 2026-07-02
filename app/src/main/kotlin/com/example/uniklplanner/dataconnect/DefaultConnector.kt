
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

import com.google.firebase.dataconnect.getInstance as _fdcGetInstance
import kotlin.time.Duration.Companion.milliseconds as _milliseconds

public interface DefaultConnector : com.google.firebase.dataconnect.generated.GeneratedConnector<DefaultConnector> {
  override val dataConnect: com.google.firebase.dataconnect.FirebaseDataConnect

  
    public val addStudyTask: AddStudyTaskMutation
  
    public val changeGroup: ChangeGroupMutation
  
    public val deleteTask: DeleteTaskMutation
  
    public val dropCourse: DropCourseMutation
  
    public val enrollCourse: EnrollCourseMutation
  
    public val getCurrentSemesterCourses: GetCurrentSemesterCoursesQuery
  
    public val getMyEnrollments: GetMyEnrollmentsQuery
  
    public val getMyTasks: GetMyTasksQuery
  
    public val getStudentProfile: GetStudentProfileQuery
  
    public val listAllCourses: ListAllCoursesQuery
  
    public val listAllGroups: ListAllGroupsQuery
  
    public val listGroupsForCourse: ListGroupsForCourseQuery
  
    public val toggleTaskCompletion: ToggleTaskCompletionMutation
  
    public val updateEnrollmentStatus: UpdateEnrollmentStatusMutation
  
    public val upsertStudentProfile: UpsertStudentProfileMutation
  

  public companion object {
    @Suppress("MemberVisibilityCanBePrivate")
    public val config: com.google.firebase.dataconnect.ConnectorConfig = com.google.firebase.dataconnect.ConnectorConfig(
      connector = "default",
      location = "asia-southeast1",
      serviceId = "uniklplanner",
    )

    public fun getInstance(
      dataConnect: com.google.firebase.dataconnect.FirebaseDataConnect
    ):DefaultConnector = synchronized(instances) {
      instances.getOrPut(dataConnect) {
        DefaultConnectorImpl(dataConnect)
      }
    }

    private val instances = java.util.WeakHashMap<com.google.firebase.dataconnect.FirebaseDataConnect, DefaultConnectorImpl>()

    
  }
}

public val DefaultConnector.Companion.instance:DefaultConnector
  get() = getInstance(com.google.firebase.dataconnect.FirebaseDataConnect._fdcGetInstance(
    config
  ))

public fun DefaultConnector.Companion.getInstance(
  settings: com.google.firebase.dataconnect.DataConnectSettings = com.google.firebase.dataconnect.DataConnectSettings()
):DefaultConnector =
  getInstance(com.google.firebase.dataconnect.FirebaseDataConnect._fdcGetInstance(config, settings))

public fun DefaultConnector.Companion.getInstance(
  app: com.google.firebase.FirebaseApp,
  settings: com.google.firebase.dataconnect.DataConnectSettings = com.google.firebase.dataconnect.DataConnectSettings()
):DefaultConnector =
  getInstance(com.google.firebase.dataconnect.FirebaseDataConnect._fdcGetInstance(app, config, settings))

private class DefaultConnectorImpl(
  override val dataConnect: com.google.firebase.dataconnect.FirebaseDataConnect
) : DefaultConnector {
  
    override val addStudyTask by lazy(LazyThreadSafetyMode.PUBLICATION) {
      AddStudyTaskMutationImpl(this)
    }
  
    override val changeGroup by lazy(LazyThreadSafetyMode.PUBLICATION) {
      ChangeGroupMutationImpl(this)
    }
  
    override val deleteTask by lazy(LazyThreadSafetyMode.PUBLICATION) {
      DeleteTaskMutationImpl(this)
    }
  
    override val dropCourse by lazy(LazyThreadSafetyMode.PUBLICATION) {
      DropCourseMutationImpl(this)
    }
  
    override val enrollCourse by lazy(LazyThreadSafetyMode.PUBLICATION) {
      EnrollCourseMutationImpl(this)
    }
  
    override val getCurrentSemesterCourses by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetCurrentSemesterCoursesQueryImpl(this)
    }
  
    override val getMyEnrollments by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetMyEnrollmentsQueryImpl(this)
    }
  
    override val getMyTasks by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetMyTasksQueryImpl(this)
    }
  
    override val getStudentProfile by lazy(LazyThreadSafetyMode.PUBLICATION) {
      GetStudentProfileQueryImpl(this)
    }
  
    override val listAllCourses by lazy(LazyThreadSafetyMode.PUBLICATION) {
      ListAllCoursesQueryImpl(this)
    }
  
    override val listAllGroups by lazy(LazyThreadSafetyMode.PUBLICATION) {
      ListAllGroupsQueryImpl(this)
    }
  
    override val listGroupsForCourse by lazy(LazyThreadSafetyMode.PUBLICATION) {
      ListGroupsForCourseQueryImpl(this)
    }
  
    override val toggleTaskCompletion by lazy(LazyThreadSafetyMode.PUBLICATION) {
      ToggleTaskCompletionMutationImpl(this)
    }
  
    override val updateEnrollmentStatus by lazy(LazyThreadSafetyMode.PUBLICATION) {
      UpdateEnrollmentStatusMutationImpl(this)
    }
  
    override val upsertStudentProfile by lazy(LazyThreadSafetyMode.PUBLICATION) {
      UpsertStudentProfileMutationImpl(this)
    }
  

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun operations(): List<com.google.firebase.dataconnect.generated.GeneratedOperation<DefaultConnector, *, *>> =
    queries() + mutations()

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun mutations(): List<com.google.firebase.dataconnect.generated.GeneratedMutation<DefaultConnector, *, *>> =
    listOf(
      addStudyTask,
        changeGroup,
        deleteTask,
        dropCourse,
        enrollCourse,
        toggleTaskCompletion,
        updateEnrollmentStatus,
        upsertStudentProfile,
        
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun queries(): List<com.google.firebase.dataconnect.generated.GeneratedQuery<DefaultConnector, *, *>> =
    listOf(
      getCurrentSemesterCourses,
        getMyEnrollments,
        getMyTasks,
        getStudentProfile,
        listAllCourses,
        listAllGroups,
        listGroupsForCourse,
        
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun copy(dataConnect: com.google.firebase.dataconnect.FirebaseDataConnect) =
    DefaultConnectorImpl(dataConnect)

  override fun equals(other: Any?): Boolean =
    other is DefaultConnectorImpl &&
    other.dataConnect == dataConnect

  override fun hashCode(): Int =
    java.util.Objects.hash(
      "DefaultConnectorImpl",
      dataConnect,
    )

  override fun toString(): String =
    "DefaultConnectorImpl(dataConnect=$dataConnect)"
}



private open class DefaultConnectorGeneratedQueryImpl<Data, Variables>(
  override val connector: DefaultConnector,
  override val operationName: String,
  override val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data>,
  override val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables>,
) : com.google.firebase.dataconnect.generated.GeneratedQuery<DefaultConnector, Data, Variables> {

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun copy(
    connector: DefaultConnector,
    operationName: String,
    dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data>,
    variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables>,
  ) =
    DefaultConnectorGeneratedQueryImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun <NewVariables> withVariablesSerializer(
    variablesSerializer: kotlinx.serialization.SerializationStrategy<NewVariables>
  ) =
    DefaultConnectorGeneratedQueryImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun <NewData> withDataDeserializer(
    dataDeserializer: kotlinx.serialization.DeserializationStrategy<NewData>
  ) =
    DefaultConnectorGeneratedQueryImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun equals(other: Any?): Boolean =
    other is DefaultConnectorGeneratedQueryImpl<*,*> &&
    other.connector == connector &&
    other.operationName == operationName &&
    other.dataDeserializer == dataDeserializer &&
    other.variablesSerializer == variablesSerializer

  override fun hashCode(): Int =
    java.util.Objects.hash(
      "DefaultConnectorGeneratedQueryImpl",
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun toString(): String =
    "DefaultConnectorGeneratedQueryImpl(" +
    "operationName=$operationName, " +
    "dataDeserializer=$dataDeserializer, " +
    "variablesSerializer=$variablesSerializer, " +
    "connector=$connector)"
}

private open class DefaultConnectorGeneratedMutationImpl<Data, Variables>(
  override val connector: DefaultConnector,
  override val operationName: String,
  override val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data>,
  override val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables>,
) : com.google.firebase.dataconnect.generated.GeneratedMutation<DefaultConnector, Data, Variables> {

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun copy(
    connector: DefaultConnector,
    operationName: String,
    dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data>,
    variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables>,
  ) =
    DefaultConnectorGeneratedMutationImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun <NewVariables> withVariablesSerializer(
    variablesSerializer: kotlinx.serialization.SerializationStrategy<NewVariables>
  ) =
    DefaultConnectorGeneratedMutationImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  @com.google.firebase.dataconnect.ExperimentalFirebaseDataConnect
  override fun <NewData> withDataDeserializer(
    dataDeserializer: kotlinx.serialization.DeserializationStrategy<NewData>
  ) =
    DefaultConnectorGeneratedMutationImpl(
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun equals(other: Any?): Boolean =
    other is DefaultConnectorGeneratedMutationImpl<*,*> &&
    other.connector == connector &&
    other.operationName == operationName &&
    other.dataDeserializer == dataDeserializer &&
    other.variablesSerializer == variablesSerializer

  override fun hashCode(): Int =
    java.util.Objects.hash(
      "DefaultConnectorGeneratedMutationImpl",
      connector, operationName, dataDeserializer, variablesSerializer
    )

  override fun toString(): String =
    "DefaultConnectorGeneratedMutationImpl(" +
    "operationName=$operationName, " +
    "dataDeserializer=$dataDeserializer, " +
    "variablesSerializer=$variablesSerializer, " +
    "connector=$connector)"
}



private class AddStudyTaskMutationImpl(
  connector: DefaultConnector
):
  AddStudyTaskMutation,
  DefaultConnectorGeneratedMutationImpl<
      AddStudyTaskMutation.Data,
      AddStudyTaskMutation.Variables
  >(
    connector,
    AddStudyTaskMutation.Companion.operationName,
    AddStudyTaskMutation.Companion.dataDeserializer,
    AddStudyTaskMutation.Companion.variablesSerializer,
  )


private class ChangeGroupMutationImpl(
  connector: DefaultConnector
):
  ChangeGroupMutation,
  DefaultConnectorGeneratedMutationImpl<
      ChangeGroupMutation.Data,
      ChangeGroupMutation.Variables
  >(
    connector,
    ChangeGroupMutation.Companion.operationName,
    ChangeGroupMutation.Companion.dataDeserializer,
    ChangeGroupMutation.Companion.variablesSerializer,
  )


private class DeleteTaskMutationImpl(
  connector: DefaultConnector
):
  DeleteTaskMutation,
  DefaultConnectorGeneratedMutationImpl<
      DeleteTaskMutation.Data,
      DeleteTaskMutation.Variables
  >(
    connector,
    DeleteTaskMutation.Companion.operationName,
    DeleteTaskMutation.Companion.dataDeserializer,
    DeleteTaskMutation.Companion.variablesSerializer,
  )


private class DropCourseMutationImpl(
  connector: DefaultConnector
):
  DropCourseMutation,
  DefaultConnectorGeneratedMutationImpl<
      DropCourseMutation.Data,
      DropCourseMutation.Variables
  >(
    connector,
    DropCourseMutation.Companion.operationName,
    DropCourseMutation.Companion.dataDeserializer,
    DropCourseMutation.Companion.variablesSerializer,
  )


private class EnrollCourseMutationImpl(
  connector: DefaultConnector
):
  EnrollCourseMutation,
  DefaultConnectorGeneratedMutationImpl<
      EnrollCourseMutation.Data,
      EnrollCourseMutation.Variables
  >(
    connector,
    EnrollCourseMutation.Companion.operationName,
    EnrollCourseMutation.Companion.dataDeserializer,
    EnrollCourseMutation.Companion.variablesSerializer,
  )


private class GetCurrentSemesterCoursesQueryImpl(
  connector: DefaultConnector
):
  GetCurrentSemesterCoursesQuery,
  DefaultConnectorGeneratedQueryImpl<
      GetCurrentSemesterCoursesQuery.Data,
      Unit
  >(
    connector,
    GetCurrentSemesterCoursesQuery.Companion.operationName,
    GetCurrentSemesterCoursesQuery.Companion.dataDeserializer,
    GetCurrentSemesterCoursesQuery.Companion.variablesSerializer,
  )


private class GetMyEnrollmentsQueryImpl(
  connector: DefaultConnector
):
  GetMyEnrollmentsQuery,
  DefaultConnectorGeneratedQueryImpl<
      GetMyEnrollmentsQuery.Data,
      Unit
  >(
    connector,
    GetMyEnrollmentsQuery.Companion.operationName,
    GetMyEnrollmentsQuery.Companion.dataDeserializer,
    GetMyEnrollmentsQuery.Companion.variablesSerializer,
  )


private class GetMyTasksQueryImpl(
  connector: DefaultConnector
):
  GetMyTasksQuery,
  DefaultConnectorGeneratedQueryImpl<
      GetMyTasksQuery.Data,
      Unit
  >(
    connector,
    GetMyTasksQuery.Companion.operationName,
    GetMyTasksQuery.Companion.dataDeserializer,
    GetMyTasksQuery.Companion.variablesSerializer,
  )


private class GetStudentProfileQueryImpl(
  connector: DefaultConnector
):
  GetStudentProfileQuery,
  DefaultConnectorGeneratedQueryImpl<
      GetStudentProfileQuery.Data,
      Unit
  >(
    connector,
    GetStudentProfileQuery.Companion.operationName,
    GetStudentProfileQuery.Companion.dataDeserializer,
    GetStudentProfileQuery.Companion.variablesSerializer,
  )


private class ListAllCoursesQueryImpl(
  connector: DefaultConnector
):
  ListAllCoursesQuery,
  DefaultConnectorGeneratedQueryImpl<
      ListAllCoursesQuery.Data,
      Unit
  >(
    connector,
    ListAllCoursesQuery.Companion.operationName,
    ListAllCoursesQuery.Companion.dataDeserializer,
    ListAllCoursesQuery.Companion.variablesSerializer,
  )


private class ListAllGroupsQueryImpl(
  connector: DefaultConnector
):
  ListAllGroupsQuery,
  DefaultConnectorGeneratedQueryImpl<
      ListAllGroupsQuery.Data,
      Unit
  >(
    connector,
    ListAllGroupsQuery.Companion.operationName,
    ListAllGroupsQuery.Companion.dataDeserializer,
    ListAllGroupsQuery.Companion.variablesSerializer,
  )


private class ListGroupsForCourseQueryImpl(
  connector: DefaultConnector
):
  ListGroupsForCourseQuery,
  DefaultConnectorGeneratedQueryImpl<
      ListGroupsForCourseQuery.Data,
      ListGroupsForCourseQuery.Variables
  >(
    connector,
    ListGroupsForCourseQuery.Companion.operationName,
    ListGroupsForCourseQuery.Companion.dataDeserializer,
    ListGroupsForCourseQuery.Companion.variablesSerializer,
  )


private class ToggleTaskCompletionMutationImpl(
  connector: DefaultConnector
):
  ToggleTaskCompletionMutation,
  DefaultConnectorGeneratedMutationImpl<
      ToggleTaskCompletionMutation.Data,
      ToggleTaskCompletionMutation.Variables
  >(
    connector,
    ToggleTaskCompletionMutation.Companion.operationName,
    ToggleTaskCompletionMutation.Companion.dataDeserializer,
    ToggleTaskCompletionMutation.Companion.variablesSerializer,
  )


private class UpdateEnrollmentStatusMutationImpl(
  connector: DefaultConnector
):
  UpdateEnrollmentStatusMutation,
  DefaultConnectorGeneratedMutationImpl<
      UpdateEnrollmentStatusMutation.Data,
      UpdateEnrollmentStatusMutation.Variables
  >(
    connector,
    UpdateEnrollmentStatusMutation.Companion.operationName,
    UpdateEnrollmentStatusMutation.Companion.dataDeserializer,
    UpdateEnrollmentStatusMutation.Companion.variablesSerializer,
  )


private class UpsertStudentProfileMutationImpl(
  connector: DefaultConnector
):
  UpsertStudentProfileMutation,
  DefaultConnectorGeneratedMutationImpl<
      UpsertStudentProfileMutation.Data,
      UpsertStudentProfileMutation.Variables
  >(
    connector,
    UpsertStudentProfileMutation.Companion.operationName,
    UpsertStudentProfileMutation.Companion.dataDeserializer,
    UpsertStudentProfileMutation.Companion.variablesSerializer,
  )


