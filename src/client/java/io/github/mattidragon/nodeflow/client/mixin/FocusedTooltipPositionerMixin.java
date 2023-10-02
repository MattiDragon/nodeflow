package io.github.mattidragon.nodeflow.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.mattidragon.nodeflow.client.ui.widget.NodeWidget;
import net.minecraft.client.gui.tooltip.FocusedTooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(FocusedTooltipPositioner.class)
public class FocusedTooltipPositionerMixin {
    @Shadow @Final private ClickableWidget widget;

    @ModifyExpressionValue(method = "getPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ClickableWidget;getX()I"))
    private int nodeflow$tweakWidgetX(int original) {
        if (widget instanceof NodeWidget node)
            return (int) node.getParent().getArea().reverseModifyX(original);
        return original;
    }

    @ModifyExpressionValue(method = "getPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ClickableWidget;getY()I"))
    private int nodeflow$tweakWidgetY(int original) {
        if (widget instanceof NodeWidget node)
            return (int) node.getParent().getArea().reverseModifyY(original);
        return original;
    }

    @ModifyExpressionValue(method = "getPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ClickableWidget;getWidth()I"))
    private int nodeflow$tweakWidgetWidth(int original) {
        if (widget instanceof NodeWidget node)
            return (int) node.getParent().getArea().reverseModifyDeltaX(original);
        return original;
    }

    @ModifyExpressionValue(method = "getPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ClickableWidget;getHeight()I"))
    private int nodeflow$tweakWidgetHeight(int original) {
        if (widget instanceof NodeWidget node)
            return (int) node.getParent().getArea().reverseModifyDeltaY(original);
        return original;
    }
}
