package io.github.mattidragon.nodeflow.client.mixin;

import io.github.mattidragon.nodeflow.client.ui.widget.NodeWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AbstractWidget.class)
public class AbstractWidgetMixin {
    @SuppressWarnings({"UnreachableCode", "ConstantValue"})
    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;containsPointInScissor(II)Z"))
    private void tweakScissorCheckForNodes(Args args) {
        if (!((Object) this instanceof NodeWidget node)) return;
        var editor = node.getParent().getArea();
        args.setAll((int) editor.reverseModifyX(args.<Integer>get(0)), (int) editor.reverseModifyY(args.<Integer>get(1)));
    }

    @SuppressWarnings({"UnreachableCode", "ConstantValue"})
    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/WidgetTooltipHolder;refreshTooltipForNextRenderPass(Lnet/minecraft/client/gui/GuiGraphics;IIZZLnet/minecraft/client/gui/navigation/ScreenRectangle;)V"))
    private void tweakTooltipMousePos(Args args) {
        if (!((Object) this instanceof NodeWidget node)) return;
        var editor = node.getParent().getArea();
        args.set(1, (int) editor.reverseModifyX(args.<Integer>get(1)));
        args.set(2, (int) editor.reverseModifyY(args.<Integer>get(2)));
    }
}
