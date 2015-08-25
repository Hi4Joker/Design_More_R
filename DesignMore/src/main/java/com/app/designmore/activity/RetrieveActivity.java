package com.app.designmore.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created by Joker on 2015/8/25.
 */
public class RetrieveActivity extends RxAppCompatActivity {

  private static final String TAG = RetrieveActivity.class.getSimpleName();
  @Nullable @Bind(R.id.retrieve_layout_toolbar_back_btn) ImageView backBtn;
  @Nullable @Bind(R.id.register_layout_name_clear_btn) ImageView phoneClearBtn;
  @Nullable @Bind(R.id.register_layout_phone_et) EditText phoneEt;
  @Nullable @Bind(R.id.retrieve_layout_code_et) EditText codeEt;
  @Nullable @Bind(R.id.retrieve_layout_code_btn) Button codeBtn;
  @Nullable @Bind(R.id.retrieve_layout_password_et) EditText passwordEt;
  @Nullable @Bind(R.id.retrieve_layout_confirm_et) EditText confirmEt;
  @Nullable @Bind(R.id.retrieve_layout_retrieve_btn) Button retrieveBtn;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.retrieve_layout);

    ButterKnife.bind(RetrieveActivity.this);
  }


  @Nullable @OnClick(R.id.retrieve_layout_toolbar_back_btn) void onBackClick() {
    RetrieveActivity.this.finish();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(RetrieveActivity.this);
  }
}
