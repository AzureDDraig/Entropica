package ddraig.net.entropica.codex;

import ddraig.net.entropica.codex.CodexWikiMetadata.WikiArticle;
import ddraig.net.entropica.data.CodexArticles;

/**
 * Expands rich technical specifications from {@link CodexArticles} into
 * matching Wiki articles and normalizes light Markdown styling for the
 * Entropic Codex Two-Page Tome Reader.
 */
public final class CodexArticlesBridge {

    private CodexArticlesBridge() {
    }

    public static String getExpandedContent(WikiArticle article) {
        if (article == null) return "";
        String base = article.content();

        if ("magic_rituals".equals(article.id())) {
            base = base + "\n\n" + CodexArticles.MAGIC_CIRCLES_ARTICLE + "\n\n" + CodexArticles.MAGIC_CIRCUITS_ARTICLE;
        }

        // Convert **Bold** markers to §f§l..§r§7 cleanly for Minecraft font rendering
        return base
                .replaceAll("\\*\\*([^*]+)\\*\\*", "§f$1§r")
                .replace("▪ ", "• ");
    }
}
