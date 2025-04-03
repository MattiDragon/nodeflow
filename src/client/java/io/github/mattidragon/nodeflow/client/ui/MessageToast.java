package io.github.mattidragon.nodeflow.client.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MessageToast implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
    private final Text title;
    private Visibility visibility = Visibility.SHOW;

    public MessageToast(Text title) {
        this.title = title;
    }

    @Override
    public Visibility getVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        visibility = time >= 2500L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, this.getWidth(), this.getHeight());
        var text = textRenderer.wrapLines(title, 140);
        if (text.size() == 1) {
            context.drawText(textRenderer, text.getFirst(), 7, 13, 0xffffffff, false);
        } else {
            for (var i = 0; i < text.size(); ++i) {
                context.drawText(textRenderer, text.get(i), 7, (7 + i * 12), 0xffffffff, false);
            }
        }
    }
}
