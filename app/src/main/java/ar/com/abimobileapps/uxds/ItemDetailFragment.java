package ar.com.abimobileapps.uxds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.MailTo;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * A fragment representing a single Item detail screen.
 * This fragment is either contained in a {@link ItemListActivity}
 * in two-pane mode (on tablets) or a {@link ItemDetailActivity}
 * on handsets.
 */
public class ItemDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Contents.ContentsItem mItem;

    WebView webview;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            Contents contents = new Contents(getActivity());
            mItem = contents.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_item_detail, container, false);

        // Show the content as html in a WebView.
        if (mItem != null) {
            getActivity().setTitle(mItem.toString());

            webview = (WebView) rootView.findViewById(R.id.item_detail);

            webview.setWebViewClient(new HelloWebViewClient(getActivity()));

            WebSettings settings = webview.getSettings();
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setBuiltInZoomControls(true);
            settings.setDatabaseEnabled(true);
            settings.setDefaultTextEncodingName("UTF-8");

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                String databasePath = this.getActivity().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
                //noinspection deprecation
                settings.setDatabasePath(databasePath);
            }

            webview.setWebChromeClient(new WebChromeClient() {
                @SuppressWarnings({"deprecation", "NullableProblems"})
                public void onExceededDatabaseQuota(String url, String databaseIdentifier, long currentQuota, long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
                    quotaUpdater.updateQuota(5 * 1024 * 1024);
                }
            });

            File file = new File(getActivity().getFilesDir() + "/" + mItem.id + ".html");
            webview.loadUrl("file://" + file);
        }

        return rootView;
    }

    public boolean onBackPressed() {
        if (webview.copyBackForwardList().getCurrentIndex() > 0) {
            webview.goBack();
            return true;
        }
        else {
            return false;
        }
    }

    private class HelloWebViewClient extends WebViewClient {
        private final WeakReference<Activity> mActivityRef;

        public HelloWebViewClient(Activity activity) {
            mActivityRef = new WeakReference<Activity>(activity);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("mailto:")) {
                final Activity activity = mActivityRef.get();
                if (activity != null) {
                    MailTo mt = MailTo.parse(url);
                    Intent i = newEmailIntent(activity, mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                    activity.startActivity(i);
                    view.reload();
                    return true;
                }
            } else {
                view.loadUrl(url);
            }
            return true;
        }

        @SuppressWarnings("UnusedParameters")
        private Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
            intent.putExtra(Intent.EXTRA_TEXT, body);
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_CC, cc);
            intent.setType("message/rfc822");
            return intent;
        }
    }
}
