package tech.pmman.csutility.core.gameObject;

public interface GameObject extends BaseGameObject {
    /**
     * 返回游戏对象id
     *
     * @return 对象id
     */
    int getId();

    /**
     * 返回当前对象是否已被移除
     *
     * @return 是否已被移除
     */
    boolean isRemoved();
}
