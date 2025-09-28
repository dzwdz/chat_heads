package dzwdz.chat_heads;

import com.mojang.blaze3d.font.GlyphInfo;

// adds one pixel of padding when used inside the chat
public class PaddedChatGlyph implements GlyphInfo {
    public GlyphInfo glyphInfo;

    public PaddedChatGlyph(GlyphInfo original) {
        this.glyphInfo = original;
    }

    @Override
    public float getAdvance() {
        return glyphInfo.getAdvance() + (ChatHeads.insideChat ? 1 : 0);
    }
}
