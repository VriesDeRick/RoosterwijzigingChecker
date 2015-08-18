package com.rickendirk.rsgwijzigingen;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class WebFragment extends Fragment {

    NestedWebView webView;
    private boolean isLoading = false;
    private boolean isFinished = false;
    boolean is1eKeerGenegeerd = false;
    ProgressBar progressBar;
    private ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;
    private SwipeRefreshLayout swipeLayout;
    // Delen hiervan afkomstig van 2e antw op http://stackoverflow.com/questions/24923021/
    // swiperefreshlayout-tab-layout-webview-cant-scroll-up

    @Override
    public void onStart() {
        swipeLayout.getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener =
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (webView.getScrollY() == 0){
                            swipeLayout.setEnabled(true);
                        } else swipeLayout.setEnabled(false);
                    }
                });
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.fragment_web, container, false);
        if (savedInstanceState != null){
            is1eKeerGenegeerd = true;
        }
        progressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);

        webView = (NestedWebView) mainView.findViewById(R.id.webView);
        webView.setNestedScrollingEnabled(true);
        WebSettings webSettings = webView.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                isLoading = true;
                isFinished = false;
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                isFinished = true;
                isLoading = false;
                //Ondestaande if voorkomt weergeven "pagina is vernieuwd" bij oriëntatie-verandering
                if (is1eKeerGenegeerd) {
                    is1eKeerGenegeerd = false;
                } else
                    Toast.makeText(getActivity(), "Pagina is vernieuwd", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                swipeLayout.setRefreshing(false);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }
        });
        swipeLayout = (SwipeRefreshLayout) mainView.findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swipeLayout.setColorSchemeResources(R.color.gmailRood, R.color.orange,
                R.color.lighter_blue, R.color.green);

        setRetainInstance(true);
        return mainView;

    }
    public void refresh(){
        webView.loadUrl("http://www.rsgtrompmeesters.nl/roosters/roosterwijzigingen/Lijsterbesstraat/subst_001.htm");
        swipeLayout.setRefreshing(true);
    }
    public boolean isFinished(){
        return isFinished;
    }
    public boolean isLoading(){
        return isLoading;
    }
    //Onderstaand voor veranderen oriëntatie apparaat

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    public void onStop() {
        swipeLayout.getViewTreeObserver().removeOnScrollChangedListener(onScrollChangedListener);
        super.onStop();
    }
}