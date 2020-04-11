package com.github.pockethub.android.markwon;

import android.text.style.URLSpan;

import androidx.annotation.NonNull;

import org.commonmark.node.Image;
import org.commonmark.node.Link;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.SpanFactory;
import io.noties.markwon.core.CoreProps;
import io.noties.markwon.image.ImageProps;
import io.noties.markwon.urlprocessor.UrlProcessor;
import io.noties.markwon.urlprocessor.UrlProcessorRelativeToAbsolute;

public class SpanLinkPlugin extends AbstractMarkwonPlugin {

    private UrlProcessor imageUrlProcessor;
    private UrlProcessor linkUrlProcessor;

    public SpanLinkPlugin(String baseUrl) {
        this.imageUrlProcessor = new UrlProcessorRelativeToAbsolute(String.format(baseUrl, "raw"));
        this.linkUrlProcessor = new UrlProcessorRelativeToAbsolute(String.format(baseUrl, "blob"));
    }


    @Override
    public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
        final SpanFactory imageOrigin = builder.getFactory(Image.class);
        if (imageOrigin != null) {
            builder.setFactory(Image.class, (configuration, props) -> {
                String dest = ImageProps.DESTINATION.require(props);
                ImageProps.DESTINATION.set(props, imageUrlProcessor.process(dest));
                return new Object[]{
                        imageOrigin.getSpans(configuration, props),
                        new URLSpan(linkUrlProcessor.process(dest))
                };
            });
        }

        final SpanFactory linkOrigin = builder.getFactory(Link.class);
        if (linkOrigin != null) {
            builder.setFactory(Link.class, (configuration, props) -> {
                String dest = CoreProps.LINK_DESTINATION.require(props);
                CoreProps.LINK_DESTINATION.set(props, linkUrlProcessor.process(dest));
                return new Object[]{ linkOrigin.getSpans(configuration, props) };
            });
        }
    }
}
