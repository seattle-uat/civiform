package services.program;

import static com.google.common.base.Preconditions.checkNotNull;

import akka.japi.Pair;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import models.DisplayMode;
import models.VersionModel;
import repository.VersionRepository;

/**
 * A data class storing the current active and draft programs. For efficient querying of information
 * about current active / draft programs. Lifespan should be measured in milliseconds - seconds at
 * the maximum - within one request serving path - because it does not have any mechanism for a
 * refresh.
 */
public final class ActiveAndDraftPrograms {

  private final ImmutableList<ProgramDefinition> activePrograms;
  private final ImmutableList<ProgramDefinition> draftPrograms;
  private final ImmutableMap<String, Pair<Optional<ProgramDefinition>, Optional<ProgramDefinition>>>
      versionedByName;

  enum ActiveAndDraftProgramsType {
    DISABLED,
    INUSE,
    ALL
  }

  /**
   * Queries the existing active and draft versions and builds a snapshotted view of the program
   * state. Since a ProgramService argument is included, we will get the full program definition,
   * which includes the question definitions.
   */
  public static ActiveAndDraftPrograms buildFromCurrentVersionsSynced(
      ProgramService service, VersionRepository repository) {
    return new ActiveAndDraftPrograms(repository, Optional.of(service), ActiveAndDraftProgramsType.ALL);
  }

  /**
   * Queries the existing active and draft versions and builds a snapshotted view of the program
   * state. These programs won't include the question definition, since ProgramService is not
   * provided.
   */
  public static ActiveAndDraftPrograms buildFromCurrentVersionsUnsynced(
      VersionRepository repository) {
    return new ActiveAndDraftPrograms(repository, Optional.empty(), ActiveAndDraftProgramsType.INUSE);
  }

  /**
   * Queries the existing active and draft and disabled versions and builds a snapshotted view of
   * the program state. These programs won't include the question definition, since ProgramService
   * is not provided.
   */
  public static ActiveAndDraftPrograms buildFromCurrentVersionsUnsyncedDisabled(
      VersionRepository repository) {
    return new ActiveAndDraftPrograms(repository, Optional.empty(), ActiveAndDraftProgramsType.DISABLED);
  }

  private ImmutableMap<String, ProgramDefinition> mapNameToProgramWithFilter(
      VersionRepository repository,
      Optional<ProgramService> service,
      VersionModel versionModel,
      Optional<DisplayMode> excludeDisplayMode) {
    return repository.getProgramsForVersion(checkNotNull(versionModel)).stream()
        .map(
            program ->
                service.isPresent()
                    ? getFullProgramDefinition(service.get(), program.id)
                    : program.getProgramDefinition())
        .filter(
            program ->
                excludeDisplayMode.isPresent()
                    ? program.displayMode() != excludeDisplayMode.get()
                    : true)
        .collect(ImmutableMap.toImmutableMap(ProgramDefinition::adminName, Function.identity()));
  }

  private ImmutableMap<String, ProgramDefinition> mapNameToProgram(
      VersionRepository repository, Optional<ProgramService> service, VersionModel versionModel) {
    return mapNameToProgramWithFilter(repository, service, versionModel, Optional.empty());
  }

  private ActiveAndDraftPrograms(
    VersionRepository repository, Optional<ProgramService> service,ActiveAndDraftProgramsType type) {
    VersionModel active = repository.getActiveVersion();
    VersionModel draft = repository.getDraftVersionOrCreate();
    // Note: Building this lookup has N+1 query behavior since a call to getProgramDefinition does
    // an additional database lookup in order to sync the set of questions associated with the
    // program.
    ImmutableMap<String, ProgramDefinition> activeNameToProgram =
      mapNameToProgramWithFilter(repository, service, active, Optional.of(DisplayMode.DISABLED));

    ImmutableMap<String, ProgramDefinition> activeNameToProgramAll =
      mapNameToProgram(repository, service, active);

    ImmutableMap<String, ProgramDefinition> draftNameToProgram =
      mapNameToProgramWithFilter(repository, service, draft, Optional.of(DisplayMode.DISABLED));

    ImmutableMap<String, ProgramDefinition> draftNameToProgramAll =
      mapNameToProgram(repository, service, draft);

    ImmutableMap<String, ProgramDefinition> disabledActiveNameToProgram =
      mapNameToProgram(repository, service, active);

    ImmutableMap<String, ProgramDefinition> disabledDraftNameToProgram =
      mapNameToProgram(repository, service, draft);
    switch(type) {
      case INUSE:
        this.activePrograms = ImmutableList.copyOf(activeNameToProgram.values());
        this.draftPrograms = ImmutableList.copyOf(draftNameToProgram.values());
        this.versionedByName = createVersionedByNameMap(activeNameToProgram, draftNameToProgram);
        break;
      case DISABLED:
        this.activePrograms = ImmutableList.copyOf(activeNameToProgram.values());
        this.draftPrograms = ImmutableList.copyOf(draftNameToProgram.values());
        // Pass only the maps, not the sets, as that's what the method expects.
        this.versionedByName = createVersionedByNameMap(disabledActiveNameToProgram, disabledDraftNameToProgram);
        break;
      case ALL:
        this.activePrograms = ImmutableList.copyOf(activeNameToProgramAll.values());
        this.draftPrograms = ImmutableList.copyOf(draftNameToProgramAll.values());
        this.versionedByName = createVersionedByNameMap(activeNameToProgramAll, draftNameToProgramAll);
        break;
      default:
        throw new IllegalArgumentException("Unsupported ActiveAndDraftProgramsType: " + type);
    }
  }

  private ImmutableMap<String, Pair<Optional<ProgramDefinition>, Optional<ProgramDefinition>>> createVersionedByNameMap(
    ImmutableMap<String, ProgramDefinition> activeNameToProgram,
    ImmutableMap<String, ProgramDefinition> draftNameToProgram) {
    Set<String> allProgramNames = Sets.union(activeNameToProgram.keySet(), draftNameToProgram.keySet());

    return allProgramNames.stream()
      .collect(ImmutableMap.toImmutableMap(
        Function.identity(),
        programName -> Pair.create(
          Optional.ofNullable(activeNameToProgram.get(programName)),
          Optional.ofNullable(draftNameToProgram.get(programName))
        )
      ));
  }

  public ImmutableList<ProgramDefinition> getActivePrograms() {
    return activePrograms;
  }

  public ImmutableList<ProgramDefinition> getDraftPrograms() {
    return draftPrograms;
  }

  public ImmutableSet<String> getProgramNames() {
    return versionedByName.keySet();
  }

  public Optional<ProgramDefinition> getActiveProgramDefinition(String name) {
    if (!versionedByName.containsKey(name)) {
      return Optional.empty();
    }

    return versionedByName.get(name).first();
  }

  public Optional<ProgramDefinition> getDraftProgramDefinition(String name) {
    if (!versionedByName.containsKey(name)) {
      return Optional.empty();
    }

    return versionedByName.get(name).second();
  }

  /** Returns the most recent version of the specified program, which may be active or a draft. */
  public ProgramDefinition getMostRecentProgramDefinition(String name) {
    return getDraftProgramDefinition(name).orElseGet(getActiveProgramDefinition(name)::get);
  }

  /**
   * Returns the most recent versions of all the programs, which may be a mix of active and draft.
   */
  public ImmutableList<ProgramDefinition> getMostRecentProgramDefinitions() {
    return getProgramNames().stream()
        .map(this::getMostRecentProgramDefinition)
        .collect(ImmutableList.toImmutableList());
  }

  public boolean anyDraft() {
    return draftPrograms.size() > 0;
  }

  private ProgramDefinition getFullProgramDefinition(ProgramService service, long id) {
    try {
      return service.getFullProgramDefinition(id);
    } catch (ProgramNotFoundException e) {
      // This is not possible because we query with existing program ids.
      throw new RuntimeException(e);
    }
  }
}
