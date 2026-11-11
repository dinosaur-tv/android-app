package com.dinotv.home

/**
 * What a home-screen widget asks the console to do. The console already knows how to sign
 * requests, pick the screen and save; a widget only opens it and presses the same button a
 * finger would. Nothing crosses back into the app but the value a script returns.
 */
enum class WidgetAction(val id: String) {
    MUSIC("music"),
    MUSIC_PREVIOUS("music.previous"),
    MUSIC_TOGGLE("music.toggle"),
    MUSIC_NEXT("music.next"),
    REMOTE("remote"),
    NIGHT_TOGGLE("night.toggle");

    /** A fixed script per action: no text from an intent ever lands in JavaScript. */
    val script: String get() = when (this) {
        MUSIC -> whenReady("open('music');")
        MUSIC_PREVIOUS -> whenReady("open('music', function(){ press('[data-music=previous]'); });")
        MUSIC_TOGGLE -> whenReady("open('music', function(){ press('[data-music=toggle]'); });")
        MUSIC_NEXT -> whenReady("open('music', function(){ press('[data-music=next]'); });")
        REMOTE -> whenReady("open('remote');")
        // The switch has two buttons, one of them lit once the state arrives; press the other.
        NIGHT_TOGGLE -> whenReady(
            "open('more', function(){ var row = document.querySelector('[data-sleep-enabled]');" +
                " if (row && row.closest('details')) { row.closest('details').open = true; row.scrollIntoView({block: 'center'}); }" +
                " press('[data-sleep-enabled]:not(.active)', '[data-sleep-enabled].active'); });"
        )
    }

    companion object {
        fun from(id: String?): WidgetAction? = entries.firstOrNull { it.id == id }

        /** "не гасить" or "01:00–08:00" as the console shows it; empty until it has loaded. */
        const val NIGHT_STATE_SCRIPT =
            "(function(){var v=document.querySelector('#sleepValue');" +
                "var on=document.querySelector('[data-sleep-enabled].active');" +
                "return on && v ? v.textContent : '';})()"

        /**
         * Waits for a signed-in console with its dock showing, then runs [body].
         * `open(tab, then)` presses the tab until its pane has stayed open for a moment: while
         * the console is still loading it switches back to its own first tab on its own.
         * `press(sel, ready)` waits until [sel] can be clicked (and [ready], when given, exists) —
         * buttons stay disabled until the television reports what is playing.
         */
        private fun whenReady(body: String) =
            "(function(){var start=Date.now();" +
                "function later(f){if(Date.now()-start<15000)setTimeout(f,250);}" +
                "function open(name,then){var steady=0;(function check(){" +
                "var pane=document.querySelector('#pane-'+name);var b=document.querySelector('[data-tab='+name+']');" +
                "if(pane&&!pane.hidden){if(++steady>=4){if(then)then();return;}}" +
                "else{steady=0;if(b&&!b.hidden)b.click();}later(check);})();}" +
                "function press(sel,ready){(function tryIt(){var b=document.querySelector(sel);" +
                "if(b&&!b.disabled&&(!ready||document.querySelector(ready))){b.click();return;}later(tryIt);})();}" +
                "(function go(){var signin=document.querySelector('#pane-signin');if(signin&&!signin.hidden)return;" +
                "var dock=document.querySelector('.dock');if(!dock||dock.hidden){later(go);return;}" +
                body + "})();})();"
    }
}
