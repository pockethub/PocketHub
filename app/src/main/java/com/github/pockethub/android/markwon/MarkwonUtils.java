package com.github.pockethub.android.markwon;

import android.content.Context;

import androidx.annotation.NonNull;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.HtmlBlock;
import org.commonmark.node.Node;
import org.commonmark.node.Visitor;
import org.commonmark.parser.Parser;
import org.commonmark.parser.PostProcessor;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonVisitor;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import io.noties.markwon.syntax.Prism4jThemeDefault;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.annotations.PrismBundle;

@PrismBundle(
        includeAll = true
)
public class MarkwonUtils {

    public static Markwon createMarkwon (Context context, String baseUrl) {
        final Prism4j prism4j = new Prism4j(new GrammarLocatorDef());

        return Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TaskListPlugin.create(context))
                .usePlugin(HtmlPlugin.create())
                .usePlugin(new AbstractMarkwonPlugin() {

                    @Override
                    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
                        builder.on(FencedCodeBlock.class, (visitor, fencedCodeBlock) -> {
                            // We actually won't be applying code spans here, as our custom view will
                            // draw background and apply mono typeface
                            //
                            // NB the `trim` operation on literal (as code will have a new line at the end)
                            final CharSequence code = visitor.configuration()
                                    .syntaxHighlight()
                                    .highlight(fencedCodeBlock.getInfo(), fencedCodeBlock.getLiteral().trim());
                            visitor.builder().append(code);
                        });
                    }

                    @Override
                    public void configureParser(@NonNull Parser.Builder builder) {
                        super.configureParser(builder);
                        builder.postProcessor(new PostProcessor() {
                            @Override
                            public Node process(Node node) {
                                Visitor t = new AbstractVisitor() {
                                    @Override
                                    public void visit(HtmlBlock htmlBlock) {
                                        String literal = htmlBlock.getLiteral();
                                        if (literal.startsWith("<!--")) {
                                            htmlBlock.unlink();
                                        } else {
                                            super.visit(htmlBlock);
                                        }
                                    }
                                };
                                node.accept(t);
                                return node;
                            }
                        });
                    }
                })
                .usePlugin(GlideImagesPlugin.create(new GifAwareGlideStore(context)))
                .usePlugin(new SpanLinkPlugin(baseUrl))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configure(@NonNull Registry registry) {
                        registry.require(HtmlPlugin.class, htmlPlugin -> htmlPlugin.addHandler(new AlignHandler()));
                    }
                })
                .usePlugin(TableEntryPlugin.create(TablePlugin.create(context)))
                .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDefault.create()))
                .usePlugin(new AsyncDrawableSchedulerPlugin())
                .build();
    }

}
