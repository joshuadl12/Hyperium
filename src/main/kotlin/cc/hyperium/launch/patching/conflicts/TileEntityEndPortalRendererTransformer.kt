package cc.hyperium.launch.patching.conflicts

import cc.hyperium.config.Settings
import codes.som.anthony.koffee.assembleBlock
import codes.som.anthony.koffee.insns.jvm._return
import codes.som.anthony.koffee.insns.jvm.getstatic
import codes.som.anthony.koffee.insns.jvm.ifeq
import org.objectweb.asm.tree.ClassNode

class TileEntityEndPortalRendererTransformer : ConflictTransformer {
    override fun getClassName() = "bhl"

    override fun transform(original: ClassNode): ClassNode {
        original.methods.find {
            it.name == "renderTileEntityAt"
        }?.apply {
            val (dontRender) = assembleBlock {
                getstatic(Settings::class, "DISABLE_END_PORTALS", boolean)
                ifeq(L["1"])
                _return
                +L["1"]
            }

            instructions.insertBefore(instructions.first, dontRender)
        }

        return original
    }
}
