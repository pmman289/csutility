package tech.pmman.csutility.client.sound;

public interface IChannelExtension {
    /** 延迟 seek（秒） */
    void syncedSound$seek(float second);

    /** 设置基础音量（SoundInstance.volume） */
    void syncedSound$setBaseVolume(float volume);

    /** 设置 3D 衰减参数 */
    void syncedSound$setupAttenuation(float maxDistance);
}
