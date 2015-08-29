package com.app.designmore.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created by Joker on 2015/8/25.
 */
public class RegisterActivity extends RxAppCompatActivity {

  private static final String TAG = RegisterActivity.class.getSimpleName();

  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.register_layout_name_et) EditText userNameEt;
  @Nullable @Bind(R.id.register_layout_name_clear_btn) ImageView clearNameBtn;
  @Nullable @Bind(R.id.register_layout_password_et) EditText passwordEt;
  @Nullable @Bind(R.id.register_layout_phone_et) EditText phoneEt;
  @Nullable @Bind(R.id.register_layout_code_et) EditText codeEt;
  @Nullable @Bind(R.id.register_layout_code_btn) Button codeBtn;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.register_layout);
    ButterKnife.bind(RegisterActivity.this);

    RegisterActivity.this.initView();
  }

  private void initView() {

    RegisterActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    toolbarTitleTv.setText("注 册");
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        RegisterActivity.this.finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Nullable @OnClick(R.id.register_layout_register_btn) void onRegisterClick() {
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    ButterKnife.unbind(RegisterActivity.this);
  }
}
