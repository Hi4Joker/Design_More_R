package com.app.designmore.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import butterknife.Bind;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.entity.JournalEntity;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.ProgressLayout;
import com.app.designmore.view.dialog.CustomShareDialog;

/**
 * Created by Joker on 2015/9/15.
 */
public class JournalDetailActivity extends BaseActivity implements CustomShareDialog.Callback {

  private static final String TAG = JournalDetailActivity.class.getCanonicalName();
  private static final String ENTITY = "ENTITY";

  @Nullable @Bind(R.id.journal_detail_layout_root_view) FrameLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;

  @Nullable @Bind(R.id.journal_detail_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.journal_detail_layout_wv) WebView webView;

  private CustomShareDialog customShareDialog;
  private WebSettings webSettings;
  private JournalEntity journalEntity;

  class MyWebViewClient extends WebViewClient {

    @Override public boolean shouldOverrideUrlLoading(WebView webView, String url) {
      webView.loadUrl(url);
      return true;
    }

    @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {

      /*开始加载显示进度条*/
      progressLayout.showLoading();
      super.onPageStarted(view, url, favicon);
    }

    @Override public void onPageFinished(WebView view, String url) {

      /*加载完毕，显示内容*/
      progressLayout.showContent();
      super.onPageFinished(view, url);
    }

    @Override public void onReceivedError(WebView view, int errorCode, String description,
        String failingUrl) {
      if (errorCode == WebViewClient.ERROR_FILE_NOT_FOUND) {
      }
    }
  }

  public static void navigateToJournalDetail(AppCompatActivity startingActivity,
      JournalEntity journalEntity) {
    Intent intent = new Intent(startingActivity, JournalDetailActivity.class);
    intent.putExtra(ENTITY, journalEntity);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.journal_detail_layout);

    JournalDetailActivity.this.initView(savedInstanceState);
    JournalDetailActivity.this.loadData();
  }

  @Override public void initView(Bundle savedInstanceState) {

    JournalDetailActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_icon);

    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("杂志详情");

    JournalDetailActivity.this.initWebView();
  }

  private void initWebView() {

    // 获取webView设置
    webSettings = webView.getSettings();

    // 设置默认缩放级别
    webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);

    // 设置出现缩放工具
    webSettings.setBuiltInZoomControls(false);

    // 支持JavaScript功能
    webSettings.setJavaScriptEnabled(true);

    //设置默认显示编码
    webSettings.setDefaultTextEncodingName("UTF-8");

    webView.setWebViewClient(new MyWebViewClient());

    /*屏蔽掉长按事件，因为webView长按时将会调用系统的复制控件*/
    webView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View v) {
        return true;
      }
    });
  }

  private void loadData() {
    this.journalEntity = (JournalEntity) getIntent().getSerializableExtra(ENTITY);
    this.webView.loadUrl(journalEntity.getJournalUrl());
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_single, menu);

    MenuItem shareItem = menu.findItem(R.id.action_inbox);
    shareItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton shareButton =
        (ImageButton) shareItem.getActionView().findViewById(R.id.action_inbox_btn);
    shareButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_share_icon));
    shareItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (customShareDialog == null) {
          customShareDialog = DialogManager.getInstance()
              .showShareDialog(JournalDetailActivity.this, JournalDetailActivity.this);
        }
        customShareDialog.show();
      }
    });

    return true;
  }

  @Override public void exit() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(JournalDetailActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            JournalDetailActivity.this.finish();
          }
        });
  }

  @Override public void onWeiboClick(String content) {

  }

  @Override public void onWechatClick(String content) {

  }
}
