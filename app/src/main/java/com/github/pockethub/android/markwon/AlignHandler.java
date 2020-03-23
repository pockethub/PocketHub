package com.github.pockethub.android.markwon;

import android.text.Layout;
import android.text.style.AlignmentSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.RenderProps;
import io.noties.markwon.html.HtmlTag;
import io.noties.markwon.html.tag.SimpleTagHandler;

public class AlignHandler extends SimpleTagHandler {

    @Nullable
    @Override
    public Object getSpans(
            @NonNull MarkwonConfiguration configuration,
            @NonNull RenderProps renderProps,
            @NonNull HtmlTag tag) {

        final Layout.Alignment alignment;
        if (tag.attributes().containsKey("align")) {
            String align = tag.attributes().get("align");
            switch (align == null ? "no-alignment" : align) {
                case "center":
                    alignment = Layout.Alignment.ALIGN_CENTER;
                    break;
                case "right":
                    alignment = Layout.Alignment.ALIGN_OPPOSITE;
                    break;
                case "left":
                default:
                    alignment = Layout.Alignment.ALIGN_NORMAL;
                    break;
            }
        } else {
            alignment = Layout.Alignment.ALIGN_NORMAL;
        }

        return new AlignmentSpan.Standard(alignment);
    }

    @NonNull
    @Override
    public Collection<String> supportedTags() {
        return Arrays.asList("p", "a");
    }
}
