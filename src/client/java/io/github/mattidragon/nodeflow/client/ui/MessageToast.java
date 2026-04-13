package io.github.mattidragon.nodeflow.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class MessageToast implements Toast {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("toast/advancement");
    private final Component title;
    private Visibility visibility = Visibility.SHOW;

    public MessageToast(Component title) {
        this.title = title;
    }

    @Override
    public Visibility getWantedVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        visibility = time >= 2500L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, Font font, long startTime) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.width(), this.height());
        var text = font.split(title, 140);
        if (text.size() == 1) {
            context.text(font, text.getFirst(), 7, 13, 0xffffffff, false);
        } else {
            for (var i = 0; i < text.size(); ++i) {
                context.text(font, text.get(i), 7, (7 + i * 12), 0xffffffff, false);
            }
        }
    }
}
