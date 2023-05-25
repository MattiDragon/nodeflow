package io.github.mattidragon.nodeflow.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

public class MessageToast implements Toast {
    private final Text title;

    public MessageToast(Text title) {
        this.title = title;
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        context.drawTexture(TEXTURE, 0, 0, 0, 0, this.getWidth(), this.getHeight());
        var text = manager.getClient().textRenderer.wrapLines(title, 140);
        if (text.size() == 1) {
            context.drawText(manager.getClient().textRenderer, text.get(0), 7, 13, 0xffffffff, false);
        } else {
            for (var i = 0; i < text.size(); ++i) {
                context.drawText(manager.getClient().textRenderer, text.get(i), 7, (7 + i * 12), 0xffffffff, false);
            }
        }
        return startTime >= 2500L ? Visibility.HIDE : Visibility.SHOW;
    }
}
