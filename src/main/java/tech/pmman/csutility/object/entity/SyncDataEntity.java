package tech.pmman.csutility.object.entity;

public interface SyncDataEntity {
    /**
     * 用于表示实体同步数据是否传输完毕
     * @return 是否传输完毕
     */
    boolean isReady();
}
