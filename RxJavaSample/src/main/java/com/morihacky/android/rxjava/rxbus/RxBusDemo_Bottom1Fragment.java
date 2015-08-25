package com.morihacky.android.rxjava.rxbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.morihacky.android.rxjava.BaseFragment;
import com.morihacky.android.rxjava.MainActivity;
import com.morihacky.android.rxjava.R;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static rx.android.app.AppObservable.bindFragment;
import static rx.android.app.AppObservable.bindSupportFragment;

public class RxBusDemo_Bottom1Fragment
      extends BaseFragment {

    @InjectView(R.id.demo_rxbus_tap_txt) TextView _tapEventTxtShow;
    private RxBus _rxBus;
    private CompositeSubscription _subscriptions;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_rxbus_bottom, container, false);
        ButterKnife.inject(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        _rxBus = ((MainActivity) getActivity()).getRxBusSingleton();
    }

    @Override
    public void onStart() {
        super.onStart();
        _subscriptions = new CompositeSubscription();

        _subscriptions//
              .add(bindSupportFragment(this, _rxBus.toObserverable())//
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object event) {
                            if (event instanceof RxBusDemoFragment.TapEvent) {
                                _showTapText();
                            }
                        }
                    }));
    }

    @Override
    public void onStop() {
        super.onStop();
        _subscriptions.unsubscribe();
    }

    private void _showTapText() {
        _tapEventTxtShow.setVisibility(View.VISIBLE);
        _tapEventTxtShow.setAlpha(1f);
        ViewCompat.animate(_tapEventTxtShow).alphaBy(-1f).setDuration(400);
    }
}
