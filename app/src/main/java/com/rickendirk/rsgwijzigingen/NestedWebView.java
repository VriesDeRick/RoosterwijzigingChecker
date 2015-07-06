package com.rickendirk.rsgwijzigingen;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.util.AttributeSet;
import android.webkit.WebView;


public class NestedWebView extends WebView implements NestedScrollingChild {
    // Onderstaande code afkomstig van https://github.com/sockeqwe/mosby/blob/master/sample-mail/
    // src/main/java/com/hannesdorfmann/mosby/sample/mail/ui/view/NestedScrollingRecyclerView.java

    private final NestedScrollingChildHelper helper = new NestedScrollingChildHelper(this);

    public NestedWebView(Context context) {
        super(context);
    }

    public void setNestedScrollingEnabled(boolean enabled) {
        helper.setNestedScrollingEnabled(enabled);
    }
    public NestedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public boolean isNestedScrollingEnabled() {
        return helper.isNestedScrollingEnabled();
    }

    public boolean startNestedScroll(int axes) {
        return helper.startNestedScroll(axes);
    }

    public void stopNestedScroll() {
        helper.stopNestedScroll();
    }

    public boolean hasNestedScrollingParent() {
        return helper.hasNestedScrollingParent();
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {

        return helper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, offsetInWindow);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return helper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return helper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return helper.dispatchNestedPreFling(velocityX, velocityY);
    }
}


