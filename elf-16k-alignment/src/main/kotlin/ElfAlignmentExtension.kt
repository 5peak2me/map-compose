import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class ElfAlignmentExtension @Inject constructor(objects: ObjectFactory) {

    /**
     * 最大对齐数
     */
    val maxAlign: Property<Long> = objects.property(Long::class.java).convention(16384L)

    /**
     * 过滤未对齐的原生库
     */
    val filter: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    val resoleOnBuild: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    val output: ElfAlignmentOutputExtension = objects.newInstance(ElfAlignmentOutputExtension::class.java)

    fun output(action: Action<ElfAlignmentOutputExtension>) {
        action.execute(output)
    }

}

abstract class ElfAlignmentOutputExtension @Inject constructor(objects: ObjectFactory) {
    val csv: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val html: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val json: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val md: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
}