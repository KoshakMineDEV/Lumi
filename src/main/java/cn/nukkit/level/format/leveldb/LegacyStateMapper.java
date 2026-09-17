package cn.nukkit.level.format.leveldb;

public interface LegacyStateMapper {

    default int legacyToHashId(int legacyId, int meta) {
        return this.legacyToRuntime(legacyId, meta);
    }

    default int hashIdToFullId(int hashId) {
        return this.runtimeToFullId(hashId);
    }

    default int hashIdToLegacyId(int hashId) {
        return this.runtimeToLegacyId(hashId);
    }

    default int hashIdToLegacyData(int hashId) {
        return this.runtimeToLegacyData(hashId);
    }

    int legacyToRuntime(int legacyId, int meta);

    int runtimeToFullId(int runtimeId);

    int runtimeToLegacyId(int runtimeId);

    int runtimeToLegacyData(int runtimeId);

}
