package com.morihacky.android.rxjava.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.morihacky.android.rxjava.R;
import com.morihacky.android.rxjava.rxbus.RxBusDemoFragment;

public class MainFragment
      extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @OnClick(R.id.btn_demo_schedulers)
    void demoConcurrencyWithSchedulers() {
        clickedOn(new ConcurrencyWithSchedulersDemoFragment());
    }

    @OnClick(R.id.btn_demo_buffer)
    void demoBuffer() {
        clickedOn(new BufferDemoFragment());
    }

    @OnClick(R.id.btn_demo_debounce)
    void demoThrottling() {
        clickedOn(new DebounceSearchEmitterFragment());
    }

    @OnClick(R.id.btn_demo_retrofit)
    void demoRetrofitCalls() {
        clickedOn(new RetrofitFragment());
    }

    @OnClick(R.id.btn_demo_double_binding_textview)
    void demoDoubleBindingWithPublishSubject() {
        clickedOn(new DoubleBindingTextViewFragment());
    }

    @OnClick(R.id.btn_demo_rxbus)
    void demoRxBus() {
        clickedOn(new RxBusDemoFragment());
    }

    @OnClick(R.id.btn_demo_form_validation_combinel)
    void formValidation() {
        clickedOn(new FormValidationCombineLatestFragment());
    }

    @OnClick(R.id.btn_demo_pseudo_cache)
    void pseudoCacheDemo() {
        clickedOn(new PseudoCacheMergeFragment());
    }

    @OnClick(R.id.btn_demo_timing)
    void demoTimerIntervalDelays() {
        clickedOn(new TimingDemoFragment());
    }

    @OnClick(R.id.btn_demo_exponential_backoff)
    void demoExponentialBackoff() {
        clickedOn(new ExponentialBackoffFragment());
    }

    @OnClick(R.id.btn_demo_rotation_persist)
    void demoRotationPersist() {
        clickedOn(new RotationPersist2Fragment());
        //clickedOn(new RotationPersist1Fragment());
    }

    private void clickedOn(@NonNull Fragment fragment) {
        final String tag = fragment.getClass().toString();
        getActivity().getSupportFragmentManager()
              .beginTransaction()
              .addToBackStack(tag)
              .replace(android.R.id.content, fragment, tag)
              .commit();
    }
}
