package com.app.designmore.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.utils.DensityUtil;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created by Joker on 2015/8/25.
 */
public class RetrieveActivity extends BaseActivity {

  private static final String TAG = RetrieveActivity.class.getSimpleName();

  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.retrieve_layout_phone_et) EditText phoneEt;
  @Nullable @Bind(R.id.retrieve_layout_name_clear_btn) ImageView phoneClearBtn;
  @Nullable @Bind(R.id.retrieve_layout_code_et) EditText codeEt;
  @Nullable @Bind(R.id.retrieve_layout_code_btn) Button codeBtn;
  @Nullable @Bind(R.id.retrieve_layout_password_et) EditText passwordEt;
  @Nullable @Bind(R.id.retrieve_layout_confirm_et) EditText confirmEt;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.retrieve_layout);

    RetrieveActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    RetrieveActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.rightMargin = DensityUtil.getActionBarSize(RetrieveActivity.this);
    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("找回密码");
  }

  @Nullable @OnClick(R.id.retrieve_layout_retrieve_btn) void onRetrieveClick() {
  }

  @Override public void exit() {
    RetrieveActivity.this.finish();
  }
}
